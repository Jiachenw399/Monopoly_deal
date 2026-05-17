package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;
import java.util.ArrayList;

import logic.RentCalculator;
import model.*;


public class GameScreen {
    private int viewedPlayerIndex = 0;
    private int lastTurnPlayerIndex = -1;
    private Canvas canvas;
    private Game game;
    private boolean isShow;
    private SlyDealPanel slyDealPanel;
    private DebtCollectorPanel debtCollectorPanel;
    private DealBreakerPanel dealBreakerPanel;
    private TwoColorRentPanel twoColorRentPanel;

    private ActionCards pendingMultipleColorRentCard = null;
    private Player selectedMultipleColorRentTarget = null;
    private PropertyColor selectedMultipleColorRentColor = null;

    private boolean useDoubleRentForMultipleColorRent = false;

    private double multiRentPanelX = 230;
    private double multiRentPanelY = 120;
    private double multiRentButtonWidth = 165;
    private double multiRentButtonHeight = 42;

    private ArrayList<Card> selectedPaymentCards = new ArrayList<>();


    private PropertiesCards selectedWildCard = null;
    private double propertyStartX = 20;
    private double propertyStartY = 285;
    private double smallCardWidth = 60;
    private double smallCardHeight = 85;
    private double propertyGap = 75;
    private double cardWidth = 82;
    private double cardHeight = 112;
    private double gap = 10;
    private double handStartX = 20;
    private double handAreaX = 20;
    private double handAreaWidth = 740;
    private double handStartY = Game.SCREEN_HEIGHT - 150;

    private ActionCards pendingBuildingCard = null;
    private RentCalculator rentCalculator;

    public GameScreen(Game game) {
        this.game = game;
        this.rentCalculator = new RentCalculator();
        canvas = new Canvas(Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
        slyDealPanel = new SlyDealPanel(game);
        debtCollectorPanel = new DebtCollectorPanel(game);
        dealBreakerPanel = new DealBreakerPanel(game);
        twoColorRentPanel = new TwoColorRentPanel(game);
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
        syncViewedPlayerWithCurrentTurn();
        drawBackground();
        drawCurrentPlayer();
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
        dealBreakerPanel.draw(canvas.getGraphicsContext2D());
        drawPaymentSelection();
        drawMultipleColorRentSelection();
        drawBuildingSelection();
    }
    private void syncViewedPlayerWithCurrentTurn() {
        int currentTurnPlayerIndex = game.getCurrentPlayerIndex();

        if (currentTurnPlayerIndex != lastTurnPlayerIndex) {
            viewedPlayerIndex = currentTurnPlayerIndex;
            lastTurnPlayerIndex = currentTurnPlayerIndex;
        }
    }

    public void drawBackground() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        ScreenDrawHelper.drawPageBackground(gc, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
    }

    private void drawCurrentPlayer() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Player currentPlayer = game.getCurrentPlayer();

        ScreenDrawHelper.drawPanel(gc, 16, 14, 735, 94);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);

        gc.setFill(ScreenDrawHelper.TEXT);
        gc.setFont(Font.font("Arial", 22));
        gc.fillText("Player " + (game.getCurrentPlayerIndex() + 1) + "'s Turn", 36, 28);

        ScreenDrawHelper.drawBadge(gc, 36, 62, 145, 28,
                "Played " + currentPlayer.getUseCardTimes() + "/3",
                Color.rgb(255, 226, 166));

        int completedSets = PlayerInfoHelper.getCompletedSetCount(currentPlayer);
        ScreenDrawHelper.drawBadge(gc, 195, 62, 155, 28,
                "Sets " + completedSets + "/3",
                Color.rgb(167, 243, 208));

        drawDeckInfoBesideSets(gc);

