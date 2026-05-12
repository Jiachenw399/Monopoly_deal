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
        scene.setOnMouseClicked(event -> handleMouseClick(event.getX(), event.getY()));
    }

    private void handleMouseClick(double x, double y) {
        if (!gameScreen.isShow()) {
            return;
        }

        if (handleSelectionModeClick(x, y)) {
            return;
        }

        if (game.isWin()) {
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

    private boolean handleSelectionModeClick(double x, double y) {
        if (handlePaymentSelection(x, y)) {
            return true;
        }

        if (handleSlyDealSelection(x, y)) {
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

    private boolean handleSlyDealSelection(double x, double y) {
        if (!gameScreen.isSlyDealSelecting()) {
            return false;
        }

        if (gameScreen.isSlyDealCancelClicked(x, y)) {
            gameScreen.cancelSlyDealSelection();
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

        if (gameScreen.isMultipleColorRentCancelClicked(x, y)) {
            gameScreen.cancelMultipleColorRentSelection();
            return true;
        }

        Player targetPlayer = gameScreen.getClickedMultipleColorRentTarget(x, y);

        if (targetPlayer != null) {
            gameScreen.setSelectedMultipleColorRentTarget(targetPlayer);
            return true;
        }

        if (gameScreen.isMultipleColorDoubleRentClicked(x, y)) {
            gameScreen.toggleMultipleColorDoubleRent();
            return true;
        }

        PropertyColor selectedColor = gameScreen.getClickedMultipleColorRentColor(x, y);

        if (selectedColor != null) {
            gameScreen.setSelectedMultipleColorRentColor(selectedColor);
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

        Player targetPlayer = gameScreen.getClickedDebtCollectorTarget(x, y);

        if (targetPlayer != null) {
            game.finishDebtCollector(
                    gameScreen.getPendingDebtCollectorCard(),
                    targetPlayer
            );

            gameScreen.cancelDebtCollectorSelection();
        }

        return true;
    }

    private boolean handleDealBreakerSelection(double x, double y) {
        if (!gameScreen.isDealBreakerSelecting()) {
            return false;
        }

        if (gameScreen.isDealBreakerCancelClicked(x, y)) {
            gameScreen.cancelDealBreakerSelection();
            return true;
        }

        GameScreen.DealBreakerChoice choice = gameScreen.getClickedDealBreakerChoice(x, y);

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

            gameScreen.setSelectedWildCard(null);
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
            game.guiEndTurn();
            return true;
        }

        if (gameScreen.isBackMenuClicked(x, y)) {
            gameScreen.setShow(false);
            menu.setShow(true);
            return true;
        }

        int viewedPlayerIndex = gameScreen.getClickedPlayerViewButtonIndex(x, y);

        if (viewedPlayerIndex != -1) {
            gameScreen.setViewedPlayerIndex(viewedPlayerIndex);
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

        if (handleActionCardClick(selectedCard)) {
            return;
        }

        game.playCard(selectedCard);

        if (currentPlayer.checkIfWin()) {
            game.setWin(true);
        }
    }

    private boolean handleActionCardClick(Card selectedCard) {
        if (!(selectedCard instanceof ActionCards actionCard)) {
            return false;
        }

        ActionCardType type = actionCard.getActionCardType();

        return switch (type) {
            case SLY_DEAL -> {
                gameScreen.startSlyDealSelection(actionCard);
                yield true;
            }
            case RENT_WITH_MULTIPLE_COLOR -> {
                gameScreen.startMultipleColorRentSelection(actionCard);
                yield true;
            }
            case HOUSE -> {
                gameScreen.startBuildingSelection(actionCard);
                yield true;
            }
            case FORCED_DEAL -> false;
            case BIRTHDAY -> {
                game.finishBirthday(actionCard);
                yield true;
            }
            case JUST_SAY_NO -> false;
            case DEBT_COLLECTOR -> {
                gameScreen.startDebtCollectorSelection(actionCard);
                yield true;
            }
            case DOUBLE_THE_RENT -> false;
            case HOTEL -> {
                gameScreen.startBuildingSelection(actionCard);
                yield true;
            }
            case DEAL_BREAKER -> {
                gameScreen.startDealBreakerSelection(actionCard);
                yield true;
            }
            case RENT_WITH_DARK_BLUE_AND_DARK_GREEN, RENT_WITH_BROWN_AND_LIGHT_BLUE, RENT_WITH_BLACK_AND_LIGHT_GREEN,
                 RENT_WITH_RED_AND_YELLOW, RENT_WITH_ORANGE_AND_PINK -> {
                gameScreen.startTwoColorRentSelection(actionCard);
                yield true;
            }
            case PASS_GO -> false;
            default -> false;
        };
    }
}