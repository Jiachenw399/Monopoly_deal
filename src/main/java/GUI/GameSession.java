package GUI;

import logic.Game;
import network.GameStateCodec;

public class GameSession {
    private final Game game;
    private final GameScreen gameScreen;

    public GameSession() {
        this(new Game());
    }

    public GameSession(Game game) {
        this.game = game;
        this.gameScreen = new GameScreen(game);
        this.game.addObserver(gameScreen);
    }

    public Game getGame() {
        return game;
    }

    public GameScreen getGameScreen() {
        return gameScreen;
    }

    public void applyOnlineSnapshot(GameStateCodec.Snapshot snapshot) {
        game.applyOnlineState(
                snapshot.players,
                snapshot.currentPlayerIndex,
                snapshot.discard,
                snapshot.paymentRequest,
                snapshot.win
        );
    }
}
