package network;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Minimal JavaFX client for the existing text protocol (same as {@link GameClient}).
 */
public class OnlinePlayWindow extends Stage {

    private static final int PORT = 5555;

    private final String host;
    private final TextArea log = new TextArea();
    private final TextField commandField = new TextField();
    private final Button sendButton = new Button("Send");

    private volatile boolean closed;
    private Socket socket;
    private PrintWriter out;

    public OnlinePlayWindow(Stage owner, String host) {
        this.host = host;

        initOwner(owner);
        setTitle("Online — " + host + ":" + PORT);

        log.setEditable(false);
        log.setWrapText(true);
        commandField.setPromptText("END_TURN, PLAY_CARD 1, DISCARD 1, STATE, START_GAME, HELP, QUIT");
        sendButton.setDefaultButton(true);
        sendButton.setDisable(true);

        VBox root = new VBox(8, log, commandField, sendButton);
        VBox.setVgrow(log, Priority.ALWAYS);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 560, 420);
        setScene(scene);

        Runnable sendAction = this::sendCurrentLine;
        sendButton.setOnAction(e -> sendAction.run());
        commandField.setOnAction(e -> sendAction.run());

        setOnCloseRequest(e -> shutdown());

        appendLog("Connecting to " + host + " …\n");
        new Thread(this::runConnection, "online-socket").start();
    }

    private void appendLog(String text) {
        log.appendText(text);
    }

    private void runConnection() {
        try {
            Socket s = new Socket(host, PORT);
            synchronized (this) {
                if (closed) {
                    s.close();
                    return;
                }
                this.socket = s;
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream(), true);

            Platform.runLater(() -> {
                appendLog("Connected. Type commands in the field below.\n");
                sendButton.setDisable(false);
            });

            String line;
            while (!closed && (line = in.readLine()) != null) {
                NetworkMessage message = NetworkMessage.decode(line);
                String text = message.getType() + " | " + message.getBody() + "\n";
                Platform.runLater(() -> appendLog(text));
            }
        } catch (IOException e) {
            if (!closed) {
                Platform.runLater(() -> appendLog("Disconnected or error: " + e.getMessage() + "\n"));
            }
        } finally {
            Platform.runLater(() -> sendButton.setDisable(true));
        }
    }

    private void sendCurrentLine() {
        if (out == null) {
            return;
        }
        String raw = commandField.getText();
        if (raw == null) {
            return;
        }
        String input = raw.trim();
        if (input.isEmpty()) {
            return;
        }
        if ("QUIT".equalsIgnoreCase(input)) {
            shutdown();
            close();
            return;
        }
        if ("HELP".equalsIgnoreCase(input)) {
            appendLog(NetworkCommandHelp.TEXT);
            commandField.clear();
            return;
        }
        NetworkMessage message = parseInput(input);
        out.println(message.encode());
        commandField.clear();
    }

    private static NetworkMessage parseInput(String input) {
        String[] parts = input.trim().split("\\s+", 2);
        String type = parts[0].toUpperCase();
        String body = parts.length > 1 ? parts[1].trim() : "";
        return new NetworkMessage(type, body);
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
}