        if (game.isDiscard()) {
            gc.setFill(ScreenDrawHelper.DANGER);
            gc.setFont(Font.font("Arial", 15));
            gc.fillText("Discard Phase: discard " + (currentPlayer.getHandCards().size() - 7) + " card(s)", 520, 67);
        }
    }

    private void drawDeckInfoBesideSets(GraphicsContext gc) {
        int remainingCards = game.getDrawCards().getDrawPile().size();

        double deckX = 375;
        double deckY = 42;
        double cardWidth = 34;
        double cardHeight = 44;

        int thickness = Math.max(1, Math.min(6, remainingCards / 15 + 1));

        for (int i = thickness - 1; i >= 0; i--) {
            double offset = i * 2.2;

            gc.setFill(Color.rgb(240, 245, 255));
            gc.fillRoundRect(deckX - offset, deckY + offset, cardWidth, cardHeight, 6, 6);

            gc.setStroke(Color.rgb(70, 85, 110));
            gc.strokeRoundRect(deckX - offset, deckY + offset, cardWidth, cardHeight, 6, 6);
        }

        gc.setFill(ScreenDrawHelper.ACCENT);
        gc.setFont(Font.font("Arial", 13));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(String.valueOf(remainingCards), deckX + cardWidth / 2, deckY + cardHeight / 2);

        gc.setFill(ScreenDrawHelper.MUTED_TEXT);
        gc.setFont(Font.font("Arial", 13));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Draw Pile", deckX + 48, deckY + 3);
        gc.fillText(remainingCards + " left", deckX + 48, deckY + 23);
    }

    private void drawBankCards() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Player currentPlayer = game.getCurrentPlayer();

        ScreenDrawHelper.drawPanel(gc, 16, 118, 735, 128);
        ScreenDrawHelper.drawSectionTitle(gc, "Bank Area", 32, 132);

        int total = PlayerInfoHelper.getBankTotal(currentPlayer);

        gc.setFont(Font.font("Arial", 15));
        gc.setFill(ScreenDrawHelper.ACCENT);
        gc.fillText("Total Money: " + total + "M", 190, 134);

        double startX = 32;
        double startY = 160;
        double cardGap = 75;

        int index = 0;
        for (Card card : currentPlayer.getBankCards()) {
            double x = startX + index * cardGap;
            double y = startY;

            if (!CardImageHelper.drawCardImage(gc, card, x, y, smallCardWidth, smallCardHeight)) {
                ScreenDrawHelper.drawSmallCard(gc, x, y, "Money", card.getValue() + "M", Color.GOLD);
            }
            index++;
        }
    }

    private void drawPropertyCards() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Player currentPlayer = game.getCurrentPlayer();

        ScreenDrawHelper.drawPanel(gc, 16, 260, 735, 128);
        ScreenDrawHelper.drawSectionTitle(gc, "Property Area", 32, 274);

        double startX = 32;
        double startY = 302;
        double cardGap = 75;

        int index = 0;
        for (PropertiesCards card : currentPlayer.getPropertyCards()) {
            double x = startX + index * cardGap;
            double y = startY;

            String colorText = card.getCurrentColor() == null ? "NO COLOR" : card.getCurrentColor().name();

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

            index++;
        }

        drawWildColorButtons();
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
        ScreenDrawHelper.drawPanel(gc, 16, titleY - 14, 745, 165);

        gc.setFill(ScreenDrawHelper.TEXT);
        gc.setFont(Font.font("Arial", 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Hand Cards", 32, titleY);

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
        if (CardImageHelper.drawCardImage(gc, card, x, y, cardWidth, cardHeight)) {
            CardImageHelper.drawHandNumberBadge(gc, number, x + 5, y + 5);
            return;
        }

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
        ScreenDrawHelper.drawWrappedText(gc, name, x + 6, y + 70, cardWidth - 12, 12);
    }

    private void drawPlayerViewButtons() {
        PlayerViewPanel.drawPlayerViewButtons(canvas.getGraphicsContext2D(), game, viewedPlayerIndex);
    }
    private void drawViewedPlayerInfo() {
        PlayerViewPanel.drawViewedPlayerInfo(canvas.getGraphicsContext2D(), game, viewedPlayerIndex);
    }
    public int getClickedPlayerViewButtonIndex(double mouseX, double mouseY) {
        return PlayerViewPanel.getClickedPlayerViewButtonIndex(game, mouseX, mouseY);
    }
    public void setViewedPlayerIndex(int viewedPlayerIndex) {
        this.viewedPlayerIndex = viewedPlayerIndex;
    }
    public void resetViewedPlayerToCurrentPlayer() {
        viewedPlayerIndex = game.getCurrentPlayerIndex();
        lastTurnPlayerIndex = game.getCurrentPlayerIndex();
    }


    private void drawButtons() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        ScreenDrawHelper.drawButton(gc, 820, 520, 170, 40, "END TURN");
        ScreenDrawHelper.drawButton(gc, 820, 570, 170, 40, "BACK MENU");
    }


    private void drawWinMessage() {
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
        slyDealPanel.startSelection(card);
        selectedWildCard = null;
    }

    public void cancelSlyDealSelection() {
        slyDealPanel.cancelSelection();
    }

    public boolean isSlyDealSelecting() {
        return slyDealPanel.isSelecting();
    }

    public ActionCards getPendingSlyDealCard() {
        return slyDealPanel.getPendingCard();
    }

    public boolean isSlyDealCancelClicked(double mouseX, double mouseY) {
        return slyDealPanel.isCancelClicked(mouseX, mouseY);
    }

    public SlyDealChoice getClickedSlyDealChoice(double mouseX, double mouseY) {
        return slyDealPanel.getClickedChoice(mouseX, mouseY);
    }

    private void drawSlyDealSelection() {
        slyDealPanel.draw(canvas.getGraphicsContext2D());
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
        debtCollectorPanel.startSelection(card);
        selectedWildCard = null;
    }

    public void cancelDebtCollectorSelection() {
        debtCollectorPanel.cancelSelection();
    }

    public boolean isDebtCollectorSelecting() {
        return debtCollectorPanel.isSelecting();
    }

    public ActionCards getPendingDebtCollectorCard() {
        return debtCollectorPanel.getPendingCard();
    }

    public boolean isDebtCollectorCancelClicked(double mouseX, double mouseY) {
        return debtCollectorPanel.isCancelClicked(mouseX, mouseY);
    }

    public Player getClickedDebtCollectorTarget(double mouseX, double mouseY) {
        return debtCollectorPanel.getClickedTarget(mouseX, mouseY);
    }

    private void drawDebtCollectorSelection() {
        debtCollectorPanel.draw(canvas.getGraphicsContext2D());
    }

    public void startTwoColorRentSelection(ActionCards card) {
        twoColorRentPanel.startSelection(card);
        selectedWildCard = null;
    }

    public boolean shouldUseDoubleRentForTwoColorRent() {
        return twoColorRentPanel.shouldUseDoubleRent();
    }

    public boolean isTwoColorDoubleRentClicked(double mouseX, double mouseY) {
        return twoColorRentPanel.isDoubleRentClicked(mouseX, mouseY);
    }

    public void toggleTwoColorDoubleRent() {
        twoColorRentPanel.toggleDoubleRent();
    }

    public void cancelTwoColorRentSelection() {
        twoColorRentPanel.cancelSelection();
    }

    public boolean isTwoColorRentSelecting() {
        return twoColorRentPanel.isSelecting();
    }

    public ActionCards getPendingTwoColorRentCard() {
        return twoColorRentPanel.getPendingCard();
    }

    public boolean isTwoColorRentCancelClicked(double mouseX, double mouseY) {
        return twoColorRentPanel.isCancelClicked(mouseX, mouseY);
    }

    public PropertyColor getClickedTwoColorRentColor(double mouseX, double mouseY) {
        return twoColorRentPanel.getClickedRentColor(mouseX, mouseY);
    }

    private void drawTwoColorRentSelection() {
        twoColorRentPanel.draw(canvas.getGraphicsContext2D());
    }

    public void startDealBreakerSelection(ActionCards card) {
        dealBreakerPanel.startSelection(card);
        selectedWildCard = null;
    }

    public void cancelDealBreakerSelection() {
        dealBreakerPanel.cancelSelection();
    }

    public boolean isDealBreakerSelecting() {
        return dealBreakerPanel.isSelecting();
    }

    public ActionCards getPendingDealBreakerCard() {
        return dealBreakerPanel.getPendingCard();
    }

    public boolean isDealBreakerCancelClicked(double mouseX, double mouseY) {
        return dealBreakerPanel.isCancelClicked(mouseX, mouseY);
    }

    public DealBreakerChoice getClickedDealBreakerChoice(double mouseX, double mouseY) {
        return dealBreakerPanel.getClickedChoice(mouseX, mouseY);
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
            ScreenDrawHelper.drawButton(gc, 330, 555, 160, 40, "CONFIRM PAY");
        } else {
            ScreenDrawHelper.drawDisabledButton(gc, 330, 555, 160, 40, "CONFIRM PAY");
        }

        ScreenDrawHelper.drawButton(gc, 510, 555, 120, 40, "CLEAR");

        if (game.canCurrentPaymentUseJustSayNo()) {
            ScreenDrawHelper.drawButton(gc, 650, 555, 220, 40, "USE JUST SAY NO");
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
            int originalCount = PlayerInfoHelper.getPropertyCountByColor(receiver, color);
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

            String text = PlayerInfoHelper.getShortColorName(color)
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

        if (CardImageHelper.drawCardImage(gc, card, x, y, 68, 93)) {
            return;
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
        ScreenDrawHelper.drawWrappedText(gc, text, x + 5, y + 52, 58, 11);
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
            ScreenDrawHelper.drawButton(gc, 500, 535, 160, 40, "CONFIRM");
        } else {
            ScreenDrawHelper.drawDisabledButton(gc, 500, 535, 160, 40, "CONFIRM");
        }

        ScreenDrawHelper.drawButton(gc, 690, 535, 140, 40, "CANCEL");
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
        int rent = rentCalculator.calculateRent(player, color);

        if (useDoubleRentForMultipleColorRent) {
            rent *= 2;
        }

        return rent;
    }

    public void startBuildingSelection(ActionCards card) {
        pendingBuildingCard = card;
        selectedWildCard = null;
    }

    public void cancelBuildingSelection() {
        pendingBuildingCard = null;
    }

    public boolean isBuildingSelecting() {
        return pendingBuildingCard != null;
    }

    public ActionCards getPendingBuildingCard() {
        return pendingBuildingCard;
    }

    public boolean isBuildingCancelClicked(double mouseX, double mouseY) {
        return isBuildingSelecting()
                && mouseX >= 720 && mouseX <= 860
                && mouseY >= 505 && mouseY <= 545;
    }

    public PropertyColor getClickedBuildingColor(double mouseX, double mouseY) {
        if (!isBuildingSelecting()) {
            return null;
        }

        ArrayList<PropertyColor> colors = getAvailableBuildingColors();

        double startX = 260;
        double startY = 160;
        double width = 160;
        double height = 45;
        double gapX = 20;
        double gapY = 18;
        int buttonsPerRow = 3;

        for (int i = 0; i < colors.size(); i++) {
            int row = i / buttonsPerRow;
            int col = i % buttonsPerRow;

            double x = startX + col * (width + gapX);
            double y = startY + row * (height + gapY);

            if (mouseX >= x && mouseX <= x + width
                    && mouseY >= y && mouseY <= y + height) {
                return colors.get(i);
            }
        }

        return null;
    }

    private ArrayList<PropertyColor> getAvailableBuildingColors() {
        ArrayList<PropertyColor> result = new ArrayList<>();
        Player currentPlayer = game.getCurrentPlayer();

        if (pendingBuildingCard == null) {
            return result;
        }

        for (PropertyColor color : PropertyColor.values()) {
            int count = PlayerInfoHelper.getPropertyCountByCurrentColor(currentPlayer, color);
            boolean complete = count >= color.getAmountToCompleteSet();

            if (!complete) {
                continue;
            }

            if (pendingBuildingCard.getActionCardType() == ActionCardType.HOUSE) {
                if (!PlayerInfoHelper.hasHouse(currentPlayer, color)) {
                    result.add(color);
                }
            } else if (pendingBuildingCard.getActionCardType() == ActionCardType.HOTEL) {
                if (PlayerInfoHelper.hasHouse(currentPlayer, color)
                        && !PlayerInfoHelper.hasHotel(currentPlayer, color)) {
                    result.add(color);
                }
            }
        }

        return result;
    }

    private void drawBuildingSelection() {
        if (!isBuildingSelecting()) {
            return;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.rgb(0, 0, 0, 0.76));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);

        String title = pendingBuildingCard.getActionCardType() == ActionCardType.HOUSE
                ? "HOUSE: Choose a completed set"
                : "HOTEL: Choose a completed set with a house";

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 26));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(title, Game.SCREEN_WIDTH / 2, 55);

        gc.setFont(Font.font("Arial", 16));
        gc.fillText("House adds +3M rent. Hotel adds another +4M rent.",
                Game.SCREEN_WIDTH / 2, 92);

        drawBuildingColorButtons(gc);
        ScreenDrawHelper.drawButton(gc, 720, 505, 140, 40, "CANCEL");
    }

    private void drawBuildingColorButtons(GraphicsContext gc) {
        ArrayList<PropertyColor> colors = getAvailableBuildingColors();

        if (colors.isEmpty()) {
            gc.setFill(Color.LIGHTYELLOW);
            gc.setFont(Font.font("Arial", 20));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("No available property set for this card.", Game.SCREEN_WIDTH / 2, 250);
            return;
        }

        double startX = 260;
        double startY = 160;
        double width = 160;
        double height = 45;
        double gapX = 20;
        double gapY = 18;
        int buttonsPerRow = 3;

        for (int i = 0; i < colors.size(); i++) {
            PropertyColor color = colors.get(i);

            int row = i / buttonsPerRow;
            int col = i % buttonsPerRow;

            double x = startX + col * (width + gapX);
            double y = startY + row * (height + gapY);

            gc.setFill(Color.LIGHTGREEN);
            gc.fillRoundRect(x, y, width, height, 12, 12);

            gc.setStroke(Color.WHITE);
            gc.strokeRoundRect(x, y, width, height, 12, 12);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 14));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(color.name(), x + width / 2, y + height / 2);
        }

        gc.setTextBaseline(VPos.TOP);
    }
}
