package logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import model.ActionCardType;
import model.ActionCards;
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

public class PaymentManagerTest {
    @Test
    public void testPaymentRequestIsIgnoredWhenPayerHasNoAssets() {
        PaymentManager manager = new PaymentManager();
        Player receiver = new Player(new DrawPileAndDiscardPile());
        Player payer = new Player(new DrawPileAndDiscardPile());

        manager.addPaymentRequest(receiver, payer, 5);
        manager.startNextPaymentRequest();

        assertFalse(manager.isPaymentSelecting());
    }

    @Test
    public void testFinishPaymentTransfersSelectedBankAndPropertyCards() {
        PaymentManager manager = new PaymentManager();
        Player receiver = new Player(new DrawPileAndDiscardPile());
        Player payer = new Player(new DrawPileAndDiscardPile());
        MoneyCards money = new MoneyCards(3);
        PropertiesCards property = new PropertiesCards(PropertiesCardsType.BROWN);
        payer.getBankCards().add(money);
        payer.getPropertyCards().add(property);

        manager.addPaymentRequest(receiver, payer, 4);
        manager.startNextPaymentRequest();

        ArrayList<Card> selectedCards = new ArrayList<>();
        selectedCards.add(money);
        selectedCards.add(property);

        assertTrue(manager.finishCurrentPayment(selectedCards));
        assertTrue(receiver.getBankCards().contains(money));
        assertTrue(receiver.getPropertyCards().contains(property));
        assertFalse(manager.isPaymentSelecting());
    }

    @Test
    public void testInvalidPaymentSelectionDoesNotTransferCards() {
        PaymentManager manager = new PaymentManager();
        Player receiver = new Player(new DrawPileAndDiscardPile());
        Player payer = new Player(new DrawPileAndDiscardPile());
        MoneyCards money = new MoneyCards(3);
        MoneyCards unselectedMoney = new MoneyCards(4);
        payer.getBankCards().add(money);
        payer.getBankCards().add(unselectedMoney);

        manager.addPaymentRequest(receiver, payer, 5);
        manager.startNextPaymentRequest();

        ArrayList<Card> selectedCards = new ArrayList<>();
        selectedCards.add(money);

        assertFalse(manager.finishCurrentPayment(selectedCards));
        assertTrue(payer.getBankCards().contains(money));
        assertTrue(payer.getBankCards().contains(unselectedMoney));
        assertFalse(receiver.getBankCards().contains(money));
    }

    @Test
    public void testJustSayNoCancelsCurrentPayment() {
        PaymentManager manager = new PaymentManager();
        Player receiver = new Player(new DrawPileAndDiscardPile());
        Player payer = new Player(new DrawPileAndDiscardPile());
        ActionCards justSayNo = new ActionCards(ActionCardType.JUST_SAY_NO);
        payer.getHandCards().add(justSayNo);
        payer.getBankCards().add(new MoneyCards(5));

        manager.addPaymentRequest(receiver, payer, 5);
        manager.startNextPaymentRequest();

        assertTrue(manager.canCurrentPaymentUseJustSayNo());
        manager.currentPaymentUseJustSayNo();

        assertNull(manager.getCurrentPaymentRequest());
        assertFalse(payer.getHandCards().contains(justSayNo));
        assertTrue(payer.getDrawCardsAndDiscardPile().getDiscardPile().contains(justSayNo));
    }

    @Test
    public void testJustSayNoCanBeCounteredToResumePayment() {
        PaymentManager manager = new PaymentManager();
        Player receiver = new Player(new DrawPileAndDiscardPile());
        Player payer = new Player(new DrawPileAndDiscardPile());
        ActionCards payerJustSayNo = new ActionCards(ActionCardType.JUST_SAY_NO);
        ActionCards receiverJustSayNo = new ActionCards(ActionCardType.JUST_SAY_NO);
        MoneyCards money = new MoneyCards(5);
        payer.getHandCards().add(payerJustSayNo);
        receiver.getHandCards().add(receiverJustSayNo);
        payer.getBankCards().add(money);

        manager.addPaymentRequest(receiver, payer, 5);
        manager.startNextPaymentRequest();
        manager.currentPaymentUseJustSayNo();

        assertTrue(manager.isCurrentPaymentWaitingForJustSayNoResponse());
        assertSame(receiver, manager.getCurrentJustSayNoResponder());
        assertFalse(payer.getHandCards().contains(payerJustSayNo));

        manager.currentPaymentUseJustSayNo();

        assertSame(payer, manager.getCurrentPaymentRequest().getPayer());
        assertFalse(manager.isCurrentPaymentWaitingForJustSayNoResponse());
        assertFalse(receiver.getHandCards().contains(receiverJustSayNo));
        assertTrue(manager.finishCurrentPayment(new ArrayList<>(java.util.List.of(money))));
        assertTrue(receiver.getBankCards().contains(money));
    }

