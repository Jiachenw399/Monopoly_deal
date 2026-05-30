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

    private int pageIndex = 0;
    private final int setsPerPage = 10;

    private final double prevX = 330;
    private final double nextX = 565;
    private final double pageY = 505;
    private final double pageButtonWidth = 90;
    private final double pageButtonHeight = 40;

    private final PlayerDetailPopupPanel detailPopupPanel;
    private GameScreen.DealBreakerChoice detailChoice;

    private final double detailConfirmX = 390;
    private final double detailBackX = 555;
    private final double detailCancelX = 720;
    private final double detailButtonY = 615;
    private final double detailButtonWidth = 140;
    private final double detailButtonHeight = 40;

    public DealBreakerPanel(Game game) {
        this.game = game;
        this.detailPopupPanel = new PlayerDetailPopupPanel(game);
    }

    public void startSelection(ActionCards card) {
        pendingDealBreakerCard = card;
        pageIndex = 0;
        detailChoice = null;
        detailPopupPanel.close();
    }

    public void cancelSelection() {
        pendingDealBreakerCard = null;
        pageIndex = 0;
        detailChoice = null;
        detailPopupPanel.close();
    }

    public boolean isSelecting() {
        return pendingDealBreakerCard != null;
    }

    public ActionCards getPendingCard() {
        return pendingDealBreakerCard;
    }

    public boolean isCancelClicked(double mouseX, double mouseY) {
        if (!isSelecting()) {
            return false;
        }

        if (detailChoice != null) {
            return ScreenDrawHelper.isInside(mouseX, mouseY,
                    detailCancelX, detailButtonY, detailButtonWidth, detailButtonHeight);
        }

        return mouseX >= 720 && mouseX <= 860
                && mouseY >= 505 && mouseY <= 545;
    }

    public GameScreen.DealBreakerChoice getClickedChoice(double mouseX, double mouseY) {
        if (!isSelecting()) {
            return null;
        }

        ArrayList<GameScreen.DealBreakerChoice> choices = getAllChoices();

        int startIndex = pageIndex * setsPerPage;
        int endIndex = Math.min(startIndex + setsPerPage, choices.size());

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;

            double x = panelX + (displayIndex % 5) * (cardWidth + cardGap);
            double y = panelY + (displayIndex / 5) * (cardHeight + 35);

            if (ScreenDrawHelper.isInside(mouseX, mouseY, x, y, cardWidth, cardHeight)) {
                return choices.get(i);
            }
        }

        return null;
    }

    public void draw(GraphicsContext gc) {
        if (detailChoice != null) {
            detailPopupPanel.draw(gc);
            ScreenDrawHelper.drawButton(gc, detailConfirmX, detailButtonY, detailButtonWidth, detailButtonHeight, "CONFIRM");
            ScreenDrawHelper.drawButton(gc, detailBackX, detailButtonY, detailButtonWidth, detailButtonHeight, "BACK");
            ScreenDrawHelper.drawButton(gc, detailCancelX, detailButtonY, detailButtonWidth, detailButtonHeight, "CANCEL");
            return;
        }

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
        ArrayList<GameScreen.DealBreakerChoice> choices = getAllChoices();

        int maxPage = ScreenDrawHelper.getMaxPage(choices.size(), setsPerPage);
        pageIndex = ScreenDrawHelper.keepPageInRange(pageIndex, maxPage);

        int startIndex = pageIndex * setsPerPage;
        int endIndex = Math.min(startIndex + setsPerPage, choices.size());

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;

            GameScreen.DealBreakerChoice choice = choices.get(i);
            Player targetPlayer = choice.getTargetPlayer();
            int playerIndex = game.getPlayers().indexOf(targetPlayer);
            PropertyColor color = choice.getSelectedSet().get(0).getCurrentColor();

            double x = panelX + (displayIndex % 5) * (cardWidth + cardGap);
            double y = panelY + (displayIndex / 5) * (cardHeight + 35);

            drawCompletedSetCard(gc, playerIndex, color, choice.getSelectedSet(), x, y);
        }

        if (choices.isEmpty()) {
            drawNoCompletedSetMessage(gc);
        }

        drawPageButtons(gc, choices.size(), maxPage);
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

    private ArrayList<GameScreen.DealBreakerChoice> getAllChoices() {
        ArrayList<GameScreen.DealBreakerChoice> choices = new ArrayList<>();

        for (int playerIndex = 0; playerIndex < game.getPlayers().size(); playerIndex++) {
            if (playerIndex == game.getCurrentPlayerIndex()) {
                continue;
            }

            Player targetPlayer = game.getPlayers().get(playerIndex);

            for (PropertyColor color : PropertyColor.values()) {
                ArrayList<PropertiesCards> completeSet =
                        PlayerInfoHelper.getCompleteSetByColor(targetPlayer, color);

                if (!completeSet.isEmpty()) {
                    choices.add(new GameScreen.DealBreakerChoice(targetPlayer, completeSet));
                }
            }
        }

        return choices;
    }

    public boolean isPrevPageClicked(double mouseX, double mouseY) {
        return isSelecting()
                && ScreenDrawHelper.isInside(mouseX, mouseY, prevX, pageY,
                pageButtonWidth, pageButtonHeight);
    }

    public boolean isNextPageClicked(double mouseX, double mouseY) {
        return isSelecting()
                && ScreenDrawHelper.isInside(mouseX, mouseY, nextX, pageY,
                pageButtonWidth, pageButtonHeight);
    }

    public void previousPage() {
        if (pageIndex > 0) {
            pageIndex--;
        }
    }

    public void nextPage() {
        int maxPage = ScreenDrawHelper.getMaxPage(getAllChoices().size(), setsPerPage);

        if (pageIndex < maxPage) {
            pageIndex++;
        }
    }

    private void drawPageButtons(GraphicsContext gc, int totalChoices, int maxPage) {
        if (totalChoices <= setsPerPage) {
            return;
        }

        if (pageIndex > 0) {
            ScreenDrawHelper.drawButton(gc, prevX, pageY, pageButtonWidth, pageButtonHeight, "Prev");
        } else {
            ScreenDrawHelper.drawDisabledButton(gc, prevX, pageY, pageButtonWidth, pageButtonHeight, "Prev");
        }

        if (pageIndex < maxPage) {
            ScreenDrawHelper.drawButton(gc, nextX, pageY, pageButtonWidth, pageButtonHeight, "Next");
        } else {
            ScreenDrawHelper.drawDisabledButton(gc, nextX, pageY, pageButtonWidth, pageButtonHeight, "Next");
        }

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 15));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("Page " + (pageIndex + 1) + "/" + (maxPage + 1),
                Game.SCREEN_WIDTH / 2,
                pageY + pageButtonHeight / 2);

        gc.setTextBaseline(VPos.TOP);
    }

    public void showDetailChoice(GameScreen.DealBreakerChoice choice) {
        detailChoice = choice;

        if (choice == null) {
            detailPopupPanel.close();
            return;
        }

        int index = game.getPlayers().indexOf(choice.getTargetPlayer());
        detailPopupPanel.showPlayer(index);
    }

    public GameScreen.DealBreakerChoice getDetailChoice() {
        return detailChoice;
    }

    public boolean isDetailCloseClicked(double mouseX, double mouseY) {
        return detailChoice != null && detailPopupPanel.isCloseClicked(mouseX, mouseY);
    }

    public boolean handleDetailPageButtonClick(double mouseX, double mouseY) {
        return detailChoice != null && detailPopupPanel.handlePageButtonClick(mouseX, mouseY);
    }

    public boolean isDetailConfirmClicked(double mouseX, double mouseY) {
        return detailChoice != null
                && ScreenDrawHelper.isInside(mouseX, mouseY,
                detailConfirmX, detailButtonY, detailButtonWidth, detailButtonHeight);
    }

    public boolean isDetailBackClicked(double mouseX, double mouseY) {
        return detailChoice != null
                && ScreenDrawHelper.isInside(mouseX, mouseY,
                detailBackX, detailButtonY, detailButtonWidth, detailButtonHeight);
    }
}
