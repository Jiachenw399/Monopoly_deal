package model;

public class StandardDeckCardFactory implements DeckCardFactory {
    // Creates money card.
    @Override
    public MoneyCards createMoneyCard(int value) {
        return new MoneyCards(value);
    }

    // Creates action card.
    @Override
    public ActionCards createActionCard(ActionCardType type) {
        return new ActionCards(type);
    }

    // Creates property card.
    @Override
    public PropertiesCards createPropertyCard(PropertiesCardsType type) {
        return new PropertiesCards(type);
    }

    // Creates property card.
    @Override
    public PropertiesCards createPropertyCard(PropertiesCardsType type,
                                             String propertyName,
                                             String imageFileName) {
        return new PropertiesCards(type, propertyName, imageFileName);
    }
}
