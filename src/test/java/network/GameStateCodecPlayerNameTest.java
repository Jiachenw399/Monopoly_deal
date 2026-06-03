package network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import logic.Game;
import model.ActionCardType;
import model.ActionCards;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;
import model.PropertiesCardsType;
import model.PropertyColor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class GameStateCodecPlayerNameTest {

    @Test
    public void testEncodeIncludesPlayerName() {
        Game game = new Game(2);
        game.startGame();
        game.getPlayers().get(0).setName("Alice");
        game.getPlayers().get(1).setName("Bob");

        String encoded = GameStateCodec.encode(game, 1);

        assertTrue(encoded.contains("P1Alice"));
        assertTrue(encoded.contains("P2Bob"));
    }

    @Test
    public void testDecodeRestoresPlayerName() {
        Game game = new Game(2);
        game.startGame();
        game.getPlayers().get(0).setName("Charlie");
        game.getPlayers().get(1).setName("Diana");
        String encoded = GameStateCodec.encode(game, 1);

        GameStateCodec.Snapshot snapshot = GameStateCodec.decode(encoded);

        assertEquals("Charlie", snapshot.players.get(0).getName());
        assertEquals("Diana", snapshot.players.get(1).getName());
    }

    @Test
    public void testDecodeHandlesNullPlayerName() {
        Game game = new Game(2);
        game.startGame();
        game.getPlayers().get(0).setName(null);
        game.getPlayers().get(1).setName("Bob");
        String encoded = GameStateCodec.encode(game, 1);

        GameStateCodec.Snapshot snapshot = GameStateCodec.decode(encoded);

        assertNull(snapshot.players.get(0).getName());
        assertEquals("Bob", snapshot.players.get(1).getName());
    }

    @Test
    public void testDecodeWithoutNameFieldHandledGracefully() {
        String body = "you=1;currentPlayer=1;discardPhase=false;"
                + "players=P1(hand=0,bank=0,properties=0,used=0),P2(hand=0,bank=0,properties=0,used=0);"
                + "yourHand=;yourBank=;yourProperties=;"
                + "publicBanks=P1[]|P2[];publicProperties=P1[]|P2[];win=false";

        GameStateCodec.Snapshot snapshot = GameStateCodec.decode(body);

        assertEquals(2, snapshot.players.size());
        assertNull(snapshot.players.get(0).getName());
        assertNull(snapshot.players.get(1).getName());
    }

    @Test
    public void testRoundTripWithNamesPreservesAllFields() {
        Game game = new Game(2);
        game.startGame();
        game.getPlayers().get(0).setName("Eve");
        game.getPlayers().get(1).setName("Frank");
        Player p1 = game.getPlayers().get(0);
        p1.getHandCards().clear();
        p1.getBankCards().clear();
        p1.getPropertyCards().clear();
        p1.getHandCards().add(new MoneyCards(7));
        p1.getBankCards().add(new MoneyCards(3));
        PropertiesCards prop = new PropertiesCards(PropertiesCardsType.BROWN);
        p1.getPropertyCards().add(prop);
        p1.setUseCardTimes(1);

        String encoded = GameStateCodec.encode(game, 1);
        GameStateCodec.Snapshot snapshot = GameStateCodec.decode(encoded);

        assertEquals("Eve", snapshot.players.get(0).getName());
        assertEquals("Frank", snapshot.players.get(1).getName());
        assertEquals(1, snapshot.players.get(0).getHandCards().size());
        assertEquals(1, snapshot.players.get(0).getBankCards().size());
        assertEquals(1, snapshot.players.get(0).getPropertyCards().size());
        assertEquals(1, snapshot.players.get(0).getUseCardTimes());
    }

    @Test
    public void testPlayerNameWithSpecialCharacters() {
        Game game = new Game(2);
        game.startGame();
        game.getPlayers().get(0).setName("Player 1");
        game.getPlayers().get(1).setName("Player 2");
        String encoded = GameStateCodec.encode(game, 1);

        GameStateCodec.Snapshot snapshot = GameStateCodec.decode(encoded);

        assertEquals("Player 1", snapshot.players.get(0).getName());
        assertEquals("Player 2", snapshot.players.get(1).getName());
    }
}
