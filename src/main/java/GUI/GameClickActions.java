package GUI;

import model.ActionCards;
import model.Card;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;

public interface GameClickActions {
    // Plays an action card as money.
    void playActionCardAsMoney(ActionCards card);

    // Uses Just Say No for the current payment.
    void useJustSayNo();

    // Finishes a payment with selected cards.
    void finishPayment(ArrayList<Card> selectedCards);

    // Checks whether the payment can be submitted.
    boolean canSubmitPayment();

    // Finishes a Forced Deal action.
    void finishForcedDeal(ActionCards card,
                          Player target,
                          PropertiesCards myProperty,
                          PropertiesCards targetProperty);

    // Finishes a Sly Deal action.
    void finishSlyDeal(ActionCards card, Player target, PropertiesCards stolenCard);

    // Finishes a multi-color rent action.
    void finishMultipleColorRent(ActionCards card,
                                 Player target,
                                 PropertyColor color,
                                 boolean useDoubleRent);

    // Handles choosing a target for multi-color rent.
    void onMultipleColorRentTargetPicked(Player target);

    // Handles choosing a target for Forced Deal.
    void onForcedDealTargetPicked(Player target);

    // Handles choosing a target for Debt Collector.
    void onDebtCollectorTargetPicked(ActionCards card, Player target);

    // Finishes a Debt Collector action.
    void finishDebtCollector(ActionCards card, Player target);

    // Handles choosing a Deal Breaker set.
    void onDealBreakerSetPicked(GameScreen.DealBreakerChoice choice);

    // Finishes a Deal Breaker action.
    void finishDealBreaker(ActionCards card, Player target, java.util.ArrayList<PropertiesCards> selectedSet);

    // Finishes a two-color rent action.
    void finishTwoColorRent(ActionCards card, PropertyColor color, boolean useDoubleRent);

    // Finishes a building action.
    void finishBuilding(ActionCards card, PropertyColor color);

    // Sets the selected wild card color.
    void setWildCardColor(PropertiesCards card, PropertyColor color);

    // Plays a hand card.
    void playHandCard(Card card);

    // Discards a hand card.
    void discardHandCard(Card card);

    // Records a win when the current player satisfies the win condition.
    void recordWinIfNeeded();

    // Ends the current turn.
    void endTurn();

    // Returns to the menu.
    void returnToMenu();

    // Starts the flow for an action card.
    void startActionCardFlow(ActionCards actionCard);
}
