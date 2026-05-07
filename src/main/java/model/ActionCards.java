package model;

public class ActionCards extends Card {
    private final ActionCardType actionCardType;

    public ActionCards(ActionCardType actionCardType) {
        this.actionCardType = actionCardType;
        setValue(actionCardType.getTypeValue());
    }

    public ActionCardType getActionCardType() {
        return actionCardType;
    }
}