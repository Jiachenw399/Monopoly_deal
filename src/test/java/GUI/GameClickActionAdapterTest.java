package GUI;

import static org.junit.jupiter.api.Assertions.assertTrue;

import network.OnlineGameClickActions;
import org.junit.jupiter.api.Test;

public class GameClickActionAdapterTest {
    @Test
    public void testLocalAndOnlineActionsUseAdapterBaseClass() {
        assertTrue(GameClickActionAdapter.class.isAssignableFrom(LocalGameClickActions.class));
        assertTrue(GameClickActionAdapter.class.isAssignableFrom(OnlineGameClickActions.class));
    }
}
