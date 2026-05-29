package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;
import logic.RentCalculator;
import model.ActionCards;
import model.Player;
import model.PropertyColor;

public class MultipleColorRentSelectionPanel {
    private final Game game;
    private final RentCalculator rentCalculator;
    private final PlayerDetailPopupPanel detailPopupPanel;

    private ActionCards pendingCard;
    private Player selectedTarget;
    private Player detailTarget;
    private PropertyColor selectedColor;
    private boolean useDoubleRent;

    private final double panelX = 230;
    private final double panelY = 120;
    private final double buttonWidth = 165;
    private final double buttonHeight = 42;

    private final double detailConfirmX = 390;
    private final double detailBackX = 555;
    private final double detailCancelX = 720;
    private final double detailButtonY = 550;
    private final double detailButtonWidth = 140;
    private final double detailButtonHeight = 40;

    public MultipleColorRentSelectionPanel(Game game) {
        this.game = game;
        this.rentCalculator = new RentCalculator();
        this.detailPopupPanel = new PlayerDetailPopupPanel(game);
        this.pendingCard = null;
        this.selectedTarget = null;
        this.detailTarget = null;
        this.selectedColor = null;
        this.useDoubleRent = false;
    }

    public void startSelection(ActionCards card) {
        pendingCard = card;
        selectedTarget = null;
        detailTarget = null;
        selectedColor = null;
        useDoubleRent = false;
        detailPopupPanel.close();
    }

    public void cancelSelection() {
        pendingCard = null;
        selectedTarget = null;
        detailTarget = null;
        selectedColor = null;
        useDoubleRent = false;
        detailPopupPanel.close();
    }

    public boolean isSelecting() {
        return pendingCard != null;
    }

    public ActionCards getPendingCard() {
        return pendingCard;
    }

    public Player getSelectedTarget() {
        return selectedTarget;
    }

    public void setSelectedTarget(Player selectedTarget) {
        this.selectedTarget = selectedTarget;
    }

    public PropertyColor getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(PropertyColor selectedColor) {
        this.selectedColor = selectedColor;
    }

    public boolean shouldUseDoubleRent() {
        return useDoubleRent;
    }

    public void toggleDoubleRent() {
        useDoubleRent = !useDoubleRent;
    }

    public boolean canConfirm() {
        return selectedTarget != null && selectedColor != null;
    }

    public boolean isDetailShowing() {
        return detailTarget != null;
    }

    public Player getDetailTarget() {
        return detailTarget;
    }

    public void showTargetDetail(Player player) {
        detailTarget = player;

        if (player == null) {
            detailPopupPanel.close();
            return;
        }

        int index = game.getPlayers().indexOf(player);
        detailPopupPanel.showPlayer(index);
    }

    public boolean handleDetailPageButtonClick(double mouseX, double mouseY) {
        return detailTarget != null && detailPopupPanel.handlePageButtonClick(mouseX, mouseY);
    }

    public boolean isDetailConfirmClicked(double mouseX, double mouseY) {
        return isSelecting()
                && detailTarget != null
                && mouseX >= detailConfirmX
                && mouseX <= detailConfirmX + detailButtonWidth
                && mouseY >= detailButtonY
                && mouseY <= detailButtonY + detailButtonHeight;
    }

    public boolean isDetailBackClicked(double mouseX, double mouseY) {
        return isSelecting()
                && detailTarget != null
                && mouseX >= detailBackX
                && mouseX <= detailBackX + detailButtonWidth
                && mouseY >= detailButtonY
                && mouseY <= detailButtonY + detailButtonHeight;
    }

    public boolean isDoubleRentClicked(double mouseX, double mouseY) {
        return isSelecting()
                && detailTarget == null
                && game.hasDoubleTheRentCard(game.getCurrentPlayer())
                && game.getCurrentPlayer().getUseCardTimes() <= 1
                && mouseX >= 370 && mouseX <= 650
                && mouseY >= 450 && mouseY <= 485;
    }

