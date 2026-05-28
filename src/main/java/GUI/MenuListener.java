package GUI;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import logic.Game;
import network.OnlineLauncher;

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
        scene.setOnKeyPressed(event -> handleKeyPressed(scene, event.getCode()));
    }

    private void handleKeyPressed(Scene scene, KeyCode code) {
        switch (code) {
            case N:
                if (menu.isShow()) {
                    showRuleScreen();
                }
                break;

            case A:
                if (menu.isShow()) {
                    startGame();
                }
                break;

            case L:
                if (menu.isShow() && scene.getWindow() instanceof Stage stage) {
                    OnlineLauncher.openLanMenu(stage);
                }
                break;

            case X:
                if (menu.isShow()) {
                    System.exit(0);
                }
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
        if (!menu.isShow()) {
            return;
        }

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
