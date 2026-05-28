package network;

import GUI.GuiScale;
import GUI.ScreenDrawHelper;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic.Game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Canvas-based LAN client that mirrors the local game table layout while sending
 * commands to the server instead of mutating a local Game directly.
 */
public class OnlinePlayWindow extends Stage {
    private static final int PORT = 5555;
    private static final double CARD_WIDTH = 82;
    private static final double CARD_HEIGHT = 112;
    private static final double SMALL_CARD_WIDTH = 60;
    private static final double SMALL_CARD_HEIGHT = 85;
    private static final List<String> COLORS = List.of(
            "DARK_BLUE", "ORANGE", "BLACK", "RED", "DARK_GREEN",
            "BROWN", "PINK", "LIGHT_BLUE", "LIGHT_GREEN", "YELLOW"
    );

    private final String host;
    private final Canvas canvas = new Canvas(GuiScale.canvasWidth(), GuiScale.canvasHeight());
    private final ArrayList<String> logLines = new ArrayList<>();
    private final Map<String, Image> imageCache = new HashMap<>();

    private volatile boolean closed;
    private Socket socket;
    private PrintWriter out;
    private OnlineState state = OnlineState.empty();
    private String connectionText = "Connecting...";
    private boolean started;

    public OnlinePlayWindow(Stage owner, String host) {
        this.host = host;
        initOwner(owner);
        setTitle("LAN Game - " + host + ":" + PORT);
        setScene(new Scene(new javafx.scene.Group(canvas), GuiScale.canvasWidth(), GuiScale.canvasHeight()));
        setOnCloseRequest(e -> shutdown());
        canvas.setOnMouseClicked(e -> handleClick(GuiScale.toLogical(e.getX()), GuiScale.toLogical(e.getY())));

        startRenderLoop();
        appendLog("Connecting to " + host + "...");
        new Thread(this::runConnection, "online-socket").start();
    }

