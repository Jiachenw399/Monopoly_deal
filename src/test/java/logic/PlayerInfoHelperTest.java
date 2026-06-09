package logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import model.DrawPileAndDiscardPile;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;
import model.PropertiesCardsType;
import model.PropertyColor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class PlayerInfoHelperTest {
    @Test
    public void testBankAndPropertyCounts() {
        Player player = new Player(new DrawPileAndDiscardPile());
        player.getBankCards().add(new MoneyCards(5));
        player.getBankCards().add(new MoneyCards(2));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));

        assertEquals(7, PlayerInfoHelper.getBankTotal(player));
        assertEquals(1, PlayerInfoHelper.getPropertyCountByCurrentColor(player, PropertyColor.BROWN));
        assertTrue(PlayerInfoHelper.hasPropertyColor(player, PropertyColor.BROWN));
    }

    @Test
    public void testCompletedSetCountAndCompleteSetLookup() {
        Player player = new Player(new DrawPileAndDiscardPile());
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));

        ArrayList<PropertiesCards> completeSet =
                PlayerInfoHelper.getCompleteSetByColor(player, PropertyColor.BROWN);

        assertTrue(PlayerInfoHelper.isCompleteSet(player, PropertyColor.BROWN));
        assertFalse(PlayerInfoHelper.isCompleteSet(player, PropertyColor.DARK_BLUE));
        assertEquals(1, PlayerInfoHelper.getCompletedSetCount(player));
        assertEquals(2, completeSet.size());
    }

    @Test
    public void testBuildingsAndSlyDealProtection() {
        Player player = new Player(new DrawPileAndDiscardPile());
        PropertiesCards first = new PropertiesCards(PropertiesCardsType.BROWN);
        PropertiesCards second = new PropertiesCards(PropertiesCardsType.BROWN);
        first.setHasHouse(true);
        second.setHasHotel(true);
        player.getPropertyCards().add(first);
        player.getPropertyCards().add(second);

        assertTrue(PlayerInfoHelper.hasHouse(player, PropertyColor.BROWN));
        assertTrue(PlayerInfoHelper.hasHotel(player, PropertyColor.BROWN));
        assertFalse(PlayerInfoHelper.canBeStolenBySlyDeal(player, first));
    }
}
