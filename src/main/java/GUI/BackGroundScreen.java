package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;
import logic.PlayerInfoHelper;
import model.ActionCards;
import model.Card;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;

public class BackGroundScreen {
    private static final double RIGHT_COLUMN_X = 760;
    private static final double CONTENT_X = ScreenDrawHelper.tableContentX();
    private static final double CONTENT_Y = ScreenDrawHelper.tableContentY();
    private static final double CONTENT_WIDTH =
            ScreenDrawHelper.tableContentWidth(Game.SCREEN_WIDTH, RIGHT_COLUMN_X);

    private static final double DECK_CENTER_X = 858;
    private static final double DECK_CENTER_Y = 372;
    private static final double DECK_CARD_WIDTH = 54;
    private static final double DECK_CARD_HEIGHT = 72;

    private final Game game;

    private final double smallCardWidth = 60;
    private final double smallCardHeight = 85;
    private final double cardWidth = 82;

    private int bankPageIndex = 0;
    private int propertyPageIndex = 0;
    private int lastPlayerIndex = -1;

    private final int cardsPerPage = 8;

    private final double bankPrevX = CONTENT_X + 629;
    private final double bankNextX = CONTENT_X + 679;
    private final double bankArrowY = 136;

    private final double propertyPrevX = CONTENT_X + 629;
    private final double propertyNextX = CONTENT_X + 679;
    private final double propertyArrowY = 278;

    private final double arrowWidth = 34;
    private final double arrowHeight = 28;

    public BackGroundScreen(Game game) {
        this.game = game;
    }

    public void drawAllBackground(Canvas canvas, PropertiesCards selectedWildCard) {
        resetPageWhenPlayerChanged();
        drawBackground(canvas);
        drawCurrentPlayer(canvas);
        drawDrawPile(canvas);
        drawBankCards(canvas);
        drawPropertyCards(canvas, selectedWildCard);
        drawHandCards(canvas);
        drawButtons(canvas);
        drawWinMessage(canvas);
    }

    //If a player change the page of the bank card, it should be reset when player changed
    private void resetPageWhenPlayerChanged() {
        int currentPlayerIndex = game.getCurrentPlayerIndex();

        if (currentPlayerIndex != lastPlayerIndex) {
            bankPageIndex = 0;
            propertyPageIndex = 0;
            lastPlayerIndex = currentPlayerIndex;
        }
    }

    //Draw game background
    private void drawBackground(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        ScreenDrawHelper.drawGameTableBackground(gc, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
    }

    private void drawCurrentPlayer(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Player currentPlayer = game.getCurrentPlayer();

        ScreenDrawHelper.drawPanel(gc, CONTENT_X, CONTENT_Y, CONTENT_WIDTH, 94);

        double textX = CONTENT_X + 20;
        double badgeY = CONTENT_Y + 48;

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);

        gc.setFill(ScreenDrawHelper.TEXT);
        gc.setFont(Font.font("Arial", 22));
        gc.fillText("Player " + (game.getCurrentPlayerIndex() + 1) + "'s Turn", textX, CONTENT_Y + 14);

        ScreenDrawHelper.drawBadge(gc, textX, badgeY, 145, 28,
                "Played " + currentPlayer.getUseCardTimes() + "/3",
                Color.rgb(255, 226, 166));

        int completedSets = PlayerInfoHelper.getCompletedSetCount(currentPlayer);
        ScreenDrawHelper.drawBadge(gc, textX + 159, badgeY, 155, 28,
                "Sets " + completedSets + "/3",
                Color.rgb(167, 243, 208));

        if (game.isDiscard()) {
            gc.setFill(ScreenDrawHelper.DANGER);
            gc.setFont(Font.font("Arial", 15));
            gc.fillText("Discard Phase: discard " + (currentPlayer.getHandCards().size() - 7) + " card(s)",
                    CONTENT_X + CONTENT_WIDTH - 310, CONTENT_Y + 53);
        }
    }

