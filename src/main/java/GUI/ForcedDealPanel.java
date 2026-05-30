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

public class ForcedDealPanel {
    private final Game game;

    private ActionCards pendingCard;
    private Player selectedTargetPlayer;
    private PropertiesCards selectedMyCard;
    private PropertiesCards selectedTargetCard;

    private final double myPanelX = 95;
    private final double targetPanelX = 545;
    private final double cardStartY = 210;
    private final double cardWidth = 90;
    private final double cardHeight = 120;
    private final double cardGap = 15;

    private int myPageIndex = 0;
    private int targetPageIndex = 0;
    private final int cardsPerPage = 8;

    private final double myPrevX = 170;
    private final double myNextX = 270;
    private final double targetPrevX = 620;
    private final double targetNextX = 720;
    private final double pageButtonY = 505;
    private final double pageButtonWidth = 80;
    private final double pageButtonHeight = 32;

    private final double actionButtonY = 610;

    private final PlayerDetailPopupPanel detailPopupPanel;
    private Player detailTargetPlayer;

    private final double detailConfirmX = 390;
    private final double detailBackX = 555;
    private final double detailCancelX = 720;
    private final double detailButtonY = 550;
    private final double detailButtonWidth = 140;
    private final double detailButtonHeight = 40;

    public ForcedDealPanel(Game game) {
        this.game = game;
        this.detailPopupPanel = new PlayerDetailPopupPanel(game);
    }

    public void startSelection(ActionCards card) {
        pendingCard = card;
        selectedTargetPlayer = null;
        selectedMyCard = null;
        selectedTargetCard = null;
        detailTargetPlayer = null;
        detailPopupPanel.close();
        myPageIndex = 0;
        targetPageIndex = 0;
    }

    public void cancelSelection() {
        pendingCard = null;
        selectedTargetPlayer = null;
        selectedMyCard = null;
        selectedTargetCard = null;
        detailTargetPlayer = null;
        detailPopupPanel.close();
        myPageIndex = 0;
        targetPageIndex = 0;
    }

    public boolean isSelecting() {
        return pendingCard != null;
    }

    public ActionCards getPendingCard() {
        return pendingCard;
    }

    public Player getSelectedTargetPlayer() {
        return selectedTargetPlayer;
    }

    public PropertiesCards getSelectedMyCard() {
        return selectedMyCard;
    }

    public PropertiesCards getSelectedTargetCard() {
        return selectedTargetCard;
    }

    public void setSelectedTargetPlayer(Player targetPlayer) {
        selectedTargetPlayer = targetPlayer;
        selectedTargetCard = null;
        targetPageIndex = 0;
    }

    public void setSelectedMyCard(PropertiesCards card) {
        selectedMyCard = card;
    }

    public void setSelectedTargetCard(PropertiesCards card) {
        selectedTargetCard = card;
    }

    public boolean canConfirm() {
        return selectedTargetPlayer != null
                && selectedMyCard != null
                && selectedTargetCard != null;
    }

    public boolean isCancelClicked(double mouseX, double mouseY) {
        if (!isSelecting()) {
            return false;
        }

        if (detailTargetPlayer != null) {
            return mouseX >= detailCancelX
                    && mouseX <= detailCancelX + detailButtonWidth
                    && mouseY >= detailButtonY
                    && mouseY <= detailButtonY + detailButtonHeight;
        }

        return mouseX >= 720 && mouseX <= 860
                && mouseY >= actionButtonY && mouseY <= actionButtonY + 40;
    }

    public boolean isBackClicked(double mouseX, double mouseY) {
        return isSelecting()
                && selectedTargetPlayer != null
                && mouseX >= 560 && mouseX <= 700
                && mouseY >= actionButtonY && mouseY <= actionButtonY + 40;
    }

    public boolean isConfirmClicked(double mouseX, double mouseY) {
        return isSelecting()
                && mouseX >= 380 && mouseX <= 520
                && mouseY >= actionButtonY && mouseY <= actionButtonY + 40;
    }

