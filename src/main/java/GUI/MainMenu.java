package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;

public class MainMenu {
    private final Canvas canvas;
    private boolean isShow;

    public MainMenu() {
        canvas = new Canvas(Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
        isShow = true;
    }

    public void paint() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        drawBackground(gc);
        drawMenuText(gc);
    }

    public void clear() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void drawBackground(GraphicsContext gc) {
        gc.clearRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
        gc.setFill(Color.DARKBLUE);
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
    }

    private void drawMenuText(GraphicsContext gc) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        gc.setFill(Color.ORANGE);
        gc.setFont(new Font("Comic Sans MS", 48));
        gc.fillText("Welcome to Monopoly Deal!!💰", Game.SCREEN_WIDTH / 2, Game.SCREEN_HEIGHT / 15);

        gc.setFont(new Font("Comic Sans MS", 34));
        gc.fillText("To start a new game press A", Game.SCREEN_WIDTH / 2, 1.5 * Game.SCREEN_HEIGHT / 4.3);
        gc.fillText("To see the game rules press N", Game.SCREEN_WIDTH / 2, 2.3 * Game.SCREEN_HEIGHT / 4.3);
        gc.fillText("To exit press X", Game.SCREEN_WIDTH / 2, 3.1 * Game.SCREEN_HEIGHT / 4.3);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }
}