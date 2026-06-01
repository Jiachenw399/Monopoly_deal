package network;

import GUI.GameClickActionAdapter;
import GUI.GameScreen;
import logic.Game;
import logic.GameFacade;
import model.BuildingPaymentCard;
import model.ActionCards;
import model.Card;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class OnlineGameClickActions extends GameClickActionAdapter {
    private int myPlayerId;
    private final BiConsumer<String, String> send;
    private final Runnable onExitToMenu;

    // Creates a OnlineGameClickActions instance.
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

    // Plays action card as money.
    @Override
    public void playActionCardAsMoney(ActionCards card) {
        if (!isMyTurn()) {
            return;
        }

        send.accept("PLAY_AS_MONEY", handNumber(card));
    }

    // Runs use just say no.
    @Override
    public void useJustSayNo() {
        send.accept("JUST_SAY_NO", "");
    }

    // Accepts the latest Just Say No.
    @Override
    public void passJustSayNo() {
        send.accept("PASS_JUST_SAY_NO", "");
    }

    // Finishes payment.
    @Override
    public void finishPayment(ArrayList<Card> selectedCards) {
        send.accept("PAY", paymentBody(selectedCards));
    }

    // Checks whether this can submit payment.
    @Override
    public boolean canSubmitPayment() {
        if (!game.isPaymentSelecting()) {
            return false;
        }

        if (game.isCurrentPaymentWaitingForJustSayNoResponse()) {
            return playerNumber(game.getCurrentJustSayNoResponder()) == myPlayerId;
        }

        return playerNumber(game.getCurrentPaymentRequest().getPayer()) == myPlayerId;
    }

    // Finishes forced deal.
    @Override
    public void finishForcedDeal(ActionCards card,
                                 Player target,
                                 PropertiesCards myProperty,
                                 PropertiesCards targetProperty) {
        if (!isMyTurn()) {
            return;
        }

        send.accept("FORCED_DEAL", handNumber(card)
                + " " + playerNumber(target)
                + " " + propertyNumber(myPlayer(), myProperty)
                + " " + propertyNumber(target, targetProperty));
    }

    // Finishes sly deal.
    @Override
    public void finishSlyDeal(ActionCards card, Player target, PropertiesCards stolenCard) {
        if (!isMyTurn()) {
            return;
        }

        send.accept("SLY", handNumber(card)
                + " " + playerNumber(target)
                + " " + propertyNumber(target, stolenCard));
    }

    // Finishes multiple color rent.
    @Override
    public void finishMultipleColorRent(ActionCards card,
                                        Player target,
                                        PropertyColor color,
                                        boolean useDoubleRent) {
        if (!isMyTurn()) {
            return;
        }

        send.accept("RENT_ANY", handNumber(card)
                + " " + playerNumber(target)
                + " " + color.name()
                + doubleToken(useDoubleRent));
    }

    // Runs on multiple color rent target picked.
    @Override
    public void onMultipleColorRentTargetPicked(Player target) {
        gameScreen.showMultipleColorRentTargetDetail(target);
    }

    // Runs on forced deal target picked.
    @Override
    public void onForcedDealTargetPicked(Player target) {
        gameScreen.showForcedDealTargetDetail(target);
    }

    // Runs on debt collector target picked.
    @Override
    public void onDebtCollectorTargetPicked(ActionCards card, Player target) {
        gameScreen.setSelectedDebtCollectorTarget(target);
    }

    // Finishes debt collector.
    @Override
    public void finishDebtCollector(ActionCards card, Player target) {
        if (!isMyTurn()) {
            return;
        }

        send.accept("DEBT", handNumber(card) + " " + playerNumber(target));
    }

    // Runs on deal breaker set picked.
    @Override
    public void onDealBreakerSetPicked(GameScreen.DealBreakerChoice choice) {
        gameScreen.showDealBreakerDetailChoice(choice);
    }

    // Finishes deal breaker.
    @Override
    public void finishDealBreaker(ActionCards card, Player target, ArrayList<PropertiesCards> selectedSet) {
        if (!isMyTurn()) {
            return;
        }

        if (selectedSet != null && !selectedSet.isEmpty()) {
            send.accept("DEAL_BREAKER", handNumber(card)
                    + " " + playerNumber(target)
                    + " " + selectedSet.getFirst().getCurrentColor().name());
        }
    }

    // Finishes two color rent.
    @Override
    public void finishTwoColorRent(ActionCards card, PropertyColor color, boolean useDoubleRent) {
        if (!isMyTurn()) {
            return;
        }

        send.accept("RENT", handNumber(card) + " " + color.name() + doubleToken(useDoubleRent));
    }

    // Finishes building.
    @Override
    public void finishBuilding(ActionCards card, PropertyColor color) {
        if (!isMyTurn()) {
            return;
        }

        send.accept(card.getActionCardType().name(), handNumber(card) + " " + color.name());
    }

    // Runs set wild card color.
    @Override
    public void setWildCardColor(PropertiesCards card, PropertyColor color) {
        if (!isMyTurn()) {
            return;
        }

        send.accept("SET_PROPERTY_COLOR",
                propertyNumber(myPlayer(), card) + " " + color.name());
    }

    // Plays hand card.
    @Override
    public void playHandCard(Card card) {
        if (!isMyTurn()) {
            return;
        }

        send.accept("PLAY_CARD", handNumber(card));
    }

    // Discards hand card.
    @Override
    public void discardHandCard(Card card) {
        if (!isMyTurn()) {
            return;
        }

        send.accept("DISCARD", handNumber(card));
    }

    // Runs record win if needed.
    @Override
    public void recordWinIfNeeded() {
        // Server drives win state.
    }

    // Runs end turn.
    @Override
    public void endTurn() {
        if (game.getCurrentPlayerIndex() + 1 == myPlayerId) {
            send.accept("END_TURN", "");
        }
    }

    // Returns to to menu.
    @Override
    public void returnToMenu() {
        onExitToMenu.run();
    }

    // Finishes immediate action.
    @Override
    protected void finishImmediateAction(ActionCards actionCard) {
        if (!isMyTurn()) {
            return;
        }

        if (actionCard.getActionCardType() == model.ActionCardType.BIRTHDAY) {
            send.accept("BIRTHDAY", handNumber(actionCard));
        } else if (actionCard.getActionCardType() == model.ActionCardType.PASS_GO) {
            send.accept("PASS_GO", handNumber(actionCard));
        }
    }

    // Runs hand number.
    private String handNumber(Card card) {
        return Integer.toString(myPlayer().getHandCards().indexOf(card) + 1);
    }

    // Plays er number.
    private int playerNumber(Player player) {
        return game.getPlayers().indexOf(player) + 1;
    }

    // Runs property number.
    private int propertyNumber(Player player, PropertiesCards card) {
        return player.getPropertyCards().indexOf(card) + 1;
    }

    // Runs my player.
    private Player myPlayer() {
        int index = myPlayerId - 1;
        if (index < 0 || index >= game.getPlayers().size()) {
            return game.getCurrentPlayer();
        }

        return game.getPlayers().get(index);
    }

    // Checks whether this client controls the current turn.
    private boolean isMyTurn() {
        return game.getCurrentPlayerIndex() + 1 == myPlayerId;
    }

    // Processes ment body.
    private String paymentBody(ArrayList<Card> selectedCards) {
        Game.PaymentRequest request = game.getCurrentPaymentRequest();
        Player payer = request.getPayer();
        ArrayList<String> tokens = new ArrayList<>();
        for (Card card : selectedCards) {
            if (card instanceof BuildingPaymentCard buildingCard) {
                tokens.add(buildingCard.getActionCardType().name() + ":" + buildingCard.getColor().name());
                continue;
            }

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

    // Runs double token.
    private String doubleToken(boolean useDoubleRent) {
        return useDoubleRent ? " DOUBLE" : "";
    }
}
