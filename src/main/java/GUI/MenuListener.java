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
    private final MusicPlayer musicPlayer;

    public MenuListener(MainMenu menu,
                        GameFacade game,
                        GameScreen gameScreen,
                        RuleScreen ruleScreen,
                        MusicPlayer musicPlayer) {
        this.menu = menu;
        this.game = game;
        this.gameScreen = gameScreen;
        this.ruleScreen = ruleScreen;
        this.musicPlayer = musicPlayer;
    }

    public void addListener(Scene scene) {
        scene.setOnKeyPressed(event -> handleKeyPressed(scene, event.getCode()));
    }

    private void handleKeyPressed(Scene scene, KeyCode code) {
        switch (code) {
            case N:
                if (menu.isShow() && !menu.isChoosingPlayerCount()) {
                    showRuleScreen();
                }
                break;

            case A:
                if (menu.isShow() && !menu.isChoosingPlayerCount()) {
                    menu.setChoosingPlayerCount(true);
                }
                break;

            case L:
                if (menu.isShow() && !menu.isChoosingPlayerCount() && scene.getWindow() instanceof Stage stage) {
                    OnlineLauncher.openLanMenu(stage, musicPlayer);
                }
                break;

            case X:
                if (menu.isShow() && !menu.isChoosingPlayerCount()) {
                    System.exit(0);
                }
                break;

            case DIGIT2:
            case NUMPAD2:
                startGameWithPlayerCount(2);
                break;

            case DIGIT3:
            case NUMPAD3:
                startGameWithPlayerCount(3);
                break;

            case DIGIT4:
            case NUMPAD4:
                startGameWithPlayerCount(4);
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

    private void startGameWithPlayerCount(int playerCount) {
        if (!menu.isShow() || !menu.isChoosingPlayerCount() || gameScreen.isShuffleAnimating()) {
            return;
        }

        menu.setChoosingPlayerCount(false);
        menu.setShow(false);
        ruleScreen.setShow(false);
        gameScreen.setShow(true);
        gameScreen.startShuffleAnimation();

        PauseTransition delay = new PauseTransition(Duration.seconds(2.0));
        delay.setOnFinished(event -> {
            game.startGame(playerCount);
            gameScreen.stopShuffleAnimation();
        });
        delay.play();
    }

    private void returnToMenu() {
        menu.setChoosingPlayerCount(false);
        ruleScreen.setShow(false);
        gameScreen.setShow(false);
        menu.setShow(true);
    }
}
