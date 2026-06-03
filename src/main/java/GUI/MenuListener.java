package GUI;

import javafx.animation.PauseTransition;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Duration;
import logic.GameFacade;
import logic.SimpleAIPlayer;
import model.Player;
import network.OnlineLauncher;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class MenuListener {
    private final MainMenu menu;
    private final GameFacade game;
    private final GameScreen gameScreen;
    private final RuleScreen ruleScreen;
    private final MusicPlayer musicPlayer;
    private final Map<KeyCode, Consumer<Scene>> keyHandlers;
    private final Map<KeyCode, Integer> playerCountKeys;
    private final Map<KeyCode, Integer> aiCountKeys;
    private boolean aiModeActive = false;

    // Creates a MenuListener instance.
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
        this.keyHandlers = createKeyHandlers();
        this.playerCountKeys = createPlayerCountKeys();
        this.aiCountKeys = createAICountKeys();
    }

    // Adds listener.
    public void addListener(Scene scene) {
        scene.setOnKeyPressed(event -> handleKeyPressed(scene, event.getCode()));
    }

    // Handles key pressed.
    private void handleKeyPressed(Scene scene, KeyCode code) {
        if (menu.isChoosingPlayerCount() && menu.isChoosingAI()) {
            Integer aiCount = aiCountKeys.get(code);
            if (aiCount != null) {
                startAIGame(aiCount, scene);
                return;
            }
        }

        Integer selectedPlayerCount = playerCountKeys.get(code);
        if (selectedPlayerCount != null && !menu.isChoosingAI()) {
            startGameWithPlayerCount(selectedPlayerCount, scene);
            return;
        }

        Consumer<Scene> handler = keyHandlers.get(code);
        if (handler != null) {
            handler.accept(scene);
        }
    }

    // Creates key handlers.
    private Map<KeyCode, Consumer<Scene>> createKeyHandlers() {
        Map<KeyCode, Consumer<Scene>> handlers = new EnumMap<>(KeyCode.class);
        handlers.put(KeyCode.N, scene -> showRuleScreenIfAvailable());
        handlers.put(KeyCode.A, scene -> showLocalPlayerCountChoice());
        handlers.put(KeyCode.B, scene -> showAIPlayerCountChoice());
        handlers.put(KeyCode.L, this::openLanMenuIfAvailable);
        handlers.put(KeyCode.X, scene -> exitIfAvailable());
        handlers.put(KeyCode.ESCAPE, scene -> returnToMenu());
        return handlers;
    }

    // Creates player count keys.
    private Map<KeyCode, Integer> createPlayerCountKeys() {
        Map<KeyCode, Integer> keys = new EnumMap<>(KeyCode.class);
        keys.put(KeyCode.DIGIT2, 2);
        keys.put(KeyCode.NUMPAD2, 2);
        keys.put(KeyCode.DIGIT3, 3);
        keys.put(KeyCode.NUMPAD3, 3);
        keys.put(KeyCode.DIGIT4, 4);
        keys.put(KeyCode.NUMPAD4, 4);
        keys.put(KeyCode.DIGIT5, 5);
        keys.put(KeyCode.NUMPAD5, 5);
        return keys;
    }

    // Creates AI count keys.
    private Map<KeyCode, Integer> createAICountKeys() {
        Map<KeyCode, Integer> keys = new EnumMap<>(KeyCode.class);
        keys.put(KeyCode.DIGIT1, 1);
        keys.put(KeyCode.NUMPAD1, 1);
        keys.put(KeyCode.DIGIT2, 2);
        keys.put(KeyCode.NUMPAD2, 2);
        keys.put(KeyCode.DIGIT3, 3);
        keys.put(KeyCode.NUMPAD3, 3);
        keys.put(KeyCode.DIGIT4, 4);
        keys.put(KeyCode.NUMPAD4, 4);
        return keys;
    }

    // Shows local player count choice.
    private void showLocalPlayerCountChoice() {
        if (menu.isShow() && !menu.isChoosingPlayerCount()) {
            menu.setChoosingAI(false);
            menu.setChoosingPlayerCount(true);
        }
    }

    // Shows AI player count choice.
    private void showAIPlayerCountChoice() {
        if (menu.isShow() && !menu.isChoosingPlayerCount()) {
            menu.setChoosingAI(true);
            menu.setChoosingPlayerCount(true);
        }
    }

    // Shows rule screen if available.
    private void showRuleScreenIfAvailable() {
        if (menu.isShow() && !menu.isChoosingPlayerCount()) {
            showRuleScreen();
        }
    }

    // Shows player count choice if available.
    private void showPlayerCountChoiceIfAvailable() {
        if (menu.isShow() && !menu.isChoosingPlayerCount()) {
            menu.setChoosingPlayerCount(true);
        }
    }

    // Opens lan menu if available.
    private void openLanMenuIfAvailable(Scene scene) {
        if (menu.isShow() && !menu.isChoosingPlayerCount() && scene.getWindow() instanceof Stage stage) {
            OnlineLauncher.openLanMenu(stage, musicPlayer);
        }
    }

    // Runs exit if available.
    private void exitIfAvailable() {
        if (menu.isShow() && !menu.isChoosingPlayerCount()) {
            System.exit(0);
        }
    }

    // Shows rule screen.
    private void showRuleScreen() {
        menu.setShow(false);
        gameScreen.setShow(false);
        ruleScreen.setShow(true);
    }

    // Starts game with player count.
    private void startGameWithPlayerCount(int playerCount, Scene scene) {
        if (!menu.isShow() || !menu.isChoosingPlayerCount() || gameScreen.isShuffleAnimating()) {
            return;
        }

        if (scene.getWindow() instanceof Stage stage) {
            TextInputDialogWrapper nameDialog = new TextInputDialogWrapper(stage, playerCount);
            Optional<List<String>> result = nameDialog.showAndWait();

            if (result.isEmpty()) {
                return;
            }

            List<String> playerNames = result.get();
            startGameWithNames(playerCount, playerNames);
        } else {
            game.startGame(playerCount);
        }
    }

    // Starts game with player names.
    private void startGameWithNames(int playerCount, List<String> playerNames) {
        menu.setChoosingPlayerCount(false);
        menu.setShow(false);
        ruleScreen.setShow(false);
        gameScreen.setShow(true);
        gameScreen.startShuffleAnimation();

        PauseTransition delay = new PauseTransition(Duration.seconds(2.0));
        delay.setOnFinished(event -> {
            game.startGame(playerCount, playerNames);
            gameScreen.stopShuffleAnimation();
        });
        delay.play();
    }

    // Starts AI game (1 human + aiCount AI opponents).
    private void startAIGame(int aiCount, Scene scene) {
        if (!menu.isShow() || !menu.isChoosingPlayerCount() || !menu.isChoosingAI()
                || gameScreen.isShuffleAnimating()) {
            return;
        }

        int totalPlayers = 1 + aiCount;
        if (totalPlayers < 2 || totalPlayers > 5) {
            return;
        }

        menu.setChoosingPlayerCount(false);
        menu.setChoosingAI(false);
        menu.setShow(false);
        ruleScreen.setShow(false);
        gameScreen.setShow(true);
        gameScreen.startShuffleAnimation();

        PauseTransition delay = new PauseTransition(Duration.seconds(2.0));
        delay.setOnFinished(event -> {
            gameScreen.stopShuffleAnimation();
            if (game instanceof logic.Game concreteGame) {
                concreteGame.startGame(totalPlayers);
                for (int i = 1; i < totalPlayers; i++) {
                    Player aiPlayer = concreteGame.getPlayers().get(i);
                    aiPlayer.setAI(true);
                    concreteGame.registerAI(aiPlayer, new SimpleAIPlayer());
                }
                concreteGame.triggerAITurnIfNeeded();
            }
        });
        delay.play();
    }

    // Returns to to menu.
    private void returnToMenu() {
        menu.setChoosingPlayerCount(false);
        menu.setChoosingAI(false);
        ruleScreen.setShow(false);
        gameScreen.setShow(false);
        menu.setShow(true);
    }
}
