package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;
import logic.PlayerInfoHelper;
import model.Card;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;

// Displays detailed information for a selected player.
public class PlayerDetailPopupPanel {
    private final Game game;

    private int selectedPlayerIndex = -1;
    private int bankPage = 0;
    private int propertyPage = 0;

    private final double popupX = 95;
    private final double popupY = 60;
    private final double popupWidth = 845;
    private final double popupHeight = 505;

    private final double closeX = 830;
    private final double closeY = 85;
    private final double closeWidth = 80;
    private final double closeHeight = 34;

    private final double cardWidth = 68;
    private final double cardHeight = 93;
    private final double cardGapX = 82;
    private final int cardsPerPage = 8;

    private final double pageButtonWidth = 34;
    private final double pageButtonHeight = 26;

    private final double bankPrevX = 715;
    private final double bankNextX = 810;
    private final double bankButtonY = 247;

    private final double propertyPrevX = 715;
    private final double propertyNextX = 810;
    private final double propertyButtonY = 405;

    // Creates the popup panel with game data.
    public PlayerDetailPopupPanel(Game game) {
        this.game = game;
    }

    // Opens the popup for the selected player.
    public void showPlayer(int playerIndex) {
        if (playerIndex < 0 || playerIndex >= game.getPlayers().size()) {
            return;
        }

        selectedPlayerIndex = playerIndex;
        bankPage = 0;
        propertyPage = 0;
    }

    // Closes the popup and resets pages.
    public void close() {
        selectedPlayerIndex = -1;
        bankPage = 0;
        propertyPage = 0;
    }

    // Checks whether the popup is currently visible.
    public boolean isShowing() {
        return selectedPlayerIndex != -1;
    }

    // Checks whether the close button was clicked.
    public boolean isCloseClicked(double mouseX, double mouseY) {
        return isShowing()
                && mouseX >= closeX && mouseX <= closeX + closeWidth
                && mouseY >= closeY && mouseY <= closeY + closeHeight;
    }

    // Handles page switching for bank and property areas.
    public boolean handlePageButtonClick(double mouseX, double mouseY) {
        if (!isShowing()) {
            return false;
        }

        Player player = game.getPlayers().get(selectedPlayerIndex);

        if (isBankPrevClicked(mouseX, mouseY)) {
            if (bankPage > 0) {
                bankPage--;
            }
            return true;
        }

        if (isBankNextClicked(mouseX, mouseY)) {
            if (bankPage < getMaxPage(player.getBankCards().size())) {
                bankPage++;
            }
            return true;
        }

        if (isPropertyPrevClicked(mouseX, mouseY)) {
            if (propertyPage > 0) {
                propertyPage--;
            }
            return true;
        }

        if (isPropertyNextClicked(mouseX, mouseY)) {
            if (propertyPage < getMaxPage(player.getPropertyCards().size())) {
                propertyPage++;
            }
            return true;
        }

        return false;
    }

    private boolean isBankPrevClicked(double mouseX, double mouseY) {
        return isInside(mouseX, mouseY, bankPrevX, bankButtonY, pageButtonWidth, pageButtonHeight);
    }

    private boolean isBankNextClicked(double mouseX, double mouseY) {
        return isInside(mouseX, mouseY, bankNextX, bankButtonY, pageButtonWidth, pageButtonHeight);
    }

    private boolean isPropertyPrevClicked(double mouseX, double mouseY) {
        return isInside(mouseX, mouseY, propertyPrevX, propertyButtonY, pageButtonWidth, pageButtonHeight);
    }

    private boolean isPropertyNextClicked(double mouseX, double mouseY) {
        return isInside(mouseX, mouseY, propertyNextX, propertyButtonY, pageButtonWidth, pageButtonHeight);
    }

    // Checks whether the mouse position is inside a rectangle.
    private boolean isInside(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX <= x + width
                && mouseY >= y && mouseY <= y + height;
    }

    // Draws the whole player detail popup.
    public void draw(GraphicsContext gc) {
        if (!isShowing()) {
            return;
        }

        Player player = game.getPlayers().get(selectedPlayerIndex);

        keepPageInRange(player);
        drawOverlay(gc);
        drawPopupBox(gc);
        drawTitle(gc, player);
        drawCloseButton(gc);
        drawBasicInfo(gc, player);
        drawBankArea(gc, player);
        drawPropertyArea(gc, player);
        drawColorSummary(gc, player);
    }

    // Keeps bank and property pages inside valid ranges.
    private void keepPageInRange(Player player) {
        int maxBankPage = getMaxPage(player.getBankCards().size());
        int maxPropertyPage = getMaxPage(player.getPropertyCards().size());

        if (bankPage > maxBankPage) {
            bankPage = maxBankPage;
        }

        if (propertyPage > maxPropertyPage) {
            propertyPage = maxPropertyPage;
        }

        if (bankPage < 0) {
            bankPage = 0;
        }

        if (propertyPage < 0) {
            propertyPage = 0;
        }
    }

