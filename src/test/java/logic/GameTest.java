package logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import model.ActionCardType;
import model.ActionCards;
import model.Card;
import model.Player;
import model.PropertiesCards;
import model.PropertiesCardsType;
import model.PropertyColor;

public class GameTest {
    private static class CountingObserver implements GameObserver {
        private int notificationCount;

        @Override
        public void onGameStateChanged() {
            notificationCount++;
        }
    }

    @Test
    public void testGameInitialization() {
        Game game = new Game();

        // 初始4个玩家
        assertEquals(4, game.getPlayers().size());

        // 每个玩家一开始5张手牌
        for (Player p : game.getPlayers()) {
            assertEquals(5, p.getHandCards().size());
        }
        assertFalse(game.isWin());
    }

    @Test//测试抓牌
    public void testStartTurnDrawTwoCards() {
        Game game = new Game();
        game.startGame();
        Player currentPlayer = game.getCurrentPlayer();
        int before = currentPlayer.getHandCards().size();
        game.startTurn(currentPlayer);
        // 手牌非空时抓2张
        assertEquals(before + 2, currentPlayer.getHandCards().size());
    }

    @Test//测试没牌抓五张
    public void testDrawFiveCardsWhenEmpty() {
        Game game = new Game();
        game.startGame();
        Player currentPlayer = game.getCurrentPlayer();
        //清空当前玩家手牌
        currentPlayer.getHandCards().clear();
        assertTrue(currentPlayer.getHandCards().isEmpty());
        game.startTurn(currentPlayer);
        //没手牌抓五张
        assertEquals(5, currentPlayer.getHandCards().size());
    }
    @Test
    public void testObserverIsNotifiedWhenGameStateChanges() {
        Game game = new Game();
        CountingObserver observer = new CountingObserver();

        game.addObserver(observer);
        game.startGame();

        assertTrue(observer.notificationCount > 0);
    }

    @Test
    public void testChangingWildPropertyColorChecksWinImmediately() {
        Game game = new Game();
        game.startGame();
        Player player = game.getCurrentPlayer();
        addCompleteBrownSet(player);
        addCompleteLightGreenSet(player);
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.DARK_BLUE));
        PropertiesCards wildCard = new PropertiesCards(PropertiesCardsType.WILD_ALL);
        player.getPropertyCards().add(wildCard);

        assertFalse(game.isWin());

        assertTrue(game.setPropertyColor(player, wildCard, PropertyColor.DARK_BLUE));

        assertTrue(game.isWin());
    }

    @Test
    public void testCannotChangeWildColorIfItBreaksBuiltCompleteSet() {
        Game game = new Game(2);
        game.startGame();
        Player player = game.getCurrentPlayer();
        PropertiesCards brownProperty = new PropertiesCards(PropertiesCardsType.BROWN);
        PropertiesCards wildCard = new PropertiesCards(PropertiesCardsType.WILD_LIGHT_BLUE_BROWN);
        wildCard.setCurrentColor(PropertyColor.BROWN);
        brownProperty.setHasHouse(true);
        player.getPropertyCards().add(brownProperty);
        player.getPropertyCards().add(wildCard);

        assertFalse(game.setPropertyColor(player, wildCard, PropertyColor.LIGHT_BLUE));
        assertEquals(PropertyColor.BROWN, wildCard.getCurrentColor());
    }

    @Test
    public void testCanChangeWildColorWhenBuiltOldColorRemainsComplete() {
        Game game = new Game(2);
        game.startGame();
        Player player = game.getCurrentPlayer();
        PropertiesCards firstBrown = new PropertiesCards(PropertiesCardsType.BROWN);
        PropertiesCards secondBrown = new PropertiesCards(PropertiesCardsType.BROWN);
        PropertiesCards wildCard = new PropertiesCards(PropertiesCardsType.WILD_LIGHT_BLUE_BROWN);
        wildCard.setCurrentColor(PropertyColor.BROWN);
        firstBrown.setHasHouse(true);
        player.getPropertyCards().add(firstBrown);
        player.getPropertyCards().add(secondBrown);
        player.getPropertyCards().add(wildCard);

        assertTrue(game.setPropertyColor(player, wildCard, PropertyColor.LIGHT_BLUE));
        assertEquals(PropertyColor.LIGHT_BLUE, wildCard.getCurrentColor());
    }

    @Test
    public void testPaymentPropertyTransferChecksReceiverWinImmediately() {
        Game game = new Game(2);
        game.startGame();
        Player receiver = game.getPlayers().get(0);
        Player payer = game.getPlayers().get(1);
        addCompleteBrownSet(receiver);
        addCompleteLightGreenSet(receiver);
        receiver.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.DARK_BLUE));
        PropertiesCards paymentProperty = new PropertiesCards(PropertiesCardsType.DARK_BLUE);
        payer.getPropertyCards().add(paymentProperty);
        ActionCards debtCollector = new ActionCards(ActionCardType.DEBT_COLLECTOR);
        receiver.getHandCards().add(debtCollector);

        assertTrue(game.finishDebtCollector(debtCollector, payer));
        ArrayList<Card> selectedCards = new ArrayList<>();
        selectedCards.add(paymentProperty);

        assertTrue(game.finishCurrentPayment(selectedCards));

        assertTrue(game.isWin());
    }

    private void addCompleteBrownSet(Player player) {
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));
    }

    private void addCompleteLightGreenSet(Player player) {
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.LIGHT_GREEN));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.LIGHT_GREEN));
    }
}
