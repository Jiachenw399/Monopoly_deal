package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;
import model.Card;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

public class PlayerDetailPopupPanel {
    private final Game game;

    private int selectedPlayerIndex = -1;

    private final double popupX = 145;
    private final double popupY = 70;
    private final double popupWidth = 745;
    private final double popupHeight = 485;

    private final double closeX = 785;
    private final double closeY = 95;
    private final double closeWidth = 80;
    private final double closeHeight = 34;

    private final double cardWidth = 68;
    private final double cardHeight = 93;
    private final double cardGapX = 82;
    private final double cardGapY = 105;
    private final int cardsPerRow = 8;

    public PlayerDetailPopupPanel(Game game) {
        this.game = game;
    }

    public void showPlayer(int playerIndex) {
        if (playerIndex < 0 || playerIndex >= game.getPlayers().size()) {
            return;
        }

        selectedPlayerIndex = playerIndex;
    }

    public void close() {
        selectedPlayerIndex = -1;
    }

    public boolean isShowing() {
        return selectedPlayerIndex != -1;
    }

    public boolean isCloseClicked(double mouseX, double mouseY) {
        return isShowing()
                && mouseX >= closeX && mouseX <= closeX + closeWidth
                && mouseY >= closeY && mouseY <= closeY + closeHeight;
    }

    public void draw(GraphicsContext gc) {
        if (!isShowing()) {
            return;
        }

        Player player = game.getPlayers().get(selectedPlayerIndex);

        drawOverlay(gc);
        drawPopupBox(gc);
        drawTitle(gc, player);
        drawCloseButton(gc);
        drawBasicInfo(gc, player);
        drawBankArea(gc, player);
        drawPropertyArea(gc, player);
    }

