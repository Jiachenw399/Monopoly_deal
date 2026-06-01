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
    private MusicPlayer musicPlayer;

    // Starts this operation.
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

    // Initializes screens.
    private void initializeScreens() {
        musicPlayer = new MusicPlayer();
        musicPlayer.play();
        menu = new MainMenu();
        session = new GameSession(new logic.Game(), musicPlayer);
        ruleScreen = new RuleScreen();
    }

    // Creates root.
    private Group createRoot() {
        Group root = new Group();

        root.getChildren().addAll(
                menu.getCanvas(),
                session.getGameScreen().getCanvas(),
                ruleScreen.getCanvas()
        );

        return root;
    }

    // Registers listeners.
    private void registerListeners(Scene scene) {
        MenuListener menuListener = new MenuListener(
                menu,
                session.getGame(),
                session.getGameScreen(),
                ruleScreen,
                musicPlayer
        );
        menuListener.addListener(scene);

        GameListener gameListener = new GameListener(menu, session.getGameScreen(), session.getGame());
        gameListener.addListener(scene);
    }

    // Runs setup stage.
    private void setupStage(Stage primaryStage, Scene scene) {
        primaryStage.setScene(scene);
        primaryStage.setTitle("Monopoly Deal");
        primaryStage.setMinWidth(GuiScale.canvasWidth());
        primaryStage.setMinHeight(GuiScale.canvasHeight());
        primaryStage.show();
    }

    // Starts render loop.
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

    // Renders menu.
    private void renderMenu() {
        renderCanvas(menu.isShow(), menu.getCanvas(), menu::paint, menu::clear);
    }

    // Renders game screen.
    private void renderGameScreen(GameScreen gameScreen) {
        renderCanvas(gameScreen.isShow(), gameScreen.getCanvas(), gameScreen::paint, gameScreen::clear);
    }

    // Renders rule screen.
    private void renderRuleScreen() {
        renderCanvas(ruleScreen.isShow(), ruleScreen.getCanvas(), ruleScreen::paint, ruleScreen::clear);
    }

    // Renders canvas.
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
