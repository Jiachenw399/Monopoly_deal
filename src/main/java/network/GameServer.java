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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GameServer {
    private static final int PORT = 5555;
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 4;

    private final AtomicInteger nextPlayerId = new AtomicInteger(1);
    private final List<ClientHandler> clients = new ArrayList<>();
    private boolean gameStarted = false;
    private Game game;

    public static void main(String[] args) {
        new GameServer().start();
    }

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

    private synchronized boolean isServerFull() {
        return clients.size() >= MAX_PLAYERS || gameStarted;
    }

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

    private synchronized void addClient(ClientHandler clientHandler) {
        clients.add(clientHandler);
        System.out.println("Player " + clientHandler.getPlayerId() + " connected. Players: " + clients.size());
    }

    private synchronized void removeClient(ClientHandler clientHandler) {
        int disconnectedPlayerId = clientHandler.getPlayerId();
        clients.remove(clientHandler);
        System.out.println("Player " + disconnectedPlayerId + " disconnected. Players: " + clients.size());
        handleDisconnectedPlayerDuringGame(disconnectedPlayerId);
    }

    private synchronized void handleDisconnectedPlayerDuringGame(int disconnectedPlayerId) {
        if (!isGameStarted()) {
            return;
        }

        int currentPlayerId = game.getCurrentPlayerIndex() + 1;

        if (disconnectedPlayerId != currentPlayerId) {
            return;
        }

        game.forceAdvanceTurnForAbsentPlayer();
        broadcast(new NetworkMessage(
                "BROADCAST",
                "Player " + disconnectedPlayerId + " left; turn advanced to Player " + (game.getCurrentPlayerIndex() + 1)
        ));
        sendGameStateToAll();
    }

    private synchronized int getPlayerCount() {
        return clients.size();
    }

    private synchronized String getPlayerListText() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < clients.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }

            builder.append("PLAYER ").append(clients.get(i).getPlayerId());
        }

        return builder.toString();
    }

    private synchronized void broadcast(NetworkMessage message) {
        String encodedMessage = message.encode();

        for (ClientHandler client : clients) {
            client.send(encodedMessage);
        }
    }

    private synchronized boolean tryStartGame(ClientHandler starter) {
        if (gameStarted) {
            starter.send(new NetworkMessage("SERVER", "Game has already started").encode());
            return false;
        }

        if (clients.size() < MIN_PLAYERS) {
            starter.send(new NetworkMessage("SERVER", "Need at least " + MIN_PLAYERS + " players to start").encode());
            return false;
        }

        gameStarted = true;
        game = new Game(clients.size());
        game.startGame();
        return true;
    }

    private synchronized boolean isGameStarted() {
        return gameStarted && game != null;
    }

    private synchronized void endTurnIfGameStarted(ClientHandler requester) {
        if (!isGameStarted()) {
            requester.send(new NetworkMessage("SERVER", "Game has not started yet").encode());
            return;
        }

        int expectedPlayerId = game.getCurrentPlayerIndex() + 1;

        if (requester.getPlayerId() != expectedPlayerId) {
            requester.send(new NetworkMessage("SERVER", "It is Player " + expectedPlayerId + "'s turn").encode());
            return;
        }

        game.guiEndTurn();
        sendGameStateToAll();
    }

    private synchronized void sendGameStateTo(ClientHandler client) {
        if (!isGameStarted()) {
            client.send(new NetworkMessage("SERVER", "Game has not started yet").encode());
            return;
        }

        client.send(new NetworkMessage("GAME_STATE", GameStateCodec.encode(game, client.getPlayerId())).encode());
    }

    private synchronized void playCardIfValid(ClientHandler requester, String cardNumberText) {
        if (!isGameStarted()) {
            requester.send(new NetworkMessage("SERVER", "Game has not started yet").encode());
            return;
        }

        int expectedPlayerId = game.getCurrentPlayerIndex() + 1;

        if (requester.getPlayerId() != expectedPlayerId) {
            requester.send(new NetworkMessage("SERVER", "It is Player " + expectedPlayerId + "'s turn").encode());
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

    private synchronized void playActionCardAsMoneyIfValid(ClientHandler requester, String cardNumberText) {
        if (!isGameStarted()) {
            requester.send(new NetworkMessage("SERVER", "Game has not started yet").encode());
            return;
        }

        int expectedPlayerId = game.getCurrentPlayerIndex() + 1;

        if (requester.getPlayerId() != expectedPlayerId) {
            requester.send(new NetworkMessage("SERVER", "It is Player " + expectedPlayerId + "'s turn").encode());
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

    private synchronized void discardIfValid(ClientHandler requester, String cardNumberText) {
        if (!isGameStarted()) {
            requester.send(new NetworkMessage("SERVER", "Game has not started yet").encode());
            return;
        }

        int expectedPlayerId = game.getCurrentPlayerIndex() + 1;

        if (requester.getPlayerId() != expectedPlayerId) {
            requester.send(new NetworkMessage("SERVER", "It is Player " + expectedPlayerId + "'s turn").encode());
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

        if (!game.discard(selectedCard)) {
            requester.send(new NetworkMessage("SERVER", "Could not discard " + selectedCardText).encode());
            return;
        }

        broadcast(new NetworkMessage("BROADCAST", "Player " + requester.getPlayerId() + " discarded " + selectedCardText));
        sendGameStateToAll();
    }

    private synchronized void payIfValid(ClientHandler requester, String paymentText) {
        if (!isGameStarted()) {
            requester.send(new NetworkMessage("SERVER", "Game has not started yet").encode());
            return;
        }

        if (!game.isPaymentSelecting()) {
            requester.send(new NetworkMessage("SERVER", "No payment is required now").encode());
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
        int payerId = game.getPlayers().indexOf(request.getPayer()) + 1;

        if (requester.getPlayerId() != payerId) {
            requester.send(new NetworkMessage("SERVER", "Player " + payerId + " must respond now").encode());
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

    private synchronized void setPropertyColorIfValid(ClientHandler requester, String body) {
        if (!isGameStarted()) {
            requester.send(new NetworkMessage("SERVER", "Game has not started yet").encode());
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

        game.setPropertyColor(player, property, color);
        broadcast(new NetworkMessage("BROADCAST", "Player " + requester.getPlayerId() + " changed a wild card to " + color));
        sendGameStateToAll();
    }

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

    private int parseOneBasedCardIndex(String cardNumberText) {
        try {
            return Integer.parseInt(cardNumberText.trim()) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private synchronized void finishActionIfValid(ClientHandler requester, String command, String body) {
        if (!isGameStarted()) {
            requester.send(new NetworkMessage("SERVER", "Game has not started yet").encode());
            return;
        }

        int expectedPlayerId = game.getCurrentPlayerIndex() + 1;

        if (requester.getPlayerId() != expectedPlayerId) {
            requester.send(new NetworkMessage("SERVER", "It is Player " + expectedPlayerId + "'s turn").encode());
            return;
        }

        String[] args = body.trim().isEmpty() ? new String[0] : body.trim().split("\\s+");
        boolean success;

        switch (command) {
            case "BIRTHDAY" -> success = finishBirthdayCommand(requester, args);
            case "PASS_GO" -> success = finishPassGoCommand(requester, args);
            case "DEBT" -> success = finishDebtCommand(requester, args);
            case "SLY" -> success = finishSlyCommand(requester, args);
            case "DEAL_BREAKER" -> success = finishDealBreakerCommand(requester, args);
            case "RENT" -> success = finishTwoColorRentCommand(requester, args);
            case "RENT_ANY" -> success = finishMultipleColorRentCommand(requester, args);
            case "HOUSE" -> success = finishBuildingCommand(requester, args, ActionCardType.HOUSE);
            case "HOTEL" -> success = finishBuildingCommand(requester, args, ActionCardType.HOTEL);
            case "FORCED_DEAL" -> success = finishForcedDealCommand(requester, args);
            default -> {
                requester.send(new NetworkMessage("SERVER", "Unknown action command: " + command).encode());
                return;
            }
        }

        if (!success) {
            requester.send(new NetworkMessage("SERVER", "Action failed: " + command + " " + body).encode());
            return;
        }

        broadcast(new NetworkMessage("BROADCAST", "Player " + requester.getPlayerId() + " used " + command));
        sendGameStateToAll();
    }

    private boolean finishPassGoCommand(ClientHandler requester, String[] args) {
        if (args.length < 1) {
            requester.send(new NetworkMessage("SERVER", "Use PASS_GO <handNo>").encode());
            return false;
        }

        ActionCards card = getActionCardFromHand(requester, args[0], ActionCardType.PASS_GO);
        return card != null && game.finishPassGo(card);
    }

    private boolean finishBirthdayCommand(ClientHandler requester, String[] args) {
        if (args.length < 1) {
            requester.send(new NetworkMessage("SERVER", "Use BIRTHDAY <handNo>").encode());
            return false;
        }

        ActionCards card = getActionCardFromHand(requester, args[0], ActionCardType.BIRTHDAY);
        return card != null && game.finishBirthday(card);
    }

    private boolean finishDebtCommand(ClientHandler requester, String[] args) {
        if (args.length < 2) {
            requester.send(new NetworkMessage("SERVER", "Use DEBT <handNo> <targetPlayer>").encode());
            return false;
        }

        ActionCards card = getActionCardFromHand(requester, args[0], ActionCardType.DEBT_COLLECTOR);
        Player target = getPlayerByOneBasedNumber(args[1]);
        return card != null && target != null && game.finishDebtCollector(card, target);
    }

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

    private ActionCards getActionCardFromHand(ClientHandler requester, String handNumberText, ActionCardType expectedType) {
        Card card = getHandCardByOneBasedNumber(requester, handNumberText);

        if (card instanceof ActionCards actionCard && actionCard.getActionCardType() == expectedType) {
            return actionCard;
        }

        requester.send(new NetworkMessage("SERVER", "Hand card " + handNumberText + " is not " + expectedType.name()).encode());
        return null;
    }

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

    private Player getPlayerByOneBasedNumber(String playerNumberText) {
        int index = parseOneBasedCardIndex(playerNumberText);

        if (index < 0 || index >= game.getPlayers().size()) {
            return null;
        }

        return game.getPlayers().get(index);
    }

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

    private PropertyColor parseColor(String colorText) {
        try {
            return PropertyColor.valueOf(colorText.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private boolean hasDoubleToken(String[] args) {
        for (String arg : args) {
            if ("DOUBLE".equalsIgnoreCase(arg)) {
                return true;
            }
        }

        return false;
    }

    private boolean isTwoColorRentCard(ActionCards card) {
        ActionCardType type = card.getActionCardType();

        return type == ActionCardType.RENT_WITH_RED_AND_YELLOW
                || type == ActionCardType.RENT_WITH_ORANGE_AND_PINK
                || type == ActionCardType.RENT_WITH_BROWN_AND_LIGHT_BLUE
                || type == ActionCardType.RENT_WITH_BLACK_AND_LIGHT_GREEN
                || type == ActionCardType.RENT_WITH_DARK_BLUE_AND_DARK_GREEN;
    }

    private synchronized void sendGameStateToAll() {
        if (game == null) {
            return;
        }

        for (ClientHandler client : clients) {
            client.send(new NetworkMessage("GAME_STATE", GameStateCodec.encode(game, client.getPlayerId())).encode());
        }
    }

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

    private String propertyToText(PropertiesCards card) {
        String currentColor = card.getCurrentColor() == null ? "NO_COLOR" : card.getCurrentColor().name();
        return "PROPERTY:" + card.getType().name() + ":" + currentColor + ":"
                + card.getImageFileName() + ":" + card.getValue() + ":"
                + card.hasHouse() + ":" + card.hasHotel();
    }

    private class ClientHandler extends Thread {
        private final Socket socket;
        private final int playerId;
        private PrintWriter out;

        private ClientHandler(Socket socket, int playerId) {
            this.socket = socket;
            this.playerId = playerId;
        }

        public int getPlayerId() {
            return playerId;
        }

        @Override
        public void run() {
            try (
                    Socket socketToClose = socket;
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
            ) {
                out = writer;
                send(new NetworkMessage("WELCOME", "PLAYER " + playerId).encode());
                broadcast(new NetworkMessage("BROADCAST", "Player " + playerId + " joined the game"));
                broadcast(new NetworkMessage("PLAYER_LIST", getPlayerListText()));

                String line;

                while ((line = in.readLine()) != null) {
                    NetworkMessage message = NetworkMessage.decode(line);
                    System.out.println("From Player " + playerId + ": " + message.getType() + " " + message.getBody());
                    handleMessage(message);
                }
            } catch (IOException e) {
                System.out.println("Connection error for Player " + playerId + ": " + e.getMessage());
            } finally {
                removeClient(this);
                broadcast(new NetworkMessage("BROADCAST", "Player " + playerId + " left the game"));
                broadcast(new NetworkMessage("PLAYER_LIST", getPlayerListText()));
            }
        }

        private void handleMessage(NetworkMessage message) {
            if ("HELLO".equals(message.getType())) {
                broadcast(new NetworkMessage("BROADCAST", "Player " + playerId + " says hello"));
            } else if ("PLAYERS".equals(message.getType())) {
                send(new NetworkMessage("PLAYER_LIST", getPlayerListText()).encode());
            } else if ("STATE".equals(message.getType())) {
                sendGameStateTo(this);
            } else if ("START_GAME".equals(message.getType())) {
                if (tryStartGame(this)) {
                    broadcast(new NetworkMessage(
                            "GAME_STARTED",
                            "Started with " + getPlayerCount() + " players: " + getPlayerListText()
                    ));
                    sendGameStateToAll();
                }
            } else if ("PLAY_CARD".equals(message.getType())) {
                playCardIfValid(this, message.getBody());
            } else if ("PLAY_AS_MONEY".equals(message.getType())) {
                playActionCardAsMoneyIfValid(this, message.getBody());
            } else if ("DISCARD".equals(message.getType())) {
                discardIfValid(this, message.getBody());
            } else if ("PAY".equals(message.getType())) {
                payIfValid(this, message.getBody());
            } else if ("JUST_SAY_NO".equals(message.getType())) {
                justSayNoIfValid(this);
            } else if ("SET_PROPERTY_COLOR".equals(message.getType())) {
                setPropertyColorIfValid(this, message.getBody());
            } else if (isActionCommand(message.getType())) {
                finishActionIfValid(this, message.getType(), message.getBody());
            } else if ("END_TURN".equals(message.getType())) {
                endTurnIfGameStarted(this);
            } else {
                send(new NetworkMessage("SERVER", "Unknown message: " + message.getType()).encode());
            }
        }

        private boolean isActionCommand(String type) {
            return "BIRTHDAY".equals(type)
                    || "PASS_GO".equals(type)
                    || "DEBT".equals(type)
                    || "SLY".equals(type)
                    || "DEAL_BREAKER".equals(type)
                    || "RENT".equals(type)
                    || "RENT_ANY".equals(type)
                    || "HOUSE".equals(type)
                    || "HOTEL".equals(type)
                    || "FORCED_DEAL".equals(type);
        }

        private void send(String encodedMessage) {
            if (out != null) {
                out.println(encodedMessage);
            }
        }
    }
}
