package logic;

import model.ActionCards;
import model.Card;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;

public class MoneyCardAndPropertyCardPlayService {
    // Plays card.
    public boolean playCard(Player currentPlayer, Card card) {
        if (!canPlayCard(currentPlayer, card)) {
            return false;
        }

        if (card instanceof MoneyCards) {
            return playMoneyCard(currentPlayer, card);
        }

        if (card instanceof PropertiesCards propertyCard) {
            return playPropertyCard(currentPlayer, propertyCard);
        }

        return false;
    }

    // Plays action card as money.
    public boolean playActionCardAsMoney(Player currentPlayer, ActionCards card) {
        if (!canPlayCard(currentPlayer, card)) {
            return false;
        }

        currentPlayer.putMoneyCard(card);
        increaseUseCardTimes(currentPlayer);
        return true;
    }

    // Checks whether this can play card.
    private boolean canPlayCard(Player currentPlayer, Card card) {
        if (currentPlayer == null || card == null) {
            return false;
        }

        if (currentPlayer.getUseCardTimes() >= 3) {
            return false;
        }

        return currentPlayer.getHandCards().contains(card);
    }

    // Plays money card.
    private boolean playMoneyCard(Player currentPlayer, Card card) {
        currentPlayer.putMoneyCard(card);
        increaseUseCardTimes(currentPlayer);
        return true;
    }

    // Plays property card.
    private boolean playPropertyCard(Player currentPlayer, PropertiesCards card) {
        currentPlayer.putPropertyCard(card);
        increaseUseCardTimes(currentPlayer);
        return true;
    }

    // Runs increase use card times.
    private void increaseUseCardTimes(Player player) {
        player.setUseCardTimes(player.getUseCardTimes() + 1);
    }
}
