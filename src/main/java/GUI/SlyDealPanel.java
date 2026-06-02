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
    private final PlayerDetailPopupPanel detailPopupPanel;
    private ActionCards pendingCard;
    private Player selectedTargetPlayer;
    private Player detailTargetPlayer;

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

    private final double detailConfirmX = 390;
    private final double detailBackX = 555;
    private final double detailCancelX = 720;
    private final double detailButtonY = 615;
    private final double detailButtonWidth = 140;
    private final double detailButtonHeight = 40;

    // Creates a SlyDealPanel instance.
    public SlyDealPanel(Game game) {
        this.game = game;
        this.detailPopupPanel = new PlayerDetailPopupPanel(game);
    }

    // Starts selection.
    public void startSelection(ActionCards card) {
        pendingCard = card;
        selectedTargetPlayer = null;
        detailTargetPlayer = null;
        detailPopupPanel.close();
        pageIndex = 0;
    }

    // Checks whether this can cel selection.
    public void cancelSelection() {
        pendingCard = null;
        selectedTargetPlayer = null;
        detailTargetPlayer = null;
        detailPopupPanel.close();
        pageIndex = 0;
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
        if (!isSelecting()) {
            return false;
        }

        if (detailTargetPlayer != null) {
            return ScreenDrawHelper.handleButtonClick(mouseX, mouseY,
                    detailCancelX, detailButtonY, detailButtonWidth, detailButtonHeight);
        }

        return isSelecting()
                && ScreenDrawHelper.handleButtonClick(mouseX, mouseY, 720, 505, 140, 40);
    }

    // Finds clicked choice.
    public GameScreen.SlyDealChoice getClickedChoice(double mouseX, double mouseY) {
        if (!isSelecting() || selectedTargetPlayer == null || detailTargetPlayer != null) {
            return null;
        }

        ArrayList<PropertiesCards> choices = getStealableProperties(selectedTargetPlayer);

        int startIndex = pageIndex * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, choices.size());

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;

            int col = displayIndex % 7;
            int row = displayIndex / 7;

            double x = panelX + col * (cardWidth + gap);
            double y = panelY + row * (cardHeight + 35);

            if (ScreenDrawHelper.isInside(mouseX, mouseY, x, y, cardWidth, cardHeight)) {
                return new GameScreen.SlyDealChoice(selectedTargetPlayer, choices.get(i));
            }
        }

        return null;
    }

    // Finds clicked target player.
    public Player getClickedTargetPlayer(double mouseX, double mouseY) {
        if (!isSelecting() || selectedTargetPlayer != null || detailTargetPlayer != null) {
            return null;
        }

        double x = 235;
        double y = 180;
        double width = 150;
        double height = 80;
        double gap = 25;
        int displayIndex = 0;

        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (i == game.getCurrentPlayerIndex()) {
                continue;
            }

            Player player = game.getPlayers().get(i);
            double cardX = x + displayIndex * (width + gap);

            if (ScreenDrawHelper.isInside(mouseX, mouseY, cardX, y, width, height)
                    && hasStealableProperty(player)) {
                return player;
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

        if (detailTargetPlayer != null) {
            detailPopupPanel.draw(gc);
            ScreenDrawHelper.drawButton(gc, detailConfirmX, detailButtonY, detailButtonWidth, detailButtonHeight, "CONFIRM");
            ScreenDrawHelper.drawButton(gc, detailBackX, detailButtonY, detailButtonWidth, detailButtonHeight, "BACK");
            ScreenDrawHelper.drawButton(gc, detailCancelX, detailButtonY, detailButtonWidth, detailButtonHeight, "CANCEL");
            return;
        }

        drawBackground(gc);
        drawTitle(gc);
        if (selectedTargetPlayer == null) {
            drawTargetPlayerChoices(gc);
        } else {
            drawChoices(gc);
            ScreenDrawHelper.drawButton(gc, 560, 505, 140, 40, "BACK");
        }
        ScreenDrawHelper.drawButton(gc, 720, 505, 140, 40, "CANCEL");
    }

    // Draws background.
    private void drawBackground(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.75));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
    }

    // Draws title.
    private void drawTitle(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 26));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("SLY DEAL: Choose a target player", Game.SCREEN_WIDTH / 2, 35);

        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Choose a player first, check details, then choose one property to steal.",
                Game.SCREEN_WIDTH / 2, 70);
    }

    // Draws target player choices.
    private void drawTargetPlayerChoices(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 20));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Choose Target Player", Game.SCREEN_WIDTH / 2, 130);

        double x = 235;
        double y = 180;
        double width = 150;
        double height = 80;
        double gap = 25;
        int displayIndex = 0;

        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (i == game.getCurrentPlayerIndex()) {
                continue;
            }

            Player player = game.getPlayers().get(i);
            double cardX = x + displayIndex * (width + gap);
            drawTargetPlayerBox(gc, player, i, cardX, y, width, height);
            displayIndex++;
        }
    }

    // Draws target player box.
    private void drawTargetPlayerBox(GraphicsContext gc,
                                     Player player,
                                     int playerIndex,
                                     double x,
                                     double y,
                                     double width,
                                     double height) {
        boolean usable = hasStealableProperty(player);

        gc.setFill(usable ? Color.LIGHTYELLOW : Color.GRAY);
        gc.fillRoundRect(x, y, width, height, 16, 16);

        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(x, y, width, height, 16, 16);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 18));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Player " + (playerIndex + 1), x + width / 2, y + 12);

        gc.setFont(Font.font("Arial", 13));
        gc.fillText("Stealable: " + getStealableProperties(player).size(), x + width / 2, y + 43);
    }

    // Draws choices.
    private void drawChoices(GraphicsContext gc) {
        ArrayList<PropertiesCards> choices = getStealableProperties(selectedTargetPlayer);
        int targetIndex = game.getPlayers().indexOf(selectedTargetPlayer) + 1;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 20));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Choose one property from Player " + targetIndex, Game.SCREEN_WIDTH / 2, 105);

        int maxPage = ScreenDrawHelper.getMaxPage(choices.size(), cardsPerPage);
        pageIndex = ScreenDrawHelper.keepPageInRange(pageIndex, maxPage);

        int startIndex = pageIndex * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, choices.size());

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;

            int col = displayIndex % 7;
            int row = displayIndex / 7;

            double x = panelX + col * (cardWidth + gap);
            double y = panelY + row * (cardHeight + 35);

            drawChoiceCard(gc, targetIndex - 1, choices.get(i), x, y);
        }

        if (choices.isEmpty()) {
            drawEmptyMessage(gc);
        }

        drawPageButtons(gc, choices.size(), maxPage);
    }

    // Draws choice card.
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

    // Draws owner badge.
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

    // Draws empty message.
    private void drawEmptyMessage(GraphicsContext gc) {
        gc.setFill(Color.LIGHTYELLOW);
        gc.setFont(Font.font("Arial", 22));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("No property can be stolen.", Game.SCREEN_WIDTH / 2, 280);
    }

    // Draws card shadow.
    private void drawCardShadow(GraphicsContext gc, double x, double y, double width, double height) {
        gc.setFill(Color.rgb(0, 0, 0, 0.35));
        gc.fillRoundRect(x + 4, y + 5, width, height, 15, 15);
    }

    // Draws fallback property card.
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

    // Draws current color badge.
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

    // Draws wild badge.
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

    // Finds short color name.
    private String getShortColorName(model.PropertyColor color) {
        return ScreenDrawHelper.getDisplayColorName(color);
    }

    // Finds stealable properties.
    private ArrayList<PropertiesCards> getStealableProperties(Player targetPlayer) {
        ArrayList<PropertiesCards> choices = new ArrayList<>();

        if (targetPlayer == null) {
            return choices;
        }

        for (PropertiesCards card : targetPlayer.getPropertyCards()) {
            if (PlayerInfoHelper.canBeStolenBySlyDeal(targetPlayer, card)) {
                choices.add(card);
            }
        }

        return choices;
    }

    // Checks whether this has stealable property.
    private boolean hasStealableProperty(Player player) {
        return !getStealableProperties(player).isEmpty();
    }

    // Checks whether prev page clicked.
    public boolean isPrevPageClicked(double mouseX, double mouseY) {
        return isSelecting()
                && selectedTargetPlayer != null
                && detailTargetPlayer == null
                && ScreenDrawHelper.handleButtonClick(mouseX, mouseY, prevX, pageY,
                pageButtonWidth, pageButtonHeight);
    }

    // Checks whether next page clicked.
    public boolean isNextPageClicked(double mouseX, double mouseY) {
        return isSelecting()
                && selectedTargetPlayer != null
                && detailTargetPlayer == null
                && ScreenDrawHelper.handleButtonClick(mouseX, mouseY, nextX, pageY,
                pageButtonWidth, pageButtonHeight);
    }

    // Runs previous page.
    public void previousPage() {
        if (pageIndex > 0) {
            pageIndex--;
        }
    }

    // Runs next page.
    public void nextPage() {
        int maxPage = ScreenDrawHelper.getMaxPage(getStealableProperties(selectedTargetPlayer).size(), cardsPerPage);

        if (pageIndex < maxPage) {
            pageIndex++;
        }
    }

    // Draws page buttons.
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

    // Shows target detail.
    public void showTargetDetail(Player player) {
        detailTargetPlayer = player;

        if (player == null) {
            detailPopupPanel.close();
            return;
        }

        int index = game.getPlayers().indexOf(player);
        detailPopupPanel.showPlayer(index);
    }

    public Player getDetailTargetPlayer() {
        return detailTargetPlayer;
    }

    public Player getSelectedTargetPlayer() {
        return selectedTargetPlayer;
    }

    // Runs set selected target player.
    public void setSelectedTargetPlayer(Player player) {
        selectedTargetPlayer = player;
        pageIndex = 0;
    }

    // Checks whether detail close clicked.
    public boolean isDetailCloseClicked(double mouseX, double mouseY) {
        return detailTargetPlayer != null && detailPopupPanel.isCloseClicked(mouseX, mouseY);
    }

    // Checks whether detail back clicked.
    public boolean isDetailBackClicked(double mouseX, double mouseY) {
        return detailTargetPlayer != null
                && ScreenDrawHelper.handleButtonClick(mouseX, mouseY,
                detailBackX, detailButtonY, detailButtonWidth, detailButtonHeight);
    }

    // Checks whether detail confirm clicked.
    public boolean isDetailConfirmClicked(double mouseX, double mouseY) {
        return detailTargetPlayer != null
                && ScreenDrawHelper.handleButtonClick(mouseX, mouseY,
                detailConfirmX, detailButtonY, detailButtonWidth, detailButtonHeight);
    }

    // Handles detail page button click.
    public boolean handleDetailPageButtonClick(double mouseX, double mouseY) {
        return detailTargetPlayer != null && detailPopupPanel.handlePageButtonClick(mouseX, mouseY);
    }

    // Checks whether back clicked.
    public boolean isBackClicked(double mouseX, double mouseY) {
        return isSelecting()
                && selectedTargetPlayer != null
                && detailTargetPlayer == null
                && ScreenDrawHelper.handleButtonClick(mouseX, mouseY, 560, 505, 140, 40);
    }
}