    private void drawDrawPile(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        int remainingCards = game.getDrawCards().getDrawPile().size();

        double deckX = DECK_CENTER_X - DECK_CARD_WIDTH / 2;
        double deckY = DECK_CENTER_Y - DECK_CARD_HEIGHT / 2 - 12;

        int thickness = Math.max(1, Math.min(6, remainingCards / 15 + 1));

        for (int i = thickness - 1; i >= 0; i--) {
            double offset = i * 3;

            gc.setFill(Color.rgb(240, 245, 255));
            gc.fillRoundRect(deckX - offset, deckY + offset, DECK_CARD_WIDTH, DECK_CARD_HEIGHT, 8, 8);

            gc.setStroke(Color.rgb(70, 85, 110));
            gc.strokeRoundRect(deckX - offset, deckY + offset, DECK_CARD_WIDTH, DECK_CARD_HEIGHT, 8, 8);
        }

        gc.setFill(ScreenDrawHelper.ACCENT_DARK);
        gc.setFont(Font.font("Arial", 18));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(String.valueOf(remainingCards), deckX + DECK_CARD_WIDTH / 2, deckY + DECK_CARD_HEIGHT / 2);

        double labelY = deckY + DECK_CARD_HEIGHT + 14;
        gc.setFill(ScreenDrawHelper.TEXT);
        gc.setFont(Font.font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Draw Pile", DECK_CENTER_X, labelY);

        gc.setFill(ScreenDrawHelper.MUTED_TEXT);
        gc.setFont(Font.font("Arial", 14));
        gc.fillText(remainingCards + " left", DECK_CENTER_X, labelY + 22);
    }

    private void drawBankCards(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Player currentPlayer = game.getCurrentPlayer();

        double bankY = CONTENT_Y + 112;
        ScreenDrawHelper.drawPanel(gc, CONTENT_X, bankY, CONTENT_WIDTH, 128);
        ScreenDrawHelper.drawSectionTitle(gc, "Bank Area", CONTENT_X + 16, bankY + 14);

        int total = PlayerInfoHelper.getBankTotal(currentPlayer);
        int cardCount = currentPlayer.getBankCards().size();
        int maxPage = getMaxPage(cardCount);

        bankPageIndex = keepPageInRange(bankPageIndex, maxPage);

        gc.setFont(Font.font("Arial", 15));
        gc.setFill(ScreenDrawHelper.ACCENT);
        gc.fillText("Total Money: " + total + "M", CONTENT_X + 174, bankY + 16);

        drawPageText(gc, bankPageIndex, maxPage, CONTENT_X + 504, bankY + 16);
        drawArrowButtons(gc, bankPrevX, bankNextX, bankArrowY, bankPageIndex, maxPage);

        double startX = CONTENT_X + 16;
        double startY = bankY + 42;
        double cardGap = 75;

        int startIndex = bankPageIndex * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, cardCount);

        for (int i = startIndex; i < endIndex; i++) {
            Card card = currentPlayer.getBankCards().get(i);

            int displayIndex = i - startIndex;
            double x = startX + displayIndex * cardGap;
            double y = startY;

            if (!CardImageHelper.drawCardImage(gc, card, x, y, smallCardWidth, smallCardHeight)) {
                ScreenDrawHelper.drawSmallCard(gc, x, y, "Money", card.getValue() + "M", Color.GOLD);
            }
        }
    }

    private void drawPropertyCards(Canvas canvas, PropertiesCards selectedWildCard) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Player currentPlayer = game.getCurrentPlayer();

        double propertyY = CONTENT_Y + 254;
        ScreenDrawHelper.drawPanel(gc, CONTENT_X, propertyY, CONTENT_WIDTH, 128);
        ScreenDrawHelper.drawSectionTitle(gc, "Property Area", CONTENT_X + 16, propertyY + 14);

        int cardCount = currentPlayer.getPropertyCards().size();
        int maxPage = getMaxPage(cardCount);

        propertyPageIndex = keepPageInRange(propertyPageIndex, maxPage);

        drawPageText(gc, propertyPageIndex, maxPage, CONTENT_X + 504, propertyY + 16);
        drawArrowButtons(gc, propertyPrevX, propertyNextX, propertyArrowY, propertyPageIndex, maxPage);

        double startX = CONTENT_X + 16;
        double startY = propertyY + 42;
        double cardGap = 75;

