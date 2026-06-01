package model;

import java.util.ArrayList;
import java.util.List;

public enum ActionCardType {
    SLY_DEAL(3),
    DEAL_BREAKER(5),
    FORCED_DEAL(3),

    DEBT_COLLECTOR(3),

    RENT_WITH_RED_AND_YELLOW(1, PropertyColor.RED, PropertyColor.YELLOW),
    RENT_WITH_ORANGE_AND_PINK(1, PropertyColor.ORANGE, PropertyColor.PINK),
    RENT_WITH_BROWN_AND_LIGHT_BLUE(1, PropertyColor.BROWN, PropertyColor.LIGHT_BLUE),
    RENT_WITH_BLACK_AND_LIGHT_GREEN(1, PropertyColor.BLACK, PropertyColor.LIGHT_GREEN),
    RENT_WITH_DARK_BLUE_AND_DARK_GREEN(1, PropertyColor.DARK_BLUE, PropertyColor.DARK_GREEN),
    RENT_WITH_MULTIPLE_COLOR(3),
    DOUBLE_THE_RENT(1),

    HOUSE(3),
    HOTEL(4),

    JUST_SAY_NO(4),
    BIRTHDAY(2),
    PASS_GO(1);

    private final int typeValue;
    private final List<PropertyColor> rentColors;

    ActionCardType(int typeValue, PropertyColor... rentColors) {
        this.typeValue = typeValue;
        this.rentColors = List.of(rentColors);
    }

    public int getTypeValue() {
        return typeValue;
    }

    // Checks whether two color rent card.
    public boolean isTwoColorRentCard() {
        return rentColors.size() == 2;
    }

    // Finds rent colors.
    public ArrayList<PropertyColor> getRentColors() {
        return new ArrayList<>(rentColors);
    }
}
