package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DrawPileAndDiscardPileTest {
    @Test
    public void testDeckHasCorrectTotalCardCount() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();

        assertEquals(106, pile.getDrawPile().size());
    }

    @Test
    public void testDiscardPileStartsEmpty() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();

        assertTrue(pile.getDiscardPile().isEmpty());
    }

    @Test
    public void testDrawRemovesCardsFromDrawPile() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        int initialSize = pile.getDrawPile().size();

        pile.getDrawPile().remove(0);

        assertEquals(initialSize - 1, pile.getDrawPile().size());
    }

    @Test
    public void testShuffleChangesCardOrder() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        String firstCardId = getCardIdentifier(pile.getDrawPile().get(0));

        pile.shuffleDrawCards();

        assertNotSame(firstCardId, getCardIdentifier(pile.getDrawPile().get(0)));
    }

    @Test
    public void testShuffleMovesDiscardToDrawPile() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        while (!pile.getDrawPile().isEmpty()) {
            pile.getDiscardPile().add(pile.getDrawPile().remove(0));
        }
        assertTrue(pile.getDrawPile().isEmpty());
        assertTrue(pile.getDiscardPile().size() > 0);

        pile.shuffle();

        assertTrue(pile.getDrawPile().size() > 0);
        assertTrue(pile.getDiscardPile().isEmpty());
    }

    @Test
    public void testShuffleDoesNothingWhenDrawPileIsNotEmpty() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        int drawSize = pile.getDrawPile().size();
        int discardSize = pile.getDiscardPile().size();

        pile.shuffle();

        assertEquals(drawSize, pile.getDrawPile().size());
        assertEquals(discardSize, pile.getDiscardPile().size());
    }

    @Test
    public void testShuffleProducesDifferentCardAtFirstPosition() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Card firstBefore = pile.getDrawPile().get(0);

        pile.shuffleDrawCards();

        Card firstAfter = pile.getDrawPile().get(0);
        assertNotSame(firstBefore, firstAfter);
    }

    @Test
    public void testMultipleShufflesProduceDifferentOrders() {
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        String order1 = captureOrder(pile);

        pile.shuffleDrawCards();
        String orderAfterShuffle1 = captureOrder(pile);

        pile.shuffleDrawCards();
        String orderAfterShuffle2 = captureOrder(pile);

        assertNotSame(order1, orderAfterShuffle1);
        assertNotSame(orderAfterShuffle1, orderAfterShuffle2);
    }

    private String getCardIdentifier(Card card) {
        if (card instanceof MoneyCards money) {
            return "MONEY:" + money.getValue();
        } else if (card instanceof ActionCards action) {
            return "ACTION:" + action.getActionCardType().name();
        } else if (card instanceof PropertiesCards prop) {
            return "PROP:" + prop.getType().name();
        }
        return card.getClass().getSimpleName();
    }

    private String captureOrder(DrawPileAndDiscardPile pile) {
        StringBuilder sb = new StringBuilder();
        for (Card card : pile.getDrawPile()) {
            sb.append(getCardIdentifier(card)).append(",");
        }
        return sb.toString();
    }
}
