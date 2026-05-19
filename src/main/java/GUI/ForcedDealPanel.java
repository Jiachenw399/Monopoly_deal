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

    public ForcedDealPanel(Game game) {
        this.game = game;
    }

    public void startSelection(ActionCards card) {
        pendingCard = card;
        selectedTargetPlayer = null;
        selectedMyCard = null;
        selectedTargetCard = null;
    }

    public void cancelSelection() {
        pendingCard = null;
        selectedTargetPlayer = null;
        selectedMyCard = null;
        selectedTargetCard = null;
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
        return isSelecting()
                && mouseX >= 720 && mouseX <= 860
                && mouseY >= 505 && mouseY <= 545;
    }

    public boolean isBackClicked(double mouseX, double mouseY) {
        return isSelecting()
                && selectedTargetPlayer != null
                && mouseX >= 560 && mouseX <= 700
                && mouseY >= 505 && mouseY <= 545;
    }

    public boolean isConfirmClicked(double mouseX, double mouseY) {
        return isSelecting()
                && mouseX >= 380 && mouseX <= 520
                && mouseY >= 505 && mouseY <= 545;
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
        int displayIndex = 0;

        for (PropertiesCards card : player.getPropertyCards()) {
            if (!PlayerInfoHelper.canBeStolenBySlyDeal(player, card)) {
                continue;
            }

            double x = startX + (displayIndex % 4) * (cardWidth + cardGap);
            double y = cardStartY + (displayIndex / 4) * (cardHeight + 32);

            if (mouseX >= x && mouseX <= x + cardWidth
                    && mouseY >= y && mouseY <= y + cardHeight) {
                return card;
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
        int displayIndex = 0;

        for (PropertiesCards card : player.getPropertyCards()) {
            if (!PlayerInfoHelper.canBeStolenBySlyDeal(player, card)) {
                continue;
            }

            double x = startX + (displayIndex % 4) * (cardWidth + cardGap);
            double y = cardStartY + (displayIndex / 4) * (cardHeight + 32);

            drawPropertyCard(gc, card, x, y, card == selectedCard);
            displayIndex++;
        }

        if (displayIndex == 0) {
            gc.setFill(Color.LIGHTYELLOW);
            gc.setFont(Font.font("Arial", 17));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("No exchangeable property", startX + 195, cardStartY + 90);
        }
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

        gc.fillText(myText + "     " + targetText, Game.SCREEN_WIDTH / 2, 455);
    }

    private void drawButtons(GraphicsContext gc) {
        if (selectedTargetPlayer != null) {
            ScreenDrawHelper.drawButton(gc, 380, 505, 140, 40, "CONFIRM");
            ScreenDrawHelper.drawButton(gc, 560, 505, 140, 40, "BACK");
        }

        ScreenDrawHelper.drawButton(gc, 720, 505, 140, 40, "CANCEL");
    }
}
