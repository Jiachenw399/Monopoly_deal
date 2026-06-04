package network;

import GUI.GameScreen;
import logic.Game;
import model.ActionCardType;
import model.ActionCards;
import model.BuildingPaymentCard;
import model.Card;
import model.DrawPileAndDiscardPile;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;
import model.PropertiesCardsType;
import model.PropertyColor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

final class NetworkSymmetricTestFixtures {
    private NetworkSymmetricTestFixtures() {
    }

    static Game twoPlayerGame() {
        return new Game(2);
    }

    static Game gameWithActivePayment(Player receiver, Player payer, int amount) {
        Game game = twoPlayerGame();
        ArrayList<Player> players = new ArrayList<>();
        players.add(receiver);
        players.add(payer);
        game.applyOnlineState(
                players,
                0,
                false,
                new Game.PaymentRequest(receiver, payer, amount),
                false
        );
        return game;
    }

    static Player player(DrawPileAndDiscardPile drawPile, String name) {
        return new Player(drawPile, name);
    }

    static OnlineGameClickActions onlineEncoder(Game game, int myPlayerId) {
        GameScreen gameScreen = new GameScreen(game);
        OnlineGameClickActions actions = new OnlineGameClickActions(
                game,
                gameScreen,
                (type, body) -> { },
                () -> { }
        );
        actions.setMyPlayerId(myPlayerId);
        return actions;
    }

    static OnlineGameClickActions capturingOnlineActions(
            Game game,
            int myPlayerId,
            List<String[]> capturedMessages) {
        GameScreen gameScreen = new GameScreen(game);
        BiConsumer<String, String> send = (type, body) -> capturedMessages.add(new String[] { type, body });
        OnlineGameClickActions actions = new OnlineGameClickActions(game, gameScreen, send, () -> { });
        actions.setMyPlayerId(myPlayerId);
        return actions;
    }

    static GameServer serverBoundTo(Game game) {
        GameServer server = new GameServer();
        server.bindGameForTest(game);
        return server;
    }

    static void assertDecodedPaymentMatches(
            ArrayList<Card> expected,
            ArrayList<Card> decoded) {
        if (expected.size() != decoded.size()) {
            throw new AssertionError("Expected " + expected.size() + " cards but decoded " + decoded.size());
        }

        for (int i = 0; i < expected.size(); i++) {
            assertCardEquivalent(expected.get(i), decoded.get(i));
        }
    }

    static void assertCardEquivalent(Card expected, Card decoded) {
        if (expected == decoded) {
            return;
        }

        if (expected instanceof BuildingPaymentCard expectedBuilding
                && decoded instanceof BuildingPaymentCard decodedBuilding) {
            if (expectedBuilding.getActionCardType() != decodedBuilding.getActionCardType()) {
                throw new AssertionError("Building type mismatch");
            }
            if (expectedBuilding.getColor() != decodedBuilding.getColor()) {
                throw new AssertionError("Building color mismatch");
            }
            return;
        }

        if (expected.getClass() != decoded.getClass()) {
            throw new AssertionError("Card type mismatch: " + expected.getClass() + " vs " + decoded.getClass());
        }

        if (expected.getValue() != decoded.getValue()) {
            throw new AssertionError("Card value mismatch");
        }

        if (expected instanceof PropertiesCards expectedProperty
                && decoded instanceof PropertiesCards decodedProperty) {
            if (expectedProperty.getCurrentColor() != decodedProperty.getCurrentColor()) {
                throw new AssertionError("Property color mismatch");
            }
        }
    }

    static ActionCards debtCollectorCard() {
        return new ActionCards(ActionCardType.DEBT_COLLECTOR);
    }

    static ActionCards slyDealCard() {
        return new ActionCards(ActionCardType.SLY_DEAL);
    }

    static ActionCards forcedDealCard() {
        return new ActionCards(ActionCardType.FORCED_DEAL);
    }

    static ActionCards rentAnyCard() {
        return new ActionCards(ActionCardType.RENT_WITH_MULTIPLE_COLOR);
    }

    static ActionCards twoColorRentCard() {
        return new ActionCards(ActionCardType.RENT_WITH_BROWN_AND_LIGHT_BLUE);
    }

    static PropertiesCards brownProperty() {
        return new PropertiesCards(PropertiesCardsType.BROWN);
    }
}
