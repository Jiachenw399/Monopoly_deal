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
import model.PropertiesCards;

public class SlyDealPanel {
    private final Game game;
    private ActionCards pendingCard;

    private final double panelX = 180;
    private final double panelY = 110;
    private final double cardWidth = 90;
    private final double cardHeight = 120;
    private final double gap = 15;

    public SlyDealPanel(Game game) {
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

    public GameScreen.SlyDealChoice getClickedChoice(double mouseX, double mouseY) {
        if (!isSelecting()) {
            return null;
        }

        int displayIndex = 0;

        for (int playerIndex = 0; playerIndex < game.getPlayers().size(); playerIndex++) {
            if (playerIndex == game.getCurrentPlayerIndex()) {
                continue;
            }

            Player targetPlayer = game.getPlayers().get(playerIndex);

            for (PropertiesCards card : targetPlayer.getPropertyCards()) {
                if (!PlayerInfoHelper.canBeStolenBySlyDeal(targetPlayer, card)) {
                    continue;
                }

                int col = displayIndex % 7;
                int row = displayIndex / 7;

                double x = panelX + col * (cardWidth + gap);
                double y = panelY + row * (cardHeight + 35);

                if (mouseX >= x && mouseX <= x + cardWidth
                        && mouseY >= y && mouseY <= y + cardHeight) {
                    return new GameScreen.SlyDealChoice(targetPlayer, card);
                }

                displayIndex++;
            }
        }

        return null;
    }

    public void draw(GraphicsContext gc) {
        if (!isSelecting()) {
            return;
        }

        drawBackground(gc);
        drawTitle(gc);
        drawChoices(gc);
        ScreenDrawHelper.drawButton(gc, 720, 505, 140, 40, "CANCEL");
    }

    private void drawBackground(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.75));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
    }

    private void drawTitle(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 26));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("SLY DEAL: Choose one property to steal", Game.SCREEN_WIDTH / 2, 35);

        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Completed sets cannot be stolen. Wild cards can be stolen if they are not in a completed set.",
                Game.SCREEN_WIDTH / 2, 70);
    }

    private void drawChoices(GraphicsContext gc) {
        int displayIndex = 0;

        for (int playerIndex = 0; playerIndex < game.getPlayers().size(); playerIndex++) {
            if (playerIndex == game.getCurrentPlayerIndex()) {
                continue;
            }

            Player targetPlayer = game.getPlayers().get(playerIndex);

            for (PropertiesCards card : targetPlayer.getPropertyCards()) {
                if (!PlayerInfoHelper.canBeStolenBySlyDeal(targetPlayer, card)) {
                    continue;
                }

                int col = displayIndex % 7;
                int row = displayIndex / 7;

                double x = panelX + col * (cardWidth + gap);
                double y = panelY + row * (cardHeight + 35);

                drawChoiceCard(gc, playerIndex, card, x, y);
                displayIndex++;
            }
        }

        if (displayIndex == 0) {
            drawEmptyMessage(gc);
        }
    }

    private void drawChoiceCard(GraphicsContext gc,
                                int playerIndex,
                                PropertiesCards card,
                                double x,
                                double y) {
        if (CardImageHelper.drawCardImage(gc, card, x, y, cardWidth, cardHeight)) {
            drawOwnerBadge(gc, playerIndex, x, y);
            return;
        }

        gc.setFill(Color.LIGHTBLUE);
        gc.fillRoundRect(x, y, cardWidth, cardHeight, 15, 15);

        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(x, y, cardWidth, cardHeight, 15, 15);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);

        gc.fillText("Player " + (playerIndex + 1), x + cardWidth / 2, y + 10);
        gc.fillText(card.getValue() + "M", x + cardWidth / 2, y + 32);

        String colorText = card.getCurrentColor() == null ? "NO COLOR" : card.getCurrentColor().name();
        ScreenDrawHelper.drawWrappedText(gc, colorText, x + 8, y + 55, cardWidth - 16, 12);

        if (card.isWildCard()) {
            gc.setFill(Color.RED);
            gc.setFont(Font.font("Arial", 11));
            gc.fillText("WILD", x + cardWidth / 2, y + 100);
        }
    }

    private void drawOwnerBadge(GraphicsContext gc, int playerIndex, double x, double y) {
        gc.setFill(Color.rgb(255, 255, 255, 0.88));
        gc.fillRoundRect(x + 6, y + 6, 54, 22, 8, 8);

        gc.setStroke(Color.rgb(30, 35, 48));
        gc.strokeRoundRect(x + 6, y + 6, 54, 22, 8, 8);

        gc.setFill(Color.rgb(30, 35, 48));
        gc.setFont(Font.font("Arial", 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("P" + (playerIndex + 1), x + 33, y + 17);

        gc.setTextBaseline(VPos.TOP);
    }

    private void drawEmptyMessage(GraphicsContext gc) {
        gc.setFill(Color.LIGHTYELLOW);
        gc.setFont(Font.font("Arial", 22));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("No property can be stolen.", Game.SCREEN_WIDTH / 2, 280);
    }
}
