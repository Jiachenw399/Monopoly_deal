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

    public GameClient(String host) {
        this.host = host;
    }

    public static void main(String[] args) {
        String host = "localhost";

        if (args.length > 0) {
            host = args[0];
        }

        new GameClient(host).start();
    }

    public void start() {
        try (
                Socket socket = new Socket(host, PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connected to server: " + host + ":" + PORT);
            startServerListener(in);
            System.out.println("Type HELLO, END_TURN, or QUIT.");

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if ("QUIT".equalsIgnoreCase(input)) {
                    break;
                }

                out.println(new NetworkMessage(input.toUpperCase(), "").encode());
            }
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }

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
