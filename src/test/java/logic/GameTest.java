package logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import model.ActionCardType;
import model.ActionCards;
import model.Card;
import model.MoneyCards;
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

    private static class ImmediateAI implements AIPlayer {
        private final AtomicInteger turnCount = new AtomicInteger();

        @Override
        public void onTurnStart(GameFacade game, Player player, Runnable onDone) {
            turnCount.incrementAndGet();
            onDone.run();
        }

        @Override
        public void onPaymentRequested(GameFacade game,
                                       Player player,
                                       Game.PaymentRequest request,
                                       Runnable onPaymentDone) {
            onPaymentDone.run();
        }

        @Override
        public void onDiscardPhaseStarted(GameFacade game, Player player, Runnable onDone) {
            onDone.run();
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
    public void testGameAllowsFivePlayers() {
        Game game = new Game(5);

        assertEquals(5, game.getPlayers().size());
    }

    @Test
    public void testGameClampsPlayerCountToRulesRange() {
        assertEquals(1, new Game(1).getPlayers().size());
        assertEquals(5, new Game(6).getPlayers().size());
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
        assertEquals(0, game.getWinnerIndex());
    }

    @Test
    public void testAIEndCallbackAutomaticallyAdvancesTurnBackToHuman() {
        Game game = new Game(2);
        game.startGame();
        Player aiPlayer = game.getPlayers().get(1);
        ImmediateAI ai = new ImmediateAI();
        game.registerAI(aiPlayer, ai);

        game.guiEndTurn();

        assertEquals(1, ai.turnCount.get());
        assertEquals(0, game.getCurrentPlayerIndex());
    }

    @Test
    public void testRestartGameRestoresRegisteredAIOpponents() {
        Game game = new Game(2);
        game.startGame();
        game.registerAI(game.getPlayers().get(1), new ImmediateAI());

        game.restartGame();

        assertTrue(game.getPlayers().get(1).isAI());
        assertTrue(game.isAI(game.getPlayers().get(1)));
    }

    @Test
    public void testAIFlaggedPlayerStillRunsIfRegistrationWasMissing() {
        Game game = new Game(2);
        game.startGame();
        game.getPlayers().get(1).setAI(true);

        game.guiEndTurn();
        waitUntilCurrentPlayerIndex(game, 0);

        assertEquals(0, game.getCurrentPlayerIndex());
        assertTrue(game.isAI(game.getPlayers().get(1)));
    }

    @Test
    public void testAIDiscardsExcessCardsAndAdvancesTurn() {
        Game game = new Game(2);
        game.startGame();
        Player aiPlayer = game.getPlayers().get(1);
        aiPlayer.getHandCards().clear();
        for (int i = 0; i < 10; i++) {
            aiPlayer.getHandCards().add(new MoneyCards(1));
        }
        game.registerAI(aiPlayer, new SimpleAIPlayer());

        game.guiEndTurn();
        waitUntilCurrentPlayerIndex(game, 0);

        assertEquals(0, game.getCurrentPlayerIndex());
        assertFalse(game.isDiscard());
        assertTrue(aiPlayer.getHandCards().size() <= 7);
    }

    @Test
    public void testGameClearsAIDiscardPhaseEvenIfAIOnlyEndsTurn() {
        Game game = new Game(2);
        game.startGame();
        Player aiPlayer = game.getPlayers().get(1);
        aiPlayer.getHandCards().clear();
        for (int i = 0; i < 10; i++) {
            aiPlayer.getHandCards().add(new MoneyCards(1));
        }
        game.registerAI(aiPlayer, new ImmediateAI());

        game.guiEndTurn();

        assertEquals(0, game.getCurrentPlayerIndex());
        assertFalse(game.isDiscard());
        assertTrue(aiPlayer.getHandCards().size() <= 7);
    }

    @Test
    public void testAIPaymentIncludesBuildingsSoPaymentDoesNotStall() {
        Game game = new Game(2);
        game.startGame();
        Player human = game.getPlayers().get(0);
        Player aiPlayer = game.getPlayers().get(1);
        game.registerAI(aiPlayer, new SimpleAIPlayer());

        PropertiesCards propertyWithBuildings = new PropertiesCards(PropertiesCardsType.BROWN);
        propertyWithBuildings.setHasHouse(true);
        propertyWithBuildings.setHasHotel(true);
        aiPlayer.getPropertyCards().add(propertyWithBuildings);
        aiPlayer.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));
        ActionCards debtCollector = new ActionCards(ActionCardType.DEBT_COLLECTOR);
        human.getHandCards().add(debtCollector);

        assertTrue(game.finishDebtCollector(debtCollector, aiPlayer));
        waitUntilPaymentSelectionEnds(game);

        assertFalse(game.isPaymentSelecting());
        assertFalse(propertyWithBuildings.hasHouse());
        assertFalse(propertyWithBuildings.hasHotel());
        assertTrue(PlayerInfoHelper.getBankTotal(human) >= 7);
    }

    private void waitUntilCurrentPlayerIndex(Game game, int expectedIndex) {
        long deadline = System.currentTimeMillis() + 2_000;
        while (System.currentTimeMillis() < deadline && game.getCurrentPlayerIndex() != expectedIndex) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void waitUntilPaymentSelectionEnds(Game game) {
        long deadline = System.currentTimeMillis() + 2_000;
        while (System.currentTimeMillis() < deadline && game.isPaymentSelecting()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
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
