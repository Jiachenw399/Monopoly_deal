package logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import model.Card;
import model.DrawPileAndDiscardPile;
import model.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class TurnManagerTest {
    @Test
    public void testStartTurnDrawsTwoCardsWhenHandIsNotEmpty() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());
        Player player = players.get(0);
        player.getHandCards().add(new model.MoneyCards(1));
        int before = player.getHandCards().size();

        turnManager.startTurn(player);

        assertEquals(before + 2, player.getHandCards().size());
    }

    @Test
    public void testStartTurnDrawsFiveCardsWhenHandIsEmpty() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());
        Player player = players.get(0);
        player.getHandCards().clear();

        turnManager.startTurn(player);

        assertEquals(5, player.getHandCards().size());
    }

    @Test
    public void testStartTurnResetsUseCardTimes() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());
        Player player = players.get(0);
        player.getHandCards().clear();
        player.setUseCardTimes(3);

        turnManager.startTurn(player);

        assertEquals(0, player.getUseCardTimes());
    }

    @Test
    public void testStartFirstTurnDrawsFiveCards() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());
        Player player = players.get(0);
        player.getHandCards().clear();

        turnManager.startFirstTurn();

        assertEquals(5, player.getHandCards().size());
        assertEquals(0, player.getUseCardTimes());
    }

    @Test
    public void testEndTurnDoesNotEnterDiscardPhaseWhenHandIsSevenOrLess() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());
        Player player = players.get(0);
        player.getHandCards().clear();
        for (int i = 0; i < 5; i++) {
            player.getHandCards().add(new model.MoneyCards(1));
        }

        turnManager.endTurn();

        assertFalse(turnManager.isDiscard());
        assertEquals(1, turnManager.getCurrentPlayerIndex());
    }

    @Test
    public void testEndTurnEntersDiscardPhaseWhenHandExceedsSeven() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());
        Player player = players.get(0);
        for (int i = 0; i < 10; i++) {
            player.getHandCards().add(new model.MoneyCards(1));
        }

        turnManager.endTurn();

        assertTrue(turnManager.isDiscard());
        assertEquals(0, turnManager.getCurrentPlayerIndex());
    }

    @Test
    public void testDiscardRemovesCardAndExitsDiscardPhaseWhenHandDropsToSeven() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());
        Player player = players.get(0);
        player.getHandCards().clear();
        for (int i = 0; i < 8; i++) {
            player.getHandCards().add(new model.MoneyCards(1));
        }
        turnManager.applyOnlineState(0, true);
        assertTrue(turnManager.isDiscard());
        assertEquals(8, player.getHandCards().size());
        Card toDiscard = player.getHandCards().get(0);

        assertTrue(turnManager.discard(toDiscard));

        assertEquals(7, player.getHandCards().size());
        assertFalse(turnManager.isDiscard());
        assertEquals(1, turnManager.getCurrentPlayerIndex());
    }

    @Test
    public void testDiscardFailsWhenNotInDiscardPhase() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());
        Player player = players.get(0);
        for (int i = 0; i < 5; i++) {
            player.getHandCards().add(new model.MoneyCards(1));
        }
        Card toDiscard = player.getHandCards().get(0);

        assertFalse(turnManager.discard(toDiscard));
        assertTrue(player.getHandCards().contains(toDiscard));
    }

    @Test
    public void testDiscardFailsForCardNotInHand() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());
        Player player = players.get(0);
        for (int i = 0; i < 10; i++) {
            player.getHandCards().add(new model.MoneyCards(1));
        }
        turnManager.endTurn();
        Card notInHand = new model.MoneyCards(99);

        assertFalse(turnManager.discard(notInHand));
    }

    @Test
    public void testDiscardFailsForNullCard() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());
        Player player = players.get(0);
        for (int i = 0; i < 10; i++) {
            player.getHandCards().add(new model.MoneyCards(1));
        }
        turnManager.endTurn();

        assertFalse(turnManager.discard(null));
    }

    @Test
    public void testForceAdvanceTurnForAbsentPlayerDiscardsDownToSeven() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());
        Player player = players.get(0);
        for (int i = 0; i < 12; i++) {
            player.getHandCards().add(new model.MoneyCards(1));
        }
        int originalIndex = turnManager.getCurrentPlayerIndex();

        turnManager.forceAdvanceTurnForAbsentPlayer();

        assertEquals(7, player.getHandCards().size());
        assertFalse(turnManager.isDiscard());
        assertEquals((originalIndex + 1) % 2, turnManager.getCurrentPlayerIndex());
    }

    @Test
    public void testForceAdvanceTurnDoesNothingWhenHandIsSevenOrLess() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());
        Player player = players.get(0);
        for (int i = 0; i < 5; i++) {
            player.getHandCards().add(new model.MoneyCards(1));
        }
        int originalIndex = turnManager.getCurrentPlayerIndex();

        turnManager.forceAdvanceTurnForAbsentPlayer();

        assertEquals(5, player.getHandCards().size());
        assertEquals((originalIndex + 1) % 2, turnManager.getCurrentPlayerIndex());
    }

    @Test
    public void testForceAdvanceTurnExitsDiscardPhase() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());
        Player player = players.get(0);
        for (int i = 0; i < 10; i++) {
            player.getHandCards().add(new model.MoneyCards(1));
        }
        turnManager.endTurn();
        assertTrue(turnManager.isDiscard());

        turnManager.forceAdvanceTurnForAbsentPlayer();

        assertFalse(turnManager.isDiscard());
    }

    @Test
    public void testGetCurrentPlayer() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());

        assertSame(players.get(0), turnManager.getCurrentPlayer());
    }

    @Test
    public void testGetCurrentPlayerIndex() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());

        assertEquals(0, turnManager.getCurrentPlayerIndex());
    }

    @Test
    public void testApplyOnlineStateClampsNegativeIndexToZero() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());

        turnManager.applyOnlineState(-5, false);

        assertEquals(0, turnManager.getCurrentPlayerIndex());
    }

    @Test
    public void testApplyOnlineStateSetsDiscardFlag() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());

        turnManager.applyOnlineState(0, true);

        assertTrue(turnManager.isDiscard());
    }

    private ArrayList<Player> createPlayers(int count) {
        DrawPileAndDiscardPile drawPile = new DrawPileAndDiscardPile();
        ArrayList<Player> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            players.add(new Player(drawPile));
        }
        return players;
    }
}
