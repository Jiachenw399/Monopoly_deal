package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;
import model.ActionCards;
import model.Card;
import model.Player;
import model.PropertiesCards;

public class DebtCollectorPanel {
    private final Game game;

    private ActionCards pendingCard;

    private final double panelX = 170;
    private final double panelY = 135;
    private final double playerWidth = 160;
    private final double playerHeight = 230;
    private final double playerGap = 25;

    public DebtCollectorPanel(Game game) {
        this.game = game;
    }

    public void startSelection(ActionCards card) {
        pendingCard = card;
    }

    public void cancelSelection() {
        pendingCard = null;
    }

    public boolean isSelecting() {
        return pendingCard != null;
    }

    public ActionCards getPendingCard() {
        return pendingCard;
    }

    public boolean isCancelClicked(double mouseX, double mouseY) {
        return isSelecting()
                && mouseX >= 720 && mouseX <= 860
                && mouseY >= 505 && mouseY <= 545;
    }

    public Player getClickedTarget(double mouseX, double mouseY) {
        if (!isSelecting()) {
            return null;
        }

        int displayIndex = 0;

        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (i == game.getCurrentPlayerIndex()) {
                continue;
            }

            double x = panelX + displayIndex * (playerWidth + playerGap);
            double y = panelY;

            if (mouseX >= x && mouseX <= x + playerWidth
                    && mouseY >= y && mouseY <= y + playerHeight) {
                return game.getPlayers().get(i);
            }

            displayIndex++;
        }

        return null;
    }

    public void draw(GraphicsContext gc) {
        if (!isSelecting()) {
            return;
        }

        drawOverlay(gc);
        drawTitle(gc);
        drawPlayerBoxes(gc);
        ScreenDrawHelper.drawButton(gc, 720, 505, 140, 40, "CANCEL");
    }

    private void drawOverlay(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.75));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
    }

    private void drawTitle(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 26));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("DEBT COLLECTOR: Choose one player to collect 5M", Game.SCREEN_WIDTH / 2, 45);

        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Click a player card to collect money. Click CANCEL if you do not want to use this card.",
                Game.SCREEN_WIDTH / 2, 80);
    }

    private void drawPlayerBoxes(GraphicsContext gc) {
        int displayIndex = 0;

        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (i == game.getCurrentPlayerIndex()) {
                continue;
            }

            Player player = game.getPlayers().get(i);
            double x = panelX + displayIndex * (playerWidth + playerGap);
            double y = panelY;

            drawPlayerBox(gc, player, i, x, y);
            displayIndex++;
        }
    }

    private void drawPlayerBox(GraphicsContext gc, Player player, int playerIndex, double x, double y) {
        gc.setFill(Color.LIGHTYELLOW);
        gc.fillRoundRect(x, y, playerWidth, playerHeight, 18, 18);

        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(x, y, playerWidth, playerHeight, 18, 18);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 20));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Player " + (playerIndex + 1), x + playerWidth / 2, y + 15);

        gc.setFont(Font.font("Arial", 15));
        gc.fillText("Bank: " + PlayerInfoHelper.getBankTotal(player) + "M", x + playerWidth / 2, y + 50);
        gc.fillText("Properties: " + player.getPropertyCards().size(), x + playerWidth / 2, y + 75);

        drawMoneyCardsText(gc, player, x, y);
        drawPropertyColorsText(gc, player, x, y);
    }

    private void drawMoneyCardsText(GraphicsContext gc, Player player, double x, double y) {
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Arial", 13));

        double textX = x + 15;
        double textY = y + 110;

        gc.fillText("Money Cards:", textX, textY);

        if (player.getBankCards().isEmpty()) {
            gc.fillText("None", textX, textY + 22);
            return;
        }

        String moneyText = "";

        for (int i = 0; i < player.getBankCards().size(); i++) {
            if (i >= 6) {
                moneyText += "...";
                break;
            }

            Card card = player.getBankCards().get(i);
            moneyText += card.getValue() + "M ";
        }

        gc.fillText(moneyText, textX, textY + 22);
    }

    private void drawPropertyColorsText(GraphicsContext gc, Player player, double x, double y) {
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Arial", 13));

        double textX = x + 15;
        double textY = y + 165;

        gc.fillText("Property Colors:", textX, textY);

        if (player.getPropertyCards().isEmpty()) {
            gc.fillText("None", textX, textY + 22);
            return;
        }

        String propertyText = "";

        for (int i = 0; i < player.getPropertyCards().size(); i++) {
            if (i >= 4) {
                propertyText += "...";
                break;
            }

            PropertiesCards card = player.getPropertyCards().get(i);

            propertyText += PlayerInfoHelper.getShortColorName(card.getCurrentColor()) + " ";
        }

        gc.fillText(propertyText, textX, textY + 22);
    }
}
