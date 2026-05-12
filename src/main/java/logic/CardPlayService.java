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

        if (card instanceof ActionCards actionCard) {
            if (!canPlayActionCardDirectly(actionCard)) {
                return false;
            }

            currentPlayer.putActionCard(actionCard);
            return true;
        }

        return false;
    }

    private boolean canPlayActionCardDirectly(ActionCards card) {
        ActionCardType type = card.getActionCardType();

        return switch (type) {
            case SLY_DEAL,
                 DEAL_BREAKER,
                 BIRTHDAY,
                 DEBT_COLLECTOR,
                 RENT_WITH_RED_AND_YELLOW,
                 RENT_WITH_ORANGE_AND_PINK,
                 RENT_WITH_BROWN_AND_LIGHT_BLUE,
                 RENT_WITH_BLACK_AND_LIGHT_GREEN,
                 RENT_WITH_DARK_BLUE_AND_DARK_GREEN,
                 RENT_WITH_MULTIPLE_COLOR,
                 DOUBLE_THE_RENT,
                 JUST_SAY_NO -> false;
            default -> true;
        };
    }

    private void increaseUseCardTimes(Player player) {
        player.setUseCardTimes(player.getUseCardTimes() + 1);
    }
}