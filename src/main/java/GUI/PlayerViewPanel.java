package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;

// Draws buttons used to view each player details.
public class PlayerViewPanel {
    private static final double BUTTON_X = 760;
    private static final double BUTTON_Y = 55;
    private static final double BUTTON_WIDTH = 100;
    private static final double BUTTON_HEIGHT = 35;
    private static final double BUTTON_GAP = 10;

    // Draws one button for each player.
    public void drawPlayerViewButtons(GraphicsContext gc, Game game, int viewedPlayerIndex) {
        for (int i = 0; i < game.getPlayers().size(); i++) {
            drawPlayerViewButton(gc, i, i == viewedPlayerIndex, game);
        }

        gc.setTextBaseline(VPos.TOP);
    }

    // Returns the clicked player button index.
    public static int getClickedPlayerViewButtonIndex(Game game, double mouseX, double mouseY) {
        for (int i = 0; i < game.getPlayers().size(); i++) {
            double buttonY = BUTTON_Y + i * (BUTTON_HEIGHT + BUTTON_GAP);

            if (ScreenDrawHelper.handleButtonClick(mouseX, mouseY, BUTTON_X, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                return i;
            }
        }

        return -1;
    }

    // Draws a single player view button.
    private static void drawPlayerViewButton(GraphicsContext gc, int playerIndex, boolean selected, Game game) {
        String playerName = getPlayerDisplayName(game, playerIndex);
        double y = BUTTON_Y + playerIndex * (BUTTON_HEIGHT + BUTTON_GAP);

        if (ScreenDrawHelper.isButtonPressed(BUTTON_X, y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            ScreenDrawHelper.drawPressedButton(gc, BUTTON_X, y, BUTTON_WIDTH, BUTTON_HEIGHT, playerName);
            return;
        }

        if (selected) {
            gc.setFill(ScreenDrawHelper.ACCENT);
        } else {
            gc.setFill(Color.rgb(42, 54, 78, 0.92));
        }

        gc.fillRoundRect(BUTTON_X, y, BUTTON_WIDTH, BUTTON_HEIGHT, 12, 12);

        gc.setStroke(Color.rgb(255, 255, 255, selected ? 0.45 : 0.18));
        gc.strokeRoundRect(BUTTON_X, y, BUTTON_WIDTH, BUTTON_HEIGHT, 12, 12);

        gc.setFill(selected ? Color.rgb(34, 26, 10) : ScreenDrawHelper.TEXT);
        gc.setFont(Font.font("Arial", 14));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(playerName, BUTTON_X + BUTTON_WIDTH / 2, y + BUTTON_HEIGHT / 2);
    }

    // Returns the display name for a player.
    private static String getPlayerDisplayName(Game game, int playerIndex) {
        if (playerIndex >= 0 && playerIndex < game.getPlayers().size()) {
            String name = game.getPlayers().get(playerIndex).getName();
            if (name != null) {
                return name;
            }
        }
        return "Player " + (playerIndex + 1);
    }
}