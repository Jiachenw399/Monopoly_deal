package GUI;

import logic.GameFacade;
import logic.Game;
import model.ActionCards;
import model.Card;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

public class GameClickHandler {
    private final GameFacade game;
    private final GameScreen gameScreen;
    private final GameClickActions actions;

    // Creates a GameClickHandler instance.
    public GameClickHandler(GameFacade game, GameScreen gameScreen, GameClickActions actions) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.actions = actions;
    }

    // Handles mouse click.
    public void handleMouseClick(double x, double y) {
        if (gameScreen.isMuteClicked(x, y)) {
            gameScreen.toggleMute();
            return;
        }

        if (handleActionCardChoiceClick(x, y)) {
            return;
        }

        if (handleSelectionModeClick(x, y)) {
            return;
        }

        if (handlePlayerDetailPopupClick(x, y)) {
            return;
        }

        if (game.isWin()) {
            if (gameScreen.isPlayAgainClicked(x, y)) {
                actions.restartGame();
            }
            return;
        }

        if (gameScreen.handleBackgroundPageButtonClick(x, y)) {
            return;
        }

        if (isAIControlledTurnBlockingHumanInput()) {
            if (gameScreen.isBackMenuClicked(x, y)) {
                gameScreen.clearSelectedWildCard();
                gameScreen.closeActionCardChoice();
                actions.returnToMenu();
            }
            return;
        }

        if (handleWildCardClick(x, y)) {
            return;
        }

        if (handleButtonClick(x, y)) {
            return;
        }

        handleHandCardClick(x, y);
    }

    // Handles action card choice click.
    private boolean handleActionCardChoiceClick(double x, double y) {
        if (!gameScreen.isActionCardChoiceShowing()) {
            return false;
        }

        if (gameScreen.isActionCardChoiceCancelClicked(x, y)) {
            gameScreen.closeActionCardChoice();
            return true;
        }

        ActionCards selectedCard = gameScreen.getSelectedActionCardChoiceCard();

        if (selectedCard == null) {
            gameScreen.closeActionCardChoice();
            return true;
        }

        if (gameScreen.isActionCardChoiceMoneyClicked(x, y)) {
            actions.playActionCardAsMoney(selectedCard);
            gameScreen.closeActionCardChoice();
            return true;
        }

        if (gameScreen.isActionCardChoiceActionClicked(x, y)) {
            if (!gameScreen.canUseSelectedActionCardAsAction()) {
                return true;
            }

            gameScreen.closeActionCardChoice();
            actions.startActionCardFlow(selectedCard);
            return true;
        }

        return true;
    }

    // Handles player detail popup click.
    private boolean handlePlayerDetailPopupClick(double x, double y) {
        if (!gameScreen.isPlayerDetailPopupShowing()) {
            return false;
        }

        if (gameScreen.isPlayerDetailPopupCloseClicked(x, y)) {
            gameScreen.closePlayerDetailPopup();
            return true;
        }

        if (gameScreen.handlePlayerDetailPopupPageButtonClick(x, y)) {
            return true;
        }

        return true;
    }

    // Handles selection mode click.
    private boolean handleSelectionModeClick(double x, double y) {
        if (handlePaymentSelection(x, y)) {
            return true;
        }

        if (handleSlyDealSelection(x, y)) {
            return true;
        }

        if (handleForcedDealSelection(x, y)) {
            return true;
        }

        if (handleMultipleColorRentSelection(x, y)) {
            return true;
        }

        if (handleDebtCollectorSelection(x, y)) {
            return true;
        }

        if (handleDealBreakerSelection(x, y)) {
            return true;
        }

        if (handleBuildingSelection(x, y)) {
            return true;
        }

        return handleTwoColorRentSelection(x, y);
    }

    // Handles payment selection.
    private boolean handlePaymentSelection(double x, double y) {
        if (!game.isPaymentSelecting()) {
            return false;
        }

        if (!actions.canSubmitPayment()) {
            return true;
        }

        if (game.isCurrentPaymentWaitingForJustSayNoResponse()) {
            return handleJustSayNoResponseClick(x, y);
        }

        if (gameScreen.isPaymentJustSayNoClicked(x, y)) {
            actions.useJustSayNo();
            gameScreen.clearPaymentSelection();
            return true;
        }

        if (gameScreen.isPaymentClearClicked(x, y)) {
            gameScreen.clearPaymentSelection();
            return true;
        }

        if (gameScreen.isPaymentConfirmClicked(x, y)) {
            if (gameScreen.canConfirmPayment()) {
                actions.finishPayment(gameScreen.getSelectedPaymentCards());
                gameScreen.clearPaymentSelection();
            }

            return true;
        }

        return gameScreen.handlePaymentCardClick(x, y);
    }

    // Handles the Just Say No counter-response step.
    private boolean handleJustSayNoResponseClick(double x, double y) {
        if (gameScreen.isPaymentJustSayNoPassClicked(x, y)) {
            actions.passJustSayNo();
            gameScreen.clearPaymentSelection();
            return true;
        }

        if (gameScreen.isPaymentJustSayNoClicked(x, y)) {
            actions.useJustSayNo();
            gameScreen.clearPaymentSelection();
            return true;
        }

        return true;
    }

    // Handles forced deal selection.
    private boolean handleForcedDealSelection(double x, double y) {
        if (!gameScreen.isForcedDealSelecting()) {
            return false;
        }

        if (handleForcedDealDetailClick(x, y)) {
            return true;
        }

        if (handleForcedDealPageClick(x, y)) {
            return true;
        }

        if (handleForcedDealTargetClick(x, y)) {
            return true;
        }

        if (handleForcedDealPropertyClick(x, y)) {
            return true;
        }

        return handleForcedDealConfirmOrCancel(x, y);
    }

    // Handles forced deal detail click.
    private boolean handleForcedDealDetailClick(double x, double y) {
        if (gameScreen.isForcedDealDetailCloseClicked(x, y)
                || gameScreen.isForcedDealDetailBackClicked(x, y)) {
            gameScreen.showForcedDealTargetDetail(null);
            return true;
        }

        if (gameScreen.isForcedDealDetailConfirmClicked(x, y)) {
            Player target = gameScreen.getForcedDealDetailTarget();

            if (target != null) {
                gameScreen.setSelectedForcedDealTarget(target);
                gameScreen.showForcedDealTargetDetail(null);
            }

            return true;
        }

        return gameScreen.handleForcedDealDetailPageButtonClick(x, y);
    }

    // Handles forced deal page click.
    private boolean handleForcedDealPageClick(double x, double y) {
        if (gameScreen.isForcedDealMyPrevPageClicked(x, y)) {
            gameScreen.previousForcedDealMyPage();
            return true;
        }

        if (gameScreen.isForcedDealMyNextPageClicked(x, y)) {
            gameScreen.nextForcedDealMyPage();
            return true;
        }

        if (gameScreen.isForcedDealTargetPrevPageClicked(x, y)) {
            gameScreen.previousForcedDealTargetPage();
            return true;
        }

        if (gameScreen.isForcedDealTargetNextPageClicked(x, y)) {
            gameScreen.nextForcedDealTargetPage();
            return true;
        }

        return false;
    }

    // Handles forced deal target click.
    private boolean handleForcedDealTargetClick(double x, double y) {
        Player targetPlayer = gameScreen.getClickedForcedDealTarget(x, y);

        if (targetPlayer != null) {
            actions.onForcedDealTargetPicked(targetPlayer);
            return true;
        }

        return false;
    }

    // Handles forced deal property click.
    private boolean handleForcedDealPropertyClick(double x, double y) {
        PropertiesCards myCard = gameScreen.getClickedForcedDealMyProperty(x, y);

        if (myCard != null) {
            gameScreen.setSelectedForcedDealMyProperty(myCard);
            return true;
        }

        PropertiesCards targetCard = gameScreen.getClickedForcedDealTargetProperty(x, y);

        if (targetCard != null) {
            gameScreen.setSelectedForcedDealTargetProperty(targetCard);
            return true;
        }

        return false;
    }

    // Handles forced deal confirm or cancel.
    private boolean handleForcedDealConfirmOrCancel(double x, double y) {
        if (gameScreen.isForcedDealCancelClicked(x, y)) {
            gameScreen.cancelForcedDealSelection();
            return true;
        }

        if (gameScreen.isForcedDealBackClicked(x, y)) {
            gameScreen.setSelectedForcedDealTarget(null);
            return true;
        }

        if (gameScreen.isForcedDealConfirmClicked(x, y)) {
            if (gameScreen.canConfirmForcedDeal()) {
                actions.finishForcedDeal(
                        gameScreen.getPendingForcedDealCard(),
                        gameScreen.getSelectedForcedDealTarget(),
                        gameScreen.getSelectedForcedDealMyProperty(),
                        gameScreen.getSelectedForcedDealTargetProperty()
                );

                gameScreen.cancelForcedDealSelection();
            }

            return true;
        }

        return true;
    }

    // Handles sly deal selection.
    private boolean handleSlyDealSelection(double x, double y) {
        if (!gameScreen.isSlyDealSelecting()) {
            return false;
        }

        if (gameScreen.isSlyDealDetailCloseClicked(x, y)
                || gameScreen.isSlyDealDetailBackClicked(x, y)) {
            gameScreen.showSlyDealTargetDetail(null);
            return true;
        }

        if (gameScreen.isSlyDealBackClicked(x, y)) {
            gameScreen.setSelectedSlyDealTarget(null);
            return true;
        }

        if (gameScreen.isSlyDealCancelClicked(x, y)) {
            gameScreen.cancelSlyDealSelection();
            return true;
        }

        if (gameScreen.isSlyDealDetailConfirmClicked(x, y)) {
            Player target = gameScreen.getSlyDealDetailTarget();

            if (target != null) {
                gameScreen.setSelectedSlyDealTarget(target);
                gameScreen.showSlyDealTargetDetail(null);
            }

            return true;
        }

        if (gameScreen.handleSlyDealDetailPageButtonClick(x, y)) {
            return true;
        }

        if (gameScreen.isSlyDealPrevPageClicked(x, y)) {
            gameScreen.previousSlyDealPage();
            return true;
        }

        if (gameScreen.isSlyDealNextPageClicked(x, y)) {
            gameScreen.nextSlyDealPage();
            return true;
        }

        Player targetPlayer = gameScreen.getClickedSlyDealTarget(x, y);

        if (targetPlayer != null) {
            gameScreen.showSlyDealTargetDetail(targetPlayer);
            return true;
        }

        GameScreen.SlyDealChoice choice = gameScreen.getClickedSlyDealChoice(x, y);

        if (choice != null) {
            actions.finishSlyDeal(
                    gameScreen.getPendingSlyDealCard(),
                    choice.getTargetPlayer(),
                    choice.getSelectedCard()
            );

            gameScreen.cancelSlyDealSelection();
        }

        return true;
    }

    // Handles multiple color rent selection.
    private boolean handleMultipleColorRentSelection(double x, double y) {
        if (!gameScreen.isMultipleColorRentSelecting()) {
            return false;
        }

        if (handleMultipleColorRentDetailClick(x, y)) {
            return true;
        }

        if (handleMultipleColorRentOptionClick(x, y)) {
            return true;
        }

        if (handleMultipleColorRentTargetClick(x, y)) {
            return true;
        }

        return handleMultipleColorRentConfirmOrCancel(x, y);
    }

    // Handles multiple color rent detail click.
    private boolean handleMultipleColorRentDetailClick(double x, double y) {
        if (gameScreen.isMultipleColorRentDetailCloseClicked(x, y)
                || gameScreen.isMultipleColorRentDetailBackClicked(x, y)) {
            gameScreen.showMultipleColorRentTargetDetail(null);
            return true;
        }

        if (gameScreen.isMultipleColorRentDetailConfirmClicked(x, y)) {
            Player target = gameScreen.getMultipleColorRentDetailTarget();

            if (target != null) {
                gameScreen.setSelectedMultipleColorRentTarget(target);
                gameScreen.showMultipleColorRentTargetDetail(null);
            }

            return true;
        }

        return gameScreen.handleMultipleColorRentDetailPageButtonClick(x, y);
    }

    // Handles multiple color rent option click.
    private boolean handleMultipleColorRentOptionClick(double x, double y) {
        if (gameScreen.isMultipleColorDoubleRentClicked(x, y)) {
            gameScreen.toggleMultipleColorDoubleRent();
            return true;
        }

        PropertyColor selectedColor = gameScreen.getClickedMultipleColorRentColor(x, y);

        if (selectedColor != null) {
            gameScreen.setSelectedMultipleColorRentColor(selectedColor);
            return true;
        }

        return false;
    }

    // Handles multiple color rent target click.
    private boolean handleMultipleColorRentTargetClick(double x, double y) {
        Player targetPlayer = gameScreen.getClickedMultipleColorRentTarget(x, y);

        if (targetPlayer != null) {
            actions.onMultipleColorRentTargetPicked(targetPlayer);
            return true;
        }

        return false;
    }

    // Handles multiple color rent confirm or cancel.
    private boolean handleMultipleColorRentConfirmOrCancel(double x, double y) {
        if (gameScreen.isMultipleColorRentCancelClicked(x, y)) {
            gameScreen.cancelMultipleColorRentSelection();
            return true;
        }

        if (gameScreen.isMultipleColorRentConfirmClicked(x, y)) {
            if (gameScreen.canConfirmMultipleColorRent()) {
                actions.finishMultipleColorRent(
                        gameScreen.getPendingMultipleColorRentCard(),
                        gameScreen.getSelectedMultipleColorRentTarget(),
                        gameScreen.getSelectedMultipleColorRentColor(),
                        gameScreen.shouldUseDoubleRentForMultipleColorRent()
                );

                gameScreen.cancelMultipleColorRentSelection();
            }

            return true;
        }

        return true;
    }

    // Handles debt collector selection.
    private boolean handleDebtCollectorSelection(double x, double y) {
        if (!gameScreen.isDebtCollectorSelecting()) {
            return false;
        }

        if (gameScreen.isDebtCollectorDetailCloseClicked(x, y)) {
            gameScreen.setSelectedDebtCollectorTarget(null);
            return true;
        }

        if (gameScreen.isDebtCollectorCancelClicked(x, y)) {
            gameScreen.cancelDebtCollectorSelection();
            return true;
        }

        if (gameScreen.isDebtCollectorBackClicked(x, y)) {
            gameScreen.setSelectedDebtCollectorTarget(null);
            return true;
        }

        if (gameScreen.isDebtCollectorConfirmClicked(x, y)) {
            Player selectedTarget = gameScreen.getSelectedDebtCollectorTarget();

            if (selectedTarget != null) {
                actions.finishDebtCollector(
                        gameScreen.getPendingDebtCollectorCard(),
                        selectedTarget
                );
                gameScreen.cancelDebtCollectorSelection();
            }

            return true;
        }

        if (gameScreen.handleDebtCollectorDetailPageButtonClick(x, y)) {
            return true;
        }

        Player targetPlayer = gameScreen.getClickedDebtCollectorTarget(x, y);

        if (targetPlayer != null) {
            actions.onDebtCollectorTargetPicked(gameScreen.getPendingDebtCollectorCard(), targetPlayer);
            return true;
        }

        return true;
    }

    // Handles deal breaker selection.
    private boolean handleDealBreakerSelection(double x, double y) {
        if (!gameScreen.isDealBreakerSelecting()) {
            return false;
        }

        if (gameScreen.isDealBreakerDetailCloseClicked(x, y)
                || gameScreen.isDealBreakerDetailBackClicked(x, y)) {
            gameScreen.showDealBreakerDetailChoice(null);
            return true;
        }

        if (gameScreen.isDealBreakerCancelClicked(x, y)) {
            gameScreen.cancelDealBreakerSelection();
            return true;
        }

        if (gameScreen.isDealBreakerDetailConfirmClicked(x, y)) {
            GameScreen.DealBreakerChoice choice = gameScreen.getDealBreakerDetailChoice();

            if (choice != null) {
                actions.finishDealBreaker(
                        gameScreen.getPendingDealBreakerCard(),
                        choice.getTargetPlayer(),
                        choice.getSelectedSet()
                );

                gameScreen.cancelDealBreakerSelection();
            }

            return true;
        }

        if (gameScreen.handleDealBreakerDetailPageButtonClick(x, y)) {
            return true;
        }

        if (gameScreen.isDealBreakerPrevPageClicked(x, y)) {
            gameScreen.previousDealBreakerPage();
            return true;
        }

        if (gameScreen.isDealBreakerNextPageClicked(x, y)) {
            gameScreen.nextDealBreakerPage();
            return true;
        }

        GameScreen.DealBreakerChoice choice = gameScreen.getClickedDealBreakerChoice(x, y);

        if (choice != null) {
            actions.onDealBreakerSetPicked(choice);
            return true;
        }

        return true;
    }

    // Handles two color rent selection.
    private boolean handleTwoColorRentSelection(double x, double y) {
        if (!gameScreen.isTwoColorRentSelecting()) {
            return false;
        }

        if (gameScreen.isTwoColorDoubleRentClicked(x, y)) {
            gameScreen.toggleTwoColorDoubleRent();
            return true;
        }

        if (gameScreen.isTwoColorRentCancelClicked(x, y)) {
            gameScreen.cancelTwoColorRentSelection();
            return true;
        }

        PropertyColor selectedRentColor = gameScreen.getClickedTwoColorRentColor(x, y);

        if (selectedRentColor != null) {
            actions.finishTwoColorRent(
                    gameScreen.getPendingTwoColorRentCard(),
                    selectedRentColor,
                    gameScreen.shouldUseDoubleRentForTwoColorRent()
            );

            gameScreen.cancelTwoColorRentSelection();
        }

        return true;
    }

    // Handles wild card click.
    private boolean handleWildCardClick(double x, double y) {
        PropertyColor selectedColor = gameScreen.getClickedWildColorButton(x, y);

        if (selectedColor != null) {
            PropertiesCards selectedWildCard = gameScreen.getSelectedWildCard();

            if (selectedWildCard != null) {
                actions.setWildCardColor(selectedWildCard, selectedColor);
            }

            gameScreen.clearSelectedWildCard();
            return true;
        }

        PropertiesCards clickedWildCard = gameScreen.getClickedWildCard(x, y);

        if (clickedWildCard != null) {
            gameScreen.setSelectedWildCard(clickedWildCard);
            return true;
        }

        return false;
    }

    // Handles building selection.
    private boolean handleBuildingSelection(double x, double y) {
        if (!gameScreen.isBuildingSelecting()) {
            return false;
        }

        if (gameScreen.isBuildingCancelClicked(x, y)) {
            gameScreen.cancelBuildingSelection();
            return true;
        }

        PropertyColor selectedColor = gameScreen.getClickedBuildingColor(x, y);

        if (selectedColor != null) {
            ActionCards card = gameScreen.getPendingBuildingCard();
            actions.finishBuilding(card, selectedColor);
            gameScreen.cancelBuildingSelection();
        }

        return true;
    }

    // Handles button click.
    private boolean handleButtonClick(double x, double y) {
        if (gameScreen.isEndTurnClicked(x, y)) {
            gameScreen.clearSelectedWildCard();
            gameScreen.closeActionCardChoice();
            actions.endTurn();
            return true;
        }

        if (gameScreen.isBackMenuClicked(x, y)) {
            gameScreen.clearSelectedWildCard();
            gameScreen.closeActionCardChoice();
            actions.returnToMenu();
            return true;
        }

        int viewedPlayerIndex = gameScreen.getClickedPlayerViewButtonIndex(x, y);

        if (viewedPlayerIndex != -1) {
            gameScreen.clearSelectedWildCard();
            gameScreen.closeActionCardChoice();
            gameScreen.showPlayerDetailPopup(viewedPlayerIndex);
            return true;
        }

        return false;
    }

    // Handles hand card click.
    private void handleHandCardClick(double x, double y) {
        int handIndex = gameScreen.getClickedHandCardIndex(x, y);

        if (handIndex == -1) {
            return;
        }

        Card selectedCard = gameScreen.getViewedHandCard(handIndex);

        if (selectedCard == null) {
            return;
        }

        Player currentPlayer = game.getCurrentPlayer();

        if (!currentPlayer.getHandCards().contains(selectedCard)) {
            return;
        }

        if (game.isDiscard()) {
            actions.discardHandCard(selectedCard);
            return;
        }

        if (currentPlayer.getUseCardTimes() >= 3) {
            return;
        }

        if (selectedCard instanceof ActionCards actionCard) {
            gameScreen.showActionCardChoice(actionCard);
            return;
        }

        actions.playHandCard(selectedCard);
    }

    // Blocks human gameplay clicks while an AI player owns the current turn.
    private boolean isAIControlledTurnBlockingHumanInput() {
        if (!game.getCurrentPlayer().isAI()) {
            return false;
        }

        if (!game.isPaymentSelecting()) {
            return true;
        }

        Game.PaymentRequest request = game.getCurrentPaymentRequest();
        return request == null || request.getPayer().isAI();
    }
}
