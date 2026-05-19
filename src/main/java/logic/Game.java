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
    private static final int DEFAULT_PLAYER_COUNT = 4;
    private static final int MIN_PLAYER_COUNT = 2;
    private static final int MAX_PLAYER_COUNT = 4;

    private final ArrayList<Player> players;
    private final RentCalculator rentCalculator;
    private final MoneyCardAndPropertyCardPlayService cardPlayService;
    private final GameSetupService gameSetupService;
    private final int playerCount;

    private DrawPileAndDiscardPile drawCards;
    private PaymentManager paymentManager;
    private ActionCardService actionCardService;
    private TurnManager turnManager;

    private boolean isWin;

    public Game() {
        this(DEFAULT_PLAYER_COUNT);
    }

    public Game(int playerCount) {
        this.playerCount = normalizePlayerCount(playerCount);
        players = new ArrayList<>();
        rentCalculator = new RentCalculator();
        cardPlayService = new MoneyCardAndPropertyCardPlayService();
        gameSetupService = new GameSetupService();

        initializeGameObjects();
        setupNewPlayers();

        isWin = false;
    }

    private int normalizePlayerCount(int playerCount) {
        if (playerCount < MIN_PLAYER_COUNT) {
            return MIN_PLAYER_COUNT;
        }

        if (playerCount > MAX_PLAYER_COUNT) {
            return MAX_PLAYER_COUNT;
        }

        return playerCount;
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
        gameSetupService.setupPlayers(players, drawCards, playerCount);
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

    public boolean discard(Card card) {
        return turnManager.discard(card);
    }

    public boolean playCard(Card card) {
        boolean success = cardPlayService.playCard(getCurrentPlayer(), card);

        if (success) {
            checkCurrentPlayerWin();
        }

        return success;
    }

    public boolean finishBirthday(ActionCards birthdayCard) {
        boolean success = actionCardService.finishBirthday(getCurrentPlayer(), birthdayCard);

        if (success) {
            checkCurrentPlayerWin();
        }

        return success;
    }

    public boolean finishSlyDeal(ActionCards slyDealCard, Player targetPlayer, PropertiesCards stolenCard) {
        boolean success = actionCardService.finishSlyDeal(
                getCurrentPlayer(),
                slyDealCard,
                targetPlayer,
                stolenCard
        );

        if (success) {
            checkCurrentPlayerWin();
        }

        return success;
    }

    public boolean finishDealBreaker(ActionCards dealBreakerCard,
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

        return success;
    }

    public boolean finishDebtCollector(ActionCards debtCollectorCard, Player targetPlayer) {
        boolean success = actionCardService.finishDebtCollector(
                getCurrentPlayer(),
                debtCollectorCard,
                targetPlayer
        );

        if (success) {
            checkCurrentPlayerWin();
        }

        return success;
    }

    public boolean finishTwoColorRent(ActionCards rentCard,
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

        return success;
    }

    public boolean finishMultipleColorRent(ActionCards rentCard,
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

        return success;
    }

    public boolean finishHouse(ActionCards houseCard, PropertyColor selectedColor) {
        boolean success = actionCardService.finishHouse(getCurrentPlayer(), houseCard, selectedColor);

        if (success) {
            checkCurrentPlayerWin();
        }

        return success;
    }

    public boolean finishHotel(ActionCards hotelCard, PropertyColor selectedColor) {
        boolean success = actionCardService.finishHotel(getCurrentPlayer(), hotelCard, selectedColor);

        if (success) {
            checkCurrentPlayerWin();
        }

        return success;
    }

    public boolean finishForcedDeal(ActionCards forcedDealCard,
                                    Player targetPlayer,
                                    PropertiesCards currentPlayerCard,
                                    PropertiesCards targetPlayerCard) {
        boolean success = actionCardService.finishForcedDeal(
                getCurrentPlayer(),
                forcedDealCard,
                targetPlayer,
                currentPlayerCard,
                targetPlayerCard
        );

        if (success) {
            checkCurrentPlayerWin();
        }

        return success;
    }

    public boolean hasDoubleTheRentCard(Player player) {return actionCardService.hasDoubleTheRentCard(player);}

    public boolean isPaymentSelecting() {return paymentManager.isPaymentSelecting();}

    public PaymentRequest getCurrentPaymentRequest() {return paymentManager.getCurrentPaymentRequest();}

    public boolean canCurrentPaymentUseJustSayNo() {return paymentManager.canCurrentPaymentUseJustSayNo();}

    public void currentPaymentUseJustSayNo() {paymentManager.currentPaymentUseJustSayNo();}

    public boolean finishCurrentPayment(ArrayList<Card> selectedCards) {
        boolean success = paymentManager.finishCurrentPayment(selectedCards);

        if (success) {
            checkCurrentPlayerWin();
        }

        return success;
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
