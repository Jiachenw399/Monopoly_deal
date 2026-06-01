package logic;

import model.ActionCards;
import model.ActionCardType;
import model.Card;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;

public class ActionCardService {
    private final ArrayList<Player> players;
    private final PaymentManager paymentManager;
    private final RentCalculator rentCalculator;

    // Creates a ActionCardService instance.
    public ActionCardService(ArrayList<Player> players,
                             PaymentManager paymentManager,
                             RentCalculator rentCalculator) {
        this.players = players;
        this.paymentManager = paymentManager;
        this.rentCalculator = rentCalculator;
    }

    // Finishes pass go.
    public boolean finishPassGo(Player currentPlayer, ActionCards passGoCard) {
        if (!canFinishActionCard(currentPlayer, passGoCard, ActionCardType.PASS_GO)) {
            return false;
        }

        moveActionCardToDiscard(currentPlayer, passGoCard);
        currentPlayer.takeCard(2);
        increaseUseCardTimes(currentPlayer);
        return true;
    }

    // Finishes birthday.
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

    // Finishes sly deal.
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

    // Finishes deal breaker.
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

    // Finishes debt collector.
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

    // Finishes two color rent.
    public boolean finishTwoColorRent(Player currentPlayer,
                                      ActionCards rentCard,
                                      PropertyColor selectedColor,
                                      boolean useDoubleRent) {
        if (!canFinishTwoColorRent(currentPlayer, rentCard, selectedColor)) {
            return false;
        }

        return finishRent(currentPlayer, rentCard, selectedColor, null, true, useDoubleRent);
    }

    // Finishes multiple color rent.
    public boolean finishMultipleColorRent(Player currentPlayer,
                                           ActionCards rentCard,
                                           Player targetPlayer,
                                           PropertyColor selectedColor,
                                           boolean useDoubleRent) {
        if (!canFinishMultipleColorRent(currentPlayer, rentCard, targetPlayer, selectedColor)) {
            return false;
        }

        return finishRent(currentPlayer, rentCard, selectedColor, targetPlayer, false, useDoubleRent);
    }

    // Finishes forced deal.
    public boolean finishForcedDeal(Player currentPlayer,
                                    ActionCards forcedDealCard,
                                    Player targetPlayer,
                                    PropertiesCards currentPlayerCard,
                                    PropertiesCards targetPlayerCard) {
        if (!canFinishForcedDeal(currentPlayer, forcedDealCard, targetPlayer, currentPlayerCard, targetPlayerCard)) {
            return false;
        }

        moveActionCardToDiscard(currentPlayer, forcedDealCard);

        currentPlayer.getPropertyCards().remove(currentPlayerCard);
        targetPlayer.getPropertyCards().remove(targetPlayerCard);

        currentPlayer.getPropertyCards().add(targetPlayerCard);
        targetPlayer.getPropertyCards().add(currentPlayerCard);

        increaseUseCardTimes(currentPlayer);
        return true;
    }

    // Finishes house.
    public boolean finishHouse(Player currentPlayer, ActionCards houseCard, PropertyColor selectedColor) {
        return finishBuilding(currentPlayer, houseCard, selectedColor, ActionCardType.HOUSE);
    }

    // Finishes hotel.
    public boolean finishHotel(Player currentPlayer, ActionCards hotelCard, PropertyColor selectedColor) {
        return finishBuilding(currentPlayer, hotelCard, selectedColor, ActionCardType.HOTEL);
    }

    // Checks whether this has double the rent card.
    public boolean hasDoubleTheRentCard(Player player) {
        if (player == null) {
            return false;
        }

        for (Card card : player.getHandCards()) {
            if (card instanceof ActionCards actionCard
                    && actionCard.getActionCardType() == ActionCardType.DOUBLE_THE_RENT) {
                return true;
            }
        }

        return false;
    }

    // Finishes rent.
    private boolean finishRent(Player currentPlayer,
                               ActionCards rentCard,
                               PropertyColor selectedColor,
                               Player targetPlayer,
                               boolean allPlayers,
                               boolean useDoubleRent) {
        boolean canUseDoubleRent = canUseDoubleRent(currentPlayer, useDoubleRent);

        if (canUseDoubleRent && currentPlayer.getUseCardTimes() > 1) {
            return false;
        }

        int rent = getFinalRent(currentPlayer, selectedColor, canUseDoubleRent);

        moveActionCardToDiscard(currentPlayer, rentCard);
        discardDoubleTheRentIfUsed(currentPlayer, canUseDoubleRent);

        if (allPlayers) {
            addPaymentRequestsForAllOtherPlayers(currentPlayer, rent);
        } else {
            paymentManager.addPaymentRequest(currentPlayer, targetPlayer, rent);
        }

        paymentManager.startNextPaymentRequest();
        increaseRentUseTimes(currentPlayer, canUseDoubleRent);
        return true;
    }

    // Adds payment requests for all other players.
    private void addPaymentRequestsForAllOtherPlayers(Player currentPlayer, int amount) {
        for (Player player : players) {
            if (player != currentPlayer) {
                paymentManager.addPaymentRequest(currentPlayer, player, amount);
            }
        }
    }

