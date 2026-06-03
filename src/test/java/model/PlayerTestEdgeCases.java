package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import logic.Game;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class PlayerTestEdgeCases {

    @Test
    public void testPlayerNameCanBeSetAndRetrieved() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile, "Alice");

        assertEquals("Alice", player.getName());
    }

    @Test
    public void testPlayerNameNullIsTreatedAsNoName() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile, (String) null);

        assertNull(player.getName());
    }

    @Test
    public void testPlayerNameWhitespaceIsTreatedAsNull() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile, "   ");

        assertNull(player.getName());
    }

    @Test
    public void testSetNameTrimsWhitespace() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);
        player.setName("  Bob  ");

        assertEquals("Bob", player.getName());
    }

    @Test
    public void testSetNameToBlankBecomesNull() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile, "Carol");
        player.setName("");

        assertNull(player.getName());
    }

    @Test
    public void testCheckIfWinReturnsFalseWhenNoProperties() {
        Player player = new Player(new DrawPileAndDiscardPile());
        player.getBankCards().add(new MoneyCards(10));

        assertFalse(player.checkIfWin());
    }

    @Test
    public void testCheckIfWinReturnsFalseWithLessThanThreeCompleteSets() {
        Player player = new Player(new DrawPileAndDiscardPile());
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.DARK_BLUE));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.DARK_BLUE));

        assertFalse(player.checkIfWin());
    }

    @Test
    public void testCheckIfWinReturnsFalseWithExactlyTwoCompleteSets() {
        Player player = new Player(new DrawPileAndDiscardPile());
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.DARK_BLUE));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.DARK_BLUE));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.LIGHT_GREEN));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.LIGHT_GREEN));

        assertFalse(player.checkIfWin());
    }

    @Test
    public void testCheckIfWinReturnsFalseForWildCardsWithoutColor() {
        Player player = new Player(new DrawPileAndDiscardPile());
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.WILD_ALL));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.WILD_ALL));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.WILD_ALL));

        assertFalse(player.checkIfWin());
    }

    @Test
    public void testTakeMoneyWithNullPayerDoesNothing() {
        Player receiver = new Player(new DrawPileAndDiscardPile());
        receiver.getBankCards().add(new MoneyCards(5));

        receiver.takeMoney(5, null);

        assertEquals(1, receiver.getBankCards().size());
    }

    @Test
    public void testTakeMoneyWithZeroAmountDoesNothing() {
        Player receiver = new Player(new DrawPileAndDiscardPile());
        Player payer = new Player(new DrawPileAndDiscardPile());
        payer.getBankCards().add(new MoneyCards(5));
        receiver.getBankCards().add(new MoneyCards(5));

        receiver.takeMoney(0, payer);

        assertEquals(1, receiver.getBankCards().size());
        assertEquals(1, payer.getBankCards().size());
    }

    @Test
    public void testTakeMoneyWithNegativeAmountDoesNothing() {
        Player receiver = new Player(new DrawPileAndDiscardPile());
        Player payer = new Player(new DrawPileAndDiscardPile());
        payer.getBankCards().add(new MoneyCards(5));
        receiver.getBankCards().add(new MoneyCards(5));

        receiver.takeMoney(-3, payer);

        assertEquals(1, receiver.getBankCards().size());
        assertEquals(1, payer.getBankCards().size());
    }

    @Test
    public void testTakeMoneyPayerWithInsufficientAssetsPaysEverything() {
        Player receiver = new Player(new DrawPileAndDiscardPile());
        Player payer = new Player(new DrawPileAndDiscardPile());
        payer.getBankCards().add(new MoneyCards(3));
        payer.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));

        receiver.takeMoney(10, payer);

        assertEquals(1, receiver.getBankCards().size());
        assertEquals(1, receiver.getPropertyCards().size());
        assertTrue(payer.getBankCards().isEmpty());
        assertTrue(payer.getPropertyCards().isEmpty());
    }

    @Test
    public void testTakeMoneyWithExactBankValuePaysFromBankOnly() {
        Player receiver = new Player(new DrawPileAndDiscardPile());
        Player payer = new Player(new DrawPileAndDiscardPile());
        payer.getBankCards().add(new MoneyCards(5));
        payer.getBankCards().add(new MoneyCards(5));

        receiver.takeMoney(5, payer);

        assertEquals(1, receiver.getBankCards().size());
        assertEquals(5, receiver.getBankCards().get(0).getValue());
        assertEquals(1, payer.getBankCards().size());
        assertEquals(5, payer.getBankCards().get(0).getValue());
    }

    @Test
    public void testTakeMoneyBankPlusPropertyWhenBankNotEnough() {
        Player receiver = new Player(new DrawPileAndDiscardPile());
        Player payer = new Player(new DrawPileAndDiscardPile());
        payer.getBankCards().add(new MoneyCards(3));
        payer.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));

        receiver.takeMoney(5, payer);

        assertEquals(1, receiver.getBankCards().size());
        assertEquals(3, receiver.getBankCards().get(0).getValue());
        assertEquals(1, receiver.getPropertyCards().size());
        assertEquals(0, payer.getBankCards().size());
        assertEquals(0, payer.getPropertyCards().size());
    }

    @Test
    public void testIncreaseUseCardTimes() {
        Player player = new Player(new DrawPileAndDiscardPile());
        assertEquals(0, player.getUseCardTimes());

        player.increaseUseCardTimes();
        assertEquals(1, player.getUseCardTimes());

        player.increaseUseCardTimes();
        player.increaseUseCardTimes();
        assertEquals(3, player.getUseCardTimes());
    }

    @Test
    public void testSetUseCardTimes() {
        Player player = new Player(new DrawPileAndDiscardPile());
        player.setUseCardTimes(3);

        assertEquals(3, player.getUseCardTimes());
    }

    @Test
    public void testMoveCardFromHandToDiscard() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);
        Card card = new MoneyCards(5);
        player.getHandCards().add(card);

        player.moveCardFromHandToDiscard(card);

        assertFalse(player.getHandCards().contains(card));
        assertTrue(pile.getDiscardPile().contains(card));
    }

    @Test
    public void testMoveCardFromHandToDiscardWithNullDoesNothing() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);
        int initialDiscardSize = pile.getDiscardPile().size();

        player.moveCardFromHandToDiscard(null);

        assertEquals(initialDiscardSize, pile.getDiscardPile().size());
    }

    @Test
    public void testMoveCardFromHandToDiscardWithCardNotInHandDoesNothing() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);
        Card card = new MoneyCards(5);
        int initialDiscardSize = pile.getDiscardPile().size();

        player.moveCardFromHandToDiscard(card);

        assertFalse(pile.getDiscardPile().contains(card));
        assertEquals(initialDiscardSize, pile.getDiscardPile().size());
    }

    @Test
    public void testDiscardCardFromHand() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);
        Card card = new ActionCards(ActionCardType.PASS_GO);
        player.getHandCards().add(card);

        assertTrue(player.discardCardFromHand(card));

        assertFalse(player.getHandCards().contains(card));
        assertTrue(pile.getDiscardPile().contains(card));
    }

    @Test
    public void testDiscardCardFromHandWithNullReturnsFalse() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);

        assertFalse(player.discardCardFromHand(null));
    }

    @Test
    public void testDiscardCardFromHandWithCardNotInHandReturnsFalse() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);
        Card card = new MoneyCards(5);

        assertFalse(player.discardCardFromHand(card));
        assertFalse(pile.getDiscardPile().contains(card));
    }

    @Test
    public void testReceiveBankCardAddsToBank() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);
        Card card = new MoneyCards(10);

        player.receiveBankCard(card);

        assertTrue(player.getBankCards().contains(card));
    }

    @Test
    public void testReceiveBankCardIgnoresNull() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);

        player.receiveBankCard(null);

        assertTrue(player.getBankCards().isEmpty());
    }

    @Test
    public void testReceivePropertyCardAddsToProperties() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);
        PropertiesCards prop = new PropertiesCards(PropertiesCardsType.BROWN);

        player.receivePropertyCard(prop);

        assertTrue(player.getPropertyCards().contains(prop));
    }

    @Test
    public void testReceivePropertyCardIgnoresNull() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);

        player.receivePropertyCard(null);

        assertTrue(player.getPropertyCards().isEmpty());
    }

    @Test
    public void testRemoveBankCard() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);
        Card card = new MoneyCards(5);
        player.getBankCards().add(card);

        player.removeBankCard(card);

        assertFalse(player.getBankCards().contains(card));
    }

    @Test
    public void testRemovePropertyCard() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);
        PropertiesCards prop = new PropertiesCards(PropertiesCardsType.BROWN);
        player.getPropertyCards().add(prop);

        player.removePropertyCard(prop);

        assertFalse(player.getPropertyCards().contains(prop));
    }

    @Test
    public void testGetPropertyCountByColor() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.DARK_BLUE));

        assertEquals(2, player.getPropertyCountByColor(PropertyColor.BROWN));
        assertEquals(1, player.getPropertyCountByColor(PropertyColor.DARK_BLUE));
        assertEquals(0, player.getPropertyCountByColor(PropertyColor.ORANGE));
    }

    @Test
    public void testGetPropertyCountByColorWithNullReturnsZero() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);

        assertEquals(0, player.getPropertyCountByColor(null));
    }

    @Test
    public void testHasPropertyColor() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));

        assertTrue(player.hasPropertyColor(PropertyColor.BROWN));
        assertFalse(player.hasPropertyColor(PropertyColor.DARK_BLUE));
        assertFalse(player.hasPropertyColor(null));
    }

    @Test
    public void testCanUseRentColorDelegatesToHasPropertyColor() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));

        assertTrue(player.canUseRentColor(PropertyColor.BROWN));
        assertFalse(player.canUseRentColor(PropertyColor.DARK_BLUE));
    }

    @Test
    public void testHasActionCard() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);
        ActionCards passGo = new ActionCards(ActionCardType.PASS_GO);
        player.getHandCards().add(passGo);

        assertTrue(player.hasActionCard(ActionCardType.PASS_GO));
        assertFalse(player.hasActionCard(ActionCardType.SLY_DEAL));
        assertFalse(player.hasActionCard(null));
    }

    @Test
    public void testFindActionCardReturnsNullForNonexistent() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player player = new Player(pile);

        assertNull(player.findActionCard(ActionCardType.PASS_GO));
        assertNull(player.findActionCard(null));
    }
}
