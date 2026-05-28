package GUI;

import logic.Game;
import model.ActionCards;
import model.Card;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;

public class LocalGameClickActions implements GameClickActions {
    private final Game game;
    private final GameScreen gameScreen;
    private final MainMenu menu;

    public LocalGameClickActions(Game game, GameScreen gameScreen, MainMenu menu) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.menu = menu;
    }

    @Override
    public void playActionCardAsMoney(ActionCards card) {
        game.playActionCardAsMoney(card);
    }

    @Override
    public void useJustSayNo() {
        game.currentPaymentUseJustSayNo();
    }

    @Override
    public void finishPayment(ArrayList<Card> selectedCards) {
        game.finishCurrentPayment(selectedCards);
    }

    @Override
    public boolean canSubmitPayment() {
        return true;
    }

    @Override
    public void finishForcedDeal(ActionCards card,
                                 Player target,
                                 PropertiesCards myProperty,
                                 PropertiesCards targetProperty) {
        game.finishForcedDeal(card, target, myProperty, targetProperty);
    }

    @Override
    public void finishSlyDeal(ActionCards card, Player target, PropertiesCards stolenCard) {
        game.finishSlyDeal(card, target, stolenCard);
    }

    @Override
    public void finishMultipleColorRent(ActionCards card,
                                        Player target,
                                        PropertyColor color,
                                        boolean useDoubleRent) {
        game.finishMultipleColorRent(card, target, color, useDoubleRent);
    }

    @Override
    public void onMultipleColorRentTargetPicked(Player target) {
        gameScreen.showMultipleColorRentTargetDetail(target);
    }

    @Override
    public void onForcedDealTargetPicked(Player target) {
        gameScreen.showForcedDealTargetDetail(target);
    }

    @Override
    public void onDebtCollectorTargetPicked(ActionCards card, Player target) {
        gameScreen.setSelectedDebtCollectorTarget(target);
    }

    @Override
    public void finishDebtCollector(ActionCards card, Player target) {
        game.finishDebtCollector(card, target);
    }

    @Override
    public void finishDealBreaker(ActionCards card, Player target, ArrayList<PropertiesCards> selectedSet) {
        game.finishDealBreaker(card, target, selectedSet);
    }

    @Override
    public void onDealBreakerSetPicked(GameScreen.DealBreakerChoice choice) {
        gameScreen.showDealBreakerDetailChoice(choice);
    }

    @Override
    public void finishTwoColorRent(ActionCards card, PropertyColor color, boolean useDoubleRent) {
        game.finishTwoColorRent(card, color, useDoubleRent);
    }

    @Override
    public void finishBuilding(ActionCards card, PropertyColor color) {
        if (card.getActionCardType() == model.ActionCardType.HOUSE) {
            game.finishHouse(card, color);
        } else if (card.getActionCardType() == model.ActionCardType.HOTEL) {
            game.finishHotel(card, color);
        }
    }

    @Override
    public void setWildCardColor(PropertiesCards card, PropertyColor color) {
        card.setCurrentColor(color);
    }

    @Override
    public void playHandCard(Card card) {
        game.playCard(card);
    }

    @Override
    public void discardHandCard(Card card) {
        game.discard(card);
    }

    @Override
    public void recordWinIfNeeded() {
        if (game.getCurrentPlayer().checkIfWin()) {
            game.setWin(true);
        }
    }

    @Override
    public void endTurn() {
        game.guiEndTurn();
    }

    @Override
    public void returnToMenu() {
        gameScreen.setShow(false);
        menu.setShow(true);
    }

    @Override
    public void startActionCardFlow(ActionCards actionCard) {
        switch (actionCard.getActionCardType()) {
            case SLY_DEAL -> gameScreen.startSlyDealSelection(actionCard);
            case RENT_WITH_MULTIPLE_COLOR -> gameScreen.startMultipleColorRentSelection(actionCard);
            case HOUSE, HOTEL -> gameScreen.startBuildingSelection(actionCard);
            case FORCED_DEAL -> gameScreen.startForcedDealSelection(actionCard);
            case BIRTHDAY -> game.finishBirthday(actionCard);
            case DEBT_COLLECTOR -> gameScreen.startDebtCollectorSelection(actionCard);
            case DEAL_BREAKER -> gameScreen.startDealBreakerSelection(actionCard);
            case RENT_WITH_DARK_BLUE_AND_DARK_GREEN,
                 RENT_WITH_BROWN_AND_LIGHT_BLUE,
                 RENT_WITH_BLACK_AND_LIGHT_GREEN,
                 RENT_WITH_RED_AND_YELLOW,
                 RENT_WITH_ORANGE_AND_PINK -> gameScreen.startTwoColorRentSelection(actionCard);
            case PASS_GO -> game.finishPassGo(actionCard);
            default -> {
            }
        }
    }
}
