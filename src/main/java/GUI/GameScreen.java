package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;
import java.util.ArrayList;
import model.*;


public class GameScreen {
    private int viewedPlayerIndex = 0;
    private Canvas canvas;
    private Game game;
    private boolean isShow;
    private ActionCards pendingSlyDealCard = null;
    private double slyPanelX = 180;
    private double slyPanelY = 110;
    private double slyCardWidth = 90;
    private double slyCardHeight = 120;
    private double slyGap = 15;
    private ActionCards pendingTwoColorRentCard = null;

    private ActionCards pendingMultipleColorRentCard = null;
    private Player selectedMultipleColorRentTarget = null;
    private PropertyColor selectedMultipleColorRentColor = null;

    private boolean useDoubleRentForTwoColorRent = false;
    private boolean useDoubleRentForMultipleColorRent = false;

    private double multiRentPanelX = 230;
    private double multiRentPanelY = 120;
    private double multiRentButtonWidth = 165;
    private double multiRentButtonHeight = 42;

    private ActionCards pendingDealBreakerCard = null;
    private double dealBreakerPanelX = 180;
    private double dealBreakerPanelY = 135;
    private double dealBreakerCardWidth = 130;
    private double dealBreakerCardHeight = 120;
    private double dealBreakerGap = 20;
    private ArrayList<Card> selectedPaymentCards = new ArrayList<>();

    private double rentPanelX = 330;
    private double rentPanelY = 190;
    private double rentButtonWidth = 170;
    private double rentButtonHeight = 55;

    private ActionCards pendingDebtCollectorCard = null;

    private double debtPanelX = 170;
    private double debtPanelY = 135;
    private double debtPlayerWidth = 160;
    private double debtPlayerHeight = 230;
    private double debtPlayerGap = 25;

    private PropertiesCards selectedWildCard = null;
    private double propertyStartX = 20;
    private double propertyStartY = 285;
    private double smallCardWidth = 60;
    private double smallCardHeight = 85;
    private double propertyGap = 75;
    private double cardWidth = 90;
    private double cardHeight = 125;
    private double gap = 12;
    private double handStartX = 20;
    private double handAreaX = 20;
    private double handAreaWidth = 740;
    private double handStartY = Game.SCREEN_HEIGHT - 150;

    public GameScreen(Game game) {
        this.game = game;
        canvas = new Canvas(Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
        this.isShow = false;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }

    public void paint() {
        drawBackground();
        drawCurrentPlayer();
        drawDeckInfo();
        drawBankCards();
        drawPropertyCards();
        drawHandCards();
        drawPlayerViewButtons();
        drawViewedPlayerInfo();
        drawButtons();
        drawWinMessage();
        drawSlyDealSelection();
        drawDebtCollectorSelection();
        drawTwoColorRentSelection();
        drawDealBreakerSelection();
        drawPaymentSelection();
        drawMultipleColorRentSelection();
    }

    public void drawBackground() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
        gc.setFill(Color.rgb(25, 34, 55));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
    }

