package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;
import model.ActionCards;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;

public class DealBreakerPanel {
    private final Game game;

    private ActionCards pendingDealBreakerCard;

    private final double panelX = 180;
    private final double panelY = 135;
    private final double cardWidth = 130;
    private final double cardHeight = 120;
    private final double cardGap = 20;

    public DealBreakerPanel(Game game) {
        this.game = game;
    }

    public void startSelection(ActionCards card) {
        pendingDealBreakerCard = card;
    }

    public void cancelSelection() {
        pendingDealBreakerCard = null;
    }

    public boolean isSelecting() {
        return pendingDealBreakerCard != null;
    }

    public ActionCards getPendingCard() {
        return pendingDealBreakerCard;
    }

    public boolean isCancelClicked(double mouseX, double mouseY) {
        return isSelecting()
                && mouseX >= 720 && mouseX <= 860
                && mouseY >= 505 && mouseY <= 545;
    }

    public GameScreen.DealBreakerChoice getClickedChoice(double mouseX, double mouseY) {
        if (!isSelecting()) {
            return null;
        }

        int displayIndex = 0;

        for (int playerIndex = 0; playerIndex < game.getPlayers().size(); playerIndex++) {
            if (playerIndex == game.getCurrentPlayerIndex()) {
                continue;
            }

            Player targetPlayer = game.getPlayers().get(playerIndex);

            for (PropertyColor color : PropertyColor.values()) {
                ArrayList<PropertiesCards> completeSet = PlayerInfoHelper.getCompleteSetByColor(targetPlayer, color);

                if (completeSet.isEmpty()) {
                    continue;
                }

                double x = panelX + (displayIndex % 5) * (cardWidth + cardGap);
                double y = panelY + (displayIndex / 5) * (cardHeight + 35);

                if (mouseX >= x && mouseX <= x + cardWidth
                        && mouseY >= y && mouseY <= y + cardHeight) {
                    return new GameScreen.DealBreakerChoice(targetPlayer, completeSet);
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

        drawOverlay(gc);
        drawTitle(gc);
        drawCompletedSets(gc);
        ScreenDrawHelper.drawButton(gc, 720, 505, 140, 40, "Cancel");
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
        gc.fillText("DEAL BREAKER: Choose one completed set to steal", Game.SCREEN_WIDTH / 2, 35);

        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Only completed property sets can be stolen.", Game.SCREEN_WIDTH / 2, 70);
    }

    private void drawCompletedSets(GraphicsContext gc) {
        int displayIndex = 0;

        for (int playerIndex = 0; playerIndex < game.getPlayers().size(); playerIndex++) {
            if (playerIndex == game.getCurrentPlayerIndex()) {
                continue;
            }

            Player targetPlayer = game.getPlayers().get(playerIndex);

            for (PropertyColor color : PropertyColor.values()) {
                ArrayList<PropertiesCards> completeSet = PlayerInfoHelper.getCompleteSetByColor(targetPlayer, color);

                if (completeSet.isEmpty()) {
                    continue;
                }

                double x = panelX + (displayIndex % 5) * (cardWidth + cardGap);
                double y = panelY + (displayIndex / 5) * (cardHeight + 35);

                drawCompletedSetCard(gc, playerIndex, color, completeSet, x, y);
                displayIndex++;
            }
        }

        if (displayIndex == 0) {
            drawNoCompletedSetMessage(gc);
        }
    }

    private void drawCompletedSetCard(GraphicsContext gc,
                                      int playerIndex,
                                      PropertyColor color,
                                      ArrayList<PropertiesCards> completeSet,
                                      double x,
                                      double y) {
        gc.setFill(Color.LIGHTGREEN);
        gc.fillRoundRect(x, y, cardWidth, cardHeight, 15, 15);

        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(x, y, cardWidth, cardHeight, 15, 15);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 13));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);

        gc.fillText("Player " + (playerIndex + 1), x + cardWidth / 2, y + 10);
        gc.fillText(color.name(), x + cardWidth / 2, y + 35);
        gc.fillText(completeSet.size() + "/" + color.getAmountToCompleteSet() + " Completed",
                x + cardWidth / 2, y + 60);

        gc.setFont(Font.font("Arial", 11));
        gc.fillText("Click to steal set", x + cardWidth / 2, y + 90);
    }

    private void drawNoCompletedSetMessage(GraphicsContext gc) {
        gc.setFill(Color.LIGHTYELLOW);
        gc.setFont(Font.font("Arial", 22));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("No player has a completed property set.", Game.SCREEN_WIDTH / 2, 260);
        gc.fillText("This Deal Breaker card cannot be used now.", Game.SCREEN_WIDTH / 2, 295);
    }
}
