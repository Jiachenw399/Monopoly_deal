package network;

import GUI.GameClickActionAdapter;
import GUI.GameScreen;
import logic.Game;
import logic.GameFacade;
import model.ActionCards;
import model.Card;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;
import java.util.function.BiConsumer;

/**
 * Network commit adapter for {@link GUI.GameClickHandler} in {@link OnlinePlayWindow}.
 */
public class OnlineGameClickActions extends GameClickActionAdapter {
    private int myPlayerId;
    private final BiConsumer<String, String> send;
    private final Runnable onExitToMenu;

    public OnlineGameClickActions(GameFacade game,
                                  GameScreen gameScreen,
                                  BiConsumer<String, String> send,
                                  Runnable onExitToMenu) {
        super(game, gameScreen);
        this.send = send;
        this.onExitToMenu = onExitToMenu;
    }

    public void setMyPlayerId(int myPlayerId) {
        this.myPlayerId = myPlayerId;
    }

    @Override
    public void playActionCardAsMoney(ActionCards card) {
        send.accept("PLAY_AS_MONEY", handNumber(card));
    }

    @Override
    public void useJustSayNo() {
        send.accept("JUST_SAY_NO", "");
    }

    @Override
    public void finishPayment(ArrayList<Card> selectedCards) {
        send.accept("PAY", paymentBody(selectedCards));
    }

    @Override
    public boolean canSubmitPayment() {
        if (!game.isPaymentSelecting()) {
            return false;
        }
        return playerNumber(game.getCurrentPaymentRequest().getPayer()) == myPlayerId;
    }

    @Override
    public void finishForcedDeal(ActionCards card,
                                 Player target,
                                 PropertiesCards myProperty,
                                 PropertiesCards targetProperty) {
        send.accept("FORCED_DEAL", handNumber(card)
                + " " + playerNumber(target)
                + " " + propertyNumber(game.getCurrentPlayer(), myProperty)
                + " " + propertyNumber(target, targetProperty));
    }

    @Override
    public void finishSlyDeal(ActionCards card, Player target, PropertiesCards stolenCard) {
        send.accept("SLY", handNumber(card)
                + " " + playerNumber(target)
                + " " + propertyNumber(target, stolenCard));
    }

    @Override
    public void finishMultipleColorRent(ActionCards card,
                                        Player target,
                                        PropertyColor color,
                                        boolean useDoubleRent) {
        send.accept("RENT_ANY", handNumber(card)
                + " " + playerNumber(target)
                + " " + color.name()
                + doubleToken(useDoubleRent));
    }

    @Override
    public void onMultipleColorRentTargetPicked(Player target) {
        gameScreen.setSelectedMultipleColorRentTarget(target);
    }

    @Override
    public void onForcedDealTargetPicked(Player target) {
        gameScreen.setSelectedForcedDealTarget(target);
    }

    @Override
    public void onDebtCollectorTargetPicked(ActionCards card, Player target) {
        send.accept("DEBT", handNumber(card) + " " + playerNumber(target));
        gameScreen.cancelDebtCollectorSelection();
    }

    @Override
    public void finishDebtCollector(ActionCards card, Player target) {
        // Online uses immediate send in onDebtCollectorTargetPicked.
    }

    @Override
    public void onDealBreakerSetPicked(GameScreen.DealBreakerChoice choice) {
        if (choice != null && !choice.getSelectedSet().isEmpty()) {
            send.accept("DEAL_BREAKER", handNumber(gameScreen.getPendingDealBreakerCard())
                    + " " + playerNumber(choice.getTargetPlayer())
                    + " " + choice.getSelectedSet().getFirst().getCurrentColor().name());
            gameScreen.cancelDealBreakerSelection();
        }
    }

    @Override
    public void finishDealBreaker(ActionCards card, Player target, ArrayList<PropertiesCards> selectedSet) {
        // Online confirms in onDealBreakerSetPicked.
    }

    @Override
    public void finishTwoColorRent(ActionCards card, PropertyColor color, boolean useDoubleRent) {
        send.accept("RENT", handNumber(card) + " " + color.name() + doubleToken(useDoubleRent));
    }

    @Override
    public void finishBuilding(ActionCards card, PropertyColor color) {
        send.accept(card.getActionCardType().name(), handNumber(card) + " " + color.name());
    }

    @Override
    public void setWildCardColor(PropertiesCards card, PropertyColor color) {
        send.accept("SET_PROPERTY_COLOR",
                propertyNumber(game.getCurrentPlayer(), card) + " " + color.name());
    }

    @Override
    public void playHandCard(Card card) {
        send.accept("PLAY_CARD", handNumber(card));
    }

    @Override
    public void discardHandCard(Card card) {
        send.accept("DISCARD", handNumber(card));
    }

    @Override
    public void recordWinIfNeeded() {
        // Server drives win state.
    }

    @Override
    public void endTurn() {
        if (game.getCurrentPlayerIndex() + 1 == myPlayerId) {
            send.accept("END_TURN", "");
        }
    }

    @Override
    public void returnToMenu() {
        onExitToMenu.run();
    }

    @Override
    protected void finishImmediateAction(ActionCards actionCard) {
        switch (actionCard.getActionCardType()) {
            case BIRTHDAY -> send.accept("BIRTHDAY", handNumber(actionCard));
            case PASS_GO -> send.accept("PASS_GO", handNumber(actionCard));
            default -> {
            }
        }
    }

    private String handNumber(Card card) {
        return Integer.toString(game.getCurrentPlayer().getHandCards().indexOf(card) + 1);
    }

    private int playerNumber(Player player) {
        return game.getPlayers().indexOf(player) + 1;
    }

    private int propertyNumber(Player player, PropertiesCards card) {
        return player.getPropertyCards().indexOf(card) + 1;
    }

    private String paymentBody(ArrayList<Card> selectedCards) {
        Game.PaymentRequest request = game.getCurrentPaymentRequest();
        Player payer = request.getPayer();
        ArrayList<String> tokens = new ArrayList<>();
        for (Card card : selectedCards) {
            int bankIndex = payer.getBankCards().indexOf(card);
            if (bankIndex >= 0) {
                tokens.add("B" + (bankIndex + 1));
                continue;
            }
            int propertyIndex = payer.getPropertyCards().indexOf(card);
            if (propertyIndex >= 0) {
                tokens.add("P" + (propertyIndex + 1));
            }
        }
        return String.join(" ", tokens);
    }

    private String doubleToken(boolean useDoubleRent) {
        return useDoubleRent ? " DOUBLE" : "";
    }
}
