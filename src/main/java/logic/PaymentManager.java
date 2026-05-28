package logic;

import model.ActionCardType;
import model.Card;
import model.Player;
import model.PropertiesCards;

import java.util.ArrayList;

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

    private boolean isValidPaymentSelection(Player payer, ArrayList<Card> selectedCards, int amount) {
        int selectedTotal = getCardsValue(selectedCards);
        int totalAssets = getTotalAssetsValue(payer);

        if (totalAssets <= amount) {
            return selectedTotal == totalAssets;
        }

        return selectedTotal >= amount;
    }

    private void transferSelectedCards(Player receiver, Player payer, ArrayList<Card> selectedCards) {
        for (Card card : selectedCards) {
            if (payer.getBankCards().remove(card)) {
                receiver.getBankCards().add(card);
            } else if (card instanceof PropertiesCards propertyCard
                    && payer.getPropertyCards().remove(propertyCard)) {
                receiver.getPropertyCards().add(propertyCard);
            }
        }
    }
}
