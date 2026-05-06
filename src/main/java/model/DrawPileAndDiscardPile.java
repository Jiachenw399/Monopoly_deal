package model;

import java.util.ArrayList;
import java.util.Collections;

//牌堆 功能完全实现 辛苦修改bug的同学 进行bug测试
public class DrawPileAndDiscardPile {
    private ArrayList<Card> DrawPile;
    private ArrayList<Card> DiscardPile;
    public DrawPileAndDiscardPile() {
        DrawPile = new ArrayList<>();
        DiscardPile = new ArrayList<>();
        addMoneyCards();
        addActionCards();
        addPropertiesCards();
        shuffleDrawCards();
        //创建对象时 更新
    }

    public void shuffleDrawCards() {
        Collections.shuffle(DrawPile);
    }

    public void update(){
        DrawPile.clear();
        addMoneyCards();
        addActionCards();
        addPropertiesCards();
        shuffleDrawCards();
        //游戏结束时 更新牌堆
    }

    public void shuffle(){
        if(DrawPile.isEmpty()){
            DrawPile.addAll(DiscardPile);
            DiscardPile.clear();
            shuffleDrawCards();
        }
    }

    public ArrayList<Card> getDrawPile() {return DrawPile;}

    public ArrayList<Card> getDiscardPile() {return DiscardPile;}

    private void addMoneyCards(){
        int[] moneyValues = {
                1,1,1,1,1,1,  // 6张1
                2,2,2,2,2,    // 5张2
                3,4,3,4,3,4,  // 3张3 + 3张4
                5,5,          // 2张5
                6};           // 1张6
        // 循环添加所有卡牌
        for (int value : moneyValues) {
            DrawPile.add(new MoneyCards(value));
        }
    }

    private void addActionCards(){
        int [] amount={
            3,3,3,3,3,3,3,
            2,2,2,2,2,2,2,2,
            10,
        };

        ActionCardType [] actionCardType={
            ActionCardType.SLY_DEAL,
            ActionCardType.RENT_WITH_MULTIPLE_COLOR,
            ActionCardType.HOUSE,
            ActionCardType.FORCED_DEAL,
            ActionCardType.BIRTHDAY,
            ActionCardType.JUST_SAY_NO,
            ActionCardType.DEBT_COLLECTOR,

            ActionCardType.DOUBLE_THE_RENT,
            ActionCardType.HOTEL,
            ActionCardType.DEAL_BREAKER,
            ActionCardType.RENT_WITH_DARK_BLUE_AND_DARK_GREEN,
            ActionCardType.RENT_WITH_BROWN_AND_LIGHT_BLUE,
            ActionCardType.RENT_WITH_BLACK_AND_LIGHT_GREEN,
            ActionCardType.RENT_WITH_RED_AND_YELLOW,
            ActionCardType.RENT_WITH_ORANGE_AND_PINK,

            ActionCardType.PASS_GO,
        };

        for (int i = 0; i < actionCardType.length; i++) {
            for (int j = 0; j < amount[i]; j++) {
                ActionCards ac = new ActionCards(actionCardType[i]);
                DrawPile.add(ac);
            }
        }
    }

    private void addPropertiesCards() {
        int[] amount = {
                2, // DARK_BLUE
                3, // ORANGE
                4, // BLACK
                3, // RED
                3, // DARK_GREEN
                2, // BROWN
                3, // PINK
                3, // LIGHT_BLUE
                2, // LIGHT_GREEN
                3, // YELLOW

                2, // WILD_PINK_ORANGE
                2, // WILD_RED_YELLOW
                1, // WILD_BLACK_DARK_GREEN
                1, // WILD_BLACK_LIGHT_BLUE
                1, // WILD_BLACK_LIGHT_GREEN
                1, // WILD_LIGHT_BLUE_BROWN
                1, // WILD_DARK_BLUE_DARK_GREEN
                2  // WILD_ALL
        };

        PropertiesCardsType[] types = {
                PropertiesCardsType.DARK_BLUE,
                PropertiesCardsType.ORANGE,
                PropertiesCardsType.BLACK,
                PropertiesCardsType.RED,
                PropertiesCardsType.DARK_GREEN,
                PropertiesCardsType.BROWN,
                PropertiesCardsType.PINK,
                PropertiesCardsType.LIGHT_BLUE,
                PropertiesCardsType.LIGHT_GREEN,
                PropertiesCardsType.YELLOW,

                PropertiesCardsType.WILD_PINK_ORANGE,
                PropertiesCardsType.WILD_RED_YELLOW,
                PropertiesCardsType.WILD_BLACK_DARK_GREEN,
                PropertiesCardsType.WILD_BLACK_LIGHT_BLUE,
                PropertiesCardsType.WILD_BLACK_LIGHT_GREEN,
                PropertiesCardsType.WILD_LIGHT_BLUE_BROWN,
                PropertiesCardsType.WILD_DARK_BLUE_DARK_GREEN,
                PropertiesCardsType.WILD_ALL
        };

        for (int i = 0; i < types.length; i++) {
            for (int j = 0; j < amount[i]; j++) {
                DrawPile.add(new PropertiesCards(types[i]));
            }
        }
    }
}
