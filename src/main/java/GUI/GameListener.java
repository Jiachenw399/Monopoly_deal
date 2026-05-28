package GUI;

import javafx.scene.Scene;
import model.ActionCardType;
import model.ActionCards;
import model.Card;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

public class GameListener {
    private final MainMenu menu;
    private final GameScreen gameScreen;
    private final logic.Game game;

    public GameListener(MainMenu menu, GameScreen gameScreen, logic.Game game) {
        this.menu = menu;
        this.gameScreen = gameScreen;
        this.game = game;
    }

    public void addListener(Scene scene) {
        scene.setOnMouseClicked(event -> handleMouseClick(
                GuiScale.toLogical(event.getX()),
                GuiScale.toLogical(event.getY())
        ));
    }

    private void handleMouseClick(double x, double y) {
        if (!gameScreen.isShow()) {
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
            return;
        }

        if (gameScreen.handleBackgroundPageButtonClick(x, y)) {
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
            playActionCardAsMoney(selectedCard);
            gameScreen.closeActionCardChoice();
            return true;
        }

        if (gameScreen.isActionCardChoiceActionClicked(x, y)) {
            if (!gameScreen.canUseSelectedActionCardAsAction()) {
                return true;
            }

            gameScreen.closeActionCardChoice();
            handleActionCardClick(selectedCard);
            return true;
        }

        return true;
    }

    private void playActionCardAsMoney(ActionCards selectedCard) {
        Player currentPlayer = game.getCurrentPlayer();

        if (!currentPlayer.getHandCards().contains(selectedCard)) {
            return;
        }

        currentPlayer.getHandCards().remove(selectedCard);
        currentPlayer.getBankCards().add(selectedCard);
        currentPlayer.setUseCardTimes(currentPlayer.getUseCardTimes() + 1);
    }

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

    private boolean handlePaymentSelection(double x, double y) {
        if (!game.isPaymentSelecting()) {
            return false;
        }

        if (gameScreen.isPaymentJustSayNoClicked(x, y)) {
            game.currentPaymentUseJustSayNo();
            gameScreen.clearPaymentSelection();
            return true;
        }

        if (gameScreen.isPaymentClearClicked(x, y)) {
            gameScreen.clearPaymentSelection();
            return true;
        }

        if (gameScreen.isPaymentConfirmClicked(x, y)) {
            if (gameScreen.canConfirmPayment()) {
                game.finishCurrentPayment(gameScreen.getSelectedPaymentCards());
                gameScreen.clearPaymentSelection();
            }

            return true;
        }

        return gameScreen.handlePaymentCardClick(x, y);
    }

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

