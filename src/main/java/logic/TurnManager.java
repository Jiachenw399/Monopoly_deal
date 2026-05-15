package logic;

import model.Card;
import model.DrawPileAndDiscardPile;
import model.Player;

import java.util.ArrayList;

public class TurnManager {
    private final ArrayList<Player> players;
    private final DrawPileAndDiscardPile drawCards;

    private int currentPlayerIndex;
    private boolean isDiscard;

    public TurnManager(ArrayList<Player> players, DrawPileAndDiscardPile drawCards) {
        this.players = players;
        this.drawCards = drawCards;
        this.currentPlayerIndex = 0;
        this.isDiscard = false;
    }

    public void startFirstTurn() {
        startTurn(getCurrentPlayer());
    }

    public void startTurn(Player currentPlayer) {
        currentPlayer.setOnTurn(true);
        currentPlayer.setUseCardTimes(0);

        int drawNumber = getDrawNumberAtTurnStart(currentPlayer);
        currentPlayer.takeCard(drawNumber);
    }

    public void endTurn() {
        Player currentPlayer = getCurrentPlayer();

        if (currentPlayer.getHandCards().size() > 7) {
            isDiscard = true;
            return;
        }

        moveToNextPlayer();
    }

    public boolean discard(Card card) {
        Player currentPlayer = getCurrentPlayer();

        if (!canDiscard(currentPlayer, card)) {
            return false;
        }

        currentPlayer.getHandCards().remove(card);
        drawCards.getDiscardPile().add(card);

        if (currentPlayer.getHandCards().size() <= 7) {
            isDiscard = false;
            moveToNextPlayer();
        }

        return true;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public boolean isDiscard() {
        return isDiscard;
    }

    private int getDrawNumberAtTurnStart(Player player) {
        if (player.getHandCards().isEmpty()) {
            return 5;
        }

        return 2;
    }

    private boolean canDiscard(Player currentPlayer, Card card) {
        return isDiscard && currentPlayer.getHandCards().contains(card);
    }

    private void moveToNextPlayer() {
        Player currentPlayer = getCurrentPlayer();

        currentPlayer.setOnTurn(false);
        currentPlayer.setUseCardTimes(0);

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

        Player nextPlayer = getCurrentPlayer();
        startTurn(nextPlayer);
    }
}
