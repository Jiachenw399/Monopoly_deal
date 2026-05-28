package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;
import logic.PlayerInfoHelper;
import model.Card;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;

// Handles the payment selection popup when a player must pay assets.
public class PaymentSelectionPanel {
    private final Game game;
    private final ArrayList<Card> selectedCards;

    private final double bankStartX = 60;
    private final double bankStartY = 180;
    private final double propertyStartX = 60;
    private final double propertyStartY = 365;
    private final double cardWidth = 68;
    private final double cardHeight = 93;
    private final double cardGapX = 90;

    private final int cardsPerPage = 7;

    private int bankPageIndex = 0;
    private int propertyPageIndex = 0;
    private Game.PaymentRequest lastRequest = null;

    private final double bankPrevX = 535;
    private final double bankNextX = 585;
    private final double bankArrowY = 142;

    private final double propertyPrevX = 535;
    private final double propertyNextX = 585;
    private final double propertyArrowY = 327;

    private final double arrowWidth = 34;
    private final double arrowHeight = 28;

    // Creates the panel and stores selected payment cards.
    public PaymentSelectionPanel(Game game) {
        this.game = game;
        this.selectedCards = new ArrayList<>();
    }

    // Draws the payment selection screen.
    public void draw(GraphicsContext gc) {
        if (!game.isPaymentSelecting()) {
            return;
        }

        Game.PaymentRequest request = game.getCurrentPaymentRequest();
        resetPagesWhenRequestChanged(request);

        Player payer = request.getPayer();
        Player receiver = request.getReceiver();

        drawOverlay(gc);
        drawTitle(gc, request, payer, receiver);
        drawPaymentBankCards(gc, payer);
        drawPaymentPropertyCards(gc, payer);
        drawReceiverPreview(gc, receiver);
        drawActionButtons(gc, request, payer);
    }

    // Resets pages when a new payment request starts.
    private void resetPagesWhenRequestChanged(Game.PaymentRequest request) {
        if (request != lastRequest) {
            bankPageIndex = 0;
            propertyPageIndex = 0;
            lastRequest = request;
        }
    }