    private boolean handleForcedDealTargetClick(double x, double y) {
        Player targetPlayer = gameScreen.getClickedForcedDealTarget(x, y);

        if (targetPlayer != null) {
            gameScreen.showForcedDealTargetDetail(targetPlayer);
            return true;
        }

        return false;
    }

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
                game.finishForcedDeal(
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

    private boolean handleSlyDealSelection(double x, double y) {
        if (!gameScreen.isSlyDealSelecting()) {
            return false;
        }

        if (gameScreen.isSlyDealCancelClicked(x, y)) {
            gameScreen.cancelSlyDealSelection();
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

        GameScreen.SlyDealChoice choice = gameScreen.getClickedSlyDealChoice(x, y);

        if (choice != null) {
            game.finishSlyDeal(
                    gameScreen.getPendingSlyDealCard(),
                    choice.getTargetPlayer(),
                    choice.getSelectedCard()
            );

            gameScreen.cancelSlyDealSelection();
        }

        return true;
    }

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

    private boolean handleMultipleColorRentTargetClick(double x, double y) {
        Player targetPlayer = gameScreen.getClickedMultipleColorRentTarget(x, y);

        if (targetPlayer != null) {
            gameScreen.showMultipleColorRentTargetDetail(targetPlayer);
            return true;
        }

        return false;
    }

    private boolean handleMultipleColorRentConfirmOrCancel(double x, double y) {
        if (gameScreen.isMultipleColorRentCancelClicked(x, y)) {
            gameScreen.cancelMultipleColorRentSelection();
            return true;
        }

        if (gameScreen.isMultipleColorRentConfirmClicked(x, y)) {
            if (gameScreen.canConfirmMultipleColorRent()) {
                game.finishMultipleColorRent(
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

    private boolean handleDebtCollectorSelection(double x, double y) {
        if (!gameScreen.isDebtCollectorSelecting()) {
            return false;
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
                game.finishDebtCollector(
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
            gameScreen.setSelectedDebtCollectorTarget(targetPlayer);
            return true;
        }

        return true;
    }

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
                game.finishDealBreaker(
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
            gameScreen.showDealBreakerDetailChoice(choice);
            return true;
        }

        return true;
    }

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
            game.finishTwoColorRent(
                    gameScreen.getPendingTwoColorRentCard(),
                    selectedRentColor,
                    gameScreen.shouldUseDoubleRentForTwoColorRent()
            );

            gameScreen.cancelTwoColorRentSelection();
        }

        return true;
    }

    private boolean handleWildCardClick(double x, double y) {
        PropertyColor selectedColor = gameScreen.getClickedWildColorButton(x, y);

        if (selectedColor != null) {
            PropertiesCards selectedWildCard = gameScreen.getSelectedWildCard();

            if (selectedWildCard != null) {
                selectedWildCard.setCurrentColor(selectedColor);
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

            if (card.getActionCardType() == ActionCardType.HOUSE) {
                game.finishHouse(card, selectedColor);
            } else if (card.getActionCardType() == ActionCardType.HOTEL) {
                game.finishHotel(card, selectedColor);
            }

            gameScreen.cancelBuildingSelection();
        }

        return true;
    }

    private boolean handleButtonClick(double x, double y) {
        if (gameScreen.isEndTurnClicked(x, y)) {
            gameScreen.clearSelectedWildCard();
            gameScreen.closeActionCardChoice();
            game.guiEndTurn();
            return true;
        }

        if (gameScreen.isBackMenuClicked(x, y)) {
            gameScreen.clearSelectedWildCard();
            gameScreen.closeActionCardChoice();
            gameScreen.setShow(false);
            menu.setShow(true);
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

    private void handleHandCardClick(double x, double y) {
        int handIndex = gameScreen.getClickedHandCardIndex(x, y);

        if (handIndex == -1) {
            return;
        }

        Player currentPlayer = game.getCurrentPlayer();

        if (handIndex >= currentPlayer.getHandCards().size()) {
            return;
        }

        Card selectedCard = currentPlayer.getHandCards().get(handIndex);

        if (game.isDiscard()) {
            game.discard(selectedCard);
            return;
        }

        if (currentPlayer.getUseCardTimes() >= 3) {
            return;
        }

        if (selectedCard instanceof ActionCards actionCard) {
            gameScreen.showActionCardChoice(actionCard);
            return;
        }

        game.playCard(selectedCard);

        if (currentPlayer.checkIfWin()) {
            game.setWin(true);
        }
    }

    private void handleActionCardClick(Card selectedCard) {
        if (!(selectedCard instanceof ActionCards actionCard)) {
            return;
        }

        ActionCardType type = actionCard.getActionCardType();

        switch (type) {
            case SLY_DEAL -> {
                gameScreen.startSlyDealSelection(actionCard);
            }
            case RENT_WITH_MULTIPLE_COLOR -> {
                gameScreen.startMultipleColorRentSelection(actionCard);
            }
            case HOUSE, HOTEL -> {
                gameScreen.startBuildingSelection(actionCard);
            }
            case FORCED_DEAL -> {
                gameScreen.startForcedDealSelection(actionCard);
            }
            case BIRTHDAY -> {
                game.finishBirthday(actionCard);
            }
            case JUST_SAY_NO -> {
            }
            case DEBT_COLLECTOR -> {
                gameScreen.startDebtCollectorSelection(actionCard);
            }
            case DOUBLE_THE_RENT -> {
            }
            case DEAL_BREAKER -> {
                gameScreen.startDealBreakerSelection(actionCard);
            }
            case RENT_WITH_DARK_BLUE_AND_DARK_GREEN,
                 RENT_WITH_BROWN_AND_LIGHT_BLUE,
                 RENT_WITH_BLACK_AND_LIGHT_GREEN,
                 RENT_WITH_RED_AND_YELLOW,
                 RENT_WITH_ORANGE_AND_PINK -> {
                gameScreen.startTwoColorRentSelection(actionCard);
            }
            case PASS_GO -> {
                game.finishPassGo(actionCard);
            }
            default -> {

            }
        }
    }
}
