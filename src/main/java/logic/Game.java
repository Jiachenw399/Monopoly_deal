package logic;

import model.ActionCards;
import model.Card;
import model.DrawPileAndDiscardPile;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;

public class Game {
    public static final double SCREEN_WIDTH = 1035;
    public static final double SCREEN_HEIGHT = 625;

    private final ArrayList<Player> players;
    private final RentCalculator rentCalculator;
    private final CardPlayService cardPlayService;
    private final GameSetupService gameSetupService;

    private DrawPileAndDiscardPile drawCards;
    private PaymentManager paymentManager;
    private ActionCardService actionCardService;
    private TurnManager turnManager;

    private boolean isWin;

    public Game() {
        players = new ArrayList<>();
        rentCalculator = new RentCalculator();
        cardPlayService = new CardPlayService();
        gameSetupService = new GameSetupService();

        initializeGameObjects();
        setupNewPlayers();

        isWin = false;
    }

    public void startGame() {
        resetGame();
        setupNewPlayers();
        turnManager.startFirstTurn();

        System.out.println("Monopoly Deal GUI game started.");
    }

    private void resetGame() {
        initializeGameObjects();
        isWin = false;
    }

    private void initializeGameObjects() {
        drawCards = new DrawPileAndDiscardPile();
        paymentManager = new PaymentManager();
        actionCardService = createActionCardService();
        turnManager = new TurnManager(players, drawCards);
    }

    private ActionCardService createActionCardService() {
        return new ActionCardService(players, drawCards, paymentManager, rentCalculator);
    }

    private void setupNewPlayers() {
        gameSetupService.setupPlayers(players, drawCards);
    }

    public void startTurn(Player currentPlayer) {
        turnManager.startTurn(currentPlayer);
    }

    public void guiEndTurn() {
        if (checkCurrentPlayerWin()) {
            return;
        }

        turnManager.endTurn();
    }

    public void discard(Card card) {
        turnManager.discard(card);
    }

    public void playCard(Card card) {
        boolean success = cardPlayService.playCard(getCurrentPlayer(), card);

        if (success) {
            checkCurrentPlayerWin();
        }
    }

    public void finishBirthday(ActionCards birthdayCard) {
        boolean success = actionCardService.finishBirthday(getCurrentPlayer(), birthdayCard);

        if (success) {
            checkCurrentPlayerWin();
        }
    }

    public void finishSlyDeal(ActionCards slyDealCard, Player targetPlayer, PropertiesCards stolenCard) {
        boolean success = actionCardService.finishSlyDeal(
                getCurrentPlayer(),
                slyDealCard,
                targetPlayer,
                stolenCard
        );

        if (success) {
            checkCurrentPlayerWin();
        }
    }

    public void finishDealBreaker(ActionCards dealBreakerCard,
                                  Player targetPlayer,
                                  ArrayList<PropertiesCards> selectedSet) {
        boolean success = actionCardService.finishDealBreaker(
                getCurrentPlayer(),
                dealBreakerCard,
                targetPlayer,
                selectedSet
        );

        if (success) {
            checkCurrentPlayerWin();
        }
    }

    public void finishDebtCollector(ActionCards debtCollectorCard, Player targetPlayer) {
        boolean success = actionCardService.finishDebtCollector(
                getCurrentPlayer(),
                debtCollectorCard,
                targetPlayer
        );

        if (success) {
            checkCurrentPlayerWin();
        }
    }

    public void finishTwoColorRent(ActionCards rentCard,
                                   PropertyColor selectedColor,
                                   boolean useDoubleRent) {
        boolean success = actionCardService.finishTwoColorRent(
                getCurrentPlayer(),
                rentCard,
                selectedColor,
                useDoubleRent
        );

        if (success) {
            checkCurrentPlayerWin();
        }
    }

    public void finishMultipleColorRent(ActionCards rentCard,
                                        Player targetPlayer,
                                        PropertyColor selectedColor,
                                        boolean useDoubleRent) {
        boolean success = actionCardService.finishMultipleColorRent(
                getCurrentPlayer(),
                rentCard,
                targetPlayer,
                selectedColor,
                useDoubleRent
        );

        if (success) {
            checkCurrentPlayerWin();
        }
    }

    public void finishHouse(ActionCards houseCard, PropertyColor selectedColor) {
        boolean success = actionCardService.finishHouse(getCurrentPlayer(), houseCard, selectedColor);

        if (success) {
            checkCurrentPlayerWin();
        }
    }

    public void finishHotel(ActionCards hotelCard, PropertyColor selectedColor) {
        boolean success = actionCardService.finishHotel(getCurrentPlayer(), hotelCard, selectedColor);

        if (success) {
            checkCurrentPlayerWin();
        }
    }

    public boolean hasDoubleTheRentCard(Player player) {return actionCardService.hasDoubleTheRentCard(player);}

    public boolean isPaymentSelecting() {return paymentManager.isPaymentSelecting();}

    public PaymentRequest getCurrentPaymentRequest() {return paymentManager.getCurrentPaymentRequest();}

    public boolean canCurrentPaymentUseJustSayNo() {return paymentManager.canCurrentPaymentUseJustSayNo();}

    public void currentPaymentUseJustSayNo() {paymentManager.currentPaymentUseJustSayNo();}

    public void finishCurrentPayment(ArrayList<Card> selectedCards) {
        boolean success = paymentManager.finishCurrentPayment(selectedCards);

        if (success) {
            checkCurrentPlayerWin();
        }
    }

    public int getTotalAssetsValue(Player player) {return paymentManager.getTotalAssetsValue(player);}

    public int getCardsValue(ArrayList<Card> cards) {return paymentManager.getCardsValue(cards);}

    private boolean checkCurrentPlayerWin() {
        if (getCurrentPlayer().checkIfWin()) {
            isWin = true;
            return true;
        }

        return false;
    }

    public Player getCurrentPlayer() {return turnManager.getCurrentPlayer();}

    public int getCurrentPlayerIndex() {return turnManager.getCurrentPlayerIndex();}

    public ArrayList<Player> getPlayers() {return players;}

    public DrawPileAndDiscardPile getDrawCards() {return drawCards;}

    public boolean isWin() {return isWin;}

    public void setWin(boolean win) {isWin = win;}

    public boolean isDiscard() {return turnManager.isDiscard();}

    public static class PaymentRequest {
        private final Player receiver;
        private final Player payer;
        private final int amount;

        public PaymentRequest(Player receiver, Player payer, int amount) {
            this.receiver = receiver;
            this.payer = payer;
            this.amount = amount;
        }

        public Player getReceiver() {return receiver;}

        public Player getPayer() {return payer;}

        public int getAmount() {return amount;}
    }
}