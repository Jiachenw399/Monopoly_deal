package network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import logic.Game;
import model.ActionCardType;
import model.BuildingPaymentCard;
import model.Card;
import model.DrawPileAndDiscardPile;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;
import model.PropertiesCardsType;
import model.PropertyColor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class PaymentSelectionSymmetricTest {
    @Test
    public void testBankCardRoundTrip() {
        DrawPileAndDiscardPile drawPile = new DrawPileAndDiscardPile();
        Player receiver = NetworkSymmetricTestFixtures.player(drawPile, "Receiver");
        Player payer = NetworkSymmetricTestFixtures.player(drawPile, "Payer");
        MoneyCards money = new MoneyCards(3);
        payer.getBankCards().add(money);

        Game game = NetworkSymmetricTestFixtures.gameWithActivePayment(receiver, payer, 3);
        OnlineGameClickActions encoder = NetworkSymmetricTestFixtures.onlineEncoder(game, 2);
        GameServer server = NetworkSymmetricTestFixtures.serverBoundTo(game);

        ArrayList<Card> selected = new ArrayList<>();
        selected.add(money);
        String encoded = encoder.paymentBody(selected);
        ArrayList<Card> decoded = server.parsePaymentCards(payer, encoded);

        NetworkSymmetricTestFixtures.assertDecodedPaymentMatches(selected, decoded);
        assertEquals("B1", encoded);
    }

    @Test
    public void testPropertyCardRoundTrip() {
        DrawPileAndDiscardPile drawPile = new DrawPileAndDiscardPile();
        Player receiver = NetworkSymmetricTestFixtures.player(drawPile, "Receiver");
        Player payer = NetworkSymmetricTestFixtures.player(drawPile, "Payer");
        PropertiesCards property = new PropertiesCards(PropertiesCardsType.BROWN);
        payer.getPropertyCards().add(property);

        Game game = NetworkSymmetricTestFixtures.gameWithActivePayment(receiver, payer, 1);
        OnlineGameClickActions encoder = NetworkSymmetricTestFixtures.onlineEncoder(game, 2);
        GameServer server = NetworkSymmetricTestFixtures.serverBoundTo(game);

        ArrayList<Card> selected = new ArrayList<>();
        selected.add(property);
        String encoded = encoder.paymentBody(selected);
        ArrayList<Card> decoded = server.parsePaymentCards(payer, encoded);

        NetworkSymmetricTestFixtures.assertDecodedPaymentMatches(selected, decoded);
        assertEquals("P1", encoded);
    }

    @Test
    public void testBuildingPaymentRoundTrip() {
        DrawPileAndDiscardPile drawPile = new DrawPileAndDiscardPile();
        Player receiver = NetworkSymmetricTestFixtures.player(drawPile, "Receiver");
        Player payer = NetworkSymmetricTestFixtures.player(drawPile, "Payer");
        BuildingPaymentCard house = new BuildingPaymentCard(ActionCardType.HOUSE, PropertyColor.BROWN);
        BuildingPaymentCard hotel = new BuildingPaymentCard(ActionCardType.HOTEL, PropertyColor.LIGHT_BLUE);

        Game game = NetworkSymmetricTestFixtures.gameWithActivePayment(receiver, payer, 7);
        OnlineGameClickActions encoder = NetworkSymmetricTestFixtures.onlineEncoder(game, 2);
        GameServer server = NetworkSymmetricTestFixtures.serverBoundTo(game);

        ArrayList<Card> selected = new ArrayList<>();
        selected.add(house);
        selected.add(hotel);
        String encoded = encoder.paymentBody(selected);
        ArrayList<Card> decoded = server.parsePaymentCards(payer, encoded);

        NetworkSymmetricTestFixtures.assertDecodedPaymentMatches(selected, decoded);
        assertTrue(encoded.contains("HOUSE:BROWN"));
        assertTrue(encoded.contains("HOTEL:LIGHT_BLUE"));
    }

    @Test
    public void testMixedPaymentRoundTripWithCommaSeparator() {
        DrawPileAndDiscardPile drawPile = new DrawPileAndDiscardPile();
        Player receiver = NetworkSymmetricTestFixtures.player(drawPile, "Receiver");
        Player payer = NetworkSymmetricTestFixtures.player(drawPile, "Payer");
        MoneyCards moneyOne = new MoneyCards(1);
        MoneyCards moneyTwo = new MoneyCards(4);
        PropertiesCards property = new PropertiesCards(PropertiesCardsType.LIGHT_BLUE);
        BuildingPaymentCard house = new BuildingPaymentCard(ActionCardType.HOUSE, PropertyColor.LIGHT_BLUE);
        payer.getBankCards().add(moneyOne);
        payer.getBankCards().add(moneyTwo);
        payer.getPropertyCards().add(property);

        Game game = NetworkSymmetricTestFixtures.gameWithActivePayment(receiver, payer, 10);
        OnlineGameClickActions encoder = NetworkSymmetricTestFixtures.onlineEncoder(game, 2);
        GameServer server = NetworkSymmetricTestFixtures.serverBoundTo(game);

        ArrayList<Card> selected = new ArrayList<>();
        selected.add(moneyOne);
        selected.add(house);
        selected.add(property);
        selected.add(moneyTwo);
        String encoded = encoder.paymentBody(selected);

        ArrayList<Card> decodedWithSpaces = server.parsePaymentCards(payer, encoded);
        NetworkSymmetricTestFixtures.assertDecodedPaymentMatches(selected, decodedWithSpaces);

        String commaSeparated = encoded.replace(' ', ',');
        ArrayList<Card> decodedWithCommas = server.parsePaymentCards(payer, commaSeparated);
        NetworkSymmetricTestFixtures.assertDecodedPaymentMatches(selected, decodedWithCommas);
    }

    @Test
    public void testEmptyPaymentBodyDecodesToEmptyList() {
        DrawPileAndDiscardPile drawPile = new DrawPileAndDiscardPile();
        Player receiver = NetworkSymmetricTestFixtures.player(drawPile, "Receiver");
        Player payer = NetworkSymmetricTestFixtures.player(drawPile, "Payer");
        payer.getBankCards().add(new MoneyCards(2));

        Game game = NetworkSymmetricTestFixtures.gameWithActivePayment(receiver, payer, 2);
        GameServer server = NetworkSymmetricTestFixtures.serverBoundTo(game);

        assertTrue(server.parsePaymentCards(payer, "").isEmpty());
        assertTrue(server.parsePaymentCards(payer, "   ").isEmpty());
    }
}
