package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player {
    private ArrayList<Card> handCards;
    private ArrayList<PropertiesCards> propertyCards;
    private ArrayList<Card> bankCards;
    private DrawPileAndDiscardPile drawCardsAndDiscardPile;
    private int useCardTimes;

    // Creates a Player instance.
    public Player(DrawPileAndDiscardPile drawCardsAndDiscardPile) {
        handCards = new ArrayList<>();
        propertyCards = new ArrayList<>();
        bankCards = new ArrayList<>();
        this.drawCardsAndDiscardPile = drawCardsAndDiscardPile;
        useCardTimes = 0;
    }

    // Takes card.
    public void takeCard(int number) {
        for (int i = 0; i < number; i++) {
            if (drawCardsAndDiscardPile.getDrawPile().isEmpty()) {
                drawCardsAndDiscardPile.shuffle();
            }

            if (drawCardsAndDiscardPile.getDrawPile().isEmpty()) {
                return;
            }

            Card card = drawCardsAndDiscardPile.getDrawPile().remove(0);
            handCards.add(card);
        }
    }

    // Runs put money card.
    public void putMoneyCard(Card card) {
        if (card == null) {
            return;
        }

        if (card instanceof PropertiesCards) {
            return;
        }

        handCards.remove(card);
        bankCards.add(card);
    }

    // Runs put property card.
    public void putPropertyCard(PropertiesCards card) {
        if (card == null) {
            return;
        }

        handCards.remove(card);

        if (card.isWildCard() && card.getCurrentColor() == null) {
            if (!card.getType().getColors().isEmpty()) {
                card.setCurrentColor(card.getType().getColors().get(0));
            }
        }

        propertyCards.add(card);
    }

    // Moves card from hand to discard.
    public void moveCardFromHandToDiscard(Card card) {
        if (card == null) {
            return;
        }

        if (handCards.remove(card)) {
            drawCardsAndDiscardPile.getDiscardPile().add(card);
        }
    }

    // Runs increase use card times.
    public void increaseUseCardTimes() {
        useCardTimes++;
    }

    // Checks whether this can lose property to sly deal.
    public boolean canLosePropertyToSlyDeal(PropertiesCards card) {
        if (card == null || !propertyCards.contains(card)) {
            return false;
        }

        PropertyColor color = card.getCurrentColor();

        if (color == null) {
            return true;
        }

        return !isCompleteSet(color);
    }

    // Checks whether this can use rent color.
    public boolean canUseRentColor(PropertyColor color) {
        return hasPropertyColor(color);
    }

    // Checks whether this has property color.
    public boolean hasPropertyColor(PropertyColor color) {
        if (color == null) {
            return false;
        }

        for (PropertiesCards card : propertyCards) {
            if (card.getCurrentColor() == color) {
                return true;
            }
        }

        return false;
    }

    // Finds property count by color.
    public int getPropertyCountByColor(PropertyColor color) {
        int count = 0;

        if (color == null) {
            return count;
        }

        for (PropertiesCards card : propertyCards) {
            if (card.getCurrentColor() == color) {
                count++;
            }
        }

        return count;
    }

    // Checks whether complete set.
    public boolean isCompleteSet(PropertyColor color) {
        if (color == null) {
            return false;
        }

        int count = getPropertyCountByColor(color);
        return count >= color.getAmountToCompleteSet();
    }

    // Checks whether this has action card.
    public boolean hasActionCard(ActionCardType type) {
        return findActionCard(type) != null;
    }

    // Runs find action card.
    public ActionCards findActionCard(ActionCardType type) {
        if (type == null) {
            return null;
        }

        for (Card card : handCards) {
            if (card instanceof ActionCards actionCard
                    && actionCard.getActionCardType() == type) {
                return actionCard;
            }
        }

        return null;
    }

    // Discards action card from hand.
    public boolean discardActionCardFromHand(ActionCardType type) {
        ActionCards card = findActionCard(type);

        if (card == null) {
            return false;
        }

        handCards.remove(card);
        drawCardsAndDiscardPile.getDiscardPile().add(card);
        return true;
    }

    // Discards card from hand.
    public boolean discardCardFromHand(Card card) {
        if (card == null) {
            return false;
        }

        if (!handCards.remove(card)) {
            return false;
        }

        drawCardsAndDiscardPile.getDiscardPile().add(card);
        return true;
    }

    // Receives bank card.
    public void receiveBankCard(Card card) {
        if (card == null) {
            return;
        }

        bankCards.add(card);
    }

    // Receives property card.
    public void receivePropertyCard(PropertiesCards card) {
        if (card == null) {
            return;
        }

        propertyCards.add(card);
    }

    // Removes bank card.
    public void removeBankCard(Card card) {
        bankCards.remove(card);
    }

    // Removes property card.
    public void removePropertyCard(PropertiesCards card) {
        propertyCards.remove(card);
    }

    // Takes money.
    public void takeMoney(int amount, Player payer) {
        if (payer == null || amount <= 0) {
            return;
        }

        int payerTotalValue = getTotalValue(payer.getBankCards()) + getTotalValue(payer.getPropertyCards());

        if (payerTotalValue <= amount) {
            bankCards.addAll(payer.getBankCards());
            propertyCards.addAll(payer.getPropertyCards());
            payer.getBankCards().clear();
            payer.getPropertyCards().clear();
            return;
        }

        int payerBankValue = getTotalValue(payer.getBankCards());

        if (payerBankValue >= amount) {
            ArrayList<Card> paymentCards = findSmallestPaymentCombination(payer.getBankCards(), amount);
            bankCards.addAll(paymentCards);
            payer.getBankCards().removeAll(paymentCards);
            return;
        }

        bankCards.addAll(payer.getBankCards());
        payer.getBankCards().clear();

        int remainingAmount = amount - payerBankValue;
        ArrayList<Card> propertyPaymentCards = findSmallestPaymentCombination(payer.getPropertyCards(), remainingAmount);

        for (Card card : propertyPaymentCards) {
            if (card instanceof PropertiesCards propertyCard) {
                propertyCards.add(propertyCard);
                payer.getPropertyCards().remove(propertyCard);
            }
        }
    }

    // Finds total value.
    private int getTotalValue(List<? extends Card> cards) {
        int total = 0;

        for (Card card : cards) {
            total += card.getValue();
        }

        return total;
    }

    // Runs find smallest payment combination.
    private ArrayList<Card> findSmallestPaymentCombination(List<? extends Card> cards, int amount) {
        ArrayList<Card> bestCombination = new ArrayList<>();
        int bestSum = Integer.MAX_VALUE;
        int cardCount = cards.size();

        if (cardCount >= 63) {
            return new ArrayList<>(cards);
        }

        for (long mask = 1; mask < (1L << cardCount); mask++) {
            ArrayList<Card> currentCombination = new ArrayList<>();
            int currentSum = 0;

            for (int i = 0; i < cardCount; i++) {
                if ((mask & (1L << i)) != 0) {
                    Card card = cards.get(i);
                    currentCombination.add(card);
                    currentSum += card.getValue();
                }
            }

            if (currentSum >= amount && currentSum < bestSum) {
                bestSum = currentSum;
                bestCombination = currentCombination;
            }
        }

        return bestCombination;
    }

    // Runs check if win.
    public boolean checkIfWin() {
        if (propertyCards.isEmpty()) {
            return false;
        }

        Map<PropertyColor, Integer> colorCount = new HashMap<>();

        for (PropertiesCards card : propertyCards) {
            PropertyColor color = card.getCurrentColor();

            if (color == null) {
                continue;
            }

            colorCount.put(color, colorCount.getOrDefault(color, 0) + 1);
        }

        int completedSets = 0;

        for (PropertyColor color : colorCount.keySet()) {
            int count = colorCount.get(color);

            if (count >= color.getAmountToCompleteSet()) {
                completedSets++;
            }
        }

        return completedSets >= 3;
    }

    public ArrayList<Card> getHandCards() {
        return handCards;
    }

    public ArrayList<PropertiesCards> getPropertyCards() {
        return propertyCards;
    }

    public ArrayList<Card> getBankCards() {
        return bankCards;
    }

    public DrawPileAndDiscardPile getDrawCardsAndDiscardPile() {
        return drawCardsAndDiscardPile;
    }

    public int getUseCardTimes() {
        return useCardTimes;
    }

    public void setUseCardTimes(int useCardTimes) {
        this.useCardTimes = useCardTimes;
    }
}
