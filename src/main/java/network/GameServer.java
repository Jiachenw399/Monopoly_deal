package network;

import logic.Game;
import model.Player;

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
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 4;

    private final AtomicInteger nextPlayerId = new AtomicInteger(1);
    private final List<ClientHandler> clients = new ArrayList<>();
    private boolean gameStarted = false;
    private Game game;

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

    private synchronized int getPlayerCount() {
        return clients.size();
    }

    private synchronized String getPlayerListText() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < clients.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }

            builder.append("PLAYER ").append(clients.get(i).getPlayerId());
        }

        return builder.toString();
    }

    private synchronized void broadcast(NetworkMessage message) {
        String encodedMessage = message.encode();

        for (ClientHandler client : clients) {
            client.send(encodedMessage);
        }
    }

    private synchronized boolean tryStartGame(ClientHandler starter) {
        if (gameStarted) {
            starter.send(new NetworkMessage("SERVER", "Game has already started").encode());
            return false;
        }

        if (clients.size() < MIN_PLAYERS) {
            starter.send(new NetworkMessage("SERVER", "Need at least " + MIN_PLAYERS + " players to start").encode());
            return false;
        }

        gameStarted = true;
        game = new Game();
        game.startGame();
        return true;
    }

    private synchronized boolean isGameStarted() {
        return gameStarted && game != null;
    }

    private synchronized void endTurnIfGameStarted(ClientHandler requester) {
        if (!isGameStarted()) {
            requester.send(new NetworkMessage("SERVER", "Game has not started yet").encode());
            return;
        }

        int expectedPlayerId = game.getCurrentPlayerIndex() + 1;

        if (requester.getPlayerId() != expectedPlayerId) {
            requester.send(new NetworkMessage("SERVER", "It is Player " + expectedPlayerId + "'s turn").encode());
            return;
        }

        game.guiEndTurn();
        broadcast(new NetworkMessage("GAME_STATE", buildGameStateText()));
    }

    private synchronized String buildGameStateText() {
        if (game == null) {
            return "NO_GAME";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("currentPlayer=").append(game.getCurrentPlayerIndex() + 1);
        builder.append(";discardPhase=").append(game.isDiscard());
        builder.append(";players=");

        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player player = game.getPlayers().get(i);

            if (i > 0) {
                builder.append(",");
            }

            builder.append("P").append(i + 1);
            builder.append("(hand=").append(player.getHandCards().size());
            builder.append(",bank=").append(player.getBankCards().size());
            builder.append(",properties=").append(player.getPropertyCards().size());
            builder.append(")");
        }

        return builder.toString();
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
                broadcast(new NetworkMessage("PLAYER_LIST", getPlayerListText()));

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
                broadcast(new NetworkMessage("PLAYER_LIST", getPlayerListText()));
            }
        }

        private void handleMessage(NetworkMessage message) {
            if ("HELLO".equals(message.getType())) {
                broadcast(new NetworkMessage("BROADCAST", "Player " + playerId + " says hello"));
            } else if ("PLAYERS".equals(message.getType())) {
                send(new NetworkMessage("PLAYER_LIST", getPlayerListText()).encode());
            } else if ("START_GAME".equals(message.getType())) {
                if (tryStartGame(this)) {
                    broadcast(new NetworkMessage(
                            "GAME_STARTED",
                            "Started with " + getPlayerCount() + " players: " + getPlayerListText()
                    ));
                    broadcast(new NetworkMessage("GAME_STATE", buildGameStateText()));
                }
            } else if ("END_TURN".equals(message.getType())) {
                endTurnIfGameStarted(this);
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
