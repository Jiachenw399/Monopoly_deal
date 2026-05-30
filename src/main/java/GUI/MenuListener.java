package GUI;

import javafx.animation.PauseTransition;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Duration;
import logic.GameFacade;
import network.OnlineLauncher;

public class MenuListener {
    private final MainMenu menu;
    private final GameFacade game;
    private final GameScreen gameScreen;
    private final RuleScreen ruleScreen;

    public MenuListener(MainMenu menu, GameFacade game, GameScreen gameScreen, RuleScreen ruleScreen) {
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
        if (!menu.isShow() || gameScreen.isShuffleAnimating()) {
            return;
        }

        menu.setShow(false);
        ruleScreen.setShow(false);
        gameScreen.setShow(true);
        gameScreen.startShuffleAnimation();

        PauseTransition delay = new PauseTransition(Duration.seconds(2.0));
        delay.setOnFinished(event -> {
            game.startGame();
            gameScreen.stopShuffleAnimation();
        });
        delay.play();
    }

    private void returnToMenu() {
        ruleScreen.setShow(false);
        gameScreen.setShow(false);
        menu.setShow(true);
    }
}
