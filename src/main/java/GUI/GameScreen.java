package GUI;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import logic.Game;
import java.util.ArrayList;
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
    private ForcedDealPanel forcedDealPanel;
    private BackGroundScreen backGroundScreen;
    private PaymentSelectionPanel paymentSelectionPanel;
    private MultipleColorRentSelectionPanel multipleColorRentSelectionPanel;
    private BuildingSelectionPanel buildingSelectionPanel;
    private WildCardSelectionPanel wildCardSelectionPanel;
    private PlayerViewPanel playerViewPanel;
    private PlayerDetailPopupPanel playerDetailPopupPanel;
    private ActionCardChoicePanel actionCardChoicePanel;

    private double cardWidth = 82;
    private double cardHeight = 112;
    private double gap = 10;
    private final double handStartX = ScreenDrawHelper.tableContentX() + 4;
    private final double handAreaX = handStartX;
    private final double handAreaWidth =
            ScreenDrawHelper.tableContentWidth(Game.SCREEN_WIDTH, 760) - 8;
    private double handStartY = Game.SCREEN_HEIGHT - 150;


    public GameScreen(Game game) {
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
        GuiScale.prepare(canvas.getGraphicsContext2D());
        syncViewedPlayerWithCurrentTurn();
        backGroundScreen.drawAllBackground(canvas, wildCardSelectionPanel.getSelectedWildCard());
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
    }

    private void syncViewedPlayerWithCurrentTurn() {
        int currentTurnPlayerIndex = game.getCurrentPlayerIndex();

        if (currentTurnPlayerIndex != lastTurnPlayerIndex) {
            viewedPlayerIndex = currentTurnPlayerIndex;
            lastTurnPlayerIndex = currentTurnPlayerIndex;
        }
    }

    public int getClickedPlayerViewButtonIndex(double mouseX, double mouseY) {
        return PlayerViewPanel.getClickedPlayerViewButtonIndex(game, mouseX, mouseY);
    }

    public void resetViewedPlayerToCurrentPlayer() {
        viewedPlayerIndex = game.getCurrentPlayerIndex();
        lastTurnPlayerIndex = game.getCurrentPlayerIndex();
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

    public boolean handleBackgroundPageButtonClick(double mouseX, double mouseY) {
        return backGroundScreen.handlePageButtonClick(mouseX, mouseY);
    }

    public boolean isEndTurnClicked(double mouseX, double mouseY) {
        return mouseX >= 820 && mouseX <= 990 && mouseY >= 520 && mouseY <= 560;
    }

    public boolean isBackMenuClicked(double mouseX, double mouseY) {
        return mouseX >= 820 && mouseX <= 990 && mouseY >= 570 && mouseY <= 610;
    }

    public void clear() {
        GuiScale.clear(canvas);
    }

    public void startSlyDealSelection(ActionCards card) {
        slyDealPanel.startSelection(card);
        wildCardSelectionPanel.clearSelection();
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
        wildCardSelectionPanel.clearSelection();
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

    public void startTwoColorRentSelection(ActionCards card) {
        twoColorRentPanel.startSelection(card);
        wildCardSelectionPanel.clearSelection();
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

    public void startDealBreakerSelection(ActionCards card) {
        dealBreakerPanel.startSelection(card);
        wildCardSelectionPanel.clearSelection();
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

    public void startMultipleColorRentSelection(ActionCards card) {
        multipleColorRentSelectionPanel.startSelection(card);
        wildCardSelectionPanel.clearSelection();
    }

    public boolean shouldUseDoubleRentForMultipleColorRent() {
        return multipleColorRentSelectionPanel.shouldUseDoubleRent();
    }

    public PropertiesCards getClickedWildCard(double mouseX, double mouseY) {
        return wildCardSelectionPanel.getClickedWildCard(mouseX, mouseY);
    }

    public PropertyColor getClickedWildColorButton(double mouseX, double mouseY) {
        return wildCardSelectionPanel.getClickedWildColorButton(mouseX, mouseY);
    }

    public PropertiesCards getSelectedWildCard() {
        return wildCardSelectionPanel.getSelectedWildCard();
    }

    public void setSelectedWildCard(PropertiesCards selectedWildCard) {
        wildCardSelectionPanel.setSelectedWildCard(selectedWildCard);
    }

    public boolean isMultipleColorDoubleRentClicked(double mouseX, double mouseY) {
        return multipleColorRentSelectionPanel.isDoubleRentClicked(mouseX, mouseY);
    }

    public void toggleMultipleColorDoubleRent() {
        multipleColorRentSelectionPanel.toggleDoubleRent();
    }

    public void cancelMultipleColorRentSelection() {
        multipleColorRentSelectionPanel.cancelSelection();
    }

    public boolean isMultipleColorRentSelecting() {
        return multipleColorRentSelectionPanel.isSelecting();
    }

    public ActionCards getPendingMultipleColorRentCard() {
        return multipleColorRentSelectionPanel.getPendingCard();
    }

    public Player getSelectedMultipleColorRentTarget() {
        return multipleColorRentSelectionPanel.getSelectedTarget();
    }

    public void setSelectedMultipleColorRentTarget(Player selectedMultipleColorRentTarget) {
        multipleColorRentSelectionPanel.setSelectedTarget(selectedMultipleColorRentTarget);
    }

    public PropertyColor getSelectedMultipleColorRentColor() {
        return multipleColorRentSelectionPanel.getSelectedColor();
    }

    public void setSelectedMultipleColorRentColor(PropertyColor selectedMultipleColorRentColor) {
        multipleColorRentSelectionPanel.setSelectedColor(selectedMultipleColorRentColor);
    }

    public boolean canConfirmMultipleColorRent() {
        return multipleColorRentSelectionPanel.canConfirm();
    }

    public boolean isMultipleColorRentCancelClicked(double mouseX, double mouseY) {
        return multipleColorRentSelectionPanel.isCancelClicked(mouseX, mouseY);
    }

    public boolean isMultipleColorRentConfirmClicked(double mouseX, double mouseY) {
        return multipleColorRentSelectionPanel.isConfirmClicked(mouseX, mouseY);
    }

    public Player getClickedMultipleColorRentTarget(double mouseX, double mouseY) {
        return multipleColorRentSelectionPanel.getClickedTarget(mouseX, mouseY);
    }

    public PropertyColor getClickedMultipleColorRentColor(double mouseX, double mouseY) {
        return multipleColorRentSelectionPanel.getClickedColor(mouseX, mouseY);
    }

    public boolean isPaymentConfirmClicked(double mouseX, double mouseY) {
        return paymentSelectionPanel.isConfirmClicked(mouseX, mouseY);
    }

    public boolean isPaymentClearClicked(double mouseX, double mouseY) {
        return paymentSelectionPanel.isClearClicked(mouseX, mouseY);
    }

    public boolean isPaymentJustSayNoClicked(double mouseX, double mouseY) {
        return paymentSelectionPanel.isJustSayNoClicked(mouseX, mouseY);
    }

    public void clearPaymentSelection() {
        paymentSelectionPanel.clearSelection();
    }

    public ArrayList<Card> getSelectedPaymentCards() {
        return paymentSelectionPanel.getSelectedCards();
    }

    public boolean canConfirmPayment() {
        return paymentSelectionPanel.canConfirm();
    }

    public boolean handlePaymentCardClick(double mouseX, double mouseY) {
        return paymentSelectionPanel.handleCardClick(mouseX, mouseY);
    }

    public void showPlayerDetailPopup(int playerIndex) {
        playerDetailPopupPanel.showPlayer(playerIndex);
    }

    public boolean isPlayerDetailPopupShowing() {
        return playerDetailPopupPanel.isShowing();
    }

    public boolean isPlayerDetailPopupCloseClicked(double mouseX, double mouseY) {
        return playerDetailPopupPanel.isCloseClicked(mouseX, mouseY);
    }

    public boolean handlePlayerDetailPopupPageButtonClick(double mouseX, double mouseY) {
        return playerDetailPopupPanel.handlePageButtonClick(mouseX, mouseY);
    }

    public void closePlayerDetailPopup() {
        playerDetailPopupPanel.close();
    }

    public void startBuildingSelection(ActionCards card) {
        buildingSelectionPanel.startSelection(card);
        wildCardSelectionPanel.clearSelection();
    }

    public void cancelBuildingSelection() {
        buildingSelectionPanel.cancelSelection();
    }

    public boolean isBuildingSelecting() {
        return buildingSelectionPanel.isSelecting();
    }

    public ActionCards getPendingBuildingCard() {
        return buildingSelectionPanel.getPendingCard();
    }

    public boolean isBuildingCancelClicked(double mouseX, double mouseY) {
        return buildingSelectionPanel.isCancelClicked(mouseX, mouseY);
    }

    public PropertyColor getClickedBuildingColor(double mouseX, double mouseY) {
        return buildingSelectionPanel.getClickedColor(mouseX, mouseY);
    }

    public void startForcedDealSelection(ActionCards card) {
        forcedDealPanel.startSelection(card);
        wildCardSelectionPanel.clearSelection();
    }

    public void cancelForcedDealSelection() {
        forcedDealPanel.cancelSelection();
    }

    public boolean isForcedDealSelecting() {
        return forcedDealPanel.isSelecting();
    }

    public ActionCards getPendingForcedDealCard() {
        return forcedDealPanel.getPendingCard();
    }

    public boolean isForcedDealCancelClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isCancelClicked(mouseX, mouseY);
    }

    public boolean isForcedDealBackClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isBackClicked(mouseX, mouseY);
    }

    public boolean isForcedDealConfirmClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isConfirmClicked(mouseX, mouseY);
    }

    public boolean canConfirmForcedDeal() {
        return forcedDealPanel.canConfirm();
    }

    public Player getClickedForcedDealTarget(double mouseX, double mouseY) {
        return forcedDealPanel.getClickedTargetPlayer(mouseX, mouseY);
    }

    public PropertiesCards getClickedForcedDealMyProperty(double mouseX, double mouseY) {
        return forcedDealPanel.getClickedMyProperty(mouseX, mouseY);
    }

    public PropertiesCards getClickedForcedDealTargetProperty(double mouseX, double mouseY) {
        return forcedDealPanel.getClickedTargetProperty(mouseX, mouseY);
    }

    public void setSelectedForcedDealTarget(Player targetPlayer) {
        forcedDealPanel.setSelectedTargetPlayer(targetPlayer);
    }

    public void setSelectedForcedDealMyProperty(PropertiesCards card) {
        forcedDealPanel.setSelectedMyCard(card);
    }

    public void setSelectedForcedDealTargetProperty(PropertiesCards card) {
        forcedDealPanel.setSelectedTargetCard(card);
    }

    public Player getSelectedForcedDealTarget() {
        return forcedDealPanel.getSelectedTargetPlayer();
    }

    public PropertiesCards getSelectedForcedDealMyProperty() {
        return forcedDealPanel.getSelectedMyCard();
    }

    public PropertiesCards getSelectedForcedDealTargetProperty() {
        return forcedDealPanel.getSelectedTargetCard();
    }

    public void clearSelectedWildCard() {
        wildCardSelectionPanel.clearSelection();
    }

    public void showActionCardChoice(ActionCards card) {
        actionCardChoicePanel.show(card);
        wildCardSelectionPanel.clearSelection();
    }

    public void closeActionCardChoice() {
        actionCardChoicePanel.close();
    }

    public boolean isActionCardChoiceShowing() {
        return actionCardChoicePanel.isShowing();
    }

    public ActionCards getSelectedActionCardChoiceCard() {
        return actionCardChoicePanel.getSelectedCard();
    }

    public boolean isActionCardChoiceMoneyClicked(double mouseX, double mouseY) {
        return actionCardChoicePanel.isMoneyClicked(mouseX, mouseY);
    }

    public boolean isActionCardChoiceActionClicked(double mouseX, double mouseY) {
        return actionCardChoicePanel.isActionClicked(mouseX, mouseY);
    }

    public boolean isActionCardChoiceCancelClicked(double mouseX, double mouseY) {
        return actionCardChoicePanel.isCancelClicked(mouseX, mouseY);
    }

    public boolean canUseSelectedActionCardAsAction() {
        return actionCardChoicePanel.canUseAsAction();
    }
}
