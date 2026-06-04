package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;
import logic.PlayerInfoHelper;
import model.ActionCardType;
import model.BuildingPaymentCard;
import model.Card;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;

// Handles the payment selection popup when a player must pay assets.
public class PaymentSelectionPanel {
    private final Game game;
    private final ArrayList<Card> selectedCards;
    private final ArrayList<Card> bankPaymentCards;

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
        this.bankPaymentCards = new ArrayList<>();
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

        if (request.isJustSayNoPending()) {
            drawJustSayNoResponse(gc, request);
            drawActionButtons(gc, request, payer);
            return;
        }

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
            selectedCards.clear();
            rebuildBankPaymentCards(request);
        }
    }

    // Runs rebuild bank payment cards.
    private void rebuildBankPaymentCards(Game.PaymentRequest request) {
        bankPaymentCards.clear();

        if (request == null) {
            return;
        }

        Player payer = request.getPayer();

        for (PropertyColor color : PropertyColor.values()) {
            if (PlayerInfoHelper.hasHotel(payer, color)) {
                bankPaymentCards.add(new BuildingPaymentCard(ActionCardType.HOTEL, color));
            }

            if (PlayerInfoHelper.hasHouse(payer, color)) {
                bankPaymentCards.add(new BuildingPaymentCard(ActionCardType.HOUSE, color));
            }
        }

        bankPaymentCards.addAll(payer.getBankCards());
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
        int selectedTotal = game.getPaymentCardsValue(payer, selectedCards);

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

        if (request.isJustSayNoPending()) {
            int lastUserIndex = game.getPlayers().indexOf(request.getLastJustSayNoUser()) + 1;
            int responderIndex = game.getPlayers().indexOf(request.getJustSayNoResponder()) + 1;
            gc.setFill(Color.LIGHTYELLOW);
            gc.fillText("Player " + lastUserIndex + " used Just Say No. Player "
                    + responderIndex + " may counter it.", Game.SCREEN_WIDTH / 2, 115);
        } else if (game.getTotalAssetsValue(payer) < request.getAmount()) {
            gc.setFill(Color.LIGHTYELLOW);
            gc.fillText("Not enough assets. Player " + payerIndex + " must pay all available assets.",
                    Game.SCREEN_WIDTH / 2, 115);
        }
    }

    // Draws the Just Say No response state.
    private void drawJustSayNoResponse(GraphicsContext gc, Game.PaymentRequest request) {
        int responderIndex = game.getPlayers().indexOf(request.getJustSayNoResponder()) + 1;

        ScreenDrawHelper.drawPanel(gc, 265, 205, 505, 230);
        gc.setFill(ScreenDrawHelper.ACCENT);
        gc.setFont(Font.font("Arial", 28));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("JUST SAY NO RESPONSE", Game.SCREEN_WIDTH / 2, 245);

        gc.setFill(ScreenDrawHelper.TEXT);
        gc.setFont(Font.font("Arial", 17));
        gc.fillText("Player " + responderIndex + ", choose whether to counter.", Game.SCREEN_WIDTH / 2, 300);

        gc.setFill(ScreenDrawHelper.MUTED_TEXT);
        gc.setFont(Font.font("Arial", 14));
        gc.fillText("Accepting applies the latest Just Say No.", Game.SCREEN_WIDTH / 2, 333);
    }

    // Draws the payer bank cards for selection.
    private void drawPaymentBankCards(GraphicsContext gc, Player payer) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 19));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Bank / Buildings", 60, 145);

        int cardCount = bankPaymentCards.size();
        int maxPage = ScreenDrawHelper.getMaxPage(cardCount, cardsPerPage);
        bankPageIndex = ScreenDrawHelper.keepPageInRange(bankPageIndex, maxPage);

        ScreenDrawHelper.drawPageText(gc, bankPageIndex, maxPage, 430, 148, Color.LIGHTGRAY);
        ScreenDrawHelper.drawArrowButtons(gc, bankPrevX, bankNextX, bankArrowY,
                arrowWidth, arrowHeight, bankPageIndex, maxPage);

        int startIndex = bankPageIndex * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, cardCount);

        for (int i = startIndex; i < endIndex; i++) {
            Card card = bankPaymentCards.get(i);
            int displayIndex = i - startIndex;

            double x = bankStartX + displayIndex * cardGapX;
            double y = bankStartY;

            if (card instanceof BuildingPaymentCard buildingCard) {
                boolean blocked = isHouseBlockedByUnpaidHotel(payer, buildingCard);
                drawPaymentCard(gc, card, x, y, "Building",
                        blocked ? "Pay hotel first" : getBuildingText(buildingCard),
                        blocked ? Color.DARKGRAY : Color.LIGHTGREEN);
            } else {
                drawPaymentCard(gc, card, x, y, "Money", card.getValue() + "M", Color.GOLD);
            }
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
        int maxPage = ScreenDrawHelper.getMaxPage(cardCount, cardsPerPage);
        propertyPageIndex = ScreenDrawHelper.keepPageInRange(propertyPageIndex, maxPage);

        ScreenDrawHelper.drawPageText(gc, propertyPageIndex, maxPage, 430, 333, Color.LIGHTGRAY);
        ScreenDrawHelper.drawArrowButtons(gc, propertyPrevX, propertyNextX, propertyArrowY,
                arrowWidth, arrowHeight, propertyPageIndex, maxPage);

        int startIndex = propertyPageIndex * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, cardCount);

        for (int i = startIndex; i < endIndex; i++) {
            PropertiesCards card = payer.getPropertyCards().get(i);
            int displayIndex = i - startIndex;

            double x = propertyStartX + displayIndex * cardGapX;
            double y = propertyStartY;
            String text = ScreenDrawHelper.getDisplayColorName(card.getCurrentColor());
            boolean blocked = isPropertyBlockedByUnpaidBuildings(payer, card);
            String label = blocked ? "Pay building first" : card.getValue() + "M";

            drawPaymentCard(gc, card, x, y, "Property", text + " " + label,
                    blocked ? Color.DARKGRAY : Color.LIGHTBLUE);
        }

        if (cardCount == 0) {
            drawEmptyText(gc, "No property cards", propertyStartX, propertyStartY + 32);
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
        gc.fillText("Buildings must be paid before property.", boxX + 15, boxY + 45);
        gc.fillText("House/Hotel go to receiver bank.", boxX + 15, boxY + 63);

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
        if (request.isJustSayNoPending()) {
            ScreenDrawHelper.drawButton(gc, 330, 555, 220, 40, "ACCEPT JUST SAY NO");

            Player responder = request.getJustSayNoResponder();
            if (responder != null && responder.hasActionCard(ActionCardType.JUST_SAY_NO)) {
                ScreenDrawHelper.drawButton(gc, 570, 555, 220, 40, "COUNTER JUST SAY NO");
            }

            return;
        }

        int requiredAmount = Math.min(request.getAmount(), game.getTotalAssetsValue(payer));
        int selectedTotal = game.getPaymentCardsValue(payer, selectedCards);

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
                && ScreenDrawHelper.handleButtonClick(mouseX, mouseY, 330, 555, 160, 40);
    }

    // Checks whether the clear button was clicked.
    public boolean isClearClicked(double mouseX, double mouseY) {
        return game.isPaymentSelecting()
                && ScreenDrawHelper.handleButtonClick(mouseX, mouseY, 510, 555, 120, 40);
    }

    // Checks whether the Just Say No button was clicked.
    public boolean isJustSayNoClicked(double mouseX, double mouseY) {
        if (game.isPaymentSelecting() && game.isCurrentPaymentWaitingForJustSayNoResponse()) {
            return game.canCurrentPaymentUseJustSayNo()
                    && ScreenDrawHelper.handleButtonClick(mouseX, mouseY, 570, 555, 220, 40);
        }

        return game.isPaymentSelecting()
                && game.canCurrentPaymentUseJustSayNo()
                && ScreenDrawHelper.handleButtonClick(mouseX, mouseY, 650, 555, 220, 40);
    }

    // Checks whether the Just Say No accept button was clicked.
    public boolean isJustSayNoPassClicked(double mouseX, double mouseY) {
        return game.isPaymentSelecting()
                && game.isCurrentPaymentWaitingForJustSayNoResponse()
                && ScreenDrawHelper.handleButtonClick(mouseX, mouseY, 330, 555, 220, 40);
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
        int selectedTotal = game.getPaymentCardsValue(request.getPayer(), selectedCards);

        return selectedTotal >= requiredAmount;
    }

    // Handles clicks on cards or page buttons.
    public boolean handleCardClick(double mouseX, double mouseY) {
        Game.PaymentRequest request = game.getCurrentPaymentRequest();

        if (request == null) {
            return false;
        }

        resetPagesWhenRequestChanged(request);
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

        if (game.isPaymentSelecting()) {
            Player payer = game.getCurrentPaymentRequest().getPayer();
            removeBlockedBuildingSelections(payer);
            removeBlockedPropertySelections(payer);
        }
    }

    // Removes building selections that became invalid after another building was unselected.
    private void removeBlockedBuildingSelections(Player payer) {
        selectedCards.removeIf(card -> card instanceof BuildingPaymentCard buildingCard
                && isHouseBlockedByUnpaidHotel(payer, buildingCard));
    }

    // Removes blocked property selections.
    private void removeBlockedPropertySelections(Player payer) {
        selectedCards.removeIf(card -> card instanceof PropertiesCards propertyCard
                && isPropertyBlockedByUnpaidBuildings(payer, propertyCard));
    }

    // Handles page button clicks for both card areas.
    private boolean handlePageButtonClick(double mouseX, double mouseY, Player payer) {
        if (handleBankPageButtonClick(mouseX, mouseY, payer)) {
            return true;
        }

        return handlePropertyPageButtonClick(mouseX, mouseY, payer);
    }

    // Handles bank page button click.
    private boolean handleBankPageButtonClick(double mouseX, double mouseY, Player payer) {
        int maxPage = ScreenDrawHelper.getMaxPage(bankPaymentCards.size(), cardsPerPage);

        if (ScreenDrawHelper.handleButtonClick(mouseX, mouseY, bankPrevX, bankArrowY, arrowWidth, arrowHeight)) {
            if (bankPageIndex > 0) {
                bankPageIndex--;
            }

            return true;
        }

        if (ScreenDrawHelper.handleButtonClick(mouseX, mouseY, bankNextX, bankArrowY, arrowWidth, arrowHeight)) {
            if (bankPageIndex < maxPage) {
                bankPageIndex++;
            }

            return true;
        }

        return false;
    }

    // Handles property page button click.
    private boolean handlePropertyPageButtonClick(double mouseX, double mouseY, Player payer) {
        int maxPage = ScreenDrawHelper.getMaxPage(payer.getPropertyCards().size(), cardsPerPage);

        if (ScreenDrawHelper.handleButtonClick(mouseX, mouseY, propertyPrevX, propertyArrowY, arrowWidth, arrowHeight)) {
            if (propertyPageIndex > 0) {
                propertyPageIndex--;
            }

            return true;
        }

        if (ScreenDrawHelper.handleButtonClick(mouseX, mouseY, propertyNextX, propertyArrowY, arrowWidth, arrowHeight)) {
            if (propertyPageIndex < maxPage) {
                propertyPageIndex++;
            }

            return true;
        }

        return false;
    }

    // Returns the clicked bank card on the current page.
    private Card getClickedPaymentBankCard(double mouseX, double mouseY, Player payer) {
        int startIndex = bankPageIndex * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, bankPaymentCards.size());

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;

            double x = bankStartX + displayIndex * cardGapX;
            double y = bankStartY;

            if (ScreenDrawHelper.isInside(mouseX, mouseY, x, y, cardWidth, cardHeight)) {
                Card card = bankPaymentCards.get(i);
                if (card instanceof BuildingPaymentCard buildingCard
                        && isHouseBlockedByUnpaidHotel(payer, buildingCard)) {
                    return null;
                }

                return bankPaymentCards.get(i);
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

            if (ScreenDrawHelper.isInside(mouseX, mouseY, x, y, cardWidth, cardHeight)) {
                if (isPropertyBlockedByUnpaidBuildings(payer, payer.getPropertyCards().get(i))) {
                    return null;
                }

                return payer.getPropertyCards().get(i);
            }
        }

        return null;
    }

    // Finds building text.
    private String getBuildingText(BuildingPaymentCard buildingCard) {
        return ScreenDrawHelper.getDisplayColorName(buildingCard.getColor()) + " " + buildingCard.getValue() + "M";
    }

    // Checks whether house blocked by unpaid hotel.
    private boolean isHouseBlockedByUnpaidHotel(Player payer, BuildingPaymentCard buildingCard) {
        return buildingCard.getActionCardType() == ActionCardType.HOUSE
                && PlayerInfoHelper.hasHotel(payer, buildingCard.getColor())
                && !hasSelectedBuilding(buildingCard.getColor(), ActionCardType.HOTEL);
    }

    // Checks whether property blocked by unpaid buildings.
    private boolean isPropertyBlockedByUnpaidBuildings(Player payer, PropertiesCards propertyCard) {
        PropertyColor color = propertyCard.getCurrentColor();
        if (color == null) {
            return false;
        }

        if (PlayerInfoHelper.hasHotel(payer, color)
                && !hasSelectedBuilding(color, ActionCardType.HOTEL)) {
            return true;
        }

        return PlayerInfoHelper.hasHouse(payer, color)
                && !hasSelectedBuilding(color, ActionCardType.HOUSE);
    }

    // Checks whether this has selected building.
    private boolean hasSelectedBuilding(PropertyColor color, ActionCardType type) {
        for (Card card : selectedCards) {
            if (card instanceof BuildingPaymentCard buildingCard
                    && buildingCard.getColor() == color
                    && buildingCard.getActionCardType() == type) {
                return true;
            }
        }

        return false;
    }
}