    private void startRenderLoop() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                paint();
            }
        }.start();
    }

    private void paint() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        GuiScale.prepare(gc);
        ScreenDrawHelper.drawPageBackground(gc, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
        drawHeader(gc);
        drawBank(gc);
        drawProperties(gc);
        drawHand(gc);
        drawSidePanel(gc);
        drawLog(gc);
    }

    private void drawHeader(GraphicsContext gc) {
        ScreenDrawHelper.drawPanel(gc, 16, 14, 735, 94);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(ScreenDrawHelper.TEXT);
        gc.setFont(Font.font("Arial", 22));

        String title = started
                ? "Player " + state.currentPlayer + "'s Turn"
                : "LAN Game Lobby";
        gc.fillText(title, 36, 42);

        String you = state.you > 0 ? "You are Player " + state.you : "Waiting for player id";
        ScreenDrawHelper.drawBadge(gc, 36, 62, 150, 28, you, Color.rgb(255, 226, 166));
        ScreenDrawHelper.drawBadge(gc, 200, 62, 170, 28, connectionText, Color.rgb(167, 243, 208));

        if (state.discardPhase) {
            gc.setFill(ScreenDrawHelper.DANGER);
            gc.setFont(Font.font("Arial", 15));
            gc.fillText("Discard Phase: choose hand cards to discard", 500, 67);
        }
    }

    private void drawBank(GraphicsContext gc) {
        ScreenDrawHelper.drawPanel(gc, 16, 118, 735, 128);
        ScreenDrawHelper.drawSectionTitle(gc, "Bank Area", 32, 132);
        drawSmallCards(gc, state.yourBank, 32, 160, false);
    }

    private void drawProperties(GraphicsContext gc) {
        ScreenDrawHelper.drawPanel(gc, 16, 260, 735, 128);
        ScreenDrawHelper.drawSectionTitle(gc, "Property Area", 32, 274);
        drawSmallCards(gc, state.yourProperties, 32, 302, true);
    }

    private void drawHand(GraphicsContext gc) {
        ScreenDrawHelper.drawPanel(gc, 16, 404, 735, 205);
        ScreenDrawHelper.drawSectionTitle(gc, "Your Hand", 32, 418);
        if (!started) {
            gc.setFill(ScreenDrawHelper.MUTED_TEXT);
            gc.setFont(Font.font("Arial", 16));
            gc.fillText("Cards will appear here after Start Game.", 32, 470);
            return;
        }

        double gap = 10;
        if (state.yourHand.size() > 1) {
            double total = state.yourHand.size() * CARD_WIDTH + (state.yourHand.size() - 1) * gap;
            if (total > 700) {
                gap = (700 - state.yourHand.size() * CARD_WIDTH) / (state.yourHand.size() - 1);
            }
        }

        for (CardInfo card : state.yourHand) {
            double x = handCardX(card.index, gap);
            double y = 465;
            drawLargeCard(gc, card, x, y);
            gc.setFill(Color.rgb(255, 255, 255, 0.92));
            gc.fillOval(x + 4, y + 4, 22, 22);
            gc.setFill(Color.rgb(30, 35, 48));
            gc.setFont(Font.font("Arial", 12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(Integer.toString(card.index), x + 15, y + 19);
        }
    }

    private double handCardX(int oneBasedIndex, double gap) {
        return 32 + (oneBasedIndex - 1) * (CARD_WIDTH + gap);
    }

    private void drawSidePanel(GraphicsContext gc) {
        ScreenDrawHelper.drawPanel(gc, 775, 14, 244, 595);
        ScreenDrawHelper.drawSectionTitle(gc, "Online", 795, 32);

        drawSideText(gc, "Players", 795, 78);
        double y = 104;
        if (state.players.isEmpty()) {
            drawMuted(gc, "Waiting for players...", 795, y);
            y += 28;
        } else {
            for (PlayerSummary player : state.players) {
                String marker = player.id == state.you ? " (you)" : "";
                drawMuted(gc, "P" + player.id + marker + "  hand " + player.hand
                        + "  bank " + player.bank + "  prop " + player.properties, 795, y);
                y += 24;
            }
        }

        drawPayment(gc, Math.max(210, y + 10));
        drawButtons(gc);
    }

    private void drawPayment(GraphicsContext gc, double y) {
        drawSideText(gc, "Payment", 795, y);
        if (!state.payment.active) {
            drawMuted(gc, "No payment due", 795, y + 28);
            return;
        }

        drawMuted(gc, "P" + state.payment.payer + " pays P" + state.payment.receiver
                + ": " + state.payment.amount + "M", 795, y + 28);
        if (state.payment.youMustPay) {
            ScreenDrawHelper.drawButton(gc, 795, y + 58, 160, 38, "Select Payment");
            if (state.payment.canJustSayNo) {
                ScreenDrawHelper.drawButton(gc, 795, y + 106, 160, 38, "Just Say No");
            }
        } else {
            drawMuted(gc, "Waiting for Player " + state.payment.payer, 795, y + 58);
        }
    }

    private void drawButtons(GraphicsContext gc) {
        if (!started) {
            ScreenDrawHelper.drawButton(gc, 795, 500, 180, 40, "Start Game");
        } else if (state.you == state.currentPlayer && !state.payment.youMustPay) {
            ScreenDrawHelper.drawButton(gc, 795, 500, 180, 40, "End Turn");
        } else {
            ScreenDrawHelper.drawDisabledButton(gc, 795, 500, 180, 40, "End Turn");
        }
        ScreenDrawHelper.drawButton(gc, 795, 552, 180, 40, "Refresh");
    }

    private void drawLog(GraphicsContext gc) {
        gc.setFill(ScreenDrawHelper.MUTED_TEXT);
        gc.setFont(Font.font("Arial", 12));
        gc.setTextAlign(TextAlignment.LEFT);
        double y = 390;
        int start = Math.max(0, logLines.size() - 5);
        for (int i = start; i < logLines.size(); i++) {
            gc.fillText(logLines.get(i), 795, y);
            y += 19;
        }
    }

    private void drawSmallCards(GraphicsContext gc, List<CardInfo> cards, double x, double y, boolean property) {
        if (cards.isEmpty()) {
            drawMuted(gc, "(empty)", x, y + 32);
            return;
        }
        for (int i = 0; i < Math.min(8, cards.size()); i++) {
            drawSmallCard(gc, cards.get(i), x + i * 75, y, property);
        }
    }

    private void drawLargeCard(GraphicsContext gc, CardInfo card, double x, double y) {
        if (drawCardImage(gc, card, x, y, CARD_WIDTH, CARD_HEIGHT)) {
            return;
        }

        Color fill = colorFor(card);
        gc.setFill(fill);
        gc.fillRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 12, 12);
        gc.setStroke(Color.rgb(20, 24, 34));
        gc.strokeRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 12, 12);
        gc.setFill(Color.rgb(20, 24, 34));
        gc.setFont(Font.font("Arial", 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(card.title(), x + CARD_WIDTH / 2, y + 30);
        gc.setFont(Font.font("Arial", 18));
        gc.fillText(card.value + "M", x + CARD_WIDTH / 2, y + 92);
    }

    private void drawSmallCard(GraphicsContext gc, CardInfo card, double x, double y, boolean property) {
        if (drawCardImage(gc, card, x, y, SMALL_CARD_WIDTH, SMALL_CARD_HEIGHT)) {
            return;
        }

        gc.setFill(colorFor(card));
        gc.fillRoundRect(x, y, SMALL_CARD_WIDTH, SMALL_CARD_HEIGHT, 10, 10);
        gc.setStroke(Color.rgb(20, 24, 34));
        gc.strokeRoundRect(x, y, SMALL_CARD_WIDTH, SMALL_CARD_HEIGHT, 10, 10);
        gc.setFill(Color.rgb(20, 24, 34));
        gc.setFont(Font.font("Arial", 9));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(property ? card.displayPropertyColor() : card.title(), x + 30, y + 24);
        gc.setFont(Font.font("Arial", 13));
        gc.fillText(card.value + "M", x + 30, y + 66);
    }

    private boolean drawCardImage(GraphicsContext gc, CardInfo card, double x, double y, double width, double height) {
        if (card.imagePath == null || card.imagePath.isBlank()) {
            return false;
        }

        Image image = loadImage(card.imagePath);
        if (image == null || image.isError()) {
            return false;
        }

        gc.drawImage(image, x, y, width, height);
        return true;
    }

    private Image loadImage(String path) {
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        InputStream inputStream = OnlinePlayWindow.class.getResourceAsStream(path);
        if (inputStream == null) {
            imageCache.put(path, null);
            return null;
        }

        Image image = new Image(inputStream);
        imageCache.put(path, image);
        return image;
    }

    private Color colorFor(CardInfo card) {
        return switch (card.kind) {
            case MONEY -> Color.GOLD;
            case ACTION -> Color.rgb(255, 224, 178);
            case PROPERTY -> Color.rgb(158, 216, 255);
            case UNKNOWN -> Color.LIGHTGRAY;
        };
    }

    private void drawSideText(GraphicsContext gc, String text, double x, double y) {
        gc.setFill(ScreenDrawHelper.TEXT);
        gc.setFont(Font.font("Arial", 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(text, x, y);
    }

    private void drawMuted(GraphicsContext gc, String text, double x, double y) {
        gc.setFill(ScreenDrawHelper.MUTED_TEXT);
        gc.setFont(Font.font("Arial", 14));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(text, x, y);
    }

    private void handleClick(double x, double y) {
        if (isRect(x, y, 795, 552, 180, 40)) {
            send("STATE", "");
            return;
        }
        if (!started && isRect(x, y, 795, 500, 180, 40)) {
            send("START_GAME", "");
            return;
        }
        if (started && isRect(x, y, 795, 500, 180, 40)) {
            send("END_TURN", "");
            return;
        }
        if (state.payment.youMustPay && isRect(x, y, 795, 268, 160, 38)) {
            showPaymentDialog();
            return;
        }
        if (state.payment.youMustPay && state.payment.canJustSayNo && isRect(x, y, 795, 316, 160, 38)) {
            send("JUST_SAY_NO", "");
            return;
        }
        int handIndex = clickedHandIndex(x, y);
        if (handIndex != -1 && handIndex <= state.yourHand.size()) {
            CardInfo card = state.yourHand.get(handIndex - 1);
            if (state.discardPhase) {
                send("DISCARD", Integer.toString(card.index));
            } else if (card.kind == CardKind.ACTION) {
                showActionDialog(card);
            } else {
                send("PLAY_CARD", Integer.toString(card.index));
            }
        }
    }

    private boolean isRect(double x, double y, double rx, double ry, double rw, double rh) {
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }

    private int clickedHandIndex(double x, double y) {
        double gap = 10;
        if (state.yourHand.size() > 1) {
            double total = state.yourHand.size() * CARD_WIDTH + (state.yourHand.size() - 1) * gap;
            if (total > 700) {
                gap = (700 - state.yourHand.size() * CARD_WIDTH) / (state.yourHand.size() - 1);
            }
        }
        for (CardInfo card : state.yourHand) {
            double cx = handCardX(card.index, gap);
            if (isRect(x, y, cx, 465, CARD_WIDTH, CARD_HEIGHT)) {
                return card.index;
            }
        }
        return -1;
    }

    private void showActionDialog(CardInfo card) {
        Stage dialog = createDialog("Play " + card.title());
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));

        ChoiceBox<Integer> targetChoice = new ChoiceBox<>(FXCollections.observableArrayList(otherPlayerIds()));
        ChoiceBox<Integer> propertyChoice = new ChoiceBox<>();
        ChoiceBox<Integer> myPropertyChoice = new ChoiceBox<>(FXCollections.observableArrayList(cardIndexes(state.yourProperties)));
        ChoiceBox<String> colorChoice = new ChoiceBox<>(FXCollections.observableArrayList(COLORS));
        CheckBox doubleRent = new CheckBox("Use Double The Rent");
        targetChoice.getSelectionModel().selectFirst();
        colorChoice.getSelectionModel().selectFirst();
        updateTargetProperties(targetChoice, propertyChoice);
        targetChoice.setOnAction(e -> updateTargetProperties(targetChoice, propertyChoice));

        String command = commandForAction(card.actionType);
        root.getChildren().add(new Label(card.title()));
        root.getChildren().add(new Button("Bank as money") {{
            setOnAction(e -> {
                send("PLAY_AS_MONEY", Integer.toString(card.index));
                dialog.close();
            });
        }});
        if (requiresTarget(command)) {
            root.getChildren().add(new Label("Target player"));
            root.getChildren().add(targetChoice);
        }
        if ("SLY".equals(command) || "FORCED_DEAL".equals(command)) {
            root.getChildren().add(new Label("Target property"));
            root.getChildren().add(propertyChoice);
        }
        if ("FORCED_DEAL".equals(command)) {
            root.getChildren().add(new Label("Your property"));
            root.getChildren().add(myPropertyChoice);
        }
        if (requiresColor(command)) {
            root.getChildren().add(new Label("Color"));
            root.getChildren().add(colorChoice);
        }
        if ("RENT".equals(command) || "RENT_ANY".equals(command)) {
            root.getChildren().add(doubleRent);
        }

        Button play = new Button("Play as action");
        play.setOnAction(e -> {
            String body = buildActionBody(command, card.index, targetChoice.getValue(), propertyChoice.getValue(),
                    myPropertyChoice.getValue(), colorChoice.getValue(), doubleRent.isSelected());
            if (!body.isBlank()) {
                send(command, body);
                dialog.close();
            }
        });
        root.getChildren().add(play);
        dialog.setScene(new Scene(new ScrollPane(root), 320, 420));
        dialog.showAndWait();
    }

    private void showPaymentDialog() {
        Stage dialog = createDialog("Select payment");
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.getChildren().add(new Label("Select cards to pay " + state.payment.amount + "M"));
        List<CheckBox> boxes = new ArrayList<>();
        for (CardInfo card : state.yourBank) {
            CheckBox box = new CheckBox("B" + card.index + " - " + card.title());
            boxes.add(box);
            root.getChildren().add(box);
        }
        for (CardInfo card : state.yourProperties) {
            CheckBox box = new CheckBox("P" + card.index + " - " + card.title());
            boxes.add(box);
            root.getChildren().add(box);
        }
        Button pay = new Button("Pay");
        pay.setOnAction(e -> {
            ArrayList<String> selected = new ArrayList<>();
            for (CheckBox box : boxes) {
                if (box.isSelected()) {
                    selected.add(box.getText().split(" ", 2)[0]);
                }
            }
            send("PAY", String.join(" ", selected));
            dialog.close();
        });
        root.getChildren().add(pay);
        dialog.setScene(new Scene(new ScrollPane(root), 360, 460));
        dialog.showAndWait();
    }

    private Stage createDialog(String title) {
        Stage dialog = new Stage();
        dialog.initOwner(this);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(title);
        return dialog;
    }

    private void updateTargetProperties(ChoiceBox<Integer> targetChoice, ChoiceBox<Integer> propertyChoice) {
        int target = targetChoice.getValue() == null ? -1 : targetChoice.getValue();
        int count = state.propertyCountForPlayer(target);
        ArrayList<Integer> propertyNumbers = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            propertyNumbers.add(i);
        }
        propertyChoice.setItems(FXCollections.observableArrayList(propertyNumbers));
        propertyChoice.getSelectionModel().selectFirst();
    }

    private List<Integer> otherPlayerIds() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (PlayerSummary player : state.players) {
            if (player.id != state.you) {
                ids.add(player.id);
            }
        }
        return ids;
    }

    private List<Integer> cardIndexes(List<CardInfo> cards) {
        ArrayList<Integer> indexes = new ArrayList<>();
        for (CardInfo card : cards) {
            indexes.add(card.index);
        }
        return indexes;
    }

    private String commandForAction(String actionType) {
        return switch (actionType) {
            case "PASS_GO" -> "PASS_GO";
            case "BIRTHDAY" -> "BIRTHDAY";
            case "DEBT_COLLECTOR" -> "DEBT";
            case "SLY_DEAL" -> "SLY";
            case "DEAL_BREAKER" -> "DEAL_BREAKER";
            case "RENT_WITH_MULTIPLE_COLOR" -> "RENT_ANY";
            case "HOUSE" -> "HOUSE";
            case "HOTEL" -> "HOTEL";
            case "FORCED_DEAL" -> "FORCED_DEAL";
            default -> "RENT";
        };
    }

    private boolean requiresTarget(String command) {
        return "DEBT".equals(command) || "SLY".equals(command) || "DEAL_BREAKER".equals(command)
                || "RENT_ANY".equals(command) || "FORCED_DEAL".equals(command);
    }

    private boolean requiresColor(String command) {
        return "DEAL_BREAKER".equals(command) || "RENT".equals(command) || "RENT_ANY".equals(command)
                || "HOUSE".equals(command) || "HOTEL".equals(command);
    }

    private String buildActionBody(String command, int handIndex, Integer target, Integer targetProperty,
                                   Integer myProperty, String color, boolean doubleRent) {
        ArrayList<String> parts = new ArrayList<>();
        parts.add(Integer.toString(handIndex));
        switch (command) {
            case "DEBT" -> {
                if (target == null) {
                    return "";
                }
                parts.add(Integer.toString(target));
            }
            case "SLY" -> {
                if (target == null || targetProperty == null) {
                    return "";
                }
                parts.add(Integer.toString(target));
                parts.add(Integer.toString(targetProperty));
            }
            case "DEAL_BREAKER", "RENT_ANY" -> {
                if (target == null || color == null) {
                    return "";
                }
                parts.add(Integer.toString(target));
                parts.add(color);
            }
            case "FORCED_DEAL" -> {
                if (target == null || myProperty == null || targetProperty == null) {
                    return "";
                }
                parts.add(Integer.toString(target));
                parts.add(Integer.toString(myProperty));
                parts.add(Integer.toString(targetProperty));
            }
            case "RENT", "HOUSE", "HOTEL" -> {
                if (color == null) {
                    return "";
                }
                parts.add(color);
            }
            default -> {
            }
        }
        if (doubleRent) {
            parts.add("DOUBLE");
        }
        return String.join(" ", parts);
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
        if ("FULL".equals(message.getType())) {
            connectionText = "Rejected";
        } else if ("WELCOME".equals(message.getType())) {
            state.you = parsePlayerId(message.getBody());
        } else if ("PLAYER_LIST".equals(message.getType()) && !started) {
            state.players.clear();
            state.players.addAll(parseLobbyPlayers(message.getBody()));
        } else if ("GAME_STATE".equals(message.getType())) {
            started = true;
            state = OnlineState.parse(message.getBody());
        }
    }

    private int parsePlayerId(String text) {
        String[] parts = text.trim().split("\\s+");
        if (parts.length == 2) {
            return OnlineState.parseInt(parts[1]);
        }
        return state.you;
    }

    private List<PlayerSummary> parseLobbyPlayers(String body) {
        ArrayList<PlayerSummary> players = new ArrayList<>();
        for (String token : body.split(",")) {
            String trimmed = token.trim();
            if (trimmed.startsWith("PLAYER ")) {
                players.add(new PlayerSummary(OnlineState.parseInt(trimmed.substring(7)), 0, 0, 0));
            }
        }
        return players;
    }

    private void send(String type, String body) {
        if (out != null) {
            out.println(new NetworkMessage(type, body == null ? "" : body).encode());
        }
    }

    private void appendLog(String text) {
        logLines.add(text);
        while (logLines.size() > 30) {
            logLines.remove(0);
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

    private enum CardKind {
        MONEY,
        ACTION,
        PROPERTY,
        UNKNOWN
    }

    private record CardInfo(int index,
                            String raw,
                            CardKind kind,
                            String actionType,
                            String propertyType,
                            String propertyColor,
                            String imagePath,
                            int value) {
        static CardInfo parse(int index, String raw) {
            if (raw.startsWith("MONEY:") || raw.startsWith("ACTION:") || raw.startsWith("PROPERTY:")) {
                return parseColonFormat(index, raw);
            }

            if (raw.startsWith("MONEY_")) {
                int value = trailingValue(raw);
                return new CardInfo(index, raw, CardKind.MONEY, "", "", "", "/images/money/money_" + value + ".png", value);
            }
            if (raw.startsWith("ACTION_")) {
                String actionType = raw.substring("ACTION_".length(), raw.lastIndexOf('_'));
                return new CardInfo(index, raw, CardKind.ACTION, actionType, "", "", actionImagePath(actionType), trailingValue(raw));
            }
            if (raw.startsWith("PROPERTY_")) {
                return new CardInfo(index, raw, CardKind.PROPERTY, "", "", "", "", trailingValue(raw));
            }
            return new CardInfo(index, raw, CardKind.UNKNOWN, "", "", "", "", 0);
        }

        private static CardInfo parseColonFormat(int index, String raw) {
            String[] parts = raw.split(":", -1);

            if ("MONEY".equals(parts[0]) && parts.length >= 2) {
                int value = parseInt(parts[1]);
                return new CardInfo(index, raw, CardKind.MONEY, "", "", "", "/images/money/money_" + value + ".png", value);
            }

            if ("ACTION".equals(parts[0]) && parts.length >= 3) {
                String actionType = parts[1];
                int value = parseInt(parts[2]);
                return new CardInfo(index, raw, CardKind.ACTION, actionType, "", "", actionImagePath(actionType), value);
            }

            if ("PROPERTY".equals(parts[0]) && parts.length >= 5) {
                String propertyType = parts[1];
                String color = parts[2];
                String imageFileName = parts[3];
                int value = parseInt(parts[4]);
                String folder = propertyType.startsWith("WILD_") ? "property_wildcards" : "property";
                return new CardInfo(index, raw, CardKind.PROPERTY, "", propertyType, color,
                        "/images/" + folder + "/" + imageFileName, value);
            }

            return new CardInfo(index, raw, CardKind.UNKNOWN, "", "", "", "", 0);
        }

        private static String actionImagePath(String actionType) {
            String fileName = actionType.toLowerCase() + ".png";
            String folder = actionType.startsWith("RENT_WITH") ? "rent" : "action";
            return "/images/" + folder + "/" + fileName;
        }

        private static int trailingValue(String raw) {
            int last = raw.lastIndexOf('_');
            if (last < 0) {
                return 0;
            }
            try {
                return Integer.parseInt(raw.substring(last + 1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        private static int parseInt(String text) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        String title() {
            if (kind == CardKind.MONEY) {
                return "Money " + value;
            }
            if (kind == CardKind.ACTION && !actionType.isBlank()) {
                return actionType.replace("_", " ");
            }
            if (kind == CardKind.PROPERTY && !propertyType.isBlank()) {
                return propertyType.replace("_", " ");
            }
            String cleaned = raw.replace("ACTION_", "").replace("PROPERTY_", "");
            int last = cleaned.lastIndexOf('_');
            if (last > 0) {
                cleaned = cleaned.substring(0, last);
            }
            return cleaned.replace("_", " ");
        }

        String displayPropertyColor() {
            if (!propertyColor.isBlank()) {
                return propertyColor.replace("_", " ");
            }

            if (!raw.startsWith("PROPERTY_")) {
                return title();
            }
            String[] parts = raw.split("_");
            if (parts.length >= 3) {
                return parts[parts.length - 2].replace("_", " ");
            }
            return title();
        }
    }

    private record PlayerSummary(int id, int hand, int bank, int properties) {
    }

    private static class PaymentState {
        private static final PaymentState NONE = new PaymentState(false, 0, 0, 0, false, false);

        private final boolean active;
        private final int payer;
        private final int receiver;
        private final int amount;
        private final boolean youMustPay;
        private final boolean canJustSayNo;

        PaymentState(boolean active, int payer, int receiver, int amount, boolean youMustPay, boolean canJustSayNo) {
            this.active = active;
            this.payer = payer;
            this.receiver = receiver;
            this.amount = amount;
            this.youMustPay = youMustPay;
            this.canJustSayNo = canJustSayNo;
        }
    }

    private static class OnlineState {
        private int you;
        private int currentPlayer;
        private boolean discardPhase;
        private PaymentState payment = PaymentState.NONE;
        private final ArrayList<PlayerSummary> players = new ArrayList<>();
        private final ArrayList<CardInfo> yourHand = new ArrayList<>();
        private final ArrayList<CardInfo> yourBank = new ArrayList<>();
        private final ArrayList<CardInfo> yourProperties = new ArrayList<>();

        static OnlineState empty() {
            return new OnlineState();
        }

        static OnlineState parse(String body) {
            OnlineState state = new OnlineState();
            Map<String, String> fields = splitFields(body);
            state.you = parseInt(fields.get("you"));
            state.currentPlayer = parseInt(fields.get("currentPlayer"));
            state.discardPhase = Boolean.parseBoolean(fields.getOrDefault("discardPhase", "false"));
            state.payment = parsePayment(fields.get("payment"));
            state.players.addAll(parsePlayers(fields.get("players")));
            state.yourHand.addAll(parseCards(fields.get("yourHand")));
            state.yourBank.addAll(parseCards(fields.get("yourBank")));
            state.yourProperties.addAll(parseCards(fields.get("yourProperties")));
            return state;
        }

        int propertyCountForPlayer(int playerId) {
            for (PlayerSummary player : players) {
                if (player.id == playerId) {
                    return player.properties;
                }
            }
            return 0;
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

        private static PaymentState parsePayment(String text) {
            if (text == null || text.equals("none")) {
                return PaymentState.NONE;
            }
            Map<String, String> values = new LinkedHashMap<>();
            for (String token : text.split(",")) {
                String[] pair = token.split("=", 2);
                if (pair.length == 2) {
                    values.put(pair[0], pair[1]);
                }
            }
            return new PaymentState(
                    true,
                    parseInt(values.get("payer")),
                    parseInt(values.get("receiver")),
                    parseInt(values.get("amount")),
                    Boolean.parseBoolean(values.getOrDefault("youMustPay", "false")),
                    Boolean.parseBoolean(values.getOrDefault("canJustSayNo", "false"))
            );
        }

        private static List<PlayerSummary> parsePlayers(String text) {
            if (text == null || text.isBlank()) {
                return List.of();
            }
            ArrayList<PlayerSummary> players = new ArrayList<>();
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
                if (!token.startsWith("P") || open < 0 || close < 0) {
                    continue;
                }
                int id = parseInt(token.substring(1, open));
                Map<String, String> values = new LinkedHashMap<>();
                for (String item : token.substring(open + 1, close).split(",")) {
                    String[] pair = item.split("=", 2);
                    if (pair.length == 2) {
                        values.put(pair[0], pair[1]);
                    }
                }
                players.add(new PlayerSummary(id, parseInt(values.get("hand")),
                        parseInt(values.get("bank")), parseInt(values.get("properties"))));
            }
            return players;
        }

        private static List<CardInfo> parseCards(String text) {
            if (text == null || text.isBlank()) {
                return List.of();
            }
            ArrayList<CardInfo> cards = new ArrayList<>();
            String[] rawCards = text.split(",");
            for (int i = 0; i < rawCards.length; i++) {
                cards.add(CardInfo.parse(i + 1, rawCards[i]));
            }
            return cards;
        }

        private static int parseInt(String text) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException | NullPointerException e) {
                return 0;
            }
        }
    }
}
