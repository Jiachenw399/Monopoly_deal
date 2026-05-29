package model;

public class StandardDeckCardFactory implements DeckCardFactory {
    @Override
    public MoneyCards createMoneyCard(int value) {
        return new MoneyCards(value);
    }

    @Override
    public ActionCards createActionCard(ActionCardType type) {
        return new ActionCards(type);
    }

    @Override
    public PropertiesCards createPropertyCard(PropertiesCardsType type) {
        return new PropertiesCards(type);
    }

    @Override
    public PropertiesCards createPropertyCard(PropertiesCardsType type,
                                             String propertyName,
                                             String imageFileName) {
        return new PropertiesCards(type, propertyName, imageFileName);
    }
}
