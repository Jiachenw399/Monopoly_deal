package logic;

import model.ActionCards;
import model.ActionCardType;
import model.Card;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;

public class CardPlayService {
    public boolean playCard(Player currentPlayer, Card card) {
        if (!canPlayCard(currentPlayer, card)) {
            return false;
        }

        boolean success = playCardByType(currentPlayer, card);

        if (success) {
            increaseUseCardTimes(currentPlayer);
        }

        return success;
    }

    private boolean canPlayCard(Player currentPlayer, Card card) {
        if (currentPlayer == null || card == null) {
            return false;
        }

        if (!currentPlayer.isOnTurn()) {
            return false;
        }

        if (currentPlayer.getUseCardTimes() >= 3) {
            return false;
        }

        return currentPlayer.getHandCards().contains(card);
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

        if (card instanceof ActionCards) {
            return false;
        }

        return false;
    }

    private void increaseUseCardTimes(Player player) {
        player.setUseCardTimes(player.getUseCardTimes() + 1);
    }
}