    // Draws the dark background overlay.
    private void drawOverlay(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.78));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
    }

    // Draws payment title and payment amount information.
    private void drawTitle(GraphicsContext gc, Game.PaymentRequest request, Player payer, Player receiver) {
        int payerIndex = game.getPlayers().indexOf(payer) + 1;
        int receiverIndex = game.getPlayers().indexOf(receiver) + 1;
        int requiredAmount = Math.min(request.getAmount(), game.getTotalAssetsValue(payer));
        int selectedTotal = game.getCardsValue(selectedCards);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 26));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("PAYMENT REQUIRED", Game.SCREEN_WIDTH / 2, 25);

        gc.setFont(Font.font("Arial", 18));
        gc.setFill(Color.LIGHTYELLOW);
        gc.fillText("Now Player " + payerIndex + " must pay Player " + receiverIndex,
                Game.SCREEN_WIDTH / 2, 60);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Required: " + requiredAmount + "M    Selected: " + selectedTotal + "M",
                Game.SCREEN_WIDTH / 2, 90);

        if (game.getTotalAssetsValue(payer) < request.getAmount()) {
            gc.setFill(Color.LIGHTYELLOW);
            gc.fillText("Not enough assets. Player " + payerIndex + " must pay all available assets.",
                    Game.SCREEN_WIDTH / 2, 115);
        }
    }

    // Draws the payer bank cards for selection.
    private void drawPaymentBankCards(GraphicsContext gc, Player payer) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 19));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Bank Cards", 60, 145);

        int cardCount = payer.getBankCards().size();
        int maxPage = getMaxPage(cardCount);
        bankPageIndex = keepPageInRange(bankPageIndex, maxPage);

        drawPageText(gc, bankPageIndex, maxPage, 430, 148);
        drawArrowButtons(gc, bankPrevX, bankNextX, bankArrowY, bankPageIndex, maxPage);

        int startIndex = bankPageIndex * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, cardCount);

        for (int i = startIndex; i < endIndex; i++) {
            Card card = payer.getBankCards().get(i);
            int displayIndex = i - startIndex;

            double x = bankStartX + displayIndex * cardGapX;
            double y = bankStartY;

            drawPaymentCard(gc, card, x, y, "Money", card.getValue() + "M", Color.GOLD);
        }

        if (cardCount == 0) {
            drawEmptyText(gc, "No bank cards", bankStartX, bankStartY + 32);
        }
    }

    // Draws the payer property cards for selection.
    private void drawPaymentPropertyCards(GraphicsContext gc, Player payer) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 19));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Property Cards", 60, 330);

        int cardCount = payer.getPropertyCards().size();
        int maxPage = getMaxPage(cardCount);
        propertyPageIndex = keepPageInRange(propertyPageIndex, maxPage);

        drawPageText(gc, propertyPageIndex, maxPage, 430, 333);
        drawArrowButtons(gc, propertyPrevX, propertyNextX, propertyArrowY, propertyPageIndex, maxPage);

        int startIndex = propertyPageIndex * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, cardCount);

        for (int i = startIndex; i < endIndex; i++) {
            PropertiesCards card = payer.getPropertyCards().get(i);
            int displayIndex = i - startIndex;

            double x = propertyStartX + displayIndex * cardGapX;
            double y = propertyStartY;
            String text = getDisplayColorName(card.getCurrentColor());

            drawPaymentCard(gc, card, x, y, "Property", text, Color.LIGHTBLUE);
        }

        if (cardCount == 0) {
            drawEmptyText(gc, "No property cards", propertyStartX, propertyStartY + 32);
        }
    }

    // Calculates the last page index for a card list.
    private int getMaxPage(int cardCount) {
        if (cardCount <= 0) {
            return 0;
        }

        return (cardCount - 1) / cardsPerPage;
    }

    // Keeps the current page index within valid bounds.
    private int keepPageInRange(int pageIndex, int maxPage) {
        if (pageIndex < 0) {
            return 0;
        }

        if (pageIndex > maxPage) {
            return maxPage;
        }

        return pageIndex;
    }

    // Draws current page number text.
    private void drawPageText(GraphicsContext gc, int pageIndex, int maxPage, double x, double y) {
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Arial", 13));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Page " + (pageIndex + 1) + "/" + (maxPage + 1), x, y);
    }

    // Draws previous and next page buttons.
    private void drawArrowButtons(GraphicsContext gc,
                                  double prevX,
                                  double nextX,
                                  double y,
                                  int pageIndex,
                                  int maxPage) {
        if (pageIndex > 0) {
            ScreenDrawHelper.drawButton(gc, prevX, y, arrowWidth, arrowHeight, "<");
        } else {
            ScreenDrawHelper.drawDisabledButton(gc, prevX, y, arrowWidth, arrowHeight, "<");
        }

        if (pageIndex < maxPage) {
            ScreenDrawHelper.drawButton(gc, nextX, y, arrowWidth, arrowHeight, ">");
        } else {
            ScreenDrawHelper.drawDisabledButton(gc, nextX, y, arrowWidth, arrowHeight, ">");
        }
    }

    // Draws message text when no cards are available.
    private void drawEmptyText(GraphicsContext gc, String text, double x, double y) {
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Arial", 15));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(text, x, y);
    }

    // Draws a preview of the receiver after payment.
    private void drawReceiverPreview(GraphicsContext gc, Player receiver) {
        double boxX = 735;
        double boxY = 145;
        double boxW = 270;
        double boxH = 365;
        int receiverIndex = game.getPlayers().indexOf(receiver) + 1;

        gc.setFill(Color.rgb(245, 245, 245));
        gc.fillRoundRect(boxX, boxY, boxW, boxH, 15, 15);

        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(boxX, boxY, boxW, boxH, 15, 15);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 17));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("If paid to Player " + receiverIndex + ":", boxX + 15, boxY + 15);

        gc.setFont(Font.font("Arial", 13));
        gc.fillText("Selected property cards will be added", boxX + 15, boxY + 45);
        gc.fillText("to this player's property area.", boxX + 15, boxY + 63);

        drawReceiverSetPreview(gc, receiver, boxX, boxY + 100);
    }

    // Shows how selected property cards affect receiver sets.
    private void drawReceiverSetPreview(GraphicsContext gc, Player receiver, double boxX, double startY) {
        double lineGap = 24;
        int row = 0;

        for (PropertyColor color : PropertyColor.values()) {
            int originalCount = PlayerInfoHelper.getPropertyCountByCurrentColor(receiver,color);
            int addedCount = getSelectedPropertyCountByColor(color);
            int newCount = originalCount + addedCount;
            int need = color.getAmountToCompleteSet();

            if (originalCount == 0 && addedCount == 0) {
                continue;
            }

            if (newCount >= need) {
                gc.setFill(Color.GREEN);
            } else if (addedCount > 0) {
                gc.setFill(Color.ORANGE);
            } else {
                gc.setFill(Color.BLACK);
            }

            String text = PropertiesCards.getShortColorName(color)
                    + ": "
                    + originalCount
                    + " + "
                    + addedCount
                    + " = "
                    + newCount
                    + "/"
                    + need;

            gc.fillText(text, boxX + 15, startY + row * lineGap);

            if (newCount >= need && originalCount < need) {
                gc.fillText("NEW SET", boxX + 175, startY + row * lineGap);
            } else if (newCount >= need) {
                gc.fillText("COMPLETE", boxX + 175, startY + row * lineGap);
            }

            row++;
        }

        if (row == 0) {
            gc.setFill(Color.GRAY);
            gc.fillText("No selected property effect yet.", boxX + 15, startY);
        }
    }

    // Draws confirm, clear, and Just Say No buttons.
    private void drawActionButtons(GraphicsContext gc, Game.PaymentRequest request, Player payer) {
        int requiredAmount = Math.min(request.getAmount(), game.getTotalAssetsValue(payer));
        int selectedTotal = game.getCardsValue(selectedCards);

        if (selectedTotal >= requiredAmount) {
            ScreenDrawHelper.drawButton(gc, 330, 555, 160, 40, "CONFIRM PAY");
        } else {
            ScreenDrawHelper.drawDisabledButton(gc, 330, 555, 160, 40, "CONFIRM PAY");
        }

        ScreenDrawHelper.drawButton(gc, 510, 555, 120, 40, "CLEAR");

        if (game.canCurrentPaymentUseJustSayNo()) {
            ScreenDrawHelper.drawButton(gc, 650, 555, 220, 40, "USE JUST SAY NO");
        }
    }

    // Counts selected property cards with the given color.
    private int getSelectedPropertyCountByColor(PropertyColor color) {
        int count = 0;

        for (Card card : selectedCards) {
            if (card instanceof PropertiesCards propertyCard) {
                if (propertyCard.getCurrentColor() == color) {
                    count++;
                }
            }
        }

        return count;
    }

    // Draws one selectable payment card.
    private void drawPaymentCard(GraphicsContext gc, Card card, double x, double y, String type, String text, Color color) {
        if (selectedCards.contains(card)) {
            gc.setFill(Color.YELLOW);
            gc.fillRoundRect(x - 5, y - 5, cardWidth + 10, cardHeight + 10, 14, 14);
        }

        if (CardImageHelper.drawCardImage(gc, card, x, y, cardWidth, cardHeight)) {
            return;
        }

        gc.setFill(color);
        gc.fillRoundRect(x, y, cardWidth, cardHeight, 12, 12);

        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(x, y, cardWidth, cardHeight, 12, 12);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 11));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);

        gc.fillText(type, x + cardWidth / 2, y + 10);
        gc.fillText(card.getValue() + "M", x + cardWidth / 2, y + 30);

        gc.setFont(Font.font("Arial", 9));
        ScreenDrawHelper.drawWrappedText(gc, text, x + 5, y + 52, cardWidth - 10, 11);
    }

    // Checks whether the confirm button was clicked.
    public boolean isConfirmClicked(double mouseX, double mouseY) {
        return game.isPaymentSelecting()
                && mouseX >= 330 && mouseX <= 490
                && mouseY >= 555 && mouseY <= 595;
    }

    // Checks whether the clear button was clicked.
    public boolean isClearClicked(double mouseX, double mouseY) {
        return game.isPaymentSelecting()
                && mouseX >= 510 && mouseX <= 630
                && mouseY >= 555 && mouseY <= 595;
    }

    // Checks whether the Just Say No button was clicked.
    public boolean isJustSayNoClicked(double mouseX, double mouseY) {
        return game.isPaymentSelecting()
                && game.canCurrentPaymentUseJustSayNo()
                && mouseX >= 650 && mouseX <= 870
                && mouseY >= 555 && mouseY <= 595;
    }

    // Clears all selected payment cards.
    public void clearSelection() {
        selectedCards.clear();
    }

    // Returns a copy of selected payment cards.
    public ArrayList<Card> getSelectedCards() {
        return new ArrayList<>(selectedCards);
    }

    // Checks whether the selected cards cover the required payment.
    public boolean canConfirm() {
        if (!game.isPaymentSelecting()) {
            return false;
        }

        Game.PaymentRequest request = game.getCurrentPaymentRequest();
        int requiredAmount = Math.min(request.getAmount(), game.getTotalAssetsValue(request.getPayer()));
        int selectedTotal = game.getCardsValue(selectedCards);

        return selectedTotal >= requiredAmount;
    }

    // Handles clicks on cards or page buttons.
    public boolean handleCardClick(double mouseX, double mouseY) {
        Game.PaymentRequest request = game.getCurrentPaymentRequest();

        if (request == null) {
            return false;
        }

        Player payer = request.getPayer();

        if (handlePageButtonClick(mouseX, mouseY, payer)) {
            return true;
        }

        Card bankCard = getClickedPaymentBankCard(mouseX, mouseY, payer);

        if (bankCard != null) {
            toggleSelectedCard(bankCard);
            return true;
        }

        Card propertyCard = getClickedPaymentPropertyCard(mouseX, mouseY, payer);

        if (propertyCard != null) {
            toggleSelectedCard(propertyCard);
            return true;
        }

        return false;
    }

    // Selects or unselects a payment card.
    private void toggleSelectedCard(Card card) {
        if (card == null) {
            return;
        }

        if (selectedCards.contains(card)) {
            selectedCards.remove(card);
        } else {
            selectedCards.add(card);
        }
    }

    // Handles page button clicks for both card areas.
    private boolean handlePageButtonClick(double mouseX, double mouseY, Player payer) {
        if (handleBankPageButtonClick(mouseX, mouseY, payer)) {
            return true;
        }

        return handlePropertyPageButtonClick(mouseX, mouseY, payer);
    }

    private boolean handleBankPageButtonClick(double mouseX, double mouseY, Player payer) {
        int maxPage = getMaxPage(payer.getBankCards().size());

        if (isInside(mouseX, mouseY, bankPrevX, bankArrowY, arrowWidth, arrowHeight)) {
            if (bankPageIndex > 0) {
                bankPageIndex--;
            }

            return true;
        }

        if (isInside(mouseX, mouseY, bankNextX, bankArrowY, arrowWidth, arrowHeight)) {
            if (bankPageIndex < maxPage) {
                bankPageIndex++;
            }

            return true;
        }

        return false;
    }

    private boolean handlePropertyPageButtonClick(double mouseX, double mouseY, Player payer) {
        int maxPage = getMaxPage(payer.getPropertyCards().size());

        if (isInside(mouseX, mouseY, propertyPrevX, propertyArrowY, arrowWidth, arrowHeight)) {
            if (propertyPageIndex > 0) {
                propertyPageIndex--;
            }

            return true;
        }

        if (isInside(mouseX, mouseY, propertyNextX, propertyArrowY, arrowWidth, arrowHeight)) {
            if (propertyPageIndex < maxPage) {
                propertyPageIndex++;
            }

            return true;
        }

        return false;
    }

    // Checks whether the mouse position is inside a rectangle.
    private boolean isInside(double mouseX,
                             double mouseY,
                             double x,
                             double y,
                             double width,
                             double height) {
        return mouseX >= x
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + height;
    }

    // Returns the clicked bank card on the current page.
    private Card getClickedPaymentBankCard(double mouseX, double mouseY, Player payer) {
        int startIndex = bankPageIndex * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, payer.getBankCards().size());

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;

            double x = bankStartX + displayIndex * cardGapX;
            double y = bankStartY;

            if (isInside(mouseX, mouseY, x, y, cardWidth, cardHeight)) {
                return payer.getBankCards().get(i);
            }
        }

        return null;
    }

    // Returns the clicked property card on the current page.
    private Card getClickedPaymentPropertyCard(double mouseX, double mouseY, Player payer) {
        int startIndex = propertyPageIndex * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, payer.getPropertyCards().size());

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;

            double x = propertyStartX + displayIndex * cardGapX;
            double y = propertyStartY;

            if (isInside(mouseX, mouseY, x, y, cardWidth, cardHeight)) {
                return payer.getPropertyCards().get(i);
            }
        }

        return null;
    }

    // Converts enum color names into readable text.
    private String getDisplayColorName(PropertyColor color) {
        if (color == null) {
            return "No Color";
        }

        String[] words = color.name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            if (!builder.isEmpty()) {
                builder.append(" ");
            }

            builder.append(word.substring(0, 1).toUpperCase());
            builder.append(word.substring(1));
        }

        return builder.toString();
    }
}
