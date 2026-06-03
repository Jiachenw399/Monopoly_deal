package logic;

import model.DrawPileAndDiscardPile;
import model.Player;

import java.util.ArrayList;
import java.util.List;

public class GameSetupService {
    private static final int INITIAL_CARD_COUNT = 5;

    // Runs setup players.
    public void setupPlayers(ArrayList<Player> players, DrawPileAndDiscardPile drawCards, int playerCount) {
        players.clear();
        createPlayers(players, drawCards, playerCount);
        dealInitialCards(players);
    }

    // Runs setup players with names.
    public void setupPlayers(ArrayList<Player> players, DrawPileAndDiscardPile drawCards, int playerCount, List<String> playerNames) {
        players.clear();
        for (int i = 0; i < playerCount; i++) {
            String name = (playerNames != null && i < playerNames.size()) ? playerNames.get(i) : null;
            players.add(new Player(drawCards, name));
        }
        dealInitialCards(players);
    }

    // Creates players.
    private void createPlayers(ArrayList<Player> players, DrawPileAndDiscardPile drawCards, int playerCount) {
        for (int i = 0; i < playerCount; i++) {
            players.add(new Player(drawCards));
        }
    }

    // Runs deal initial cards.
    private void dealInitialCards(ArrayList<Player> players) {
        for (Player player : players) {
            player.takeCard(INITIAL_CARD_COUNT);
        }
    }
}
