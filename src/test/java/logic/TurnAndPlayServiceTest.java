package logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import model.ActionCardType;
import model.ActionCards;
import model.Card;
import model.DrawPileAndDiscardPile;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;
import model.PropertiesCardsType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class TurnAndPlayServiceTest {
    @Test
    public void testMoneyAndPropertyPlayServiceMovesCardsToCorrectAreas() {
        MoneyCardAndPropertyCardPlayService service = new MoneyCardAndPropertyCardPlayService();
        Player player = new Player(new DrawPileAndDiscardPile());
        MoneyCards money = new MoneyCards(2);
        PropertiesCards property = new PropertiesCards(PropertiesCardsType.BROWN);
        player.getHandCards().add(money);
        player.getHandCards().add(property);

        assertTrue(service.playCard(player, money));
        assertTrue(service.playCard(player, property));

        assertTrue(player.getBankCards().contains(money));
        assertTrue(player.getPropertyCards().contains(property));
        assertEquals(2, player.getUseCardTimes());
    }

    @Test
    public void testActionCardCanBePlayedAsMoney() {
        MoneyCardAndPropertyCardPlayService service = new MoneyCardAndPropertyCardPlayService();
        Player player = new Player(new DrawPileAndDiscardPile());
        ActionCards actionCard = new ActionCards(ActionCardType.DEAL_BREAKER);
        player.getHandCards().add(actionCard);

        assertTrue(service.playActionCardAsMoney(player, actionCard));

        assertTrue(player.getBankCards().contains(actionCard));
        assertEquals(1, player.getUseCardTimes());
    }

    @Test
    public void testPlayServiceRejectsCardAfterThreeUses() {
        MoneyCardAndPropertyCardPlayService service = new MoneyCardAndPropertyCardPlayService();
        Player player = new Player(new DrawPileAndDiscardPile());
        MoneyCards money = new MoneyCards(1);
        player.getHandCards().add(money);
        player.setUseCardTimes(3);

        assertFalse(service.playCard(player, money));
        assertTrue(player.getHandCards().contains(money));
    }

    @Test
    public void testTurnManagerMovesToNextPlayerWhenNoDiscardNeeded() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());
        players.get(0).takeCard(5);

        turnManager.endTurn();

        assertEquals(1, turnManager.getCurrentPlayerIndex());
        assertFalse(turnManager.isDiscard());
    }

    @Test
    public void testTurnManagerEntersDiscardPhaseAndLeavesAfterDiscardingToSeven() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());
        Player currentPlayer = players.get(0);
        currentPlayer.takeCard(8);
        Card discarded = currentPlayer.getHandCards().get(0);

        turnManager.endTurn();
        assertTrue(turnManager.isDiscard());

        assertTrue(turnManager.discard(discarded));
        assertEquals(1, turnManager.getCurrentPlayerIndex());
        assertFalse(turnManager.isDiscard());
    }

    @Test
    public void testApplyOnlineStateClampsPlayerIndex() {
        ArrayList<Player> players = createPlayers(2);
        TurnManager turnManager = new TurnManager(players, new DrawPileAndDiscardPile());

        turnManager.applyOnlineState(9, true);

        assertEquals(1, turnManager.getCurrentPlayerIndex());
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