    public Player getClickedTargetPlayer(double mouseX, double mouseY) {
        if (!isSelecting()) {
            return null;
        }

        double x = 235;
        double y = 180;
        double width = 150;
        double height = 70;
        double gap = 25;

        int displayIndex = 0;

        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (i == game.getCurrentPlayerIndex()) {
                continue;
            }

            double cardX = x + displayIndex * (width + gap);

            Player targetPlayer = game.getPlayers().get(i);

            if (mouseX >= cardX && mouseX <= cardX + width
                    && mouseY >= y && mouseY <= y + height
                    && hasExchangeableProperty(targetPlayer)) {
                return targetPlayer;
            }

            displayIndex++;
        }

        return null;
    }

    public PropertiesCards getClickedMyProperty(double mouseX, double mouseY) {
        if (!isSelecting() || selectedTargetPlayer == null) {
            return null;
        }

        return getClickedProperty(game.getCurrentPlayer(), myPanelX, mouseX, mouseY);
    }

    public PropertiesCards getClickedTargetProperty(double mouseX, double mouseY) {
        if (!isSelecting() || selectedTargetPlayer == null) {
            return null;
        }

        return getClickedProperty(selectedTargetPlayer, targetPanelX, mouseX, mouseY);
    }

    private PropertiesCards getClickedProperty(Player player, double startX, double mouseX, double mouseY) {
        ArrayList<PropertiesCards> cards = getExchangeableProperties(player);

        int page = startX == myPanelX ? myPageIndex : targetPageIndex;
        int startIndex = page * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, cards.size());

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;

            double x = startX + (displayIndex % 4) * (cardWidth + cardGap);
            double y = cardStartY + (displayIndex / 4) * (cardHeight + 32);

            if (mouseX >= x && mouseX <= x + cardWidth
                    && mouseY >= y && mouseY <= y + cardHeight) {
                return cards.get(i);
            }
        }

        return null;
    }

    public void draw(GraphicsContext gc) {
        if (detailTargetPlayer != null) {
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

        if (selectedTargetPlayer == null) {
            drawTargetPlayerChoices(gc);
        } else {
            drawExchangeChoices(gc);
        }

        drawButtons(gc);
        gc.setTextBaseline(VPos.TOP);
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
        gc.fillText("FORCED DEAL: Choose properties to exchange", Game.SCREEN_WIDTH / 2, 35);

        gc.setFont(Font.font("Arial", 16));
        gc.fillText("First choose a target player, then choose one of your properties and one target property.",
                Game.SCREEN_WIDTH / 2, 72);
    }

    private void drawTargetPlayerChoices(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 20));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Choose Target Player", Game.SCREEN_WIDTH / 2, 130);

        double x = 235;
        double y = 180;
        double width = 150;
        double height = 70;
        double gap = 25;

        int displayIndex = 0;

        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (i == game.getCurrentPlayerIndex()) {
                continue;
            }

            Player targetPlayer = game.getPlayers().get(i);
            double cardX = x + displayIndex * (width + gap);
            drawTargetPlayerBox(gc, targetPlayer, i, cardX, y, width, height);
            displayIndex++;
        }
    }

    private void drawTargetPlayerBox(GraphicsContext gc,
                                     Player targetPlayer,
                                     int playerIndex,
                                     double x,
                                     double y,
                                     double width,
                                     double height) {
        boolean usable = hasExchangeableProperty(targetPlayer);

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
        gc.fillText("Properties: " + targetPlayer.getPropertyCards().size(), x + width / 2, y + 42);
    }

    private boolean hasExchangeableProperty(Player player) {
        for (PropertiesCards card : player.getPropertyCards()) {
            if (PlayerInfoHelper.canBeStolenBySlyDeal(player, card)) {
                return true;
            }
        }

        return false;
    }

    private void drawExchangeChoices(GraphicsContext gc) {
        drawColumnTitle(gc, "Your Property", myPanelX, 155);
        drawColumnTitle(gc, getTargetTitle(), targetPanelX, 155);

        drawPropertyCards(gc, game.getCurrentPlayer(), myPanelX, selectedMyCard);
        drawPropertyCards(gc, selectedTargetPlayer, targetPanelX, selectedTargetCard);
        drawStatus(gc);
    }

    private String getTargetTitle() {
        int targetIndex = game.getPlayers().indexOf(selectedTargetPlayer) + 1;
        return "Player " + targetIndex + " Property";
    }

    private void drawColumnTitle(GraphicsContext gc, String title, double x, double y) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 20));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(title, x, y);

        gc.setFill(Color.rgb(255, 255, 255, 0.12));
        gc.fillRoundRect(x - 15, y + 35, 420, 270, 18, 18);
    }

    private void drawPropertyCards(GraphicsContext gc,
                                   Player player,
                                   double startX,
                                   PropertiesCards selectedCard) {
        ArrayList<PropertiesCards> cards = getExchangeableProperties(player);

        int page = startX == myPanelX ? myPageIndex : targetPageIndex;
        int maxPage = getMaxPage(cards.size());

        if (startX == myPanelX) {
            myPageIndex = keepPageInRange(myPageIndex, maxPage);
            page = myPageIndex;
        } else {
            targetPageIndex = keepPageInRange(targetPageIndex, maxPage);
            page = targetPageIndex;
        }

        int startIndex = page * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, cards.size());

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;

            double x = startX + (displayIndex % 4) * (cardWidth + cardGap);
            double y = cardStartY + (displayIndex / 4) * (cardHeight + 32);

            drawPropertyCard(gc, cards.get(i), x, y, cards.get(i) == selectedCard);
        }

        if (cards.isEmpty()) {
            gc.setFill(Color.LIGHTYELLOW);
            gc.setFont(Font.font("Arial", 17));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("No exchangeable property", startX + 195, cardStartY + 90);
        }

        drawPropertyPageButtons(gc, startX, cards.size(), page, maxPage);
    }

    private void drawPropertyCard(GraphicsContext gc,
                                  PropertiesCards card,
                                  double x,
                                  double y,
                                  boolean selected) {
        if (selected) {
            gc.setStroke(ScreenDrawHelper.ACCENT);
            gc.setLineWidth(5);
            gc.strokeRoundRect(x - 4, y - 4, cardWidth + 8, cardHeight + 8, 16, 16);
            gc.setLineWidth(1);
        }

        if (CardImageHelper.drawCardImage(gc, card, x, y, cardWidth, cardHeight)) {
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

        gc.fillText(card.getValue() + "M", x + cardWidth / 2, y + 10);

        String colorText = card.getCurrentColor() == null ? "NO COLOR" : card.getCurrentColor().name();
        ScreenDrawHelper.drawWrappedText(gc, colorText, x + 8, y + 38, cardWidth - 16, 12);

        if (card.isWildCard()) {
            gc.setFill(Color.RED);
            gc.setFont(Font.font("Arial", 11));
            gc.fillText("WILD", x + cardWidth / 2, y + 98);
        }
    }

    private void drawStatus(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);

        String myText = selectedMyCard == null ? "Your card: not selected" : "Your card: selected";
        String targetText = selectedTargetCard == null ? "Target card: not selected" : "Target card: selected";

        gc.fillText(myText + "     " + targetText, Game.SCREEN_WIDTH / 2, 480);
    }

    private void drawButtons(GraphicsContext gc) {
        if (selectedTargetPlayer != null) {
            ScreenDrawHelper.drawButton(gc, 380, actionButtonY, 140, 40, "CONFIRM");
            ScreenDrawHelper.drawButton(gc, 560, actionButtonY, 140, 40, "BACK");
        }

        ScreenDrawHelper.drawButton(gc, 720, actionButtonY, 140, 40, "CANCEL");
    }

    private ArrayList<PropertiesCards> getExchangeableProperties(Player player) {
        ArrayList<PropertiesCards> cards = new ArrayList<>();

        for (PropertiesCards card : player.getPropertyCards()) {
            if (PlayerInfoHelper.canBeStolenBySlyDeal(player, card)) {
                cards.add(card);
            }
        }

        return cards;
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

    public boolean isMyPrevPageClicked(double mouseX, double mouseY) {
        return isSelecting()
                && selectedTargetPlayer != null
                && mouseX >= myPrevX && mouseX <= myPrevX + pageButtonWidth
                && mouseY >= pageButtonY && mouseY <= pageButtonY + pageButtonHeight;
    }

    public boolean isMyNextPageClicked(double mouseX, double mouseY) {
        return isSelecting()
                && selectedTargetPlayer != null
                && mouseX >= myNextX && mouseX <= myNextX + pageButtonWidth
                && mouseY >= pageButtonY && mouseY <= pageButtonY + pageButtonHeight;
    }

    public boolean isTargetPrevPageClicked(double mouseX, double mouseY) {
        return isSelecting()
                && selectedTargetPlayer != null
                && mouseX >= targetPrevX && mouseX <= targetPrevX + pageButtonWidth
                && mouseY >= pageButtonY && mouseY <= pageButtonY + pageButtonHeight;
    }

    public boolean isTargetNextPageClicked(double mouseX, double mouseY) {
        return isSelecting()
                && selectedTargetPlayer != null
                && mouseX >= targetNextX && mouseX <= targetNextX + pageButtonWidth
                && mouseY >= pageButtonY && mouseY <= pageButtonY + pageButtonHeight;
    }

    public void previousMyPage() {
        if (myPageIndex > 0) {
            myPageIndex--;
        }
    }

    public void nextMyPage() {
        int maxPage = getMaxPage(getExchangeableProperties(game.getCurrentPlayer()).size());

        if (myPageIndex < maxPage) {
            myPageIndex++;
        }
    }

    public void previousTargetPage() {
        if (targetPageIndex > 0) {
            targetPageIndex--;
        }
    }

    public void nextTargetPage() {
        if (selectedTargetPlayer == null) {
            return;
        }

        int maxPage = getMaxPage(getExchangeableProperties(selectedTargetPlayer).size());

        if (targetPageIndex < maxPage) {
            targetPageIndex++;
        }
    }

    private void drawPropertyPageButtons(GraphicsContext gc,
                                         double startX,
                                         int totalCards,
                                         int currentPage,
                                         int maxPage) {
        if (totalCards <= cardsPerPage) {
            return;
        }

        double prevButtonX = startX == myPanelX ? myPrevX : targetPrevX;
        double nextButtonX = startX == myPanelX ? myNextX : targetNextX;

        if (currentPage > 0) {
            ScreenDrawHelper.drawButton(gc, prevButtonX, pageButtonY, pageButtonWidth, pageButtonHeight, "Prev");
        } else {
            ScreenDrawHelper.drawDisabledButton(gc, prevButtonX, pageButtonY, pageButtonWidth, pageButtonHeight, "Prev");
        }

        if (currentPage < maxPage) {
            ScreenDrawHelper.drawButton(gc, nextButtonX, pageButtonY, pageButtonWidth, pageButtonHeight, "Next");
        } else {
            ScreenDrawHelper.drawDisabledButton(gc, nextButtonX, pageButtonY, pageButtonWidth, pageButtonHeight, "Next");
        }

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 13));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("Page " + (currentPage + 1) + "/" + (maxPage + 1),
                prevButtonX + 90,
                pageButtonY + pageButtonHeight / 2);

        gc.setTextBaseline(VPos.TOP);
    }

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

    public boolean isDetailCloseClicked(double mouseX, double mouseY) {
        return detailTargetPlayer != null && detailPopupPanel.isCloseClicked(mouseX, mouseY);
    }

    public boolean handleDetailPageButtonClick(double mouseX, double mouseY) {
        return detailTargetPlayer != null && detailPopupPanel.handlePageButtonClick(mouseX, mouseY);
    }

    public boolean isDetailConfirmClicked(double mouseX, double mouseY) {
        return detailTargetPlayer != null
                && mouseX >= detailConfirmX && mouseX <= detailConfirmX + detailButtonWidth
                && mouseY >= detailButtonY && mouseY <= detailButtonY + detailButtonHeight;
    }

    public boolean isDetailBackClicked(double mouseX, double mouseY) {
        return detailTargetPlayer != null
                && mouseX >= detailBackX && mouseX <= detailBackX + detailButtonWidth
                && mouseY >= detailButtonY && mouseY <= detailButtonY + detailButtonHeight;
    }
}
