package logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import model.DrawPileAndDiscardPile;
import model.Player;
import model.PropertiesCards;
import model.PropertiesCardsType;
import model.PropertyColor;
import org.junit.jupiter.api.Test;

public class RentCalculatorTest {
    @Test
    public void testDecoratorsAddBuildingRentAndDoubleFinalRent() {
        Player player = new Player(new DrawPileAndDiscardPile());
        PropertiesCards baltic = new PropertiesCards(
                PropertiesCardsType.BROWN,
                "Baltic Avenue",
                "baltic_avenue.png"
        );
        PropertiesCards mediterranean = new PropertiesCards(
                PropertiesCardsType.BROWN,
                "Mediterranean Avenue",
                "mediterranean_avenue.png"
        );
        baltic.setHasHouse(true);
        mediterranean.setHasHotel(true);
        player.getPropertyCards().add(baltic);
        player.getPropertyCards().add(mediterranean);

        RentCalculator rentCalculator = new RentCalculator();

        assertEquals(9, rentCalculator.calculateRent(player, PropertyColor.BROWN));
        assertEquals(18, rentCalculator.calculateRent(player, PropertyColor.BROWN, true));
    }
}
