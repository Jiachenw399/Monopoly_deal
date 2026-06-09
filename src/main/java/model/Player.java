package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Player {
    private ArrayList<Card> handCards;
    private ArrayList<PropertiesCards> propertyCards;
    private ArrayList<Card> bankCards;
    private DrawPileAndDiscardPile drawCardsAndDiscardPile;
    private int useCardTimes;
    private String name;
    private boolean isAI;

    // Creates a Player instance.
    public Player(DrawPileAndDiscardPile drawCardsAndDiscardPile) {
        handCards = new ArrayList<>();
        propertyCards = new ArrayList<>();
        bankCards = new ArrayList<>();
        this.drawCardsAndDiscardPile = drawCardsAndDiscardPile;
        useCardTimes = 0;
        this.name = null;
        this.isAI = false;
    }

    // Creates a Player instance with a name.
    public Player(DrawPileAndDiscardPile drawCardsAndDiscardPile, String name) {
        handCards = new ArrayList<>();
        propertyCards = new ArrayList<>();
        bankCards = new ArrayList<>();
        this.drawCardsAndDiscardPile = drawCardsAndDiscardPile;
        useCardTimes = 0;
        this.name = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        this.isAI = false;
    }

    // Creates a Player instance with name and AI flag.
    public Player(DrawPileAndDiscardPile drawCardsAndDiscardPile, String name, boolean isAI) {
        handCards = new ArrayList<>();
        propertyCards = new ArrayList<>();
        bankCards = new ArrayList<>();
        this.drawCardsAndDiscardPile = drawCardsAndDiscardPile;
        useCardTimes = 0;
        this.name = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        this.isAI = isAI;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
    }

    public boolean isAI() {
        return isAI;
    }

    public void setAI(boolean AI) {
        this.isAI = AI;
    }
}
