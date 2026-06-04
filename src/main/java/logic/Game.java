package logic;

import model.ActionCards;
import model.Card;
import model.DeckCardFactory;
import model.DrawPileAndDiscardPile;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;
import model.StandardDeckCardFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Game implements GameFacade {
    public static final double SCREEN_WIDTH = 1035;
    public static final double SCREEN_HEIGHT = 700;
    private static final int DEFAULT_PLAYER_COUNT = 4;
    private static final int MIN_PLAYER_COUNT = 1;
    private static final int MAX_PLAYER_COUNT = 5;

    private final ArrayList<Player> players;
    private final RentCalculator rentCalculator;
    private final MoneyCardAndPropertyCardPlayService cardPlayService;
    private final GameSetupService gameSetupService;
    private final List<GameObserver> observers;
    private final DeckCardFactory cardFactory;
    private int playerCount;

    private DrawPileAndDiscardPile drawCards;
    private PaymentManager paymentManager;
    private ActionCardService actionCardService;
    private TurnManager turnManager;
    private List<String> playerNames;
    private Map<Player, AIPlayer> aiPlayers;
    private Runnable aiTurnCallback;
    private Player activeAIPlayer;
    private int aiOpponentCount;
    private boolean turnAdvancedByDiscard;

    private boolean isWin;
    private int winnerIndex = -1;

    // Creates a Game instance.
    public Game() {
        this(DEFAULT_PLAYER_COUNT, new StandardDeckCardFactory());
    }

    // Creates a Game instance.
    public Game(int playerCount) {
        this(playerCount, new StandardDeckCardFactory());
    }

    // Creates a Game instance.
    public Game(int playerCount, DeckCardFactory cardFactory) {
        this.playerCount = normalizePlayerCount(playerCount);
        this.playerNames = null;
        this.cardFactory = Objects.requireNonNull(cardFactory);
        players = new ArrayList<>();
        rentCalculator = new RentCalculator();
        cardPlayService = new MoneyCardAndPropertyCardPlayService();
        gameSetupService = new GameSetupService();
        observers = new ArrayList<>();
        aiPlayers = new HashMap<>();

        initializeGameObjects();
        setupNewPlayers();

        isWin = false;
        winnerIndex = -1;
    }

    // Normalizes player count.
    private int normalizePlayerCount(int playerCount) {
        if (playerCount < MIN_PLAYER_COUNT) {
            return MIN_PLAYER_COUNT;
        }

        if (playerCount > MAX_PLAYER_COUNT) {
            return MAX_PLAYER_COUNT;
        }

        return playerCount;
    }

    // Starts game.
    public void startGame() {
        aiOpponentCount = 0;
        resetGame();
        setupNewPlayers();
        turnManager.startFirstTurn();
        notifyObservers();
    }

    // Starts game.
    public void startGame(int playerCount) {
        this.playerCount = normalizePlayerCount(playerCount);
        startGame();
    }

    // Starts game with player names.
    public void startGame(int playerCount, List<String> playerNames) {
        this.playerCount = normalizePlayerCount(playerCount);
        this.playerNames = playerNames;
        startGame();
    }

    // Starts game with a prepared draw pile.
    public void startGame(int playerCount, DrawPileAndDiscardPile preparedDrawCards) {
        aiOpponentCount = 0;
        this.playerCount = normalizePlayerCount(playerCount);
        resetGame(Objects.requireNonNull(preparedDrawCards));
        setupNewPlayers();
        turnManager.startFirstTurn();
        notifyObservers();
    }

    // Runs reset game.
    private void resetGame() {
        aiPlayers.clear();
        activeAIPlayer = null;
        initializeGameObjects();
        isWin = false;
        winnerIndex = -1;
    }

    // Runs reset game.
    private void resetGame(DrawPileAndDiscardPile preparedDrawCards) {
        aiPlayers.clear();
        activeAIPlayer = null;
        initializeGameObjects(preparedDrawCards);
        isWin = false;
        winnerIndex = -1;
    }

    // Initializes game objects.
    private void initializeGameObjects() {
        initializeGameObjects(new DrawPileAndDiscardPile(cardFactory));
    }

    // Initializes game objects.
    private void initializeGameObjects(DrawPileAndDiscardPile preparedDrawCards) {
        drawCards = preparedDrawCards;
        paymentManager = new PaymentManager();
        actionCardService = createActionCardService();
        turnManager = new TurnManager(players, drawCards);
    }

    // Creates action card service.
    private ActionCardService createActionCardService() {
        return new ActionCardService(players, paymentManager, rentCalculator);
    }

    // Runs setup new players.
    private void setupNewPlayers() {
        gameSetupService.setupPlayers(players, drawCards, playerCount, playerNames);
    }

    // Starts turn.
    public void startTurn(Player currentPlayer) {
        turnManager.startTurn(currentPlayer);
        notifyObservers();
    }

    // Runs gui end turn.
    public void guiEndTurn() {
        if (checkAnyPlayerWin()) {
            notifyObservers();
            return;
        }

        turnManager.endTurn();
        notifyObservers();

        if (!triggerAITurnIfNeeded()) {
            return;
        }
    }

    // Forces advance turn for absent player.
    public void forceAdvanceTurnForAbsentPlayer() {
        if (checkAnyPlayerWin()) {
            notifyObservers();
            return;
        }

        turnManager.forceAdvanceTurnForAbsentPlayer();
        turnAdvancedByDiscard = true;
        notifyObservers();
    }

    // Discards this operation.
    public boolean discard(Card card) {
        boolean result = finishAction(turnManager.discard(card));
        if (result) {
            turnAdvancedByDiscard = true;
        }
        return result;
    }

    // Plays card.
    public boolean playCard(Card card) {
        return finishAction(cardPlayService.playCard(getCurrentPlayer(), card));
    }

    // Plays action card as money.
    public boolean playActionCardAsMoney(ActionCards card) {
        return finishAction(cardPlayService.playActionCardAsMoney(getCurrentPlayer(), card));
    }

    // Finishes pass go.
    public boolean finishPassGo(ActionCards passGoCard) {
        return finishAction(actionCardService.finishPassGo(getCurrentPlayer(), passGoCard));
    }

    // Finishes birthday.
    public boolean finishBirthday(ActionCards birthdayCard) {
        return finishAction(actionCardService.finishBirthday(getCurrentPlayer(), birthdayCard));
    }

    // Finishes sly deal.
    public boolean finishSlyDeal(ActionCards slyDealCard, Player targetPlayer, PropertiesCards stolenCard) {
        return finishAction(actionCardService.finishSlyDeal(
                getCurrentPlayer(),
                slyDealCard,
                targetPlayer,
                stolenCard
        ));
    }

    // Finishes deal breaker.
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

    // Finishes debt collector.
    public boolean finishDebtCollector(ActionCards debtCollectorCard, Player targetPlayer) {
        return finishAction(actionCardService.finishDebtCollector(
                getCurrentPlayer(),
                debtCollectorCard,
                targetPlayer
        ));
    }

    // Finishes two color rent.
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

    // Finishes multiple color rent.
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

    // Finishes house.
    public boolean finishHouse(ActionCards houseCard, PropertyColor selectedColor) {
        return finishAction(actionCardService.finishHouse(getCurrentPlayer(), houseCard, selectedColor));
    }

    // Finishes hotel.
    public boolean finishHotel(ActionCards hotelCard, PropertyColor selectedColor) {
        return finishAction(actionCardService.finishHotel(getCurrentPlayer(), hotelCard, selectedColor));
    }

    // Finishes forced deal.
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

    // Checks whether this has double the rent card.
    public boolean hasDoubleTheRentCard(Player player) {
        return actionCardService.hasDoubleTheRentCard(player);
    }

    // Checks whether payment selecting.
    public boolean isPaymentSelecting() {
        return paymentManager.isPaymentSelecting();
    }

    // Finds current payment request.
    public PaymentRequest getCurrentPaymentRequest() {
        return paymentManager.getCurrentPaymentRequest();
    }

    // Checks whether this can current payment use just say no.
    public boolean canCurrentPaymentUseJustSayNo() {
        return paymentManager.canCurrentPaymentUseJustSayNo();
    }

    // Checks whether the current payment is waiting for a Just Say No counter.
    public boolean isCurrentPaymentWaitingForJustSayNoResponse() {
        return paymentManager.isCurrentPaymentWaitingForJustSayNoResponse();
    }

    // Finds the player who may answer the latest Just Say No.
    public Player getCurrentJustSayNoResponder() {
        return paymentManager.getCurrentJustSayNoResponder();
    }

    // Runs current payment use just say no.
    public void currentPaymentUseJustSayNo() {
        paymentManager.currentPaymentUseJustSayNo();
        notifyObservers();
    }

    // Accepts the latest Just Say No for the current payment.
    public void currentPaymentPassJustSayNo() {
        paymentManager.currentPaymentPassJustSayNo();
        notifyObservers();
    }

    // Finishes current payment.
    public boolean finishCurrentPayment(ArrayList<Card> selectedCards) {
        if (selectedCards == null || selectedCards.isEmpty()) {
            paymentManager.skipCurrentPayment();
            notifyObservers();
            if (isPaymentSelecting()) {
                triggerAIPaymentIfNeeded();
            } else {
                triggerAITurnIfNeeded();
            }
            return false;
        }
        boolean result = finishAction(paymentManager.finishCurrentPayment(selectedCards));
        if (result && isPaymentSelecting()) {
            triggerAIPaymentIfNeeded();
        } else if (result) {
            triggerAITurnIfNeeded();
        }
        return result;
    }

    // Runs set property color.
    public boolean setPropertyColor(Player player, PropertiesCards propertyCard, PropertyColor color) {
        if (player == null || propertyCard == null || color == null) {
            return false;
        }

        if (!player.getPropertyCards().contains(propertyCard)) {
            return false;
        }

        if (!propertyCard.isWildCard() || !propertyCard.getType().getColors().contains(color)) {
            return false;
        }

        if (wouldBreakBuiltCompleteSet(player, propertyCard, color)) {
            return false;
        }

        propertyCard.setCurrentColor(color);
        checkAnyPlayerWin();
        notifyObservers();
        return true;
    }

    // Checks whether changing a wild card color would break a built complete set.
    private boolean wouldBreakBuiltCompleteSet(Player player, PropertiesCards propertyCard, PropertyColor newColor) {
        PropertyColor oldColor = propertyCard.getCurrentColor();

        if (oldColor == null || oldColor == newColor) {
            return false;
        }

        if (!PlayerInfoHelper.hasHouse(player, oldColor) && !PlayerInfoHelper.hasHotel(player, oldColor)) {
            return false;
        }

        int oldColorCount = PlayerInfoHelper.getPropertyCountByCurrentColor(player, oldColor);
        return oldColorCount >= oldColor.getAmountToCompleteSet()
                && oldColorCount - 1 < oldColor.getAmountToCompleteSet();
    }

    // Finds total assets value.
    public int getTotalAssetsValue(Player player) {
        return paymentManager.getTotalAssetsValue(player);
    }

    // Finds cards value.
    public int getCardsValue(ArrayList<Card> cards) {
        return paymentManager.getCardsValue(cards);
    }

    // Finds payment cards value.
    public int getPaymentCardsValue(Player payer, ArrayList<Card> cards) {
        return paymentManager.getPaymentCardsValue(payer, cards);
    }

    // Finishes action.
    private boolean finishAction(boolean success) {
        if (success) {
            checkAnyPlayerWin();
            notifyObservers();
            if (isPaymentSelecting()) {
                triggerAIPaymentIfNeeded();
            }
        }

        return success;
    }

    // Runs check any player win.
    private boolean checkAnyPlayerWin() {
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (player.checkIfWin()) {
                isWin = true;
                winnerIndex = i;
                return true;
            }
        }

        return false;
    }

    // Finds current player.
    public Player getCurrentPlayer() {
        return turnManager.getCurrentPlayer();
    }

    // Finds current player index.
    public int getCurrentPlayerIndex() {
        return turnManager.getCurrentPlayerIndex();
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    // Applies online state.
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
        if (!win) {
            winnerIndex = -1;
        } else if (winnerIndex < 0) {
            winnerIndex = getCurrentPlayerIndex();
        }
        notifyObservers();
    }

    // Finds winner index, or -1 if the game has not been won.
    public int getWinnerIndex() {
        return winnerIndex;
    }

    public DrawPileAndDiscardPile getDrawCards() {
        return drawCards;
    }

    public boolean isWin() {
        return isWin;
    }

    // Runs set win.
    public void setWin(boolean win) {
        isWin = win;
        notifyObservers();
    }

    // Checks whether discard.
    public boolean isDiscard() {
        return turnManager.isDiscard();
    }

    // Restarts the game.
    public void restartGame() {
        int aiCountToRestore = aiOpponentCount;
        if (playerNames != null && !playerNames.isEmpty()) {
            this.playerCount = normalizePlayerCount(playerCount);
            this.playerNames = playerNames;
            resetGame();
            setupNewPlayers();
            turnManager.startFirstTurn();
            notifyObservers();
        } else {
            this.playerCount = normalizePlayerCount(playerCount);
            resetGame();
            setupNewPlayers();
            turnManager.startFirstTurn();
            notifyObservers();
        }
        restoreAIPlayers(aiCountToRestore);
        triggerAITurnIfNeeded();
    }

    // Adds observer.
    public void addObserver(GameObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    // Removes observer.
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    // Runs notify observers.
    private void notifyObservers() {
        for (GameObserver observer : new ArrayList<>(observers)) {
            observer.onGameStateChanged();
        }
    }

    public static class PaymentRequest {
        private final Player receiver;
        private final Player payer;
        private final int amount;
        private boolean justSayNoPending;
        private Player justSayNoResponder;
        private Player lastJustSayNoUser;

        // Runs payment request.
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

        public boolean isJustSayNoPending() {
            return justSayNoPending;
        }

        public Player getJustSayNoResponder() {
            return justSayNoResponder;
        }

        public Player getLastJustSayNoUser() {
            return lastJustSayNoUser;
        }

        // Starts waiting for the opponent to answer a Just Say No.
        public void startJustSayNoResponse(Player lastUser, Player responder) {
            justSayNoPending = true;
            lastJustSayNoUser = lastUser;
            justSayNoResponder = responder;
        }

        // Clears Just Say No response state.
        public void clearJustSayNoResponse() {
            justSayNoPending = false;
            justSayNoResponder = null;
            lastJustSayNoUser = null;
        }
    }

    // Registers an AI player for the given player.
    public void registerAI(Player player, AIPlayer ai) {
        if (player != null && ai != null) {
            player.setAI(true);
            aiPlayers.put(player, ai);
            int index = players.indexOf(player);
            if (index > 0) {
                aiOpponentCount = Math.max(aiOpponentCount, index);
            }
        }
    }

    // Sets the callback invoked after each AI turn ends.
    public void setAiTurnCallback(Runnable callback) {
        this.aiTurnCallback = callback;
    }

    // Checks whether the given player is an AI.
    public boolean isAI(Player player) {
        return aiPlayers.containsKey(player);
    }

    // Triggers the AI turn for the current player if it is an AI.
    // Returns true if an AI turn was triggered, false otherwise.
    public boolean triggerAITurnIfNeeded() {
        Player current = getCurrentPlayer();
        AIPlayer ai = aiPlayers.get(current);
        if (ai == null && current.isAI()) {
            ai = new SimpleAIPlayer();
            aiPlayers.put(current, ai);
        }
        if (ai == null) {
            return false;
        }
        if (activeAIPlayer == current) {
            return true;
        }
        activeAIPlayer = current;
        turnAdvancedByDiscard = false;
        ai.onTurnStart(this, current, () -> {
            activeAIPlayer = null;
            notifyObservers();
            if (!isWin() && !isPaymentSelecting()) {
                if (turnAdvancedByDiscard) {
                    if (!triggerAITurnIfNeeded()) {
                        notifyObservers();
                    }
                } else if (isDiscard()) {
                    forceAdvanceTurnForAbsentPlayer();
                    if (!triggerAITurnIfNeeded()) {
                        notifyObservers();
                    }
                } else {
                    guiEndTurn();
                }
            }
            if (aiTurnCallback != null) {
                aiTurnCallback.run();
            }
        });
        return true;
    }

    // Triggers the AI to respond to a payment request if the payer is an AI.
    public boolean triggerAIPaymentIfNeeded() {
        if (!isPaymentSelecting()) {
            return false;
        }
        Game.PaymentRequest request = getCurrentPaymentRequest();
        if (request == null) {
            return false;
        }
        Player payer = request.getPayer();
        AIPlayer ai = aiPlayers.get(payer);
        if (ai == null && payer.isAI()) {
            ai = new SimpleAIPlayer();
            aiPlayers.put(payer, ai);
        }
        if (ai == null) {
            return false;
        }
        ai.onPaymentRequested(this, payer, request, () -> {
            activeAIPlayer = null;
            notifyObservers();
            if (aiTurnCallback != null) {
                aiTurnCallback.run();
            }
        });
        return true;
    }

    // Restores AI opponents after a restart.
    private void restoreAIPlayers(int aiCountToRestore) {
        aiOpponentCount = aiCountToRestore;
        int maxAIIndex = Math.min(aiCountToRestore, players.size() - 1);
        for (int i = 1; i <= maxAIIndex; i++) {
            registerAI(players.get(i), new SimpleAIPlayer());
        }
    }
}
