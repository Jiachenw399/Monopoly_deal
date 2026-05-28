package model;

public class CardFactory {

    public static MoneyCards createMoneyCard(int value) {
        return new MoneyCards(value);
    }

    public static ActionCards createActionCard(ActionCardType type) {
        return new ActionCards(type);
    }

    public static PropertiesCards createPropertyCard(PropertiesCardsType type) {
        return new PropertiesCards(type);
    }

    public static PropertiesCards createPropertyCard(PropertiesCardsType type, String propertyName, String imageFileName) {
        return new PropertiesCards(type, propertyName, imageFileName);
    }
}