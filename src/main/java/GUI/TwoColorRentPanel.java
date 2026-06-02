package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;
import logic.PlayerInfoHelper;
import model.ActionCards;
import model.PropertyColor;

import java.util.ArrayList;

// Handles the selection UI for two-color rent cards.
public class TwoColorRentPanel {
    private final Game game;

    private ActionCards pendingCard;
    private boolean useDoubleRent;

    private final double panelX = 330;
    private final double panelY = 190;
    private final double buttonWidth = 170;
    private final double buttonHeight = 55;

    // Creates the panel and stores game data.
    public TwoColorRentPanel(Game game) {
        this.game = game;
        this.pendingCard = null;
        this.useDoubleRent = false;
    }

    // Starts rent color selection for the given card.
    public void startSelection(ActionCards card) {
        pendingCard = card;
        useDoubleRent = false;
    }

    // Cancels the current rent selection.
    public void cancelSelection() {
        pendingCard = null;
    }

    // Checks whether this panel is active.
    public boolean isSelecting() {
        return pendingCard != null;
    }

    public ActionCards getPendingCard() {
        return pendingCard;
    }

    // Returns whether Double the Rent is selected.
    public boolean shouldUseDoubleRent() {
        return useDoubleRent;
    }

    // Switches the Double the Rent option on or off.
    public void toggleDoubleRent() {
        useDoubleRent = !useDoubleRent;
    }

    // Checks whether the Double the Rent option was clicked.
    public boolean isDoubleRentClicked(double mouseX, double mouseY) {
        return isSelecting()
                && game.hasDoubleTheRentCard(game.getCurrentPlayer())
                && game.getCurrentPlayer().getUseCardTimes() <= 1
                && ScreenDrawHelper.handleButtonClick(mouseX, mouseY, 370, 410, 280, 35);
    }

    // Checks whether the cancel button was clicked.
    public boolean isCancelClicked(double mouseX, double mouseY) {
        return isSelecting()
                && ScreenDrawHelper.handleButtonClick(mouseX, mouseY, 720, 505, 140, 40);
    }

    // Returns the clicked rent color if it is usable.
    public PropertyColor getClickedRentColor(double mouseX, double mouseY) {
        if (!isSelecting()) {
            return null;
        }

        ArrayList<PropertyColor> colors = pendingCard.getActionCardType().getRentColors();

        for (int i = 0; i < colors.size(); i++) {
            PropertyColor color = colors.get(i);

            double x = panelX + i * (buttonWidth + 40);
            double y = panelY;

            if (ScreenDrawHelper.isInside(mouseX, mouseY, x, y, buttonWidth, buttonHeight)) {
                if (PlayerInfoHelper.hasPropertyColor(game.getCurrentPlayer(), color)) {
                    ScreenDrawHelper.handleButtonClick(mouseX, mouseY, x, y, buttonWidth, buttonHeight);
                    return color;
                }

                return null;
            }
        }

        return null;
    }

    // Draws the whole two-color rent panel.
    public void draw(GraphicsContext gc) {
        if (!isSelecting()) {
            return;
        }

        drawOverlay(gc);
        drawTitle(gc);
        drawColorButtons(gc);
        drawDoubleRentOption(gc, 370, 410);
        ScreenDrawHelper.drawButton(gc, 720, 505, 140, 40, "CANCEL");
        gc.setTextBaseline(VPos.TOP);
    }

    // Draws the dark background overlay.
    private void drawOverlay(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.75));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
    }

    // Draws the panel title and instruction.
    private void drawTitle(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 26));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("RENT CARD: Choose one color to charge rent", Game.SCREEN_WIDTH / 2, 60);

        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Grey color means you do not have that property color, so it cannot be selected.",
                Game.SCREEN_WIDTH / 2, 100);
    }

    // Draws the two possible rent color buttons.
    private void drawColorButtons(GraphicsContext gc) {
        ArrayList<PropertyColor> colors = pendingCard.getActionCardType().getRentColors();
        boolean hasAnyColor = false;

        for (int i = 0; i < colors.size(); i++) {
            PropertyColor color = colors.get(i);
            double x = panelX + i * (buttonWidth + 40);
            double y = panelY;

            boolean usable = PlayerInfoHelper.hasPropertyColor(game.getCurrentPlayer(), color);

            if (usable) {
                hasAnyColor = true;
                gc.setFill(Color.LIGHTGREEN);
            } else {
                gc.setFill(Color.GRAY);
            }

            drawColorButton(gc, color, x, y, usable);
        }

        if (!hasAnyColor) {
            drawNoUsableColorMessage(gc);
        }
    }

    // Draws one color button.
    private void drawColorButton(GraphicsContext gc, PropertyColor color, double x, double y, boolean usable) {
        if (usable && ScreenDrawHelper.isButtonPressed(x, y, buttonWidth, buttonHeight)) {
            ScreenDrawHelper.drawPressedButton(gc, x, y, buttonWidth, buttonHeight, color.name());
            return;
        }

        gc.fillRoundRect(x, y, buttonWidth, buttonHeight, 14, 14);

        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(x, y, buttonWidth, buttonHeight, 14, 14);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(color.name(), x + buttonWidth / 2, y + buttonHeight / 2);

        if (!usable) {
            gc.setFill(Color.DARKRED);
            gc.setFont(Font.font("Arial", 12));
            gc.fillText("Unavailable", x + buttonWidth / 2, y + buttonHeight + 18);
        }
    }

    // Draws a warning when no color can be used.
    private void drawNoUsableColorMessage(GraphicsContext gc) {
        gc.setFill(Color.LIGHTYELLOW);
        gc.setFont(Font.font("Arial", 20));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("You do not have either color. This card cannot be used now.",
                Game.SCREEN_WIDTH / 2, 340);
    }

    // Draws the optional Double the Rent checkbox.
    private void drawDoubleRentOption(GraphicsContext gc, double x, double y) {
        if (!game.hasDoubleTheRentCard(game.getCurrentPlayer())
                || game.getCurrentPlayer().getUseCardTimes() > 1) {
            return;
        }

        gc.setFill(Color.LIGHTYELLOW);
        gc.fillRoundRect(x, y, 280, 35, 10, 10);

        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(x, y, 280, 35, 10, 10);

        gc.setFill(Color.WHITE);
        gc.strokeRect(x + 12, y + 8, 18, 18);

        if (useDoubleRent) {
            gc.setFill(Color.LIGHTGREEN);
            gc.fillText("✓", x + 21, y + 8);
        }

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 14));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("Use DOUBLE THE RENT  ×2", x + 42, y + 17.5);

        gc.setTextBaseline(VPos.TOP);
    }

}
