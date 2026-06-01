package GUI;

import javafx.scene.Scene;
import logic.GameFacade;

public class GameListener {
    private final GameScreen gameScreen;
    private final GameClickHandler clickHandler;

    // Creates a GameListener instance.
    public GameListener(MainMenu menu, GameScreen gameScreen, GameFacade game) {
        this.gameScreen = gameScreen;
        this.clickHandler = new GameClickHandler(
                game,
                gameScreen,
                new LocalGameClickActions(game, gameScreen, menu)
        );
    }

    // Adds listener.
    public void addListener(Scene scene) {
        scene.setOnMouseClicked(event -> {
            if (!gameScreen.isShow()) {
                return;
            }

            if (gameScreen.isShuffleAnimating()) {
                return;
            }

            clickHandler.handleMouseClick(
                    GuiScale.toLogical(event.getX()),
                    GuiScale.toLogical(event.getY())
            );
        });
    }
}
