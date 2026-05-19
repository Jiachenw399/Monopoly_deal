package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;
import model.Card;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;

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
    private final double cardGapY = 105;
    private final int cardsPerRow = 7;

    public PaymentSelectionPanel(Game game) {
        this.game = game;
        this.selectedCards = new ArrayList<>();
    }

    public void draw(GraphicsContext gc) {
        if (!game.isPaymentSelecting()) {
            return;
        }

        Game.PaymentRequest request = game.getCurrentPaymentRequest();
        Player payer = request.getPayer();
        Player receiver = request.getReceiver();

        drawOverlay(gc);
        drawTitle(gc, request, payer, receiver);
        drawPaymentBankCards(gc, payer);
        drawPaymentPropertyCards(gc, payer);
        drawReceiverPreview(gc, receiver);
        drawActionButtons(gc, request, payer);
    }

    private void drawOverlay(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.78));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
    }

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

    private void drawPaymentBankCards(GraphicsContext gc, Player payer) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 19));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Bank Cards", 60, 145);

        for (int i = 0; i < payer.getBankCards().size(); i++) {
            Card card = payer.getBankCards().get(i);
            double x = bankStartX + (i % cardsPerRow) * cardGapX;
            double y = bankStartY + (i / cardsPerRow) * cardGapY;

            drawPaymentCard(gc, card, x, y, "Money", card.getValue() + "M", Color.GOLD);
        }
    }

    private void drawPaymentPropertyCards(GraphicsContext gc, Player payer) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 19));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Property Cards", 60, 330);

        for (int i = 0; i < payer.getPropertyCards().size(); i++) {
            PropertiesCards card = payer.getPropertyCards().get(i);
            double x = propertyStartX + (i % cardsPerRow) * cardGapX;
            double y = propertyStartY + (i / cardsPerRow) * cardGapY;
            String text = getDisplayColorName(card.getCurrentColor());

            drawPaymentCard(gc, card, x, y, "Property", text, Color.LIGHTBLUE);
        }
    }

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

    private void drawReceiverSetPreview(GraphicsContext gc, Player receiver, double boxX, double startY) {
        double lineGap = 24;
        int row = 0;

        for (PropertyColor color : PropertyColor.values()) {
            int originalCount = PlayerInfoHelper.getPropertyCountByColor(receiver, color);
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

            String text = PlayerInfoHelper.getShortColorName(color)
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

    public boolean isConfirmClicked(double mouseX, double mouseY) {
        return game.isPaymentSelecting()
                && mouseX >= 330 && mouseX <= 490
                && mouseY >= 555 && mouseY <= 595;
    }

    public boolean isClearClicked(double mouseX, double mouseY) {
        return game.isPaymentSelecting()
                && mouseX >= 510 && mouseX <= 630
                && mouseY >= 555 && mouseY <= 595;
    }

    public boolean isJustSayNoClicked(double mouseX, double mouseY) {
        return game.isPaymentSelecting()
                && game.canCurrentPaymentUseJustSayNo()
                && mouseX >= 650 && mouseX <= 870
                && mouseY >= 555 && mouseY <= 595;
    }

    public void clearSelection() {
        selectedCards.clear();
    }

    public ArrayList<Card> getSelectedCards() {
        return new ArrayList<>(selectedCards);
    }

    public boolean canConfirm() {
        if (!game.isPaymentSelecting()) {
            return false;
        }

        Game.PaymentRequest request = game.getCurrentPaymentRequest();
        int requiredAmount = Math.min(request.getAmount(), game.getTotalAssetsValue(request.getPayer()));
        int selectedTotal = game.getCardsValue(selectedCards);

        return selectedTotal >= requiredAmount;
    }

    public boolean handleCardClick(double mouseX, double mouseY) {
        if (!game.isPaymentSelecting()) {
            return false;
        }

        Player payer = game.getCurrentPaymentRequest().getPayer();
        Card clickedCard = getClickedBankCard(mouseX, mouseY, payer);

        if (clickedCard == null) {
            clickedCard = getClickedPropertyCard(mouseX, mouseY, payer);
        }

        if (clickedCard == null) {
            return false;
        }

        if (selectedCards.contains(clickedCard)) {
            selectedCards.remove(clickedCard);
        } else {
            selectedCards.add(clickedCard);
        }

        return true;
    }

    private Card getClickedBankCard(double mouseX, double mouseY, Player payer) {
        for (int i = 0; i < payer.getBankCards().size(); i++) {
            double x = bankStartX + (i % cardsPerRow) * cardGapX;
            double y = bankStartY + (i / cardsPerRow) * cardGapY;

            if (isInsideCard(mouseX, mouseY, x, y)) {
                return payer.getBankCards().get(i);
            }
        }

        return null;
    }

    private Card getClickedPropertyCard(double mouseX, double mouseY, Player payer) {
        for (int i = 0; i < payer.getPropertyCards().size(); i++) {
            double x = propertyStartX + (i % cardsPerRow) * cardGapX;
            double y = propertyStartY + (i / cardsPerRow) * cardGapY;

            if (isInsideCard(mouseX, mouseY, x, y)) {
                return payer.getPropertyCards().get(i);
            }
        }

        return null;
    }

    private boolean isInsideCard(double mouseX, double mouseY, double x, double y) {
        return mouseX >= x
                && mouseX <= x + cardWidth
                && mouseY >= y
                && mouseY <= y + cardHeight;
    }

    private String getDisplayColorName(PropertyColor color) {
        if (color == null) {
            return "No Color";
        }

        String[] words = color.name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            if (builder.length() > 0) {
                builder.append(" ");
            }

            builder.append(word.substring(0, 1).toUpperCase());
            builder.append(word.substring(1));
        }

        return builder.toString();
    }
}
