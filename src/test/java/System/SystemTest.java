package System;

import logic.Game;
import model.ActionCardType;
import model.ActionCards;
import model.Card;
import model.DrawPileAndDiscardPile;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;
import model.PropertiesCardsType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemTest {
    @Test
    public void testFivePlayerGameStartsWithRequiredInitialCardCoverage() {
        DrawPileAndDiscardPile preparedDrawPile = createPreparedOpeningDeck();
        Game game = new Game(5);

        game.startGame(5, preparedDrawPile);

        ArrayList<Player> players = game.getPlayers();
        List<Card> openingCards = collectOpeningCards(players);
        EnumSet<ActionCardType> actionTypesInOpeningHands = collectActionTypes(openingCards);

        assertEquals(5, players.size());
        for (Player player : players) {
            assertEquals(5, player.getHandCards().size());
        }
        assertEquals(25, openingCards.size());
        assertTrue(openingCards.stream().anyMatch(card -> card instanceof MoneyCards));
        assertTrue(openingCards.stream().anyMatch(this::isNormalPropertyCard));
        assertTrue(actionTypesInOpeningHands.containsAll(Arrays.asList(ActionCardType.values())));
    }

    private DrawPileAndDiscardPile createPreparedOpeningDeck() {
        DrawPileAndDiscardPile drawPile = new DrawPileAndDiscardPile();
        ArrayList<Card> cards = drawPile.getDrawPile();
        cards.clear();

        cards.add(new MoneyCards(1));
        cards.add(new PropertiesCards(PropertiesCardsType.BROWN, "Mediterranean Avenue", "mediterranean_avenue.png"));
        cards.add(new ActionCards(ActionCardType.SLY_DEAL));
        cards.add(new ActionCards(ActionCardType.DEAL_BREAKER));
        cards.add(new ActionCards(ActionCardType.FORCED_DEAL));

        cards.add(new ActionCards(ActionCardType.DEBT_COLLECTOR));
        cards.add(new ActionCards(ActionCardType.RENT_WITH_RED_AND_YELLOW));
        cards.add(new ActionCards(ActionCardType.RENT_WITH_ORANGE_AND_PINK));
        cards.add(new ActionCards(ActionCardType.RENT_WITH_BROWN_AND_LIGHT_BLUE));
        cards.add(new ActionCards(ActionCardType.RENT_WITH_BLACK_AND_LIGHT_GREEN));

        cards.add(new ActionCards(ActionCardType.RENT_WITH_DARK_BLUE_AND_DARK_GREEN));
        cards.add(new ActionCards(ActionCardType.RENT_WITH_MULTIPLE_COLOR));
        cards.add(new ActionCards(ActionCardType.DOUBLE_THE_RENT));
        cards.add(new ActionCards(ActionCardType.HOUSE));
        cards.add(new ActionCards(ActionCardType.HOTEL));

        cards.add(new ActionCards(ActionCardType.JUST_SAY_NO));
        cards.add(new ActionCards(ActionCardType.BIRTHDAY));
        cards.add(new ActionCards(ActionCardType.PASS_GO));
        cards.add(new MoneyCards(2));
        cards.add(new PropertiesCards(PropertiesCardsType.LIGHT_BLUE, "Oriental Avenue", "oriental_avenue.png"));

        cards.add(new MoneyCards(3));
        cards.add(new PropertiesCards(PropertiesCardsType.RED, "Kentucky Avenue", "kentucky_avenue.png"));
        cards.add(new MoneyCards(4));
        cards.add(new PropertiesCards(PropertiesCardsType.YELLOW, "Atlantic Avenue", "atlantic_avenue.png"));
        cards.add(new PropertiesCards(PropertiesCardsType.BLACK, "Reading Railroad", "reading_railroad.png"));

        return drawPile;
    }

    private List<Card> collectOpeningCards(ArrayList<Player> players) {
        ArrayList<Card> cards = new ArrayList<>();
        for (Player player : players) {
            cards.addAll(player.getHandCards());
        }
        return cards;
    }

    private EnumSet<ActionCardType> collectActionTypes(List<Card> cards) {
        EnumSet<ActionCardType> actionTypes = EnumSet.noneOf(ActionCardType.class);
        for (Card card : cards) {
            if (card instanceof ActionCards actionCard) {
                actionTypes.add(actionCard.getActionCardType());
            }
        }
        return actionTypes;
    }

    private boolean isNormalPropertyCard(Card card) {
        return card instanceof PropertiesCards propertyCard && !propertyCard.isWildCard();
    }
}
