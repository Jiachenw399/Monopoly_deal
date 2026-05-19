package logic;

import model.Card;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;

public class MoneyCardAndPropertyCardPlayService {
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

    private boolean canPlayCard(Player currentPlayer, Card card) {
        if (currentPlayer == null || card == null) {
            return false;
        }

        if (currentPlayer.getUseCardTimes() >= 3) {
            return false;
        }

        return currentPlayer.getHandCards().contains(card);
    }

    private boolean playMoneyCard(Player currentPlayer, Card card) {
        currentPlayer.putMoneyCard(card);
        increaseUseCardTimes(currentPlayer);
        return true;
    }

    private boolean playPropertyCard(Player currentPlayer, PropertiesCards card) {
        currentPlayer.putPropertyCard(card);
        increaseUseCardTimes(currentPlayer);
        return true;
    }

    private void increaseUseCardTimes(Player player) {
        player.setUseCardTimes(player.getUseCardTimes() + 1);
    }
}