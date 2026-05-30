package GUI;

import javafx.scene.canvas.Canvas;
import logic.Game;
import logic.GameObserver;
import java.util.ArrayList;
import model.*;


public class GameScreen implements GameObserver {
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
    private double handStartX = 20;
    private double handAreaX = 20;
    private double handAreaWidth = 740;
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

    @Override
    public void onGameStateChanged() {
        resetViewedPlayerToCurrentPlayer();
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

    public boolean isSlyDealPrevPageClicked(double mouseX, double mouseY) {
        return slyDealPanel.isPrevPageClicked(mouseX, mouseY);
    }

    public boolean isSlyDealNextPageClicked(double mouseX, double mouseY) {
        return slyDealPanel.isNextPageClicked(mouseX, mouseY);
    }

    public void previousSlyDealPage() {
        slyDealPanel.previousPage();
    }

    public void nextSlyDealPage() {
        slyDealPanel.nextPage();
    }

    public Player getClickedSlyDealTarget(double mouseX, double mouseY) {
        return slyDealPanel.getClickedTargetPlayer(mouseX, mouseY);
    }

    public void showSlyDealTargetDetail(Player player) {
        slyDealPanel.showTargetDetail(player);
    }

    public Player getSlyDealDetailTarget() {
        return slyDealPanel.getDetailTargetPlayer();
    }

    public void setSelectedSlyDealTarget(Player player) {
        slyDealPanel.setSelectedTargetPlayer(player);
    }

    public Player getSelectedSlyDealTarget() {
        return slyDealPanel.getSelectedTargetPlayer();
    }

    public boolean isSlyDealDetailCloseClicked(double mouseX, double mouseY) {
        return slyDealPanel.isDetailCloseClicked(mouseX, mouseY);
    }

    public boolean isSlyDealDetailBackClicked(double mouseX, double mouseY) {
        return slyDealPanel.isDetailBackClicked(mouseX, mouseY);
    }

    public boolean isSlyDealDetailConfirmClicked(double mouseX, double mouseY) {
        return slyDealPanel.isDetailConfirmClicked(mouseX, mouseY);
    }

    public boolean handleSlyDealDetailPageButtonClick(double mouseX, double mouseY) {
        return slyDealPanel.handleDetailPageButtonClick(mouseX, mouseY);
    }

    public boolean isSlyDealBackClicked(double mouseX, double mouseY) {
        return slyDealPanel.isBackClicked(mouseX, mouseY);
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
        return backGroundScreen.getClickedWildCard(mouseX, mouseY);
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

    public boolean isDealBreakerPrevPageClicked(double mouseX, double mouseY) {
        return dealBreakerPanel.isPrevPageClicked(mouseX, mouseY);
    }

    public boolean isDealBreakerNextPageClicked(double mouseX, double mouseY) {
        return dealBreakerPanel.isNextPageClicked(mouseX, mouseY);
    }

    public void previousDealBreakerPage() {
        dealBreakerPanel.previousPage();
    }

    public void nextDealBreakerPage() {
        dealBreakerPanel.nextPage();
    }

    public boolean isForcedDealMyPrevPageClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isMyPrevPageClicked(mouseX, mouseY);
    }

    public boolean isForcedDealMyNextPageClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isMyNextPageClicked(mouseX, mouseY);
    }

    public boolean isForcedDealTargetPrevPageClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isTargetPrevPageClicked(mouseX, mouseY);
    }

    public boolean isForcedDealTargetNextPageClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isTargetNextPageClicked(mouseX, mouseY);
    }

    public void previousForcedDealMyPage() {
        forcedDealPanel.previousMyPage();
    }

    public void nextForcedDealMyPage() {
        forcedDealPanel.nextMyPage();
    }

    public void previousForcedDealTargetPage() {
        forcedDealPanel.previousTargetPage();
    }

    public void nextForcedDealTargetPage() {
        forcedDealPanel.nextTargetPage();
    }

    public Player getSelectedDebtCollectorTarget() {
        return debtCollectorPanel.getSelectedTarget();
    }

    public void setSelectedDebtCollectorTarget(Player player) {
        debtCollectorPanel.setSelectedTarget(player);
    }

    public boolean isDebtCollectorBackClicked(double mouseX, double mouseY) {
        return debtCollectorPanel.isBackClicked(mouseX, mouseY);
    }

    public boolean isDebtCollectorConfirmClicked(double mouseX, double mouseY) {
        return debtCollectorPanel.isConfirmClicked(mouseX, mouseY);
    }

    public boolean handleDebtCollectorDetailPageButtonClick(double mouseX, double mouseY) {
        return debtCollectorPanel.handleDetailPageButtonClick(mouseX, mouseY);
    }

    public boolean isDebtCollectorDetailCloseClicked(double mouseX, double mouseY) {
        return debtCollectorPanel.isDetailCloseClicked(mouseX, mouseY);
    }

    public boolean isMultipleColorRentDetailShowing() {
        return multipleColorRentSelectionPanel.isDetailShowing();
    }

    public void showMultipleColorRentTargetDetail(Player player) {
        multipleColorRentSelectionPanel.showTargetDetail(player);
    }

    public Player getMultipleColorRentDetailTarget() {
        return multipleColorRentSelectionPanel.getDetailTarget();
    }

    public boolean handleMultipleColorRentDetailPageButtonClick(double mouseX, double mouseY) {
        return multipleColorRentSelectionPanel.handleDetailPageButtonClick(mouseX, mouseY);
    }

    public boolean isMultipleColorRentDetailConfirmClicked(double mouseX, double mouseY) {
        return multipleColorRentSelectionPanel.isDetailConfirmClicked(mouseX, mouseY);
    }

    public boolean isMultipleColorRentDetailBackClicked(double mouseX, double mouseY) {
        return multipleColorRentSelectionPanel.isDetailBackClicked(mouseX, mouseY);
    }

    public void showDealBreakerDetailChoice(DealBreakerChoice choice) {
        dealBreakerPanel.showDetailChoice(choice);
    }

    public DealBreakerChoice getDealBreakerDetailChoice() {
        return dealBreakerPanel.getDetailChoice();
    }

    public boolean isDealBreakerDetailCloseClicked(double mouseX, double mouseY) {
        return dealBreakerPanel.isDetailCloseClicked(mouseX, mouseY);
    }

    public boolean isDealBreakerDetailBackClicked(double mouseX, double mouseY) {
        return dealBreakerPanel.isDetailBackClicked(mouseX, mouseY);
    }

    public boolean isDealBreakerDetailConfirmClicked(double mouseX, double mouseY) {
        return dealBreakerPanel.isDetailConfirmClicked(mouseX, mouseY);
    }

    public boolean handleDealBreakerDetailPageButtonClick(double mouseX, double mouseY) {
        return dealBreakerPanel.handleDetailPageButtonClick(mouseX, mouseY);
    }

    public void showForcedDealTargetDetail(Player player) {
        forcedDealPanel.showTargetDetail(player);
    }

    public Player getForcedDealDetailTarget() {
        return forcedDealPanel.getDetailTargetPlayer();
    }

    public boolean isForcedDealDetailCloseClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isDetailCloseClicked(mouseX, mouseY);
    }

    public boolean isForcedDealDetailBackClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isDetailBackClicked(mouseX, mouseY);
    }

    public boolean isForcedDealDetailConfirmClicked(double mouseX, double mouseY) {
        return forcedDealPanel.isDetailConfirmClicked(mouseX, mouseY);
    }

    public boolean handleForcedDealDetailPageButtonClick(double mouseX, double mouseY) {
        return forcedDealPanel.handleDetailPageButtonClick(mouseX, mouseY);
    }

    public boolean isMultipleColorRentDetailCloseClicked(double mouseX, double mouseY) {
        return multipleColorRentSelectionPanel.isDetailCloseClicked(mouseX, mouseY);
    }
}
