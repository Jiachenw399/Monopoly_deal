package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;
import model.ActionCardType;
import model.ActionCards;

public class ActionCardChoicePanel {
    private ActionCards selectedCard;

    private final double panelX = 330;
    private final double panelY = 190;
    private final double panelWidth = 360;
    private final double panelHeight = 230;

    private final double moneyButtonX = panelX + 40;
    private final double moneyButtonY = panelY + 115;
    private final double moneyButtonWidth = 125;
    private final double moneyButtonHeight = 48;

    private final double actionButtonX = panelX + 195;
    private final double actionButtonY = panelY + 115;
    private final double actionButtonWidth = 125;
    private final double actionButtonHeight = 48;

    private final double cancelButtonX = panelX + 125;
    private final double cancelButtonY = panelY + 175;
    private final double cancelButtonWidth = 110;
    private final double cancelButtonHeight = 34;

    // Shows this screen area.
    public void show(ActionCards card) {
        selectedCard = card;
    }

    // Closes this screen area.
    public void close() {
        selectedCard = null;
    }

    // Checks whether showing.
    public boolean isShowing() {
        return selectedCard != null;
    }

    public ActionCards getSelectedCard() {
        return selectedCard;
    }

    // Checks whether money clicked.
    public boolean isMoneyClicked(double mouseX, double mouseY) {
        return isShowing()
                && ScreenDrawHelper.handleButtonClick(mouseX, mouseY,
                moneyButtonX, moneyButtonY, moneyButtonWidth, moneyButtonHeight);
    }

    // Checks whether action clicked.
    public boolean isActionClicked(double mouseX, double mouseY) {
        return isShowing()
                && ScreenDrawHelper.handleButtonClick(mouseX, mouseY,
                actionButtonX, actionButtonY, actionButtonWidth, actionButtonHeight);
    }

    // Checks whether cancel clicked.
    public boolean isCancelClicked(double mouseX, double mouseY) {
        return isShowing()
                && ScreenDrawHelper.handleButtonClick(mouseX, mouseY,
                cancelButtonX, cancelButtonY, cancelButtonWidth, cancelButtonHeight);
    }

    // Checks whether this can use as action.
    public boolean canUseAsAction() {
        ActionCardType type = selectedCard.getActionCardType();

        //JUST_SAY_NO and DOUBLE_THE_RENT cannot be directly used
        return type != ActionCardType.JUST_SAY_NO
                && type != ActionCardType.DOUBLE_THE_RENT;
    }

    // Draws this screen area.
    public void draw(GraphicsContext gc) {
        if (!isShowing()) {
            return;
        }

        drawOverlay(gc);
        drawPanel(gc);
        drawChoiceWindow(gc);
        drawButtons(gc);
    }

    // Draws overlay.
    private void drawOverlay(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.70));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
    }

    // Draws panel.
    private void drawPanel(GraphicsContext gc) {
        gc.setFill(Color.rgb(18, 24, 35));
        gc.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);

        gc.setStroke(Color.rgb(255, 184, 77));
        gc.setLineWidth(2);
        gc.strokeRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);
        gc.setLineWidth(1);
    }

    // Draws choice window.
    private void drawChoiceWindow(GraphicsContext gc) {
        gc.setFill(Color.rgb(255, 232, 180));
        gc.setFont(Font.font("Arial", 24));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Choose Card Usage", panelX + panelWidth / 2, panelY + 28);

        gc.setFill(Color.rgb(210, 218, 230));
        gc.setFont(Font.font("Arial", 14));
        gc.fillText("Use this action card as money or play its action effect",
                panelX + panelWidth / 2,
                panelY + 68
        );
    }

    // Draws buttons.
    private void drawButtons(GraphicsContext gc) {
        drawButtonCanBeUsed(gc, moneyButtonX, moneyButtonY, moneyButtonWidth, moneyButtonHeight, "As Money");

        if (canUseAsAction()) {
            drawButtonCanBeUsed(gc, actionButtonX, actionButtonY, actionButtonWidth, actionButtonHeight, "As Action");
        } else {
            drawButtonCannotBeUsed(gc, actionButtonX, actionButtonY, actionButtonWidth, actionButtonHeight, "As Action");
        }

        drawCancelButton(gc);
    }

    // Draws button can be used.
    private void drawButtonCanBeUsed(GraphicsContext gc, double x, double y, double width, double height, String text) {
        if (ScreenDrawHelper.isButtonPressed(x, y, width, height)) {
            ScreenDrawHelper.drawPressedButton(gc, x, y, width, height, text);
            return;
        }

        gc.setFill(Color.rgb(255, 184, 77));
        gc.fillRoundRect(x, y, width, height, 12, 12);

        gc.setStroke(Color.rgb(220, 130, 40));
        gc.strokeRoundRect(x, y, width, height, 12, 12);

        gc.setFill(Color.rgb(34, 26, 10));
        gc.setFont(Font.font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, x + width / 2, y + height / 2);
    }

    // Draws button cannot be used.
    private void drawButtonCannotBeUsed(GraphicsContext gc, double x, double y, double width, double height, String text) {
        gc.setFill(Color.rgb(20, 20, 20));
        gc.fillRoundRect(x, y, width, height, 12, 12);

        gc.setStroke(Color.rgb(80, 80, 80));
        gc.strokeRoundRect(x, y, width, height, 12, 12);

        gc.setFill(Color.rgb(120, 120, 120));
        gc.setFont(Font.font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, x + width / 2, y + height / 2);
    }

    // Draws cancel button.
    private void drawCancelButton(GraphicsContext gc) {
        if (ScreenDrawHelper.isButtonPressed(cancelButtonX, cancelButtonY, cancelButtonWidth, cancelButtonHeight)) {
            ScreenDrawHelper.drawPressedButton(gc, cancelButtonX, cancelButtonY, cancelButtonWidth, cancelButtonHeight, "Cancel");
            return;
        }

        gc.setFill(Color.rgb(45, 55, 72));
        gc.fillRoundRect(cancelButtonX, cancelButtonY, cancelButtonWidth, cancelButtonHeight, 10, 10);

        gc.setStroke(Color.rgb(120, 130, 150));
        gc.strokeRoundRect(cancelButtonX, cancelButtonY, cancelButtonWidth, cancelButtonHeight, 10, 10);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 14));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("Cancel", cancelButtonX + cancelButtonWidth / 2, cancelButtonY + cancelButtonHeight / 2);
    }
}
