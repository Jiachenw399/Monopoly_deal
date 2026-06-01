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
    private boolean choosingPlayerCount;

    // Creates a MainMenu instance.
    public MainMenu() {
        canvas = new Canvas(GuiScale.canvasWidth(), GuiScale.canvasHeight());
        isShow = true;
        choosingPlayerCount = false;
    }

    // Runs paint.
    public void paint() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        GuiScale.prepare(gc);

        drawBackground(gc);
        if (choosingPlayerCount) {
            drawPlayerCountChoice(gc);
        } else {
            drawMenuText(gc);
            drawMenuCards(gc);
        }
    }

    // Clears the current state.
    public void clear() {
        GuiScale.clear(canvas);
    }

    //Draw main menu background
    private void drawBackground(GraphicsContext gc) {
        ScreenDrawHelper.drawPageBackground(gc, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);

        gc.setFill(Color.rgb(255, 184, 77, 0.08));
        gc.fillRoundRect(220, 80, 595, 510, 36, 36);

        gc.setStroke(Color.rgb(255, 255, 255, 0.15));
        gc.strokeRoundRect(220, 80, 595, 510, 36, 36);
    }

    // Draws menu text.
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

    // Draws menu cards.
    private void drawMenuCards(GraphicsContext gc) {
        drawMenuOption(gc, 365, 235, "A", "Start New Game");
        drawMenuOption(gc, 365, 310, "N", "View Game Rules");
        drawMenuOption(gc, 365, 385, "L", "LAN / OnlinePlayWindow…");
        drawMenuOption(gc, 365, 460, "X", "Exit Game");
    }

    // Draws player count choice.
    private void drawPlayerCountChoice(GraphicsContext gc) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        gc.setFill(ScreenDrawHelper.ACCENT);
        gc.setFont(new Font("Arial", 42));
        gc.fillText("Choose Players", Game.SCREEN_WIDTH / 2, 145);

        gc.setFill(ScreenDrawHelper.MUTED_TEXT);
        gc.setFont(new Font("Arial", 18));
        gc.fillText("Press 2, 3, 4, or 5 to start a local game.", Game.SCREEN_WIDTH / 2, 192);

        drawPlayerCountOption(gc, 285, 265, "2", "2 Players");
        drawPlayerCountOption(gc, 440, 265, "3", "3 Players");
        drawPlayerCountOption(gc, 285, 395, "4", "4 Players");
        drawPlayerCountOption(gc, 440, 395, "5", "5 Players");

        gc.setFill(ScreenDrawHelper.MUTED_TEXT);
        gc.setFont(Font.font("Arial", 15));
        gc.fillText("Esc returns to the main menu.", Game.SCREEN_WIDTH / 2, 542);
    }

    // Draws player count option.
    private void drawPlayerCountOption(GraphicsContext gc, double x, double y, String key, String text) {
        ScreenDrawHelper.drawPanel(gc, x, y, 135, 110);

        gc.setFill(ScreenDrawHelper.ACCENT);
        gc.setFont(Font.font("Arial", 36));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(key, x + 67.5, y + 38);

        gc.setFill(ScreenDrawHelper.TEXT);
        gc.setFont(Font.font("Arial", 16));
        gc.fillText(text, x + 67.5, y + 78);
    }

    // Draws menu option.
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

    public boolean isChoosingPlayerCount() {
        return choosingPlayerCount;
    }

    public void setChoosingPlayerCount(boolean choosingPlayerCount) {
        this.choosingPlayerCount = choosingPlayerCount;
    }
}
