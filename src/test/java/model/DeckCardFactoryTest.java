package model;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DeckCardFactoryTest {
    private static class CountingDeckCardFactory implements DeckCardFactory {
        private int moneyCardsCreated;
        private int actionCardsCreated;
        private int propertyCardsCreated;

        @Override
        public MoneyCards createMoneyCard(int value) {
            moneyCardsCreated++;
            return new MoneyCards(value);
        }

        @Override
        public ActionCards createActionCard(ActionCardType type) {
            actionCardsCreated++;
            return new ActionCards(type);
        }

        @Override
        public PropertiesCards createPropertyCard(PropertiesCardsType type) {
            propertyCardsCreated++;
            return new PropertiesCards(type);
        }

        @Override
        public PropertiesCards createPropertyCard(PropertiesCardsType type,
                                                 String propertyName,
                                                 String imageFileName) {
            propertyCardsCreated++;
            return new PropertiesCards(type, propertyName, imageFileName);
        }
    }

    @Test
    public void testDrawPileUsesInjectedCardFactory() {
        CountingDeckCardFactory factory = new CountingDeckCardFactory();

        new DrawPileAndDiscardPile(factory);

        assertTrue(factory.moneyCardsCreated > 0);
        assertTrue(factory.actionCardsCreated > 0);
        assertTrue(factory.propertyCardsCreated > 0);
    }
}
