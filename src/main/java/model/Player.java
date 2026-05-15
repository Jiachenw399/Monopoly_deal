package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Player {

    private ArrayList<Card> HandCards;
    private ArrayList<PropertiesCards> PropertyCards;
    private ArrayList<Card> BankCards;
    private boolean isOnTurn;
    private DrawPileAndDiscardPile drawCardsAndDiscardPile;
    private int UseCardTimes;
    private ArrayList<Player> Enemy;

    public Player(DrawPileAndDiscardPile drawCardsAndDiscardPile) {
        Enemy = new ArrayList<>();
        HandCards = new ArrayList<>();
        PropertyCards = new ArrayList<>();
        BankCards = new ArrayList<>();
        this.isOnTurn = false;
        this.drawCardsAndDiscardPile = drawCardsAndDiscardPile;
        this.UseCardTimes = 0;
    }//创建玩家时 创建各种列表 手上的 地产 钱

    public void takeCard(int number) {
        for (int i = 0; i < number; i++) {
            if (drawCardsAndDiscardPile.getDrawPile().isEmpty()) {
                drawCardsAndDiscardPile.shuffle();
            }
            if (drawCardsAndDiscardPile.getDrawPile().isEmpty()) {
                return;
            }
            HandCards.add(drawCardsAndDiscardPile.getDrawPile().remove(0));
        }
    }

    public void takeMoney(int number, Player player) {
        if (player == null || number <= 0) {
            return;
        }

        int totalAvailable = getTotalValue(player.getBankCards()) + getTotalValue(player.getPropertyCards());

        if (totalAvailable <= number) {
            BankCards.addAll(player.getBankCards());
            PropertyCards.addAll(player.getPropertyCards());
            player.getBankCards().clear();
            player.getPropertyCards().clear();
            return;
        }

        int bankTotal = getTotalValue(player.getBankCards());

        if (bankTotal >= number) {
            ArrayList<Card> bankPayment = findSmallestPaymentCombination(player.getBankCards(), number);
            BankCards.addAll(bankPayment);
            player.getBankCards().removeAll(bankPayment);
            return;
        }

        BankCards.addAll(player.getBankCards());
        player.getBankCards().clear();

        int remainingAmount = number - bankTotal;
        ArrayList<Card> propertyPayment = findSmallestPaymentCombination(player.getPropertyCards(), remainingAmount);

        for (Card card : propertyPayment) {
            if (card instanceof PropertiesCards propertyCard) {
                PropertyCards.add(propertyCard);
                player.getPropertyCards().remove(propertyCard);
            }
        }
    }

    private int getTotalValue(List<? extends Card> cards) {
        int total = 0;

        for (Card card : cards) {
            total += card.getValue();
        }

        return total;
    }

    private ArrayList<Card> findSmallestPaymentCombination(List<? extends Card> cards, int amount) {
        ArrayList<Card> bestCombination = new ArrayList<>();
        int bestSum = Integer.MAX_VALUE;
        int n = cards.size();

        for (long mask = 1; mask < (1L << n); mask++) {
            ArrayList<Card> currentCombination = new ArrayList<>();
            int currentSum = 0;

            for (int i = 0; i < n; i++) {
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

    public void putMoneyCard(Card card) {
        if(card.getClass().equals(PropertiesCards.class)){
            return;
        }
        HandCards.remove(card);
        BankCards.add(card);
    }//用钱 对应规则A

    public void putPropertyCard(PropertiesCards card) {
        HandCards.remove(card);
        if (card.isWildCard() && card.getCurrentColor() == null) {
            card.setCurrentColor(card.getType().getColors().getFirst());
        }
        PropertyCards.add(card);
    }

    private boolean isCompleteSet(PropertyColor color) {
        int count = 0;
    
        for (PropertiesCards card : PropertyCards) {
            if (card.getCurrentColor() == color) {
                count++;
            }
        }
    
        return count >= color.getAmountToCompleteSet();
    }

    private int countPropertiesByColor(PropertyColor color) {
        int count = 0;
    
        for (PropertiesCards card : PropertyCards) {
            if (card.getCurrentColor() == color) {
                count++;
            }
        }
    
        return count;
    }
    
    private int calculateRent(PropertyColor color) {
        int propertyCount = countPropertiesByColor(color);
    
        if (propertyCount == 0) {
            return 0;
        }
    
        int rent = propertyCount;
    
        for (PropertiesCards card : PropertyCards) {
            if (card.getCurrentColor() == color) {
                if (card.hasHouse()) {
                    rent += 3;
                }
    
                if (card.hasHotel()) {
                    rent += 4;
                }
            }
        }
    
        return rent;
    }
    
    private void receivePayment(int amount, Player payer) {
        takeMoney(amount, payer);
    }
    
    private PropertiesCards findFirstPropertyThatCanBeStolen(Player target) {
        for (PropertiesCards card : target.getPropertyCards()) {
            if (target.canLosePropertyToSlyDeal(card)) {
                return card;
            }
        }
    
        return null;
    }

    public boolean canLosePropertyToSlyDeal(PropertiesCards card) {
        if (card == null || !PropertyCards.contains(card)) {
            return false;
        }

        PropertyColor color = card.getCurrentColor();

        if (color == null) {
            return true;
        }

        return !isCompleteSet(color);
    }

    public boolean canUseRentColor(PropertyColor color) {
        return hasPropertyColor(color);
    }
    
    private ArrayList<PropertiesCards> findFirstCompleteSet(Player target) {
        ArrayList<PropertiesCards> result = new ArrayList<>();
    
        for (PropertyColor color : PropertyColor.values()) {
            result.clear();
    
            for (PropertiesCards card : target.getPropertyCards()) {
                if (card.getCurrentColor() == color) {
                    result.add(card);
                }
            }
    
            if (result.size() >= color.getAmountToCompleteSet()) {
                return new ArrayList<>(result);
            }
        }
    
        return new ArrayList<>();
    }
    
    private PropertiesCards findFirstPropertyByColor(PropertyColor color) {
        for (PropertiesCards card : PropertyCards) {
            if (card.getCurrentColor() == color) {
                return card;
            }
        }
    
        return null;
    }
    
    private PropertyColor getFirstUsableRentColor(ActionCardType type) {
        switch (type) {
            case RENT_WITH_RED_AND_YELLOW:
                return hasPropertyColor(PropertyColor.RED) ? PropertyColor.RED : PropertyColor.YELLOW;
    
            case RENT_WITH_ORANGE_AND_PINK:
                return hasPropertyColor(PropertyColor.ORANGE) ? PropertyColor.ORANGE : PropertyColor.PINK;
    
            case RENT_WITH_BROWN_AND_LIGHT_BLUE:
                return hasPropertyColor(PropertyColor.BROWN) ? PropertyColor.BROWN : PropertyColor.LIGHT_BLUE;
    
            case RENT_WITH_BLACK_AND_LIGHT_GREEN:
                return hasPropertyColor(PropertyColor.BLACK) ? PropertyColor.BLACK : PropertyColor.LIGHT_GREEN;
    
            case RENT_WITH_DARK_BLUE_AND_DARK_GREEN:
                return hasPropertyColor(PropertyColor.DARK_BLUE) ? PropertyColor.DARK_BLUE : PropertyColor.DARK_GREEN;
    
            case RENT_WITH_MULTIPLE_COLOR:
                for (PropertyColor color : PropertyColor.values()) {
                    if (hasPropertyColor(color)) {
                        return color;
                    }
                }
                return null;
    
            default:
                return null;
        }
    }

    private boolean hasPropertyColor(PropertyColor color) {
        for (PropertiesCards card : PropertyCards) {
            if (card.getCurrentColor() == color) {
                return true;
            }
        }
    
        return false;
    }

    public boolean hasActionCard(ActionCardType type) {
        return findActionCard(type) != null;
    }

    public boolean discardActionCardFromHand(ActionCardType type) {
        ActionCards card = findActionCard(type);

        if (card == null) {
            return false;
        }

        HandCards.remove(card);
        drawCardsAndDiscardPile.getDiscardPile().add(card);
        return true;
    }

    public ActionCards findActionCard(ActionCardType type) {
        for (Card card : HandCards) {
            if (card instanceof ActionCards actionCard && actionCard.getActionCardType() == type) {
                return actionCard;
            }
        }

        return null;
    }

    public void collectRentFrom(Player target, PropertyColor color, boolean doubleRent) {
        chargeRentFromOnePlayer(target, color, doubleRent);
    }
    
    private void chargeRentFromOnePlayer(Player target, PropertyColor color, boolean doubleRent) {
        int rent = calculateRent(color);
    
        if (doubleRent) {
            rent *= 2;
        }
    
        receivePayment(rent, target);
    }

    private void chargeRentFromAllPlayers(PropertyColor color, boolean doubleRent) {
        for (Player enemy : Enemy) {
            chargeRentFromOnePlayer(enemy, color, doubleRent);
        }
    }

    public void putActionCard(ActionCards card) {
        HandCards.remove(card);
        drawCardsAndDiscardPile.getDiscardPile().add(card);

        ActionCardType type = card.getActionCardType();

        switch (type) {
            case PASS_GO:
                takeCard(2);
                break;

            case BIRTHDAY:
                for (Player enemy : Enemy) {
                    receivePayment(2, enemy);
                }
                break;

            case DEBT_COLLECTOR:
                if (!Enemy.isEmpty()) {
                    receivePayment(5, Enemy.get(0));
                }
                break;

            case SLY_DEAL:
                if (!Enemy.isEmpty()) {
                    Player target = Enemy.get(0);
                    PropertiesCards stolenCard = findFirstPropertyThatCanBeStolen(target);

                    if (stolenCard != null) {
                        target.getPropertyCards().remove(stolenCard);
                        PropertyCards.add(stolenCard);
                    }
                }
                break;

            case DEAL_BREAKER:
                if (!Enemy.isEmpty()) {
                    Player target = Enemy.get(0);
                    ArrayList<PropertiesCards> completeSet = findFirstCompleteSet(target);

                    if (!completeSet.isEmpty()) {
                        target.getPropertyCards().removeAll(completeSet);
                        PropertyCards.addAll(completeSet);
                    }
                }
                break;

            case FORCED_DEAL:
                if (!Enemy.isEmpty() && !PropertyCards.isEmpty()) {
                    Player target = Enemy.get(0);

                    if (!target.getPropertyCards().isEmpty()) {
                        PropertiesCards myCard = PropertyCards.remove(0);
                        PropertiesCards targetCard = target.getPropertyCards().remove(0);

                        PropertyCards.add(targetCard);
                        target.getPropertyCards().add(myCard);
                    }
                }
                break;

            case RENT_WITH_RED_AND_YELLOW:
            case RENT_WITH_ORANGE_AND_PINK:
            case RENT_WITH_BROWN_AND_LIGHT_BLUE:
            case RENT_WITH_BLACK_AND_LIGHT_GREEN:
            case RENT_WITH_DARK_BLUE_AND_DARK_GREEN:
                PropertyColor groupRentColor = getFirstUsableRentColor(type);

                if (groupRentColor != null) {
                    chargeRentFromAllPlayers(groupRentColor, false);
                }
                break;

            case RENT_WITH_MULTIPLE_COLOR:
                if (!Enemy.isEmpty()) {
                    PropertyColor rentColor = getFirstUsableRentColor(type);

                    if (rentColor != null) {
                        chargeRentFromOnePlayer(Enemy.get(0), rentColor, false);
                    }
                }
                break;

            case DOUBLE_THE_RENT:
                HandCards.add(card);
                drawCardsAndDiscardPile.getDiscardPile().remove(card);
                System.out.println("Double The Rent must be played together with a rent card.");
                break;

            case HOUSE:
                for (PropertyColor color : PropertyColor.values()) {
                    if (isCompleteSet(color)) {
                        PropertiesCards property = findFirstPropertyByColor(color);

                        if (property != null && !property.hasHouse()) {
                            property.setHasHouse(true);
                            break;
                        }
                    }
                }
                break;

            case HOTEL:
                for (PropertyColor color : PropertyColor.values()) {
                    if (isCompleteSet(color)) {
                        PropertiesCards property = findFirstPropertyByColor(color);

                        if (property != null && property.hasHouse() && !property.hasHotel()) {
                            property.setHasHotel(true);
                            break;
                        }
                    }
                }
                break;

            case JUST_SAY_NO:
                HandCards.add(card);
                drawCardsAndDiscardPile.getDiscardPile().remove(card);
                System.out.println("Just Say No can only be used to cancel another action.");
                break;

            default:
                break;
        }
    }

    public ArrayList<Card> getHandCards() {return HandCards;}

    public ArrayList<PropertiesCards> getPropertyCards() {return PropertyCards;}

    public ArrayList<Card> getBankCards() {return BankCards;}

    public ArrayList<Player> getEnemy() {return Enemy;}


    public boolean checkIfWin() {
        if (PropertyCards == null || PropertyCards.isEmpty()) {
            return false;
        }
        Map <PropertyColor, Integer> colorCount = new java.util.HashMap<>();
        for (int i = 0; i < PropertyCards.size(); i++) {
            colorCount.put(PropertyCards.get(i).getCurrentColor(), colorCount.getOrDefault(PropertyCards.get(i).getCurrentColor(), 0) + 1);
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


    public boolean isOnTurn() {
        return isOnTurn;
    }

    public void setOnTurn(boolean onTurn) {
        isOnTurn = onTurn;
    }

    public int getUseCardTimes() {
        return UseCardTimes;
    }

    public void setUseCardTimes(int useCardTimes) {
        UseCardTimes = useCardTimes;
    }

    public DrawPileAndDiscardPile getDrawCardsAndDiscardPile() {return drawCardsAndDiscardPile;}
}
