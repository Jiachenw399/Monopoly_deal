package network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import logic.Game;
import model.ActionCardType;
import model.ActionCards;
import model.HiddenCard;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;
import model.PropertiesCardsType;
import model.PropertyColor;
import org.junit.jupiter.api.Test;

public class GameStateCodecTest {
    @Test
    public void testEncodeNullGame() {
        assertEquals("NO_GAME", GameStateCodec.encode(null, 1));
    }

    @Test
    public void testEncodeAndDecodeSnapshot() {
        Game game = new Game(2);
        Player playerOne = game.getPlayers().get(0);
        Player playerTwo = game.getPlayers().get(1);
        playerOne.getHandCards().clear();
        playerOne.getBankCards().clear();
        playerOne.getPropertyCards().clear();
        playerTwo.getHandCards().clear();
        playerTwo.getBankCards().clear();
        playerTwo.getPropertyCards().clear();

        playerOne.getHandCards().add(new ActionCards(ActionCardType.PASS_GO));
        playerOne.getBankCards().add(new MoneyCards(5));
        PropertiesCards property = new PropertiesCards(PropertiesCardsType.WILD_RED_YELLOW);
        property.setCurrentColor(PropertyColor.YELLOW);
        property.setHasHouse(true);
        playerOne.getPropertyCards().add(property);
        playerOne.setUseCardTimes(2);

        String encoded = GameStateCodec.encode(game, 1);
        GameStateCodec.Snapshot snapshot = GameStateCodec.decode(encoded);

        assertEquals(1, snapshot.you);
        assertEquals(2, snapshot.players.size());
        assertEquals(0, snapshot.currentPlayerIndex);
        assertEquals(1, snapshot.players.get(0).getHandCards().size());
        assertEquals(1, snapshot.players.get(0).getBankCards().size());
        assertEquals(1, snapshot.players.get(0).getPropertyCards().size());
        assertEquals(2, snapshot.players.get(0).getUseCardTimes());

        PropertiesCards decodedProperty = snapshot.players.get(0).getPropertyCards().get(0);
        assertEquals(PropertyColor.YELLOW, decodedProperty.getCurrentColor());
        assertTrue(decodedProperty.hasHouse());
        assertFalse(decodedProperty.hasHotel());
    }

    @Test
    public void testDecodePaymentRequest() {
        String body = "you=1;currentPlayer=1;discardPhase=false;"
                + "payment=payer=2,receiver=1,amount=5;"
                + "players=P1(hand=0,bank=0,properties=0,used=0),P2(hand=0,bank=1,properties=0,used=0);"
                + "yourHand=;yourBank=;yourProperties=;"
                + "publicBanks=P1[]|P2[MONEY:5];publicProperties=P1[]|P2[];win=false";

        GameStateCodec.Snapshot snapshot = GameStateCodec.decode(body);

        assertNotNull(snapshot.paymentRequest);
        assertEquals(snapshot.players.get(0), snapshot.paymentRequest.getReceiver());
        assertEquals(snapshot.players.get(1), snapshot.paymentRequest.getPayer());
        assertEquals(5, snapshot.paymentRequest.getAmount());
    }

    @Test
    public void testDecodePaymentRequestWithJustSayNoResponse() {
        String body = "you=1;currentPlayer=1;discardPhase=false;"
                + "payment=payer=2,receiver=1,amount=5,justSayNoPending=true,"
                + "justSayNoResponder=1,lastJustSayNoUser=2;"
                + "players=P1(hand=0,bank=0,properties=0,used=0),P2(hand=0,bank=1,properties=0,used=0);"
                + "yourHand=;yourBank=;yourProperties=;"
                + "publicBanks=P1[]|P2[MONEY:5];publicProperties=P1[]|P2[];win=false";

        GameStateCodec.Snapshot snapshot = GameStateCodec.decode(body);

        assertNotNull(snapshot.paymentRequest);
        assertTrue(snapshot.paymentRequest.isJustSayNoPending());
        assertEquals(snapshot.players.get(0), snapshot.paymentRequest.getJustSayNoResponder());
        assertEquals(snapshot.players.get(1), snapshot.paymentRequest.getLastJustSayNoUser());
    }

    @Test
    public void testDecodeUsesHiddenCardsForOtherPlayersHands() {
        Game game = new Game(2);
        game.startGame();

        GameStateCodec.Snapshot snapshotForPlayerTwo = GameStateCodec.decode(GameStateCodec.encode(game, 2));

        assertEquals(2, snapshotForPlayerTwo.you);
        assertEquals(0, snapshotForPlayerTwo.currentPlayerIndex);
        assertEquals(game.getPlayers().get(0).getHandCards().size(),
                snapshotForPlayerTwo.players.get(0).getHandCards().size());
        assertTrue(snapshotForPlayerTwo.players.get(0).getHandCards().get(0) instanceof HiddenCard);
        assertTrue(snapshotForPlayerTwo.players.get(1).getHandCards().size() > 0);
        assertFalse(snapshotForPlayerTwo.players.get(1).getHandCards().get(0) instanceof HiddenCard);
    }

}
