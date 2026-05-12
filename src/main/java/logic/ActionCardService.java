package logic;

import model.ActionCards;
import model.ActionCardType;
import model.Card;
import model.DrawPileAndDiscardPile;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;

public class ActionCardService {
    private final ArrayList<Player> players;
    private final DrawPileAndDiscardPile drawCards;
    private final PaymentManager paymentManager;
    private final RentCalculator rentCalculator;

    public ActionCardService(ArrayList<Player> players,
                             DrawPileAndDiscardPile drawCards,
                             PaymentManager paymentManager,
                             RentCalculator rentCalculator) {
        this.players = players;
        this.drawCards = drawCards;
        this.paymentManager = paymentManager;
        this.rentCalculator = rentCalculator;
    }

    public boolean finishBirthday(Player currentPlayer, ActionCards birthdayCard) {
        if (!canFinishActionCard(currentPlayer, birthdayCard, ActionCardType.BIRTHDAY)) {
            return false;
        }

        moveActionCardToDiscard(currentPlayer, birthdayCard);

        for (Player player : players) {
            if (player != currentPlayer) {
                paymentManager.addPaymentRequest(currentPlayer, player, 2);
            }
        }

        paymentManager.startNextPaymentRequest();
        increaseUseCardTimes(currentPlayer);
        return true;
    }

    public boolean finishSlyDeal(Player currentPlayer,
                                 ActionCards slyDealCard,
                                 Player targetPlayer,
                                 PropertiesCards stolenCard) {
        if (!canFinishSlyDeal(currentPlayer, slyDealCard, targetPlayer, stolenCard)) {
            return false;
        }

        moveActionCardToDiscard(currentPlayer, slyDealCard);
        targetPlayer.getPropertyCards().remove(stolenCard);
        currentPlayer.getPropertyCards().add(stolenCard);

        increaseUseCardTimes(currentPlayer);
        return true;
    }

    public boolean finishDealBreaker(Player currentPlayer,
                                     ActionCards dealBreakerCard,
                                     Player targetPlayer,
                                     ArrayList<PropertiesCards> selectedSet) {
        if (!canFinishDealBreaker(currentPlayer, dealBreakerCard, targetPlayer, selectedSet)) {
            return false;
        }

        moveActionCardToDiscard(currentPlayer, dealBreakerCard);
        targetPlayer.getPropertyCards().removeAll(selectedSet);
        currentPlayer.getPropertyCards().addAll(selectedSet);

        increaseUseCardTimes(currentPlayer);
        return true;
    }

    public boolean finishDebtCollector(Player currentPlayer,
                                       ActionCards debtCollectorCard,
                                       Player targetPlayer) {
        if (!canFinishDebtCollector(currentPlayer, debtCollectorCard, targetPlayer)) {
            return false;
        }

        moveActionCardToDiscard(currentPlayer, debtCollectorCard);
        paymentManager.addPaymentRequest(currentPlayer, targetPlayer, 5);
        paymentManager.startNextPaymentRequest();

        increaseUseCardTimes(currentPlayer);
        return true;
    }

    public boolean finishTwoColorRent(Player currentPlayer,
                                      ActionCards rentCard,
                                      PropertyColor selectedColor,
                                      boolean useDoubleRent) {
        if (!canFinishTwoColorRent(currentPlayer, rentCard, selectedColor)) {
            return false;
        }

        boolean canUseDoubleRent = canUseDoubleRent(currentPlayer, useDoubleRent);

        if (canUseDoubleRent && currentPlayer.getUseCardTimes() > 1) {
            return false;
        }

        int rent = getFinalRent(currentPlayer, selectedColor, canUseDoubleRent);

        moveActionCardToDiscard(currentPlayer, rentCard);
        discardDoubleTheRentIfUsed(currentPlayer, canUseDoubleRent);

        for (Player player : players) {
            if (player != currentPlayer) {
                paymentManager.addPaymentRequest(currentPlayer, player, rent);
            }
        }

        paymentManager.startNextPaymentRequest();
        increaseRentUseTimes(currentPlayer, canUseDoubleRent);
        return true;
    }

    public boolean finishMultipleColorRent(Player currentPlayer,
                                           ActionCards rentCard,
                                           Player targetPlayer,
                                           PropertyColor selectedColor,
                                           boolean useDoubleRent) {
        if (!canFinishMultipleColorRent(currentPlayer, rentCard, targetPlayer, selectedColor)) {
            return false;
        }

        boolean canUseDoubleRent = canUseDoubleRent(currentPlayer, useDoubleRent);

        if (canUseDoubleRent && currentPlayer.getUseCardTimes() > 1) {
            return false;
        }

        int rent = getFinalRent(currentPlayer, selectedColor, canUseDoubleRent);

        moveActionCardToDiscard(currentPlayer, rentCard);
        discardDoubleTheRentIfUsed(currentPlayer, canUseDoubleRent);

        paymentManager.addPaymentRequest(currentPlayer, targetPlayer, rent);
        paymentManager.startNextPaymentRequest();

        increaseRentUseTimes(currentPlayer, canUseDoubleRent);
        return true;
    }

