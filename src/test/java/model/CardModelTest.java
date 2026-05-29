package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CardModelTest {
    @Test
    public void testMoneyAndActionCardsExposeValues() {
        assertEquals(10, new MoneyCards(10).getValue());
        assertEquals(5, new ActionCards(ActionCardType.DEAL_BREAKER).getValue());
    }

    @Test
    public void testNormalPropertyHasDefaultColorAndValue() {
        PropertiesCards property = new PropertiesCards(PropertiesCardsType.DARK_BLUE);

        assertEquals(PropertyColor.DARK_BLUE, property.getCurrentColor());
        assertEquals(4, property.getValue());
        assertFalse(property.isWildCard());
    }

    @Test
    public void testWildPropertyStartsWithoutCurrentColor() {
        PropertiesCards property = new PropertiesCards(PropertiesCardsType.WILD_RED_YELLOW);

        assertNull(property.getCurrentColor());
        assertTrue(property.isWildCard());
        assertTrue(property.getType().getColors().contains(PropertyColor.RED));
        assertTrue(property.getType().getColors().contains(PropertyColor.YELLOW));
    }

    @Test
    public void testShortColorName() {
        assertEquals("D.BLUE", PropertiesCards.getShortColorName(PropertyColor.DARK_BLUE));
        assertEquals("L.GREEN", PropertiesCards.getShortColorName(PropertyColor.LIGHT_GREEN));
    }
}
