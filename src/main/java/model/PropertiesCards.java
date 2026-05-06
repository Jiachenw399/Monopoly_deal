package model;

public class PropertiesCards extends Card {
    private PropertyColor currentColor;
    private PropertiesCardsType type;
    private boolean hasHouse;
    private boolean hasHotel;

    public PropertiesCards(PropertiesCardsType type) {
        this.type = type;
        this.hasHouse = false;
        this.hasHotel = false;

        if (type.getColors().size() == 1) {
            this.currentColor = type.getColors().get(0);
        } else {
            this.currentColor = null;
        }
    }

    public PropertiesCardsType getType() {
        return type;
    }

    public PropertyColor getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(PropertyColor currentColor) {
        this.currentColor = currentColor;
    }

    public void setType(PropertiesCardsType type) {
        this.type = type;
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