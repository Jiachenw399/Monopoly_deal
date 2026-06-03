package logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import model.PropertyColor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class GameTestEdgeCases {
    private static class CountingObserver implements GameObserver {
        private int notificationCount;

        @Override
        public void onGameStateChanged() {
            notificationCount++;
        }
    }

    @Test
    public void testGameWithPlayerNamesAssignsNamesToPlayers() {
        ArrayList<String> names = new ArrayList<>();
        names.add("Alice");
        names.add("Bob");
        names.add("Carol");
        Game game = new Game(3);
        game.startGame(3, names);

        assertEquals("Alice", game.getPlayers().get(0).getName());
        assertEquals("Bob", game.getPlayers().get(1).getName());
        assertEquals("Carol", game.getPlayers().get(2).getName());
    }

    @Test
    public void testGameWithNullPlayerNamesTreatsAsNoName() {
        ArrayList<String> names = new ArrayList<>();
        names.add(null);
        names.add("Bob");
        Game game = new Game(2);
        game.startGame(2, names);

        assertNull(game.getPlayers().get(0).getName());
        assertEquals("Bob", game.getPlayers().get(1).getName());
    }

    @Test
    public void testPlayCardMovesMoneyCardToBank() {
        Game game = new Game(2);
        game.startGame();
        Player player = game.getCurrentPlayer();
        player.getHandCards().clear();
        MoneyCards money = new MoneyCards(5);
        player.getHandCards().add(money);

        assertTrue(game.playCard(money));

        assertTrue(player.getBankCards().contains(money));
        assertFalse(player.getHandCards().contains(money));
        assertEquals(1, player.getUseCardTimes());
    }

    @Test
    public void testPlayCardMovesPropertyCardToProperties() {
        Game game = new Game(2);
        game.startGame();
        Player player = game.getCurrentPlayer();
        player.getHandCards().clear();
        PropertiesCards property = new PropertiesCards(PropertiesCardsType.BROWN);
        player.getHandCards().add(property);

        assertTrue(game.playCard(property));

        assertTrue(player.getPropertyCards().contains(property));
        assertFalse(player.getHandCards().contains(property));
    }

    @Test
    public void testPlayActionCardAsMoneyMovesCardToBank() {
        Game game = new Game(2);
        game.startGame();
        Player player = game.getCurrentPlayer();
        player.getHandCards().clear();
        ActionCards action = new ActionCards(ActionCardType.SLY_DEAL);
        player.getHandCards().add(action);

        assertTrue(game.playActionCardAsMoney(action));

        assertTrue(player.getBankCards().contains(action));
        assertFalse(player.getHandCards().contains(action));
        assertEquals(1, player.getUseCardTimes());
    }

    @Test
    public void testDiscardCardMovesCardToDiscardPile() {
        Game game = new Game(2);
        game.startGame();
        Player player = game.getCurrentPlayer();
        player.getHandCards().clear();
        Card card = new MoneyCards(1);
        player.getHandCards().add(card);

        assertTrue(game.discard(card));

        assertFalse(player.getHandCards().contains(card));
        assertTrue(game.getDrawCards().getDiscardPile().contains(card));
    }

    @Test
    public void testGuiEndTurnAdvancesToNextPlayer() {
        Game game = new Game(3);
        game.startGame();
        int firstPlayerIndex = game.getCurrentPlayerIndex();

        game.guiEndTurn();

        assertEquals((firstPlayerIndex + 1) % 3, game.getCurrentPlayerIndex());
    }

    @Test
    public void testGuiEndTurnChecksWinCondition() {
        Game game = new Game(2);
        game.startGame();
        Player player = game.getCurrentPlayer();
        addThreeCompleteSets(player);

        game.guiEndTurn();

        assertTrue(game.isWin());
    }

    @Test
    public void testForceAdvanceTurnForAbsentPlayer() {
        Game game = new Game(2);
        game.startGame();
        Player player = game.getCurrentPlayer();
        player.getHandCards().clear();
        for (int i = 0; i < 10; i++) {
            player.getHandCards().add(new MoneyCards(1));
        }
        int initialIndex = game.getCurrentPlayerIndex();

        game.forceAdvanceTurnForAbsentPlayer();

        assertEquals((initialIndex + 1) % 2, game.getCurrentPlayerIndex());
        assertFalse(game.isDiscard());
    }

    @Test
    public void testForceAdvanceTurnChecksWin() {
        Game game = new Game(2);
        game.startGame();
        Player player = game.getCurrentPlayer();
        addThreeCompleteSets(player);

        game.forceAdvanceTurnForAbsentPlayer();

        assertTrue(game.isWin());
    }

    @Test
    public void testRemoveObserverStopsNotifications() {
        Game game = new Game();
        CountingObserver observer = new CountingObserver();
        game.addObserver(observer);
        int countAfterAdd = observer.notificationCount;

        game.removeObserver(observer);
        game.startGame();

        assertEquals(countAfterAdd, observer.notificationCount);
    }

    @Test
    public void testApplyOnlineStateReplacesPlayersList() {
        Game game = new Game(2);
        game.startGame();
        DrawPileAndDiscardPile pile = new DrawPileAndDiscardPile();
        Player newPlayer = new Player(pile);
        newPlayer.getHandCards().add(new MoneyCards(10));
        ArrayList<Player> newPlayers = new ArrayList<>();
        newPlayers.add(newPlayer);

        game.applyOnlineState(newPlayers, 0, false, null, false);

        assertEquals(1, game.getPlayers().size());
        assertEquals(10, game.getPlayers().get(0).getHandCards().get(0).getValue());
    }

    @Test
    public void testApplyOnlineStateWithNullPlayersClearsList() {
        Game game = new Game(2);
        game.startGame();

        game.applyOnlineState(null, 0, false, null, false);

        assertTrue(game.getPlayers().isEmpty());
    }

    @Test
    public void testSetWinUpdatesStateAndNotifies() {
        Game game = new Game();
        CountingObserver observer = new CountingObserver();
        game.addObserver(observer);
        int countBefore = observer.notificationCount;

        game.setWin(true);

        assertTrue(game.isWin());
        assertEquals(countBefore + 1, observer.notificationCount);
    }

    @Test
    public void testHasDoubleTheRentCardReturnsTrue() {
        Game game = new Game(2);
        game.startGame();
        Player player = game.getCurrentPlayer();
        player.getHandCards().clear();
        ActionCards doubleRent = new ActionCards(ActionCardType.DOUBLE_THE_RENT);
        player.getHandCards().add(doubleRent);

        assertTrue(game.hasDoubleTheRentCard(player));
    }

    @Test
    public void testHasDoubleTheRentCardReturnsFalse() {
        Game game = new Game(2);
        game.startGame();
        Player player = game.getCurrentPlayer();
        player.getHandCards().clear();
        player.getHandCards().add(new MoneyCards(5));

        assertFalse(game.hasDoubleTheRentCard(player));
    }

    @Test
    public void testHasDoubleTheRentCardReturnsFalseForNull() {
        Game game = new Game(2);
        game.startGame();

        assertFalse(game.hasDoubleTheRentCard(null));
    }

    @Test
    public void testIsDiscardReturnsFalseAtTurnStart() {
        Game game = new Game(2);
        game.startGame();

        assertFalse(game.isDiscard());
    }

    @Test
    public void testIsDiscardReturnsTrueAfterTooManyCards() {
        Game game = new Game(2);
        game.startGame();
        Player player = game.getCurrentPlayer();
        for (int i = 0; i < 8; i++) {
            player.getHandCards().add(new MoneyCards(1));
        }

        game.guiEndTurn();

        assertTrue(game.isDiscard());
    }

    @Test
    public void testGetTotalAssetsValue() {
        Game game = new Game(2);
        game.startGame();
        Player player = game.getCurrentPlayer();
        player.getBankCards().add(new MoneyCards(5));
        player.getBankCards().add(new MoneyCards(3));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));

        assertEquals(9, game.getTotalAssetsValue(player));
    }

    @Test
    public void testGetCardsValue() {
        Game game = new Game(2);
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(new MoneyCards(5));
        cards.add(new MoneyCards(3));
        cards.add(new PropertiesCards(PropertiesCardsType.BROWN));

        assertEquals(9, game.getCardsValue(cards));
    }

    @Test
    public void testGetPaymentCardsValue() {
        Game game = new Game(2);
        Player payer = game.getCurrentPlayer();
        payer.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(new PropertiesCards(PropertiesCardsType.BROWN));

        assertEquals(1, game.getPaymentCardsValue(payer, cards));
    }

    @Test
    public void testFinishBirthdayCreatesPaymentRequests() {
        Game game = new Game(3);
        game.startGame();
        Player player = game.getCurrentPlayer();
        player.getHandCards().clear();
        ActionCards birthday = new ActionCards(ActionCardType.BIRTHDAY);
        player.getHandCards().add(birthday);

        assertTrue(game.finishBirthday(birthday));

        assertTrue(game.isPaymentSelecting());
        assertNotNull(game.getCurrentPaymentRequest());
    }

    @Test
    public void testSetPropertyColorFailsForNonWildCard() {
        Game game = new Game(2);
        game.startGame();
        Player player = game.getCurrentPlayer();
        PropertiesCards brown = new PropertiesCards(PropertiesCardsType.BROWN);
        player.getPropertyCards().add(brown);

        assertFalse(game.setPropertyColor(player, brown, PropertyColor.DARK_BLUE));
    }

    @Test
    public void testSetPropertyColorFailsForNullColor() {
        Game game = new Game(2);
        game.startGame();
        Player player = game.getCurrentPlayer();
        PropertiesCards wild = new PropertiesCards(PropertiesCardsType.WILD_ALL);
        player.getPropertyCards().add(wild);

        assertFalse(game.setPropertyColor(player, wild, null));
    }

    @Test
    public void testSetPropertyColorFailsForInvalidColor() {
        Game game = new Game(2);
        game.startGame();
        Player player = game.getCurrentPlayer();
        PropertiesCards wild = new PropertiesCards(PropertiesCardsType.WILD_RED_YELLOW);
        player.getPropertyCards().add(wild);

        assertFalse(game.setPropertyColor(player, wild, PropertyColor.DARK_BLUE));
    }

    @Test
    public void testSetPropertyColorFailsForNullPlayer() {
        Game game = new Game(2);
        game.startGame();
        PropertiesCards wild = new PropertiesCards(PropertiesCardsType.WILD_ALL);

        assertFalse(game.setPropertyColor(null, wild, PropertyColor.DARK_BLUE));
    }

    private void addThreeCompleteSets(Player player) {
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.BROWN));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.DARK_BLUE));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.DARK_BLUE));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.LIGHT_GREEN));
        player.getPropertyCards().add(new PropertiesCards(PropertiesCardsType.LIGHT_GREEN));
    }
}
