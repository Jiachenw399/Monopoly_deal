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
        drawMenuCards(gc);
    }

    public void clear() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void drawBackground(GraphicsContext gc) {
        ScreenDrawHelper.drawPageBackground(gc, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);

        gc.setFill(Color.rgb(255, 184, 77, 0.08));
        gc.fillRoundRect(220, 80, 595, 480, 36, 36);

        gc.setStroke(Color.rgb(255, 255, 255, 0.15));
        gc.strokeRoundRect(220, 80, 595, 480, 36, 36);
    }

    private void drawMenuText(GraphicsContext gc) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        gc.setFill(ScreenDrawHelper.ACCENT);
        gc.setFont(new Font("Arial", 48));
        gc.fillText("Monopoly Deal", Game.SCREEN_WIDTH / 2, 135);

        gc.setFill(ScreenDrawHelper.MUTED_TEXT);
        gc.setFont(new Font("Arial", 18));
        gc.fillText("Build property sets, collect rent, and win the deal.", Game.SCREEN_WIDTH / 2, 180);
    }

    private void drawMenuCards(GraphicsContext gc) {
        drawMenuOption(gc, 365, 235, "A", "Game With four People");
        drawMenuOption(gc, 365, 310, "B", "Game With AI And Player");
        drawMenuOption(gc, 365, 385, "N", "View Game Rules");
        drawMenuOption(gc, 365, 460, "X", "Exit Game");
    }

    private void drawMenuOption(GraphicsContext gc, double x, double y, String key, String text) {
        ScreenDrawHelper.drawPanel(gc, x, y, 305, 52);

        gc.setFill(ScreenDrawHelper.ACCENT);
        gc.fillRoundRect(x + 18, y + 11, 46, 30, 10, 10);

        gc.setFill(Color.rgb(34, 26, 10));
        gc.setFont(Font.font("Arial", 18));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(key, x + 41, y + 26);

        gc.setFill(ScreenDrawHelper.TEXT);
        gc.setFont(Font.font("Arial", 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(text, x + 82, y + 26);
        gc.setTextBaseline(VPos.TOP);
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
