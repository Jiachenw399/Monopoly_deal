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

public class ActionCardServiceGuardTest {

    @Test
    public void testSlyDealFailsForNullTarget() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        ActionCards slyDeal = addToHand(current, ActionCardType.SLY_DEAL);
        PropertiesCards card = new PropertiesCards(PropertiesCardsType.BROWN);

        assertFalse(ctx.service.finishSlyDeal(current, slyDeal, null, card));
    }

    @Test
    public void testSlyDealFailsForSelfTarget() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        ActionCards slyDeal = addToHand(current, ActionCardType.SLY_DEAL);
        PropertiesCards card = new PropertiesCards(PropertiesCardsType.BROWN);
        current.getPropertyCards().add(card);

        assertFalse(ctx.service.finishSlyDeal(current, slyDeal, current, card));
    }

    @Test
    public void testSlyDealFailsForCompleteSet() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        Player target = ctx.players.get(1);
        ActionCards slyDeal = addToHand(current, ActionCardType.SLY_DEAL);
        PropertiesCards brown1 = new PropertiesCards(PropertiesCardsType.BROWN);
        PropertiesCards brown2 = new PropertiesCards(PropertiesCardsType.BROWN);
        target.getPropertyCards().add(brown1);
        target.getPropertyCards().add(brown2);

        assertFalse(ctx.service.finishSlyDeal(current, slyDeal, target, brown1));
    }

    @Test
    public void testSlyDealFailsForCardNotOwnedByTarget() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        Player target = ctx.players.get(1);
        ActionCards slyDeal = addToHand(current, ActionCardType.SLY_DEAL);
        PropertiesCards card = new PropertiesCards(PropertiesCardsType.BROWN);
        current.getPropertyCards().add(card);

        assertFalse(ctx.service.finishSlyDeal(current, slyDeal, target, card));
    }

    @Test
    public void testForcedDealFailsForSelfTarget() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        Player target = ctx.players.get(1);
        ActionCards forcedDeal = addToHand(current, ActionCardType.FORCED_DEAL);
        PropertiesCards mine = new PropertiesCards(PropertiesCardsType.BROWN);
        PropertiesCards theirs = new PropertiesCards(PropertiesCardsType.DARK_BLUE);
        current.getPropertyCards().add(mine);
        target.getPropertyCards().add(theirs);

        assertFalse(ctx.service.finishForcedDeal(current, forcedDeal, current, mine, theirs));
    }

    @Test
    public void testForcedDealFailsForNullCurrentPlayerCard() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        Player target = ctx.players.get(1);
        ActionCards forcedDeal = addToHand(current, ActionCardType.FORCED_DEAL);
        PropertiesCards theirs = new PropertiesCards(PropertiesCardsType.DARK_BLUE);
        target.getPropertyCards().add(theirs);

        assertFalse(ctx.service.finishForcedDeal(current, forcedDeal, target, null, theirs));
    }

    @Test
    public void testForcedDealFailsForNullTargetCard() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        Player target = ctx.players.get(1);
        ActionCards forcedDeal = addToHand(current, ActionCardType.FORCED_DEAL);
        PropertiesCards mine = new PropertiesCards(PropertiesCardsType.BROWN);
        current.getPropertyCards().add(mine);

        assertFalse(ctx.service.finishForcedDeal(current, forcedDeal, target, mine, null));
    }

    @Test
    public void testForcedDealFailsWhenCurrentPlayerDoesNotOwnTheirCard() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        Player target = ctx.players.get(1);
        ActionCards forcedDeal = addToHand(current, ActionCardType.FORCED_DEAL);
        PropertiesCards mine = new PropertiesCards(PropertiesCardsType.BROWN);
        PropertiesCards theirs = new PropertiesCards(PropertiesCardsType.DARK_BLUE);
        target.getPropertyCards().add(mine);
        target.getPropertyCards().add(theirs);

        assertFalse(ctx.service.finishForcedDeal(current, forcedDeal, target, mine, theirs));
    }

    @Test
    public void testForcedDealFailsWhenTargetDoesNotOwnTheirCard() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        Player target = ctx.players.get(1);
        ActionCards forcedDeal = addToHand(current, ActionCardType.FORCED_DEAL);
        PropertiesCards mine = new PropertiesCards(PropertiesCardsType.BROWN);
        PropertiesCards theirs = new PropertiesCards(PropertiesCardsType.DARK_BLUE);
        current.getPropertyCards().add(mine);
        current.getPropertyCards().add(theirs);

        assertFalse(ctx.service.finishForcedDeal(current, forcedDeal, target, mine, theirs));
    }

    @Test
    public void testForcedDealFailsWhenBothCardsAreInCompleteSet() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        Player target = ctx.players.get(1);
        ActionCards forcedDeal = addToHand(current, ActionCardType.FORCED_DEAL);
        PropertiesCards brown1 = new PropertiesCards(PropertiesCardsType.BROWN);
        PropertiesCards brown2 = new PropertiesCards(PropertiesCardsType.BROWN);
        PropertiesCards darkBlue1 = new PropertiesCards(PropertiesCardsType.DARK_BLUE);
        PropertiesCards darkBlue2 = new PropertiesCards(PropertiesCardsType.DARK_BLUE);
        current.getPropertyCards().add(brown1);
        current.getPropertyCards().add(brown2);
        target.getPropertyCards().add(darkBlue1);
        target.getPropertyCards().add(darkBlue2);

        PropertiesCards stolenFromCurrent = new PropertiesCards(PropertiesCardsType.BROWN);
        PropertiesCards stolenFromTarget = new PropertiesCards(PropertiesCardsType.DARK_BLUE);
        current.getHandCards().add(stolenFromCurrent);
        target.getPropertyCards().add(stolenFromTarget);

        assertFalse(ctx.service.finishForcedDeal(current, forcedDeal, target, stolenFromCurrent, stolenFromTarget));
    }

    @Test
    public void testDealBreakerFailsForNullTarget() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        ActionCards dealBreaker = addToHand(current, ActionCardType.DEAL_BREAKER);
        ArrayList<PropertiesCards> set = new ArrayList<>();

        assertFalse(ctx.service.finishDealBreaker(current, dealBreaker, null, set));
    }

    @Test
    public void testDealBreakerFailsWhenNotAllCardsBelongToTarget() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        Player target = ctx.players.get(1);
        ActionCards dealBreaker = addToHand(current, ActionCardType.DEAL_BREAKER);
        PropertiesCards ownedByCurrent = new PropertiesCards(PropertiesCardsType.BROWN);
        PropertiesCards ownedByTarget = new PropertiesCards(PropertiesCardsType.BROWN);
        current.getPropertyCards().add(ownedByCurrent);
        target.getPropertyCards().add(ownedByTarget);
        ArrayList<PropertiesCards> set = new ArrayList<>();
        set.add(ownedByCurrent);
        set.add(ownedByTarget);

        assertFalse(ctx.service.finishDealBreaker(current, dealBreaker, target, set));
    }

    @Test
    public void testDealBreakerFailsForIncompleteSet() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        Player target = ctx.players.get(1);
        ActionCards dealBreaker = addToHand(current, ActionCardType.DEAL_BREAKER);
        PropertiesCards brown1 = new PropertiesCards(PropertiesCardsType.BROWN);
        target.getPropertyCards().add(brown1);
        ArrayList<PropertiesCards> set = new ArrayList<>();
        set.add(brown1);

        assertFalse(ctx.service.finishDealBreaker(current, dealBreaker, target, set));
    }

    @Test
    public void testDebtCollectorFailsForNullTarget() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        ActionCards debt = addToHand(current, ActionCardType.DEBT_COLLECTOR);

        assertFalse(ctx.service.finishDebtCollector(current, debt, null));
    }

    @Test
    public void testDebtCollectorFailsForSelfTarget() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        ActionCards debt = addToHand(current, ActionCardType.DEBT_COLLECTOR);

        assertFalse(ctx.service.finishDebtCollector(current, debt, current));
    }

    @Test
    public void testTwoColorRentFailsWhenPlayerDoesNotOwnColor() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        ActionCards rent = addToHand(current, ActionCardType.RENT_WITH_BROWN_AND_LIGHT_BLUE);

        assertFalse(ctx.service.finishTwoColorRent(current, rent, PropertyColor.BROWN, false));
    }

    @Test
    public void testMultipleColorRentFailsWhenPlayerDoesNotOwnColor() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        Player target = ctx.players.get(1);
        ActionCards rent = addToHand(current, ActionCardType.RENT_WITH_MULTIPLE_COLOR);

        assertFalse(ctx.service.finishMultipleColorRent(current, rent, target, PropertyColor.BROWN, false));
    }

    @Test
    public void testMultipleColorRentFailsForNullTarget() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        ActionCards rent = addToHand(current, ActionCardType.RENT_WITH_MULTIPLE_COLOR);

        assertFalse(ctx.service.finishMultipleColorRent(current, rent, null, PropertyColor.BROWN, false));
    }

    @Test
    public void testHouseFailsWhenPlayerDoesNotOwnColor() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        ActionCards house = addToHand(current, ActionCardType.HOUSE);

        assertFalse(ctx.service.finishHouse(current, house, PropertyColor.BROWN));
    }

    @Test
    public void testHotelFailsWhenPlayerDoesNotOwnColor() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        ActionCards hotel = addToHand(current, ActionCardType.HOTEL);

        assertFalse(ctx.service.finishHotel(current, hotel, PropertyColor.BROWN));
    }

    @Test
    public void testDoubleTheRentCanBeUsedWithRentCard() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        Player target = ctx.players.get(1);
        current.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));
        current.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));
        target.getBankCards().add(new MoneyCards(10));
        ActionCards rent = addToHand(current, ActionCardType.RENT_WITH_BROWN_AND_LIGHT_BLUE);
        ActionCards doubleRent = addToHand(current, ActionCardType.DOUBLE_THE_RENT);

        assertTrue(ctx.service.finishTwoColorRent(current, rent, PropertyColor.BROWN, true));

        assertSame(target, ctx.paymentManager.getCurrentPaymentRequest().getPayer());
        assertEquals(4, ctx.paymentManager.getCurrentPaymentRequest().getAmount());
    }

    @Test
    public void testHasDoubleTheRentCardReturnsFalseForNull() {
        TestContext ctx = new TestContext();
        assertFalse(ctx.service.hasDoubleTheRentCard(null));
    }

    @Test
    public void testHasDoubleTheRentCardReturnsFalseWhenNotOwned() {
        TestContext ctx = new TestContext();
        Player player = ctx.players.get(0);
        player.getHandCards().add(new MoneyCards(5));

        assertFalse(ctx.service.hasDoubleTheRentCard(player));
    }

    @Test
    public void testPassGoDrawsTwoCards() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        current.getHandCards().clear();
        ActionCards passGo = addToHand(current, ActionCardType.PASS_GO);

        ctx.service.finishPassGo(current, passGo);

        assertEquals(2, current.getHandCards().size());
        assertFalse(current.getHandCards().contains(passGo));
    }

    @Test
    public void testBirthdayCreatesPaymentForAllOtherPlayers() {
        TestContext ctx = new TestContext();
        Player current = ctx.players.get(0);
        Player target1 = ctx.players.get(1);
        ActionCards birthday = addToHand(current, ActionCardType.BIRTHDAY);
        target1.getBankCards().add(new MoneyCards(5));

        ctx.service.finishBirthday(current, birthday);

        assertTrue(ctx.paymentManager.isPaymentSelecting());
        assertEquals(2, ctx.paymentManager.getCurrentPaymentRequest().getAmount());
    }

    private ActionCards addToHand(Player player, ActionCardType type) {
        ActionCards card = new ActionCards(type);
        player.getHandCards().add(card);
        return card;
    }

    private static class TestContext {
        private final ArrayList<Player> players = new ArrayList<>();
        private final PaymentManager paymentManager = new PaymentManager();
        private final ActionCardService service;

        private TestContext() {
            DrawPileAndDiscardPile drawPile = new DrawPileAndDiscardPile();
            players.add(new Player(drawPile));
            players.add(new Player(drawPile));
            players.add(new Player(drawPile));
            service = new ActionCardService(players, paymentManager, new RentCalculator());
        }
    }
}
