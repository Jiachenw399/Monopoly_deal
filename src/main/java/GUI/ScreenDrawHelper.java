package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import model.PropertyColor;

public class ScreenDrawHelper {
    public static final Color BACKGROUND = Color.rgb(18, 24, 38);
    public static final Color PANEL = Color.rgb(34, 45, 67, 0.88);
    public static final Color PANEL_LIGHT = Color.rgb(245, 247, 250, 0.93);
    public static final Color BORDER = Color.rgb(255, 255, 255, 0.18);
    public static final Color TEXT = Color.rgb(245, 248, 255);
    public static final Color MUTED_TEXT = Color.rgb(185, 196, 215);
    public static final Color ACCENT = Color.rgb(255, 184, 77);
    public static final Color ACCENT_DARK = Color.rgb(231, 139, 48);
    public static final Color DANGER = Color.rgb(255, 112, 112);
    private static final long BUTTON_PRESSED_MS = 180;
    private static String pressedButtonKey;
    private static long pressedUntilMillis;

    // Draws page background.
    public static void drawPageBackground(GraphicsContext gc, double width, double height) {
        gc.clearRect(0, 0, width, height);
        gc.setFill(BACKGROUND);
        gc.fillRect(0, 0, width, height);

        gc.setFill(Color.rgb(40, 66, 104, 0.55));
        gc.fillOval(-140, -170, 430, 310);

        gc.setFill(Color.rgb(88, 55, 118, 0.35));
        gc.fillOval(width - 260, -120, 410, 280);

        gc.setFill(Color.rgb(255, 184, 77, 0.08));
        gc.fillOval(width - 220, height - 170, 330, 230);
    }

    // Draws overlay.
    public static void drawOverlay(GraphicsContext gc) {
        gc.setFill(Color.rgb(5, 8, 14, 0.78));
        gc.fillRect(0, 0, 1035, 625);
    }

    // Draws panel.
    public static void drawPanel(GraphicsContext gc,
                                 double x,
                                 double y,
                                 double width,
                                 double height) {
        gc.setFill(PANEL);
        gc.fillRoundRect(x, y, width, height, 20, 20);
        gc.setStroke(BORDER);
        gc.strokeRoundRect(x, y, width, height, 20, 20);
    }

    // Draws section title.
    public static void drawSectionTitle(GraphicsContext gc, String title, double x, double y) {
        gc.setFill(TEXT);
        gc.setFont(Font.font("Arial", 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(title, x, y);
    }

    // Draws badge.
    public static void drawBadge(GraphicsContext gc,
                                 double x,
                                 double y,
                                 double width,
                                 double height,
                                 String text,
                                 Color fillColor) {
        gc.setFill(fillColor);
        gc.fillRoundRect(x, y, width, height, 12, 12);
        gc.setFill(Color.rgb(20, 24, 34));
        gc.setFont(Font.font("Arial", 13));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, x + width / 2, y + height / 2);
        gc.setTextBaseline(VPos.TOP);
    }

    // Draws button.
    public static void drawButton(GraphicsContext gc,
                                  double x,
                                  double y,
                                  double width,
                                  double height,
                                  String text) {
        if (isButtonPressed(x, y, width, height)) {
            drawPressedButton(gc, x, y, width, height, text);
            return;
        }

        gc.setFill(ACCENT_DARK);
        gc.fillRoundRect(x + 3, y + 4, width, height, 14, 14);

        gc.setFill(ACCENT);
        gc.fillRoundRect(x, y, width, height, 14, 14);

        gc.setStroke(Color.rgb(255, 255, 255, 0.35));
        gc.strokeRoundRect(x, y, width, height, 14, 14);

        gc.setFill(Color.rgb(34, 26, 10));
        gc.setFont(Font.font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, x + width / 2, y + height / 2);
        gc.setTextBaseline(VPos.TOP);
    }

    // Draws a button in pressed (light gray) state.
    public static void drawPressedButton(GraphicsContext gc,
                                         double x,
                                         double y,
                                         double width,
                                         double height,
                                         String text) {
        gc.setFill(Color.rgb(165, 170, 180));
        gc.fillRoundRect(x + 2, y + 3, width, height, 14, 14);

        gc.setFill(Color.rgb(205, 210, 220));
        gc.fillRoundRect(x, y, width, height, 14, 14);

        gc.setStroke(Color.rgb(255, 255, 255, 0.25));
        gc.strokeRoundRect(x, y, width, height, 14, 14);

        gc.setFill(Color.rgb(70, 75, 85));
        gc.setFont(Font.font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, x + width / 2, y + height / 2);
        gc.setTextBaseline(VPos.TOP);
    }

    // Draws disabled button.
    public static void drawDisabledButton(GraphicsContext gc,
                                          double x,
                                          double y,
                                          double width,
                                          double height,
                                          String text) {
        gc.setFill(Color.rgb(85, 92, 107));
        gc.fillRoundRect(x, y, width, height, 14, 14);

        gc.setStroke(Color.rgb(255, 255, 255, 0.15));
        gc.strokeRoundRect(x, y, width, height, 14, 14);

        gc.setFill(Color.rgb(190, 196, 207));
        gc.setFont(Font.font("Arial", 15));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, x + width / 2, y + height / 2);
        gc.setTextBaseline(VPos.TOP);
    }

