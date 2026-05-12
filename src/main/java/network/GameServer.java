package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class GameServer {
    private static final int PORT = 5555;
    private final AtomicInteger nextPlayerId = new AtomicInteger(1);

    public static void main(String[] args) {
        new GameServer().start();
    }

    public void start() {
        System.out.println("Server starting on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is ready. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                int playerId = nextPlayerId.getAndIncrement();
                Thread clientThread = new Thread(() -> handleClient(clientSocket, playerId));
                clientThread.start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket, int playerId) {
        System.out.println("Player " + playerId + " connected.");

        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            out.println(new NetworkMessage("WELCOME", "PLAYER " + playerId).encode());

            String line;

            while ((line = in.readLine()) != null) {
                NetworkMessage message = NetworkMessage.decode(line);
                System.out.println("From Player " + playerId + ": " + message.getType() + " " + message.getBody());

                if ("HELLO".equals(message.getType())) {
                    out.println(new NetworkMessage("SERVER", "Hello, Player " + playerId).encode());
                } else if ("END_TURN".equals(message.getType())) {
                    out.println(new NetworkMessage("SERVER", "Player " + playerId + " wants to end turn").encode());
                } else {
                    out.println(new NetworkMessage("SERVER", "Unknown message: " + message.getType()).encode());
                }
            }
        } catch (IOException e) {
            System.out.println("Player " + playerId + " disconnected.");
        }
    }
}