    public boolean isCancelClicked(double mouseX, double mouseY) {
        if (!isSelecting()) {
            return false;
        }

        if (detailTarget != null) {
            return mouseX >= detailCancelX
                    && mouseX <= detailCancelX + detailButtonWidth
                    && mouseY >= detailButtonY
                    && mouseY <= detailButtonY + detailButtonHeight;
        }

        return mouseX >= 690 && mouseX <= 830
                && mouseY >= 535 && mouseY <= 575;
    }

    public boolean isConfirmClicked(double mouseX, double mouseY) {
        return isSelecting()
                && detailTarget == null
                && mouseX >= 500 && mouseX <= 660
                && mouseY >= 535 && mouseY <= 575;
    }

    public Player getClickedTarget(double mouseX, double mouseY) {
        if (!isSelecting() || detailTarget != null) {
            return null;
        }

        double x = panelX;
        double y = panelY + 80;
        double gap = 16;

        int displayIndex = 0;

        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (i == game.getCurrentPlayerIndex()) {
                continue;
            }

            double buttonY = y + displayIndex * (buttonHeight + gap);

            if (mouseX >= x && mouseX <= x + buttonWidth
                    && mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
                return game.getPlayers().get(i);
            }

            displayIndex++;
        }

        return null;
    }

    public PropertyColor getClickedColor(double mouseX, double mouseY) {
        if (!isSelecting() || detailTarget != null) {
            return null;
        }

        Player currentPlayer = game.getCurrentPlayer();

        double x = panelX + 300;
        double y = panelY + 80;
        double gapX = 12;
        double gapY = 12;
        int buttonsPerRow = 2;

        int displayIndex = 0;

        for (PropertyColor color : PropertyColor.values()) {
            if (!currentPlayer.canUseRentColor(color)) {
                continue;
            }

            int row = displayIndex / buttonsPerRow;
            int col = displayIndex % buttonsPerRow;

            double buttonX = x + col * (buttonWidth + gapX);
            double buttonY = y + row * (buttonHeight + gapY);

            if (mouseX >= buttonX && mouseX <= buttonX + buttonWidth
                    && mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
                return color;
            }

            displayIndex++;
        }

        return null;
    }

    public void draw(GraphicsContext gc) {
        if (!isSelecting()) {
            return;
        }

        if (detailTarget != null) {
            detailPopupPanel.draw(gc);
            ScreenDrawHelper.drawButton(gc, detailConfirmX, detailButtonY, detailButtonWidth, detailButtonHeight, "CONFIRM");
            ScreenDrawHelper.drawButton(gc, detailBackX, detailButtonY, detailButtonWidth, detailButtonHeight, "BACK");
            ScreenDrawHelper.drawButton(gc, detailCancelX, detailButtonY, detailButtonWidth, detailButtonHeight, "CANCEL");
            return;
        }

        drawOverlay(gc);
        drawTitle(gc);
        drawTargets(gc);
        drawColors(gc);
        drawStatus(gc);
        drawDoubleRentOption(gc);

        if (canConfirm()) {
            ScreenDrawHelper.drawButton(gc, 500, 535, 160, 40, "CONFIRM");
        } else {
            ScreenDrawHelper.drawDisabledButton(gc, 500, 535, 160, 40, "CONFIRM");
        }

        ScreenDrawHelper.drawButton(gc, 690, 535, 140, 40, "CANCEL");
    }

    private void drawOverlay(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.76));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
    }

    private void drawTitle(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 26));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("MULTI-COLOR RENT: Choose target and color", Game.SCREEN_WIDTH / 2, 35);

        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Click a player to view details first, then confirm the target.",
                Game.SCREEN_WIDTH / 2, 70);
    }

    private void drawTargets(GraphicsContext gc) {
        double x = panelX;
        double y = panelY + 80;
        double gap = 16;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Choose Target Player", x, panelY + 42);

        int displayIndex = 0;

        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (i == game.getCurrentPlayerIndex()) {
                continue;
            }

            Player player = game.getPlayers().get(i);
            double buttonY = y + displayIndex * (buttonHeight + gap);

            if (player == selectedTarget) {
                gc.setFill(Color.LIGHTGREEN);
            } else {
                gc.setFill(Color.LIGHTYELLOW);
            }

            gc.fillRoundRect(x, buttonY, buttonWidth, buttonHeight, 12, 12);

            gc.setStroke(Color.WHITE);
            gc.strokeRoundRect(x, buttonY, buttonWidth, buttonHeight, 12, 12);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 15));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText("Player " + (i + 1), x + buttonWidth / 2, buttonY + buttonHeight / 2);

            displayIndex++;
        }

        gc.setTextBaseline(VPos.TOP);
    }

    private void drawColors(GraphicsContext gc) {
        Player currentPlayer = game.getCurrentPlayer();

        double x = panelX + 300;
        double y = panelY + 80;
        double gapX = 12;
        double gapY = 12;
        int buttonsPerRow = 2;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Choose Rent Color", x, panelY + 42);

        int displayIndex = 0;

        for (PropertyColor color : PropertyColor.values()) {
            if (!currentPlayer.canUseRentColor(color)) {
                continue;
            }

            int row = displayIndex / buttonsPerRow;
            int col = displayIndex % buttonsPerRow;

            double buttonX = x + col * (buttonWidth + gapX);
            double buttonY = y + row * (buttonHeight + gapY);

            if (color == selectedColor) {
                gc.setFill(Color.LIGHTGREEN);
            } else {
                gc.setFill(Color.LIGHTBLUE);
            }

            gc.fillRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 12, 12);

            gc.setStroke(Color.WHITE);
            gc.strokeRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 12, 12);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 13));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(getDisplayColorName(color), buttonX + buttonWidth / 2, buttonY + buttonHeight / 2);

            displayIndex++;
        }

        if (displayIndex == 0) {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 17));
            gc.setTextAlign(TextAlignment.LEFT);
            gc.setTextBaseline(VPos.TOP);
            gc.fillText("You have no property color to charge rent.", x, y);
        }

        gc.setTextBaseline(VPos.TOP);
    }

    private void drawStatus(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);

        String targetText;

        if (selectedTarget == null) {
            targetText = "Target: not selected";
        } else {
            targetText = "Target: Player " + (game.getPlayers().indexOf(selectedTarget) + 1);
        }

        String colorText;

        if (selectedColor == null) {
            colorText = "Color: not selected";
        } else {
            colorText = "Color: " + getDisplayColorName(selectedColor);
        }

        gc.fillText(targetText + "     " + colorText, Game.SCREEN_WIDTH / 2, 485);

        if (selectedColor != null) {
            int rent = calculatePreviewRent(game.getCurrentPlayer(), selectedColor);
            gc.setFill(Color.LIGHTGREEN);
            gc.fillText("Rent Amount: " + rent + "M", Game.SCREEN_WIDTH / 2, 510);
        }
    }

    private void drawDoubleRentOption(GraphicsContext gc) {
        if (!game.hasDoubleTheRentCard(game.getCurrentPlayer())
                || game.getCurrentPlayer().getUseCardTimes() > 1) {
            return;
        }

        double x = 370;
        double y = 450;

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

    private int calculatePreviewRent(Player player, PropertyColor color) {
        return rentCalculator.calculateRent(player, color, useDoubleRent);
    }

    private String getDisplayColorName(PropertyColor color) {
        if (color == null) {
            return "No Color";
        }

        String[] words = color.name().toLowerCase().split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!result.isEmpty()) {
                result.append(" ");
            }

            result.append(word.substring(0, 1).toUpperCase());
            result.append(word.substring(1));
        }

        return result.toString();
    }

    public boolean isDetailCloseClicked(double mouseX, double mouseY) {
        return detailTarget != null && detailPopupPanel.isCloseClicked(mouseX, mouseY);
    }
}
