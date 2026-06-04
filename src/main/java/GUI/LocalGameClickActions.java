package GUI;

import logic.GameFacade;
import model.ActionCards;
import model.Card;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;

public class LocalGameClickActions extends GameClickActionAdapter {
    private final MainMenu menu;

    // Creates a LocalGameClickActions instance.
    public LocalGameClickActions(GameFacade game, GameScreen gameScreen, MainMenu menu) {
        super(game, gameScreen);
        this.menu = menu;
    }

    // Plays action card as money.
    @Override
    public void playActionCardAsMoney(ActionCards card) {
        game.playActionCardAsMoney(card);
    }

    // Runs use just say no.
    @Override
    public void useJustSayNo() {
        game.currentPaymentUseJustSayNo();
    }

    // Accepts the latest Just Say No.
    @Override
    public void passJustSayNo() {
        game.currentPaymentPassJustSayNo();
    }

    // Finishes payment.
    @Override
    public void finishPayment(ArrayList<Card> selectedCards) {
        game.finishCurrentPayment(selectedCards);
    }

    // Checks whether this can submit payment.
    @Override
    public boolean canSubmitPayment() {
        return true;
    }

    // Finishes forced deal.
    @Override
    public void finishForcedDeal(ActionCards card,
                                 Player target,
                                 PropertiesCards myProperty,
                                 PropertiesCards targetProperty) {
        game.finishForcedDeal(card, target, myProperty, targetProperty);
    }

    // Finishes sly deal.
    @Override
    public void finishSlyDeal(ActionCards card, Player target, PropertiesCards stolenCard) {
        game.finishSlyDeal(card, target, stolenCard);
    }

    // Finishes multiple color rent.
    @Override
    public void finishMultipleColorRent(ActionCards card,
                                        Player target,
                                        PropertyColor color,
                                        boolean useDoubleRent) {
        game.finishMultipleColorRent(card, target, color, useDoubleRent);
    }

    // Finishes debt collector.
    @Override
    public void finishDebtCollector(ActionCards card, Player target) {
        game.finishDebtCollector(card, target);
    }

    // Finishes deal breaker.
    @Override
    public void finishDealBreaker(ActionCards card, Player target, ArrayList<PropertiesCards> selectedSet) {
        game.finishDealBreaker(card, target, selectedSet);
    }

    // Finishes two color rent.
    @Override
    public void finishTwoColorRent(ActionCards card, PropertyColor color, boolean useDoubleRent) {
        game.finishTwoColorRent(card, color, useDoubleRent);
    }

    // Finishes building.
    @Override
    public void finishBuilding(ActionCards card, PropertyColor color) {
        if (isHouse(card)) {
            game.finishHouse(card, color);
        } else if (isHotel(card)) {
            game.finishHotel(card, color);
        }
    }

    // Runs set wild card color.
    @Override
    public void setWildCardColor(PropertiesCards card, PropertyColor color) {
        game.setPropertyColor(game.getCurrentPlayer(), card, color);
    }

    // Plays hand card.
    @Override
    public void playHandCard(Card card) {
        game.playCard(card);
    }

    // Discards hand card.
    @Override
    public void discardHandCard(Card card) {
        game.discard(card);
    }

    // Runs record win if needed.
    @Override
    public void recordWinIfNeeded() {
        if (game.getCurrentPlayer().checkIfWin()) {
            game.setWin(true);
        }
    }

    // Runs end turn.
    @Override
    public void endTurn() {
        game.guiEndTurn();
    }

    // Returns to to menu.
    @Override
    public void returnToMenu() {
        gameScreen.setShow(false);
        menu.setShow(true);
    }

    // Restarts the game.
    @Override
    public void restartGame() {
        game.restartGame();
    }

    // Finishes immediate action.
    @Override
    protected void finishImmediateAction(ActionCards actionCard) {
        if (actionCard.getActionCardType() == model.ActionCardType.BIRTHDAY) {
            game.finishBirthday(actionCard);
        } else if (actionCard.getActionCardType() == model.ActionCardType.PASS_GO) {
            game.finishPassGo(actionCard);
        }
    }

    // Checks whether house.
    private boolean isHouse(ActionCards card) {
        return card.getActionCardType() == model.ActionCardType.HOUSE;
    }

    // Checks whether hotel.
    private boolean isHotel(ActionCards card) {
        return card.getActionCardType() == model.ActionCardType.HOTEL;
    }
}
