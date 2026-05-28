package network;

import GUI.GameClickHandler;
import GUI.GameScreen;
import GUI.GameSession;
import GUI.GuiScale;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Online client shell that reuses {@link GameSession} and {@link GameClickHandler}.
 */
public class OnlinePlayWindow extends Stage {
    private static final int PORT = 5555;

    private final String host;
    private final GameSession session = new GameSession();
    private final OnlineGameClickActions clickActions;
    private final GameClickHandler clickHandler;
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

    private void startRenderLoop() {
        GameScreen gameScreen = session.getGameScreen();
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

        if (!session.getGameScreen().isShow()) {
            return;
        }

        clickHandler.handleMouseClick(x, y);
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
            case "WELCOME" -> {
                myPlayerId = parseInt(message.getBody().replace("PLAYER", "").trim());
                clickActions.setMyPlayerId(myPlayerId);
            }
            case "FULL" -> connectionText = "Rejected";
            case "GAME_STATE" -> {
                GameStateCodec.Snapshot snapshot = GameStateCodec.decode(message.getBody());
                myPlayerId = snapshot.you;
                clickActions.setMyPlayerId(myPlayerId);
                session.applyOnlineSnapshot(snapshot);
                started = true;
            }
            default -> {
            }
        }
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
}
