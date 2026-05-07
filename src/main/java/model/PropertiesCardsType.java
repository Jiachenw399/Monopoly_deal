package model;

import java.util.ArrayList;
import java.util.List;

public enum PropertiesCardsType {
    DARK_BLUE(4, PropertyColor.DARK_BLUE),
    ORANGE(2, PropertyColor.ORANGE),
    BLACK(2, PropertyColor.BLACK),
    RED(3, PropertyColor.RED),
    DARK_GREEN(4, PropertyColor.DARK_GREEN),
    BROWN(1, PropertyColor.BROWN),
    PINK(2, PropertyColor.PINK),
    LIGHT_BLUE(1, PropertyColor.LIGHT_BLUE),
    LIGHT_GREEN(2, PropertyColor.LIGHT_GREEN),
    YELLOW(3, PropertyColor.YELLOW),

    WILD_PINK_ORANGE(2, PropertyColor.PINK, PropertyColor.ORANGE),
    WILD_RED_YELLOW(3, PropertyColor.RED, PropertyColor.YELLOW),
    WILD_BLACK_DARK_GREEN(4, PropertyColor.BLACK, PropertyColor.DARK_GREEN),
    WILD_BLACK_LIGHT_BLUE(4, PropertyColor.BLACK, PropertyColor.LIGHT_BLUE),
    WILD_BLACK_LIGHT_GREEN(2, PropertyColor.BLACK, PropertyColor.LIGHT_GREEN),
    WILD_LIGHT_BLUE_BROWN(1, PropertyColor.LIGHT_BLUE, PropertyColor.BROWN),
    WILD_DARK_BLUE_DARK_GREEN(4, PropertyColor.DARK_BLUE, PropertyColor.DARK_GREEN),
    WILD_ALL(0, PropertyColor.values());

    private final int value;
    private final ArrayList<PropertyColor> colors;

    PropertiesCardsType(int value, PropertyColor... colors) {
        this.value = value;
        this.colors = new ArrayList<>(List.of(colors));
    }

    public int getValue() {
        return value;
    }

    public ArrayList<PropertyColor> getColors() {
        return new ArrayList<>(colors);
    }
}