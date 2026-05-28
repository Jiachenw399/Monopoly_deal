package network;

import GUI.GameScreen;
import GUI.GuiScale;
import GUI.MainMenu;
import GUI.ScreenDrawHelper;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import logic.Game;
import model.ActionCardType;
import model.ActionCards;
import model.Card;
import model.DrawPileAndDiscardPile;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;
import model.PropertiesCardsType;
import model.PropertyColor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Online client shell that reuses the same GameScreen and click flow as local play.
 */
public class OnlinePlayWindow extends Stage {
    private static final int PORT = 5555;

    private final String host;
    private final Game game = new Game();
    private final GameScreen gameScreen = new GameScreen(game);
    private final Canvas lobbyCanvas = new Canvas(GuiScale.canvasWidth(), GuiScale.canvasHeight());
    private final ArrayList<String> logLines = new ArrayList<>();

    private volatile boolean closed;
    private Socket socket;
    private PrintWriter out;
    private boolean started;
    private int myPlayerId;
    private String connectionText = "Connecting...";

    public OnlinePlayWindow(Stage owner, String host) {
        this.host = host;
        initOwner(owner);
        setTitle("LAN Game - " + host + ":" + PORT);

        Group root = new Group(lobbyCanvas, gameScreen.getCanvas());
        Scene scene = new Scene(root, GuiScale.canvasWidth(), GuiScale.canvasHeight());
        setScene(scene);
        setOnCloseRequest(e -> shutdown());
        scene.setOnMouseClicked(e -> handleMouseClick(GuiScale.toLogical(e.getX()), GuiScale.toLogical(e.getY())));

        startRenderLoop();
        appendLog("Connecting to " + host + "...");
        new Thread(this::runConnection, "online-socket").start();
    }

