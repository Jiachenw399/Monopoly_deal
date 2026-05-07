package model;

import java.util.ArrayList;
import java.util.Collections;

public class DrawPileAndDiscardPile {
    private final ArrayList<Card> drawPile;
    private final ArrayList<Card> discardPile;

    public DrawPileAndDiscardPile() {
        drawPile = new ArrayList<>();
        discardPile = new ArrayList<>();
        resetDeck();
    }

    public void shuffleDrawCards() {
        Collections.shuffle(drawPile);
    }

    public void shuffle() {
        if (!drawPile.isEmpty()) {
            return;
        }

        drawPile.addAll(discardPile);
        discardPile.clear();
        shuffleDrawCards();
    }

    public ArrayList<Card> getDrawPile() {
        return drawPile;
    }

    public ArrayList<Card> getDiscardPile() {
        return discardPile;
    }

    private void resetDeck() {
        drawPile.clear();
        discardPile.clear();

        addMoneyCards();
        addActionCards();
        addPropertiesCards();

        shuffleDrawCards();
    }

    private void addMoneyCards() {
        int[] moneyValues = {
                1, 1, 1, 1, 1, 1,
                2, 2, 2, 2, 2,
                3, 4, 3, 4, 3, 4,
                5, 5,
                6
        };

        for (int value : moneyValues) {
            drawPile.add(new MoneyCards(value));
        }
    }

    private void addActionCards() {
        addActionCardsByType(ActionCardType.SLY_DEAL, 3);
        addActionCardsByType(ActionCardType.RENT_WITH_MULTIPLE_COLOR, 3);
        addActionCardsByType(ActionCardType.HOUSE, 3);
        addActionCardsByType(ActionCardType.FORCED_DEAL, 3);
        addActionCardsByType(ActionCardType.BIRTHDAY, 3);
        addActionCardsByType(ActionCardType.JUST_SAY_NO, 3);
        addActionCardsByType(ActionCardType.DEBT_COLLECTOR, 3);

        addActionCardsByType(ActionCardType.DOUBLE_THE_RENT, 2);
        addActionCardsByType(ActionCardType.HOTEL, 2);
        addActionCardsByType(ActionCardType.DEAL_BREAKER, 2);
        addActionCardsByType(ActionCardType.RENT_WITH_DARK_BLUE_AND_DARK_GREEN, 2);
        addActionCardsByType(ActionCardType.RENT_WITH_BROWN_AND_LIGHT_BLUE, 2);
        addActionCardsByType(ActionCardType.RENT_WITH_BLACK_AND_LIGHT_GREEN, 2);
        addActionCardsByType(ActionCardType.RENT_WITH_RED_AND_YELLOW, 2);
        addActionCardsByType(ActionCardType.RENT_WITH_ORANGE_AND_PINK, 2);

        addActionCardsByType(ActionCardType.PASS_GO, 10);
    }

    private void addActionCardsByType(ActionCardType type, int amount) {
        for (int i = 0; i < amount; i++) {
            drawPile.add(new ActionCards(type));
        }
    }

    private void addPropertiesCards() {
        addPropertiesCardsByType(PropertiesCardsType.DARK_BLUE, 2);
        addPropertiesCardsByType(PropertiesCardsType.ORANGE, 3);
        addPropertiesCardsByType(PropertiesCardsType.BLACK, 4);
        addPropertiesCardsByType(PropertiesCardsType.RED, 3);
        addPropertiesCardsByType(PropertiesCardsType.DARK_GREEN, 3);
        addPropertiesCardsByType(PropertiesCardsType.BROWN, 2);
        addPropertiesCardsByType(PropertiesCardsType.PINK, 3);
        addPropertiesCardsByType(PropertiesCardsType.LIGHT_BLUE, 3);
        addPropertiesCardsByType(PropertiesCardsType.LIGHT_GREEN, 2);
        addPropertiesCardsByType(PropertiesCardsType.YELLOW, 3);

        addPropertiesCardsByType(PropertiesCardsType.WILD_PINK_ORANGE, 2);
        addPropertiesCardsByType(PropertiesCardsType.WILD_RED_YELLOW, 2);
        addPropertiesCardsByType(PropertiesCardsType.WILD_BLACK_DARK_GREEN, 1);
        addPropertiesCardsByType(PropertiesCardsType.WILD_BLACK_LIGHT_BLUE, 1);
        addPropertiesCardsByType(PropertiesCardsType.WILD_BLACK_LIGHT_GREEN, 1);
        addPropertiesCardsByType(PropertiesCardsType.WILD_LIGHT_BLUE_BROWN, 1);
        addPropertiesCardsByType(PropertiesCardsType.WILD_DARK_BLUE_DARK_GREEN, 1);
        addPropertiesCardsByType(PropertiesCardsType.WILD_ALL, 2);
    }

    private void addPropertiesCardsByType(PropertiesCardsType type, int amount) {
        for (int i = 0; i < amount; i++) {
            drawPile.add(new PropertiesCards(type));
        }
    }
}