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

    private PropertiesCards selectedWildCard = null;
    private double propertyStartX = 20;
    private double propertyStartY = 285;
    private double smallCardWidth = 60;
    private double smallCardHeight = 85;
    private double propertyGap = 75;
    private double cardWidth = 90;
    private double cardHeight = 125;
    private double gap = 12;
    private double handStartX = 35;
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
        double w = 110;
        double h = 28;
        double gap = 6;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 16));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Choose Wild Color:", x, y - 25);

        for (int i = 0; i < selectedWildCard.getType().getColors().size(); i++) {
            PropertyColor color = selectedWildCard.getType().getColors().get(i);

            double buttonY = y + i * (h + gap);

            gc.setFill(Color.LIGHTYELLOW);
            gc.fillRoundRect(x, buttonY, w, h, 8, 8);

            gc.setStroke(Color.BLACK);
            gc.strokeRoundRect(x, buttonY, w, h, 8, 8);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(color.name(), x + w / 2, buttonY + h / 2);
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
        double w = 110;
        double h = 28;
        double gap = 6;

        for (int i = 0; i < selectedWildCard.getType().getColors().size(); i++) {
            double buttonY = y + i * (h + gap);

            if (mouseX >= x && mouseX <= x + w
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

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 18));
        gc.fillText("Hand Cards", 20, Game.SCREEN_HEIGHT - 180);

        for (int i = 0; i < handCards.size(); i++) {
            double x = handStartX + i * (cardWidth + gap);
            double y = handStartY;

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

        for (int i = 0; i < currentPlayer.getHandCards().size(); i++) {
            double x = handStartX + i * (cardWidth + gap);
            double y = handStartY;

            if (mouseX >= x && mouseX <= x + cardWidth && mouseY >= y && mouseY <= y + cardHeight) {
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
}