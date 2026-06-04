package logic;

import model.Card;
import model.DrawPileAndDiscardPile;
import model.Player;

import java.util.ArrayList;

public class TurnManager {
    private final ArrayList<Player> players;

    private int currentPlayerIndex;
    private boolean isDiscard;

    // Creates a TurnManager instance.
    public TurnManager(ArrayList<Player> players, DrawPileAndDiscardPile drawCards) {
        this.players = players;
        this.currentPlayerIndex = 0;
        this.isDiscard = false;
    }

    // Starts first turn.
    public void startFirstTurn() {
        startTurn(getCurrentPlayer());
    }

    // Starts turn.
    public void startTurn(Player currentPlayer) {
        currentPlayer.setUseCardTimes(0);

        int drawNumber = getDrawNumberAtTurnStart(currentPlayer);
        currentPlayer.takeCard(drawNumber);
    }

    // Runs end turn.
    public void endTurn() {
        Player currentPlayer = getCurrentPlayer();

        if (currentPlayer.getHandCards().size() > 7) {
            isDiscard = true;
            return;
        }

        moveToNextPlayer();
    }

    // Forces advance turn for absent player.
    public void forceAdvanceTurnForAbsentPlayer() {
        Player currentPlayer = getCurrentPlayer();

        while (currentPlayer.getHandCards().size() > 7) {
            Card card = selectLowestValueCard(currentPlayer);
            currentPlayer.discardCardFromHand(card);
        }

        isDiscard = false;
        moveToNextPlayer();
    }

    // Discards this operation.
    public boolean discard(Card card) {
        Player currentPlayer = getCurrentPlayer();

        if (!canDiscard(currentPlayer, card)) {
            return false;
        }

        currentPlayer.discardCardFromHand(card);

        if (currentPlayer.getHandCards().size() <= 7) {
            isDiscard = false;
            moveToNextPlayer();
        }

        return true;
    }

    // Finds current player.
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public boolean isDiscard() {
        return isDiscard;
    }

    // Applies online state.
    public void applyOnlineState(int currentPlayerIndex, boolean isDiscard) {
        if (players.isEmpty()) {
            this.currentPlayerIndex = 0;
        } else if (currentPlayerIndex < 0) {
            this.currentPlayerIndex = 0;
        } else if (currentPlayerIndex >= players.size()) {
            this.currentPlayerIndex = players.size() - 1;
        } else {
            this.currentPlayerIndex = currentPlayerIndex;
        }

        this.isDiscard = isDiscard;
    }

    // Finds draw number at turn start.
    private int getDrawNumberAtTurnStart(Player player) {
        if (player.getHandCards().isEmpty()) {
            return 5;
        }

        return 2;
    }

    // Checks whether this can discard.
    private boolean canDiscard(Player currentPlayer, Card card) {
        return isDiscard
                && card != null
                && currentPlayer.getHandCards().contains(card);
    }

    // Finds a low-value card to remove during automatic cleanup.
    private Card selectLowestValueCard(Player currentPlayer) {
        Card lowestValue = currentPlayer.getHandCards().get(0);
        int lowest = lowestValue.getValue();
        for (Card card : currentPlayer.getHandCards()) {
            if (card.getValue() < lowest) {
                lowest = card.getValue();
                lowestValue = card;
            }
        }
        return lowestValue;
    }

    // Moves to next player.
    private void moveToNextPlayer() {
        Player currentPlayer = getCurrentPlayer();
        currentPlayer.setUseCardTimes(0);

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

        Player nextPlayer = getCurrentPlayer();
        startTurn(nextPlayer);
    }
}
