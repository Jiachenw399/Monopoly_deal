package logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class GameFacadeTest {
    @Test
    public void testGameCanBeUsedThroughFacade() {
        GameFacade game = new Game();

        game.startGame();

        assertEquals(4, game.getPlayers().size());
        assertFalse(game.isWin());
    }
}
