package GUI;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import logic.Game;

public class MonopolyApp extends Application {
    private MainMenu menu;
    private Game game;
    private GameScreen gameScreen;
    private RuleScreen ruleScreen;

    @Override
    public void start(Stage primaryStage) {
        initializeScreens();

        Group root = createRoot();
        Scene scene = new Scene(root);

        registerListeners(scene);
        setupStage(primaryStage, scene);
        startRenderLoop();

        root.requestFocus();
    }

    private void initializeScreens() {
        menu = new MainMenu();
        game = new Game();
        gameScreen = new GameScreen(game);
        ruleScreen = new RuleScreen();
    }

    private Group createRoot() {
        Group root = new Group();

        root.getChildren().addAll(
                menu.getCanvas(),
                gameScreen.getCanvas(),
                ruleScreen.getCanvas()
        );

        return root;
    }

    private void registerListeners(Scene scene) {
        MenuListener menuListener = new MenuListener(menu, game, gameScreen, ruleScreen);
        menuListener.addListener(scene);

        GameListener gameListener = new GameListener(menu, gameScreen, game);
        gameListener.addListener(scene);
    }

    private void setupStage(Stage primaryStage, Scene scene) {
        primaryStage.setScene(scene);
        primaryStage.setTitle("Monopoly Deal");
        primaryStage.show();
    }

    private void startRenderLoop() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                renderMenu();
                renderGameScreen();
                renderRuleScreen();
            }
        }.start();
    }

    private void renderMenu() {
        renderCanvas(menu.isShow(), menu.getCanvas(), menu::paint, menu::clear);
    }

    private void renderGameScreen() {
        renderCanvas(gameScreen.isShow(), gameScreen.getCanvas(), gameScreen::paint, gameScreen::clear);
    }

    private void renderRuleScreen() {
        renderCanvas(ruleScreen.isShow(), ruleScreen.getCanvas(), ruleScreen::paint, ruleScreen::clear);
    }

    private void renderCanvas(boolean shouldShow, Canvas canvas, Runnable paintAction, Runnable clearAction) {
        if (shouldShow) {
            paintAction.run();
            canvas.setVisible(true);
        } else {
            clearAction.run();
            canvas.setVisible(false);
        }
    }
}