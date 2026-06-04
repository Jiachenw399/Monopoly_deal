package network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import logic.Game;
import model.ActionCards;
import model.Card;
import model.DrawPileAndDiscardPile;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class CommandArgumentSymmetricTest {
    @Test
    public void testHandNumberRoundTrip() {
        Game game = setupTwoPlayerTurnGame();
        Player current = game.getPlayers().get(0);
        ActionCards card = NetworkSymmetricTestFixtures.debtCollectorCard();
        current.getHandCards().add(card);

        OnlineGameClickActions encoder = NetworkSymmetricTestFixtures.onlineEncoder(game, 1);
        GameServer server = NetworkSymmetricTestFixtures.serverBoundTo(game);

        String encoded = encoder.handNumber(card);
        Card decoded = server.resolveHandCardForTest(1, encoded);

        assertEquals("1", encoded);
        assertSame(card, decoded);
    }

    @Test
    public void testPlayerNumberRoundTrip() {
        Game game = setupTwoPlayerTurnGame();
        Player target = game.getPlayers().get(1);

        OnlineGameClickActions encoder = NetworkSymmetricTestFixtures.onlineEncoder(game, 1);
        GameServer server = NetworkSymmetricTestFixtures.serverBoundTo(game);

        String encoded = Integer.toString(encoder.playerNumber(target));
        Player decoded = server.resolvePlayerForTest(encoded);

        assertEquals("2", encoded);
        assertSame(target, decoded);
    }

    @Test
    public void testPropertyNumberRoundTrip() {
        Game game = setupTwoPlayerTurnGame();
        Player owner = game.getPlayers().get(1);
        PropertiesCards property = NetworkSymmetricTestFixtures.brownProperty();
        owner.getPropertyCards().add(property);

        OnlineGameClickActions encoder = NetworkSymmetricTestFixtures.onlineEncoder(game, 1);
        GameServer server = NetworkSymmetricTestFixtures.serverBoundTo(game);

        String encoded = Integer.toString(encoder.propertyNumber(owner, property));
        PropertiesCards decoded = server.resolvePropertyForTest(owner, encoded);

        assertEquals("1", encoded);
        assertSame(property, decoded);
    }

    @Test
    public void testSlyDealBodyRoundTrip() {
        Game game = setupTwoPlayerTurnGame();
        Player current = game.getPlayers().get(0);
        Player target = game.getPlayers().get(1);
        ActionCards card = NetworkSymmetricTestFixtures.slyDealCard();
        PropertiesCards stolen = NetworkSymmetricTestFixtures.brownProperty();
        current.getHandCards().add(card);
        target.getPropertyCards().add(stolen);

        List<String[]> captured = new ArrayList<>();
        OnlineGameClickActions encoder = NetworkSymmetricTestFixtures.capturingOnlineActions(game, 1, captured);
        GameServer server = NetworkSymmetricTestFixtures.serverBoundTo(game);

        encoder.finishSlyDeal(card, target, stolen);

        assertEquals(1, captured.size());
        assertEquals("SLY", captured.get(0)[0]);
        String body = captured.get(0)[1];
        String[] args = body.split("\\s+");

        assertEquals(3, args.length);
        assertSame(card, server.resolveHandCardForTest(1, args[0]));
        assertSame(target, server.resolvePlayerForTest(args[1]));
        assertSame(stolen, server.resolvePropertyForTest(target, args[2]));
    }

    @Test
    public void testForcedDealBodyRoundTrip() {
        Game game = setupTwoPlayerTurnGame();
        Player current = game.getPlayers().get(0);
        Player target = game.getPlayers().get(1);
        ActionCards card = NetworkSymmetricTestFixtures.forcedDealCard();
        PropertiesCards myProperty = NetworkSymmetricTestFixtures.brownProperty();
        PropertiesCards targetProperty = new PropertiesCards(myProperty.getType());
        current.getHandCards().add(card);
        current.getPropertyCards().add(myProperty);
        target.getPropertyCards().add(targetProperty);

        List<String[]> captured = new ArrayList<>();
        OnlineGameClickActions encoder = NetworkSymmetricTestFixtures.capturingOnlineActions(game, 1, captured);
        GameServer server = NetworkSymmetricTestFixtures.serverBoundTo(game);

        encoder.finishForcedDeal(card, target, myProperty, targetProperty);

        assertEquals("FORCED_DEAL", captured.get(0)[0]);
        String[] args = captured.get(0)[1].split("\\s+");

        assertEquals(4, args.length);
        assertSame(card, server.resolveHandCardForTest(1, args[0]));
        assertSame(target, server.resolvePlayerForTest(args[1]));
        assertSame(myProperty, server.resolvePropertyForTest(current, args[2]));
        assertSame(targetProperty, server.resolvePropertyForTest(target, args[3]));
    }

    @Test
    public void testDebtCollectorBodyRoundTrip() {
        Game game = setupTwoPlayerTurnGame();
        Player current = game.getPlayers().get(0);
        Player target = game.getPlayers().get(1);
        ActionCards card = NetworkSymmetricTestFixtures.debtCollectorCard();
        current.getHandCards().add(card);

        List<String[]> captured = new ArrayList<>();
        OnlineGameClickActions encoder = NetworkSymmetricTestFixtures.capturingOnlineActions(game, 1, captured);
        GameServer server = NetworkSymmetricTestFixtures.serverBoundTo(game);

        encoder.finishDebtCollector(card, target);

        assertEquals("DEBT", captured.get(0)[0]);
        String[] args = captured.get(0)[1].split("\\s+");

        assertSame(card, server.resolveHandCardForTest(1, args[0]));
        assertSame(target, server.resolvePlayerForTest(args[1]));
    }

    @Test
    public void testRentAnyBodyRoundTrip() {
        Game game = setupTwoPlayerTurnGame();
        Player current = game.getPlayers().get(0);
        Player target = game.getPlayers().get(1);
        ActionCards card = NetworkSymmetricTestFixtures.rentAnyCard();
        current.getHandCards().add(card);

        List<String[]> captured = new ArrayList<>();
        OnlineGameClickActions encoder = NetworkSymmetricTestFixtures.capturingOnlineActions(game, 1, captured);
        GameServer server = NetworkSymmetricTestFixtures.serverBoundTo(game);

        encoder.finishMultipleColorRent(card, target, PropertyColor.RED, true);

        assertEquals("RENT_ANY", captured.get(0)[0]);
        String[] args = captured.get(0)[1].split("\\s+");

        assertEquals(4, args.length);
        assertSame(card, server.resolveHandCardForTest(1, args[0]));
        assertSame(target, server.resolvePlayerForTest(args[1]));
        assertEquals(PropertyColor.RED, PropertyColor.valueOf(args[2]));
        assertEquals("DOUBLE", args[3]);
        assertNotNull(server.resolveHandCardForTest(1, args[0]));
    }

    @Test
    public void testTwoColorRentBodyRoundTrip() {
        Game game = setupTwoPlayerTurnGame();
        Player current = game.getPlayers().get(0);
        ActionCards card = NetworkSymmetricTestFixtures.twoColorRentCard();
        current.getHandCards().add(card);

        List<String[]> captured = new ArrayList<>();
        OnlineGameClickActions encoder = NetworkSymmetricTestFixtures.capturingOnlineActions(game, 1, captured);
        GameServer server = NetworkSymmetricTestFixtures.serverBoundTo(game);

        encoder.finishTwoColorRent(card, PropertyColor.LIGHT_BLUE, false);

        assertEquals("RENT", captured.get(0)[0]);
        String[] args = captured.get(0)[1].split("\\s+");

        assertEquals(2, args.length);
        assertSame(card, server.resolveHandCardForTest(1, args[0]));
        assertEquals(PropertyColor.LIGHT_BLUE, PropertyColor.valueOf(args[1]));
        assertTrue(args.length < 3 || !"DOUBLE".equalsIgnoreCase(args[2]));
    }

    private Game setupTwoPlayerTurnGame() {
        DrawPileAndDiscardPile drawPile = new DrawPileAndDiscardPile();
        Player playerOne = NetworkSymmetricTestFixtures.player(drawPile, "P1");
        Player playerTwo = NetworkSymmetricTestFixtures.player(drawPile, "P2");
        ArrayList<Player> players = new ArrayList<>();
        players.add(playerOne);
        players.add(playerTwo);

        Game game = NetworkSymmetricTestFixtures.twoPlayerGame();
        game.applyOnlineState(players, 0, false, null, false);
        return game;
    }
}
