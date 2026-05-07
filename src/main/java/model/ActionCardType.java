package model;

public enum ActionCardType {
    SLY_DEAL(3),
    DEAL_BREAKER(5),
    FORCED_DEAL(3),

    DEBT_COLLECTOR(3),

    RENT_WITH_RED_AND_YELLOW(1),
    RENT_WITH_ORANGE_AND_PINK(1),
    RENT_WITH_BROWN_AND_LIGHT_BLUE(1),
    RENT_WITH_BLACK_AND_LIGHT_GREEN(1),
    RENT_WITH_DARK_BLUE_AND_DARK_GREEN(1),
    RENT_WITH_MULTIPLE_COLOR(3),
    DOUBLE_THE_RENT(1),

    HOUSE(3),
    HOTEL(4),

    JUST_SAY_NO(4),
    BIRTHDAY(2),
    PASS_GO(1);

    private final int typeValue;

    ActionCardType(int typeValue) {
        this.typeValue = typeValue;
    }

    public int getTypeValue() {
        return typeValue;
    }
}