    // Draws page text.
    public static void drawPageText(GraphicsContext gc,
                                    int pageIndex,
                                    int maxPage,
                                    double x,
                                    double y,
                                    Color color) {
        gc.setFill(color);
        gc.setFont(Font.font("Arial", 13));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Page " + (pageIndex + 1) + "/" + (maxPage + 1), x, y);
    }

    // Draws arrow buttons.
    public static void drawArrowButtons(GraphicsContext gc,
                                        double prevX,
                                        double nextX,
                                        double y,
                                        double width,
                                        double height,
                                        int pageIndex,
                                        int maxPage) {
        if (pageIndex > 0) {
            drawButton(gc, prevX, y, width, height, "<");
        } else {
            drawDisabledButton(gc, prevX, y, width, height, "<");
        }

        if (pageIndex < maxPage) {
            drawButton(gc, nextX, y, width, height, ">");
        } else {
            drawDisabledButton(gc, nextX, y, width, height, ">");
        }
    }

    // Draws small card.
    public static void drawSmallCard(GraphicsContext gc,
                                     double x,
                                     double y,
                                     String type,
                                     String text,
                                     Color color) {
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

    // Draws wrapped text.
    public static void drawWrappedText(GraphicsContext gc,
                                       String text,
                                       double x,
                                       double y,
                                       double maxWidth,
                                       double lineHeight) {
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

    // Finds max page.
    public static int getMaxPage(int itemCount, int itemsPerPage) {
        if (itemCount <= 0) {
            return 0;
        }

        return (itemCount - 1) / itemsPerPage;
    }

    // Keeps page in range.
    public static int keepPageInRange(int pageIndex, int maxPage) {
        if (pageIndex < 0) {
            return 0;
        }

        return Math.min(pageIndex, maxPage);
    }

    // Checks whether inside.
    public static boolean isInside(double mouseX,
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

    // Checks whether a button was clicked and marks it for pressed feedback.
    public static boolean handleButtonClick(double mouseX,
                                            double mouseY,
                                            double x,
                                            double y,
                                            double width,
                                            double height) {
        if (!isInside(mouseX, mouseY, x, y, width, height)) {
            return false;
        }

        markButtonPressed(x, y, width, height);
        return true;
    }

    // Checks whether a button should currently show pressed feedback.
    public static boolean isButtonPressed(double x, double y, double width, double height) {
        return pressedButtonKey != null
                && System.currentTimeMillis() < pressedUntilMillis
                && pressedButtonKey.equals(buttonKey(x, y, width, height));
    }

    // Marks a button as recently pressed for visual feedback.
    private static void markButtonPressed(double x, double y, double width, double height) {
        pressedButtonKey = buttonKey(x, y, width, height);
        pressedUntilMillis = System.currentTimeMillis() + BUTTON_PRESSED_MS;
    }

    private static String buttonKey(double x, double y, double width, double height) {
        return x + ":" + y + ":" + width + ":" + height;
    }

    // Finds display color name.
    public static String getDisplayColorName(PropertyColor color) {
        if (color == null) {
            return "No Color";
        }

        return color.getDisplayName();
    }
}