    public boolean hasDoubleTheRentCard(Player player) {
        for (Card card : player.getHandCards()) {
            if (card instanceof ActionCards actionCard
                    && actionCard.getActionCardType() == ActionCardType.DOUBLE_THE_RENT) {
                return true;
            }
        }

        return false;
    }

    private boolean canFinishSlyDeal(Player currentPlayer,
                                     ActionCards card,
                                     Player targetPlayer,
                                     PropertiesCards stolenCard) {
        if (!canFinishActionCard(currentPlayer, card, ActionCardType.SLY_DEAL)) {
            return false;
        }

        if (targetPlayer == null || stolenCard == null || targetPlayer == currentPlayer) {
            return false;
        }

        return targetPlayer.getPropertyCards().contains(stolenCard)
                && targetPlayer.canLosePropertyToSlyDeal(stolenCard);
    }

    private boolean canFinishDealBreaker(Player currentPlayer,
                                         ActionCards card,
                                         Player targetPlayer,
                                         ArrayList<PropertiesCards> selectedSet) {
        if (!canFinishActionCard(currentPlayer, card, ActionCardType.DEAL_BREAKER)) {
            return false;
        }

        if (targetPlayer == null || selectedSet == null || selectedSet.isEmpty() || targetPlayer == currentPlayer) {
            return false;
        }

        for (PropertiesCards propertyCard : selectedSet) {
            if (!targetPlayer.getPropertyCards().contains(propertyCard)) {
                return false;
            }
        }

        PropertyColor color = selectedSet.get(0).getCurrentColor();

        if (color == null) {
            return false;
        }

        return selectedSet.size() >= color.getAmountToCompleteSet();
    }

    private boolean canFinishDebtCollector(Player currentPlayer, ActionCards card, Player targetPlayer) {
        if (!canFinishActionCard(currentPlayer, card, ActionCardType.DEBT_COLLECTOR)) {
            return false;
        }

        return targetPlayer != null && targetPlayer != currentPlayer;
    }

    private boolean canFinishTwoColorRent(Player currentPlayer,
                                          ActionCards card,
                                          PropertyColor selectedColor) {
        if (card == null || selectedColor == null) {
            return false;
        }

        if (!canPlayCard(currentPlayer, card)) {
            return false;
        }

        if (!currentPlayer.canUseRentColor(selectedColor)) {
            return false;
        }

        return switch (card.getActionCardType()) {
            case RENT_WITH_RED_AND_YELLOW ->
                    selectedColor == PropertyColor.RED || selectedColor == PropertyColor.YELLOW;
            case RENT_WITH_ORANGE_AND_PINK ->
                    selectedColor == PropertyColor.ORANGE || selectedColor == PropertyColor.PINK;
            case RENT_WITH_BROWN_AND_LIGHT_BLUE ->
                    selectedColor == PropertyColor.BROWN || selectedColor == PropertyColor.LIGHT_BLUE;
            case RENT_WITH_BLACK_AND_LIGHT_GREEN ->
                    selectedColor == PropertyColor.BLACK || selectedColor == PropertyColor.LIGHT_GREEN;
            case RENT_WITH_DARK_BLUE_AND_DARK_GREEN ->
                    selectedColor == PropertyColor.DARK_BLUE || selectedColor == PropertyColor.DARK_GREEN;
            default -> false;
        };
    }

    private boolean canFinishMultipleColorRent(Player currentPlayer,
                                               ActionCards card,
                                               Player targetPlayer,
                                               PropertyColor selectedColor) {
        if (!canFinishActionCard(currentPlayer, card, ActionCardType.RENT_WITH_MULTIPLE_COLOR)) {
            return false;
        }

        if (targetPlayer == null || targetPlayer == currentPlayer || selectedColor == null) {
            return false;
        }

        return currentPlayer.canUseRentColor(selectedColor);
    }

    private boolean canFinishActionCard(Player currentPlayer, ActionCards card, ActionCardType expectedType) {
        if (card == null || card.getActionCardType() != expectedType) {
            return false;
        }

        return canPlayCard(currentPlayer, card);
    }

    private boolean canPlayCard(Player currentPlayer, Card card) {
        if (!currentPlayer.isOnTurn()) {
            return false;
        }

        if (currentPlayer.getUseCardTimes() >= 3) {
            return false;
        }

        return currentPlayer.getHandCards().contains(card);
    }

