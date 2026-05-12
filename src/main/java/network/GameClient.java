package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class GameClient {
    private static final String HOST = "localhost";
    private static final int PORT = 5555;

    public static void main(String[] args) {
        new GameClient().start();
    }

    public void start() {
        try (
                Socket socket = new Socket(HOST, PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connected to server.");
            System.out.println("Server says: " + in.readLine());
            System.out.println("Type HELLO, END_TURN, or QUIT.");

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if ("QUIT".equalsIgnoreCase(input)) {
                    break;
                }

                out.println(new NetworkMessage(input.toUpperCase(), "").encode());
                System.out.println("Server says: " + in.readLine());
            }
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}
