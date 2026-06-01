package model;

public class PropertiesCards extends Card {
    private PropertyColor currentColor;
    private final PropertiesCardsType type;
    private final String propertyName;
    private final String imageFileName;
    private boolean hasHouse;
    private boolean hasHotel;

    // Creates a PropertiesCards instance.
    public PropertiesCards(PropertiesCardsType type) {
        this(type, type.name(), type.name().toLowerCase() + ".png");
    }

    // Creates a PropertiesCards instance.
    public PropertiesCards(PropertiesCardsType type, String propertyName, String imageFileName) {
        this.type = type;
        this.propertyName = propertyName;
        this.imageFileName = imageFileName;
        this.currentColor = getDefaultColor(type);
        this.hasHouse = false;
        this.hasHotel = false;
    }

    // Finds default color.
    private PropertyColor getDefaultColor(PropertiesCardsType type) {
        if (type.getColors().size() == 1) {
            return type.getColors().get(0);
        }

        return null;
    }

    public PropertiesCardsType getType() {
        return type;
    }

    // Finds short color name.
    public static String getShortColorName(PropertyColor color) {
        return color.getShortName();
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

    // Finds value.
    @Override
    public int getValue() {
        return type.getValue();
    }

    // Checks whether wild card.
    public boolean isWildCard() {
        return type.getColors().size() > 1;
    }

    // Checks whether this has house.
    public boolean hasHouse() {
        return hasHouse;
    }

    public void setHasHouse(boolean hasHouse) {
        this.hasHouse = hasHouse;
    }

    // Checks whether this has hotel.
    public boolean hasHotel() {
        return hasHotel;
    }

    public void setHasHotel(boolean hasHotel) {
        this.hasHotel = hasHotel;
    }
}
