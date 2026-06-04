package logic;

import model.ActionCards;
import model.Card;
import model.DrawPileAndDiscardPile;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;
import java.util.List;

public interface GameFacade {
    // Starts a game with the current player count.
    void startGame();

    // Starts a game with the given player count.
    void startGame(int playerCount);

    // Starts a game with the given player count and player names.
    void startGame(int playerCount, List<String> playerNames);

    // Starts a turn for the given player.
    void startTurn(Player currentPlayer);

    // Ends the current turn from the GUI.
    void guiEndTurn();

    // Advances the turn when the current player is absent.
    void forceAdvanceTurnForAbsentPlayer();

    // Discards the selected card.
    boolean discard(Card card);

    // Plays the selected card.
    boolean playCard(Card card);

    // Plays an action card as money.
    boolean playActionCardAsMoney(ActionCards card);

    // Finishes a Pass Go action.
    boolean finishPassGo(ActionCards passGoCard);

    // Finishes a Birthday action.
    boolean finishBirthday(ActionCards birthdayCard);

    // Finishes a Sly Deal action.
    boolean finishSlyDeal(ActionCards slyDealCard, Player targetPlayer, PropertiesCards stolenCard);

    // Finishes a Deal Breaker action.
    boolean finishDealBreaker(ActionCards dealBreakerCard,
                              Player targetPlayer,
                              ArrayList<PropertiesCards> selectedSet);

    // Finishes a Debt Collector action.
    boolean finishDebtCollector(ActionCards debtCollectorCard, Player targetPlayer);

    // Finishes a two-color rent action.
    boolean finishTwoColorRent(ActionCards rentCard, PropertyColor selectedColor, boolean useDoubleRent);

    // Finishes a multi-color rent action.
    boolean finishMultipleColorRent(ActionCards rentCard,
                                    Player targetPlayer,
                                    PropertyColor selectedColor,
                                    boolean useDoubleRent);

    // Finishes a House action.
    boolean finishHouse(ActionCards houseCard, PropertyColor selectedColor);

    // Finishes a Hotel action.
    boolean finishHotel(ActionCards hotelCard, PropertyColor selectedColor);

    // Finishes a Forced Deal action.
    boolean finishForcedDeal(ActionCards forcedDealCard,
                             Player targetPlayer,
                             PropertiesCards currentPlayerCard,
                             PropertiesCards targetPlayerCard);

    // Checks whether the player has a Double the Rent card.
    boolean hasDoubleTheRentCard(Player player);

    // Checks whether a payment selection is active.
    boolean isPaymentSelecting();

    Game.PaymentRequest getCurrentPaymentRequest();

    // Checks whether the current payment can use Just Say No.
    boolean canCurrentPaymentUseJustSayNo();

    // Checks whether the current payment is waiting for a Just Say No counter.
    boolean isCurrentPaymentWaitingForJustSayNoResponse();

    // Finds the player who may answer the latest Just Say No.
    Player getCurrentJustSayNoResponder();

    // Uses Just Say No for the current payment.
    void currentPaymentUseJustSayNo();

    // Accepts the latest Just Say No for the current payment.
    void currentPaymentPassJustSayNo();

    // Finishes the current payment with selected cards.
    boolean finishCurrentPayment(ArrayList<Card> selectedCards);

    // Sets a wild property card color.
    boolean setPropertyColor(Player player, PropertiesCards propertyCard, PropertyColor color);

    // Calculates the total asset value for a player.
    int getTotalAssetsValue(Player player);

    // Calculates the total value of selected cards.
    int getCardsValue(ArrayList<Card> cards);

    // Calculates the selected payment card value for a payer.
    int getPaymentCardsValue(Player payer, ArrayList<Card> cards);

    Player getCurrentPlayer();

    int getCurrentPlayerIndex();

    ArrayList<Player> getPlayers();

    void applyOnlineState(ArrayList<Player> snapshotPlayers,
                          int currentPlayerIndex,
                          boolean discard,
                          Game.PaymentRequest paymentRequest,
                          boolean win);

    DrawPileAndDiscardPile getDrawCards();

    // Checks whether the game has a winner.
    boolean isWin();

    // Updates the win state.
    void setWin(boolean win);

    // Checks whether the game is in discard phase.
    boolean isDiscard();

    // Adds an observer for game state changes.
    void addObserver(GameObserver observer);

    // Restarts the game with the same player count and names.
    void restartGame();

    // Refreshes the UI after an AI plays a card (no-op by default).
    default void refreshAiUi() {
    }
}
