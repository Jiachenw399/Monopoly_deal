package logic;

import model.ActionCardType;
import model.ActionCards;
import model.BuildingPaymentCard;
import model.Card;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PaymentManager {
    private final ArrayList<Game.PaymentRequest> paymentRequests = new ArrayList<>();
    private Game.PaymentRequest currentPaymentRequest;

    public void addPaymentRequest(Player receiver, Player payer, int amount) {
        if (receiver == null || payer == null || amount <= 0) {
            return;
        }

        if (getTotalAssetsValue(payer) <= 0) {
            return;
        }

        paymentRequests.add(new Game.PaymentRequest(receiver, payer, amount));
    }

    public void startNextPaymentRequest() {
        if (currentPaymentRequest != null || paymentRequests.isEmpty()) {
            return;
        }

        currentPaymentRequest = paymentRequests.removeFirst();
    }

    public void currentPaymentUseJustSayNo() {
        if (!canCurrentPaymentUseJustSayNo()) {
            return;
        }

        currentPaymentRequest.getPayer().discardActionCardFromHand(ActionCardType.JUST_SAY_NO);
        currentPaymentRequest = null;
        startNextPaymentRequest();
    }

    public boolean finishCurrentPayment(ArrayList<Card> selectedCards) {
        if (currentPaymentRequest == null || selectedCards == null || selectedCards.isEmpty()) {
            return false;
        }

        Player receiver = currentPaymentRequest.getReceiver();
        Player payer = currentPaymentRequest.getPayer();

        if (!isValidPaymentSelection(payer, selectedCards, currentPaymentRequest.getAmount())) {
            return false;
        }

        transferSelectedCards(receiver, payer, selectedCards);

        currentPaymentRequest = null;
        startNextPaymentRequest();
        return true;
    }

    public boolean isPaymentSelecting() {
        return currentPaymentRequest != null;
    }

    public Game.PaymentRequest getCurrentPaymentRequest() {
        return currentPaymentRequest;
    }

    public void applyOnlineState(Game.PaymentRequest currentPaymentRequest) {
        paymentRequests.clear();
        this.currentPaymentRequest = currentPaymentRequest;
    }

    public boolean canCurrentPaymentUseJustSayNo() {
        return currentPaymentRequest != null
                && currentPaymentRequest.getPayer().hasActionCard(ActionCardType.JUST_SAY_NO);
    }

    public int getTotalAssetsValue(Player player) {
        int total = PlayerInfoHelper.getBankTotal(player);

        for (PropertiesCards card : player.getPropertyCards()) {
            total += card.getValue();
            total += getBuildingValue(card);
        }

        return total;
    }

    public int getCardsValue(ArrayList<Card> cards) {
        int total = 0;

        for (Card card : cards) {
            total += card.getValue();
        }

        return total;
    }

    public int getPaymentCardsValue(Player payer, ArrayList<Card> cards) {
        int total = 0;

        for (Card card : cards) {
            total += card.getValue();
        }

        return total;
    }

    private boolean isValidPaymentSelection(Player payer, ArrayList<Card> selectedCards, int amount) {
        if (!isValidBuildingSelection(payer, selectedCards)
                || containsBlockedPropertySelection(payer, selectedCards)) {
            return false;
        }

        int selectedTotal = getPaymentCardsValue(payer, selectedCards);
        int totalAssets = getTotalAssetsValue(payer);

        if (totalAssets <= amount) {
            return selectedTotal == totalAssets;
        }

        return selectedTotal >= amount;
    }

    private void transferSelectedCards(Player receiver, Player payer, ArrayList<Card> selectedCards) {
        transferSelectedBuildings(receiver, payer, selectedCards);

        for (Card card : selectedCards) {
            if (card instanceof BuildingPaymentCard) {
                continue;
            }

            if (payer.getBankCards().remove(card)) {
                receiver.getBankCards().add(card);
            } else if (card instanceof PropertiesCards propertyCard
                    && payer.getPropertyCards().contains(propertyCard)) {
                payer.getPropertyCards().remove(propertyCard);
                receiver.getPropertyCards().add(propertyCard);
            }
        }
    }

    private void transferSelectedBuildings(Player receiver, Player payer, ArrayList<Card> selectedCards) {
        for (Card card : selectedCards) {
            if (card instanceof BuildingPaymentCard buildingCard) {
                transferBuilding(receiver, payer, buildingCard);
            }
        }
    }

    private void transferBuilding(Player receiver, Player payer, BuildingPaymentCard buildingCard) {
        PropertiesCards sourceCard = findBuildingCardByColor(
                payer,
                buildingCard.getColor(),
                buildingCard.getActionCardType()
        );

        if (sourceCard == null) {
            return;
        }

        if (buildingCard.getActionCardType() == ActionCardType.HOTEL) {
            receiver.getBankCards().add(new ActionCards(ActionCardType.HOTEL));
            sourceCard.setHasHotel(false);
            return;
        }

        if (buildingCard.getActionCardType() == ActionCardType.HOUSE) {
            receiver.getBankCards().add(new ActionCards(ActionCardType.HOUSE));
            sourceCard.setHasHouse(false);
        }
    }

    private int getBuildingValue(PropertiesCards propertyCard) {
        int total = 0;

        if (propertyCard.hasHotel()) {
            total += ActionCardType.HOTEL.getTypeValue();
        }

        if (propertyCard.hasHouse()) {
            total += ActionCardType.HOUSE.getTypeValue();
        }

        return total;
    }

    private PropertiesCards findBuildingCardByColor(Player player, PropertyColor color, ActionCardType type) {
        for (PropertiesCards propertyCard : player.getPropertyCards()) {
            if (propertyCard.getCurrentColor() == color && hasBuilding(propertyCard, type)) {
                return propertyCard;
            }
        }

        return null;
    }

    private boolean hasBuilding(PropertiesCards propertyCard, ActionCardType type) {
        if (type == ActionCardType.HOTEL) {
            return propertyCard.hasHotel();
        }

        if (type == ActionCardType.HOUSE) {
            return propertyCard.hasHouse();
        }

        return false;
    }

    private boolean isValidBuildingSelection(Player payer, ArrayList<Card> selectedCards) {
        Set<String> selectedBuildings = new HashSet<>();

        for (Card card : selectedCards) {
            if (card instanceof BuildingPaymentCard buildingCard) {
                if (buildingCard.getColor() == null
                        || findBuildingCardByColor(
                        payer,
                        buildingCard.getColor(),
                        buildingCard.getActionCardType()
                ) == null) {
                    return false;
                }

                if (!selectedBuildings.add(buildingKey(buildingCard.getColor(), buildingCard.getActionCardType()))) {
                    return false;
                }

                if (buildingCard.getActionCardType() == ActionCardType.HOUSE
                        && PlayerInfoHelper.hasHotel(payer, buildingCard.getColor())
                        && !hasSelectedBuilding(selectedCards, buildingCard.getColor(), ActionCardType.HOTEL)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean containsBlockedPropertySelection(Player payer, ArrayList<Card> selectedCards) {
        for (Card card : selectedCards) {
            if (card instanceof PropertiesCards propertyCard
                    && isPropertyBlockedByBuildings(payer, propertyCard, selectedCards)) {
                return true;
            }
        }

        return false;
    }

    private boolean isPropertyBlockedByBuildings(Player payer,
                                                 PropertiesCards propertyCard,
                                                 ArrayList<Card> selectedCards) {
        PropertyColor color = propertyCard.getCurrentColor();

        if (color == null) {
            return false;
        }

        if (PlayerInfoHelper.hasHotel(payer, color)
                && !hasSelectedBuilding(selectedCards, color, ActionCardType.HOTEL)) {
            return true;
        }

        return PlayerInfoHelper.hasHouse(payer, color)
                && !hasSelectedBuilding(selectedCards, color, ActionCardType.HOUSE);
    }

    private boolean hasSelectedBuilding(ArrayList<Card> selectedCards, PropertyColor color, ActionCardType type) {
        for (Card card : selectedCards) {
            if (card instanceof BuildingPaymentCard buildingCard
                    && buildingCard.getColor() == color
                    && buildingCard.getActionCardType() == type) {
                return true;
            }
        }

        return false;
    }

    private String buildingKey(PropertyColor color, ActionCardType type) {
        return color.name() + ":" + type.name();
    }
}
