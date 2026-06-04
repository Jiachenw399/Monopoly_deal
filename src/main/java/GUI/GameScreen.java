package GUI;

import javafx.scene.canvas.Canvas;
import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import logic.Game;
import logic.GameObserver;
import java.util.ArrayList;
import model.*;


public class GameScreen implements GameObserver {
    private int viewedPlayerIndex = 0;
    private int lastTurnPlayerIndex = -1;
    private boolean lockViewToCurrentTurn = true;
    private Canvas canvas;
    private Game game;
    private boolean isShow;

    private SlyDealPanel slyDealPanel;
    private DebtCollectorPanel debtCollectorPanel;
    private DealBreakerPanel dealBreakerPanel;
    private TwoColorRentPanel twoColorRentPanel;
    private ForcedDealPanel forcedDealPanel;
    private BackGroundScreen backGroundScreen;
    private PaymentSelectionPanel paymentSelectionPanel;
    private MultipleColorRentSelectionPanel multipleColorRentSelectionPanel;
    private BuildingSelectionPanel buildingSelectionPanel;
    private WildCardSelectionPanel wildCardSelectionPanel;
    private PlayerViewPanel playerViewPanel;
    private PlayerDetailPopupPanel playerDetailPopupPanel;
    private ActionCardChoicePanel actionCardChoicePanel;
    private MusicPlayer musicPlayer;
    private boolean shuffleAnimating;
    private long shuffleStartNanos;
    private boolean endTurnEnabled = true;

    private double cardWidth = 82;
    private double cardHeight = 112;
    private double gap = 10;
    private double handStartX = 20;
    private double handAreaX = 20;
    private double handAreaWidth = 740;
    private double handStartY = Game.SCREEN_HEIGHT - 150;


    // Creates a GameScreen instance.
    public GameScreen(Game game) {
        this(game, null);
    }

