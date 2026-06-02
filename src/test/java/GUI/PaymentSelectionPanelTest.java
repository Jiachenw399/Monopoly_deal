package GUI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import logic.Game;
import model.DrawPileAndDiscardPile;
import model.Player;
import model.PropertiesCards;
import model.PropertiesCardsType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class PaymentSelectionPanelTest {
    @Test
    public void testBuiltPropertyRequiresBuildingSelectionFirst() {
        Game game = new Game(2);
        DrawPileAndDiscardPile drawPile = new DrawPileAndDiscardPile();
        Player receiver = new Player(drawPile);
        Player payer = new Player(drawPile);
        PropertiesCards property = new PropertiesCards(PropertiesCardsType.BROWN);
        property.setHasHouse(true);
        property.setHasHotel(true);
        payer.getPropertyCards().add(property);

        ArrayList<Player> players = new ArrayList<>();
        players.add(receiver);
        players.add(payer);
        game.applyOnlineState(players, 0, false, new Game.PaymentRequest(receiver, payer, 8), false);

        PaymentSelectionPanel panel = new PaymentSelectionPanel(game);

        assertFalse(panel.handleCardClick(151, 181));
        assertEquals(0, panel.getSelectedCards().size());

        assertFalse(panel.handleCardClick(61, 366));
        assertEquals(0, panel.getSelectedCards().size());

        assertTrue(panel.handleCardClick(61, 181));
        assertEquals(1, panel.getSelectedCards().size());
        assertTrue(panel.handleCardClick(151, 181));
        assertEquals(2, panel.getSelectedCards().size());
        assertTrue(panel.handleCardClick(61, 366));
        assertEquals(3, panel.getSelectedCards().size());
        assertTrue(panel.canConfirm());
    }

    @Test
    public void testBuildingSelectionCanCoverPaymentWithoutProperty() {
        Game game = new Game(2);
        DrawPileAndDiscardPile drawPile = new DrawPileAndDiscardPile();
        Player receiver = new Player(drawPile);
        Player payer = new Player(drawPile);
        PropertiesCards property = new PropertiesCards(PropertiesCardsType.BROWN);
        property.setHasHouse(true);
        property.setHasHotel(true);
        payer.getPropertyCards().add(property);

        ArrayList<Player> players = new ArrayList<>();
        players.add(receiver);
        players.add(payer);
        game.applyOnlineState(players, 0, false, new Game.PaymentRequest(receiver, payer, 7), false);

        PaymentSelectionPanel panel = new PaymentSelectionPanel(game);

        assertTrue(panel.handleCardClick(61, 181));
        assertTrue(panel.handleCardClick(151, 181));
        assertEquals(2, panel.getSelectedCards().size());
        assertTrue(panel.canConfirm());
    }

    @Test
    public void testUnselectingHotelAlsoRemovesBlockedHouseSelection() {
        Game game = new Game(2);
        DrawPileAndDiscardPile drawPile = new DrawPileAndDiscardPile();
        Player receiver = new Player(drawPile);
        Player payer = new Player(drawPile);
        PropertiesCards property = new PropertiesCards(PropertiesCardsType.BROWN);
        property.setHasHouse(true);
        property.setHasHotel(true);
        payer.getPropertyCards().add(property);

        ArrayList<Player> players = new ArrayList<>();
        players.add(receiver);
        players.add(payer);
        game.applyOnlineState(players, 0, false, new Game.PaymentRequest(receiver, payer, 7), false);

        PaymentSelectionPanel panel = new PaymentSelectionPanel(game);

        assertTrue(panel.handleCardClick(61, 181));
        assertTrue(panel.handleCardClick(151, 181));
        assertEquals(2, panel.getSelectedCards().size());

        assertTrue(panel.handleCardClick(61, 181));

        assertEquals(0, panel.getSelectedCards().size());
        assertFalse(panel.canConfirm());
    }
}
