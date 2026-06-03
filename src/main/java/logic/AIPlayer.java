package logic;

import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents an AI player that can make game decisions autonomously.
 * The AI is notified of game state changes and decides which actions to take.
 */
public interface AIPlayer {

    /**
     * Called when it becomes this AI player's turn.
     *
     * @param game    the game facade through which actions are taken
     * @param player  the AI player
     * @param onDone  callback to invoke when the AI has finished its turn
     */
    void onTurnStart(GameFacade game, Player player, Runnable onDone);

    /**
     * Called when a payment is requested and the AI must select cards to pay.
     *
     * @param game         the game facade
     * @param player       the AI player
     * @param request      the payment request
     * @param onPaymentDone callback to invoke when payment selection is complete
     */
    void onPaymentRequested(GameFacade game, Player player,
                             Game.PaymentRequest request,
                             Runnable onPaymentDone);

    /**
     * Called when a rent card is played and the AI needs to choose a color.
     *
     * @param game     the game facade
     * @param player   the AI player
     * @param card     the rent card
     * @param options  the available color choices
     * @param onDone   callback to invoke when a color is chosen
     */
    default void onRentColorChoice(GameFacade game, Player player,
                                   ActionCards card,
                                   List<PropertyColor> options,
                                   Runnable onDone) {
        if (options != null && !options.isEmpty()) {
            onDone.run();
        }
    }

    /**
     * Called when the AI enters the discard phase (hand > 7 cards).
     *
     * @param game    the game facade
     * @param player  the AI player
     * @param onDone  callback to invoke when discards are complete
     */
    void onDiscardPhaseStarted(GameFacade game, Player player, Runnable onDone);
}
