package model;

public class PropertiesCards extends Card {
    private PropertyColor currentColor;
    private final PropertiesCardsType type;
    private final String propertyName;
    private final String imageFileName;
    private boolean hasHouse;
    private boolean hasHotel;

    public PropertiesCards(PropertiesCardsType type) {
        this(type, type.name(), type.name().toLowerCase() + ".png");
    }

    public PropertiesCards(PropertiesCardsType type, String propertyName, String imageFileName) {
        this.type = type;
        this.propertyName = propertyName;
        this.imageFileName = imageFileName;
        this.currentColor = getDefaultColor(type);
        this.hasHouse = false;
        this.hasHotel = false;
    }

    private PropertyColor getDefaultColor(PropertiesCardsType type) {
        if (type.getColors().size() == 1) {
            return type.getColors().get(0);
        }

        return null;
    }

    public PropertiesCardsType getType() {
        return type;
    }

    public static String getShortColorName(PropertyColor color) {
        return switch (color) {
            case DARK_BLUE -> "D.BLUE";
            case ORANGE -> "ORANGE";
            case BLACK -> "BLACK";
            case RED -> "RED";
            case DARK_GREEN -> "D.GREEN";
            case BROWN -> "BROWN";
            case PINK -> "PINK";
            case LIGHT_BLUE -> "L.BLUE";
            case LIGHT_GREEN -> "L.GREEN";
            case YELLOW -> "YELLOW";
        };
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public PropertyColor getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(PropertyColor currentColor) {
        this.currentColor = currentColor;
    }

    @Override
    public int getValue() {
        return type.getValue();
    }

    public boolean isWildCard() {
        return type.getColors().size() > 1;
    }

    public boolean hasHouse() {
        return hasHouse;
    }

    public void setHasHouse(boolean hasHouse) {
        this.hasHouse = hasHouse;
    }

    public boolean hasHotel() {
        return hasHotel;
    }

    public void setHasHotel(boolean hasHotel) {
        this.hasHotel = hasHotel;
    }
}
