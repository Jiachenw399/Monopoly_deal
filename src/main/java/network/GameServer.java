package network;

import logic.Game;
import model.ActionCardType;
import model.ActionCards;
import model.BuildingPaymentCard;
import model.Card;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class GameServer {
    private static final int PORT = 5555;
    private static final int MIN_PLAYERS = 1;
    private static final int MAX_PLAYERS = 5;
    private static final int TURN_SECONDS = 120;

    private final AtomicInteger nextPlayerId = new AtomicInteger(1);
    private final List<ClientHandler> clients = new ArrayList<>();
    private final Map<String, ActionCommand> actionCommands = createActionCommands();
    private final ScheduledExecutorService turnTimer = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> turnTimeoutTask;
    private long turnDeadlineMillis = -1;
    private boolean gameStarted = false;
    private Game game;

    // Starts the application entry point.
    public static void main(String[] args) {
        new GameServer().start();
    }

    // Starts this operation.
    public void start() {
        System.out.println("Server starting on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is ready. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();

                if (isServerFull()) {
                    rejectClient(clientSocket);
                    continue;
                }

                ClientHandler clientHandler = new ClientHandler(clientSocket, nextPlayerId.getAndIncrement());
                addClient(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    // Checks whether server full.
    private synchronized boolean isServerFull() {
        return clients.size() >= MAX_PLAYERS || gameStarted;
    }

    // Rejects client.
    private void rejectClient(Socket clientSocket) {
        try (
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                Socket socketToClose = clientSocket
        ) {
            String reason = gameStarted
                    ? "Game has already started. Join before Start Game."
                    : "Server already has " + MAX_PLAYERS + " players";
            out.println(new NetworkMessage("FULL", reason).encode());
        } catch (IOException e) {
            System.out.println("Failed to reject client: " + e.getMessage());
        }
    }

    // Adds client.
    private synchronized void addClient(ClientHandler clientHandler) {
        clients.add(clientHandler);
        System.out.println("Player " + clientHandler.getPlayerId() + " connected. Players: " + clients.size());
    }

    // Removes client.
    private synchronized void removeClient(ClientHandler clientHandler) {
        int disconnectedPlayerId = clientHandler.getPlayerId();
        clients.remove(clientHandler);
        System.out.println("Player " + disconnectedPlayerId + " disconnected. Players: " + clients.size());
        handleDisconnectedPlayerDuringGame(disconnectedPlayerId);
    }

    // Handles disconnected player during game.
    private synchronized void handleDisconnectedPlayerDuringGame(int disconnectedPlayerId) {
        if (!isGameStarted()) {
            return;
        }

        int currentPlayerId = game.getCurrentPlayerIndex() + 1;

        if (disconnectedPlayerId != currentPlayerId) {
            return;
        }

        game.forceAdvanceTurnForAbsentPlayer();
        restartTurnTimer();
        broadcast(new NetworkMessage(
                "BROADCAST",
                "Player " + disconnectedPlayerId + " left; turn advanced to Player " + (game.getCurrentPlayerIndex() + 1)
        ));
        sendGameStateToAll();
    }

    // Finds player count.
    private synchronized int getPlayerCount() {
        return clients.size();
    }

    // Finds player list text.
    private synchronized String getPlayerListText() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < clients.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }

            builder.append(clients.get(i).getPlayerName());
        }

        return builder.toString();
    }

    // Broadcasts this operation.
    private synchronized void broadcast(NetworkMessage message) {
        String encodedMessage = message.encode();

        for (ClientHandler client : clients) {
            client.send(encodedMessage);
        }
    }

    // Runs try start game.
    private synchronized boolean tryStartGame(ClientHandler starter) {
        if (gameStarted) {
            starter.send(new NetworkMessage("SERVER", "Game has already started").encode());
            return false;
        }

        if (clients.size() < MIN_PLAYERS) {
            starter.send(new NetworkMessage("SERVER", "Need at least " + MIN_PLAYERS + " players to start").encode());
            return false;
        }

        List<String> playerNames = new ArrayList<>();
        for (ClientHandler client : clients) {
            playerNames.add(client.getPlayerName());
        }

        gameStarted = true;
        game = new Game(clients.size());
        game.startGame(clients.size(), playerNames);
        restartTurnTimer();
        return true;
    }

    // Checks whether game started.
    private synchronized boolean isGameStarted() {
        return gameStarted && game != null;
    }

    // Runs end turn if game started.
    private synchronized void endTurnIfGameStarted(ClientHandler requester) {
        if (!requireGameStarted(requester) || !requireCurrentTurn(requester)) {
            return;
        }

        game.guiEndTurn();
        restartTurnTimer();
        sendGameStateToAll();
    }

    // Sends game state to.
    private synchronized void sendGameStateTo(ClientHandler client) {
        if (!isGameStarted()) {
            client.send(new NetworkMessage("SERVER", "Game has not started yet").encode());
            return;
        }

        cancelTurnTimerIfGameWon();
        client.send(new NetworkMessage(
                "GAME_STATE",
                GameStateCodec.encode(game, client.getPlayerId(), getTurnRemainingSeconds())
        ).encode());
    }

    // Plays card if valid.
    private synchronized void playCardIfValid(ClientHandler requester, String cardNumberText) {
        if (!requireGameStarted(requester) || !requireCurrentTurn(requester)) {
            return;
        }

        int cardIndex = parseOneBasedCardIndex(cardNumberText);

        if (cardIndex < 0) {
            requester.send(new NetworkMessage("SERVER", "Use PLAY_CARD <number>, for example PLAY_CARD 1").encode());
            return;
        }

        Player player = game.getPlayers().get(requester.getPlayerId() - 1);

        if (cardIndex >= player.getHandCards().size()) {
            requester.send(new NetworkMessage("SERVER", "No hand card number " + (cardIndex + 1)).encode());
            return;
        }

        Card selectedCard = player.getHandCards().get(cardIndex);
        String selectedCardText = cardToText(selectedCard);
        boolean success = game.playCard(selectedCard);

        if (!success) {
            requester.send(new NetworkMessage("SERVER", "Could not play " + selectedCardText).encode());
            return;
        }

        broadcast(new NetworkMessage("BROADCAST", "Player " + requester.getPlayerId() + " played " + selectedCardText));
        sendGameStateToAll();
    }

    // Plays action card as money if valid.
    private synchronized void playActionCardAsMoneyIfValid(ClientHandler requester, String cardNumberText) {
        if (!requireGameStarted(requester) || !requireCurrentTurn(requester)) {
            return;
        }

        int cardIndex = parseOneBasedCardIndex(cardNumberText);

        if (cardIndex < 0) {
            requester.send(new NetworkMessage("SERVER", "Use PLAY_AS_MONEY <number>, for example PLAY_AS_MONEY 1").encode());
            return;
        }

        Player player = game.getPlayers().get(requester.getPlayerId() - 1);

        if (player.getUseCardTimes() >= 3) {
            requester.send(new NetworkMessage("SERVER", "You have already played 3 cards this turn").encode());
            return;
        }

        if (cardIndex >= player.getHandCards().size()) {
            requester.send(new NetworkMessage("SERVER", "No hand card number " + (cardIndex + 1)).encode());
            return;
        }

        Card selectedCard = player.getHandCards().get(cardIndex);

        if (!(selectedCard instanceof ActionCards)) {
            requester.send(new NetworkMessage("SERVER", "Only action cards need PLAY_AS_MONEY").encode());
            return;
        }

        String selectedCardText = cardToText(selectedCard);
        player.putMoneyCard(selectedCard);
        player.increaseUseCardTimes();
        broadcast(new NetworkMessage("BROADCAST", "Player " + requester.getPlayerId() + " banked " + selectedCardText));
        sendGameStateToAll();
    }

    // Discards if valid.
    private synchronized void discardIfValid(ClientHandler requester, String cardNumberText) {
        if (!requireGameStarted(requester) || !requireCurrentTurn(requester)) {
            return;
        }

        int cardIndex = parseOneBasedCardIndex(cardNumberText);

        if (cardIndex < 0) {
            requester.send(new NetworkMessage("SERVER", "Use DISCARD <number>, for example DISCARD 1").encode());
            return;
        }

        Player player = game.getPlayers().get(requester.getPlayerId() - 1);

        if (cardIndex >= player.getHandCards().size()) {
            requester.send(new NetworkMessage("SERVER", "No hand card number " + (cardIndex + 1)).encode());
            return;
        }

        Card selectedCard = player.getHandCards().get(cardIndex);
        String selectedCardText = cardToText(selectedCard);
        int currentPlayerIndexBeforeDiscard = game.getCurrentPlayerIndex();

        if (!game.discard(selectedCard)) {
            requester.send(new NetworkMessage("SERVER", "Could not discard " + selectedCardText).encode());
            return;
        }

        restartTurnTimerIfTurnChanged(currentPlayerIndexBeforeDiscard);
        broadcast(new NetworkMessage("BROADCAST", "Player " + requester.getPlayerId() + " discarded " + selectedCardText));
        sendGameStateToAll();
    }

    // Processes if valid.
    private synchronized void payIfValid(ClientHandler requester, String paymentText) {
        if (!isGameStarted()) {
            requester.send(new NetworkMessage("SERVER", "Game has not started yet").encode());
            return;
        }

        if (!game.isPaymentSelecting()) {
            requester.send(new NetworkMessage("SERVER", "No payment is required now").encode());
            return;
        }

        if (game.isCurrentPaymentWaitingForJustSayNoResponse()) {
            requester.send(new NetworkMessage("SERVER", "Resolve Just Say No before paying").encode());
            return;
        }

        Game.PaymentRequest request = game.getCurrentPaymentRequest();
        int payerId = game.getPlayers().indexOf(request.getPayer()) + 1;

        if (requester.getPlayerId() != payerId) {
            requester.send(new NetworkMessage("SERVER", "Player " + payerId + " must pay now").encode());
            return;
        }

        ArrayList<Card> selectedCards = parsePaymentCards(request.getPayer(), paymentText);
        int selectedValue = game.getPaymentCardsValue(request.getPayer(), selectedCards);

        if (selectedCards.isEmpty()) {
            requester.send(new NetworkMessage(
                    "SERVER",
                    "Use PAY B1 HOUSE:BROWN HOTEL:BROWN P2. B means bank card, HOUSE/HOTEL means building, P means property card"
            ).encode());
            return;
        }

        if (!game.finishCurrentPayment(selectedCards)) {
            requester.send(new NetworkMessage("SERVER", "Invalid payment selection").encode());
            return;
        }

        broadcast(new NetworkMessage("BROADCAST", "Player " + requester.getPlayerId() + " paid " + selectedValue + "M"));
        sendGameStateToAll();
    }

    // Runs just say no if valid.
    private synchronized void justSayNoIfValid(ClientHandler requester) {
        if (!isGameStarted()) {
            requester.send(new NetworkMessage("SERVER", "Game has not started yet").encode());
            return;
        }

        if (!game.isPaymentSelecting()) {
            requester.send(new NetworkMessage("SERVER", "No payment is required now").encode());
            return;
        }

        Game.PaymentRequest request = game.getCurrentPaymentRequest();
        int expectedPlayerId = getCurrentJustSayNoPlayerId(request);

        if (requester.getPlayerId() != expectedPlayerId) {
            requester.send(new NetworkMessage("SERVER", "Player " + expectedPlayerId + " must respond now").encode());
            return;
        }

        if (!game.canCurrentPaymentUseJustSayNo()) {
            requester.send(new NetworkMessage("SERVER", "You do not have Just Say No").encode());
            return;
        }

        game.currentPaymentUseJustSayNo();
        broadcast(new NetworkMessage("BROADCAST", "Player " + requester.getPlayerId() + " used Just Say No"));
        sendGameStateToAll();
    }

    // Accepts the latest Just Say No if the responder chooses not to counter.
    private synchronized void passJustSayNoIfValid(ClientHandler requester) {
        if (!isGameStarted()) {
            requester.send(new NetworkMessage("SERVER", "Game has not started yet").encode());
            return;
        }

        if (!game.isPaymentSelecting() || !game.isCurrentPaymentWaitingForJustSayNoResponse()) {
            requester.send(new NetworkMessage("SERVER", "No Just Say No response is required now").encode());
            return;
        }

        Game.PaymentRequest request = game.getCurrentPaymentRequest();
        int responderId = game.getPlayers().indexOf(request.getJustSayNoResponder()) + 1;

        if (requester.getPlayerId() != responderId) {
            requester.send(new NetworkMessage("SERVER", "Player " + responderId + " must respond now").encode());
            return;
        }

        game.currentPaymentPassJustSayNo();
        broadcast(new NetworkMessage("BROADCAST", "Player " + requester.getPlayerId() + " accepted Just Say No"));
        sendGameStateToAll();
    }

    // Runs set property color if valid.
    private synchronized void setPropertyColorIfValid(ClientHandler requester, String body) {
        if (!requireGameStarted(requester) || !requireCurrentTurn(requester)) {
            return;
        }

        String[] args = body.trim().split("\\s+");
        if (args.length < 2) {
            requester.send(new NetworkMessage("SERVER", "Use SET_PROPERTY_COLOR <propertyNo> <color>").encode());
            return;
        }

        Player player = game.getPlayers().get(requester.getPlayerId() - 1);
        PropertiesCards property = getPropertyByOneBasedNumber(player, args[0]);
        PropertyColor color = parseColor(args[1]);

        if (property == null || color == null) {
            requester.send(new NetworkMessage("SERVER", "Invalid property or color").encode());
            return;
        }

        if (!property.isWildCard() || !property.getType().getColors().contains(color)) {
            requester.send(new NetworkMessage("SERVER", "That property cannot use color " + args[1]).encode());
            return;
        }

        if (!game.setPropertyColor(player, property, color)) {
            requester.send(new NetworkMessage(
                    "SERVER",
                    "That color change would break a built complete set"
            ).encode());
            return;
        }

        broadcast(new NetworkMessage("BROADCAST", "Player " + requester.getPlayerId() + " changed a wild card to " + color));
        sendGameStateToAll();
    }

    // Parses payment cards.
    private ArrayList<Card> parsePaymentCards(Player payer, String paymentText) {
        ArrayList<Card> selectedCards = new ArrayList<>();
        String[] tokens = paymentText.trim().split("[,\\s]+");

        for (String token : tokens) {
            if (token.length() < 2) {
                continue;
            }

            Card buildingCard = parseBuildingPaymentCard(token);
            if (buildingCard != null) {
                selectedCards.add(buildingCard);
                continue;
            }

            char source = Character.toUpperCase(token.charAt(0));
            int index = parseOneBasedCardIndex(token.substring(1));

            if (index < 0) {
                continue;
            }

            if (source == 'B' && index < payer.getBankCards().size()) {
                selectedCards.add(payer.getBankCards().get(index));
            } else if (source == 'P' && index < payer.getPropertyCards().size()) {
                selectedCards.add(payer.getPropertyCards().get(index));
            }
        }

        return selectedCards;
    }

    // Parses building payment card.
    private Card parseBuildingPaymentCard(String token) {
        String[] parts = token.split(":", 2);

        if (parts.length != 2) {
            return null;
        }

        try {
            ActionCardType type = ActionCardType.valueOf(parts[0].toUpperCase());
            PropertyColor color = PropertyColor.valueOf(parts[1].toUpperCase());

            if (type == ActionCardType.HOUSE || type == ActionCardType.HOTEL) {
                return new BuildingPaymentCard(type, color);
            }
        } catch (IllegalArgumentException e) {
            return null;
        }

        return null;
    }

    // Parses one based card index.
    private int parseOneBasedCardIndex(String cardNumberText) {
        try {
            return Integer.parseInt(cardNumberText.trim()) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Finishes action if valid.
    private synchronized void finishActionIfValid(ClientHandler requester, String command, String body) {
        if (!requireGameStarted(requester) || !requireCurrentTurn(requester)) {
            return;
        }

        String[] args = body.trim().isEmpty() ? new String[0] : body.trim().split("\\s+");
        ActionCommand actionCommand = actionCommands.get(command);

        if (actionCommand == null) {
            requester.send(new NetworkMessage("SERVER", "Unknown action command: " + command).encode());
            return;
        }

        boolean success = actionCommand.finish(requester, args);

        if (!success) {
            requester.send(new NetworkMessage("SERVER", "Action failed: " + command + " " + body).encode());
            return;
        }

        broadcast(new NetworkMessage("BROADCAST", "Player " + requester.getPlayerId() + " used " + command));
        sendGameStateToAll();
    }

    // Runs require game started.
    private boolean requireGameStarted(ClientHandler requester) {
        if (!isGameStarted()) {
            requester.send(new NetworkMessage("SERVER", "Game has not started yet").encode());
            return false;
        }

        return true;
    }

    // Runs require current turn.
    private boolean requireCurrentTurn(ClientHandler requester) {
        int expectedPlayerId = game.getCurrentPlayerIndex() + 1;

        if (requester.getPlayerId() != expectedPlayerId) {
            requester.send(new NetworkMessage("SERVER", "It is Player " + expectedPlayerId + "'s turn").encode());
            return false;
        }

        return true;
    }

    // Finds the player who can currently use Just Say No.
    private int getCurrentJustSayNoPlayerId(Game.PaymentRequest request) {
        if (request.isJustSayNoPending()) {
            return game.getPlayers().indexOf(request.getJustSayNoResponder()) + 1;
        }

        return game.getPlayers().indexOf(request.getPayer()) + 1;
    }

    // Creates action commands.
    private Map<String, ActionCommand> createActionCommands() {
        Map<String, ActionCommand> commands = new HashMap<>();
        commands.put("BIRTHDAY", this::finishBirthdayCommand);
        commands.put("PASS_GO", this::finishPassGoCommand);
        commands.put("DEBT", this::finishDebtCommand);
        commands.put("SLY", this::finishSlyCommand);
        commands.put("DEAL_BREAKER", this::finishDealBreakerCommand);
        commands.put("RENT", this::finishTwoColorRentCommand);
        commands.put("RENT_ANY", this::finishMultipleColorRentCommand);
        commands.put("HOUSE", (requester, args) -> finishBuildingCommand(requester, args, ActionCardType.HOUSE));
        commands.put("HOTEL", (requester, args) -> finishBuildingCommand(requester, args, ActionCardType.HOTEL));
        commands.put("FORCED_DEAL", this::finishForcedDealCommand);
        return commands;
    }

    // Finishes pass go command.
    private boolean finishPassGoCommand(ClientHandler requester, String[] args) {
        if (args.length < 1) {
            requester.send(new NetworkMessage("SERVER", "Use PASS_GO <handNo>").encode());
            return false;
        }

        ActionCards card = getActionCardFromHand(requester, args[0], ActionCardType.PASS_GO);
        return card != null && game.finishPassGo(card);
    }

    // Finishes birthday command.
    private boolean finishBirthdayCommand(ClientHandler requester, String[] args) {
        if (args.length < 1) {
            requester.send(new NetworkMessage("SERVER", "Use BIRTHDAY <handNo>").encode());
            return false;
        }

        ActionCards card = getActionCardFromHand(requester, args[0], ActionCardType.BIRTHDAY);
        return card != null && game.finishBirthday(card);
    }

    // Finishes debt command.
    private boolean finishDebtCommand(ClientHandler requester, String[] args) {
        if (args.length < 2) {
            requester.send(new NetworkMessage("SERVER", "Use DEBT <handNo> <targetPlayer>").encode());
            return false;
        }

        ActionCards card = getActionCardFromHand(requester, args[0], ActionCardType.DEBT_COLLECTOR);
        Player target = getPlayerByOneBasedNumber(args[1]);
        return card != null && target != null && game.finishDebtCollector(card, target);
    }

    // Finishes sly command.
    private boolean finishSlyCommand(ClientHandler requester, String[] args) {
        if (args.length < 3) {
            requester.send(new NetworkMessage("SERVER", "Use SLY <handNo> <targetPlayer> <propertyNo>").encode());
            return false;
        }

        ActionCards card = getActionCardFromHand(requester, args[0], ActionCardType.SLY_DEAL);
        Player target = getPlayerByOneBasedNumber(args[1]);
        PropertiesCards property = getPropertyByOneBasedNumber(target, args[2]);
        return card != null && property != null && game.finishSlyDeal(card, target, property);
    }

    // Finishes deal breaker command.
    private boolean finishDealBreakerCommand(ClientHandler requester, String[] args) {
        if (args.length < 3) {
            requester.send(new NetworkMessage("SERVER", "Use DEAL_BREAKER <handNo> <targetPlayer> <color>").encode());
            return false;
        }

        ActionCards card = getActionCardFromHand(requester, args[0], ActionCardType.DEAL_BREAKER);
        Player target = getPlayerByOneBasedNumber(args[1]);
        PropertyColor color = parseColor(args[2]);
        ArrayList<PropertiesCards> selectedSet = getPropertiesByColor(target, color);

        return card != null && target != null && !selectedSet.isEmpty()
                && game.finishDealBreaker(card, target, selectedSet);
    }

    // Finishes two color rent command.
    private boolean finishTwoColorRentCommand(ClientHandler requester, String[] args) {
        if (args.length < 2) {
            requester.send(new NetworkMessage("SERVER", "Use RENT <handNo> <color> [DOUBLE]").encode());
            return false;
        }

        Card rawCard = getHandCardByOneBasedNumber(requester, args[0]);
        PropertyColor color = parseColor(args[1]);
        boolean useDoubleRent = hasDoubleToken(args);

        if (!(rawCard instanceof ActionCards actionCard)) {
            return false;
        }

        return color != null && isTwoColorRentCard(actionCard)
                && game.finishTwoColorRent(actionCard, color, useDoubleRent);
    }

    // Finishes multiple color rent command.
    private boolean finishMultipleColorRentCommand(ClientHandler requester, String[] args) {
        if (args.length < 3) {
            requester.send(new NetworkMessage("SERVER", "Use RENT_ANY <handNo> <targetPlayer> <color> [DOUBLE]").encode());
            return false;
        }

        ActionCards card = getActionCardFromHand(requester, args[0], ActionCardType.RENT_WITH_MULTIPLE_COLOR);
        Player target = getPlayerByOneBasedNumber(args[1]);
        PropertyColor color = parseColor(args[2]);
        boolean useDoubleRent = hasDoubleToken(args);

        return card != null && target != null && color != null
                && game.finishMultipleColorRent(card, target, color, useDoubleRent);
    }

    // Finishes forced deal command.
    private boolean finishForcedDealCommand(ClientHandler requester, String[] args) {
        if (args.length < 4) {
            requester.send(new NetworkMessage("SERVER", "Use FORCED_DEAL <handNo> <targetPlayer> <myPropertyNo> <targetPropertyNo>").encode());
            return false;
        }

        ActionCards card = getActionCardFromHand(requester, args[0], ActionCardType.FORCED_DEAL);
        Player target = getPlayerByOneBasedNumber(args[1]);
        Player currentPlayer = game.getPlayers().get(requester.getPlayerId() - 1);
        PropertiesCards myProperty = getPropertyByOneBasedNumber(currentPlayer, args[2]);
        PropertiesCards targetProperty = getPropertyByOneBasedNumber(target, args[3]);

        return card != null && target != null && myProperty != null && targetProperty != null
                && game.finishForcedDeal(card, target, myProperty, targetProperty);
    }

    // Finishes building command.
    private boolean finishBuildingCommand(ClientHandler requester, String[] args, ActionCardType type) {
        if (args.length < 2) {
            requester.send(new NetworkMessage("SERVER", "Use " + type.name() + " <handNo> <color>").encode());
            return false;
        }

        ActionCards card = getActionCardFromHand(requester, args[0], type);
        PropertyColor color = parseColor(args[1]);

        if (card == null || color == null) {
            return false;
        }

        if (type == ActionCardType.HOUSE) {
            return game.finishHouse(card, color);
        }

        return game.finishHotel(card, color);
    }

    // Finds action card from hand.
    private ActionCards getActionCardFromHand(ClientHandler requester, String handNumberText, ActionCardType expectedType) {
        Card card = getHandCardByOneBasedNumber(requester, handNumberText);

        if (card instanceof ActionCards actionCard && actionCard.getActionCardType() == expectedType) {
            return actionCard;
        }

        requester.send(new NetworkMessage("SERVER", "Hand card " + handNumberText + " is not " + expectedType.name()).encode());
        return null;
    }

    // Finds hand card by one based number.
    private Card getHandCardByOneBasedNumber(ClientHandler requester, String handNumberText) {
        int index = parseOneBasedCardIndex(handNumberText);

        if (index < 0) {
            return null;
        }

        Player player = game.getPlayers().get(requester.getPlayerId() - 1);

        if (index >= player.getHandCards().size()) {
            return null;
        }

        return player.getHandCards().get(index);
    }

    // Finds player by one based number.
    private Player getPlayerByOneBasedNumber(String playerNumberText) {
        int index = parseOneBasedCardIndex(playerNumberText);

        if (index < 0 || index >= game.getPlayers().size()) {
            return null;
        }

        return game.getPlayers().get(index);
    }

    // Finds property by one based number.
    private PropertiesCards getPropertyByOneBasedNumber(Player player, String propertyNumberText) {
        if (player == null) {
            return null;
        }

        int index = parseOneBasedCardIndex(propertyNumberText);

        if (index < 0 || index >= player.getPropertyCards().size()) {
            return null;
        }

        return player.getPropertyCards().get(index);
    }

    // Finds properties by color.
    private ArrayList<PropertiesCards> getPropertiesByColor(Player player, PropertyColor color) {
        ArrayList<PropertiesCards> result = new ArrayList<>();

        if (player == null || color == null) {
            return result;
        }

        for (PropertiesCards card : player.getPropertyCards()) {
            if (card.getCurrentColor() == color) {
                result.add(card);
            }
        }

        return result;
    }

    // Parses color.
    private PropertyColor parseColor(String colorText) {
        try {
            return PropertyColor.valueOf(colorText.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    // Checks whether this has double token.
    private boolean hasDoubleToken(String[] args) {
        for (String arg : args) {
            if ("DOUBLE".equalsIgnoreCase(arg)) {
                return true;
            }
        }

        return false;
    }

    // Checks whether two color rent card.
    private boolean isTwoColorRentCard(ActionCards card) {
        return card.getActionCardType().isTwoColorRentCard();
    }

    // Restarts the timer when an action advances to another player.
    private synchronized void restartTurnTimerIfTurnChanged(int previousPlayerIndex) {
        if (isGameStarted() && game.getCurrentPlayerIndex() != previousPlayerIndex) {
            restartTurnTimer();
        }
    }

    // Sends game state to all.
    private synchronized void sendGameStateToAll() {
        if (game == null) {
            return;
        }

        cancelTurnTimerIfGameWon();
        for (ClientHandler client : clients) {
            client.send(new NetworkMessage(
                    "GAME_STATE",
                    GameStateCodec.encode(game, client.getPlayerId(), getTurnRemainingSeconds())
            ).encode());
        }
    }

    // Restarts the authoritative server-side turn timer.
    private synchronized void restartTurnTimer() {
        cancelTurnTimer();

        if (!isGameStarted() || game.isWin()) {
            turnDeadlineMillis = -1;
            return;
        }

        int timedPlayerIndex = game.getCurrentPlayerIndex();
        turnDeadlineMillis = System.currentTimeMillis() + TURN_SECONDS * 1000L;
        turnTimeoutTask = turnTimer.schedule(
                () -> handleTurnTimeout(timedPlayerIndex),
                TURN_SECONDS,
                TimeUnit.SECONDS
        );
    }

    // Cancels the current turn timer task.
    private synchronized void cancelTurnTimer() {
        if (turnTimeoutTask != null) {
            turnTimeoutTask.cancel(false);
            turnTimeoutTask = null;
        }
    }

    // Cancels the timer once the game has already been won.
    private synchronized void cancelTurnTimerIfGameWon() {
        if (isGameStarted() && game.isWin()) {
            cancelTurnTimer();
            turnDeadlineMillis = -1;
        }
    }

    // Handles a timed-out turn and advances the game when the same player is still active.
    private synchronized void handleTurnTimeout(int timedPlayerIndex) {
        if (!isGameStarted() || game.isWin() || game.getCurrentPlayerIndex() != timedPlayerIndex) {
            return;
        }

        if (game.isPaymentSelecting()) {
            restartTurnTimer();
            sendGameStateToAll();
            return;
        }

        int timedOutPlayerId = timedPlayerIndex + 1;
        game.forceAdvanceTurnForAbsentPlayer();
        restartTurnTimer();
        broadcast(new NetworkMessage(
                "BROADCAST",
                "Player " + timedOutPlayerId + " timed out; turn advanced to Player "
                        + (game.getCurrentPlayerIndex() + 1)
        ));
        sendGameStateToAll();
    }

    // Finds the remaining seconds for the current turn timer.
    private synchronized int getTurnRemainingSeconds() {
        if (turnDeadlineMillis < 0) {
            return -1;
        }

        long remainingMillis = turnDeadlineMillis - System.currentTimeMillis();
        return (int) Math.max(0, (remainingMillis + 999) / 1000);
    }

    // Runs card to text.
    private String cardToText(Card card) {
        if (card instanceof MoneyCards) {
            return "MONEY:" + card.getValue();
        }

        if (card instanceof ActionCards actionCard) {
            return "ACTION:" + actionCard.getActionCardType().name() + ":" + card.getValue();
        }

        if (card instanceof PropertiesCards propertyCard) {
            return propertyToText(propertyCard);
        }

        return "CARD_" + card.getValue();
    }

    // Runs property to text.
    private String propertyToText(PropertiesCards card) {
        String currentColor = card.getCurrentColor() == null ? "NO_COLOR" : card.getCurrentColor().name();
        return "PROPERTY:" + card.getType().name() + ":" + currentColor + ":"
                + card.getImageFileName() + ":" + card.getValue() + ":"
                + card.hasHouse() + ":" + card.hasHotel();
    }

    private class ClientHandler extends Thread {
        private final Socket socket;
        private final int playerId;
        private final Map<String, Consumer<NetworkMessage>> messageHandlers;
        private String playerName;
        private boolean nameAnnounced;
        private PrintWriter out;

        // Runs client handler.
        private ClientHandler(Socket socket, int playerId) {
            this.socket = socket;
            this.playerId = playerId;
            this.playerName = "Player";
            this.messageHandlers = createMessageHandlers();
        }

        public int getPlayerId() {
            return playerId;
        }

        public String getPlayerName() {
            return playerName;
        }

        // Runs run.
        @Override
        public void run() {
            try (
                    Socket socketToClose = socket;
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
            ) {
                out = writer;
                send(new NetworkMessage("WELCOME", "PLAYER " + playerId).encode());

                String line;

                while ((line = in.readLine()) != null) {
                    NetworkMessage message = NetworkMessage.decode(line);
                    System.out.println("From Player " + playerId + ": " + message.getType() + " " + message.getBody());
                    handleMessage(message);
                }
            } catch (IOException e) {
                System.out.println("Connection error for Player " + playerId + ": " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Runtime error for Player " + playerId + ": " + e.getMessage());
                e.printStackTrace();
                send(new NetworkMessage("SERVER", "Connection closed by server error: " + e.getMessage()).encode());
            } finally {
                removeClient(this);
                broadcast(new NetworkMessage("BROADCAST", playerName + " left the game"));
                broadcast(new NetworkMessage("PLAYER_LIST", getPlayerListText()));
            }
        }

        // Handles message.
        private void handleMessage(NetworkMessage message) {
            Consumer<NetworkMessage> handler = messageHandlers.get(message.getType());

            if (handler != null) {
                handler.accept(message);
            } else if (isActionCommand(message.getType())) {
                finishActionIfValid(this, message.getType(), message.getBody());
            } else {
                send(new NetworkMessage("SERVER", "Unknown message: " + message.getType()).encode());
            }
        }

        // Creates message handlers.
        private Map<String, Consumer<NetworkMessage>> createMessageHandlers() {
            Map<String, Consumer<NetworkMessage>> handlers = new HashMap<>();
            handlers.put("HELLO", message -> broadcast(new NetworkMessage("BROADCAST", playerName + " says hello")));
            handlers.put("NAME", message -> updatePlayerName(message.getBody()));
            handlers.put("PLAYERS", message -> send(new NetworkMessage("PLAYER_LIST", getPlayerListText()).encode()));
            handlers.put("STATE", message -> sendGameStateTo(this));
            handlers.put("START_GAME", message -> startGameIfReady());
            handlers.put("PLAY_CARD", message -> playCardIfValid(this, message.getBody()));
            handlers.put("PLAY_AS_MONEY", message -> playActionCardAsMoneyIfValid(this, message.getBody()));
            handlers.put("DISCARD", message -> discardIfValid(this, message.getBody()));
            handlers.put("PAY", message -> payIfValid(this, message.getBody()));
            handlers.put("JUST_SAY_NO", message -> justSayNoIfValid(this));
            handlers.put("PASS_JUST_SAY_NO", message -> passJustSayNoIfValid(this));
            handlers.put("SET_PROPERTY_COLOR", message -> setPropertyColorIfValid(this, message.getBody()));
            handlers.put("END_TURN", message -> endTurnIfGameStarted(this));
            return handlers;
        }

        // Updates player name before the game starts.
        private void updatePlayerName(String requestedName) {
            synchronized (GameServer.this) {
                if (gameStarted) {
                    send(new NetworkMessage("SERVER", "Name cannot be changed after game starts").encode());
                    return;
                }

                playerName = sanitizePlayerName(requestedName, playerId);
            }

            if (!nameAnnounced) {
                nameAnnounced = true;
                broadcast(new NetworkMessage("BROADCAST", playerName + " joined the game"));
            } else {
                broadcast(new NetworkMessage("BROADCAST", playerName + " updated name"));
            }
            broadcast(new NetworkMessage("PLAYER_LIST", getPlayerListText()));
        }

        // Starts game if ready.
        private void startGameIfReady() {
            if (tryStartGame(this)) {
                broadcast(new NetworkMessage(
                        "GAME_STARTED",
                        "Started with " + getPlayerCount() + " players: " + getPlayerListText()
                ));
                sendGameStateToAll();
            }
        }

        // Checks whether action command.
        private boolean isActionCommand(String type) {
            return actionCommands.containsKey(type);
        }

        // Sends this operation.
        private void send(String encodedMessage) {
            if (out != null) {
                out.println(encodedMessage);
            }
        }
    }

    // Sanitizes player name for the simple text network protocol.
    private static String sanitizePlayerName(String requestedName, int playerId) {
        String sanitized = requestedName == null
                ? ""
                : requestedName.trim()
                .replace("|", "")
                .replace(",", "")
                .replace(";", "")
                .replace("[", "")
                .replace("]", "");

        if (sanitized.isEmpty()) {
            sanitized = "Player";
        }

        return sanitized.length() > 16 ? sanitized.substring(0, 16) : sanitized;
    }

    @FunctionalInterface
    private interface ActionCommand {
        // Finishes an action command for a client.
        boolean finish(ClientHandler requester, String[] args);
    }
}