    private void drawOverlay(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.72));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
    }

    private void drawPopupBox(GraphicsContext gc) {
        gc.setFill(Color.rgb(245, 247, 250));
        gc.fillRoundRect(popupX, popupY, popupWidth, popupHeight, 24, 24);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRoundRect(popupX, popupY, popupWidth, popupHeight, 24, 24);
        gc.setLineWidth(1);
    }

    private void drawTitle(GraphicsContext gc, Player player) {
        gc.setFill(Color.rgb(30, 35, 48));
        gc.setFont(Font.font("Arial", 28));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Player " + (selectedPlayerIndex + 1) + " Details", popupX + 28, popupY + 24);

        gc.setFont(Font.font("Arial", 15));
        gc.setFill(Color.rgb(90, 100, 118));
        gc.fillText("Hand, bank area and property area overview", popupX + 30, popupY + 62);
    }

    private void drawCloseButton(GraphicsContext gc) {
        gc.setFill(Color.rgb(255, 184, 77));
        gc.fillRoundRect(closeX, closeY, closeWidth, closeHeight, 10, 10);

        gc.setStroke(Color.rgb(220, 130, 40));
        gc.strokeRoundRect(closeX, closeY, closeWidth, closeHeight, 10, 10);

        gc.setFill(Color.rgb(34, 26, 10));
        gc.setFont(Font.font("Arial", 15));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("Close", closeX + closeWidth / 2, closeY + closeHeight / 2);

        gc.setTextBaseline(VPos.TOP);
    }

    private void drawBasicInfo(GraphicsContext gc, Player player) {
        double x = popupX + 30;
        double y = popupY + 100;

        int bankTotal = PlayerInfoHelper.getBankTotal(player);
        int completedSets = PlayerInfoHelper.getCompletedSetCount(player);

        drawInfoBadge(gc, x, y, 155, "Hand Cards", String.valueOf(player.getHandCards().size()));
        drawInfoBadge(gc, x + 175, y, 155, "Bank Total", bankTotal + "M");
        drawInfoBadge(gc, x + 350, y, 155, "Completed Sets", completedSets + "/3");
    }

    private void drawInfoBadge(GraphicsContext gc, double x, double y, double width, String title, String value) {
        gc.setFill(Color.rgb(225, 232, 242));
        gc.fillRoundRect(x, y, width, 58, 14, 14);

        gc.setStroke(Color.rgb(205, 212, 225));
        gc.strokeRoundRect(x, y, width, 58, 14, 14);

        gc.setFill(Color.rgb(90, 100, 118));
        gc.setFont(Font.font("Arial", 13));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(title, x + width / 2, y + 8);

        gc.setFill(Color.rgb(30, 35, 48));
        gc.setFont(Font.font("Arial", 20));
        gc.fillText(value, x + width / 2, y + 29);
    }

    private void drawBankArea(GraphicsContext gc, Player player) {
        double titleX = popupX + 30;
        double titleY = popupY + 178;

        gc.setFill(Color.rgb(30, 35, 48));
        gc.setFont(Font.font("Arial", 19));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Bank Area", titleX, titleY);

        if (player.getBankCards().isEmpty()) {
            drawEmptyText(gc, "No bank cards", titleX, titleY + 40);
            return;
        }

        double startX = popupX + 30;
        double startY = popupY + 212;

        for (int i = 0; i < player.getBankCards().size(); i++) {
            Card card = player.getBankCards().get(i);

            double x = startX + (i % cardsPerRow) * cardGapX;
            double y = startY + (i / cardsPerRow) * cardGapY;

            drawSmallCard(gc, card, x, y);
        }
    }

    private void drawPropertyArea(GraphicsContext gc, Player player) {
        double titleX = popupX + 30;
        double titleY = popupY + 335;

        gc.setFill(Color.rgb(30, 35, 48));
        gc.setFont(Font.font("Arial", 19));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Property Area", titleX, titleY);

        if (player.getPropertyCards().isEmpty()) {
            drawEmptyText(gc, "No property cards", titleX, titleY + 40);
            return;
        }

        double startX = popupX + 30;
        double startY = popupY + 368;

        for (int i = 0; i < player.getPropertyCards().size(); i++) {
            Card card = player.getPropertyCards().get(i);

            double x = startX + (i % cardsPerRow) * cardGapX;
            double y = startY + (i / cardsPerRow) * cardGapY;

            drawSmallCard(gc, card, x, y);
        }
    }

    private void drawEmptyText(GraphicsContext gc, String text, double x, double y) {
        gc.setFill(Color.rgb(120, 128, 145));
        gc.setFont(Font.font("Arial", 15));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(text, x, y);
    }

    private void drawSmallCard(GraphicsContext gc, Card card, double x, double y) {
        if (CardImageHelper.drawCardImage(gc, card, x, y, cardWidth, cardHeight)) {
            return;
        }

        String title = getCardTitle(card);
        String detail = getCardDetail(card);
        Color fillColor = getCardFillColor(card);

        gc.setFill(fillColor);
        gc.fillRoundRect(x, y, cardWidth, cardHeight, 12, 12);

        gc.setStroke(Color.rgb(60, 60, 60));
        gc.strokeRoundRect(x, y, cardWidth, cardHeight, 12, 12);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 11));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(title, x + cardWidth / 2, y + 10);

        gc.setFont(Font.font("Arial", 10));
        ScreenDrawHelper.drawWrappedText(gc, detail, x + 5, y + 32, cardWidth - 10, 12);
    }

    private String getCardTitle(Card card) {
        if (card instanceof MoneyCards) {
            return "Money";
        }

        if (card instanceof PropertiesCards) {
            return "Property";
        }

        return "Card";
    }

    private String getCardDetail(Card card) {
        if (card instanceof MoneyCards) {
            return card.getValue() + "M";
        }

        if (card instanceof PropertiesCards propertyCard) {
            return getDisplayColorName(propertyCard.getCurrentColor());
        }

        return card.getValue() + "M";
    }

    private Color getCardFillColor(Card card) {
        if (card instanceof MoneyCards) {
            return Color.GOLD;
        }

        if (card instanceof PropertiesCards) {
            return Color.LIGHTBLUE;
        }

        return Color.WHITE;
    }

    private String getDisplayColorName(PropertyColor color) {
        if (color == null) {
            return "No Color";
        }

        String[] words = color.name().toLowerCase().split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!result.isEmpty()) {
                result.append(" ");
            }

            result.append(word.substring(0, 1).toUpperCase());
            result.append(word.substring(1));
        }

        return result.toString();
    }
}