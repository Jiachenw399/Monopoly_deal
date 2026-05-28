package model;

public enum PropertyColor {
    DARK_BLUE(2),
    ORANGE(3),
    BLACK(4),
    RED(3),
    DARK_GREEN(3),
    BROWN(2),
    PINK(3),
    LIGHT_BLUE(3),
    LIGHT_GREEN(2),
    YELLOW(3);

    private final int amountToCompleteSet;

    PropertyColor(int amountToCompleteSet) {
        this.amountToCompleteSet = amountToCompleteSet;
    }

    public int getAmountToCompleteSet() {
        return amountToCompleteSet;
    }
}