package logic;

import model.ActionCards;
import model.Card;
import model.DrawPileAndDiscardPile;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;

public interface GameFacade {
    void startGame();

    void startGame(int playerCount);

    void startTurn(Player currentPlayer);

    void guiEndTurn();

    void forceAdvanceTurnForAbsentPlayer();

    boolean discard(Card card);

    boolean playCard(Card card);

    boolean playActionCardAsMoney(ActionCards card);

    boolean finishPassGo(ActionCards passGoCard);

    boolean finishBirthday(ActionCards birthdayCard);

    boolean finishSlyDeal(ActionCards slyDealCard, Player targetPlayer, PropertiesCards stolenCard);

    boolean finishDealBreaker(ActionCards dealBreakerCard,
                              Player targetPlayer,
                              ArrayList<PropertiesCards> selectedSet);

    boolean finishDebtCollector(ActionCards debtCollectorCard, Player targetPlayer);

    boolean finishTwoColorRent(ActionCards rentCard, PropertyColor selectedColor, boolean useDoubleRent);

    boolean finishMultipleColorRent(ActionCards rentCard,
                                    Player targetPlayer,
                                    PropertyColor selectedColor,
                                    boolean useDoubleRent);

    boolean finishHouse(ActionCards houseCard, PropertyColor selectedColor);

    boolean finishHotel(ActionCards hotelCard, PropertyColor selectedColor);

    boolean finishForcedDeal(ActionCards forcedDealCard,
                             Player targetPlayer,
                             PropertiesCards currentPlayerCard,
                             PropertiesCards targetPlayerCard);

    boolean hasDoubleTheRentCard(Player player);

    boolean isPaymentSelecting();

    Game.PaymentRequest getCurrentPaymentRequest();

    boolean canCurrentPaymentUseJustSayNo();

    void currentPaymentUseJustSayNo();

    boolean finishCurrentPayment(ArrayList<Card> selectedCards);

    boolean setPropertyColor(Player player, PropertiesCards propertyCard, PropertyColor color);

    int getTotalAssetsValue(Player player);

    int getCardsValue(ArrayList<Card> cards);

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

    boolean isWin();

    void setWin(boolean win);

    boolean isDiscard();

    void addObserver(GameObserver observer);
}
