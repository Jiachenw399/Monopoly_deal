package network;

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
    private static final int MAX_PLAYERS = 4;

    private final AtomicInteger nextPlayerId = new AtomicInteger(1);
    private final List<ClientHandler> clients = new ArrayList<>();

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
        return clients.size() >= MAX_PLAYERS;
    }

    private void rejectClient(Socket clientSocket) {
        try (
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                Socket socketToClose = clientSocket
        ) {
            out.println(new NetworkMessage("FULL", "Server already has " + MAX_PLAYERS + " players").encode());
        } catch (IOException e) {
            System.out.println("Failed to reject client: " + e.getMessage());
        }
    }

    private synchronized void addClient(ClientHandler clientHandler) {
        clients.add(clientHandler);
        System.out.println("Player " + clientHandler.getPlayerId() + " connected. Players: " + clients.size());
    }

    private synchronized void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        System.out.println("Player " + clientHandler.getPlayerId() + " disconnected. Players: " + clients.size());
    }

    private synchronized void broadcast(NetworkMessage message) {
        String encodedMessage = message.encode();

        for (ClientHandler client : clients) {
            client.send(encodedMessage);
        }
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
            }
        }

        private void handleMessage(NetworkMessage message) {
            if ("HELLO".equals(message.getType())) {
                broadcast(new NetworkMessage("BROADCAST", "Player " + playerId + " says hello"));
            } else if ("END_TURN".equals(message.getType())) {
                broadcast(new NetworkMessage("BROADCAST", "Player " + playerId + " wants to end turn"));
            } else {
                send(new NetworkMessage("SERVER", "Unknown message: " + message.getType()).encode());
            }
        }

        private void send(String encodedMessage) {
            if (out != null) {
                out.println(encodedMessage);
            }
        }
    }
}