        int startIndex = propertyPageIndex * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, cardCount);

        for (int i = startIndex; i < endIndex; i++) {
            PropertiesCards card = currentPlayer.getPropertyCards().get(i);

            int displayIndex = i - startIndex;
            double x = startX + displayIndex * cardGap;
            double y = startY;

            String colorText = getDisplayColorName(card.getCurrentColor());

            if (card == selectedWildCard) {
                gc.setStroke(ScreenDrawHelper.ACCENT);
                gc.setLineWidth(4);
                gc.strokeRoundRect(x - 3, y - 3, smallCardWidth + 6, smallCardHeight + 6, 12, 12);
                gc.setLineWidth(1);
            }

            boolean hasImage = CardImageHelper.drawCardImage(gc, card, x, y, smallCardWidth, smallCardHeight);

            if (!hasImage) {
                ScreenDrawHelper.drawSmallCard(gc, x, y, "Property", colorText, Color.LIGHTBLUE);

                if (card.isWildCard()) {
                    gc.setFill(Color.RED);
                    gc.setFont(Font.font("Arial", 10));
                    gc.setTextAlign(TextAlignment.CENTER);
                    gc.fillText("WILD", x + 30, y + 75);
                }
            }

            drawPropertyBuildingLabel(gc, card, x, y);
        }

        drawWildColorButtons(gc, selectedWildCard, propertyY);
    }

    private int getMaxPage(int cardCount) {
        if (cardCount <= 0) {
            return 0;
        }

        return (cardCount - 1) / cardsPerPage;
    }

    private int keepPageInRange(int pageIndex, int maxPage) {
        if (pageIndex < 0) {
            return 0;
        }

        if (pageIndex > maxPage) {
            return maxPage;
        }

        return pageIndex;
    }

    private void drawPageText(GraphicsContext gc, int pageIndex, int maxPage, double x, double y) {
        gc.setFill(ScreenDrawHelper.MUTED_TEXT);
        gc.setFont(Font.font("Arial", 13));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Page " + (pageIndex + 1) + "/" + (maxPage + 1), x, y);
    }

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

    private void drawPropertyBuildingLabel(GraphicsContext gc, PropertiesCards card, double x, double y) {
        PropertyColor color = card.getCurrentColor();

        if (color == null) {
            return;
        }

        double labelY = y + smallCardHeight - 20;

        if (PlayerInfoHelper.hasHotel(game.getCurrentPlayer(), color)) {
            gc.setFill(Color.DARKBLUE);
            gc.setFont(Font.font("Arial", 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("HOTEL", x + smallCardWidth / 2, labelY);
            return;
        }

        if (PlayerInfoHelper.hasHouse(game.getCurrentPlayer(), color)) {
            gc.setFill(Color.DARKGREEN);
            gc.setFont(Font.font("Arial", 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("HOUSE", x + smallCardWidth / 2, labelY);
        }
    }

    private void drawWildColorButtons(GraphicsContext gc, PropertiesCards selectedWildCard, double propertyPanelY) {
        if (selectedWildCard == null) {
            return;
        }

        double x = CONTENT_X + 488;
        double y = propertyPanelY - 5;
        double w = 115;
        double h = 28;
        double gapX = 10;
        double gapY = 8;
        int buttonsPerRow = 2;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 16));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Choose Wild Color:", x, y - 25);

        for (int i = 0; i < selectedWildCard.getType().getColors().size(); i++) {
            PropertyColor color = selectedWildCard.getType().getColors().get(i);

            int row = i / buttonsPerRow;
            int col = i % buttonsPerRow;

            double buttonX = x + col * (w + gapX);
            double buttonY = y + row * (h + gapY);

            gc.setFill(Color.LIGHTYELLOW);
            gc.fillRoundRect(buttonX, buttonY, w, h, 8, 8);

            gc.setStroke(Color.BLACK);
            gc.strokeRoundRect(buttonX, buttonY, w, h, 8, 8);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(getDisplayColorName(color), buttonX + w / 2, buttonY + h / 2);
        }

        gc.setTextBaseline(VPos.TOP);
    }

    private void drawHandCards(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Player currentPlayer = game.getCurrentPlayer();
        ArrayList<Card> handCards = currentPlayer.getHandCards();

        double currentGap = getHandCardGap(handCards);

        for (int i = 0; i < handCards.size(); i++) {
            double handStartX = CONTENT_X + 4;
            double x = handStartX + i * (cardWidth + currentGap);
            double y = Game.SCREEN_HEIGHT - 150;

            Card card = handCards.get(i);
            drawHandCard(gc, card, x, y, i + 1);
        }
    }

    private double getHandCardGap(ArrayList<Card> handCards) {
        double gap = 10;
        double currentGap = gap;

        if (handCards.size() > 1) {
            double totalWidth = handCards.size() * cardWidth + (handCards.size() - 1) * gap;

            double handAreaWidth = CONTENT_WIDTH - 8;
            if (totalWidth > handAreaWidth) {
                currentGap = (handAreaWidth - handCards.size() * cardWidth) / (handCards.size() - 1);
            }
        }

        return currentGap;
    }

    private void drawHandCard(GraphicsContext gc, Card card, double x, double y, int number) {
        double cardHeight = 112;

        if (CardImageHelper.drawCardImage(gc, card, x, y, cardWidth, cardHeight)) {
            return;
        }

        Color color = Color.WHITE;
        String type = "";
        String name = "";
        String value = card.getValue() + "M";

        switch (card) {
            case MoneyCards moneyCards -> {
                color = Color.GOLD;
                type = "Money";
                name = value;
            }
            case PropertiesCards propertyCard -> {
                color = Color.LIGHTBLUE;
                type = "Property";
                name = propertyCard.getType().name();
            }
            case ActionCards actionCard -> {
                color = Color.LIGHTPINK;
                type = "Action";
                name = actionCard.getActionCardType().name();
            }
            default -> {
            }
        }

        gc.setFill(color);
        gc.fillRoundRect(x, y, cardWidth, cardHeight, 15, 15);

        gc.setStroke(Color.BLACK);
        gc.strokeRoundRect(x, y, cardWidth, cardHeight, 15, 15);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 13));
        gc.setTextAlign(TextAlignment.CENTER);

        gc.fillText(type, x + cardWidth / 2, y + 30);
        gc.fillText(value, x + cardWidth / 2, y + 50);

        gc.setFont(Font.font("Arial", 10));
        ScreenDrawHelper.drawWrappedText(gc, name, x + 6, y + 70, cardWidth - 12, 12);
    }

    private void drawButtons(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        ScreenDrawHelper.drawButton(gc, 820, 520, 170, 40, "End Turn");
        ScreenDrawHelper.drawButton(gc, 820, 570, 170, 40, "Back Menu");
    }

    private void drawWinMessage(Canvas canvas) {
        if (!game.isWin()) {
            return;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        ScreenDrawHelper.drawOverlay(gc);
        ScreenDrawHelper.drawPanel(gc, 275, 220, 485, 155);

        gc.setFill(ScreenDrawHelper.ACCENT);
        gc.setFont(Font.font("Arial", 42));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("Player " + (game.getCurrentPlayerIndex() + 1) + " Wins!", Game.SCREEN_WIDTH / 2, 285);

        gc.setFill(ScreenDrawHelper.MUTED_TEXT);
        gc.setFont(Font.font("Arial", 17));
        gc.fillText("Congratulations!", Game.SCREEN_WIDTH / 2, 332);
    }

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

    public boolean handlePageButtonClick(double mouseX, double mouseY) {
        if (handleBankPageButtonClick(mouseX, mouseY)) {
            return true;
        }

        return handlePropertyPageButtonClick(mouseX, mouseY);
    }

    private boolean handleBankPageButtonClick(double mouseX, double mouseY) {
        Player currentPlayer = game.getCurrentPlayer();
        int maxPage = getMaxPage(currentPlayer.getBankCards().size());

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

    private boolean handlePropertyPageButtonClick(double mouseX, double mouseY) {
        Player currentPlayer = game.getCurrentPlayer();
        int maxPage = getMaxPage(currentPlayer.getPropertyCards().size());

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
}
