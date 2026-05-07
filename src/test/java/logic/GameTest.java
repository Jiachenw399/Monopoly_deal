package logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import model.Player;

public class GameTest {
    @Test
    public void testGameInitialization() {
        Game game = new Game();

        // 初始4个玩家
        assertEquals(4, game.getPlayers().size());

        // 每个玩家一开始5张手牌
        for (Player p : game.getPlayers()) {
            assertEquals(5, p.getHandCards().size());
        }
        assertFalse(game.isWin());
    }

    @Test
    public void testStartgame(){
        Game game = new Game();
        game.startGame();
        assertEquals(0, game.getCurrentPlayerIndex());
        assertTrue(game.getCurrentPlayer().isOnTurn());
    }

    @Test
    public void testEndTurnSwitchPlayer() {
        Game game = new Game();
        game.startGame();
        Player firstPlayer = game.getCurrentPlayer();
        Player secondPlayer = game.getCurrentPlayer();

        assertNotSame(firstPlayer, secondPlayer);
        assertEquals(1, game.getCurrentPlayerIndex());
        assertTrue(secondPlayer.isOnTurn());
        assertFalse(firstPlayer.isOnTurn());
    }

    @Test//测试抓牌
    public void testStartTurnDrawTwoCards() {
        Game game = new Game();
        game.startGame();
        Player currentPlayer = game.getCurrentPlayer();
        int before = currentPlayer.getHandCards().size();
        game.startTurn(currentPlayer);
        // 手牌非空时抓2张
        assertEquals(before + 2, currentPlayer.getHandCards().size());
    }

    @Test//测试没牌抓五张
    public void testDrawFiveCardsWhenEmpty() {
        Game game = new Game();
        game.startGame();
        Player currentPlayer = game.getCurrentPlayer();
        //清空当前玩家手牌
        currentPlayer.getHandCards().clear();
        assertTrue(currentPlayer.getHandCards().isEmpty());
        game.startTurn(currentPlayer);
        //没手牌抓五张
        assertEquals(5, currentPlayer.getHandCards().size());
    }
}
