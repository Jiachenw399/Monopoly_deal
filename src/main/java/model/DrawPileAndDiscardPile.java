package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class DrawPileAndDiscardPile {
    private ArrayList<Card> DrawPile;
    private ArrayList<Card> DiscardPile;
    private final DeckCardFactory cardFactory;

    public DrawPileAndDiscardPile() {
        this(new StandardDeckCardFactory());
    }

    public DrawPileAndDiscardPile(DeckCardFactory cardFactory) {
        this.cardFactory = Objects.requireNonNull(cardFactory);
        DrawPile = new ArrayList<>();
        DiscardPile = new ArrayList<>();
        addMoneyCards();
        addActionCards();
        addPropertiesCards();
        shuffleDrawCards();
    }

    public void shuffleDrawCards() {
        Collections.shuffle(DrawPile);
    }

    public void shuffle() {
        if (DrawPile.isEmpty()) {
            DrawPile.addAll(DiscardPile);
            DiscardPile.clear();
            shuffleDrawCards();
        }
    }

    public ArrayList<Card> getDrawPile() {
        return DrawPile;
    }

    public ArrayList<Card> getDiscardPile() {
        return DiscardPile;
    }

    private void addMoneyCards() {
        int[] moneyValues = {
                1, 1, 1, 1, 1, 1,
                2, 2, 2, 2, 2,
                3, 4, 3, 4, 3, 4,
                5, 5,
                10
        };

        for (int value : moneyValues) {
            DrawPile.add(cardFactory.createMoneyCard(value));
        }
    }

    private void addActionCards() {
        int[] amount = {
                3, 3, 3, 3, 3, 3, 3,
                2, 2, 2, 2, 2, 2, 2, 2,
                10
        };

        ActionCardType[] actionCardType = {
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

                ActionCardType.PASS_GO
        };

        for (int i = 0; i < actionCardType.length; i++) {
            for (int j = 0; j < amount[i]; j++) {
                DrawPile.add(cardFactory.createActionCard(actionCardType[i]));
            }
        }
    }

    private void addPropertiesCards() {
        addNormalPropertyCards();
        addPropertyWildCards();
    }

    private void addNormalPropertyCards() {
        addPropertyCard(PropertiesCardsType.DARK_BLUE, "Boardwalk", "boardwalk.png");
        addPropertyCard(PropertiesCardsType.DARK_BLUE, "Park Place", "park_place.png");

        addPropertyCard(PropertiesCardsType.BROWN, "Baltic Avenue", "baltic_avenue.png");
        addPropertyCard(PropertiesCardsType.BROWN, "Mediterranean Avenue", "mediterranean_avenue.png");

        addPropertyCard(PropertiesCardsType.LIGHT_BLUE, "Connecticut Avenue", "connecticut_avenue.png");
        addPropertyCard(PropertiesCardsType.LIGHT_BLUE, "Oriental Avenue", "oriental_avenue.png");
        addPropertyCard(PropertiesCardsType.LIGHT_BLUE, "Vermont Avenue", "vermont_avenue.png");

        addPropertyCard(PropertiesCardsType.PINK, "St. Charles Place", "st_charles_place.png");
        addPropertyCard(PropertiesCardsType.PINK, "States Avenue", "states_avenue.png");
        addPropertyCard(PropertiesCardsType.PINK, "Virginia Avenue", "virginia_avenue.png");

        addPropertyCard(PropertiesCardsType.ORANGE, "New York Avenue", "new_york_avenue.png");
        addPropertyCard(PropertiesCardsType.ORANGE, "St. James Place", "st_james_place.png");
        addPropertyCard(PropertiesCardsType.ORANGE, "Tennessee Avenue", "tennessee_avenue.png");

        addPropertyCard(PropertiesCardsType.RED, "Illinois Avenue", "illinois_avenue.png");
        addPropertyCard(PropertiesCardsType.RED, "Indiana Avenue", "indiana_avenue.png");
        addPropertyCard(PropertiesCardsType.RED, "Kentucky Avenue", "kentucky_avenue.png");

        addPropertyCard(PropertiesCardsType.YELLOW, "Atlantic Avenue", "atlantic_avenue.png");
        addPropertyCard(PropertiesCardsType.YELLOW, "Marvin Gardens", "marvin_gardens.png");
        addPropertyCard(PropertiesCardsType.YELLOW, "Ventnor Avenue", "ventnor_avenue.png");

        addPropertyCard(PropertiesCardsType.DARK_GREEN, "North Carolina Avenue", "north_carolina_avenue.png");
        addPropertyCard(PropertiesCardsType.DARK_GREEN, "Pacific Avenue", "pacific_avenue.png");
        addPropertyCard(PropertiesCardsType.DARK_GREEN, "Pennsylvania Avenue", "pennsylvania_avenue.png");

        addPropertyCard(PropertiesCardsType.BLACK, "B&O Railroad", "b_and_o_railroad.png");
        addPropertyCard(PropertiesCardsType.BLACK, "Pennsylvania Railroad", "pennsylvania_railroad.png");
        addPropertyCard(PropertiesCardsType.BLACK, "Reading Railroad", "reading_railroad.png");
        addPropertyCard(PropertiesCardsType.BLACK, "Short Line", "short_line.png");

        addPropertyCard(PropertiesCardsType.LIGHT_GREEN, "Electric Company", "electric_company.png");
        addPropertyCard(PropertiesCardsType.LIGHT_GREEN, "Water Works", "water_works.png");
    }

    private void addPropertyWildCards() {
        addSeveralPropertyCards(PropertiesCardsType.WILD_PINK_ORANGE, 2);
        addSeveralPropertyCards(PropertiesCardsType.WILD_RED_YELLOW, 2);
        addSeveralPropertyCards(PropertiesCardsType.WILD_BLACK_DARK_GREEN, 1);
        addSeveralPropertyCards(PropertiesCardsType.WILD_BLACK_LIGHT_BLUE, 1);
        addSeveralPropertyCards(PropertiesCardsType.WILD_BLACK_LIGHT_GREEN, 1);
        addSeveralPropertyCards(PropertiesCardsType.WILD_LIGHT_BLUE_BROWN, 1);
        addSeveralPropertyCards(PropertiesCardsType.WILD_DARK_BLUE_DARK_GREEN, 1);
        addSeveralPropertyCards(PropertiesCardsType.WILD_ALL, 2);
    }

    private void addPropertyCard(PropertiesCardsType type, String propertyName, String imageFileName) {
        DrawPile.add(cardFactory.createPropertyCard(type, propertyName, imageFileName));
    }

    private void addSeveralPropertyCards(PropertiesCardsType type, int amount) {
        for (int i = 0; i < amount; i++) {
            DrawPile.add(cardFactory.createPropertyCard(type));
        }
    }
}
