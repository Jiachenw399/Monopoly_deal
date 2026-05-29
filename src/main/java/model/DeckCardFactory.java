package model;

public interface DeckCardFactory {
    MoneyCards createMoneyCard(int value);

    ActionCards createActionCard(ActionCardType type);

    PropertiesCards createPropertyCard(PropertiesCardsType type);

    PropertiesCards createPropertyCard(PropertiesCardsType type, String propertyName, String imageFileName);
}
