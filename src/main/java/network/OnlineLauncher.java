package network;

import GUI.MusicPlayer;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Entry from the main menu: host the embedded {@link GameServer} or open {@link OnlinePlayWindow}.
 */
public final class OnlineLauncher {

    private static final Object SERVER_LOCK = new Object();
    private static Thread gameServerThread;
    private static OnlinePlayWindow activeWindow;

    // Creates a OnlineLauncher instance.
    private OnlineLauncher() {
    }

    // Opens lan menu.
    public static void openLanMenu(Stage owner, MusicPlayer musicPlayer) {
        if (showActiveWindowIfPresent()) {
            return;
        }

        ChoiceDialog<String> choice = new ChoiceDialog<>("Join", "Join", "Host");
        choice.setTitle("LAN / OnlinePlayWindow");
        choice.setHeaderText("Host starts a server on this PC (port 5555). Join connects to the host's IP.");
        choice.setContentText("Choose role:");

        Optional<String> result = choice.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        if ("Host".equals(result.get())) {
            startHostServerIfNeeded();
            showHostInfo(owner);
            promptPlayerName(owner).ifPresent(playerName -> openClient(owner, "127.0.0.1", playerName, musicPlayer));
        } else {
            promptJoin(owner, musicPlayer);
        }
    }

    // Shows active window if present.
    private static boolean showActiveWindowIfPresent() {
        if (activeWindow == null || !activeWindow.isShowing()) {
            return false;
        }

        activeWindow.toFront();
        activeWindow.requestFocus();
        return true;
    }

    // Starts host server if needed.
    private static void startHostServerIfNeeded() {
        synchronized (SERVER_LOCK) {
            if (gameServerThread != null && gameServerThread.isAlive()) {
                return;
            }

            gameServerThread = new Thread(() -> new GameServer().start(), "GameServer");
            gameServerThread.setDaemon(true);
            gameServerThread.start();
        }
    }

    // Shows host info.
    private static void showHostInfo(Stage owner) {
        String ips = formatLanAddresses();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setTitle("LAN host");
        alert.setHeaderText("Server is starting (or already running) on port 5555.");
        alert.setContentText("This computer will automatically join as Player 1 after you close this dialog.\n\n"
                + "Other computers: main menu -> L -> Join -> enter this computer's IPv4 address.\n\n"
                + "Detected IPv4 addresses:\n"
                + ips
                + "\n\n"
                + "Click Start Game in the LAN window when you are ready (solo or with others).");
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.showAndWait();
    }

    // Opens client.
    private static void openClient(Stage owner, String host, String playerName, MusicPlayer musicPlayer) {
        if (showActiveWindowIfPresent()) {
            return;
        }

        OnlinePlayWindow window = new OnlinePlayWindow(owner, host, playerName, musicPlayer);
        activeWindow = window;
        window.setOnHidden(event -> {
            if (activeWindow == window) {
                activeWindow = null;
            }
        });
        window.show();
    }

    // Runs prompt join.
    private static void promptJoin(Stage owner, MusicPlayer musicPlayer) {
        TextInputDialog dialog = new TextInputDialog("127.0.0.1");
        dialog.initOwner(owner);
        dialog.setTitle("Join LAN game");
        dialog.setHeaderText("Enter the host computer's IP address (or 127.0.0.1 on the same PC).");
        dialog.setContentText("Host IP:");

        Optional<String> ip = dialog.showAndWait();
        if (ip.isEmpty()) {
            return;
        }

        String host = ip.get().trim();
        if (host.isEmpty()) {
            return;
        }

        promptPlayerName(owner).ifPresent(playerName -> openClient(owner, host, playerName, musicPlayer));
    }

    // Prompts player name before joining the online lobby.
    private static Optional<String> promptPlayerName(Stage owner) {
        TextInputDialog dialog = new TextInputDialog("Player");
        dialog.initOwner(owner);
        dialog.setTitle("Player name");
        dialog.setHeaderText("Enter the name shown in the lobby before the game starts.");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return Optional.empty();
        }

        String name = sanitizePlayerName(result.get());
        if (name.isEmpty()) {
            name = "Player";
        }

        return Optional.of(name);
    }

    // Sanitizes player name for the simple text network protocol.
    private static String sanitizePlayerName(String name) {
        return name == null
                ? ""
                : name.trim()
                .replace("|", "")
                .replace(",", "")
                .replace(";", "")
                .replace("[", "")
                .replace("]", "");
    }

    // Runs format lan addresses.
    private static String formatLanAddresses() {
        List<String> lines = new ArrayList<>();
        try {
            for (NetworkInterface ni : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback()) {
                    continue;
                }

                for (InterfaceAddress ifAddr : ni.getInterfaceAddresses()) {
                    if (ifAddr.getAddress() instanceof Inet4Address addr && !addr.isLoopbackAddress()) {
                        lines.add("- " + addr.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            lines.add("(Could not list interfaces: " + e.getMessage() + ")");
        }

        if (lines.isEmpty()) {
            lines.add("- (none found; try ipconfig / ifconfig on the host)");
        }

        return String.join("\n", lines);
    }
}