    // Finishes building.
    private boolean finishBuilding(Player currentPlayer,
                                   ActionCards buildingCard,
                                   PropertyColor selectedColor,
                                   ActionCardType buildingType) {
        if (!canFinishBuilding(currentPlayer, buildingCard, selectedColor, buildingType)) {
            return false;
        }

        moveActionCardToDiscard(currentPlayer, buildingCard);

        PropertiesCards property = findFirstPropertyByColor(currentPlayer, selectedColor);

        if (property == null) {
            return false;
        }

        if (buildingType == ActionCardType.HOUSE) {
            property.setHasHouse(true);
        } else {
            property.setHasHotel(true);
        }

        increaseUseCardTimes(currentPlayer);
        return true;
    }

    // Checks whether this can finish building.
    private boolean canFinishBuilding(Player currentPlayer,
                                      ActionCards card,
                                      PropertyColor selectedColor,
                                      ActionCardType buildingType) {
        if (!canFinishActionCard(currentPlayer, card, buildingType) || selectedColor == null) {
            return false;
        }

        if (!isCompleteSet(currentPlayer, selectedColor)) {
            return false;
        }

        boolean hasHouse = PlayerInfoHelper.hasHouse(currentPlayer, selectedColor);
        boolean hasHotel = PlayerInfoHelper.hasHotel(currentPlayer, selectedColor);

        if (buildingType == ActionCardType.HOUSE) {
            return !hasHouse;
        }

        return hasHouse && !hasHotel;
    }

    // Checks whether this can finish sly deal.
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

    // Checks whether this can finish forced deal.
    private boolean canFinishForcedDeal(Player currentPlayer,
                                        ActionCards card,
                                        Player targetPlayer,
                                        PropertiesCards currentPlayerCard,
                                        PropertiesCards targetPlayerCard) {
        if (!canFinishActionCard(currentPlayer, card, ActionCardType.FORCED_DEAL)) {
            return false;
        }

        if (targetPlayer == null || targetPlayer == currentPlayer) {
            return false;
        }

        if (currentPlayerCard == null || targetPlayerCard == null) {
            return false;
        }

        if (!currentPlayer.getPropertyCards().contains(currentPlayerCard)) {
            return false;
        }

        if (!targetPlayer.getPropertyCards().contains(targetPlayerCard)) {
            return false;
        }

        if (!PlayerInfoHelper.canBeStolenBySlyDeal(currentPlayer, currentPlayerCard)) {
            return false;
        }

        return PlayerInfoHelper.canBeStolenBySlyDeal(targetPlayer, targetPlayerCard);
    }

    // Checks whether this can finish deal breaker.
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

        PropertyColor color = selectedSet.get(0).getCurrentColor();

        if (color == null) {
            return false;
        }

        for (PropertiesCards propertyCard : selectedSet) {
            if (!targetPlayer.getPropertyCards().contains(propertyCard)
                    || propertyCard.getCurrentColor() != color) {
                return false;
            }
        }

        return selectedSet.size() >= color.getAmountToCompleteSet();
    }

    // Checks whether this can finish debt collector.
    private boolean canFinishDebtCollector(Player currentPlayer, ActionCards card, Player targetPlayer) {
        if (!canFinishActionCard(currentPlayer, card, ActionCardType.DEBT_COLLECTOR)) {
            return false;
        }

        return targetPlayer != null && targetPlayer != currentPlayer;
    }

    // Checks whether this can finish two color rent.
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

        return card.getActionCardType().getRentColors().contains(selectedColor);
    }

    // Checks whether this can finish multiple color rent.
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

    // Checks whether this can finish action card.
    private boolean canFinishActionCard(Player currentPlayer, ActionCards card, ActionCardType expectedType) {
        if (card == null || card.getActionCardType() != expectedType) {
            return false;
        }

        return canPlayCard(currentPlayer, card);
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

    // Checks whether this can use double rent.
    private boolean canUseDoubleRent(Player player, boolean useDoubleRent) {
        return useDoubleRent && hasDoubleTheRentCard(player);
    }

    // Finds final rent.
    private int getFinalRent(Player player, PropertyColor selectedColor, boolean canUseDoubleRent) {
        return rentCalculator.calculateRent(player, selectedColor, canUseDoubleRent);
    }

    // Runs increase rent use times.
    private void increaseRentUseTimes(Player player, boolean canUseDoubleRent) {
        increaseUseCardTimes(player);

        if (canUseDoubleRent) {
            increaseUseCardTimes(player);
        }
    }

    // Discards double the rent if used.
    private void discardDoubleTheRentIfUsed(Player player, boolean useDoubleRent) {
        if (!useDoubleRent) {
            return;
        }

        ActionCards doubleRentCard = player.findActionCard(ActionCardType.DOUBLE_THE_RENT);

        if (doubleRentCard != null) {
            player.moveCardFromHandToDiscard(doubleRentCard);
        }
    }

    // Moves action card to discard.
    private void moveActionCardToDiscard(Player currentPlayer, ActionCards card) {
        currentPlayer.moveCardFromHandToDiscard(card);
    }

    // Runs increase use card times.
    private void increaseUseCardTimes(Player player) {
        player.setUseCardTimes(player.getUseCardTimes() + 1);
    }

    // Checks whether complete set.
    private boolean isCompleteSet(Player player, PropertyColor color) {
        int count = PlayerInfoHelper.getPropertyCountByCurrentColor(player, color);
        return count >= color.getAmountToCompleteSet();
    }

    // Runs find first property by color.
    private PropertiesCards findFirstPropertyByColor(Player player, PropertyColor color) {
        for (PropertiesCards card : player.getPropertyCards()) {
            if (card.getCurrentColor() == color) {
                return card;
            }
        }

        return null;
    }
}
