package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;
import logic.PlayerInfoHelper;
import model.ActionCards;
import model.Player;
import model.PropertyColor;

public class BuildingSelectionPanel {
    private final Game game;
    private ActionCards pendingCard;

    private final double panelX = 260;
    private final double panelY = 135;
    private final double buttonWidth = 165;
    private final double buttonHeight = 42;
    private final double buttonGapX = 14;
    private final double buttonGapY = 14;
    private final int buttonsPerRow = 3;

    // Creates a BuildingSelectionPanel instance.
    public BuildingSelectionPanel(Game game) {
        this.game = game;
        this.pendingCard = null;
    }

    // Starts selection.
    public void startSelection(ActionCards card) {
        pendingCard = card;
    }

    // Checks whether this can cel selection.
    public void cancelSelection() {
        pendingCard = null;
    }

    // Checks whether selecting.
    public boolean isSelecting() {
        return pendingCard != null;
    }

    public ActionCards getPendingCard() {
        return pendingCard;
    }

    // Checks whether cancel clicked.
    public boolean isCancelClicked(double mouseX, double mouseY) {
        return isSelecting()
                && mouseX >= 720 && mouseX <= 860
                && mouseY >= 505 && mouseY <= 545;
    }

    // Finds clicked color.
    public PropertyColor getClickedColor(double mouseX, double mouseY) {
        if (!isSelecting()) {
            return null;
        }

        Player currentPlayer = game.getCurrentPlayer();
        int displayIndex = 0;

        for (PropertyColor color : PropertyColor.values()) {
            if (canAddBuildingToColor(currentPlayer, color)) {
                continue;
            }

            int row = displayIndex / buttonsPerRow;
            int col = displayIndex % buttonsPerRow;

            double x = panelX + col * (buttonWidth + buttonGapX);
            double y = panelY + 100 + row * (buttonHeight + buttonGapY);

            if (ScreenDrawHelper.isInside(mouseX, mouseY, x, y, buttonWidth, buttonHeight)) {
                return color;
            }

            displayIndex++;
        }

        return null;
    }

    // Draws this screen area.
    public void draw(GraphicsContext gc) {
        if (!isSelecting()) {
            return;
        }

        drawOverlay(gc);
        drawTitle(gc);
        drawColorButtons(gc);
        ScreenDrawHelper.drawButton(gc, 720, 505, 140, 40, "CANCEL");
    }

    // Draws overlay.
    private void drawOverlay(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.75));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
    }

    // Draws title.
    private void drawTitle(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 26));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("BUILDING CARD: Choose a completed property set", Game.SCREEN_WIDTH / 2, 45);

        gc.setFont(Font.font("Arial", 16));
        gc.fillText("House or Hotel can only be placed on a completed property set.",
                Game.SCREEN_WIDTH / 2, 82);
    }

    // Draws color buttons.
    private void drawColorButtons(GraphicsContext gc) {
        Player currentPlayer = game.getCurrentPlayer();
        int displayIndex = 0;

        gc.setFont(Font.font("Arial", 15));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        for (PropertyColor color : PropertyColor.values()) {
            if (canAddBuildingToColor(currentPlayer, color)) {
                continue;
            }

            int row = displayIndex / buttonsPerRow;
            int col = displayIndex % buttonsPerRow;

            double x = panelX + col * (buttonWidth + buttonGapX);
            double y = panelY + 100 + row * (buttonHeight + buttonGapY);

            gc.setFill(Color.LIGHTGREEN);
            gc.fillRoundRect(x, y, buttonWidth, buttonHeight, 12, 12);

            gc.setStroke(Color.WHITE);
            gc.strokeRoundRect(x, y, buttonWidth, buttonHeight, 12, 12);

            gc.setFill(Color.BLACK);
            gc.fillText(ScreenDrawHelper.getDisplayColorName(color), x + buttonWidth / 2, y + buttonHeight / 2);

            displayIndex++;
        }

        if (displayIndex == 0) {
            drawNoAvailableSetMessage(gc);
        }

        gc.setTextBaseline(VPos.TOP);
    }

    // Draws no available set message.
    private void drawNoAvailableSetMessage(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 20));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("No completed property set is available for this building.",
                Game.SCREEN_WIDTH / 2, 245);
    }

    // Checks whether this can add building to color.
    private boolean canAddBuildingToColor(Player player, PropertyColor color) {
        int count = PlayerInfoHelper.getPropertyCountByCurrentColor(player, color);

        if (count < color.getAmountToCompleteSet()) {
            return true;
        }

        if (isHouseCard()) {
            return PlayerInfoHelper.hasHouse(player, color);
        }

        if (isHotelCard()) {
            return !PlayerInfoHelper.hasHouse(player, color)
                    || PlayerInfoHelper.hasHotel(player, color);
        }

        return true;
    }

    // Checks whether house card.
    private boolean isHouseCard() {
        return pendingCard != null
                && pendingCard.getActionCardType().name().contains("HOUSE");
    }

    // Checks whether hotel card.
    private boolean isHotelCard() {
        return pendingCard != null
                && pendingCard.getActionCardType().name().contains("HOTEL");
    }

}
