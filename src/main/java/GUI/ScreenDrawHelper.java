package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

// Provides shared drawing helper methods and colors.
public class ScreenDrawHelper {
    public static final Color BACKGROUND = Color.rgb(18, 24, 38);
    public static final Color BACKGROUND_LIGHT = Color.rgb(28, 38, 58);
    public static final Color PANEL = Color.rgb(34, 45, 67, 0.88);
    public static final Color PANEL_LIGHT = Color.rgb(245, 247, 250, 0.93);
    public static final Color BORDER = Color.rgb(255, 255, 255, 0.18);
    public static final Color TEXT = Color.rgb(245, 248, 255);
    public static final Color MUTED_TEXT = Color.rgb(185, 196, 215);
    public static final Color ACCENT = Color.rgb(255, 184, 77);
    public static final Color ACCENT_DARK = Color.rgb(231, 139, 48);
    public static final Color SUCCESS = Color.rgb(99, 230, 156);
    public static final Color WARNING = Color.rgb(255, 210, 93);
    public static final Color DANGER = Color.rgb(255, 112, 112);
    
    public static final Color TABLE_FELT = Color.rgb(16, 68, 42);
    public static final Color TABLE_FELT_HIGHLIGHT = Color.rgb(24, 88, 54, 0.4);
    public static final Color TABLE_RAIL = Color.rgb(110, 72, 38);
    public static final Color TABLE_RAIL_DARK = Color.rgb(72, 46, 24);
    public static final double TABLE_BORDER_WIDTH = 26;
    public static final double TABLE_CONTENT_PAD = 8;

    public static double tableContentX() {
        return TABLE_BORDER_WIDTH + TABLE_CONTENT_PAD;
    }

    public static double tableContentY() {
        return TABLE_BORDER_WIDTH + TABLE_CONTENT_PAD;
    }

    public static double tableContentWidth(double screenWidth, double rightColumnX) {
        return rightColumnX - tableContentX() - TABLE_CONTENT_PAD;
    }

    public static void drawGameTableBackground(GraphicsContext gc, double width, double height) {
        gc.clearRect(0, 0, width, height);

        gc.setFill(TABLE_RAIL_DARK);
        gc.fillRect(0, 0, width, height);

        gc.setFill(TABLE_RAIL);
        gc.fillRect(4, 4, width - 8, height - 8);

        double inset = TABLE_BORDER_WIDTH;
        double feltWidth = width - inset * 2;
        double feltHeight = height - inset * 2;

        gc.setFill(TABLE_FELT);
        gc.fillRect(inset, inset, feltWidth, feltHeight);

        gc.setFill(TABLE_FELT_HIGHLIGHT);
        gc.fillOval(inset + feltWidth * 0.15, inset + feltHeight * 0.1, feltWidth * 0.55, feltHeight * 0.45);
        gc.setFill(Color.rgb(10, 52, 32, 0.25));
        gc.fillOval(inset + feltWidth * 0.45, inset + feltHeight * 0.45, feltWidth * 0.5, feltHeight * 0.4);

        gc.setStroke(Color.rgb(140, 95, 52, 0.55));
        gc.setLineWidth(2);
        gc.strokeRect(inset + 1, inset + 1, feltWidth - 2, feltHeight - 2);
    }

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

    // Draws a dark overlay over the screen.
    public static void drawOverlay(GraphicsContext gc) {
        gc.setFill(Color.rgb(5, 8, 14, 0.78));
        gc.fillRect(0, 0, 1035, 625);
    }

    // Draws a dark rounded panel.
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

    // Draws a light rounded panel.
    public static void drawLightPanel(GraphicsContext gc,
                                      double x,
                                      double y,
                                      double width,
                                      double height) {
        gc.setFill(PANEL_LIGHT);
        gc.fillRoundRect(x, y, width, height, 20, 20);
        gc.setStroke(Color.rgb(255, 255, 255, 0.85));
        gc.strokeRoundRect(x, y, width, height, 20, 20);
    }

    // Draws a section title.
    public static void drawSectionTitle(GraphicsContext gc, String title, double x, double y) {
        gc.setFill(TEXT);
        gc.setFont(Font.font("Arial", 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(title, x, y);
    }

    // Draws a small rounded text badge.
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

    // Draws an active button.
    public static void drawButton(GraphicsContext gc,
                                  double x,
                                  double y,
                                  double width,
                                  double height,
                                  String text) {
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

    // Draws a disabled button.
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

    // Draws the shared Double the Rent checkbox.
    public static void drawDoubleRentOption(GraphicsContext gc,
                                            double x,
                                            double y,
                                            boolean selected) {
        gc.setFill(Color.rgb(255, 240, 199));
        gc.fillRoundRect(x, y, 280, 35, 12, 12);

        gc.setStroke(Color.rgb(255, 255, 255, 0.85));
        gc.strokeRoundRect(x, y, 280, 35, 12, 12);

        gc.setFill(Color.WHITE);
        gc.fillRoundRect(x + 12, y + 8, 18, 18, 5, 5);
        gc.setStroke(Color.rgb(120, 95, 35));
        gc.strokeRoundRect(x + 12, y + 8, 18, 18, 5, 5);

        if (selected) {
            gc.setFill(SUCCESS);
            gc.setFont(Font.font("Arial", 18));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText("✓", x + 21, y + 17);
        }

        gc.setFill(Color.rgb(50, 40, 20));
        gc.setFont(Font.font("Arial", 14));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("Use DOUBLE THE RENT  ×2", x + 42, y + 17.5);
        gc.setTextBaseline(VPos.TOP);
    }

    // Draws a simple small card without image.
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

    // Draws long text across multiple lines.
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
}
