package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class Player {

    private ArrayList<Card> HandCards;
    private ArrayList<PropertiesCards> PropertyCards;
    private ArrayList<Card> BankCards;
    private boolean isOnTurn;
    private DrawPileAndDiscardPile drawCardsAndDiscardPile;
    private int UseCardTimes;
    private ArrayList<Player> Enemy;
    static int bestSum = Integer.MAX_VALUE;
    static List<Integer> best = new ArrayList<>();

    public void setHandCards(ArrayList<Card> handCards) {
        HandCards = handCards;
    }

    public void setPropertyCards(ArrayList<PropertiesCards> propertyCards) {
        PropertyCards = propertyCards;
    }

    public void setBankCards(ArrayList<Card> bankCards) {
        BankCards = bankCards;
    }

    public void setDrawCardsAndDiscardPile(DrawPileAndDiscardPile drawCardsAndDiscardPile) {
        this.drawCardsAndDiscardPile = drawCardsAndDiscardPile;
    }

    public void setEnemy(ArrayList<Player> enemy) {
        Enemy = enemy;
    }

    public static int getBestSum() {
        return bestSum;
    }

    public static void setBestSum(int bestSum) {
        Player.bestSum = bestSum;
    }

    public static List<Integer> getBest() {
        return best;
    }

    public static void setBest(List<Integer> best) {
        Player.best = best;
    }



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

    public void takeMoney(int number,Player player) {
        ArrayList<Card> cards = player.getBankCards();
        int totalMoney = 0;
        for(Card card : cards){
            totalMoney += card.getValue();
        }
//        int [] eachMoney = new int[cards.size()];
//        for (int i = 0; i < cards.size(); i++) {
//            totalMoney += cards.get(i).getValue();
//            eachMoney[i] = cards.get(i).getValue();
//        }
        if (totalMoney <= number) {
            BankCards.addAll(player.getBankCards());
            player.getBankCards().clear();
            return;
        }
        ArrayList<Card> bestCombination = new ArrayList<>();
        int bestSum = Integer.MAX_VALUE;
        int n = cards.size();
        for(int mask = 1; mask < (1<<n); mask++){
            ArrayList<Card> com = new ArrayList<>();//the current combination
            int currentSum = 0;
            for(int i=0; i<n; i++){
                if ((mask & (1 << i)) != 0) {
                    Card card = cards.get(i);
                    com.add(card);
                    currentSum += card.getValue();
                }
            }
            if(currentSum>=number){
                if(currentSum < bestSum){
                    bestSum = currentSum;
                    bestCombination = com;
                }
            }
        }
        BankCards.addAll(bestCombination);
        cards.removeAll(bestCombination);
        //TODO：待完善，还不能用户选择组合给牌
    }

    public void putCard(Card card) {
        switch (card) {
            case MoneyCards moneyCards -> putMoneyCard(moneyCards);
            case PropertiesCards propertiesCards -> putPropertyCard(propertiesCards);
            case ActionCards actionCards -> putActionCard(actionCards);
            case null, default -> {
                return;
            }
        }
        UseCardTimes++;
    }//合并一下出牌功能

    public void putMoneyCard(Card card) {
        if(card.getClass().equals(PropertiesCards.class)){
            return;
        }
        HandCards.remove(card);
        BankCards.add(card);
    }//用钱 对应规则A

    public PropertyColor choosePropertyColorForWildCards(PropertiesCards card) {
        ArrayList<PropertyColor> colorsCanBeUsed = card.getType().getColors();
        int i = 1;
        for (PropertyColor color : colorsCanBeUsed) {
            System.out.println(i+color.toString());
            i+=1;
        }
        Scanner input = new Scanner(System.in);
        int choice = input.nextInt();
        System.out.println("What is your property color?");
        if(choice<=0 || choice >=colorsCanBeUsed.size()){
            System.out.println("Invalid choice.");
            return choosePropertyColorForWildCards(card);
        }else{
            card.setCurrentColor(colorsCanBeUsed.get(choice-1));
            return colorsCanBeUsed.get(choice-1);
        }
    }

    public void putPropertyCard(PropertiesCards card) {
        HandCards.remove(card);
        if (card.isWildCard() && card.getCurrentColor() == null) {
            card.setCurrentColor(card.getType().getColors().getFirst());
        }
        PropertyCards.add(card);
    }

    //在这个下面粘贴 跟其他方法对齐
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
        if (payer == null || amount <= 0) {
            return;
        }
    
        int paid = 0;
    
        while (!payer.getBankCards().isEmpty() && paid < amount) {
            Card card = payer.getBankCards().remove(0);
            BankCards.add(card);
            paid += card.getValue();
        }
    
        while (!payer.getPropertyCards().isEmpty() && paid < amount) {
            PropertiesCards card = payer.getPropertyCards().remove(0);
            PropertyCards.add(card);
            paid += card.getValue();
        }
    }
    
    private PropertiesCards findFirstPropertyThatCanBeStolen(Player target) {
        for (PropertiesCards card : target.getPropertyCards()) {
            PropertyColor color = card.getCurrentColor();
    
            if (color == null) {
                return card;
            }
    
            if (!target.isCompleteSet(color)) {
                return card;
            }
        }
    
        return null;
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
//
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
                takeCard(1);
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
                BankCards.add(card);
                drawCardsAndDiscardPile.getDiscardPile().remove(card);
                break;

            default:
                break;
        }
    }

    

    private void steal(Player p, PropertiesCards prop){
        p.getPropertyCards().remove(prop);
        this.PropertyCards.add(prop);
    }//用于偷一个牌

    private void steal(Player p, ArrayList<PropertiesCards> props){
        p.getPropertyCards().removeAll(props);
        this.PropertyCards.addAll(props);
    }//用于偷一组牌

    public ArrayList<Card> getHandCards() {return HandCards;}

    public ArrayList<PropertiesCards> getPropertyCards() {return PropertyCards;}

    public ArrayList<Card> getBankCards() {return BankCards;}

    public DrawPileAndDiscardPile getDrawCardsAndDiscardPile() {return drawCardsAndDiscardPile;}

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
            // 关键：判断是否达到该颜色需要的数量
            if (count >= color.getAmountToCompleteSet()) {
                completedSets++;
            }
        }
        // Monopoly Deal规则：3套获胜
        return completedSets >= 3;
    }//这个方法 大概没问题了


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

    public void OnTurn() {
        // 抽牌阶段
        if (HandCards.isEmpty()) {
            takeCard(5);
        } else {
            takeCard(2);
        }

        isOnTurn = true;
        UseCardTimes = 0;  // 重置本回合打牌计数

        // 打牌阶段：最多打出3张卡（依赖GUI Listener选择）
        while (UseCardTimes < 3 && !HandCards.isEmpty()) {
            // TODO: GUI通过GameListener传入玩家选择的卡牌
            // Card selectedCard = listener.getSelectedCard();

        /*
        if (selectedCard != null) {
            if (selectedCard instanceof MoneyCards) {
                putMoneyCard(selectedCard);
            } else if (selectedCard instanceof PropertiesCards) {
                putPropertyCard((PropertiesCards) selectedCard);
            } else if (selectedCard instanceof ActionCards) {
                putActionCard((ActionCards) selectedCard);
            }

            // 打牌后立即检查胜利
            if (checkIfWin(PropertyCards)) {
                // 可在此通知Game结束游戏
                break;
            }
        } else {
            break;  // 玩家选择结束回合
        }
        */
        }

        // 回合结束：手牌超过7张需弃牌（规则要求）
        while (HandCards.size() > 7) {
            // TODO: GUI选择要弃的牌
            // Card discard = ...;
            // HandCards.remove(discard);
            // drawCardsAndDiscardPile.getDiscardPile().add(discard);
        }

        isOnTurn = false;
        UseCardTimes = 0;
    }

    public void printAllCardsOfHands(){
        int i = 1;
        System.out.println("To end a turn, please enter 0");
        for (Card handCard : HandCards) {
            if (handCard instanceof MoneyCards) {
                System.out.println(i+". Money Card: " + handCard.getValue()+ "M$ ");
            }else if(handCard instanceof PropertiesCards){
                System.out.println(i+". Properties Card: " + handCard.getValue()+ "M$ "+((PropertiesCards) handCard).getType());
            }else if(handCard instanceof ActionCards){
                System.out.println(i+". Action Card: "+ handCard.getValue()+ "M$ "+((ActionCards) handCard).getActionCardType());
            }
            i+=1;
        }
    }

    public void printAllCardsOfEnemy(){
        for (Player player : Enemy) {
            for (int i1 = 0; i1 < player.getBankCards().size(); i1++) {
                System.out.println("Money Card: " + player.getBankCards().get(i1).getValue()+ "M$");
            }
            for (int i1 = 0; i1 < player.getPropertyCards().size(); i1++) {
                PropertiesCards propertyCards = (PropertiesCards) player.getPropertyCards().get(i1);
                System.out.println("Properties Card: " + propertyCards.getValue()+ "M$"+propertyCards.getType());
            }
        }
    }

    public int chooseHandCard(){
        Scanner sc  = new Scanner(System.in);
        printAllCardsOfHands();
        System.out.println("Please enter the number of a card to use");
        return sc.nextInt();
    }
}
