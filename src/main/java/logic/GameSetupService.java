package logic;

import model.DrawPileAndDiscardPile;
import model.Player;

import java.util.ArrayList;

public class GameSetupService {
    private static final int INITIAL_CARD_COUNT = 5;

    public void setupPlayers(ArrayList<Player> players, DrawPileAndDiscardPile drawCards, int playerCount) {
        players.clear();
        createPlayers(players, drawCards, playerCount);
        dealInitialCards(players);
    }

    private void createPlayers(ArrayList<Player> players, DrawPileAndDiscardPile drawCards, int playerCount) {
        for (int i = 0; i < playerCount; i++) {
            players.add(new Player(drawCards));
        }
    }

    private void dealInitialCards(ArrayList<Player> players) {
        for (Player player : players) {
            player.takeCard(INITIAL_CARD_COUNT);
        }
    }
}