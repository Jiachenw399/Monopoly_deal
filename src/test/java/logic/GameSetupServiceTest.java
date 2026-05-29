package logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import model.DrawPileAndDiscardPile;
import model.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class GameSetupServiceTest {
    @Test
    public void testSetupPlayersClearsExistingPlayersAndDealsInitialCards() {
        GameSetupService service = new GameSetupService();
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player(new DrawPileAndDiscardPile()));

        service.setupPlayers(players, new DrawPileAndDiscardPile(), 3);

        assertEquals(3, players.size());
        for (Player player : players) {
            assertEquals(5, player.getHandCards().size());
        }
    }
}
