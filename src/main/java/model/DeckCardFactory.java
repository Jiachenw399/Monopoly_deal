package model;

public interface DeckCardFactory {
    // Creates a money card.
    MoneyCards createMoneyCard(int value);

    // Creates an action card.
    ActionCards createActionCard(ActionCardType type);

    // Creates a property card.
    PropertiesCards createPropertyCard(PropertiesCardsType type);

    // Creates a property card with custom display metadata.
    PropertiesCards createPropertyCard(PropertiesCardsType type, String propertyName, String imageFileName);
}
