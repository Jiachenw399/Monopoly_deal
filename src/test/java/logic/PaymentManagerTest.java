package logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import model.ActionCardType;
import model.ActionCards;
import model.Card;
import model.DrawPileAndDiscardPile;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;
import model.PropertiesCardsType;
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
}
