package network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NetworkMessageTest {
    @Test
    public void testEncodeAndDecodeMessageWithBody() {
        NetworkMessage message = new NetworkMessage("PLAY_CARD", "1");

        NetworkMessage decoded = NetworkMessage.decode(message.encode());

        assertEquals("PLAY_CARD", decoded.getType());
        assertEquals("1", decoded.getBody());
    }

    @Test
    public void testDecodePreservesSeparatorInsideBody() {
        NetworkMessage decoded = NetworkMessage.decode("CHAT|hello|world");

        assertEquals("CHAT", decoded.getType());
        assertEquals("hello|world", decoded.getBody());
    }

    @Test
    public void testDecodeBlankMessage() {
        NetworkMessage decoded = NetworkMessage.decode(" ");

        assertEquals("EMPTY", decoded.getType());
        assertEquals("", decoded.getBody());
    }

    @Test
    public void testDecodeMessageWithoutBody() {
        NetworkMessage decoded = NetworkMessage.decode("START_GAME");

        assertEquals("START_GAME", decoded.getType());
        assertEquals("", decoded.getBody());
    }
}