    // Creates a GameScreen instance.
    public GameScreen(Game game, MusicPlayer musicPlayer) {
        this.game = game;
        canvas = new Canvas(GuiScale.canvasWidth(), GuiScale.canvasHeight());
        slyDealPanel = new SlyDealPanel(game);
        debtCollectorPanel = new DebtCollectorPanel(game);
        dealBreakerPanel = new DealBreakerPanel(game);
        twoColorRentPanel = new TwoColorRentPanel(game);
        forcedDealPanel = new ForcedDealPanel(game);
        backGroundScreen = new BackGroundScreen(game);
        paymentSelectionPanel = new PaymentSelectionPanel(game);
        multipleColorRentSelectionPanel = new MultipleColorRentSelectionPanel(game);
        buildingSelectionPanel = new BuildingSelectionPanel(game);
        wildCardSelectionPanel = new WildCardSelectionPanel(game);
        playerViewPanel = new PlayerViewPanel();
        playerDetailPopupPanel = new PlayerDetailPopupPanel(game);
        actionCardChoicePanel = new ActionCardChoicePanel();
        this.musicPlayer = musicPlayer;
        this.isShow = false;
        this.shuffleAnimating = false;
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

    // Sets whether End Turn is currently clickable.
    public void setEndTurnEnabled(boolean endTurnEnabled) {
        this.endTurnEnabled = endTurnEnabled;
        backGroundScreen.setEndTurnEnabled(endTurnEnabled);
    }

    public void setTurnRemainingSeconds(int turnRemainingSeconds) {
        backGroundScreen.setTurnRemainingSeconds(turnRemainingSeconds);
    }

    // Runs paint.
    public void paint() {
        GuiScale.prepare(canvas.getGraphicsContext2D());
        syncViewedPlayerWithCurrentTurn();
        backGroundScreen.setEndTurnEnabled(isEndTurnEnabled());
        if (lockViewToCurrentTurn) {
            backGroundScreen.drawAllBackground(canvas, wildCardSelectionPanel.getSelectedWildCard());
        } else {
            backGroundScreen.drawAllBackground(canvas, wildCardSelectionPanel.getSelectedWildCard(), viewedPlayerIndex);
        }
        playerViewPanel.drawPlayerViewButtons(canvas.getGraphicsContext2D(), game, viewedPlayerIndex);
        slyDealPanel.draw(canvas.getGraphicsContext2D());
        debtCollectorPanel.draw(canvas.getGraphicsContext2D());
        twoColorRentPanel.draw(canvas.getGraphicsContext2D());
        dealBreakerPanel.draw(canvas.getGraphicsContext2D());
        paymentSelectionPanel.draw(canvas.getGraphicsContext2D());
        multipleColorRentSelectionPanel.draw(canvas.getGraphicsContext2D());
        buildingSelectionPanel.draw(canvas.getGraphicsContext2D());
        forcedDealPanel.draw(canvas.getGraphicsContext2D());
        playerDetailPopupPanel.draw(canvas.getGraphicsContext2D());
        actionCardChoicePanel.draw(canvas.getGraphicsContext2D());
        drawMuteButton(canvas.getGraphicsContext2D());
        drawShuffleAnimation(canvas.getGraphicsContext2D());
    }

    // Starts shuffle animation.
    public void startShuffleAnimation() {
        shuffleAnimating = true;
        shuffleStartNanos = System.nanoTime();
    }

    // Stops shuffle animation.
    public void stopShuffleAnimation() {
        shuffleAnimating = false;
    }

    public boolean isShuffleAnimating() {
        return shuffleAnimating;
    }

    // Draws shuffle animation.
    private void drawShuffleAnimation(javafx.scene.canvas.GraphicsContext gc) {
        if (!shuffleAnimating) {
            return;
        }

        double elapsed = (System.nanoTime() - shuffleStartNanos) / 1_000_000_000.0;
        double centerX = Game.SCREEN_WIDTH / 2;
        double centerY = Game.SCREEN_HEIGHT / 2 + 8;

        gc.setFill(Color.rgb(10, 14, 24, 0.82));
        gc.fillRect(0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(ScreenDrawHelper.ACCENT);
        gc.setFont(Font.font("Arial", 34));
        gc.fillText("Shuffling the Deal", centerX, 118);

        gc.setFill(Color.rgb(220, 230, 245));
        gc.setFont(Font.font("Arial", 17));
        gc.fillText("Get ready...", centerX, 158);

        drawShuffleDeck(gc, centerX - 180, centerY, elapsed, -1);
        drawShuffleDeck(gc, centerX + 180, centerY, elapsed, 1);
        drawFlyingCards(gc, centerX, centerY, elapsed);

        gc.setFill(Color.rgb(255, 184, 77, 0.35 + 0.25 * Math.sin(elapsed * 8)));
        gc.fillOval(centerX - 190, centerY + 120, 380, 32);
    }

    // Draws shuffle deck.
    private void drawShuffleDeck(javafx.scene.canvas.GraphicsContext gc,
                                 double x,
                                 double y,
                                 double elapsed,
                                 int direction) {
        for (int i = 0; i < 8; i++) {
            double offset = i * 3.5;
            double pulse = Math.sin(elapsed * 5 + i * 0.4) * 4;
            drawAnimatedCard(gc, x + direction * offset, y - offset + pulse, direction * 8, i);
        }
    }

    // Draws flying cards.
    private void drawFlyingCards(javafx.scene.canvas.GraphicsContext gc,
                                 double centerX,
                                 double centerY,
                                 double elapsed) {
        for (int i = 0; i < 10; i++) {
            double t = (elapsed * 0.75 + i / 10.0) % 1.0;
            double side = i % 2 == 0 ? -1 : 1;
            double arc = Math.sin(t * Math.PI);
            double x = centerX + side * (210 - t * 420);
            double y = centerY - 80 * arc + Math.cos(t * Math.PI * 2) * 10;
            double angle = side * (35 - t * 70);

            drawAnimatedCard(gc, x, y, angle, i);
        }
    }

    // Draws animated card.
    private void drawAnimatedCard(javafx.scene.canvas.GraphicsContext gc,
                                  double x,
                                  double y,
                                  double angle,
                                  int index) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(angle);

        Color fill = switch (index % 4) {
            case 0 -> Color.rgb(255, 252, 225);
            case 1 -> Color.rgb(219, 239, 255);
            case 2 -> Color.rgb(226, 255, 229);
            default -> Color.rgb(255, 226, 235);
        };

        gc.setFill(Color.rgb(0, 0, 0, 0.24));
        gc.fillRoundRect(-30 + 4, -42 + 5, 60, 84, 10, 10);
        gc.setFill(fill);
        gc.fillRoundRect(-30, -42, 60, 84, 10, 10);
        gc.setStroke(ScreenDrawHelper.ACCENT);
        gc.strokeRoundRect(-30, -42, 60, 84, 10, 10);

        gc.setFill(Color.rgb(35, 45, 63));
        gc.setFont(Font.font("Arial", 20));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(index % 3 == 0 ? "$" : "M", 0, 0);
        gc.restore();
    }

    // Draws mute button.
    private void drawMuteButton(javafx.scene.canvas.GraphicsContext gc) {
        if (musicPlayer == null) {
            return;
        }

        ScreenDrawHelper.drawButton(gc, 895, 20, 120, 36, musicPlayer.isMuted() ? "Unmute" : "Mute");
    }

    // Synchronizes viewed player with current turn.
    private void syncViewedPlayerWithCurrentTurn() {
        if (!lockViewToCurrentTurn) {
            keepViewedPlayerInRange();
            return;
        }

        int currentTurnPlayerIndex = game.getCurrentPlayerIndex();

        if (currentTurnPlayerIndex != lastTurnPlayerIndex) {
            viewedPlayerIndex = currentTurnPlayerIndex;
            lastTurnPlayerIndex = currentTurnPlayerIndex;
        }
    }

    // Keeps viewed player in range.
    private void keepViewedPlayerInRange() {
        if (game.getPlayers().isEmpty()) {
            viewedPlayerIndex = 0;
            return;
        }

        if (viewedPlayerIndex < 0) {
            viewedPlayerIndex = 0;
        } else if (viewedPlayerIndex >= game.getPlayers().size()) {
            viewedPlayerIndex = game.getPlayers().size() - 1;
        }
    }

    // Finds clicked player view button index.
    public int getClickedPlayerViewButtonIndex(double mouseX, double mouseY) {
        return PlayerViewPanel.getClickedPlayerViewButtonIndex(game, mouseX, mouseY);
    }

    // Runs reset viewed player to current player.
    public void resetViewedPlayerToCurrentPlayer() {
        viewedPlayerIndex = game.getCurrentPlayerIndex();
        lastTurnPlayerIndex = game.getCurrentPlayerIndex();
    }

    // Runs lock viewed player.
    public void lockViewedPlayer(int playerIndex) {
        lockViewToCurrentTurn = false;
        viewedPlayerIndex = playerIndex;
        keepViewedPlayerInRange();
    }

    // Runs on game state changed.
    @Override
    public void onGameStateChanged() {
        if (lockViewToCurrentTurn) {
            resetViewedPlayerToCurrentPlayer();
        } else {
            keepViewedPlayerInRange();
        }
    }


    // Finds clicked hand card index.
    public int getClickedHandCardIndex(double mouseX, double mouseY) {
        Player currentPlayer = getViewedPlayer();
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

    // Finds viewed hand card.
    public Card getViewedHandCard(int handIndex) {
        Player player = getViewedPlayer();

        if (handIndex < 0 || handIndex >= player.getHandCards().size()) {
            return null;
        }

        return player.getHandCards().get(handIndex);
    }

    // Handles background page button click.
    public boolean handleBackgroundPageButtonClick(double mouseX, double mouseY) {
        if (lockViewToCurrentTurn) {
            return backGroundScreen.handlePageButtonClick(mouseX, mouseY);
        }

        return backGroundScreen.handlePageButtonClick(mouseX, mouseY, viewedPlayerIndex);
    }

    // Checks whether end turn clicked.
    public boolean isEndTurnClicked(double mouseX, double mouseY) {
        return isEndTurnEnabled()
                && ScreenDrawHelper.handleButtonClick(mouseX, mouseY, 820, 520, 170, 40);
    }

    // Checks whether End Turn can currently be used.
    public boolean isEndTurnEnabled() {
        return endTurnEnabled && !game.getCurrentPlayer().isAI();
    }

    // Checks whether back menu clicked.
    public boolean isBackMenuClicked(double mouseX, double mouseY) {
        return ScreenDrawHelper.handleButtonClick(mouseX, mouseY, 820, 570, 170, 40);
    }

    // Checks whether play again clicked.
    public boolean isPlayAgainClicked(double mouseX, double mouseY) {
        return backGroundScreen.isPlayAgainClicked(mouseX, mouseY);
    }

    // Checks whether mute clicked.
    public boolean isMuteClicked(double mouseX, double mouseY) {
        return musicPlayer != null
                && ScreenDrawHelper.handleButtonClick(mouseX, mouseY, 895, 20, 120, 36);
    }

    // Toggles mute.
    public void toggleMute() {
        if (musicPlayer != null) {
            musicPlayer.toggleMute();
        }
    }

    // Clears the current state.
    public void clear() {
        GuiScale.clear(canvas);
    }

    // Starts sly deal selection.
    public void startSlyDealSelection(ActionCards card) {
        slyDealPanel.startSelection(card);
        wildCardSelectionPanel.clearSelection();
    }

    // Checks whether this can cel sly deal selection.
    public void cancelSlyDealSelection() {
        slyDealPanel.cancelSelection();
    }

    // Checks whether sly deal selecting.
    public boolean isSlyDealSelecting() {
        return slyDealPanel.isSelecting();
    }

    // Finds pending sly deal card.
    public ActionCards getPendingSlyDealCard() {
        return slyDealPanel.getPendingCard();
    }

    // Checks whether sly deal cancel clicked.
    public boolean isSlyDealCancelClicked(double mouseX, double mouseY) {
        return slyDealPanel.isCancelClicked(mouseX, mouseY);
    }

    // Finds clicked sly deal choice.
    public SlyDealChoice getClickedSlyDealChoice(double mouseX, double mouseY) {
        return slyDealPanel.getClickedChoice(mouseX, mouseY);
    }

    public static class SlyDealChoice {
        private Player targetPlayer;
        private PropertiesCards selectedCard;

        // Runs sly deal choice.
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

    // Checks whether sly deal prev page clicked.
    public boolean isSlyDealPrevPageClicked(double mouseX, double mouseY) {
        return slyDealPanel.isPrevPageClicked(mouseX, mouseY);
    }

    // Checks whether sly deal next page clicked.
    public boolean isSlyDealNextPageClicked(double mouseX, double mouseY) {
        return slyDealPanel.isNextPageClicked(mouseX, mouseY);
    }

    // Runs previous sly deal page.
    public void previousSlyDealPage() {
        slyDealPanel.previousPage();
    }

    // Runs next sly deal page.
    public void nextSlyDealPage() {
        slyDealPanel.nextPage();
    }

    // Finds clicked sly deal target.
    public Player getClickedSlyDealTarget(double mouseX, double mouseY) {
        return slyDealPanel.getClickedTargetPlayer(mouseX, mouseY);
    }

    // Shows sly deal target detail.
    public void showSlyDealTargetDetail(Player player) {
        slyDealPanel.showTargetDetail(player);
    }

    // Finds sly deal detail target.
    public Player getSlyDealDetailTarget() {
        return slyDealPanel.getDetailTargetPlayer();
    }

    // Runs set selected sly deal target.
    public void setSelectedSlyDealTarget(Player player) {
        slyDealPanel.setSelectedTargetPlayer(player);
    }

    // Finds selected sly deal target.
    public Player getSelectedSlyDealTarget() {
        return slyDealPanel.getSelectedTargetPlayer();
    }

    // Checks whether sly deal detail close clicked.
    public boolean isSlyDealDetailCloseClicked(double mouseX, double mouseY) {
        return slyDealPanel.isDetailCloseClicked(mouseX, mouseY);
    }

    // Checks whether sly deal detail back clicked.
    public boolean isSlyDealDetailBackClicked(double mouseX, double mouseY) {
        return slyDealPanel.isDetailBackClicked(mouseX, mouseY);
    }

    // Checks whether sly deal detail confirm clicked.
    public boolean isSlyDealDetailConfirmClicked(double mouseX, double mouseY) {
        return slyDealPanel.isDetailConfirmClicked(mouseX, mouseY);
    }

    // Handles sly deal detail page button click.
    public boolean handleSlyDealDetailPageButtonClick(double mouseX, double mouseY) {
        return slyDealPanel.handleDetailPageButtonClick(mouseX, mouseY);
    }

    // Checks whether sly deal back clicked.
    public boolean isSlyDealBackClicked(double mouseX, double mouseY) {
        return slyDealPanel.isBackClicked(mouseX, mouseY);
    }

    // Starts debt collector selection.
    public void startDebtCollectorSelection(ActionCards card) {
        debtCollectorPanel.startSelection(card);
        wildCardSelectionPanel.clearSelection();
    }

    // Checks whether this can cel debt collector selection.
    public void cancelDebtCollectorSelection() {
        debtCollectorPanel.cancelSelection();
    }

    // Checks whether debt collector selecting.
    public boolean isDebtCollectorSelecting() {
        return debtCollectorPanel.isSelecting();
    }

    // Finds pending debt collector card.
    public ActionCards getPendingDebtCollectorCard() {
        return debtCollectorPanel.getPendingCard();
    }

    // Checks whether debt collector cancel clicked.
    public boolean isDebtCollectorCancelClicked(double mouseX, double mouseY) {
        return debtCollectorPanel.isCancelClicked(mouseX, mouseY);
    }

    // Finds clicked debt collector target.
    public Player getClickedDebtCollectorTarget(double mouseX, double mouseY) {
        return debtCollectorPanel.getClickedTarget(mouseX, mouseY);
    }

    // Starts two color rent selection.
    public void startTwoColorRentSelection(ActionCards card) {
        twoColorRentPanel.startSelection(card);
        wildCardSelectionPanel.clearSelection();
    }

    // Checks whether this should use double rent for two color rent.
    public boolean shouldUseDoubleRentForTwoColorRent() {
        return twoColorRentPanel.shouldUseDoubleRent();
    }

    // Checks whether two color double rent clicked.
    public boolean isTwoColorDoubleRentClicked(double mouseX, double mouseY) {
        return twoColorRentPanel.isDoubleRentClicked(mouseX, mouseY);
    }

    // Toggles two color double rent.
    public void toggleTwoColorDoubleRent() {
        twoColorRentPanel.toggleDoubleRent();
    }

    // Checks whether this can cel two color rent selection.
    public void cancelTwoColorRentSelection() {
        twoColorRentPanel.cancelSelection();
    }

    // Checks whether two color rent selecting.
    public boolean isTwoColorRentSelecting() {
        return twoColorRentPanel.isSelecting();
    }

    // Finds pending two color rent card.
    public ActionCards getPendingTwoColorRentCard() {
        return twoColorRentPanel.getPendingCard();
    }

    // Checks whether two color rent cancel clicked.
    public boolean isTwoColorRentCancelClicked(double mouseX, double mouseY) {
        return twoColorRentPanel.isCancelClicked(mouseX, mouseY);
    }

    // Finds clicked two color rent color.
    public PropertyColor getClickedTwoColorRentColor(double mouseX, double mouseY) {
        return twoColorRentPanel.getClickedRentColor(mouseX, mouseY);
    }

    // Starts deal breaker selection.
    public void startDealBreakerSelection(ActionCards card) {
        dealBreakerPanel.startSelection(card);
        wildCardSelectionPanel.clearSelection();
    }

    // Checks whether this can cel deal breaker selection.
    public void cancelDealBreakerSelection() {
        dealBreakerPanel.cancelSelection();
    }

    // Checks whether deal breaker selecting.
    public boolean isDealBreakerSelecting() {
        return dealBreakerPanel.isSelecting();
    }

    // Finds pending deal breaker card.
    public ActionCards getPendingDealBreakerCard() {
        return dealBreakerPanel.getPendingCard();
    }

    // Checks whether deal breaker cancel clicked.
    public boolean isDealBreakerCancelClicked(double mouseX, double mouseY) {
        return dealBreakerPanel.isCancelClicked(mouseX, mouseY);
    }

    // Finds clicked deal breaker choice.
    public DealBreakerChoice getClickedDealBreakerChoice(double mouseX, double mouseY) {
        return dealBreakerPanel.getClickedChoice(mouseX, mouseY);
    }

    public static class DealBreakerChoice {
        private Player targetPlayer;
        private ArrayList<PropertiesCards> selectedSet;

        // Runs deal breaker choice.
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

    // Starts multiple color rent selection.
    public void startMultipleColorRentSelection(ActionCards card) {
        multipleColorRentSelectionPanel.startSelection(card);
        wildCardSelectionPanel.clearSelection();
    }

    // Checks whether this should use double rent for multiple color rent.
    public boolean shouldUseDoubleRentForMultipleColorRent() {
        return multipleColorRentSelectionPanel.shouldUseDoubleRent();
    }

    // Finds clicked wild card.
    public PropertiesCards getClickedWildCard(double mouseX, double mouseY) {
        if (lockViewToCurrentTurn) {
            return backGroundScreen.getClickedWildCard(mouseX, mouseY);
        }

        return backGroundScreen.getClickedWildCard(mouseX, mouseY, viewedPlayerIndex);
    }

    // Finds clicked wild color button.
    public PropertyColor getClickedWildColorButton(double mouseX, double mouseY) {
        return wildCardSelectionPanel.getClickedWildColorButton(mouseX, mouseY);
    }

    // Finds selected wild card.
    public PropertiesCards getSelectedWildCard() {
        return wildCardSelectionPanel.getSelectedWildCard();
    }

    // Runs set selected wild card.
    public void setSelectedWildCard(PropertiesCards selectedWildCard) {
        wildCardSelectionPanel.setSelectedWildCard(selectedWildCard);
    }

    // Checks whether multiple color double rent clicked.
    public boolean isMultipleColorDoubleRentClicked(double mouseX, double mouseY) {
        return multipleColorRentSelectionPanel.isDoubleRentClicked(mouseX, mouseY);
    }

    // Toggles multiple color double rent.
    public void toggleMultipleColorDoubleRent() {
        multipleColorRentSelectionPanel.toggleDoubleRent();
    }

    // Checks whether this can cel multiple color rent selection.
    public void cancelMultipleColorRentSelection() {
        multipleColorRentSelectionPanel.cancelSelection();
    }

    // Checks whether multiple color rent selecting.
    public boolean isMultipleColorRentSelecting() {
        return multipleColorRentSelectionPanel.isSelecting();
    }

    // Finds pending multiple color rent card.
    public ActionCards getPendingMultipleColorRentCard() {
        return multipleColorRentSelectionPanel.getPendingCard();
    }

    // Finds selected multiple color rent target.
    public Player getSelectedMultipleColorRentTarget() {
        return multipleColorRentSelectionPanel.getSelectedTarget();
    }

    // Runs set selected multiple color rent target.
    public void setSelectedMultipleColorRentTarget(Player selectedMultipleColorRentTarget) {
        multipleColorRentSelectionPanel.setSelectedTarget(selectedMultipleColorRentTarget);
    }

    // Finds selected multiple color rent color.
    public PropertyColor getSelectedMultipleColorRentColor() {
        return multipleColorRentSelectionPanel.getSelectedColor();
    }

    // Runs set selected multiple color rent color.
    public void setSelectedMultipleColorRentColor(PropertyColor selectedMultipleColorRentColor) {
        multipleColorRentSelectionPanel.setSelectedColor(selectedMultipleColorRentColor);
    }

    // Checks whether this can confirm multiple color rent.
    public boolean canConfirmMultipleColorRent() {
        return multipleColorRentSelectionPanel.canConfirm();
    }

    // Checks whether multiple color rent cancel clicked.
    public boolean isMultipleColorRentCancelClicked(double mouseX, double mouseY) {
        return multipleColorRentSelectionPanel.isCancelClicked(mouseX, mouseY);
    }

    // Checks whether multiple color rent confirm clicked.
    public boolean isMultipleColorRentConfirmClicked(double mouseX, double mouseY) {
        return multipleColorRentSelectionPanel.isConfirmClicked(mouseX, mouseY);
    }

    // Finds clicked multiple color rent target.
    public Player getClickedMultipleColorRentTarget(double mouseX, double mouseY) {
        return multipleColorRentSelectionPanel.getClickedTarget(mouseX, mouseY);
    }

    // Finds clicked multiple color rent color.
    public PropertyColor getClickedMultipleColorRentColor(double mouseX, double mouseY) {
        return multipleColorRentSelectionPanel.getClickedColor(mouseX, mouseY);
    }

    // Checks whether payment confirm clicked.
    public boolean isPaymentConfirmClicked(double mouseX, double mouseY) {
        return paymentSelectionPanel.isConfirmClicked(mouseX, mouseY);
    }

    // Checks whether payment clear clicked.
    public boolean isPaymentClearClicked(double mouseX, double mouseY) {
        return paymentSelectionPanel.isClearClicked(mouseX, mouseY);
    }

    // Checks whether payment just say no clicked.
    public boolean isPaymentJustSayNoClicked(double mouseX, double mouseY) {
        return paymentSelectionPanel.isJustSayNoClicked(mouseX, mouseY);
    }

    // Checks whether payment Just Say No pass clicked.
    public boolean isPaymentJustSayNoPassClicked(double mouseX, double mouseY) {
        return paymentSelectionPanel.isJustSayNoPassClicked(mouseX, mouseY);
    }

    // Clears payment selection.
    public void clearPaymentSelection() {
        paymentSelectionPanel.clearSelection();
    }

    // Finds selected payment cards.
    public ArrayList<Card> getSelectedPaymentCards() {
        return paymentSelectionPanel.getSelectedCards();
    }

    // Checks whether this can confirm payment.
    public boolean canConfirmPayment() {
        return paymentSelectionPanel.canConfirm();
    }

    // Handles payment card click.
    public boolean handlePaymentCardClick(double mouseX, double mouseY) {
        return paymentSelectionPanel.handleCardClick(mouseX, mouseY);
    }

    // Shows player detail popup.
    public void showPlayerDetailPopup(int playerIndex) {
        playerDetailPopupPanel.showPlayer(playerIndex);
    }

    // Checks whether player detail popup showing.
    public boolean isPlayerDetailPopupShowing() {
        return playerDetailPopupPanel.isShowing();
    }

    // Checks whether player detail popup close clicked.
    public boolean isPlayerDetailPopupCloseClicked(double mouseX, double mouseY) {
        return playerDetailPopupPanel.isCloseClicked(mouseX, mouseY);
    }

    // Handles player detail popup page button click.
    public boolean handlePlayerDetailPopupPageButtonClick(double mouseX, double mouseY) {
        return playerDetailPopupPanel.handlePageButtonClick(mouseX, mouseY);
    }

    // Closes player detail popup.
    public void closePlayerDetailPopup() {
        playerDetailPopupPanel.close();
    }

    // Starts building selection.
    public void startBuildingSelection(ActionCards card) {
        buildingSelectionPanel.startSelection(card);
        wildCardSelectionPanel.clearSelection();
    }

    // Checks whether this can cel building selection.
    public void cancelBuildingSelection() {
        buildingSelectionPanel.cancelSelection();
    }

    // Checks whether building selecting.
    public boolean isBuildingSelecting() {
        return buildingSelectionPanel.isSelecting();
    }

    // Finds pending building card.
    public ActionCards getPendingBuildingCard() {
        return buildingSelectionPanel.getPendingCard();
    }

    // Checks whether building cancel clicked.
    public boolean isBuildingCancelClicked(double mouseX, double mouseY) {
        return buildingSelectionPanel.isCancelClicked(mouseX, mouseY);
    }

    // Finds clicked building color.
    public PropertyColor getClickedBuildingColor(double mouseX, double mouseY) {
        return buildingSelectionPanel.getClickedColor(mouseX, mouseY);
    }

    // Starts forced deal selection.
    public void startForcedDealSelection(ActionCards card) {
        forcedDealPanel.startSelection(card);
        wildCardSelectionPanel.clearSelection();
    }

    // Checks whether this can cel forced deal selection.
    public void cancelForcedDealSelection() {
        forcedDealPanel.cancelSelection();
    }

    // Checks whether forced deal selecting.
    public boolean isForcedDealSelecting() {
        return forcedDealPanel.isSelecting();
    }

    // Finds pending forced deal card.
    public ActionCards getPendingForcedDealCard() {
        return forcedDealPanel.getPendingCard();
    }

    // Checks whether forced deal cancel clicked.
    public boolean isForcedDealCancelClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isCancelClicked(mouseX, mouseY);
    }

    // Checks whether forced deal back clicked.
    public boolean isForcedDealBackClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isBackClicked(mouseX, mouseY);
    }

    // Checks whether forced deal confirm clicked.
    public boolean isForcedDealConfirmClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isConfirmClicked(mouseX, mouseY);
    }

    // Checks whether this can confirm forced deal.
    public boolean canConfirmForcedDeal() {
        return forcedDealPanel.canConfirm();
    }

    // Finds clicked forced deal target.
    public Player getClickedForcedDealTarget(double mouseX, double mouseY) {
        return forcedDealPanel.getClickedTargetPlayer(mouseX, mouseY);
    }

    // Finds clicked forced deal my property.
    public PropertiesCards getClickedForcedDealMyProperty(double mouseX, double mouseY) {
        return forcedDealPanel.getClickedMyProperty(mouseX, mouseY);
    }

    // Finds clicked forced deal target property.
    public PropertiesCards getClickedForcedDealTargetProperty(double mouseX, double mouseY) {
        return forcedDealPanel.getClickedTargetProperty(mouseX, mouseY);
    }

    // Runs set selected forced deal target.
    public void setSelectedForcedDealTarget(Player targetPlayer) {
        forcedDealPanel.setSelectedTargetPlayer(targetPlayer);
    }

    // Runs set selected forced deal my property.
    public void setSelectedForcedDealMyProperty(PropertiesCards card) {
        forcedDealPanel.setSelectedMyCard(card);
    }

    // Runs set selected forced deal target property.
    public void setSelectedForcedDealTargetProperty(PropertiesCards card) {
        forcedDealPanel.setSelectedTargetCard(card);
    }

    // Finds selected forced deal target.
    public Player getSelectedForcedDealTarget() {
        return forcedDealPanel.getSelectedTargetPlayer();
    }

    // Finds selected forced deal my property.
    public PropertiesCards getSelectedForcedDealMyProperty() {
        return forcedDealPanel.getSelectedMyCard();
    }

    // Finds selected forced deal target property.
    public PropertiesCards getSelectedForcedDealTargetProperty() {
        return forcedDealPanel.getSelectedTargetCard();
    }

    // Clears selected wild card.
    public void clearSelectedWildCard() {
        wildCardSelectionPanel.clearSelection();
    }

    // Shows action card choice.
    public void showActionCardChoice(ActionCards card) {
        actionCardChoicePanel.show(card);
        wildCardSelectionPanel.clearSelection();
    }

    // Closes action card choice.
    public void closeActionCardChoice() {
        actionCardChoicePanel.close();
    }

    // Checks whether action card choice showing.
    public boolean isActionCardChoiceShowing() {
        return actionCardChoicePanel.isShowing();
    }

    // Finds selected action card choice card.
    public ActionCards getSelectedActionCardChoiceCard() {
        return actionCardChoicePanel.getSelectedCard();
    }

    // Checks whether action card choice money clicked.
    public boolean isActionCardChoiceMoneyClicked(double mouseX, double mouseY) {
        return actionCardChoicePanel.isMoneyClicked(mouseX, mouseY);
    }

    // Checks whether action card choice action clicked.
    public boolean isActionCardChoiceActionClicked(double mouseX, double mouseY) {
        return actionCardChoicePanel.isActionClicked(mouseX, mouseY);
    }

    // Checks whether action card choice cancel clicked.
    public boolean isActionCardChoiceCancelClicked(double mouseX, double mouseY) {
        return actionCardChoicePanel.isCancelClicked(mouseX, mouseY);
    }

    // Checks whether this can use selected action card as action.
    public boolean canUseSelectedActionCardAsAction() {
        return actionCardChoicePanel.canUseAsAction();
    }

    // Checks whether deal breaker prev page clicked.
    public boolean isDealBreakerPrevPageClicked(double mouseX, double mouseY) {
        return dealBreakerPanel.isPrevPageClicked(mouseX, mouseY);
    }

    // Checks whether deal breaker next page clicked.
    public boolean isDealBreakerNextPageClicked(double mouseX, double mouseY) {
        return dealBreakerPanel.isNextPageClicked(mouseX, mouseY);
    }

    // Runs previous deal breaker page.
    public void previousDealBreakerPage() {
        dealBreakerPanel.previousPage();
    }

    // Runs next deal breaker page.
    public void nextDealBreakerPage() {
        dealBreakerPanel.nextPage();
    }

    // Checks whether forced deal my prev page clicked.
    public boolean isForcedDealMyPrevPageClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isMyPrevPageClicked(mouseX, mouseY);
    }

