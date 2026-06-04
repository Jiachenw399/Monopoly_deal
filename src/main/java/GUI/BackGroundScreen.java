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
    private final Game game;
    private static final model.HiddenCard HIDDEN_CARD_BACK = new model.HiddenCard();

    private final double smallCardWidth = 60;
    private final double smallCardHeight = 85;
    private final double cardWidth = 82;

    private int bankPageIndex = 0;
    private int propertyPageIndex = 0;
    private int lastPlayerIndex = -1;
    private boolean endTurnEnabled = true;
    private int turnRemainingSeconds = -1;

    private final int cardsPerPage = 8;

    private final double bankPrevX = 645;
    private final double bankNextX = 695;
    private final double bankArrowY = 130;

    private final double propertyPrevX = 645;
    private final double propertyNextX = 695;
    private final double propertyArrowY = 272;

    private final double arrowWidth = 34;
    private final double arrowHeight = 28;

    // Creates a BackGroundScreen instance.
    public BackGroundScreen(Game game) {
        this.game = game;
    }

    // Sets whether the End Turn button can be used.
    public void setEndTurnEnabled(boolean endTurnEnabled) {
        this.endTurnEnabled = endTurnEnabled;
    }

    public void setTurnRemainingSeconds(int turnRemainingSeconds) {
        this.turnRemainingSeconds = turnRemainingSeconds;
    }

    // Draws all background.
    public void drawAllBackground(Canvas canvas, PropertiesCards selectedWildCard) {
        drawAllBackground(canvas, selectedWildCard, game.getCurrentPlayerIndex(), false);
    }

    // Draws all background.
    public void drawAllBackground(Canvas canvas, PropertiesCards selectedWildCard, int displayPlayerIndex) {
        drawAllBackground(canvas, selectedWildCard, displayPlayerIndex, true);
    }

    // Draws all background.
    private void drawAllBackground(Canvas canvas, PropertiesCards selectedWildCard,
                                   int displayPlayerIndex, boolean showViewingPlayer) {
        Player displayPlayer = getDisplayPlayer(displayPlayerIndex);
        int currentPlayerIndex = game.getCurrentPlayerIndex();
        boolean showFaceDown = displayPlayer.isAI();

        resetPageWhenPlayerChanged(displayPlayerIndex);
        drawBackground(canvas);
        drawCurrentPlayer(canvas, displayPlayer, displayPlayerIndex, showViewingPlayer);
        drawBankCards(canvas, displayPlayer);
        drawPropertyCards(canvas, displayPlayer, selectedWildCard);
        drawHandCards(canvas, displayPlayer, showFaceDown);
        drawButtons(canvas);
        drawWinMessage(canvas);
    }

    //If a player change the page of the bank card, it should be reset when player changed
    private void resetPageWhenPlayerChanged(int displayPlayerIndex) {
        if (displayPlayerIndex != lastPlayerIndex) {
            bankPageIndex = 0;
            propertyPageIndex = 0;
            lastPlayerIndex = displayPlayerIndex;
        }
    }

    //Draw game background
    private void drawBackground(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        ScreenDrawHelper.drawPageBackground(gc, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
    }

    // Draws current player.
    private void drawCurrentPlayer(Canvas canvas, Player displayPlayer, int displayPlayerIndex,
                                   boolean showViewingPlayer) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        ScreenDrawHelper.drawPanel(gc, 16, 14, 735, 94);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);

        gc.setFill(ScreenDrawHelper.TEXT);
        gc.setFont(Font.font("Arial", 22));
        gc.fillText(getPlayerDisplayName(game.getCurrentPlayerIndex()) + "'s Turn", 36, 28);

        if (turnRemainingSeconds >= 0) {
            gc.setFill(ScreenDrawHelper.MUTED_TEXT);
            gc.setFont(Font.font("Arial", 14));
            gc.fillText(turnRemainingSeconds + "s left", 520, 33);
        }

        if (showViewingPlayer) {
            gc.setFill(ScreenDrawHelper.MUTED_TEXT);
            gc.setFont(Font.font("Arial", 14));
            gc.fillText("Viewing " + getPlayerDisplayName(displayPlayerIndex), 235, 33);
        }

        ScreenDrawHelper.drawBadge(gc, 36, 62, 145, 28,
                "Played " + displayPlayer.getUseCardTimes() + "/3",
                Color.rgb(255, 226, 166));

        int completedSets = PlayerInfoHelper.getCompletedSetCount(displayPlayer);
        ScreenDrawHelper.drawBadge(gc, 195, 62, 155, 28,
                "Sets " + completedSets + "/3",
                Color.rgb(167, 243, 208));

        drawDeckInfoBesideSets(gc);

        if (game.isDiscard()) {
            gc.setFill(ScreenDrawHelper.DANGER);
            gc.setFont(Font.font("Arial", 15));
            gc.fillText("Discard Phase: " + getPlayerDisplayName(game.getCurrentPlayerIndex()) + " must discard", 500, 67);
        }
    }

    // Returns the display name for a player.
    private String getPlayerDisplayName(int playerIndex) {
        if (playerIndex >= 0 && playerIndex < game.getPlayers().size()) {
            String name = game.getPlayers().get(playerIndex).getName();
            if (name != null) {
                return name;
            }
        }
        return "Player " + (playerIndex + 1);
    }

    //Draw 'Draw pile'
    private void drawDeckInfoBesideSets(GraphicsContext gc) {
        int remainingCards = game.getDrawCards().getDrawPile().size();

        double deckX = 375;
        double deckY = 42;
        double deckWidth = 34;
        double deckHeight = 44;

        int thickness = Math.max(1, Math.min(6, remainingCards / 15 + 1));

        for (int i = thickness - 1; i >= 0; i--) {
            double offset = i * 2.2;

            gc.setFill(Color.rgb(240, 245, 255));
            gc.fillRoundRect(deckX - offset, deckY + offset, deckWidth, deckHeight, 6, 6);

            gc.setStroke(Color.rgb(70, 85, 110));
            gc.strokeRoundRect(deckX - offset, deckY + offset, deckWidth, deckHeight, 6, 6);
        }

        gc.setFill(ScreenDrawHelper.ACCENT);
        gc.setFont(Font.font("Arial", 13));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(String.valueOf(remainingCards), deckX + deckWidth / 2, deckY + deckHeight / 2);

        gc.setFill(ScreenDrawHelper.MUTED_TEXT);
        gc.setFont(Font.font("Arial", 13));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Draw Pile", deckX + 48, deckY + 3);
        gc.fillText(remainingCards + " left", deckX + 48, deckY + 23);
    }

    // Draws bank cards.
    private void drawBankCards(Canvas canvas, Player displayPlayer) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        ScreenDrawHelper.drawPanel(gc, 16, 118, 735, 128);
        ScreenDrawHelper.drawSectionTitle(gc, "Bank Area", 32, 132);

        int total = PlayerInfoHelper.getBankTotal(displayPlayer);
        int cardCount = displayPlayer.getBankCards().size();
        int maxPage = ScreenDrawHelper.getMaxPage(cardCount, cardsPerPage);

        bankPageIndex = ScreenDrawHelper.keepPageInRange(bankPageIndex, maxPage);

        gc.setFont(Font.font("Arial", 15));
        gc.setFill(ScreenDrawHelper.ACCENT);
        gc.fillText("Total Money: " + total + "M", 190, 134);

        ScreenDrawHelper.drawPageText(gc, bankPageIndex, maxPage, 520, 134, ScreenDrawHelper.MUTED_TEXT);
        ScreenDrawHelper.drawArrowButtons(gc, bankPrevX, bankNextX, bankArrowY,
                arrowWidth, arrowHeight, bankPageIndex, maxPage);

        double startX = 32;
        double startY = 160;
        double cardGap = 75;

        int startIndex = bankPageIndex * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, cardCount);

        for (int i = startIndex; i < endIndex; i++) {
            Card card = displayPlayer.getBankCards().get(i);

            int displayIndex = i - startIndex;
            double x = startX + displayIndex * cardGap;
            double y = startY;

            if (!CardImageHelper.drawCardImage(gc, card, x, y, smallCardWidth, smallCardHeight)) {
                ScreenDrawHelper.drawSmallCard(gc, x, y, "Money", card.getValue() + "M", Color.GOLD);
            }
        }
    }

    // Draws property cards.
    private void drawPropertyCards(Canvas canvas, Player displayPlayer, PropertiesCards selectedWildCard) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        ScreenDrawHelper.drawPanel(gc, 16, 260, 735, 128);
        ScreenDrawHelper.drawSectionTitle(gc, "Property Area", 32, 274);

        int cardCount = displayPlayer.getPropertyCards().size();
        int maxPage = ScreenDrawHelper.getMaxPage(cardCount, cardsPerPage);

        propertyPageIndex = ScreenDrawHelper.keepPageInRange(propertyPageIndex, maxPage);

        ScreenDrawHelper.drawPageText(gc, propertyPageIndex, maxPage, 520, 276, ScreenDrawHelper.MUTED_TEXT);
        ScreenDrawHelper.drawArrowButtons(gc, propertyPrevX, propertyNextX, propertyArrowY,
                arrowWidth, arrowHeight, propertyPageIndex, maxPage);

        double startX = 32;
        double startY = 302;
        double cardGap = 75;

        int startIndex = propertyPageIndex * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, cardCount);

        for (int i = startIndex; i < endIndex; i++) {
            PropertiesCards card = displayPlayer.getPropertyCards().get(i);

            int displayIndex = i - startIndex;
            double x = startX + displayIndex * cardGap;
            double y = startY;

            String colorText = ScreenDrawHelper.getDisplayColorName(card.getCurrentColor());

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

            drawPropertyBuildingLabel(gc, displayPlayer, card, x, y);
        }

        drawWildColorButtons(gc, selectedWildCard);
    }

    // Draws property building label.
    private void drawPropertyBuildingLabel(GraphicsContext gc, Player displayPlayer, PropertiesCards card, double x, double y) {
        PropertyColor color = card.getCurrentColor();

        if (color == null) {
            return;
        }

        double labelY = y + smallCardHeight - 20;

        if (PlayerInfoHelper.hasHotel(displayPlayer, color)) {
            gc.setFill(Color.DARKBLUE);
            gc.setFont(Font.font("Arial", 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("HOTEL", x + smallCardWidth / 2, labelY);
            return;
        }

        if (PlayerInfoHelper.hasHouse(displayPlayer, color)) {
            gc.setFill(Color.DARKGREEN);
            gc.setFont(Font.font("Arial", 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("HOUSE", x + smallCardWidth / 2, labelY);
        }
    }

    // Draws wild color buttons.
    private void drawWildColorButtons(GraphicsContext gc, PropertiesCards selectedWildCard) {
        if (selectedWildCard == null) {
            return;
        }

        double x = 520;
        double y = 255;
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

            if (ScreenDrawHelper.isButtonPressed(buttonX, buttonY, w, h)) {
                ScreenDrawHelper.drawPressedButton(gc, buttonX, buttonY, w, h,
                        ScreenDrawHelper.getDisplayColorName(color));
                continue;
            }

            gc.setFill(Color.LIGHTYELLOW);
            gc.fillRoundRect(buttonX, buttonY, w, h, 8, 8);

            gc.setStroke(Color.BLACK);
            gc.strokeRoundRect(buttonX, buttonY, w, h, 8, 8);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(ScreenDrawHelper.getDisplayColorName(color), buttonX + w / 2, buttonY + h / 2);
        }

        gc.setTextBaseline(VPos.TOP);
    }

    // Draws hand cards.
    private void drawHandCards(Canvas canvas, Player displayPlayer, boolean showFaceDown) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        ArrayList<Card> handCards = displayPlayer.getHandCards();

        double titleY = Game.SCREEN_HEIGHT - 180;
        ScreenDrawHelper.drawPanel(gc, 16, titleY - 14, 745, 165);

        gc.setFill(ScreenDrawHelper.TEXT);
        gc.setFont(Font.font("Arial", 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Hand Cards", 32, titleY);

        double currentGap = getHandCardGap(handCards);

        for (int i = 0; i < handCards.size(); i++) {
            double handStartX = 20;
            double x = handStartX + i * (cardWidth + currentGap);
            double y = Game.SCREEN_HEIGHT - 150;

            Card card = handCards.get(i);
            drawHandCard(gc, card, x, y, i + 1, showFaceDown);
        }
    }

    // Finds hand card gap.
    private double getHandCardGap(ArrayList<Card> handCards) {
        double gap = 10;
        double currentGap = gap;

        if (handCards.size() > 1) {
            double totalWidth = handCards.size() * cardWidth + (handCards.size() - 1) * gap;

            double handAreaWidth = 740;
            if (totalWidth > handAreaWidth) {
                currentGap = (handAreaWidth - handCards.size() * cardWidth) / (handCards.size() - 1);
            }
        }

        return currentGap;
    }

    // Draws hand card.
    private void drawHandCard(GraphicsContext gc, Card card, double x, double y, int number, boolean showFaceDown) {
        double cardHeight = 112;

        if (showFaceDown) {
            if (!CardImageHelper.drawCardImage(gc, HIDDEN_CARD_BACK, x, y, cardWidth, cardHeight)) {
                drawCardBack(gc, x, y, cardWidth, cardHeight);
            }
            return;
        }

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

    // Draws card back.
    private void drawCardBack(GraphicsContext gc, double x, double y, double width, double height) {
        gc.setFill(Color.DARKBLUE);
        gc.fillRect(x, y, width, height);

        gc.setStroke(Color.CORNFLOWERBLUE);
        gc.setLineWidth(2);
        gc.strokeRect(x + 3, y + 3, width - 6, height - 6);

        double innerPad = 6;
        gc.strokeRect(x + innerPad, y + innerPad, width - innerPad * 2, height - innerPad * 2);

        double spacing = 8;
        int rows = 3;
        int cols = 3;
        double starW = 8;
        double starH = 8;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double sx = x + width / 2 - cols * starW / 2 + c * starW;
                double sy = y + height / 2 - rows * starH / 2 + r * starH;
                gc.setFill(Color.CORNFLOWERBLUE);
                gc.fillRect(sx, sy, starW - 2, starH - 2);
            }
        }
    }

    // Draws buttons.
    private void drawButtons(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        if (endTurnEnabled) {
            ScreenDrawHelper.drawButton(gc, 820, 520, 170, 40, "End Turn");
        } else {
            ScreenDrawHelper.drawDisabledButton(gc, 820, 520, 170, 40, "End Turn");
            gc.setFill(ScreenDrawHelper.MUTED_TEXT);
            gc.setFont(Font.font("Arial", 12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.TOP);
            gc.fillText("Waiting for your turn", 905, 505);
        }

        ScreenDrawHelper.drawButton(gc, 820, 570, 170, 40, "Back Menu");
    }

    // Draws win message.
    private void drawWinMessage(Canvas canvas) {
        if (!game.isWin()) {
            return;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        ScreenDrawHelper.drawOverlay(gc);
        ScreenDrawHelper.drawPanel(gc, 275, 190, 485, 185);

        gc.setFill(ScreenDrawHelper.ACCENT);
        gc.setFont(Font.font("Arial", 42));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        int winnerIndex = game.getWinnerIndex();
        if (winnerIndex < 0) {
            winnerIndex = game.getCurrentPlayerIndex();
        }
        gc.fillText(getPlayerDisplayName(winnerIndex) + " Wins!", Game.SCREEN_WIDTH / 2, 255);

        gc.setFill(ScreenDrawHelper.MUTED_TEXT);
        gc.setFont(Font.font("Arial", 17));
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("Congratulations!", Game.SCREEN_WIDTH / 2, 305);

        ScreenDrawHelper.drawButton(gc, 427.5, 335, 180, 40, "Play Again");

        gc.setTextBaseline(VPos.TOP);
    }

    // Checks whether the play again button was clicked.
    public boolean isPlayAgainClicked(double mouseX, double mouseY) {
        return game.isWin() && ScreenDrawHelper.isInside(mouseX, mouseY, 427.5, 335, 180, 40);
    }

    // Handles page button click.
    public boolean handlePageButtonClick(double mouseX, double mouseY, int displayPlayerIndex) {
        if (handleBankPageButtonClick(mouseX, mouseY, displayPlayerIndex)) {
            return true;
        }

        return handlePropertyPageButtonClick(mouseX, mouseY, displayPlayerIndex);
    }

    // Handles page button click.
    public boolean handlePageButtonClick(double mouseX, double mouseY) {
        return handlePageButtonClick(mouseX, mouseY, game.getCurrentPlayerIndex());
    }

    // Finds clicked wild card.
    public PropertiesCards getClickedWildCard(double mouseX, double mouseY, int displayPlayerIndex) {
        Player currentPlayer = getDisplayPlayer(displayPlayerIndex);
        int startIndex = propertyPageIndex * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, currentPlayer.getPropertyCards().size());

        double startX = 32;
        double startY = 302;
        double cardGap = 75;

        for (int i = startIndex; i < endIndex; i++) {
            PropertiesCards card = currentPlayer.getPropertyCards().get(i);

            if (!card.isWildCard()) {
                continue;
            }

            int displayIndex = i - startIndex;
            double x = startX + displayIndex * cardGap;

            if (ScreenDrawHelper.isInside(mouseX, mouseY, x, startY, smallCardWidth, smallCardHeight)) {
                return card;
            }
        }

        return null;
    }

    // Finds clicked wild card.
    public PropertiesCards getClickedWildCard(double mouseX, double mouseY) {
        return getClickedWildCard(mouseX, mouseY, game.getCurrentPlayerIndex());
    }

    // Handles bank page button click.
    private boolean handleBankPageButtonClick(double mouseX, double mouseY, int displayPlayerIndex) {
        Player currentPlayer = getDisplayPlayer(displayPlayerIndex);
        int maxPage = ScreenDrawHelper.getMaxPage(currentPlayer.getBankCards().size(), cardsPerPage);

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
    private boolean handlePropertyPageButtonClick(double mouseX, double mouseY, int displayPlayerIndex) {
        Player currentPlayer = getDisplayPlayer(displayPlayerIndex);
        int maxPage = ScreenDrawHelper.getMaxPage(currentPlayer.getPropertyCards().size(), cardsPerPage);

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

    // Finds display player.
    private Player getDisplayPlayer(int displayPlayerIndex) {
        if (displayPlayerIndex < 0 || displayPlayerIndex >= game.getPlayers().size()) {
            return game.getCurrentPlayer();
        }

        return game.getPlayers().get(displayPlayerIndex);
    }

}