    private void drawCurrentPlayer() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Player currentPlayer = game.getCurrentPlayer();

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);

        gc.fillText("Current Player: Player " + (game.getCurrentPlayerIndex() + 1), 20, 20);
        gc.fillText("Played Cards: " + currentPlayer.getUseCardTimes() + "/3", 20, 45);

        int completedSets = getCompletedSetCount(currentPlayer);
        gc.setFill(Color.LIGHTGREEN);
        gc.fillText("Completed Sets: " + completedSets + "/3", 20, 70);

        if (game.isDiscard()) {
            gc.setFill(Color.RED);
            gc.fillText("Discard Phase: You must discard " + (currentPlayer.getHandCards().size() - 7) + " card(s).", 20, 95);
        } else {
            gc.setFill(Color.LIGHTYELLOW);
            gc.fillText("Click a hand card to play it. Press END TURN button to finish.", 20, 95);
        }
    }

    private void drawDeckInfo() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Draw Pile: " + game.getDrawCards().getDrawPile().size(), 780, 20);
        gc.fillText("Discard Pile: " + game.getDrawCards().getDiscardPile().size(), 780, 45);
    }

    private void drawBankCards() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Player currentPlayer = game.getCurrentPlayer();

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 18));
        gc.fillText("Bank Area", 20, 115);

        double x = 20;
        double y = 145;

        int total = 0;
        for (Card card : currentPlayer.getBankCards()) {
            total += card.getValue();
        }

        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Total Money: " + total + "M", 120, 115);

        int index = 0;
        for (Card card : currentPlayer.getBankCards()) {
            drawSmallCard(gc, x + index * 65, y, "Money", card.getValue() + "M", Color.GOLD);
            index++;
        }
    }

    private void drawPropertyCards() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Player currentPlayer = game.getCurrentPlayer();

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Property Area", 20, 255);

        int index = 0;
        for (PropertiesCards card : currentPlayer.getPropertyCards()) {
            double x = propertyStartX + index * propertyGap;
            double y = propertyStartY;

            String colorText = card.getCurrentColor() == null ? "NO COLOR" : card.getCurrentColor().name();

            if (card == selectedWildCard) {
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(4);
                gc.strokeRoundRect(x - 3, y - 3, smallCardWidth + 6, smallCardHeight + 6, 12, 12);
                gc.setLineWidth(1);
            }

            drawSmallCard(gc, x, y, "Property", colorText, Color.LIGHTBLUE);

            if (card.isWildCard()) {
                gc.setFill(Color.RED);
                gc.setFont(Font.font("Arial", 10));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("WILD", x + 30, y + 75);
            }

            index++;
        }

        drawWildColorButtons();
    }

    private void drawWildColorButtons() {
        if (selectedWildCard == null) {
            return;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();

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

            gc.setFill(Color.LIGHTYELLOW);
            gc.fillRoundRect(buttonX, buttonY, w, h, 8, 8);

            gc.setStroke(Color.BLACK);
            gc.strokeRoundRect(buttonX, buttonY, w, h, 8, 8);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(color.name(), buttonX + w / 2, buttonY + h / 2);
        }

        gc.setTextBaseline(VPos.TOP);
    }

    public PropertiesCards getClickedWildCard(double mouseX, double mouseY) {
        Player currentPlayer = game.getCurrentPlayer();

        for (int i = 0; i < currentPlayer.getPropertyCards().size(); i++) {
            PropertiesCards card = currentPlayer.getPropertyCards().get(i);

            if (!card.isWildCard()) {
                continue;
            }

            double x = propertyStartX + i * propertyGap;
            double y = propertyStartY;

            if (mouseX >= x && mouseX <= x + smallCardWidth
                    && mouseY >= y && mouseY <= y + smallCardHeight) {
                return card;
            }
        }

        return null;
    }

    public PropertyColor getClickedWildColorButton(double mouseX, double mouseY) {
        if (selectedWildCard == null) {
            return null;
        }

        double x = 520;
        double y = 255;
        double w = 115;
        double h = 28;
        double gapX = 10;
        double gapY = 8;
        int buttonsPerRow = 2;

        for (int i = 0; i < selectedWildCard.getType().getColors().size(); i++) {
            int row = i / buttonsPerRow;
            int col = i % buttonsPerRow;

            double buttonX = x + col * (w + gapX);
            double buttonY = y + row * (h + gapY);

            if (mouseX >= buttonX && mouseX <= buttonX + w
                    && mouseY >= buttonY && mouseY <= buttonY + h) {
                return selectedWildCard.getType().getColors().get(i);
            }
        }

        return null;
    }


    private void drawHandCards() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Player currentPlayer = game.getCurrentPlayer();
        ArrayList<Card> handCards = currentPlayer.getHandCards();

        double titleY = Game.SCREEN_HEIGHT - 180;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Hand Cards", handAreaX, titleY);

        double startY = handStartY;
        double availableWidth = handAreaWidth;
        double currentGap = gap;

        if (handCards.size() > 1) {
            double totalWidth = handCards.size() * cardWidth + (handCards.size() - 1) * gap;

            if (totalWidth > availableWidth) {
                currentGap = (availableWidth - handCards.size() * cardWidth) / (handCards.size() - 1);
            }
        }

        for (int i = 0; i < handCards.size(); i++) {
            double x = handStartX + i * (cardWidth + currentGap);
            double y = startY;

            Card card = handCards.get(i);
            drawHandCard(gc, card, x, y, i + 1);
        }
    }

    private void drawHandCard(GraphicsContext gc, Card card, double x, double y, int number) {
        Color color = Color.WHITE;
        String type = "";
        String name = "";
        String value = card.getValue() + "M";

        if (card instanceof MoneyCards) {
            color = Color.GOLD;
            type = "Money";
            name = value;
        } else if (card instanceof PropertiesCards propertyCard) {
            color = Color.LIGHTBLUE;
            type = "Property";
            name = propertyCard.getType().name();
        } else if (card instanceof ActionCards actionCard) {
            color = Color.LIGHTPINK;
            type = "Action";
            name = actionCard.getActionCardType().name();
        }

        gc.setFill(color);
        gc.fillRoundRect(x, y, cardWidth, cardHeight, 15, 15);

        gc.setStroke(Color.BLACK);
        gc.strokeRoundRect(x, y, cardWidth, cardHeight, 15, 15);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 13));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(number + "", x + cardWidth / 2, y + 8);
        gc.fillText(type, x + cardWidth / 2, y + 30);
        gc.fillText(value, x + cardWidth / 2, y + 50);

        gc.setFont(Font.font("Arial", 10));
        drawWrappedText(gc, name, x + 6, y + 70, cardWidth - 12, 12);
    }

    private void drawPlayerViewButtons() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double x = 760;
        double y = 90;
        double w = 100;
        double h = 35;
        double gap = 10;

        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (i == viewedPlayerIndex) {
                gc.setFill(Color.LIGHTGREEN);
            } else {
                gc.setFill(Color.LIGHTGRAY);
            }

            gc.fillRoundRect(x, y + i * (h + gap), w, h, 10, 10);
            gc.setStroke(Color.BLACK);
            gc.strokeRoundRect(x, y + i * (h + gap), w, h, 10, 10);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 14));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText("PLAYER " + (i + 1), x + w / 2, y + i * (h + gap) + h / 2);
        }

        gc.setTextBaseline(VPos.TOP);
    }

    private void drawHotelIcon(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.DARKBLUE);
        gc.fillRect(x + 2, y + 2, 14, 14);

        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(x + 5, y + 5, 3, 3);
        gc.fillRect(x + 10, y + 5, 3, 3);
        gc.fillRect(x + 5, y + 10, 3, 3);
        gc.fillRect(x + 10, y + 10, 3, 3);

        gc.setStroke(Color.BLACK);
        gc.strokeRect(x + 2, y + 2, 14, 14);
    }

    private void drawHouseIcon(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.DARKRED);
        gc.fillPolygon(
                new double[]{x, x + 8, x + 16},
                new double[]{y + 8, y, y + 8},
                3
        );

        gc.setFill(Color.RED);
        gc.fillRect(x + 2, y + 8, 12, 8);

        gc.setStroke(Color.BLACK);
        gc.strokePolygon(
                new double[]{x, x + 8, x + 16},
                new double[]{y + 8, y, y + 8},
                3
        );
        gc.strokeRect(x + 2, y + 8, 12, 8);
    }

    private void drawViewedPlayerInfo() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        if (viewedPlayerIndex < 0 || viewedPlayerIndex >= game.getPlayers().size()) {
            return;
        }

        Player viewedPlayer = game.getPlayers().get(viewedPlayerIndex);

        double boxX = 865;
        double boxY = 85;
        double boxW = 160;
        double boxH = 430;

        gc.setFill(Color.rgb(240, 240, 240));
        gc.fillRoundRect(boxX, boxY, boxW, boxH, 15, 15);

        gc.setStroke(Color.BLACK);
        gc.strokeRoundRect(boxX, boxY, boxW, boxH, 15, 15);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 15));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);

        double textX = boxX + 10;
        double textY = boxY + 10;

        gc.fillText("Viewing: P" + (viewedPlayerIndex + 1), textX, textY);
        gc.fillText("Hand: " + viewedPlayer.getHandCards().size(), textX, textY + 25);

        int completedSets = getCompletedSetCount(viewedPlayer);
        gc.setFill(Color.GREEN);
        gc.fillText("Sets: " + completedSets + "/3", textX, textY + 50);

        int bankTotal = 0;
        for (Card card : viewedPlayer.getBankCards()) {
            bankTotal += card.getValue();
        }

        gc.setFill(Color.BLACK);
        gc.fillText("Bank: " + bankTotal + "M", textX, textY + 75);

        gc.setFont(Font.font("Arial", 13));
        gc.fillText("Money Cards:", textX, textY + 105);

        double moneyY = textY + 128;
        int moneyCount = 0;

        if (viewedPlayer.getBankCards().isEmpty()) {
            gc.fillText("None", textX, moneyY);
        } else {
            String moneyText = "";

            for (Card card : viewedPlayer.getBankCards()) {
                moneyText += card.getValue() + "M ";
                moneyCount++;

                if (moneyCount >= 6) {
                    break;
                }
            }

            gc.fillText(moneyText, textX, moneyY);

            if (viewedPlayer.getBankCards().size() > 6) {
                gc.fillText("...", textX, moneyY + 18);
            }
        }

        gc.setFont(Font.font("Arial", 14));
        gc.setFill(Color.BLACK);
        gc.fillText("Property Sets:", textX, boxY + 190);

        double leftX = boxX + 10;
        double rightX = boxX + 85;
        double startY = boxY + 220;
        double lineGap = 30;

        PropertyColor[] colors = PropertyColor.values();

        for (int i = 0; i < colors.length; i++) {
            PropertyColor color = colors[i];

            int current = getPropertyCountByCurrentColor(viewedPlayer, color);
            int need = color.getAmountToCompleteSet();

            boolean hasHouse = false;
            boolean hasHotel = false;

            for (PropertiesCards card : viewedPlayer.getPropertyCards()) {
                if (card.getCurrentColor() == color) {
                    if (card.hasHouse()) {
                        hasHouse = true;
                    }

                    if (card.hasHotel()) {
                        hasHotel = true;
                    }
                }
            }

            double x;
            double y;

            if (i < 5) {
                x = leftX;
                y = startY + i * lineGap;
            } else {
                x = rightX;
                y = startY + (i - 5) * lineGap;
            }

            if (current >= need) {
                gc.setFill(Color.GREEN);
            } else {
                gc.setFill(Color.BLACK);
            }

            gc.setFont(Font.font("Arial", 10));

            String shortName = getShortColorName(color);
            gc.fillText(shortName + ": " + current + "/" + need, x, y);

            if (hasHotel) {
                drawHotelIcon(gc, x + 45, y - 2);
            } else if (hasHouse) {
                drawHouseIcon(gc, x + 45, y - 2);
            }
        }
    }

    private String getShortColorName(PropertyColor color) {
        switch (color) {
            case DARK_BLUE:
                return "D.BLUE";
            case ORANGE:
                return "ORANGE";
            case BLACK:
                return "BLACK";
            case RED:
                return "RED";
            case DARK_GREEN:
                return "D.GREEN";
            case BROWN:
                return "BROWN";
            case PINK:
                return "PINK";
            case LIGHT_BLUE:
                return "L.BLUE";
            case LIGHT_GREEN:
                return "L.GREEN";
            case YELLOW:
                return "YELLOW";
            default:
                return color.name();
        }
    }

    private int getPropertyCountByCurrentColor(Player player, PropertyColor color) {
        int count = 0;

        for (PropertiesCards card : player.getPropertyCards()) {
            PropertyColor currentColor = card.getCurrentColor();

            if (currentColor != null && currentColor == color) {
                count++;
            }
        }

        return count;
    }

    private int getCompletedSetCount(Player player) {
        int completedSets = 0;

        for (PropertyColor color : PropertyColor.values()) {
            int current = 0;

            for (PropertiesCards card : player.getPropertyCards()) {
                PropertyColor currentColor = card.getCurrentColor();

                if (currentColor != null && currentColor == color) {
                    current++;
                }
            }

            if (current >= color.getAmountToCompleteSet()) {
                completedSets++;
            }
        }

        return completedSets;
    }

    public int getClickedPlayerViewButtonIndex(double mouseX, double mouseY) {
        double x = 760;
        double y = 90;
        double w = 100;
        double h = 35;
        double gap = 10;

        for (int i = 0; i < game.getPlayers().size(); i++) {
            double buttonY = y + i * (h + gap);

            if (mouseX >= x && mouseX <= x + w && mouseY >= buttonY && mouseY <= buttonY + h) {
                return i;
            }
        }

        return -1;
    }

    public void setViewedPlayerIndex(int viewedPlayerIndex) {
        this.viewedPlayerIndex = viewedPlayerIndex;
    }

    private void drawSmallCard(GraphicsContext gc, double x, double y, String type, String text, Color color) {
        gc.setFill(color);
        gc.fillRoundRect(x, y, 60, 85, 12, 12);

        gc.setStroke(Color.BLACK);
        gc.strokeRoundRect(x, y, 60, 85, 12, 12);

        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", 10));
        gc.fillText(type, x + 30, y + 15);
        drawWrappedText(gc, text, x + 5, y + 35, 50, 11);
    }

    private void drawButtons() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        drawButton(gc, 820, 520, 170, 40, "END TURN");
        drawButton(gc, 820, 570, 170, 40, "BACK MENU");
    }

    private void drawButton(GraphicsContext gc, double x, double y, double w, double h, String text) {
        gc.setFill(Color.ORANGE);
        gc.fillRoundRect(x, y, w, h, 12, 12);

        gc.setStroke(Color.BLACK);
        gc.strokeRoundRect(x, y, w, h, 12, 12);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, x + w / 2, y + h / 2);
        gc.setTextBaseline(VPos.TOP);
    }

    private void drawWinMessage() {
        if (!game.isWin()) {
            return;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);

        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("Arial", 42));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("Player " + (game.getCurrentPlayerIndex() + 1) + " Wins!", Game.SCREEN_WIDTH / 2, Game.SCREEN_HEIGHT / 2);
    }

    private void drawWrappedText(GraphicsContext gc, String text, double x, double y, double maxWidth, double lineHeight) {
        String[] parts = text.split("_");
        String line = "";
        double currentY = y;

        for (String part : parts) {
            String testLine = line.isEmpty() ? part : line + "_" + part;

            if (testLine.length() > 12) {
                gc.fillText(line, x + maxWidth / 2, currentY);
                line = part;
                currentY += lineHeight;
            } else {
                line = testLine;
            }
        }

        if (!line.isEmpty()) {
            gc.fillText(line, x + maxWidth / 2, currentY);
        }
    }

    public int getClickedHandCardIndex(double mouseX, double mouseY) {
        Player currentPlayer = game.getCurrentPlayer();
        ArrayList<Card> handCards = currentPlayer.getHandCards();

        double availableWidth = handAreaWidth;
        double currentGap = gap;

        if (handCards.size() > 1) {
            double totalWidth = handCards.size() * cardWidth + (handCards.size() - 1) * gap;

            if (totalWidth > availableWidth) {
                currentGap = (availableWidth - handCards.size() * cardWidth) / (handCards.size() - 1);
            }
        }

        for (int i = 0; i < handCards.size(); i++) {
            double x = handStartX + i * (cardWidth + currentGap);
            double y = handStartY;

            if (mouseX >= x && mouseX <= x + cardWidth
                    && mouseY >= y && mouseY <= y + cardHeight) {
                return i;
            }
        }

        return -1;
    }

    public boolean isEndTurnClicked(double mouseX, double mouseY) {
        return mouseX >= 820 && mouseX <= 990 && mouseY >= 520 && mouseY <= 560;
    }

    public boolean isBackMenuClicked(double mouseX, double mouseY) {
        return mouseX >= 820 && mouseX <= 990 && mouseY >= 570 && mouseY <= 610;
    }

    public PropertiesCards getSelectedWildCard() {
        return selectedWildCard;
    }

    public void setSelectedWildCard(PropertiesCards selectedWildCard) {
        this.selectedWildCard = selectedWildCard;
    }

    public void clear() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void startSlyDealSelection(ActionCards card) {
        pendingSlyDealCard = card;
        selectedWildCard = null;
    }

    public void cancelSlyDealSelection() {
        pendingSlyDealCard = null;
    }

    public boolean isSlyDealSelecting() {
        return pendingSlyDealCard != null;
    }

    public ActionCards getPendingSlyDealCard() {
        return pendingSlyDealCard;
    }

    public boolean isSlyDealCancelClicked(double mouseX, double mouseY) {
        return isSlyDealSelecting()
                && mouseX >= 720 && mouseX <= 860
                && mouseY >= 505 && mouseY <= 545;
    }

    public SlyDealChoice getClickedSlyDealChoice(double mouseX, double mouseY) {
        if (!isSlyDealSelecting()) {
            return null;
        }

        int displayIndex = 0;

        for (int playerIndex = 0; playerIndex < game.getPlayers().size(); playerIndex++) {
            if (playerIndex == game.getCurrentPlayerIndex()) {
                continue;
            }

            Player targetPlayer = game.getPlayers().get(playerIndex);

            for (PropertiesCards card : targetPlayer.getPropertyCards()) {
                if (!canBeStolenBySlyDeal(targetPlayer, card)) {
                    continue;
                }

                double x = slyPanelX + (displayIndex % 7) * (slyCardWidth + slyGap);
                double y = slyPanelY + (displayIndex / 7) * (slyCardHeight + 35);

                if (mouseX >= x && mouseX <= x + slyCardWidth
                        && mouseY >= y && mouseY <= y + slyCardHeight) {
                    return new SlyDealChoice(targetPlayer, card);
                }

                displayIndex++;
            }
        }

        return null;
    }

    private void drawSlyDealSelection() {
        if (!isSlyDealSelecting()) {
            return;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.rgb(0, 0, 0, 0.75));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 26));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("SLY DEAL: Choose one property to steal", Game.SCREEN_WIDTH / 2, 35);

        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Completed sets cannot be stolen. Wild cards can be stolen if they are not in a completed set.",
                Game.SCREEN_WIDTH / 2, 70);

        int displayIndex = 0;

        for (int playerIndex = 0; playerIndex < game.getPlayers().size(); playerIndex++) {
            if (playerIndex == game.getCurrentPlayerIndex()) {
                continue;
            }

            Player targetPlayer = game.getPlayers().get(playerIndex);

            for (PropertiesCards card : targetPlayer.getPropertyCards()) {
                if (!canBeStolenBySlyDeal(targetPlayer, card)) {
                    continue;
                }

                double x = slyPanelX + (displayIndex % 7) * (slyCardWidth + slyGap);
                double y = slyPanelY + (displayIndex / 7) * (slyCardHeight + 35);

                gc.setFill(Color.LIGHTBLUE);
                gc.fillRoundRect(x, y, slyCardWidth, slyCardHeight, 15, 15);

                gc.setStroke(Color.WHITE);
                gc.strokeRoundRect(x, y, slyCardWidth, slyCardHeight, 15, 15);

                gc.setFill(Color.BLACK);
                gc.setFont(Font.font("Arial", 12));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setTextBaseline(VPos.TOP);

                gc.fillText("Player " + (playerIndex + 1), x + slyCardWidth / 2, y + 10);
                gc.fillText(card.getValue() + "M", x + slyCardWidth / 2, y + 32);

                String colorText = card.getCurrentColor() == null ? "NO COLOR" : card.getCurrentColor().name();
                drawWrappedText(gc, colorText, x + 8, y + 55, slyCardWidth - 16, 12);

                if (card.isWildCard()) {
                    gc.setFill(Color.RED);
                    gc.setFont(Font.font("Arial", 11));
                    gc.fillText("WILD", x + slyCardWidth / 2, y + 100);
                }

                displayIndex++;
            }
        }

        if (displayIndex == 0) {
            gc.setFill(Color.LIGHTYELLOW);
            gc.setFont(Font.font("Arial", 22));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("No property can be stolen.", Game.SCREEN_WIDTH / 2, 280);
        }

        drawButton(gc, 720, 505, 140, 40, "CANCEL");
    }

    private boolean canBeStolenBySlyDeal(Player targetPlayer, PropertiesCards card) {
        PropertyColor color = card.getCurrentColor();

        if (color == null) {
            return true;
        }

        int count = 0;

        for (PropertiesCards propertyCard : targetPlayer.getPropertyCards()) {
            if (propertyCard.getCurrentColor() == color) {
                count++;
            }
        }

        return count < color.getAmountToCompleteSet();
    }

    public static class SlyDealChoice {
        private Player targetPlayer;
        private PropertiesCards selectedCard;

        public SlyDealChoice(Player targetPlayer, PropertiesCards selectedCard) {
            this.targetPlayer = targetPlayer;
            this.selectedCard = selectedCard;
        }

        public Player getTargetPlayer() {
            return targetPlayer;
        }

        public PropertiesCards getSelectedCard() {
            return selectedCard;
        }
    }

    public void startDebtCollectorSelection(ActionCards card) {
        pendingDebtCollectorCard = card;
        selectedWildCard = null;
    }

    public void cancelDebtCollectorSelection() {
        pendingDebtCollectorCard = null;
    }

    public boolean isDebtCollectorSelecting() {
        return pendingDebtCollectorCard != null;
    }

    public ActionCards getPendingDebtCollectorCard() {
        return pendingDebtCollectorCard;
    }

    public boolean isDebtCollectorCancelClicked(double mouseX, double mouseY) {
        return isDebtCollectorSelecting()
                && mouseX >= 720 && mouseX <= 860
                && mouseY >= 505 && mouseY <= 545;
    }

    public Player getClickedDebtCollectorTarget(double mouseX, double mouseY) {
        if (!isDebtCollectorSelecting()) {
            return null;
        }

        int displayIndex = 0;

        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (i == game.getCurrentPlayerIndex()) {
                continue;
            }

            double x = debtPanelX + displayIndex * (debtPlayerWidth + debtPlayerGap);
            double y = debtPanelY;

            if (mouseX >= x && mouseX <= x + debtPlayerWidth
                    && mouseY >= y && mouseY <= y + debtPlayerHeight) {
                return game.getPlayers().get(i);
            }

            displayIndex++;
        }

        return null;
    }

    private void drawDebtCollectorSelection() {
        if (!isDebtCollectorSelecting()) {
            return;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.rgb(0, 0, 0, 0.75));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 26));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("DEBT COLLECTOR: Choose one player to collect 5M", Game.SCREEN_WIDTH / 2, 45);

        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Click a player card to collect money. Click CANCEL if you do not want to use this card.",
                Game.SCREEN_WIDTH / 2, 80);

        int displayIndex = 0;

        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (i == game.getCurrentPlayerIndex()) {
                continue;
            }

            Player player = game.getPlayers().get(i);

            double x = debtPanelX + displayIndex * (debtPlayerWidth + debtPlayerGap);
            double y = debtPanelY;

            drawDebtCollectorPlayerBox(gc, player, i, x, y);

            displayIndex++;
        }

        drawButton(gc, 720, 505, 140, 40, "CANCEL");
    }

    private void drawDebtCollectorPlayerBox(GraphicsContext gc, Player player, int playerIndex, double x, double y) {
        gc.setFill(Color.LIGHTYELLOW);
        gc.fillRoundRect(x, y, debtPlayerWidth, debtPlayerHeight, 18, 18);

        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(x, y, debtPlayerWidth, debtPlayerHeight, 18, 18);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 20));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Player " + (playerIndex + 1), x + debtPlayerWidth / 2, y + 15);

        int bankTotal = 0;

        for (Card card : player.getBankCards()) {
            bankTotal += card.getValue();
        }

        gc.setFont(Font.font("Arial", 15));
        gc.fillText("Bank: " + bankTotal + "M", x + debtPlayerWidth / 2, y + 50);
        gc.fillText("Properties: " + player.getPropertyCards().size(), x + debtPlayerWidth / 2, y + 75);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Arial", 13));

        double textX = x + 15;
        double textY = y + 110;

        gc.fillText("Money Cards:", textX, textY);

        if (player.getBankCards().isEmpty()) {
            gc.fillText("None", textX, textY + 22);
        } else {
            String moneyText = "";

            for (int i = 0; i < player.getBankCards().size(); i++) {
                if (i >= 6) {
                    moneyText += "...";
                    break;
                }

                moneyText += player.getBankCards().get(i).getValue() + "M ";
            }

            gc.fillText(moneyText, textX, textY + 22);
        }

        gc.fillText("Property Colors:", textX, textY + 55);

        if (player.getPropertyCards().isEmpty()) {
            gc.fillText("None", textX, textY + 77);
        } else {
            String propertyText = "";

            for (int i = 0; i < player.getPropertyCards().size(); i++) {
                if (i >= 4) {
                    propertyText += "...";
                    break;
                }

                PropertiesCards card = player.getPropertyCards().get(i);

                if (card.getCurrentColor() == null) {
                    propertyText += "NO ";
                } else {
                    propertyText += getShortColorName(card.getCurrentColor()) + " ";
                }
            }

            gc.fillText(propertyText, textX, textY + 77);
        }
    }

    public void startTwoColorRentSelection(ActionCards card) {
        pendingTwoColorRentCard = card;
        useDoubleRentForTwoColorRent = false;
        selectedWildCard = null;
    }

    public boolean shouldUseDoubleRentForTwoColorRent() {
        return useDoubleRentForTwoColorRent;
    }

    public boolean isTwoColorDoubleRentClicked(double mouseX, double mouseY) {
        return isTwoColorRentSelecting()
                && game.hasDoubleTheRentCard(game.getCurrentPlayer())
                && game.getCurrentPlayer().getUseCardTimes() <= 1
                && mouseX >= 370 && mouseX <= 650
                && mouseY >= 410 && mouseY <= 445;
    }

    public void toggleTwoColorDoubleRent() {
        useDoubleRentForTwoColorRent = !useDoubleRentForTwoColorRent;
    }

    public void cancelTwoColorRentSelection() {
        pendingTwoColorRentCard = null;
    }

    public boolean isTwoColorRentSelecting() {
        return pendingTwoColorRentCard != null;
    }

    public ActionCards getPendingTwoColorRentCard() {
        return pendingTwoColorRentCard;
    }

    public boolean isTwoColorRentCancelClicked(double mouseX, double mouseY) {
        return isTwoColorRentSelecting()
                && mouseX >= 720 && mouseX <= 860
                && mouseY >= 505 && mouseY <= 545;
    }

    public PropertyColor getClickedTwoColorRentColor(double mouseX, double mouseY) {
        if (!isTwoColorRentSelecting()) {
            return null;
        }

        ArrayList<PropertyColor> colors = getTwoRentColors(pendingTwoColorRentCard.getActionCardType());

        for (int i = 0; i < colors.size(); i++) {
            PropertyColor color = colors.get(i);

            double x = rentPanelX + i * (rentButtonWidth + 40);
            double y = rentPanelY;

            if (mouseX >= x && mouseX <= x + rentButtonWidth
                    && mouseY >= y && mouseY <= y + rentButtonHeight) {

                if (currentPlayerHasColor(color)) {
                    return color;
                }

                return null;
            }
        }

        return null;
    }

    private void drawTwoColorRentSelection() {
        if (!isTwoColorRentSelecting()) {
            return;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.rgb(0, 0, 0, 0.75));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 26));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("RENT CARD: Choose one color to charge rent", Game.SCREEN_WIDTH / 2, 60);

        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Grey color means you do not have that property color, so it cannot be selected.",
                Game.SCREEN_WIDTH / 2, 100);

        ArrayList<PropertyColor> colors = getTwoRentColors(pendingTwoColorRentCard.getActionCardType());

        boolean hasAnyColor = false;

        for (int i = 0; i < colors.size(); i++) {
            PropertyColor color = colors.get(i);

            double x = rentPanelX + i * (rentButtonWidth + 40);
            double y = rentPanelY;

            boolean usable = currentPlayerHasColor(color);

            if (usable) {
                hasAnyColor = true;
                gc.setFill(Color.LIGHTGREEN);
            } else {
                gc.setFill(Color.GRAY);
            }

            gc.fillRoundRect(x, y, rentButtonWidth, rentButtonHeight, 14, 14);

            gc.setStroke(Color.WHITE);
            gc.strokeRoundRect(x, y, rentButtonWidth, rentButtonHeight, 14, 14);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 16));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(color.name(), x + rentButtonWidth / 2, y + rentButtonHeight / 2);

            if (!usable) {
                gc.setFill(Color.DARKRED);
                gc.setFont(Font.font("Arial", 12));
                gc.fillText("Unavailable", x + rentButtonWidth / 2, y + rentButtonHeight + 18);
            }
        }

        if (!hasAnyColor) {
            gc.setFill(Color.LIGHTYELLOW);
            gc.setFont(Font.font("Arial", 20));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.TOP);
            gc.fillText("You do not have either color. This card cannot be used now.",
                    Game.SCREEN_WIDTH / 2, 340);
        }

        drawDoubleRentOption(gc, 370, 410, useDoubleRentForTwoColorRent);

        drawButton(gc, 720, 505, 140, 40, "CANCEL");

        gc.setTextBaseline(VPos.TOP);
    }

    private void drawDoubleRentOption(GraphicsContext gc, double x, double y, boolean selected) {
        if (!game.hasDoubleTheRentCard(game.getCurrentPlayer())
                || game.getCurrentPlayer().getUseCardTimes() > 1) {
            return;
        }

        gc.setFill(Color.LIGHTYELLOW);
        gc.fillRoundRect(x, y, 280, 35, 10, 10);

        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(x, y, 280, 35, 10, 10);

        gc.setFill(Color.WHITE);
        gc.strokeRect(x + 12, y + 8, 18, 18);

        if (selected) {
            gc.setFill(Color.LIGHTGREEN);
            gc.fillText("✓", x + 21, y + 8);
        }

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 14));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("Use DOUBLE THE RENT  ×2", x + 42, y + 17.5);

        gc.setTextBaseline(VPos.TOP);
    }

    private ArrayList<PropertyColor> getTwoRentColors(ActionCardType type) {
        ArrayList<PropertyColor> colors = new ArrayList<>();

        switch (type) {
            case RENT_WITH_RED_AND_YELLOW:
                colors.add(PropertyColor.RED);
                colors.add(PropertyColor.YELLOW);
                break;

            case RENT_WITH_ORANGE_AND_PINK:
                colors.add(PropertyColor.ORANGE);
                colors.add(PropertyColor.PINK);
                break;

            case RENT_WITH_BROWN_AND_LIGHT_BLUE:
                colors.add(PropertyColor.BROWN);
                colors.add(PropertyColor.LIGHT_BLUE);
                break;

            case RENT_WITH_BLACK_AND_LIGHT_GREEN:
                colors.add(PropertyColor.BLACK);
                colors.add(PropertyColor.LIGHT_GREEN);
                break;

            case RENT_WITH_DARK_BLUE_AND_DARK_GREEN:
                colors.add(PropertyColor.DARK_BLUE);
                colors.add(PropertyColor.DARK_GREEN);
                break;

            default:
                break;
        }

        return colors;
    }

    private boolean currentPlayerHasColor(PropertyColor color) {
        Player currentPlayer = game.getCurrentPlayer();

        for (PropertiesCards card : currentPlayer.getPropertyCards()) {
            if (card.getCurrentColor() == color) {
                return true;
            }
        }

        return false;
    }

    public void startDealBreakerSelection(ActionCards card) {
        pendingDealBreakerCard = card;
        selectedWildCard = null;
    }

    public void cancelDealBreakerSelection() {
        pendingDealBreakerCard = null;
    }

    public boolean isDealBreakerSelecting() {
        return pendingDealBreakerCard != null;
    }

    public ActionCards getPendingDealBreakerCard() {
        return pendingDealBreakerCard;
    }

    public boolean isDealBreakerCancelClicked(double mouseX, double mouseY) {
        return isDealBreakerSelecting()
                && mouseX >= 720 && mouseX <= 860
                && mouseY >= 505 && mouseY <= 545;
    }

    public DealBreakerChoice getClickedDealBreakerChoice(double mouseX, double mouseY) {
        if (!isDealBreakerSelecting()) {
            return null;
        }

        int displayIndex = 0;

        for (int playerIndex = 0; playerIndex < game.getPlayers().size(); playerIndex++) {
            if (playerIndex == game.getCurrentPlayerIndex()) {
                continue;
            }

            Player targetPlayer = game.getPlayers().get(playerIndex);

            for (PropertyColor color : PropertyColor.values()) {
                ArrayList<PropertiesCards> completeSet = getCompleteSetByColor(targetPlayer, color);

                if (completeSet.isEmpty()) {
                    continue;
                }

                double x = dealBreakerPanelX + (displayIndex % 5) * (dealBreakerCardWidth + dealBreakerGap);
                double y = dealBreakerPanelY + (displayIndex / 5) * (dealBreakerCardHeight + 35);

                if (mouseX >= x && mouseX <= x + dealBreakerCardWidth
                        && mouseY >= y && mouseY <= y + dealBreakerCardHeight) {
                    return new DealBreakerChoice(targetPlayer, completeSet);
                }

                displayIndex++;
            }
        }

        return null;
    }

    private void drawDealBreakerSelection() {
        if (!isDealBreakerSelecting()) {
            return;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.rgb(0, 0, 0, 0.75));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 26));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("DEAL BREAKER: Choose one completed set to steal", Game.SCREEN_WIDTH / 2, 35);

        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Only completed property sets can be stolen.", Game.SCREEN_WIDTH / 2, 70);

        int displayIndex = 0;

        for (int playerIndex = 0; playerIndex < game.getPlayers().size(); playerIndex++) {
            if (playerIndex == game.getCurrentPlayerIndex()) {
                continue;
            }

            Player targetPlayer = game.getPlayers().get(playerIndex);

            for (PropertyColor color : PropertyColor.values()) {
                ArrayList<PropertiesCards> completeSet = getCompleteSetByColor(targetPlayer, color);

                if (completeSet.isEmpty()) {
                    continue;
                }

                double x = dealBreakerPanelX + (displayIndex % 5) * (dealBreakerCardWidth + dealBreakerGap);
                double y = dealBreakerPanelY + (displayIndex / 5) * (dealBreakerCardHeight + 35);

                gc.setFill(Color.LIGHTGREEN);
                gc.fillRoundRect(x, y, dealBreakerCardWidth, dealBreakerCardHeight, 15, 15);

                gc.setStroke(Color.WHITE);
                gc.strokeRoundRect(x, y, dealBreakerCardWidth, dealBreakerCardHeight, 15, 15);

                gc.setFill(Color.BLACK);
                gc.setFont(Font.font("Arial", 13));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setTextBaseline(VPos.TOP);

                gc.fillText("Player " + (playerIndex + 1), x + dealBreakerCardWidth / 2, y + 10);
                gc.fillText(color.name(), x + dealBreakerCardWidth / 2, y + 35);
                gc.fillText(completeSet.size() + "/" + color.getAmountToCompleteSet() + " Completed",
                        x + dealBreakerCardWidth / 2, y + 60);

                gc.setFont(Font.font("Arial", 11));
                gc.fillText("Click to steal set", x + dealBreakerCardWidth / 2, y + 90);

                displayIndex++;
            }
        }

        if (displayIndex == 0) {
            gc.setFill(Color.LIGHTYELLOW);
            gc.setFont(Font.font("Arial", 22));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("No player has a completed property set.", Game.SCREEN_WIDTH / 2, 260);
            gc.fillText("This Deal Breaker card cannot be used now.", Game.SCREEN_WIDTH / 2, 295);
        }

        drawButton(gc, 720, 505, 140, 40, "CANCEL");
    }

    private ArrayList<PropertiesCards> getCompleteSetByColor(Player player, PropertyColor color) {
        ArrayList<PropertiesCards> result = new ArrayList<>();

        for (PropertiesCards card : player.getPropertyCards()) {
            if (card.getCurrentColor() == color) {
                result.add(card);
            }
        }

        if (result.size() >= color.getAmountToCompleteSet()) {
            return result;
        }

        return new ArrayList<>();
    }

    public static class DealBreakerChoice {
        private Player targetPlayer;
        private ArrayList<PropertiesCards> selectedSet;

        public DealBreakerChoice(Player targetPlayer, ArrayList<PropertiesCards> selectedSet) {
            this.targetPlayer = targetPlayer;
            this.selectedSet = selectedSet;
        }

        public Player getTargetPlayer() {
            return targetPlayer;
        }

        public ArrayList<PropertiesCards> getSelectedSet() {
            return selectedSet;
        }
    }

    private void drawPaymentSelection() {
        if (!game.isPaymentSelecting()) {
            return;
        }

        Game.PaymentRequest request = game.getCurrentPaymentRequest();
        Player payer = request.getPayer();
        Player receiver = request.getReceiver();

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.rgb(0, 0, 0, 0.78));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);

        int payerIndex = game.getPlayers().indexOf(payer) + 1;
        int receiverIndex = game.getPlayers().indexOf(receiver) + 1;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 26));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("PAYMENT REQUIRED", Game.SCREEN_WIDTH / 2, 25);

        gc.setFont(Font.font("Arial", 18));
        gc.setFill(Color.LIGHTYELLOW);
        gc.fillText("Now Player " + payerIndex + " must pay Player " + receiverIndex,
                Game.SCREEN_WIDTH / 2, 60);

        int requiredAmount = Math.min(request.getAmount(), game.getTotalAssetsValue(payer));
        int selectedTotal = game.getCardsValue(selectedPaymentCards);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Required: " + requiredAmount + "M    Selected: " + selectedTotal + "M",
                Game.SCREEN_WIDTH / 2, 90);

        if (game.getTotalAssetsValue(payer) < request.getAmount()) {
            gc.setFill(Color.LIGHTYELLOW);
            gc.fillText("Not enough assets. Player " + payerIndex + " must pay all available assets.",
                    Game.SCREEN_WIDTH / 2, 115);
        }

        drawPaymentBankCards(gc, payer);
        drawPaymentPropertyCards(gc, payer);
        drawPaymentReceiverPreview(gc, receiver);

        if (selectedTotal >= requiredAmount) {
            drawButton(gc, 330, 555, 160, 40, "CONFIRM PAY");
        } else {
            drawDisabledButton(gc, 330, 555, 160, 40, "CONFIRM PAY");
        }

        drawButton(gc, 510, 555, 120, 40, "CLEAR");

        if (game.canCurrentPaymentUseJustSayNo()) {
            drawButton(gc, 650, 555, 220, 40, "USE JUST SAY NO");
        }
    }

    private void drawPaymentBankCards(GraphicsContext gc, Player payer) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 19));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Bank Cards", 60, 145);

        for (int i = 0; i < payer.getBankCards().size(); i++) {
            Card card = payer.getBankCards().get(i);

            double x = 60 + (i % 7) * 90;
            double y = 180 + (i / 7) * 105;

            drawPaymentCard(gc, card, x, y, "Money", card.getValue() + "M", Color.GOLD);
        }
    }

    private void drawPaymentPropertyCards(GraphicsContext gc, Player payer) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 19));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Property Cards", 60, 330);

        for (int i = 0; i < payer.getPropertyCards().size(); i++) {
            PropertiesCards card = payer.getPropertyCards().get(i);

            double x = 60 + (i % 7) * 90;
            double y = 365 + (i / 7) * 105;

            String text = card.getCurrentColor() == null ? "NO COLOR" : card.getCurrentColor().name();
            drawPaymentCard(gc, card, x, y, "Property", text, Color.LIGHTBLUE);
        }
    }

    private void drawPaymentReceiverPreview(GraphicsContext gc, Player receiver) {
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

        double startY = boxY + 100;
        double lineGap = 24;

        int row = 0;

        for (PropertyColor color : PropertyColor.values()) {
            int originalCount = getPropertyCountByColor(receiver, color);
            int addedCount = getSelectedPaymentPropertyCountByColor(color);
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

            String text = getShortColorName(color)
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

    private int getPropertyCountByColor(Player player, PropertyColor color) {
        int count = 0;

        for (PropertiesCards card : player.getPropertyCards()) {
            if (card.getCurrentColor() == color) {
                count++;
            }
        }

        return count;
    }

    private int getSelectedPaymentPropertyCountByColor(PropertyColor color) {
        int count = 0;

        for (Card card : selectedPaymentCards) {
            if (card instanceof PropertiesCards propertyCard) {
                if (propertyCard.getCurrentColor() == color) {
                    count++;
                }
            }
        }

        return count;
    }

    private void drawPaymentCard(GraphicsContext gc, Card card, double x, double y, String type, String text, Color color) {
        if (selectedPaymentCards.contains(card)) {
            gc.setFill(Color.YELLOW);
            gc.fillRoundRect(x - 5, y - 5, 78, 103, 14, 14);
        }

        gc.setFill(color);
        gc.fillRoundRect(x, y, 68, 93, 12, 12);

        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(x, y, 68, 93, 12, 12);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 11));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);

        gc.fillText(type, x + 34, y + 10);
        gc.fillText(card.getValue() + "M", x + 34, y + 30);

        gc.setFont(Font.font("Arial", 9));
        drawWrappedText(gc, text, x + 5, y + 52, 58, 11);
    }

    private void drawDisabledButton(GraphicsContext gc, double x, double y, double w, double h, String text) {
        gc.setFill(Color.GRAY);
        gc.fillRoundRect(x, y, w, h, 12, 12);

        gc.setStroke(Color.BLACK);
        gc.strokeRoundRect(x, y, w, h, 12, 12);

        gc.setFill(Color.DARKGRAY);
        gc.setFont(Font.font("Arial", 15));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, x + w / 2, y + h / 2);
        gc.setTextBaseline(VPos.TOP);
    }

    public boolean isPaymentConfirmClicked(double mouseX, double mouseY) {
        if (!game.isPaymentSelecting()) {
            return false;
        }

        return mouseX >= 330 && mouseX <= 490 && mouseY >= 555 && mouseY <= 595;
    }

    public boolean isPaymentClearClicked(double mouseX, double mouseY) {
        if (!game.isPaymentSelecting()) {
            return false;
        }

        return mouseX >= 510 && mouseX <= 630 && mouseY >= 555 && mouseY <= 595;
    }

    public boolean isPaymentJustSayNoClicked(double mouseX, double mouseY) {
        if (!game.isPaymentSelecting() || !game.canCurrentPaymentUseJustSayNo()) {
            return false;
        }

        return mouseX >= 650 && mouseX <= 870 && mouseY >= 555 && mouseY <= 595;
    }

    public void clearPaymentSelection() {
        selectedPaymentCards.clear();
    }

    public ArrayList<Card> getSelectedPaymentCards() {
        return new ArrayList<>(selectedPaymentCards);
    }

    public boolean canConfirmPayment() {
        if (!game.isPaymentSelecting()) {
            return false;
        }

        Game.PaymentRequest request = game.getCurrentPaymentRequest();

        int requiredAmount = Math.min(request.getAmount(), game.getTotalAssetsValue(request.getPayer()));
        int selectedTotal = game.getCardsValue(selectedPaymentCards);

        return selectedTotal >= requiredAmount;
    }

    public boolean handlePaymentCardClick(double mouseX, double mouseY) {
        if (!game.isPaymentSelecting()) {
            return false;
        }

        Game.PaymentRequest request = game.getCurrentPaymentRequest();
        Player payer = request.getPayer();

        Card clickedCard = getClickedPaymentBankCard(mouseX, mouseY, payer);

        if (clickedCard == null) {
            clickedCard = getClickedPaymentPropertyCard(mouseX, mouseY, payer);
        }

        if (clickedCard == null) {
            return false;
        }

        if (selectedPaymentCards.contains(clickedCard)) {
            selectedPaymentCards.remove(clickedCard);
        } else {
            selectedPaymentCards.add(clickedCard);
        }

        return true;
    }

    private Card getClickedPaymentBankCard(double mouseX, double mouseY, Player payer) {
        for (int i = 0; i < payer.getBankCards().size(); i++) {
            double x = 60 + (i % 7) * 90;
            double y = 180 + (i / 7) * 105;

            if (mouseX >= x && mouseX <= x + 68 && mouseY >= y && mouseY <= y + 93) {
                return payer.getBankCards().get(i);
            }
        }

        return null;
    }

    private Card getClickedPaymentPropertyCard(double mouseX, double mouseY, Player payer) {
        for (int i = 0; i < payer.getPropertyCards().size(); i++) {
            double x = 60 + (i % 7) * 90;
            double y = 365 + (i / 7) * 105;

            if (mouseX >= x && mouseX <= x + 68 && mouseY >= y && mouseY <= y + 93) {
                return payer.getPropertyCards().get(i);
            }
        }

        return null;
    }

    public void startMultipleColorRentSelection(ActionCards card) {
        pendingMultipleColorRentCard = card;
        selectedMultipleColorRentTarget = null;
        selectedMultipleColorRentColor = null;
        useDoubleRentForMultipleColorRent = false;
        selectedWildCard = null;
    }

    public boolean shouldUseDoubleRentForMultipleColorRent() {
        return useDoubleRentForMultipleColorRent;
    }

    public boolean isMultipleColorDoubleRentClicked(double mouseX, double mouseY) {
        return isMultipleColorRentSelecting()
                && game.hasDoubleTheRentCard(game.getCurrentPlayer())
                && game.getCurrentPlayer().getUseCardTimes() <= 1
                && mouseX >= 370 && mouseX <= 650
                && mouseY >= 450 && mouseY <= 485;
    }

    public void toggleMultipleColorDoubleRent() {
        useDoubleRentForMultipleColorRent = !useDoubleRentForMultipleColorRent;
    }

    public void cancelMultipleColorRentSelection() {
        pendingMultipleColorRentCard = null;
        selectedMultipleColorRentTarget = null;
        selectedMultipleColorRentColor = null;
    }

    public boolean isMultipleColorRentSelecting() {
        return pendingMultipleColorRentCard != null;
    }

    public ActionCards getPendingMultipleColorRentCard() {
        return pendingMultipleColorRentCard;
    }

    public Player getSelectedMultipleColorRentTarget() {
        return selectedMultipleColorRentTarget;
    }

    public void setSelectedMultipleColorRentTarget(Player selectedMultipleColorRentTarget) {
        this.selectedMultipleColorRentTarget = selectedMultipleColorRentTarget;
    }

    public PropertyColor getSelectedMultipleColorRentColor() {
        return selectedMultipleColorRentColor;
    }

    public void setSelectedMultipleColorRentColor(PropertyColor selectedMultipleColorRentColor) {
        this.selectedMultipleColorRentColor = selectedMultipleColorRentColor;
    }

    public boolean canConfirmMultipleColorRent() {
        return selectedMultipleColorRentTarget != null && selectedMultipleColorRentColor != null;
    }

    public boolean isMultipleColorRentCancelClicked(double mouseX, double mouseY) {
        return isMultipleColorRentSelecting()
                && mouseX >= 690 && mouseX <= 830
                && mouseY >= 535 && mouseY <= 575;
    }

    public boolean isMultipleColorRentConfirmClicked(double mouseX, double mouseY) {
        return isMultipleColorRentSelecting()
                && mouseX >= 500 && mouseX <= 660
                && mouseY >= 535 && mouseY <= 575;
    }

    public Player getClickedMultipleColorRentTarget(double mouseX, double mouseY) {
        if (!isMultipleColorRentSelecting()) {
            return null;
        }

        double x = multiRentPanelX;
        double y = multiRentPanelY + 80;
        double gap = 16;

        int displayIndex = 0;

        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (i == game.getCurrentPlayerIndex()) {
                continue;
            }

            double buttonY = y + displayIndex * (multiRentButtonHeight + gap);

            if (mouseX >= x && mouseX <= x + multiRentButtonWidth
                    && mouseY >= buttonY && mouseY <= buttonY + multiRentButtonHeight) {
                return game.getPlayers().get(i);
            }

            displayIndex++;
        }

        return null;
    }

    public PropertyColor getClickedMultipleColorRentColor(double mouseX, double mouseY) {
        if (!isMultipleColorRentSelecting()) {
            return null;
        }

        Player currentPlayer = game.getCurrentPlayer();

        double x = multiRentPanelX + 300;
        double y = multiRentPanelY + 80;
        double gapX = 12;
        double gapY = 12;
        int buttonsPerRow = 2;

        int displayIndex = 0;

        for (PropertyColor color : PropertyColor.values()) {
            if (!currentPlayer.canUseRentColor(color)) {
                continue;
            }

            int row = displayIndex / buttonsPerRow;
            int col = displayIndex % buttonsPerRow;

            double buttonX = x + col * (multiRentButtonWidth + gapX);
            double buttonY = y + row * (multiRentButtonHeight + gapY);

            if (mouseX >= buttonX && mouseX <= buttonX + multiRentButtonWidth
                    && mouseY >= buttonY && mouseY <= buttonY + multiRentButtonHeight) {
                return color;
            }

            displayIndex++;
        }

        return null;
    }

    private void drawMultipleColorRentSelection() {
        if (!isMultipleColorRentSelecting()) {
            return;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.rgb(0, 0, 0, 0.76));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 26));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("MULTI-COLOR RENT: Choose target and color", Game.SCREEN_WIDTH / 2, 35);

        gc.setFont(Font.font("Arial", 16));
        gc.fillText("This card charges rent from one selected player only.",
                Game.SCREEN_WIDTH / 2, 70);

        drawMultipleColorRentTargets(gc);
        drawMultipleColorRentColors(gc);
        drawMultipleColorRentStatus(gc);

        drawDoubleRentOption(gc, 370, 450, useDoubleRentForMultipleColorRent);

        if (canConfirmMultipleColorRent()) {
            drawButton(gc, 500, 535, 160, 40, "CONFIRM");
        } else {
            drawDisabledButton(gc, 500, 535, 160, 40, "CONFIRM");
        }

        drawButton(gc, 690, 535, 140, 40, "CANCEL");
    }

    private void drawMultipleColorRentTargets(GraphicsContext gc) {
        double x = multiRentPanelX;
        double y = multiRentPanelY + 80;
        double gap = 16;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 19));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Choose Target Player", x, multiRentPanelY + 35);

        int displayIndex = 0;

        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (i == game.getCurrentPlayerIndex()) {
                continue;
            }

            Player player = game.getPlayers().get(i);
            double buttonY = y + displayIndex * (multiRentButtonHeight + gap);

            if (player == selectedMultipleColorRentTarget) {
                gc.setFill(Color.LIGHTGREEN);
            } else {
                gc.setFill(Color.LIGHTGRAY);
            }

            gc.fillRoundRect(x, buttonY, multiRentButtonWidth, multiRentButtonHeight, 10, 10);
            gc.setStroke(Color.BLACK);
            gc.strokeRoundRect(x, buttonY, multiRentButtonWidth, multiRentButtonHeight, 10, 10);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 15));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText("Player " + (i + 1), x + multiRentButtonWidth / 2, buttonY + multiRentButtonHeight / 2);

            displayIndex++;
        }

        gc.setTextBaseline(VPos.TOP);
    }

    private void drawMultipleColorRentColors(GraphicsContext gc) {
        Player currentPlayer = game.getCurrentPlayer();

        double x = multiRentPanelX + 300;
        double y = multiRentPanelY + 80;
        double gapX = 12;
        double gapY = 12;
        int buttonsPerRow = 2;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 19));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Choose Rent Color", x, multiRentPanelY + 35);

        int displayIndex = 0;

        for (PropertyColor color : PropertyColor.values()) {
            if (!currentPlayer.canUseRentColor(color)) {
                continue;
            }

            int row = displayIndex / buttonsPerRow;
            int col = displayIndex % buttonsPerRow;

            double buttonX = x + col * (multiRentButtonWidth + gapX);
            double buttonY = y + row * (multiRentButtonHeight + gapY);

            if (color == selectedMultipleColorRentColor) {
                gc.setFill(Color.LIGHTGREEN);
            } else {
                gc.setFill(Color.LIGHTYELLOW);
            }

            gc.fillRoundRect(buttonX, buttonY, multiRentButtonWidth, multiRentButtonHeight, 10, 10);
            gc.setStroke(Color.BLACK);
            gc.strokeRoundRect(buttonX, buttonY, multiRentButtonWidth, multiRentButtonHeight, 10, 10);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 13));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(color.name(), buttonX + multiRentButtonWidth / 2, buttonY + multiRentButtonHeight / 2);

            displayIndex++;
        }

        if (displayIndex == 0) {
            gc.setFill(Color.LIGHTYELLOW);
            gc.setFont(Font.font("Arial", 17));
            gc.setTextAlign(TextAlignment.LEFT);
            gc.setTextBaseline(VPos.TOP);
            gc.fillText("You have no property color to charge rent.", x, y);
        }

        gc.setTextBaseline(VPos.TOP);
    }

    private void drawMultipleColorRentStatus(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);

        String targetText = selectedMultipleColorRentTarget == null
                ? "Target: not selected"
                : "Target: Player " + (game.getPlayers().indexOf(selectedMultipleColorRentTarget) + 1);

        String colorText = selectedMultipleColorRentColor == null
                ? "Color: not selected"
                : "Color: " + selectedMultipleColorRentColor.name();

        gc.fillText(targetText + "     " + colorText, Game.SCREEN_WIDTH / 2, 485);

        if (selectedMultipleColorRentColor != null) {
            int rent = calculatePreviewRent(game.getCurrentPlayer(), selectedMultipleColorRentColor);
            gc.setFill(Color.LIGHTGREEN);
            gc.fillText("Rent Amount: " + rent + "M", Game.SCREEN_WIDTH / 2, 510);
        }
    }

    private int calculatePreviewRent(Player player, PropertyColor color) {
        int propertyCount = 0;
        int rent = 0;

        for (PropertiesCards card : player.getPropertyCards()) {
            if (card.getCurrentColor() == color) {
                propertyCount++;

                if (card.hasHouse()) {
                    rent += 3;
                }

                if (card.hasHotel()) {
                    rent += 4;
                }
            }
        }

        return rent + propertyCount;
    }
}