    @Test
    public void testJustSayNoCounterCanBeCounteredAgain() {
        PaymentManager manager = new PaymentManager();
        Player receiver = new Player(new DrawPileAndDiscardPile());
        Player payer = new Player(new DrawPileAndDiscardPile());
        payer.getHandCards().add(new ActionCards(ActionCardType.JUST_SAY_NO));
        payer.getHandCards().add(new ActionCards(ActionCardType.JUST_SAY_NO));
        receiver.getHandCards().add(new ActionCards(ActionCardType.JUST_SAY_NO));
        payer.getBankCards().add(new MoneyCards(5));

        manager.addPaymentRequest(receiver, payer, 5);
        manager.startNextPaymentRequest();
        manager.currentPaymentUseJustSayNo();
        manager.currentPaymentUseJustSayNo();

        assertTrue(manager.isCurrentPaymentWaitingForJustSayNoResponse());
        assertSame(payer, manager.getCurrentJustSayNoResponder());

        manager.currentPaymentUseJustSayNo();

        assertNull(manager.getCurrentPaymentRequest());
        assertFalse(manager.isPaymentSelecting());
    }

    @Test
    public void testApplyOnlineStateReplacesCurrentPayment() {
        PaymentManager manager = new PaymentManager();
        Player receiver = new Player(new DrawPileAndDiscardPile());
        Player payer = new Player(new DrawPileAndDiscardPile());
        Game.PaymentRequest request = new Game.PaymentRequest(receiver, payer, 2);

        manager.applyOnlineState(request);

        assertSame(request, manager.getCurrentPaymentRequest());
    }

    @Test
    public void testAssetAndCardValueHelpers() {
        PaymentManager manager = new PaymentManager();
        Player player = new Player(new DrawPileAndDiscardPile());
        MoneyCards money = new MoneyCards(5);
        PropertiesCards property = new PropertiesCards(PropertiesCardsType.DARK_BLUE);
        player.getBankCards().add(money);
        player.getPropertyCards().add(property);

        ArrayList<Card> cards = new ArrayList<>();
        cards.add(money);
        cards.add(property);

        assertEquals(9, manager.getTotalAssetsValue(player));
        assertEquals(9, manager.getCardsValue(cards));
    }

    @Test
    public void testBuiltSetPaysHotelThenHouseToBankBeforeProperty() {
        PaymentManager manager = new PaymentManager();
        Player receiver = new Player(new DrawPileAndDiscardPile());
        Player payer = new Player(new DrawPileAndDiscardPile());
        PropertiesCards propertyWithBuildings = new PropertiesCards(PropertiesCardsType.BROWN);
        PropertiesCards selectedProperty = new PropertiesCards(PropertiesCardsType.BROWN);
        propertyWithBuildings.setHasHouse(true);
        propertyWithBuildings.setHasHotel(true);
        payer.getPropertyCards().add(propertyWithBuildings);
        payer.getPropertyCards().add(selectedProperty);

        manager.addPaymentRequest(receiver, payer, 9);
        manager.startNextPaymentRequest();

        ArrayList<Card> selectedCards = new ArrayList<>();
        selectedCards.add(new BuildingPaymentCard(ActionCardType.HOTEL, PropertyColor.BROWN));
        selectedCards.add(new BuildingPaymentCard(ActionCardType.HOUSE, PropertyColor.BROWN));
        selectedCards.add(propertyWithBuildings);
        selectedCards.add(selectedProperty);

        assertEquals(9, manager.getPaymentCardsValue(payer, selectedCards));
        assertTrue(manager.finishCurrentPayment(selectedCards));

        assertEquals(2, receiver.getBankCards().size());
        assertActionCard(receiver.getBankCards().get(0), ActionCardType.HOTEL);
        assertActionCard(receiver.getBankCards().get(1), ActionCardType.HOUSE);
        assertTrue(receiver.getPropertyCards().contains(propertyWithBuildings));
        assertTrue(receiver.getPropertyCards().contains(selectedProperty));
        assertFalse(propertyWithBuildings.hasHotel());
        assertFalse(propertyWithBuildings.hasHouse());
        assertEquals(0, PlayerInfoHelper.getPropertyCountByCurrentColor(payer, PropertyColor.BROWN));
    }

