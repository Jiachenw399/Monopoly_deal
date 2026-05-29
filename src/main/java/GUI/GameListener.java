package GUI;

import javafx.scene.Scene;
import logic.GameFacade;

public class GameListener {
    private final GameScreen gameScreen;
    private final GameClickHandler clickHandler;

    public GameListener(MainMenu menu, GameScreen gameScreen, GameFacade game) {
        this.gameScreen = gameScreen;
        this.clickHandler = new GameClickHandler(
                game,
                gameScreen,
                new LocalGameClickActions(game, gameScreen, menu)
        );
    }

    public void addListener(Scene scene) {
        scene.setOnMouseClicked(event -> {
            if (!gameScreen.isShow()) {
                return;
            }

            clickHandler.handleMouseClick(
                    GuiScale.toLogical(event.getX()),
                    GuiScale.toLogical(event.getY())
            );
        });
    }
}
