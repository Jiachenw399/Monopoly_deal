package logic;

import model.Player;

public interface AIPlayer {

    void onTurnStart(GameFacade game, Player player, Runnable onDone);

    void onPaymentRequested(GameFacade game, Player player,
                             Game.PaymentRequest request,
                             Runnable onPaymentDone);

    // Handles the discard phase for AI players.
    default void onDiscardPhaseStarted(GameFacade game, Player player, Runnable onDone) {
        onDone.run();
    }
}
