package network;

import GUI.GameClickHandler;
import GUI.GameScreen;
import GUI.GameSession;
import GUI.GuiScale;
import GUI.MusicPlayer;
import GUI.ScreenDrawHelper;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import logic.Game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class OnlinePlayWindow extends Stage {
    private static final int PORT = 5555;

    private final String host;
    private final String playerName;
    private final GameSession session;
    private final OnlineGameClickActions clickActions;
    private final GameClickHandler clickHandler;
    private final Canvas lobbyCanvas = new Canvas(GuiScale.canvasWidth(), GuiScale.canvasHeight());
    private final ArrayList<String> logLines = new ArrayList<>();

    private volatile boolean closed;
    private Socket socket;
    private PrintWriter out;
    private boolean started;
    private boolean lastMyTurn;
    private int myPlayerId;
    private long turnDeadlineMillis = -1;
    private String connectionText = "Connecting...";
    private String playerListText = "Waiting for players...";

    // Creates a OnlinePlayWindow instance.
    public OnlinePlayWindow(Stage owner, String host, String playerName, MusicPlayer musicPlayer) {
        this.host = host;
        this.playerName = sanitizePlayerName(playerName);
        this.session = new GameSession(new Game(), musicPlayer);
        initOwner(owner);
        setTitle("LAN Game - " + host + ":" + PORT);

        clickActions = new OnlineGameClickActions(
                session.getGame(),
                session.getGameScreen(),
                this::send,
                () -> {
                    close();
                    shutdown();
                }
        );
        clickHandler = new GameClickHandler(session.getGame(), session.getGameScreen(), clickActions);

        Group root = new Group(lobbyCanvas, session.getGameScreen().getCanvas());
        Scene scene = new Scene(root, GuiScale.canvasWidth(), GuiScale.canvasHeight());
        setScene(scene);
        setOnCloseRequest(e -> shutdown());
        scene.setOnMouseClicked(e -> handleMouseClick(GuiScale.toLogical(e.getX()), GuiScale.toLogical(e.getY())));

        startRenderLoop();
        appendLog("Connecting to " + host + "...");
        new Thread(this::runConnection, "online-socket").start();
    }

    // Starts render loop.
    private void startRenderLoop() {
        GameScreen gameScreen = session.getGameScreen();
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (started) {
                    updateTurnRemainingDisplay();
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

    // Runs paint lobby.
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
        gc.fillText(myPlayerId > 0 ? "You are " + playerName : "Waiting for server",
                Game.SCREEN_WIDTH / 2, 270);

        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Players: " + playerListText, Game.SCREEN_WIDTH / 2, 300);

        ScreenDrawHelper.drawButton(gc, 390, 340, 255, 48, "Start Game");
        ScreenDrawHelper.drawButton(gc, 390, 410, 255, 48, "Refresh Players");

        gc.setFill(ScreenDrawHelper.MUTED_TEXT);
        gc.setFont(Font.font("Arial", 13));
        int start = Math.max(0, logLines.size() - 4);
        double y = 535;
        for (int i = start; i < logLines.size(); i++) {
            gc.fillText(logLines.get(i), Game.SCREEN_WIDTH / 2, y);
            y += 18;
        }
    }

    // Handles mouse click.
    private void handleMouseClick(double x, double y) {
        if (!started) {
            if (ScreenDrawHelper.handleButtonClick(x, y, 390, 340, 255, 48)) {
                send("START_GAME", "");
            } else if (ScreenDrawHelper.handleButtonClick(x, y, 390, 410, 255, 48)) {
                send("PLAYERS", "");
            }
            return;
        }

        if (!session.getGameScreen().isShow()) {
            return;
        }

        clickHandler.handleMouseClick(x, y);
    }

    // Runs run connection.
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
                send("NAME", playerName);
                send("PLAYERS", "");
            });
            String line;
            while (!closed && (line = in.readLine()) != null) {
                String rawLine = line;
                NetworkMessage message = NetworkMessage.decode(line);
                Platform.runLater(() -> handleServerMessageSafely(message, rawLine));
            }
            if (!closed) {
                Platform.runLater(() -> {
                    connectionText = "Disconnected";
                    appendLog("Server closed the connection.");
                });
            }
        } catch (IOException e) {
            if (!closed) {
                Platform.runLater(() -> {
                    connectionText = "Disconnected";
                    appendLog("Error: " + e.getMessage());
                });
            }
        } catch (RuntimeException e) {
            if (!closed) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    connectionText = "Client error";
                    appendLog("Client socket error: " + e.getMessage());
                });
            }
        }
    }

    // Handles a server message without letting JavaFX client-side errors disappear silently.
    private void handleServerMessageSafely(NetworkMessage message, String rawLine) {
        try {
            handleServerMessage(message);
        } catch (RuntimeException e) {
            connectionText = "Client error";
            appendLog("Client message error: " + e.getMessage());
            appendLog("Raw: " + rawLine);
            e.printStackTrace();
        }
    }

    // Handles server message.
    private void handleServerMessage(NetworkMessage message) {
        appendLog(message.getType() + ": " + message.getBody());
        switch (message.getType()) {
            case "WELCOME" -> {
                myPlayerId = parseInt(message.getBody().replace("PLAYER", "").trim());
                clickActions.setMyPlayerId(myPlayerId);
            }
            case "FULL" -> connectionText = "Rejected";
            case "PLAYER_LIST" -> playerListText = message.getBody();
            case "GAME_STARTED" -> send("STATE", "");
            case "GAME_STATE" -> {
                try {
                    GameStateCodec.Snapshot snapshot = GameStateCodec.decode(message.getBody());
                    myPlayerId = snapshot.you;
                    clickActions.setMyPlayerId(myPlayerId);
                    session.applyOnlineSnapshot(snapshot);
                    updateViewedPlayer(snapshot);
                    session.getGameScreen().setEndTurnEnabled(snapshot.currentPlayerIndex + 1 == myPlayerId);
                    updateTurnDeadline(snapshot.turnRemainingSeconds);
                    notifyWhenMyTurn(snapshot.currentPlayerIndex + 1 == myPlayerId);
                    started = true;
                } catch (RuntimeException e) {
                    connectionText = "State error";
                    appendLog("Could not load game state: " + e.getMessage());
                    send("STATE", "");
                }
            }
            default -> {
            }
        }
    }

    // Keeps the online view locked to this player's own hand and play area.
    private void updateViewedPlayer(GameStateCodec.Snapshot snapshot) {
        session.getGameScreen().lockViewedPlayer(myPlayerId - 1);
    }

    // Updates the local countdown deadline from the latest server state.
    private void updateTurnDeadline(int turnRemainingSeconds) {
        if (turnRemainingSeconds < 0) {
            turnDeadlineMillis = -1;
            session.getGameScreen().setTurnRemainingSeconds(-1);
            return;
        }

        turnDeadlineMillis = System.currentTimeMillis() + turnRemainingSeconds * 1000L;
        updateTurnRemainingDisplay();
    }

    // Refreshes the visible countdown using the locally estimated server deadline.
    private void updateTurnRemainingDisplay() {
        if (turnDeadlineMillis < 0) {
            session.getGameScreen().setTurnRemainingSeconds(-1);
            return;
        }

        long remainingMillis = turnDeadlineMillis - System.currentTimeMillis();
        int remainingSeconds = (int) Math.max(0, (remainingMillis + 999) / 1000);
        session.getGameScreen().setTurnRemainingSeconds(remainingSeconds);
    }

    // Shows a prominent prompt when control enters this player's turn.
    private void notifyWhenMyTurn(boolean myTurn) {
        if (myTurn && !lastMyTurn) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Your Turn");
            alert.setHeaderText("It is your turn");
            alert.setContentText("You have 2 minutes to play. The turn will move to the next player when time runs out.");
            alert.initOwner(this);
            alert.show();
        }

        lastMyTurn = myTurn;
    }

    // Sends this operation.
    private void send(String type, String body) {
        if (out != null) {
            out.println(new NetworkMessage(type, body == null ? "" : body).encode());
        }
    }

    // Runs append log.
    private void appendLog(String text) {
        logLines.add(text);
        while (logLines.size() > 20) {
            logLines.removeFirst();
        }
    }

    // Runs shutdown.
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

    // Parses int.
    private static int parseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException | NullPointerException e) {
            return 0;
        }
    }

    // Sanitizes player name for display and the simple text network protocol.
    private static String sanitizePlayerName(String name) {
        String sanitized = name == null
                ? ""
                : name.trim()
                .replace("|", "")
                .replace(",", "")
                .replace(";", "")
                .replace("[", "")
                .replace("]", "");

        if (sanitized.isEmpty()) {
            return "Player";
        }

        return sanitized.length() > 16 ? sanitized.substring(0, 16) : sanitized;
    }
}