    // Checks whether forced deal my next page clicked.
    public boolean isForcedDealMyNextPageClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isMyNextPageClicked(mouseX, mouseY);
    }

    // Checks whether forced deal target prev page clicked.
    public boolean isForcedDealTargetPrevPageClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isTargetPrevPageClicked(mouseX, mouseY);
    }

    // Checks whether forced deal target next page clicked.
    public boolean isForcedDealTargetNextPageClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isTargetNextPageClicked(mouseX, mouseY);
    }

    // Runs previous forced deal my page.
    public void previousForcedDealMyPage() {
        forcedDealPanel.previousMyPage();
    }

    // Runs next forced deal my page.
    public void nextForcedDealMyPage() {
        forcedDealPanel.nextMyPage();
    }

    // Runs previous forced deal target page.
    public void previousForcedDealTargetPage() {
        forcedDealPanel.previousTargetPage();
    }

    // Runs next forced deal target page.
    public void nextForcedDealTargetPage() {
        forcedDealPanel.nextTargetPage();
    }

    // Finds selected debt collector target.
    public Player getSelectedDebtCollectorTarget() {
        return debtCollectorPanel.getSelectedTarget();
    }

    // Runs set selected debt collector target.
    public void setSelectedDebtCollectorTarget(Player player) {
        debtCollectorPanel.setSelectedTarget(player);
    }

    // Checks whether debt collector back clicked.
    public boolean isDebtCollectorBackClicked(double mouseX, double mouseY) {
        return debtCollectorPanel.isBackClicked(mouseX, mouseY);
    }

    // Checks whether debt collector confirm clicked.
    public boolean isDebtCollectorConfirmClicked(double mouseX, double mouseY) {
        return debtCollectorPanel.isConfirmClicked(mouseX, mouseY);
    }

    // Handles debt collector detail page button click.
    public boolean handleDebtCollectorDetailPageButtonClick(double mouseX, double mouseY) {
        return debtCollectorPanel.handleDetailPageButtonClick(mouseX, mouseY);
    }

    // Checks whether debt collector detail close clicked.
    public boolean isDebtCollectorDetailCloseClicked(double mouseX, double mouseY) {
        return debtCollectorPanel.isDetailCloseClicked(mouseX, mouseY);
    }

    // Checks whether multiple color rent detail showing.
    public boolean isMultipleColorRentDetailShowing() {
        return multipleColorRentSelectionPanel.isDetailShowing();
    }

    // Shows multiple color rent target detail.
    public void showMultipleColorRentTargetDetail(Player player) {
        multipleColorRentSelectionPanel.showTargetDetail(player);
    }

    // Finds multiple color rent detail target.
    public Player getMultipleColorRentDetailTarget() {
        return multipleColorRentSelectionPanel.getDetailTarget();
    }

    // Handles multiple color rent detail page button click.
    public boolean handleMultipleColorRentDetailPageButtonClick(double mouseX, double mouseY) {
        return multipleColorRentSelectionPanel.handleDetailPageButtonClick(mouseX, mouseY);
    }

    // Checks whether multiple color rent detail confirm clicked.
    public boolean isMultipleColorRentDetailConfirmClicked(double mouseX, double mouseY) {
        return multipleColorRentSelectionPanel.isDetailConfirmClicked(mouseX, mouseY);
    }

    // Checks whether multiple color rent detail back clicked.
    public boolean isMultipleColorRentDetailBackClicked(double mouseX, double mouseY) {
        return multipleColorRentSelectionPanel.isDetailBackClicked(mouseX, mouseY);
    }

    // Shows deal breaker detail choice.
    public void showDealBreakerDetailChoice(DealBreakerChoice choice) {
        dealBreakerPanel.showDetailChoice(choice);
    }

    // Finds deal breaker detail choice.
    public DealBreakerChoice getDealBreakerDetailChoice() {
        return dealBreakerPanel.getDetailChoice();
    }

    // Checks whether deal breaker detail close clicked.
    public boolean isDealBreakerDetailCloseClicked(double mouseX, double mouseY) {
        return dealBreakerPanel.isDetailCloseClicked(mouseX, mouseY);
    }

    // Checks whether deal breaker detail back clicked.
    public boolean isDealBreakerDetailBackClicked(double mouseX, double mouseY) {
        return dealBreakerPanel.isDetailBackClicked(mouseX, mouseY);
    }

    // Checks whether deal breaker detail confirm clicked.
    public boolean isDealBreakerDetailConfirmClicked(double mouseX, double mouseY) {
        return dealBreakerPanel.isDetailConfirmClicked(mouseX, mouseY);
    }

    // Handles deal breaker detail page button click.
    public boolean handleDealBreakerDetailPageButtonClick(double mouseX, double mouseY) {
        return dealBreakerPanel.handleDetailPageButtonClick(mouseX, mouseY);
    }

    // Shows forced deal target detail.
    public void showForcedDealTargetDetail(Player player) {
        forcedDealPanel.showTargetDetail(player);
    }

    // Finds forced deal detail target.
    public Player getForcedDealDetailTarget() {
        return forcedDealPanel.getDetailTargetPlayer();
    }

    // Checks whether forced deal detail close clicked.
    public boolean isForcedDealDetailCloseClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isDetailCloseClicked(mouseX, mouseY);
    }

    // Checks whether forced deal detail back clicked.
    public boolean isForcedDealDetailBackClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isDetailBackClicked(mouseX, mouseY);
    }

    // Checks whether forced deal detail confirm clicked.
    public boolean isForcedDealDetailConfirmClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isDetailConfirmClicked(mouseX, mouseY);
    }

    // Handles forced deal detail page button click.
    public boolean handleForcedDealDetailPageButtonClick(double mouseX, double mouseY) {
        return forcedDealPanel.handleDetailPageButtonClick(mouseX, mouseY);
    }

    // Checks whether multiple color rent detail close clicked.
    public boolean isMultipleColorRentDetailCloseClicked(double mouseX, double mouseY) {
        return multipleColorRentSelectionPanel.isDetailCloseClicked(mouseX, mouseY);
    }

    // Finds viewed player.
    private Player getViewedPlayer() {
        keepViewedPlayerInRange();
        return game.getPlayers().get(viewedPlayerIndex);
    }
}