    @Test
    public void testPropertiesDoNotCountBuildingValuesUntilBuildingsAreSelected() {
        PaymentManager manager = new PaymentManager();
        Player receiver = new Player(new DrawPileAndDiscardPile());
        Player payer = new Player(new DrawPileAndDiscardPile());
        PropertiesCards first = new PropertiesCards(PropertiesCardsType.BROWN);
        PropertiesCards second = new PropertiesCards(PropertiesCardsType.BROWN);
        first.setHasHouse(true);
        first.setHasHotel(true);
        payer.getPropertyCards().add(first);
        payer.getPropertyCards().add(second);
        manager.addPaymentRequest(receiver, payer, 2);
        manager.startNextPaymentRequest();

        ArrayList<Card> selectedCards = new ArrayList<>();
        selectedCards.add(first);
        selectedCards.add(second);

        assertEquals(2, manager.getPaymentCardsValue(payer, selectedCards));
        assertEquals(9, manager.getTotalAssetsValue(payer));
        assertFalse(manager.finishCurrentPayment(selectedCards));
        assertTrue(payer.getPropertyCards().contains(first));
        assertTrue(payer.getPropertyCards().contains(second));
    }

    @Test
    public void testSelectedBuildingsMoveToReceiverBankAndStayOffProperty() {
        PaymentManager manager = new PaymentManager();
        Player receiver = new Player(new DrawPileAndDiscardPile());
        Player payer = new Player(new DrawPileAndDiscardPile());
        PropertiesCards propertyWithBuildings = new PropertiesCards(PropertiesCardsType.BROWN);
        propertyWithBuildings.setHasHouse(true);
        propertyWithBuildings.setHasHotel(true);
        payer.getPropertyCards().add(propertyWithBuildings);
        manager.addPaymentRequest(receiver, payer, 7);
        manager.startNextPaymentRequest();

        ArrayList<Card> selectedCards = new ArrayList<>();
        selectedCards.add(new BuildingPaymentCard(ActionCardType.HOTEL, PropertyColor.BROWN));
        selectedCards.add(new BuildingPaymentCard(ActionCardType.HOUSE, PropertyColor.BROWN));

        assertTrue(manager.finishCurrentPayment(selectedCards));

        assertEquals(2, receiver.getBankCards().size());
        assertActionCard(receiver.getBankCards().get(0), ActionCardType.HOTEL);
        assertActionCard(receiver.getBankCards().get(1), ActionCardType.HOUSE);
        assertTrue(payer.getPropertyCards().contains(propertyWithBuildings));
        assertFalse(propertyWithBuildings.hasHotel());
        assertFalse(propertyWithBuildings.hasHouse());
    }

    @Test
    public void testHouseCannotBePaidBeforeHotel() {
        PaymentManager manager = new PaymentManager();
        Player receiver = new Player(new DrawPileAndDiscardPile());
        Player payer = new Player(new DrawPileAndDiscardPile());
        PropertiesCards propertyWithBuildings = new PropertiesCards(PropertiesCardsType.BROWN);
        propertyWithBuildings.setHasHouse(true);
        propertyWithBuildings.setHasHotel(true);
        payer.getPropertyCards().add(propertyWithBuildings);
        manager.addPaymentRequest(receiver, payer, 3);
        manager.startNextPaymentRequest();

        ArrayList<Card> selectedCards = new ArrayList<>();
        selectedCards.add(new BuildingPaymentCard(ActionCardType.HOUSE, PropertyColor.BROWN));

        assertFalse(manager.finishCurrentPayment(selectedCards));
        assertTrue(propertyWithBuildings.hasHotel());
        assertTrue(propertyWithBuildings.hasHouse());
        assertTrue(receiver.getBankCards().isEmpty());
    }

    private void assertActionCard(Card card, ActionCardType type) {
        assertTrue(card instanceof ActionCards);
        assertEquals(type, ((ActionCards) card).getActionCardType());
    }
}
