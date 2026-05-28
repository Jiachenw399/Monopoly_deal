package GUI;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;

public class MonopolyApp extends Application {
    private MainMenu menu;
    private GameSession session;
    private RuleScreen ruleScreen;

    @Override
    public void start(Stage primaryStage) {
        initializeScreens();

        Group root = createRoot();
        Scene scene = new Scene(root, GuiScale.canvasWidth(), GuiScale.canvasHeight());

        registerListeners(scene);
        setupStage(primaryStage, scene);
        startRenderLoop();

        root.requestFocus();
    }

    private void initializeScreens() {
        menu = new MainMenu();
        session = new GameSession();
        ruleScreen = new RuleScreen();
    }

    private Group createRoot() {
        Group root = new Group();

        root.getChildren().addAll(
                menu.getCanvas(),
                session.getGameScreen().getCanvas(),
                ruleScreen.getCanvas()
        );

        return root;
    }

    private void registerListeners(Scene scene) {
        MenuListener menuListener = new MenuListener(
                menu,
                session.getGame(),
                session.getGameScreen(),
                ruleScreen
        );
        menuListener.addListener(scene);

        GameListener gameListener = new GameListener(menu, session.getGameScreen(), session.getGame());
        gameListener.addListener(scene);
    }

    private void setupStage(Stage primaryStage, Scene scene) {
        primaryStage.setScene(scene);
        primaryStage.setTitle("Monopoly Deal");
        primaryStage.show();
    }

    private void startRenderLoop() {
        GameScreen gameScreen = session.getGameScreen();
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                renderMenu();
                renderGameScreen(gameScreen);
                renderRuleScreen();
            }
        }.start();
    }

    private void renderMenu() {
        renderCanvas(menu.isShow(), menu.getCanvas(), menu::paint, menu::clear);
    }

    private void renderGameScreen(GameScreen gameScreen) {
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
