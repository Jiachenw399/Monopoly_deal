package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PlayerTest {
    @Test
    public void testTakeCardDrawsRequestedCards() {
        Player player = new Player(new DrawPileAndDiscardPile());

        player.takeCard(6);

        assertEquals(6, player.getHandCards().size());
    }

    @Test
    public void testPutMoneyCardMovesCardFromHandToBank() {
        Player player = new Player(new DrawPileAndDiscardPile());
        MoneyCards moneyCard = new MoneyCards(5);
        player.getHandCards().add(moneyCard);

        player.putMoneyCard(moneyCard);

        assertFalse(player.getHandCards().contains(moneyCard));
        assertTrue(player.getBankCards().contains(moneyCard));
    }

    @Test
    public void testPropertyCardCannotBeBankedAsMoney() {
        Player player = new Player(new DrawPileAndDiscardPile());
        PropertiesCards property = new PropertiesCards(PropertiesCardsType.BROWN);
        player.getHandCards().add(property);

        player.putMoneyCard(property);

        assertTrue(player.getHandCards().contains(property));
        assertFalse(player.getBankCards().contains(property));
    }

    @Test
    public void testPutPropertyCardAssignsDefaultWildColor() {
        Player player = new Player(new DrawPileAndDiscardPile());
        PropertiesCards wildCard = new PropertiesCards(PropertiesCardsType.WILD_RED_YELLOW);
        player.getHandCards().add(wildCard);

        player.putPropertyCard(wildCard);

        assertFalse(player.getHandCards().contains(wildCard));
        assertTrue(player.getPropertyCards().contains(wildCard));
        assertEquals(PropertyColor.RED, wildCard.getCurrentColor());
    }

    @Test
    public void testFindAndDiscardActionCardFromHand() {
        Player player = new Player(new DrawPileAndDiscardPile());
        ActionCards passGo = new ActionCards(ActionCardType.PASS_GO);
        player.getHandCards().add(passGo);

        assertSame(passGo, player.findActionCard(ActionCardType.PASS_GO));
        assertTrue(player.discardActionCardFromHand(ActionCardType.PASS_GO));
        assertFalse(player.getHandCards().contains(passGo));
        assertTrue(player.getDrawCardsAndDiscardPile().getDiscardPile().contains(passGo));
    }

    @Test
    public void testCompleteSetCannotLosePropertyToSlyDeal() {
        Player player = new Player(new DrawPileAndDiscardPile());
        PropertiesCards baltic = new PropertiesCards(PropertiesCardsType.BROWN);
        PropertiesCards mediterranean = new PropertiesCards(PropertiesCardsType.BROWN);
        player.getPropertyCards().add(baltic);
        player.getPropertyCards().add(mediterranean);

        assertTrue(player.isCompleteSet(PropertyColor.BROWN));
        assertFalse(player.canLosePropertyToSlyDeal(baltic));
    }

    @Test
    public void testCheckIfWinRequiresThreeCompletedSets() {
        Player player = new Player(new DrawPileAndDiscardPile());
        addProperties(player, PropertiesCardsType.BROWN, 2);
        addProperties(player, PropertiesCardsType.DARK_BLUE, 2);
        addProperties(player, PropertiesCardsType.LIGHT_GREEN, 2);

        assertTrue(player.checkIfWin());
    }

    private void addProperties(Player player, PropertiesCardsType type, int amount) {
        for (int i = 0; i < amount; i++) {
            player.getPropertyCards().add(new PropertiesCards(type));
        }
    }
}