    // Calculates the last page index for cards.
    private int getMaxPage(int size) {
        if (size <= 0) {
            return 0;
        }

        return (size - 1) / cardsPerPage;
    }

    // Draws the dark background overlay.
    private void drawOverlay(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.72));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
    }

    // Draws the popup container.
    private void drawPopupBox(GraphicsContext gc) {
        gc.setFill(Color.rgb(18, 24, 35));
        gc.fillRoundRect(popupX, popupY, popupWidth, popupHeight, 24, 24);

        gc.setStroke(Color.rgb(255, 184, 77));
        gc.setLineWidth(2);
        gc.strokeRoundRect(popupX, popupY, popupWidth, popupHeight, 24, 24);
        gc.setLineWidth(1);
    }

    // Draws the popup title text.
    private void drawTitle(GraphicsContext gc, Player player) {
        gc.setFill(Color.rgb(255, 232, 180));
        gc.setFont(Font.font("Arial", 28));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Player " + (selectedPlayerIndex + 1) + " Details", popupX + 28, popupY + 24);

        gc.setFont(Font.font("Arial", 15));
        gc.setFill(Color.rgb(210, 218, 230));
        gc.fillText("Hand, bank area and property area overview", popupX + 30, popupY + 62);
    }

    // Draws the close button.
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

    // Draws hand, bank, and completed set information.
    private void drawBasicInfo(GraphicsContext gc, Player player) {
        double x = popupX + 30;
        double y = popupY + 100;

        int bankTotal = PlayerInfoHelper.getBankTotal(player);
        int completedSets = PlayerInfoHelper.getCompletedSetCount(player);

        drawInfoBadge(gc, x, y, 155, "Hand Cards", String.valueOf(player.getHandCards().size()));
        drawInfoBadge(gc, x + 175, y, 155, "Bank Total", bankTotal + "M");
        drawInfoBadge(gc, x + 350, y, 155, "Completed Sets", completedSets + "/3");
    }

    // Draws one information badge.
    private void drawInfoBadge(GraphicsContext gc, double x, double y, double width, String title, String value) {
        gc.setFill(Color.rgb(35, 45, 63));
        gc.fillRoundRect(x, y, width, 58, 14, 14);

        gc.setStroke(Color.rgb(255, 184, 77, 0.75));
        gc.strokeRoundRect(x, y, width, 58, 14, 14);

        gc.setFill(Color.rgb(210, 218, 230));
        gc.setFont(Font.font("Arial", 13));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(title, x + width / 2, y + 8);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 20));
        gc.fillText(value, x + width / 2, y + 29);
    }

    // Draws the selected player bank area.
    private void drawBankArea(GraphicsContext gc, Player player) {
        double titleX = popupX + 30;
        double titleY = popupY + 178;
        double startX = popupX + 30;
        double startY = popupY + 212;

        drawAreaTitle(gc, "Bank Area", titleX, titleY);

        if (player.getBankCards().isEmpty()) {
            drawEmptyText(gc, "No bank cards", startX, startY + 25);
            return;
        }

        drawCardsByPage(gc, player.getBankCards(), startX, startY, bankPage);
        drawPageController(
                gc,
                bankPrevX,
                bankNextX,
                bankButtonY,
                bankPage,
                getMaxPage(player.getBankCards().size())
        );
    }

    // Draws the selected player property area.
    private void drawPropertyArea(GraphicsContext gc, Player player) {
        double titleX = popupX + 30;
        double titleY = popupY + 335;
        double startX = popupX + 30;
        double startY = popupY + 368;

        drawAreaTitle(gc, "Property Area", titleX, titleY);

        if (player.getPropertyCards().isEmpty()) {
            drawEmptyText(gc, "No property cards", startX, startY + 25);
            return;
        }

        drawCardsByPage(gc, player.getPropertyCards(), startX, startY, propertyPage);
        drawPageController(
                gc,
                propertyPrevX,
                propertyNextX,
                propertyButtonY,
                propertyPage,
                getMaxPage(player.getPropertyCards().size())
        );
    }

    // Draws a section title.
    private void drawAreaTitle(GraphicsContext gc, String title, double x, double y) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 19));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(title, x, y);
    }

    // Draws cards for the current page only.
    private void drawCardsByPage(GraphicsContext gc, ArrayList<? extends Card> cards, double startX, double startY, int page) {
        int startIndex = page * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, cards.size());

        for (int i = startIndex; i < endIndex; i++) {
            Card card = cards.get(i);

            double x = startX + (i - startIndex) * cardGapX;
            double y = startY;

            drawSmallCard(gc, card, x, y);
        }
    }

    // Draws page buttons and page number.
    private void drawPageController(GraphicsContext gc, double prevX, double nextX, double y, int currentPage, int maxPage) {
        drawPageButton(gc, prevX, y, "<", currentPage > 0);
        drawPageButton(gc, nextX, y, ">", currentPage < maxPage);

        gc.setFill(Color.rgb(210, 218, 230));
        gc.setFont(Font.font("Arial", 14));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText((currentPage + 1) + "/" + (maxPage + 1), prevX + 64, y + pageButtonHeight / 2);

        gc.setTextBaseline(VPos.TOP);
    }

    // Draws a single page button.
    private void drawPageButton(GraphicsContext gc, double x, double y, String text, boolean enabled) {
        if (enabled) {
            gc.setFill(Color.rgb(255, 184, 77));
        } else {
            gc.setFill(Color.rgb(90, 90, 90));
        }

        gc.fillRoundRect(x, y, pageButtonWidth, pageButtonHeight, 8, 8);

        if (enabled) {
            gc.setStroke(Color.rgb(220, 130, 40));
        } else {
            gc.setStroke(Color.rgb(120, 120, 120));
        }

        gc.strokeRoundRect(x, y, pageButtonWidth, pageButtonHeight, 8, 8);

        if (enabled) {
            gc.setFill(Color.rgb(34, 26, 10));
        } else {
            gc.setFill(Color.rgb(190, 190, 190));
        }

        gc.setFont(Font.font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, x + pageButtonWidth / 2, y + pageButtonHeight / 2);

        gc.setTextBaseline(VPos.TOP);
    }

    // Draws text when an area has no cards.
    private void drawEmptyText(GraphicsContext gc, String text, double x, double y) {
        gc.setFill(Color.rgb(170, 180, 195));
        gc.setFont(Font.font("Arial", 15));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(text, x, y);
    }

    // Draws a small card preview.
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

    // Gets the card type title for display.
    private String getCardTitle(Card card) {
        if (card instanceof MoneyCards) {
            return "Money";
        }

        if (card instanceof PropertiesCards) {
            return "Property";
        }

        return "Card";
    }

    // Gets the card detail text for display.
    private String getCardDetail(Card card) {
        if (card instanceof MoneyCards) {
            return card.getValue() + "M";
        }

        if (card instanceof PropertiesCards propertyCard) {
            return getDisplayColorName(propertyCard.getCurrentColor());
        }

        return card.getValue() + "M";
    }

    // Gets the fallback card color.
    private Color getCardFillColor(Card card) {
        if (card instanceof MoneyCards) {
            return Color.GOLD;
        }

        if (card instanceof PropertiesCards) {
            return Color.LIGHTBLUE;
        }

        return Color.WHITE;
    }

    // Converts enum color names into readable text.
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

    private void drawColorSummary(GraphicsContext gc, Player player) {
        double panelX = popupX + 620;
        double panelY = popupY + 100;
        double panelWidth = 190;
        double panelHeight = 365;

        gc.setFill(Color.rgb(25, 34, 50));
        gc.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 18, 18);

        gc.setStroke(Color.rgb(255, 184, 77, 0.65));
        gc.strokeRoundRect(panelX, panelY, panelWidth, panelHeight, 18, 18);

        gc.setFill(Color.rgb(255, 232, 180));
        gc.setFont(Font.font("Arial", 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Color Summary", panelX + 14, panelY + 14);

        gc.setStroke(Color.rgb(255, 184, 77, 0.6));
        gc.strokeLine(panelX + 14, panelY + 43, panelX + panelWidth - 14, panelY + 43);

        double y = panelY + 58;

        for (PropertyColor color : PropertyColor.values()) {
            int count = PlayerInfoHelper.getPropertyCountByCurrentColor(player, color);
            int required = color.getAmountToCompleteSet();

            gc.setFill(getPropertyDotColor(color));
            gc.fillOval(panelX + 16, y + 4, 9, 9);

            gc.setFill(Color.rgb(230, 236, 246));
            gc.setFont(Font.font("Arial", 12));
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText(getShortColorName(color), panelX + 31, y);

            gc.setTextAlign(TextAlignment.RIGHT);
            gc.fillText(count + "/" + required, panelX + panelWidth - 16, y);

            y += 22;
        }

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
    }

    private Color getPropertyDotColor(PropertyColor color) {
        return switch (color) {
            case DARK_BLUE -> Color.rgb(25, 76, 140);
            case ORANGE -> Color.rgb(245, 145, 35);
            case BLACK -> Color.rgb(50, 50, 50);
            case RED -> Color.rgb(220, 55, 48);
            case DARK_GREEN -> Color.rgb(45, 140, 70);
            case BROWN -> Color.rgb(135, 85, 50);
            case PINK -> Color.rgb(230, 105, 170);
            case LIGHT_BLUE -> Color.rgb(75, 180, 230);
            case LIGHT_GREEN -> Color.rgb(145, 220, 85);
            case YELLOW -> Color.rgb(245, 200, 35);
        };
    }

    private String getShortColorName(PropertyColor color) {
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
}