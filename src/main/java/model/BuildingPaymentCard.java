package model;

public class BuildingPaymentCard extends ActionCards {
    private final PropertyColor color;

    public BuildingPaymentCard(ActionCardType actionCardType, PropertyColor color) {
        super(actionCardType);
        this.color = color;
    }

    public PropertyColor getColor() {
        return color;
    }
}
