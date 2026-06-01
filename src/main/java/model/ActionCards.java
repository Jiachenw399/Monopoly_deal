package model;

public class ActionCards extends Card {
    private final ActionCardType actionCardType;

    // Creates a ActionCards instance.
    public ActionCards(ActionCardType actionCardType) {
        this.actionCardType = actionCardType;
        setValue(actionCardType.getTypeValue());
    }

    public ActionCardType getActionCardType() {
        return actionCardType;
    }
}
