package logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import model.ActionCardType;
import model.ActionCards;
import model.DrawPileAndDiscardPile;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;
import model.PropertiesCardsType;
import model.PropertyColor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class ActionCardServiceTest {
    @Test
    public void testPassGoDiscardsActionCardAndDrawsTwoCards() {
        TestContext context = new TestContext();
        Player currentPlayer = context.players.get(0);
        ActionCards passGo = addActionCardToHand(currentPlayer, ActionCardType.PASS_GO);

        assertTrue(context.service.finishPassGo(currentPlayer, passGo));

        assertFalse(currentPlayer.getHandCards().contains(passGo));
        assertTrue(currentPlayer.getDrawCardsAndDiscardPile().getDiscardPile().contains(passGo));
        assertEquals(2, currentPlayer.getHandCards().size());
        assertEquals(1, currentPlayer.getUseCardTimes());
    }

    @Test
    public void testSlyDealTransfersNonCompleteProperty() {
        TestContext context = new TestContext();
        Player currentPlayer = context.players.get(0);
        Player targetPlayer = context.players.get(1);
        ActionCards slyDeal = addActionCardToHand(currentPlayer, ActionCardType.SLY_DEAL);
        PropertiesCards stolenCard = new PropertiesCards(PropertiesCardsType.BROWN);
        targetPlayer.getPropertyCards().add(stolenCard);

        assertTrue(context.service.finishSlyDeal(currentPlayer, slyDeal, targetPlayer, stolenCard));

        assertTrue(currentPlayer.getPropertyCards().contains(stolenCard));
        assertFalse(targetPlayer.getPropertyCards().contains(stolenCard));
    }

    @Test
    public void testDealBreakerTransfersCompletedSet() {
        TestContext context = new TestContext();
        Player currentPlayer = context.players.get(0);
        Player targetPlayer = context.players.get(1);
        ActionCards dealBreaker = addActionCardToHand(currentPlayer, ActionCardType.DEAL_BREAKER);
        ArrayList<PropertiesCards> selectedSet = new ArrayList<>();
        selectedSet.add(new PropertiesCards(PropertiesCardsType.BROWN));
        selectedSet.add(new PropertiesCards(PropertiesCardsType.BROWN));
        targetPlayer.getPropertyCards().addAll(selectedSet);

        assertTrue(context.service.finishDealBreaker(currentPlayer, dealBreaker, targetPlayer, selectedSet));

        assertTrue(currentPlayer.getPropertyCards().containsAll(selectedSet));
        assertFalse(targetPlayer.getPropertyCards().contains(selectedSet.get(0)));
    }

    @Test
    public void testForcedDealSwapsProperties() {
        TestContext context = new TestContext();
        Player currentPlayer = context.players.get(0);
        Player targetPlayer = context.players.get(1);
        ActionCards forcedDeal = addActionCardToHand(currentPlayer, ActionCardType.FORCED_DEAL);
        PropertiesCards mine = new PropertiesCards(PropertiesCardsType.BROWN);
        PropertiesCards theirs = new PropertiesCards(PropertiesCardsType.DARK_BLUE);
        currentPlayer.getPropertyCards().add(mine);
        targetPlayer.getPropertyCards().add(theirs);

        assertTrue(context.service.finishForcedDeal(currentPlayer, forcedDeal, targetPlayer, mine, theirs));

        assertTrue(currentPlayer.getPropertyCards().contains(theirs));
        assertTrue(targetPlayer.getPropertyCards().contains(mine));
    }

    @Test
    public void testHouseAndHotelCanBeAddedToCompleteSet() {
        TestContext context = new TestContext();
        Player currentPlayer = context.players.get(0);
        addBrownSet(currentPlayer);
        ActionCards house = addActionCardToHand(currentPlayer, ActionCardType.HOUSE);
        ActionCards hotel = addActionCardToHand(currentPlayer, ActionCardType.HOTEL);

        assertTrue(context.service.finishHouse(currentPlayer, house, PropertyColor.BROWN));
        assertTrue(PlayerInfoHelper.hasHouse(currentPlayer, PropertyColor.BROWN));
        assertTrue(context.service.finishHotel(currentPlayer, hotel, PropertyColor.BROWN));
        assertTrue(PlayerInfoHelper.hasHotel(currentPlayer, PropertyColor.BROWN));
    }

    @Test
    public void testHouseCannotBeAddedToIncompleteSet() {
        TestContext context = new TestContext();
        Player currentPlayer = context.players.get(0);
        currentPlayer.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));
        ActionCards house = addActionCardToHand(currentPlayer, ActionCardType.HOUSE);

        assertFalse(context.service.finishHouse(currentPlayer, house, PropertyColor.BROWN));

        assertFalse(PlayerInfoHelper.hasHouse(currentPlayer, PropertyColor.BROWN));
        assertTrue(currentPlayer.getHandCards().contains(house));
    }

    @Test
    public void testHotelRequiresHouseFirst() {
        TestContext context = new TestContext();
        Player currentPlayer = context.players.get(0);
        addBrownSet(currentPlayer);
        ActionCards hotel = addActionCardToHand(currentPlayer, ActionCardType.HOTEL);

        assertFalse(context.service.finishHotel(currentPlayer, hotel, PropertyColor.BROWN));

        assertFalse(PlayerInfoHelper.hasHotel(currentPlayer, PropertyColor.BROWN));
        assertTrue(currentPlayer.getHandCards().contains(hotel));
    }

    @Test
    public void testCannotAddDuplicateHouseOrHotel() {
        TestContext context = new TestContext();
        Player currentPlayer = context.players.get(0);
        addBrownSet(currentPlayer);
        ActionCards firstHouse = addActionCardToHand(currentPlayer, ActionCardType.HOUSE);
        ActionCards secondHouse = addActionCardToHand(currentPlayer, ActionCardType.HOUSE);
        ActionCards firstHotel = addActionCardToHand(currentPlayer, ActionCardType.HOTEL);
        ActionCards secondHotel = addActionCardToHand(currentPlayer, ActionCardType.HOTEL);

        assertTrue(context.service.finishHouse(currentPlayer, firstHouse, PropertyColor.BROWN));
        assertFalse(context.service.finishHouse(currentPlayer, secondHouse, PropertyColor.BROWN));
        assertTrue(context.service.finishHotel(currentPlayer, firstHotel, PropertyColor.BROWN));
        assertFalse(context.service.finishHotel(currentPlayer, secondHotel, PropertyColor.BROWN));

        assertTrue(currentPlayer.getHandCards().contains(secondHouse));
        assertTrue(currentPlayer.getHandCards().contains(secondHotel));
    }

    @Test
    public void testRentWithDoubleRentCreatesPaymentAndUsesTwoCards() {
        TestContext context = new TestContext();
        Player currentPlayer = context.players.get(0);
        Player targetPlayer = context.players.get(1);
        addBrownSet(currentPlayer);
        targetPlayer.getBankCards().add(new MoneyCards(10));
        ActionCards rent = addActionCardToHand(currentPlayer, ActionCardType.RENT_WITH_BROWN_AND_LIGHT_BLUE);
        ActionCards doubleRent = addActionCardToHand(currentPlayer, ActionCardType.DOUBLE_THE_RENT);

        assertTrue(context.service.finishTwoColorRent(currentPlayer, rent, PropertyColor.BROWN, true));

        assertSame(targetPlayer, context.paymentManager.getCurrentPaymentRequest().getPayer());
        assertEquals(4, context.paymentManager.getCurrentPaymentRequest().getAmount());
        assertFalse(currentPlayer.getHandCards().contains(doubleRent));
        assertEquals(2, currentPlayer.getUseCardTimes());
    }

    @Test
    public void testDebtCollectorCreatesPaymentRequest() {
        TestContext context = new TestContext();
        Player currentPlayer = context.players.get(0);
        Player targetPlayer = context.players.get(1);
        targetPlayer.getBankCards().add(new MoneyCards(5));
        ActionCards debtCollector = addActionCardToHand(currentPlayer, ActionCardType.DEBT_COLLECTOR);

        assertTrue(context.service.finishDebtCollector(currentPlayer, debtCollector, targetPlayer));

        assertSame(targetPlayer, context.paymentManager.getCurrentPaymentRequest().getPayer());
        assertEquals(5, context.paymentManager.getCurrentPaymentRequest().getAmount());
    }

    private ActionCards addActionCardToHand(Player player, ActionCardType type) {
        ActionCards card = new ActionCards(type);
        player.getHandCards().add(card);
        return card;
    }

    private void addBrownSet(Player player) {
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));
    }

    private static class TestContext {
        private final ArrayList<Player> players = new ArrayList<>();
        private final PaymentManager paymentManager = new PaymentManager();
        private final ActionCardService service;

        private TestContext() {
            DrawPileAndDiscardPile drawPile = new DrawPileAndDiscardPile();
            players.add(new Player(drawPile));
            players.add(new Player(drawPile));
            service = new ActionCardService(players, paymentManager, new RentCalculator());
        }
    }
}