    private boolean canUseDoubleRent(Player player, boolean useDoubleRent) {
        return useDoubleRent && hasDoubleTheRentCard(player);
    }

    private int getFinalRent(Player player, PropertyColor selectedColor, boolean canUseDoubleRent) {
        int rent = rentCalculator.calculateRent(player, selectedColor);

        if (canUseDoubleRent) {
            rent *= 2;
        }

        return rent;
    }

    private void increaseRentUseTimes(Player player, boolean canUseDoubleRent) {
        increaseUseCardTimes(player);

        if (canUseDoubleRent) {
            increaseUseCardTimes(player);
        }
    }

    private void discardDoubleTheRentIfUsed(Player player, boolean useDoubleRent) {
        if (!useDoubleRent) {
            return;
        }

        for (Card card : new ArrayList<>(player.getHandCards())) {
            if (card instanceof ActionCards actionCard
                    && actionCard.getActionCardType() == ActionCardType.DOUBLE_THE_RENT) {
                player.getHandCards().remove(actionCard);
                drawCards.getDiscardPile().add(actionCard);
                return;
            }
        }
    }

    private void moveActionCardToDiscard(Player currentPlayer, ActionCards card) {
        currentPlayer.getHandCards().remove(card);
        drawCards.getDiscardPile().add(card);
    }

    private void increaseUseCardTimes(Player player) {
        player.setUseCardTimes(player.getUseCardTimes() + 1);
    }

    public boolean finishHouse(Player currentPlayer, ActionCards houseCard, PropertyColor selectedColor) {
        if (!canFinishHouse(currentPlayer, houseCard, selectedColor)) {
            return false;
        }

        moveActionCardToDiscard(currentPlayer, houseCard);

        PropertiesCards property = findFirstPropertyByColor(currentPlayer, selectedColor);
        property.setHasHouse(true);

        increaseUseCardTimes(currentPlayer);
        return true;
    }

    public boolean finishHotel(Player currentPlayer, ActionCards hotelCard, PropertyColor selectedColor) {
        if (!canFinishHotel(currentPlayer, hotelCard, selectedColor)) {
            return false;
        }

        moveActionCardToDiscard(currentPlayer, hotelCard);

        PropertiesCards property = findFirstPropertyByColor(currentPlayer, selectedColor);
        property.setHasHotel(true);

        increaseUseCardTimes(currentPlayer);
        return true;
    }

    private boolean canFinishHouse(Player currentPlayer, ActionCards card, PropertyColor selectedColor) {
        if (!canFinishActionCard(currentPlayer, card, ActionCardType.HOUSE)) {
            return false;
        }

        if (selectedColor == null) {
            return false;
        }

        return isCompleteSet(currentPlayer, selectedColor)
                && !hasHouse(currentPlayer, selectedColor);
    }

    private boolean canFinishHotel(Player currentPlayer, ActionCards card, PropertyColor selectedColor) {
        if (!canFinishActionCard(currentPlayer, card, ActionCardType.HOTEL)) {
            return false;
        }

        if (selectedColor == null) {
            return false;
        }

        return isCompleteSet(currentPlayer, selectedColor)
                && hasHouse(currentPlayer, selectedColor)
                && !hasHotel(currentPlayer, selectedColor);
    }

    private boolean isCompleteSet(Player player, PropertyColor color) {
        int count = 0;

        for (PropertiesCards card : player.getPropertyCards()) {
            if (card.getCurrentColor() == color) {
                count++;
            }
        }

        return count >= color.getAmountToCompleteSet();
    }

    private boolean hasHouse(Player player, PropertyColor color) {
        for (PropertiesCards card : player.getPropertyCards()) {
            if (card.getCurrentColor() == color && card.hasHouse()) {
                return true;
            }
        }

        return false;
    }

    private boolean hasHotel(Player player, PropertyColor color) {
        for (PropertiesCards card : player.getPropertyCards()) {
            if (card.getCurrentColor() == color && card.hasHotel()) {
                return true;
            }
        }

        return false;
    }

    private PropertiesCards findFirstPropertyByColor(Player player, PropertyColor color) {
        for (PropertiesCards card : player.getPropertyCards()) {
            if (card.getCurrentColor() == color) {
                return card;
            }
        }

        return null;
    }
    public boolean finishPassGo(Player currentPlayer, ActionCards passGoCard) {
        if (!canFinishActionCard(currentPlayer, passGoCard, ActionCardType.PASS_GO)) {
            return false;
        }

        moveActionCardToDiscard(currentPlayer, passGoCard);
        currentPlayer.takeCard(2);
        increaseUseCardTimes(currentPlayer);

        return true;
    }
}