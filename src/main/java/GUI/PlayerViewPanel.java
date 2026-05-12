package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;
import model.Card;
import model.Player;
import model.PropertyColor;

public class PlayerViewPanel {
    private static final double BUTTON_X = 760;
    private static final double BUTTON_Y = 55;
    private static final double BUTTON_WIDTH = 100;
    private static final double BUTTON_HEIGHT = 35;
    private static final double BUTTON_GAP = 10;

    private static final double BOX_X = 865;
    private static final double BOX_Y = 45;
    private static final double BOX_WIDTH = 160;
    private static final double BOX_HEIGHT = 470;

    public static void drawPlayerViewButtons(GraphicsContext gc, Game game, int viewedPlayerIndex) {
        for (int i = 0; i < game.getPlayers().size(); i++) {
            drawPlayerViewButton(gc, i, i == viewedPlayerIndex);
        }

        gc.setTextBaseline(VPos.TOP);
    }

    public static void drawViewedPlayerInfo(GraphicsContext gc, Game game, int viewedPlayerIndex) {
        if (viewedPlayerIndex < 0 || viewedPlayerIndex >= game.getPlayers().size()) {
            return;
        }

        Player viewedPlayer = game.getPlayers().get(viewedPlayerIndex);

        drawInfoBox(gc);
        drawBasicPlayerInfo(gc, viewedPlayer, viewedPlayerIndex);
        drawMoneyPreview(gc, viewedPlayer);
        drawPropertySetPreview(gc, viewedPlayer);
    }

    public static int getClickedPlayerViewButtonIndex(Game game, double mouseX, double mouseY) {
        for (int i = 0; i < game.getPlayers().size(); i++) {
            double buttonY = BUTTON_Y + i * (BUTTON_HEIGHT + BUTTON_GAP);

            if (mouseX >= BUTTON_X && mouseX <= BUTTON_X + BUTTON_WIDTH
                    && mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT) {
                return i;
            }
        }

        return -1;
    }

    private static void drawPlayerViewButton(GraphicsContext gc, int playerIndex, boolean selected) {
        double y = BUTTON_Y + playerIndex * (BUTTON_HEIGHT + BUTTON_GAP);

        if (selected) {
            gc.setFill(ScreenDrawHelper.ACCENT);
        } else {
            gc.setFill(Color.rgb(42, 54, 78, 0.92));
        }

        gc.fillRoundRect(BUTTON_X, y, BUTTON_WIDTH, BUTTON_HEIGHT, 12, 12);
        gc.setStroke(Color.rgb(255, 255, 255, selected ? 0.45 : 0.18));
        gc.strokeRoundRect(BUTTON_X, y, BUTTON_WIDTH, BUTTON_HEIGHT, 12, 12);

        gc.setFill(selected ? Color.rgb(34, 26, 10) : ScreenDrawHelper.TEXT);
        gc.setFont(Font.font("Arial", 14));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("PLAYER " + (playerIndex + 1), BUTTON_X + BUTTON_WIDTH / 2, y + BUTTON_HEIGHT / 2);
    }

    private static void drawInfoBox(GraphicsContext gc) {
        ScreenDrawHelper.drawLightPanel(gc, BOX_X, BOX_Y, BOX_WIDTH, BOX_HEIGHT);
    }

    private static void drawBasicPlayerInfo(GraphicsContext gc, Player viewedPlayer, int viewedPlayerIndex) {
        double textX = BOX_X + 10;
        double textY = BOX_Y + 8;

        gc.setFill(Color.rgb(30, 35, 48));
        gc.setFont(Font.font("Arial", 15));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);

        gc.fillText("Viewing: P" + (viewedPlayerIndex + 1), textX, textY);
        gc.fillText("Hand: " + viewedPlayer.getHandCards().size(), textX, textY + 25);

        gc.setFill(Color.rgb(36, 150, 92));
        gc.fillText("Sets: " + PlayerInfoHelper.getCompletedSetCount(viewedPlayer) + "/3", textX, textY + 50);

        gc.setFill(Color.rgb(30, 35, 48));
        gc.fillText("Bank: " + PlayerInfoHelper.getBankTotal(viewedPlayer) + "M", textX, textY + 75);
    }

    private static void drawMoneyPreview(GraphicsContext gc, Player viewedPlayer) {
        double textX = BOX_X + 10;
        double textY = BOX_Y + 10;
        double moneyY = textY + 128;

        gc.setFill(Color.rgb(30, 35, 48));
        gc.setFont(Font.font("Arial", 13));
        gc.fillText("Money Cards:", textX, textY + 105);

        if (viewedPlayer.getBankCards().isEmpty()) {
            gc.setFill(Color.GRAY);
            gc.fillText("None", textX, moneyY);
            return;
        }

        String moneyText = buildMoneyText(viewedPlayer);
        gc.setFill(Color.rgb(92, 73, 22));
        gc.fillText(moneyText, textX, moneyY);

        if (viewedPlayer.getBankCards().size() > 6) {
            gc.fillText("...", textX, moneyY + 18);
        }
    }

    private static String buildMoneyText(Player viewedPlayer) {
        String moneyText = "";
        int moneyCount = 0;

        for (Card card : viewedPlayer.getBankCards()) {
            moneyText += card.getValue() + "M ";
            moneyCount++;

            if (moneyCount >= 6) {
                break;
            }
        }

        return moneyText;
    }

    private static void drawPropertySetPreview(GraphicsContext gc, Player viewedPlayer) {
        gc.setFont(Font.font("Arial", 14));
        gc.setFill(Color.rgb(30, 35, 48));
        gc.fillText("Property Sets:", BOX_X + 10, BOX_Y + 190);

        double leftX = BOX_X + 10;
        double rightX = BOX_X + 85;
        double startY = BOX_Y + 220;
        double lineGap = 34;

        PropertyColor[] colors = PropertyColor.values();

        for (int i = 0; i < colors.length; i++) {
            PropertyColor color = colors[i];
            double x = i < 5 ? leftX : rightX;
            double y = i < 5 ? startY + i * lineGap : startY + (i - 5) * lineGap;

            drawPropertySetLine(gc, viewedPlayer, color, x, y);
        }
    }

    private static void drawPropertySetLine(GraphicsContext gc,
                                            Player viewedPlayer,
                                            PropertyColor color,
                                            double x,
                                            double y) {
        int current = PlayerInfoHelper.getPropertyCountByCurrentColor(viewedPlayer, color);
        int need = color.getAmountToCompleteSet();

        if (current >= need) {
            gc.setFill(Color.rgb(36, 150, 92));
        } else {
            gc.setFill(Color.rgb(50, 56, 70));
        }

        gc.setFont(Font.font("Arial", 10));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(PlayerInfoHelper.getShortColorName(color) + ": " + current + "/" + need, x, y);

        if (PlayerInfoHelper.hasHotel(viewedPlayer, color)) {
            gc.setFill(Color.rgb(30, 70, 150));
            gc.setFont(Font.font("Arial", 9));
            gc.fillText("HOTEL", x, y + 13);
        } else if (PlayerInfoHelper.hasHouse(viewedPlayer, color)) {
            gc.setFill(Color.rgb(36, 120, 70));
            gc.setFont(Font.font("Arial", 9));
            gc.fillText("HOUSE", x, y + 13);
        }
    }
}
