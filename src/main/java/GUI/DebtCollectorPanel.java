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

public class DebtCollectorPanel {
    private final Game game;
    private final PlayerDetailPopupPanel detailPopupPanel;

    private ActionCards pendingCard;
    private Player selectedTarget;

    private final double targetX = 230;
    private final double targetY = 165;
    private final double targetWidth = 165;
    private final double targetHeight = 95;
    private final double targetGap = 25;

    private final double confirmX = 390;
    private final double confirmY = 550;
    private final double backX = 555;
    private final double backY = 550;
    private final double cancelX = 720;
    private final double cancelY = 550;
    private final double buttonWidth = 140;
    private final double buttonHeight = 40;

    public DebtCollectorPanel(Game game) {
        this.game = game;
        this.detailPopupPanel = new PlayerDetailPopupPanel(game);
    }

    public void startSelection(ActionCards card) {
        pendingCard = card;
        selectedTarget = null;
        detailPopupPanel.close();
    }

    public void cancelSelection() {
        pendingCard = null;
        selectedTarget = null;
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

    public void setSelectedTarget(Player player) {
        selectedTarget = player;

        if (player == null) {
            detailPopupPanel.close();
            return;
        }

        int index = game.getPlayers().indexOf(player);
        detailPopupPanel.showPlayer(index);
    }

    public Player getClickedTarget(double mouseX, double mouseY) {
        if (!isSelecting() || selectedTarget != null) {
            return null;
        }

        int displayIndex = 0;

        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (i == game.getCurrentPlayerIndex()) {
                continue;
            }

            double x = targetX + displayIndex * (targetWidth + targetGap);

            if (mouseX >= x && mouseX <= x + targetWidth
                    && mouseY >= targetY && mouseY <= targetY + targetHeight) {
                return game.getPlayers().get(i);
            }

            displayIndex++;
        }

        return null;
    }

    public boolean isCancelClicked(double mouseX, double mouseY) {
        return isSelecting()
                && mouseX >= cancelX && mouseX <= cancelX + buttonWidth
                && mouseY >= cancelY && mouseY <= cancelY + buttonHeight;
    }

    public boolean isBackClicked(double mouseX, double mouseY) {
        return isSelecting()
                && selectedTarget != null
                && mouseX >= backX && mouseX <= backX + buttonWidth
                && mouseY >= backY && mouseY <= backY + buttonHeight;
    }

    public boolean isConfirmClicked(double mouseX, double mouseY) {
        return isSelecting()
                && selectedTarget != null
                && mouseX >= confirmX && mouseX <= confirmX + buttonWidth
                && mouseY >= confirmY && mouseY <= confirmY + buttonHeight;
    }

    public boolean handleDetailPageButtonClick(double mouseX, double mouseY) {
        return selectedTarget != null && detailPopupPanel.handlePageButtonClick(mouseX, mouseY);
    }

    public boolean isDetailCloseClicked(double mouseX, double mouseY) {
        return selectedTarget != null && detailPopupPanel.isCloseClicked(mouseX, mouseY);
    }

    public void draw(GraphicsContext gc) {
        if (!isSelecting()) {
            return;
        }

        if (selectedTarget == null) {
            drawOverlay(gc);
            drawTitle(gc);
            drawTargetChoices(gc);
            ScreenDrawHelper.drawButton(gc, cancelX, cancelY, buttonWidth, buttonHeight, "CANCEL");
        } else {
            detailPopupPanel.draw(gc);
            ScreenDrawHelper.drawButton(gc, confirmX, confirmY, buttonWidth, buttonHeight, "CONFIRM");
            ScreenDrawHelper.drawButton(gc, backX, backY, buttonWidth, buttonHeight, "BACK");
            ScreenDrawHelper.drawButton(gc, cancelX, cancelY, buttonWidth, buttonHeight, "CANCEL");
        }

        gc.setTextBaseline(VPos.TOP);
    }

    private void drawOverlay(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.78));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
    }

    private void drawTitle(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 26));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("DEBT COLLECTOR", Game.SCREEN_WIDTH / 2, 38);

        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Choose one player first. Then check details and confirm.",
                Game.SCREEN_WIDTH / 2, 75);
    }

    private void drawTargetChoices(GraphicsContext gc) {
        int displayIndex = 0;

        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (i == game.getCurrentPlayerIndex()) {
                continue;
            }

            Player player = game.getPlayers().get(i);
            double x = targetX + displayIndex * (targetWidth + targetGap);

            drawTargetCard(gc, player, i, x, targetY);
            displayIndex++;
        }
    }

    private void drawTargetCard(GraphicsContext gc, Player player, int playerIndex, double x, double y) {
        gc.setFill(Color.rgb(255, 252, 220));
        gc.fillRoundRect(x, y, targetWidth, targetHeight, 16, 16);

        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(x, y, targetWidth, targetHeight, 16, 16);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 19));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Player " + (playerIndex + 1), x + targetWidth / 2, y + 14);

        gc.setFont(Font.font("Arial", 14));
        gc.fillText("Bank: " + PlayerInfoHelper.getBankTotal(player) + "M", x + targetWidth / 2, y + 48);
        gc.fillText("Properties: " + player.getPropertyCards().size(), x + targetWidth / 2, y + 70);
    }
}
