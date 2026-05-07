package logic;

import model.ActionCards;
import model.Card;
import model.DrawPileAndDiscardPile;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;

public class Game {
    public static final double SCREEN_WIDTH = 1035;
    public static final double SCREEN_HEIGHT = 625;

    private final ArrayList<Player> players;
    private DrawPileAndDiscardPile drawCards;
    private boolean isWin;
    private int currentPlayerIndex;
    private boolean isDiscard;

    public Game() {
        players = new ArrayList<>();
        drawCards = new DrawPileAndDiscardPile();
        isWin = false;
        currentPlayerIndex = 0;
        isDiscard = false;
        addPlayer();
    }

    public void startGame() {
        resetGame();
        addPlayer();

        Player currentPlayer = getCurrentPlayer();
        currentPlayer.setOnTurn(true);
        currentPlayer.setUseCardTimes(0);

        System.out.println("Monopoly Deal GUI game started.");
    }

    private void resetGame() {
        players.clear();
        drawCards = new DrawPileAndDiscardPile();
        currentPlayerIndex = 0;
        isWin = false;
        isDiscard = false;
    }

    public void startTurn(Player currentPlayer) {
        currentPlayer.setOnTurn(true);
        currentPlayer.setUseCardTimes(0);

        int drawNumber = getDrawNumberAtTurnStart(currentPlayer);
        currentPlayer.takeCard(drawNumber);

        System.out.println("Player " + (currentPlayerIndex + 1) + " starts turn.");
        System.out.println("Draw cards: " + drawNumber);
    }

    private int getDrawNumberAtTurnStart(Player player) {
        if (player.getHandCards().isEmpty()) {
            return 5;
        }

        return 2;
    }

    public void guiEndTurn() {
        Player currentPlayer = getCurrentPlayer();

        if (checkCurrentPlayerWin()) {
            return;
        }

        if (shouldEnterDiscardPhase(currentPlayer)) {
            isDiscard = true;
            return;
        }

        moveToNextPlayer(currentPlayer);
    }

    private boolean checkCurrentPlayerWin() {
        if (getCurrentPlayer().checkIfWin()) {
            isWin = true;
            return true;
        }

        return false;
    }

    private boolean shouldEnterDiscardPhase(Player player) {
        return player.getHandCards().size() > 7;
    }

    private void moveToNextPlayer(Player currentPlayer) {
        currentPlayer.setOnTurn(false);
        currentPlayer.setUseCardTimes(0);

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

        Player nextPlayer = getCurrentPlayer();
        startTurn(nextPlayer);
    }

    public void discard(Card card) {
        Player currentPlayer = getCurrentPlayer();

        if (!canDiscard(currentPlayer, card)) {
            return;
        }

        currentPlayer.getHandCards().remove(card);
        drawCards.getDiscardPile().add(card);

        if (currentPlayer.getHandCards().size() <= 7) {
            isDiscard = false;
        }
    }

    private boolean canDiscard(Player currentPlayer, Card card) {
        if (!isDiscard) {
            System.out.println("Not in discard phase.");
            return false;
        }

        if (!currentPlayer.getHandCards().contains(card)) {
            System.out.println("This card is not in current player's hand.");
            return false;
        }

        return true;
    }

    public void playCard(Card card) {
        Player currentPlayer = getCurrentPlayer();

        if (!canPlayCard(currentPlayer, card)) {
            return;
        }

        boolean success = playCardByType(currentPlayer, card);

        if (success) {
            increaseUseCardTimes(currentPlayer);
        }

        checkCurrentPlayerWin();
    }

    private boolean canPlayCard(Player currentPlayer, Card card) {
        if (!currentPlayer.isOnTurn()) {
            System.out.println("It is not this player's turn.");
            return false;
        }

        if (currentPlayer.getUseCardTimes() >= 3) {
            System.out.println("This player has already played 3 cards this turn.");
            return false;
        }

        if (!currentPlayer.getHandCards().contains(card)) {
            System.out.println("This card is not in current player's hand.");
            return false;
        }

        return true;
    }

