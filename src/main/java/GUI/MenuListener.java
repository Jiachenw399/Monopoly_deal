package GUI;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import logic.Game;

public class MenuListener {
    private final MainMenu menu;
    private final Game game;
    private final GameScreen gameScreen;
    private final RuleScreen ruleScreen;

    public MenuListener(MainMenu menu, Game game, GameScreen gameScreen, RuleScreen ruleScreen) {
        this.menu = menu;
        this.game = game;
        this.gameScreen = gameScreen;
        this.ruleScreen = ruleScreen;
    }

    public void addListener(Scene scene) {
        scene.setOnKeyPressed(event -> handleKeyPressed(event.getCode()));
    }

    private void handleKeyPressed(KeyCode code) {
        switch (code) {
            case N:
                showRuleScreen();
                break;

            case A:
                startGame();
                break;

            case X:
                System.exit(0);
                break;

            case ESCAPE:
                returnToMenu();
                break;

            default:
                break;
        }
    }

    private void showRuleScreen() {
        menu.setShow(false);
        gameScreen.setShow(false);
        ruleScreen.setShow(true);
    }

    private void startGame() {
        menu.setShow(false);
        ruleScreen.setShow(false);
        gameScreen.setShow(true);

        game.startGame();
    }

    private void returnToMenu() {
        ruleScreen.setShow(false);
        gameScreen.setShow(false);
        menu.setShow(true);
    }
}
