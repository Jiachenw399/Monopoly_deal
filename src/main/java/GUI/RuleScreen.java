package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;

public class RuleScreen {
    private final Canvas canvas;
    private boolean isShow;

    private static final String[] RULES = {
            "1. Each player starts with 5 cards.",
            "2. At the start of a turn, draw 2 cards.",
            "3. If the player has no hand cards, draw 5 cards.",
            "4. Each player can play up to 3 cards per turn.",
            "5. Money cards go to the bank area.",
            "6. Property cards go to the property area.",
            "7. If hand cards are more than 7, the player must discard.",
            "8. The first player to complete 3 property sets wins."
    };

    public RuleScreen() {
        canvas = new Canvas(Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
        isShow = false;
    }

    public void paint() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        drawBackground(gc);
        drawTitle(gc);
        drawRules(gc);
        drawFooter(gc);
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

    private void drawTitle(GraphicsContext gc) {
        gc.setFill(Color.ORANGE);
        gc.setFont(Font.font("Arial", 36));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Monopoly Deal Rules", Game.SCREEN_WIDTH / 2, 40);
    }

    private void drawRules(GraphicsContext gc) {
        double x = 100;
        double y = 120;
        double gap = 42;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 22));
        gc.setTextAlign(TextAlignment.LEFT);

        for (int i = 0; i < RULES.length; i++) {
            gc.fillText(RULES[i], x, y + gap * i);
        }
    }

    private void drawFooter(GraphicsContext gc) {
        gc.setFill(Color.ORANGE);
        gc.setFont(Font.font("Arial", 24));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Press ESC to return to main menu", Game.SCREEN_WIDTH / 2, 560);
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