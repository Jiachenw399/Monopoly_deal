package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class GameClient {
    private static final int PORT = 5555;
    private final String host;

    // Creates a GameClient instance.
    public GameClient(String host) {
        this.host = host;
    }

    // Starts the application entry point.
    public static void main(String[] args) {
        String host = "localhost";

        if (args.length > 0) {
            host = args[0];
        }

        new GameClient(host).start();
    }

    // Starts this operation.
    public void start() {
        try (
                Socket socket = new Socket(host, PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connected to server: " + host + ":" + PORT);
            startServerListener(in);
            System.out.println("Type HELP for commands, or QUIT.");

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if ("QUIT".equalsIgnoreCase(input)) {
                    break;
                }

                if ("HELP".equalsIgnoreCase(input)) {
                    System.out.println(NetworkCommandHelp.TEXT);
                    continue;
                }

                out.println(parseInput(input).encode());
            }
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }

    // Parses input.
    private NetworkMessage parseInput(String input) {
        String[] parts = input.trim().split("\\s+", 2);
        String type = parts[0].toUpperCase();
        String body = parts.length > 1 ? parts[1].trim() : "";

        return new NetworkMessage(type, body);
    }

    // Starts server listener.
    private void startServerListener(BufferedReader in) {
        Thread listenerThread = new Thread(() -> {
            try {
                String line;

                while ((line = in.readLine()) != null) {
                    NetworkMessage message = NetworkMessage.decode(line);
                    System.out.println();
                    System.out.println("Server says: " + message.getType() + " " + message.getBody());
                    System.out.print("> ");
                }
            } catch (IOException e) {
                System.out.println();
                System.out.println("Disconnected from server.");
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }
}
