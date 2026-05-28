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
        return new ActionCardService(players, paymentManager, rentCalculator);
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

    public void forceAdvanceTurnForAbsentPlayer() {
        if (checkCurrentPlayerWin()) {
            return;
        }

        turnManager.forceAdvanceTurnForAbsentPlayer();
    }

    public boolean discard(Card card) {
        return turnManager.discard(card);
    }

    public boolean playCard(Card card) {
        return finishAction(cardPlayService.playCard(getCurrentPlayer(), card));
    }

    public boolean playActionCardAsMoney(ActionCards card) {
        Player currentPlayer = getCurrentPlayer();

        if (currentPlayer == null || card == null || !currentPlayer.getHandCards().contains(card)) {
            return false;
        }

        if (currentPlayer.getUseCardTimes() >= 3) {
            return false;
        }

        currentPlayer.getHandCards().remove(card);
        currentPlayer.getBankCards().add(card);
        currentPlayer.setUseCardTimes(currentPlayer.getUseCardTimes() + 1);
        return true;
    }

    public boolean finishPassGo(ActionCards passGoCard) {
        return finishAction(actionCardService.finishPassGo(getCurrentPlayer(), passGoCard));
    }

    public boolean finishBirthday(ActionCards birthdayCard) {
        return finishAction(actionCardService.finishBirthday(getCurrentPlayer(), birthdayCard));
    }

    public boolean finishSlyDeal(ActionCards slyDealCard, Player targetPlayer, PropertiesCards stolenCard) {
        return finishAction(actionCardService.finishSlyDeal(
                getCurrentPlayer(),
                slyDealCard,
                targetPlayer,
                stolenCard
        ));
    }

    public boolean finishDealBreaker(ActionCards dealBreakerCard,
                                     Player targetPlayer,
                                     ArrayList<PropertiesCards> selectedSet) {
        return finishAction(actionCardService.finishDealBreaker(
                getCurrentPlayer(),
                dealBreakerCard,
                targetPlayer,
                selectedSet
        ));
    }

    public boolean finishDebtCollector(ActionCards debtCollectorCard, Player targetPlayer) {
        return finishAction(actionCardService.finishDebtCollector(
                getCurrentPlayer(),
                debtCollectorCard,
                targetPlayer
        ));
    }

    public boolean finishTwoColorRent(ActionCards rentCard,
                                      PropertyColor selectedColor,
                                      boolean useDoubleRent) {
        return finishAction(actionCardService.finishTwoColorRent(
                getCurrentPlayer(),
                rentCard,
                selectedColor,
                useDoubleRent
        ));
    }

    public boolean finishMultipleColorRent(ActionCards rentCard,
                                           Player targetPlayer,
                                           PropertyColor selectedColor,
                                           boolean useDoubleRent) {
        return finishAction(actionCardService.finishMultipleColorRent(
                getCurrentPlayer(),
                rentCard,
                targetPlayer,
                selectedColor,
                useDoubleRent
        ));
    }

    public boolean finishHouse(ActionCards houseCard, PropertyColor selectedColor) {
        return finishAction(actionCardService.finishHouse(getCurrentPlayer(), houseCard, selectedColor));
    }

    public boolean finishHotel(ActionCards hotelCard, PropertyColor selectedColor) {
        return finishAction(actionCardService.finishHotel(getCurrentPlayer(), hotelCard, selectedColor));
    }

    public boolean finishForcedDeal(ActionCards forcedDealCard,
                                    Player targetPlayer,
                                    PropertiesCards currentPlayerCard,
                                    PropertiesCards targetPlayerCard) {
        return finishAction(actionCardService.finishForcedDeal(
                getCurrentPlayer(),
                forcedDealCard,
                targetPlayer,
                currentPlayerCard,
                targetPlayerCard
        ));
    }

    public boolean hasDoubleTheRentCard(Player player) {
        return actionCardService.hasDoubleTheRentCard(player);
    }

    public boolean isPaymentSelecting() {
        return paymentManager.isPaymentSelecting();
    }

    public PaymentRequest getCurrentPaymentRequest() {
        return paymentManager.getCurrentPaymentRequest();
    }

    public boolean canCurrentPaymentUseJustSayNo() {
        return paymentManager.canCurrentPaymentUseJustSayNo();
    }

    public void currentPaymentUseJustSayNo() {
        paymentManager.currentPaymentUseJustSayNo();
    }

    public boolean finishCurrentPayment(ArrayList<Card> selectedCards) {
        return finishAction(paymentManager.finishCurrentPayment(selectedCards));
    }

    public int getTotalAssetsValue(Player player) {
        return paymentManager.getTotalAssetsValue(player);
    }

    public int getCardsValue(ArrayList<Card> cards) {
        return paymentManager.getCardsValue(cards);
    }

    private boolean finishAction(boolean success) {
        if (success) {
            checkCurrentPlayerWin();
        }

        return success;
    }

    private boolean checkCurrentPlayerWin() {
        if (getCurrentPlayer().checkIfWin()) {
            isWin = true;
            return true;
        }

        return false;
    }

    public Player getCurrentPlayer() {
        return turnManager.getCurrentPlayer();
    }

    public int getCurrentPlayerIndex() {
        return turnManager.getCurrentPlayerIndex();
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void applyOnlineState(ArrayList<Player> snapshotPlayers,
                                 int currentPlayerIndex,
                                 boolean discard,
                                 PaymentRequest paymentRequest,
                                 boolean win) {
        players.clear();

        if (snapshotPlayers != null) {
            players.addAll(snapshotPlayers);
        }

        turnManager.applyOnlineState(currentPlayerIndex, discard);
        paymentManager.applyOnlineState(paymentRequest);
        isWin = win;
    }

    public DrawPileAndDiscardPile getDrawCards() {
        return drawCards;
    }

    public boolean isWin() {
        return isWin;
    }

    public void setWin(boolean win) {
        isWin = win;
    }

    public boolean isDiscard() {
        return turnManager.isDiscard();
    }

    public static class PaymentRequest {
        private final Player receiver;
        private final Player payer;
        private final int amount;

        public PaymentRequest(Player receiver, Player payer, int amount) {
            this.receiver = receiver;
            this.payer = payer;
            this.amount = amount;
        }

        public Player getReceiver() {
            return receiver;
        }

        public Player getPayer() {
            return payer;
        }

        public int getAmount() {
            return amount;
        }
    }
}
