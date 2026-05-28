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

import java.util.ArrayList;

public class SlyDealPanel {
    private final Game game;
    private ActionCards pendingCard;

    private final double panelX = 180;
    private final double panelY = 110;
    private final double cardWidth = 90;
    private final double cardHeight = 120;
    private final double gap = 15;

    private int pageIndex = 0;
    private final int cardsPerPage = 14;

    private final double prevX = 330;
    private final double nextX = 565;
    private final double pageY = 505;
    private final double pageButtonWidth = 90;
    private final double pageButtonHeight = 40;

    public SlyDealPanel(Game game) {
        this.game = game;
    }

    public void startSelection(ActionCards card) {
        pendingCard = card;
        pageIndex = 0;
    }

    public void cancelSelection() {
        pendingCard = null;
        pageIndex = 0;
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

        ArrayList<GameScreen.SlyDealChoice> choices = getAllChoices();

        int startIndex = pageIndex * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, choices.size());

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;

            int col = displayIndex % 7;
            int row = displayIndex / 7;

            double x = panelX + col * (cardWidth + gap);
            double y = panelY + row * (cardHeight + 35);

            if (mouseX >= x && mouseX <= x + cardWidth
                    && mouseY >= y && mouseY <= y + cardHeight) {
                return choices.get(i);
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
        ArrayList<GameScreen.SlyDealChoice> choices = getAllChoices();

        int maxPage = getMaxPage(choices.size());
        pageIndex = keepPageInRange(pageIndex, maxPage);

        int startIndex = pageIndex * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, choices.size());

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;

            int col = displayIndex % 7;
            int row = displayIndex / 7;

            double x = panelX + col * (cardWidth + gap);
            double y = panelY + row * (cardHeight + 35);

            GameScreen.SlyDealChoice choice = choices.get(i);
            int playerIndex = game.getPlayers().indexOf(choice.getTargetPlayer());

            drawChoiceCard(gc, playerIndex, choice.getSelectedCard(), x, y);
        }

        if (choices.isEmpty()) {
            drawEmptyMessage(gc);
        }

        drawPageButtons(gc, choices.size(), maxPage);
    }

    private void drawChoiceCard(GraphicsContext gc,
                                int playerIndex,
                                PropertiesCards card,
                                double x,
                                double y) {
        drawCardShadow(gc, x, y, cardWidth, cardHeight);

        if (!CardImageHelper.drawCardImage(gc, card, x, y, cardWidth, cardHeight)) {
            drawFallbackPropertyCard(gc, card, x, y);
        }

        drawOwnerBadge(gc, playerIndex, x, y);
        drawCurrentColorBadge(gc, card, x, y + cardHeight - 28);

        if (card.isWildCard()) {
            drawWildBadge(gc, x + cardWidth - 45, y + 6);
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

    private void drawCardShadow(GraphicsContext gc, double x, double y, double width, double height) {
        gc.setFill(Color.rgb(0, 0, 0, 0.35));
        gc.fillRoundRect(x + 4, y + 5, width, height, 15, 15);
    }

    private void drawFallbackPropertyCard(GraphicsContext gc, PropertiesCards card, double x, double y) {
        gc.setFill(Color.rgb(225, 241, 255));
        gc.fillRoundRect(x, y, cardWidth, cardHeight, 15, 15);

        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(x, y, cardWidth, cardHeight, 15, 15);

        gc.setFill(Color.rgb(25, 35, 50));
        gc.setFont(Font.font("Arial", 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);

        gc.fillText(card.getValue() + "M", x + cardWidth / 2, y + 35);

        String colorText = card.getCurrentColor() == null ? "NO COLOR" : card.getCurrentColor().name();
        ScreenDrawHelper.drawWrappedText(gc, colorText, x + 8, y + 58, cardWidth - 16, 12);
    }

    private void drawCurrentColorBadge(GraphicsContext gc, PropertiesCards card, double x, double y) {
        String colorText = card.getCurrentColor() == null
                ? "No Color"
                : getShortColorName(card.getCurrentColor());

        gc.setFill(Color.rgb(18, 24, 35, 0.86));
        gc.fillRoundRect(x + 6, y, cardWidth - 12, 22, 8, 8);

        gc.setStroke(Color.rgb(255, 255, 255, 0.65));
        gc.strokeRoundRect(x + 6, y, cardWidth - 12, 22, 8, 8);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 10));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(colorText, x + cardWidth / 2, y + 11);

        gc.setTextBaseline(VPos.TOP);
    }

    private void drawWildBadge(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.rgb(255, 80, 80));
        gc.fillRoundRect(x, y, 38, 18, 8, 8);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 9));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("WILD", x + 19, y + 9);

        gc.setTextBaseline(VPos.TOP);
    }

    private String getShortColorName(model.PropertyColor color) {
        return switch (color) {
            case DARK_BLUE -> "Dark Blue";
            case ORANGE -> "Orange";
            case BLACK -> "Black";
            case RED -> "Red";
            case DARK_GREEN -> "Dark Green";
            case BROWN -> "Brown";
            case PINK -> "Pink";
            case LIGHT_BLUE -> "Light Blue";
            case LIGHT_GREEN -> "Light Green";
            case YELLOW -> "Yellow";
        };
    }

    private ArrayList<GameScreen.SlyDealChoice> getAllChoices() {
        ArrayList<GameScreen.SlyDealChoice> choices = new ArrayList<>();

        for (int playerIndex = 0; playerIndex < game.getPlayers().size(); playerIndex++) {
            if (playerIndex == game.getCurrentPlayerIndex()) {
                continue;
            }

            Player targetPlayer = game.getPlayers().get(playerIndex);

            for (PropertiesCards card : targetPlayer.getPropertyCards()) {
                if (!PlayerInfoHelper.canBeStolenBySlyDeal(targetPlayer, card)) {
                    continue;
                }

                choices.add(new GameScreen.SlyDealChoice(targetPlayer, card));
            }
        }

        return choices;
    }

    private int getMaxPage(int size) {
        if (size <= 0) {
            return 0;
        }

        return (size - 1) / cardsPerPage;
    }

    private int keepPageInRange(int currentPage, int maxPage) {
        if (currentPage < 0) {
            return 0;
        }

        if (currentPage > maxPage) {
            return maxPage;
        }

        return currentPage;
    }

    public boolean isPrevPageClicked(double mouseX, double mouseY) {
        return isSelecting()
                && mouseX >= prevX && mouseX <= prevX + pageButtonWidth
                && mouseY >= pageY && mouseY <= pageY + pageButtonHeight;
    }

    public boolean isNextPageClicked(double mouseX, double mouseY) {
        return isSelecting()
                && mouseX >= nextX && mouseX <= nextX + pageButtonWidth
                && mouseY >= pageY && mouseY <= pageY + pageButtonHeight;
    }

    public void previousPage() {
        if (pageIndex > 0) {
            pageIndex--;
        }
    }

    public void nextPage() {
        int maxPage = getMaxPage(getAllChoices().size());

        if (pageIndex < maxPage) {
            pageIndex++;
        }
    }

    private void drawPageButtons(GraphicsContext gc, int totalChoices, int maxPage) {
        if (totalChoices <= cardsPerPage) {
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
}
