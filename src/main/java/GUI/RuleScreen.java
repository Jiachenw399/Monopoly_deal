package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;


// Draws the rule screen for the game.
public class RuleScreen {
    private final Canvas canvas;
    private boolean isShow;

    private static final String[] RULES = {
            "1. Each player starts with 5 cards.",
            "2. At the start of a turn, take 2 cards.",
            "3. If the player has no hand cards, take 5 cards.",
            "4. Each player can play up to 3 cards per turn.",
            "5. Money cards go to the bank area.",
            "6. Property cards go to the property area.",
            "7. If hand cards are more than 7, the player must discard.",
            "8. The first player to complete 3 property sets wins."
    };

    // Creates the rule screen canvas.
    public RuleScreen() {
        canvas = new Canvas(GuiScale.canvasWidth(), GuiScale.canvasHeight());
        isShow = false;
    }

    // Draws the whole rule screen.
    public void paint() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        GuiScale.prepare(gc);

        drawBackground(gc);
        drawTitle(gc);
        drawRules(gc);
        drawFooter(gc);
    }

    // Clears the rule screen canvas.
    public void clear() {
        GuiScale.clear(canvas);
    }

    // Draws the background and main panel.
    private void drawBackground(GraphicsContext gc) {
        ScreenDrawHelper.drawPageBackground(gc, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
        ScreenDrawHelper.drawPanel(gc, 170, 70, 695, 455);
    }

    // Draws the title and subtitle.
    private void drawTitle(GraphicsContext gc) {
        gc.setFill(ScreenDrawHelper.ACCENT);
        gc.setFont(Font.font("Arial", 38));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Monopoly Deal Rules", Game.SCREEN_WIDTH / 2, 105);

        gc.setFill(ScreenDrawHelper.MUTED_TEXT);
        gc.setFont(Font.font("Arial", 17));
        gc.fillText("Quick guide for the current local game mode", Game.SCREEN_WIDTH / 2, 153);
    }

    // Draws all rule lines.
    private void drawRules(GraphicsContext gc) {
        double x = 235;
        double y = 205;
        double gap = 37;

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(Font.font("Arial", 19));

        for (int i = 0; i < RULES.length; i++) {
            gc.setFill(i % 2 == 0 ? Color.rgb(255, 255, 255, 0.05) : Color.rgb(255, 255, 255, 0.02));
            gc.fillRoundRect(x - 18, y + gap * i - 7, 585, 30, 10, 10);

            gc.setFill(ScreenDrawHelper.TEXT);
            gc.fillText(RULES[i], x, y + gap * i);
        }
    }

    // Draws the return instruction text.
    private void drawFooter(GraphicsContext gc) {
        gc.setFill(ScreenDrawHelper.ACCENT);
        gc.setFont(Font.font("Arial", 22));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Press ESC to return to main menu", Game.SCREEN_WIDTH / 2, 555);
    }

    // Returns the canvas used by this screen.
    public Canvas getCanvas() {
        return canvas;
    }

    // Checks whether the rule screen is visible.
    public boolean isShow() {
        return isShow;
    }

    // Sets whether the rule screen should be visible.
    public void setShow(boolean show) {
        isShow = show;
    }
}