    private boolean playCardByType(Player currentPlayer, Card card) {
        if (card instanceof MoneyCards) {
            currentPlayer.putMoneyCard(card);
            return true;
        }

        if (card instanceof PropertiesCards propertyCard) {
            currentPlayer.putPropertyCard(propertyCard);
            return true;
        }

        if (card instanceof ActionCards actionCard) {
            currentPlayer.putActionCard(actionCard);
            return true;
        }

        return false;
    }

    public void finishSlyDeal(ActionCards slyDealCard, Player targetPlayer, PropertiesCards stolenCard) {
        Player currentPlayer = getCurrentPlayer();

        if (!canFinishSlyDeal(currentPlayer, slyDealCard, targetPlayer, stolenCard)) {
            return;
        }

        moveActionCardToDiscard(currentPlayer, slyDealCard);
        targetPlayer.getPropertyCards().remove(stolenCard);
        currentPlayer.getPropertyCards().add(stolenCard);

        increaseUseCardTimes(currentPlayer);
        checkCurrentPlayerWin();
    }

    private boolean canFinishSlyDeal(Player currentPlayer, ActionCards card, Player targetPlayer, PropertiesCards stolenCard) {
        if (card == null || targetPlayer == null || stolenCard == null) {
            return false;
        }

        if (!currentPlayer.getHandCards().contains(card)) {
            return false;
        }

        return targetPlayer.getPropertyCards().contains(stolenCard);
    }

    public void finishDebtCollector(ActionCards debtCollectorCard, Player targetPlayer) {
        Player currentPlayer = getCurrentPlayer();

        if (!canFinishDebtCollector(currentPlayer, debtCollectorCard, targetPlayer)) {
            return;
        }

        moveActionCardToDiscard(currentPlayer, debtCollectorCard);
        currentPlayer.takeMoney(5, targetPlayer);

        increaseUseCardTimes(currentPlayer);
        checkCurrentPlayerWin();
    }

    private boolean canFinishDebtCollector(Player currentPlayer, ActionCards card, Player targetPlayer) {
        if (card == null || targetPlayer == null) {
            return false;
        }

        if (targetPlayer == currentPlayer) {
            return false;
        }

        return currentPlayer.getHandCards().contains(card);
    }

    public void finishTwoColorRent(ActionCards rentCard, PropertyColor selectedColor) {
        Player currentPlayer = getCurrentPlayer();

        if (rentCard == null || selectedColor == null) {
            return;
        }

        currentPlayer.playSelectedTwoColorRent(rentCard, selectedColor);
        checkCurrentPlayerWin();
    }

    private void moveActionCardToDiscard(Player currentPlayer, ActionCards card) {
        currentPlayer.getHandCards().remove(card);
        drawCards.getDiscardPile().add(card);
    }

    private void increaseUseCardTimes(Player player) {
        player.setUseCardTimes(player.getUseCardTimes() + 1);
    }

    private void addPlayer() {
        for (int i = 0; i < 4; i++) {
            players.add(new Player(drawCards));
        }

        addEnemiesForPlayers();
        dealInitialCards();
    }

    private void addEnemiesForPlayers() {
        for (Player player : players) {
            player.getEnemy().clear();

            for (Player otherPlayer : players) {
                if (player != otherPlayer) {
                    player.getEnemy().add(otherPlayer);
                }
            }
        }
    }

    private void dealInitialCards() {
        for (Player player : players) {
            player.takeCard(5);
        }
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public DrawPileAndDiscardPile getDrawCards() {
        return drawCards;
    }

    public boolean isWin() {
        return isWin;
    }

    public void setWin(boolean win) {
        isWin = win;
    }

    public boolean isDiscard() {
        return isDiscard;
    }
}