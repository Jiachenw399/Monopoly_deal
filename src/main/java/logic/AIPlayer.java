package logic;

import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface AIPlayer {

    void onTurnStart(GameFacade game, Player player, Runnable onDone);

    void onPaymentRequested(GameFacade game, Player player,
                             Game.PaymentRequest request,
                             Runnable onPaymentDone);
}
