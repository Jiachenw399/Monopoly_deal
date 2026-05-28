package GUI;

import model.ActionCards;
import model.Card;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;

/**
 * Commit hooks for {@link GameClickHandler}; local and online modes implement differently.
 */
public interface GameClickActions {
    void playActionCardAsMoney(ActionCards card);

    void useJustSayNo();

    void finishPayment(ArrayList<Card> selectedCards);

    boolean canSubmitPayment();

    void finishForcedDeal(ActionCards card,
                          Player target,
                          PropertiesCards myProperty,
                          PropertiesCards targetProperty);

    void finishSlyDeal(ActionCards card, Player target, PropertiesCards stolenCard);

    void finishMultipleColorRent(ActionCards card,
                                 Player target,
                                 PropertyColor color,
                                 boolean useDoubleRent);

    void onMultipleColorRentTargetPicked(Player target);

    void onForcedDealTargetPicked(Player target);

    void onDebtCollectorTargetPicked(ActionCards card, Player target);

    void finishDebtCollector(ActionCards card, Player target);

    void onDealBreakerSetPicked(GameScreen.DealBreakerChoice choice);

    void finishDealBreaker(ActionCards card, Player target, java.util.ArrayList<PropertiesCards> selectedSet);

    void finishTwoColorRent(ActionCards card, PropertyColor color, boolean useDoubleRent);

    void finishBuilding(ActionCards card, PropertyColor color);

    void setWildCardColor(PropertiesCards card, PropertyColor color);

    void playHandCard(Card card);

    void discardHandCard(Card card);

    void recordWinIfNeeded();

    void endTurn();

    void returnToMenu();

    void startActionCardFlow(ActionCards actionCard);
}
