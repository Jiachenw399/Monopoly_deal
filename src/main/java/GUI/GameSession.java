package GUI;

import logic.Game;
import network.GameStateCodec;

public class GameSession {
    private final Game game;
    private final GameScreen gameScreen;

    // Creates a GameSession instance.
    public GameSession() {
        this(new Game());
    }

    // Creates a GameSession instance.
    public GameSession(Game game) {
        this(game, null);
    }

    // Creates a GameSession instance.
    public GameSession(Game game, MusicPlayer musicPlayer) {
        this.game = game;
        this.gameScreen = new GameScreen(game, musicPlayer);
        this.game.addObserver(gameScreen);
    }

    public Game getGame() {
        return game;
    }

    public GameScreen getGameScreen() {
        return gameScreen;
    }

    // Applies online snapshot.
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