    private void startRenderLoop() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (started) {
                    lobbyCanvas.setVisible(false);
                    gameScreen.setShow(true);
                    gameScreen.paint();
                } else {
                    gameScreen.setShow(false);
                    gameScreen.clear();
                    lobbyCanvas.setVisible(true);
                    paintLobby();
                }
            }
        }.start();
    }

    private void paintLobby() {
        GraphicsContext gc = lobbyCanvas.getGraphicsContext2D();
        GuiScale.prepare(gc);
        ScreenDrawHelper.drawPageBackground(gc, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
        ScreenDrawHelper.drawPanel(gc, 250, 120, 535, 380);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(ScreenDrawHelper.ACCENT);
        gc.setFont(Font.font("Arial", 38));
        gc.fillText("LAN Game", Game.SCREEN_WIDTH / 2, 185);

        gc.setFill(ScreenDrawHelper.TEXT);
        gc.setFont(Font.font("Arial", 18));
        gc.fillText(connectionText + "  " + host + ":" + PORT, Game.SCREEN_WIDTH / 2, 235);
        gc.fillText(myPlayerId > 0 ? "You are Player " + myPlayerId : "Waiting for player id",
                Game.SCREEN_WIDTH / 2, 270);

        ScreenDrawHelper.drawButton(gc, 390, 320, 255, 48, "Start Game");
        ScreenDrawHelper.drawButton(gc, 390, 390, 255, 48, "Refresh Players");

        gc.setFill(ScreenDrawHelper.MUTED_TEXT);
        gc.setFont(Font.font("Arial", 13));
        int start = Math.max(0, logLines.size() - 4);
        double y = 535;
        for (int i = start; i < logLines.size(); i++) {
            gc.fillText(logLines.get(i), Game.SCREEN_WIDTH / 2, y);
            y += 18;
        }
    }

    private void handleMouseClick(double x, double y) {
        if (!started) {
            if (isRect(x, y, 390, 320, 255, 48)) {
                send("START_GAME", "");
            } else if (isRect(x, y, 390, 390, 255, 48)) {
                send("PLAYERS", "");
            }
            return;
        }

        if (handleActionCardChoiceClick(x, y)) {
            return;
        }
        if (handleSelectionModeClick(x, y)) {
            return;
        }
        if (handlePlayerDetailPopupClick(x, y)) {
            return;
        }
        if (game.isWin()) {
            return;
        }
        if (gameScreen.handleBackgroundPageButtonClick(x, y)) {
            return;
        }
        if (handleWildCardClick(x, y)) {
            return;
        }
        if (handleButtonClick(x, y)) {
            return;
        }
        handleHandCardClick(x, y);
    }

    private boolean handleActionCardChoiceClick(double x, double y) {
        if (!gameScreen.isActionCardChoiceShowing()) {
            return false;
        }

        if (gameScreen.isActionCardChoiceCancelClicked(x, y)) {
            gameScreen.closeActionCardChoice();
            return true;
        }

        ActionCards selectedCard = gameScreen.getSelectedActionCardChoiceCard();
        if (selectedCard == null) {
            gameScreen.closeActionCardChoice();
            return true;
        }

        if (gameScreen.isActionCardChoiceMoneyClicked(x, y)) {
            send("PLAY_AS_MONEY", handNumber(selectedCard));
            gameScreen.closeActionCardChoice();
            return true;
        }

        if (gameScreen.isActionCardChoiceActionClicked(x, y)) {
            if (!gameScreen.canUseSelectedActionCardAsAction()) {
                return true;
            }
            gameScreen.closeActionCardChoice();
            handleActionCardClick(selectedCard);
            return true;
        }

        return true;
    }

    private boolean handlePlayerDetailPopupClick(double x, double y) {
        if (!gameScreen.isPlayerDetailPopupShowing()) {
            return false;
        }
        if (gameScreen.isPlayerDetailPopupCloseClicked(x, y)) {
            gameScreen.closePlayerDetailPopup();
            return true;
        }
        if (gameScreen.handlePlayerDetailPopupPageButtonClick(x, y)) {
            return true;
        }
        return true;
    }

    private boolean handleSelectionModeClick(double x, double y) {
        if (handlePaymentSelection(x, y)) {
            return true;
        }
        if (handleSlyDealSelection(x, y)) {
            return true;
        }
        if (handleForcedDealSelection(x, y)) {
            return true;
        }
        if (handleMultipleColorRentSelection(x, y)) {
            return true;
        }
        if (handleDebtCollectorSelection(x, y)) {
            return true;
        }
        if (handleDealBreakerSelection(x, y)) {
            return true;
        }
        if (handleBuildingSelection(x, y)) {
            return true;
        }
        return handleTwoColorRentSelection(x, y);
    }

    private boolean handlePaymentSelection(double x, double y) {
        if (!game.isPaymentSelecting()) {
            return false;
        }
        if (gameScreen.isPaymentJustSayNoClicked(x, y)) {
            send("JUST_SAY_NO", "");
            gameScreen.clearPaymentSelection();
            return true;
        }
        if (gameScreen.isPaymentClearClicked(x, y)) {
            gameScreen.clearPaymentSelection();
            return true;
        }
        if (gameScreen.isPaymentConfirmClicked(x, y)) {
            if (gameScreen.canConfirmPayment()) {
                send("PAY", paymentBody(gameScreen.getSelectedPaymentCards()));
                gameScreen.clearPaymentSelection();
            }
            return true;
        }
        return gameScreen.handlePaymentCardClick(x, y);
    }

    private boolean handleForcedDealSelection(double x, double y) {
        if (!gameScreen.isForcedDealSelecting()) {
            return false;
        }
        if (gameScreen.isForcedDealCancelClicked(x, y)) {
            gameScreen.cancelForcedDealSelection();
            return true;
        }
        if (gameScreen.isForcedDealBackClicked(x, y)) {
            gameScreen.setSelectedForcedDealTarget(null);
            return true;
        }
        Player targetPlayer = gameScreen.getClickedForcedDealTarget(x, y);
        if (targetPlayer != null) {
            gameScreen.setSelectedForcedDealTarget(targetPlayer);
            return true;
        }
        PropertiesCards myCard = gameScreen.getClickedForcedDealMyProperty(x, y);
        if (myCard != null) {
            gameScreen.setSelectedForcedDealMyProperty(myCard);
            return true;
        }
        PropertiesCards targetCard = gameScreen.getClickedForcedDealTargetProperty(x, y);
        if (targetCard != null) {
            gameScreen.setSelectedForcedDealTargetProperty(targetCard);
            return true;
        }
        if (gameScreen.isForcedDealConfirmClicked(x, y)) {
            if (gameScreen.canConfirmForcedDeal()) {
                send("FORCED_DEAL", handNumber(gameScreen.getPendingForcedDealCard())
                        + " " + playerNumber(gameScreen.getSelectedForcedDealTarget())
                        + " " + propertyNumber(game.getCurrentPlayer(), gameScreen.getSelectedForcedDealMyProperty())
                        + " " + propertyNumber(gameScreen.getSelectedForcedDealTarget(), gameScreen.getSelectedForcedDealTargetProperty()));
                gameScreen.cancelForcedDealSelection();
            }
            return true;
        }
        return true;
    }

    private boolean handleSlyDealSelection(double x, double y) {
        if (!gameScreen.isSlyDealSelecting()) {
            return false;
        }
        if (gameScreen.isSlyDealCancelClicked(x, y)) {
            gameScreen.cancelSlyDealSelection();
            return true;
        }
        GameScreen.SlyDealChoice choice = gameScreen.getClickedSlyDealChoice(x, y);
        if (choice != null) {
            send("SLY", handNumber(gameScreen.getPendingSlyDealCard())
                    + " " + playerNumber(choice.getTargetPlayer())
                    + " " + propertyNumber(choice.getTargetPlayer(), choice.getSelectedCard()));
            gameScreen.cancelSlyDealSelection();
        }
        return true;
    }

    private boolean handleMultipleColorRentSelection(double x, double y) {
        if (!gameScreen.isMultipleColorRentSelecting()) {
            return false;
        }
        if (gameScreen.isMultipleColorRentCancelClicked(x, y)) {
            gameScreen.cancelMultipleColorRentSelection();
            return true;
        }
        Player targetPlayer = gameScreen.getClickedMultipleColorRentTarget(x, y);
        if (targetPlayer != null) {
            gameScreen.setSelectedMultipleColorRentTarget(targetPlayer);
            return true;
        }
        if (gameScreen.isMultipleColorDoubleRentClicked(x, y)) {
            gameScreen.toggleMultipleColorDoubleRent();
            return true;
        }
        PropertyColor selectedColor = gameScreen.getClickedMultipleColorRentColor(x, y);
        if (selectedColor != null) {
            gameScreen.setSelectedMultipleColorRentColor(selectedColor);
            return true;
        }
        if (gameScreen.isMultipleColorRentConfirmClicked(x, y)) {
            if (gameScreen.canConfirmMultipleColorRent()) {
                send("RENT_ANY", handNumber(gameScreen.getPendingMultipleColorRentCard())
                        + " " + playerNumber(gameScreen.getSelectedMultipleColorRentTarget())
                        + " " + gameScreen.getSelectedMultipleColorRentColor().name()
                        + doubleToken(gameScreen.shouldUseDoubleRentForMultipleColorRent()));
                gameScreen.cancelMultipleColorRentSelection();
            }
            return true;
        }
        return true;
    }

    private boolean handleDebtCollectorSelection(double x, double y) {
        if (!gameScreen.isDebtCollectorSelecting()) {
            return false;
        }
        if (gameScreen.isDebtCollectorCancelClicked(x, y)) {
            gameScreen.cancelDebtCollectorSelection();
            return true;
        }
        Player targetPlayer = gameScreen.getClickedDebtCollectorTarget(x, y);
        if (targetPlayer != null) {
            send("DEBT", handNumber(gameScreen.getPendingDebtCollectorCard()) + " " + playerNumber(targetPlayer));
            gameScreen.cancelDebtCollectorSelection();
        }
        return true;
    }

    private boolean handleDealBreakerSelection(double x, double y) {
        if (!gameScreen.isDealBreakerSelecting()) {
            return false;
        }
        if (gameScreen.isDealBreakerCancelClicked(x, y)) {
            gameScreen.cancelDealBreakerSelection();
            return true;
        }
        GameScreen.DealBreakerChoice choice = gameScreen.getClickedDealBreakerChoice(x, y);
        if (choice != null && !choice.getSelectedSet().isEmpty()) {
            send("DEAL_BREAKER", handNumber(gameScreen.getPendingDealBreakerCard())
                    + " " + playerNumber(choice.getTargetPlayer())
                    + " " + choice.getSelectedSet().getFirst().getCurrentColor().name());
            gameScreen.cancelDealBreakerSelection();
        }
        return true;
    }

    private boolean handleTwoColorRentSelection(double x, double y) {
        if (!gameScreen.isTwoColorRentSelecting()) {
            return false;
        }
        if (gameScreen.isTwoColorDoubleRentClicked(x, y)) {
            gameScreen.toggleTwoColorDoubleRent();
            return true;
        }
        if (gameScreen.isTwoColorRentCancelClicked(x, y)) {
            gameScreen.cancelTwoColorRentSelection();
            return true;
        }
        PropertyColor selectedRentColor = gameScreen.getClickedTwoColorRentColor(x, y);
        if (selectedRentColor != null) {
            send("RENT", handNumber(gameScreen.getPendingTwoColorRentCard())
                    + " " + selectedRentColor.name()
                    + doubleToken(gameScreen.shouldUseDoubleRentForTwoColorRent()));
            gameScreen.cancelTwoColorRentSelection();
        }
        return true;
    }

    private boolean handleWildCardClick(double x, double y) {
        PropertyColor selectedColor = gameScreen.getClickedWildColorButton(x, y);
        if (selectedColor != null && gameScreen.getSelectedWildCard() != null) {
            send("SET_PROPERTY_COLOR", propertyNumber(game.getCurrentPlayer(), gameScreen.getSelectedWildCard())
                    + " " + selectedColor.name());
            gameScreen.clearSelectedWildCard();
            return true;
        }
        PropertiesCards clickedWildCard = gameScreen.getClickedWildCard(x, y);
        if (clickedWildCard != null) {
            gameScreen.setSelectedWildCard(clickedWildCard);
            return true;
        }
        return false;
    }

    private boolean handleBuildingSelection(double x, double y) {
        if (!gameScreen.isBuildingSelecting()) {
            return false;
        }
        if (gameScreen.isBuildingCancelClicked(x, y)) {
            gameScreen.cancelBuildingSelection();
            return true;
        }
        PropertyColor selectedColor = gameScreen.getClickedBuildingColor(x, y);
        if (selectedColor != null) {
            ActionCards card = gameScreen.getPendingBuildingCard();
            send(card.getActionCardType().name(), handNumber(card) + " " + selectedColor.name());
            gameScreen.cancelBuildingSelection();
        }
        return true;
    }

    private boolean handleButtonClick(double x, double y) {
        if (gameScreen.isEndTurnClicked(x, y)) {
            gameScreen.clearSelectedWildCard();
            gameScreen.closeActionCardChoice();
            send("END_TURN", "");
            return true;
        }
        if (gameScreen.isBackMenuClicked(x, y)) {
            close();
            shutdown();
            return true;
        }
        int viewedPlayerIndex = gameScreen.getClickedPlayerViewButtonIndex(x, y);
        if (viewedPlayerIndex != -1) {
            gameScreen.clearSelectedWildCard();
            gameScreen.closeActionCardChoice();
            gameScreen.showPlayerDetailPopup(viewedPlayerIndex);
            return true;
        }
        return false;
    }

    private void handleHandCardClick(double x, double y) {
        int handIndex = gameScreen.getClickedHandCardIndex(x, y);
        if (handIndex == -1 || handIndex >= game.getCurrentPlayer().getHandCards().size()) {
            return;
        }
        Card selectedCard = game.getCurrentPlayer().getHandCards().get(handIndex);
        if (game.isDiscard()) {
            send("DISCARD", Integer.toString(handIndex + 1));
            return;
        }
        if (game.getCurrentPlayer().getUseCardTimes() >= 3) {
            return;
        }
        if (selectedCard instanceof ActionCards actionCard) {
            gameScreen.showActionCardChoice(actionCard);
            return;
        }
        send("PLAY_CARD", Integer.toString(handIndex + 1));
    }

    private void handleActionCardClick(Card selectedCard) {
        if (!(selectedCard instanceof ActionCards actionCard)) {
            return;
        }
        ActionCardType type = actionCard.getActionCardType();
        switch (type) {
            case SLY_DEAL -> gameScreen.startSlyDealSelection(actionCard);
            case RENT_WITH_MULTIPLE_COLOR -> gameScreen.startMultipleColorRentSelection(actionCard);
            case HOUSE, HOTEL -> gameScreen.startBuildingSelection(actionCard);
            case FORCED_DEAL -> gameScreen.startForcedDealSelection(actionCard);
            case BIRTHDAY -> send("BIRTHDAY", handNumber(actionCard));
            case JUST_SAY_NO, DOUBLE_THE_RENT -> {
            }
            case DEBT_COLLECTOR -> gameScreen.startDebtCollectorSelection(actionCard);
            case DEAL_BREAKER -> gameScreen.startDealBreakerSelection(actionCard);
            case RENT_WITH_DARK_BLUE_AND_DARK_GREEN,
                 RENT_WITH_BROWN_AND_LIGHT_BLUE,
                 RENT_WITH_BLACK_AND_LIGHT_GREEN,
                 RENT_WITH_RED_AND_YELLOW,
                 RENT_WITH_ORANGE_AND_PINK -> gameScreen.startTwoColorRentSelection(actionCard);
            case PASS_GO -> send("PASS_GO", handNumber(actionCard));
        }
    }

    private void runConnection() {
        try {
            Socket s = new Socket(host, PORT);
            synchronized (this) {
                if (closed) {
                    s.close();
                    return;
                }
                socket = s;
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream(), true);
            Platform.runLater(() -> {
                connectionText = "Connected";
                appendLog("Connected.");
                send("PLAYERS", "");
            });
            String line;
            while (!closed && (line = in.readLine()) != null) {
                NetworkMessage message = NetworkMessage.decode(line);
                Platform.runLater(() -> handleServerMessage(message));
            }
        } catch (IOException e) {
            if (!closed) {
                Platform.runLater(() -> {
                    connectionText = "Disconnected";
                    appendLog("Error: " + e.getMessage());
                });
            }
        }
    }

    private void handleServerMessage(NetworkMessage message) {
        appendLog(message.getType() + ": " + message.getBody());
        switch (message.getType()) {
            case "WELCOME" -> myPlayerId = parseInt(message.getBody().replace("PLAYER", "").trim());
            case "FULL" -> connectionText = "Rejected";
            case "GAME_STATE" -> {
                OnlineSnapshot snapshot = OnlineSnapshot.parse(message.getBody());
                myPlayerId = snapshot.you;
                game.applyOnlineState(snapshot.players, snapshot.currentPlayerIndex, snapshot.discard,
                        snapshot.paymentRequest, snapshot.win);
                gameScreen.resetViewedPlayerToCurrentPlayer();
                started = true;
            }
            default -> {
            }
        }
    }

    private String handNumber(Card card) {
        return Integer.toString(game.getCurrentPlayer().getHandCards().indexOf(card) + 1);
    }

    private int playerNumber(Player player) {
        return game.getPlayers().indexOf(player) + 1;
    }

    private int propertyNumber(Player player, PropertiesCards card) {
        return player.getPropertyCards().indexOf(card) + 1;
    }

    private String paymentBody(ArrayList<Card> selectedCards) {
        Game.PaymentRequest request = game.getCurrentPaymentRequest();
        Player payer = request.getPayer();
        ArrayList<String> tokens = new ArrayList<>();
        for (Card card : selectedCards) {
            int bankIndex = payer.getBankCards().indexOf(card);
            if (bankIndex >= 0) {
                tokens.add("B" + (bankIndex + 1));
                continue;
            }
            int propertyIndex = payer.getPropertyCards().indexOf(card);
            if (propertyIndex >= 0) {
                tokens.add("P" + (propertyIndex + 1));
            }
        }
        return String.join(" ", tokens);
    }

    private String doubleToken(boolean useDoubleRent) {
        return useDoubleRent ? " DOUBLE" : "";
    }

    private boolean isRect(double x, double y, double rx, double ry, double rw, double rh) {
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }

    private void send(String type, String body) {
        if (out != null) {
            out.println(new NetworkMessage(type, body == null ? "" : body).encode());
        }
    }

    private void appendLog(String text) {
        logLines.add(text);
        while (logLines.size() > 20) {
            logLines.removeFirst();
        }
    }

    private void shutdown() {
        closed = true;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
            // ignore
        }
    }

    private static int parseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException | NullPointerException e) {
            return 0;
        }
    }

    private static class OnlineSnapshot {
        private final ArrayList<Player> players = new ArrayList<>();
        private int you;
        private int currentPlayerIndex;
        private boolean discard;
        private boolean win;
        private Game.PaymentRequest paymentRequest;

        static OnlineSnapshot parse(String body) {
            OnlineSnapshot snapshot = new OnlineSnapshot();
            Map<String, String> fields = splitFields(body);
            snapshot.you = parseInt(fields.get("you"));
            snapshot.currentPlayerIndex = Math.max(0, parseInt(fields.get("currentPlayer")) - 1);
            snapshot.discard = Boolean.parseBoolean(fields.getOrDefault("discardPhase", "false"));
            snapshot.win = Boolean.parseBoolean(fields.getOrDefault("win", "false"));

            Map<Integer, Integer> usedCounts = parseUsedCounts(fields.get("players"));
            Map<Integer, List<Card>> hands = parseCardGroups(fields.get("allHands"));
            Map<Integer, List<Card>> banks = parseCardGroups(fields.get("publicBanks"));
            Map<Integer, List<Card>> properties = parseCardGroups(fields.get("publicProperties"));
            int playerCount = Math.max(Math.max(hands.size(), banks.size()), properties.size());

            DrawPileAndDiscardPile drawPile = new DrawPileAndDiscardPile();
            for (int i = 1; i <= playerCount; i++) {
                Player player = new Player(drawPile);
                player.getHandCards().clear();
                player.getBankCards().clear();
                player.getPropertyCards().clear();
                player.getHandCards().addAll(hands.getOrDefault(i, List.of()));
                player.getBankCards().addAll(banks.getOrDefault(i, List.of()));
                for (Card card : properties.getOrDefault(i, List.of())) {
                    if (card instanceof PropertiesCards propertyCard) {
                        player.getPropertyCards().add(propertyCard);
                    }
                }
                player.setUseCardTimes(usedCounts.getOrDefault(i, 0));
                snapshot.players.add(player);
            }

            snapshot.paymentRequest = parsePayment(fields.get("payment"), snapshot.players);
            return snapshot;
        }

        private static Map<String, String> splitFields(String body) {
            Map<String, String> fields = new LinkedHashMap<>();
            for (String part : body.split(";")) {
                int equals = part.indexOf('=');
                if (equals > 0) {
                    fields.put(part.substring(0, equals), part.substring(equals + 1));
                }
            }
            return fields;
        }

        private static Map<Integer, Integer> parseUsedCounts(String text) {
            Map<Integer, Integer> result = new LinkedHashMap<>();
            if (text == null || text.isBlank()) {
                return result;
            }
            int cursor = 0;
            while (cursor < text.length()) {
                int next = text.indexOf("),P", cursor);
                String token;
                if (next < 0) {
                    token = text.substring(cursor);
                    cursor = text.length();
                } else {
                    token = text.substring(cursor, next + 1);
                    cursor = next + 2;
                }
                int open = token.indexOf('(');
                int close = token.indexOf(')');
                if (!token.startsWith("P") || open < 0 || close < open) {
                    continue;
                }
                int playerId = parseInt(token.substring(1, open));
                int used = 0;
                for (String item : token.substring(open + 1, close).split(",")) {
                    String[] pair = item.split("=", 2);
                    if (pair.length == 2 && "used".equals(pair[0])) {
                        used = parseInt(pair[1]);
                    }
                }
                result.put(playerId, used);
            }
            return result;
        }

        private static Map<Integer, List<Card>> parseCardGroups(String text) {
            Map<Integer, List<Card>> result = new LinkedHashMap<>();
            if (text == null || text.isBlank()) {
                return result;
            }
            for (String token : text.split("\\|")) {
                int open = token.indexOf('[');
                int close = token.lastIndexOf(']');
                if (!token.startsWith("P") || open < 0 || close < open) {
                    continue;
                }
                int playerId = parseInt(token.substring(1, open));
                result.put(playerId, parseCards(token.substring(open + 1, close), "~"));
            }
            return result;
        }

        private static List<Card> parseCards(String text, String separator) {
            if (text == null || text.isBlank()) {
                return List.of();
            }
            ArrayList<Card> cards = new ArrayList<>();
            for (String raw : text.split(Pattern.quote(separator))) {
                Card card = parseCard(raw);
                if (card != null) {
                    cards.add(card);
                }
            }
            return cards;
        }

        private static Card parseCard(String raw) {
            String[] parts = raw.split(":", -1);
            if (parts.length == 0) {
                return null;
            }
            if ("MONEY".equals(parts[0]) && parts.length >= 2) {
                return new MoneyCards(parseInt(parts[1]));
            }
            if ("ACTION".equals(parts[0]) && parts.length >= 2) {
                return new ActionCards(ActionCardType.valueOf(parts[1]));
            }
            if ("PROPERTY".equals(parts[0]) && parts.length >= 5) {
                PropertiesCardsType type = PropertiesCardsType.valueOf(parts[1]);
                PropertiesCards card = new PropertiesCards(type, type.name(), parts[3]);
                if (!"NO_COLOR".equals(parts[2])) {
                    card.setCurrentColor(PropertyColor.valueOf(parts[2]));
                }
                if (parts.length >= 7) {
                    card.setHasHouse(Boolean.parseBoolean(parts[5]));
                    card.setHasHotel(Boolean.parseBoolean(parts[6]));
                }
                return card;
            }
            return null;
        }

        private static Game.PaymentRequest parsePayment(String text, ArrayList<Player> players) {
            if (text == null || text.equals("none")) {
                return null;
            }
            Map<String, String> values = new LinkedHashMap<>();
            for (String token : text.split(",")) {
                String[] pair = token.split("=", 2);
                if (pair.length == 2) {
                    values.put(pair[0], pair[1]);
                }
            }
            int payerIndex = parseInt(values.get("payer")) - 1;
            int receiverIndex = parseInt(values.get("receiver")) - 1;
            int amount = parseInt(values.get("amount"));
            if (payerIndex < 0 || receiverIndex < 0
                    || payerIndex >= players.size() || receiverIndex >= players.size()) {
                return null;
            }
            return new Game.PaymentRequest(players.get(receiverIndex), players.get(payerIndex), amount);
        }
    }
}
