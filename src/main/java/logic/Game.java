package logic;

import model.ActionCards;
import model.ActionCardType;
import model.Card;
import model.DrawPileAndDiscardPile;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;

public class Game {
    public static final double SCREEN_WIDTH = 1035;
    public static final double SCREEN_HEIGHT = 625;

    private final ArrayList<Player> players;
    private DrawPileAndDiscardPile drawCards;
    private boolean isWin;
    private int currentPlayerIndex;
    private boolean isDiscard;

    private ArrayList<PaymentRequest> paymentRequests = new ArrayList<>();
    private PaymentRequest currentPaymentRequest = null;

    public Game() {
        players = new ArrayList<>();
        drawCards = new DrawPileAndDiscardPile();
        isWin = false;
        currentPlayerIndex = 0;
        isDiscard = false;
        addPlayer();
    }

    public void startGame() {
        resetGame();
        addPlayer();

        Player currentPlayer = getCurrentPlayer();
        currentPlayer.setOnTurn(true);
        currentPlayer.setUseCardTimes(0);

        System.out.println("Monopoly Deal GUI game started.");
    }

    private void resetGame() {
        players.clear();
        drawCards = new DrawPileAndDiscardPile();
        currentPlayerIndex = 0;
        isWin = false;
        isDiscard = false;
    }

    public void startTurn(Player currentPlayer) {
        currentPlayer.setOnTurn(true);
        currentPlayer.setUseCardTimes(0);

        int drawNumber = getDrawNumberAtTurnStart(currentPlayer);
        currentPlayer.takeCard(drawNumber);

        System.out.println("Player " + (currentPlayerIndex + 1) + " starts turn.");
        System.out.println("Draw cards: " + drawNumber);
    }

    private int getDrawNumberAtTurnStart(Player player) {
        if (player.getHandCards().size() >= 7) {
            return 0;
        }

        if (player.getHandCards().isEmpty()) {
            return 5;
        }

        return 2;
    }

    public void guiEndTurn() {
        Player currentPlayer = getCurrentPlayer();

        if (checkCurrentPlayerWin()) {
            return;
        }

        if (shouldEnterDiscardPhase(currentPlayer)) {
            isDiscard = true;
            return;
        }

        moveToNextPlayer(currentPlayer);
    }

    private boolean checkCurrentPlayerWin() {
        if (getCurrentPlayer().checkIfWin()) {
            isWin = true;
            return true;
        }

        return false;
    }

    private boolean shouldEnterDiscardPhase(Player player) {
        return player.getHandCards().size() > 7;
    }

    private void moveToNextPlayer(Player currentPlayer) {
        currentPlayer.setOnTurn(false);
        currentPlayer.setUseCardTimes(0);

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

        Player nextPlayer = getCurrentPlayer();
        startTurn(nextPlayer);
    }

    public void discard(Card card) {
        Player currentPlayer = getCurrentPlayer();

        if (!canDiscard(currentPlayer, card)) {
            return;
        }

        currentPlayer.getHandCards().remove(card);
        drawCards.getDiscardPile().add(card);

        if (currentPlayer.getHandCards().size() <= 7) {
            isDiscard = false;
            moveToNextPlayer(currentPlayer);
        }
    }

    private boolean canDiscard(Player currentPlayer, Card card) {
        if (!isDiscard) {
            System.out.println("Not in discard phase.");
            return false;
        }

        if (!currentPlayer.getHandCards().contains(card)) {
            System.out.println("This card is not in current player's hand.");
            return false;
        }

        return true;
    }

    public void playCard(Card card) {
        Player currentPlayer = getCurrentPlayer();

        if (!canPlayCard(currentPlayer, card)) {
            return;
        }

        boolean success = playCardByType(currentPlayer, card);

        if (success) {
            increaseUseCardTimes(currentPlayer);
        }

        checkCurrentPlayerWin();
    }

    private boolean canPlayCard(Player currentPlayer, Card card) {
        if (!currentPlayer.isOnTurn()) {
            System.out.println("It is not this player's turn.");
            return false;
        }

        if (currentPlayer.getUseCardTimes() >= 3) {
            System.out.println("This player has already played 3 cards this turn.");
            return false;
        }

        if (!currentPlayer.getHandCards().contains(card)) {
            System.out.println("This card is not in current player's hand.");
            return false;
        }

        return true;
    }

    private boolean playCardByType(Player currentPlayer, Card card) {
        if (card instanceof MoneyCards) {
            currentPlayer.putMoneyCard(card);
            return true;
        }

        if (card instanceof PropertiesCards propertyCard) {
            currentPlayer.putPropertyCard(propertyCard);
            return true;
        }

        if (card instanceof ActionCards actionCard) {
            if (!canPlayActionCardDirectly(actionCard)) {
                return false;
            }

            currentPlayer.putActionCard(actionCard);
            return true;
        }

        return false;
    }

    private boolean canPlayActionCardDirectly(ActionCards card) {
        ActionCardType type = card.getActionCardType();

        return switch (type) {
            case SLY_DEAL,
                 DEAL_BREAKER,
                 BIRTHDAY,
                 DEBT_COLLECTOR,
                 RENT_WITH_RED_AND_YELLOW,
                 RENT_WITH_ORANGE_AND_PINK,
                 RENT_WITH_BROWN_AND_LIGHT_BLUE,
                 RENT_WITH_BLACK_AND_LIGHT_GREEN,
                 RENT_WITH_DARK_BLUE_AND_DARK_GREEN,
                 RENT_WITH_MULTIPLE_COLOR,
                 DOUBLE_THE_RENT,
                 JUST_SAY_NO -> false;
            default -> true;
        };
    }

    public void finishBirthday(ActionCards birthdayCard) {
        Player currentPlayer = getCurrentPlayer();

        if (birthdayCard == null || birthdayCard.getActionCardType() != ActionCardType.BIRTHDAY) {
            return;
        }

        if (!canPlayCard(currentPlayer, birthdayCard)) {
            return;
        }

        moveActionCardToDiscard(currentPlayer, birthdayCard);

        for (Player player : players) {
            if (player != currentPlayer) {
                addPaymentRequest(currentPlayer, player, 2);
            }
        }

        startNextPaymentRequest();

        increaseUseCardTimes(currentPlayer);
        checkCurrentPlayerWin();
    }

    public void finishSlyDeal(ActionCards slyDealCard, Player targetPlayer, PropertiesCards stolenCard) {
        Player currentPlayer = getCurrentPlayer();

        if (!canFinishSlyDeal(currentPlayer, slyDealCard, targetPlayer, stolenCard)) {
            return;
        }

        moveActionCardToDiscard(currentPlayer, slyDealCard);
        targetPlayer.getPropertyCards().remove(stolenCard);
        currentPlayer.getPropertyCards().add(stolenCard);

        increaseUseCardTimes(currentPlayer);
        checkCurrentPlayerWin();
    }

    private boolean canFinishSlyDeal(Player currentPlayer, ActionCards card, Player targetPlayer, PropertiesCards stolenCard) {
        if (card == null || targetPlayer == null || stolenCard == null) {
            return false;
        }

        if (card.getActionCardType() != ActionCardType.SLY_DEAL) {
            return false;
        }

        if (targetPlayer == currentPlayer) {
            return false;
        }

        if (!canPlayCard(currentPlayer, card)) {
            return false;
        }

        return targetPlayer.getPropertyCards().contains(stolenCard)
                && targetPlayer.canLosePropertyToSlyDeal(stolenCard);
    }

    public void finishDealBreaker(ActionCards dealBreakerCard, Player targetPlayer, ArrayList<PropertiesCards> selectedSet) {
        Player currentPlayer = getCurrentPlayer();

        if (!canFinishDealBreaker(currentPlayer, dealBreakerCard, targetPlayer, selectedSet)) {
            return;
        }

        moveActionCardToDiscard(currentPlayer, dealBreakerCard);
        targetPlayer.getPropertyCards().removeAll(selectedSet);
        currentPlayer.getPropertyCards().addAll(selectedSet);

        increaseUseCardTimes(currentPlayer);
        checkCurrentPlayerWin();
    }

    private boolean canFinishDealBreaker(Player currentPlayer, ActionCards card, Player targetPlayer, ArrayList<PropertiesCards> selectedSet) {
        if (card == null || targetPlayer == null || selectedSet == null || selectedSet.isEmpty()) {
            return false;
        }

        if (card.getActionCardType() != ActionCardType.DEAL_BREAKER) {
            return false;
        }

        if (targetPlayer == currentPlayer) {
            return false;
        }

        if (!canPlayCard(currentPlayer, card)) {
            return false;
        }

        for (PropertiesCards propertyCard : selectedSet) {
            if (!targetPlayer.getPropertyCards().contains(propertyCard)) {
                return false;
            }
        }

        PropertyColor color = selectedSet.get(0).getCurrentColor();

        if (color == null) {
            return false;
        }

        return selectedSet.size() >= color.getAmountToCompleteSet();
    }


    public void finishDebtCollector(ActionCards debtCollectorCard, Player targetPlayer) {
        Player currentPlayer = getCurrentPlayer();

        if (!canFinishDebtCollector(currentPlayer, debtCollectorCard, targetPlayer)) {
            return;
        }

        moveActionCardToDiscard(currentPlayer, debtCollectorCard);
        addPaymentRequest(currentPlayer, targetPlayer, 5);
        startNextPaymentRequest();

        increaseUseCardTimes(currentPlayer);
        checkCurrentPlayerWin();
    }

    private boolean canFinishDebtCollector(Player currentPlayer, ActionCards card, Player targetPlayer) {
        if (card == null || targetPlayer == null) {
            return false;
        }

        if (card.getActionCardType() != ActionCardType.DEBT_COLLECTOR) {
            return false;
        }

        if (targetPlayer == currentPlayer) {
            return false;
        }

        return canPlayCard(currentPlayer, card);
    }

    public void finishTwoColorRent(ActionCards rentCard, PropertyColor selectedColor, boolean useDoubleRent) {
        Player currentPlayer = getCurrentPlayer();

        if (!canFinishTwoColorRent(currentPlayer, rentCard, selectedColor)) {
            return;
        }

        boolean canUseDoubleRent = useDoubleRent && hasDoubleTheRentCard(currentPlayer);

        moveActionCardToDiscard(currentPlayer, rentCard);
        discardDoubleTheRentIfUsed(currentPlayer, canUseDoubleRent);

        int rent = calculateRentForPlayer(currentPlayer, selectedColor);

        if (canUseDoubleRent) {
            rent *= 2;
        }

        for (Player player : players) {
            if (player != currentPlayer) {
                addPaymentRequest(currentPlayer, player, rent);
            }
        }

        startNextPaymentRequest();

        increaseUseCardTimes(currentPlayer);
        checkCurrentPlayerWin();
    }

    public void finishMultipleColorRent(ActionCards rentCard, Player targetPlayer, PropertyColor selectedColor, boolean useDoubleRent) {
        Player currentPlayer = getCurrentPlayer();

        if (!canFinishMultipleColorRent(currentPlayer, rentCard, targetPlayer, selectedColor)) {
            return;
        }

        boolean canUseDoubleRent = useDoubleRent && hasDoubleTheRentCard(currentPlayer);

        moveActionCardToDiscard(currentPlayer, rentCard);
        discardDoubleTheRentIfUsed(currentPlayer, canUseDoubleRent);

        int rent = calculateRentForPlayer(currentPlayer, selectedColor);

        if (canUseDoubleRent) {
            rent *= 2;
        }

        addPaymentRequest(currentPlayer, targetPlayer, rent);
        startNextPaymentRequest();

        increaseUseCardTimes(currentPlayer);
        checkCurrentPlayerWin();
    }

    private boolean canFinishMultipleColorRent(Player currentPlayer, ActionCards card, Player targetPlayer, PropertyColor selectedColor) {
        if (card == null || targetPlayer == null || selectedColor == null) {
            return false;
        }

        if (card.getActionCardType() != ActionCardType.RENT_WITH_MULTIPLE_COLOR) {
            return false;
        }

        if (targetPlayer == currentPlayer) {
            return false;
        }

        if (!canPlayCard(currentPlayer, card)) {
            return false;
        }

        return currentPlayer.canUseRentColor(selectedColor);
    }

    public boolean hasDoubleTheRentCard(Player player) {
        for (Card card : player.getHandCards()) {
            if (card instanceof ActionCards actionCard
                    && actionCard.getActionCardType() == ActionCardType.DOUBLE_THE_RENT) {
                return true;
            }
        }

        return false;
    }

    private void discardDoubleTheRentIfUsed(Player player, boolean useDoubleRent) {
        if (!useDoubleRent) {
            return;
        }

        for (Card card : new ArrayList<>(player.getHandCards())) {
            if (card instanceof ActionCards actionCard
                    && actionCard.getActionCardType() == ActionCardType.DOUBLE_THE_RENT) {
                player.getHandCards().remove(actionCard);
                drawCards.getDiscardPile().add(actionCard);
                return;
            }
        }
    }

    private int calculateRentForPlayer(Player player, PropertyColor color) {
        int propertyCount = 0;
        int rent = 0;

        for (PropertiesCards card : player.getPropertyCards()) {
            if (card.getCurrentColor() == color) {
                propertyCount++;

                if (card.hasHouse()) {
                    rent += 3;
                }

                if (card.hasHotel()) {
                    rent += 4;
                }
            }
        }

        rent += propertyCount;
        return rent;
    }

    private void addPaymentRequest(Player receiver, Player payer, int amount) {
        if (receiver == null || payer == null || amount <= 0) {
            return;
        }

        if (getTotalAssetsValue(payer) <= 0) {
            return;
        }

        paymentRequests.add(new PaymentRequest(receiver, payer, amount));
    }

    private void startNextPaymentRequest() {
        if (currentPaymentRequest != null) {
            return;
        }

        if (paymentRequests.isEmpty()) {
            return;
        }

        currentPaymentRequest = paymentRequests.remove(0);
    }

    public boolean isPaymentSelecting() {
        return currentPaymentRequest != null;
    }

    public PaymentRequest getCurrentPaymentRequest() {
        return currentPaymentRequest;
    }

    public void finishCurrentPayment(ArrayList<Card> selectedCards) {
        if (currentPaymentRequest == null || selectedCards == null || selectedCards.isEmpty()) {
            return;
        }

        Player receiver = currentPaymentRequest.getReceiver();
        Player payer = currentPaymentRequest.getPayer();

        if (!isValidPaymentSelection(payer, selectedCards, currentPaymentRequest.getAmount())) {
            return;
        }

        for (Card card : selectedCards) {
            if (payer.getBankCards().remove(card)) {
                receiver.getBankCards().add(card);
            } else if (card instanceof PropertiesCards propertyCard && payer.getPropertyCards().remove(propertyCard)) {
                receiver.getPropertyCards().add(propertyCard);
            }
        }

        currentPaymentRequest = null;
        startNextPaymentRequest();

        checkCurrentPlayerWin();
    }

    private boolean isValidPaymentSelection(Player payer, ArrayList<Card> selectedCards, int amount) {
        int selectedTotal = getCardsValue(selectedCards);
        int totalAssets = getTotalAssetsValue(payer);

        if (totalAssets <= amount) {
            return selectedTotal == totalAssets;
        }

        return selectedTotal >= amount;
    }

    public int getTotalAssetsValue(Player player) {
        int total = 0;

        for (Card card : player.getBankCards()) {
            total += card.getValue();
        }

        for (PropertiesCards card : player.getPropertyCards()) {
            total += card.getValue();
        }

        return total;
    }

    public int getCardsValue(ArrayList<Card> cards) {
        int total = 0;

        for (Card card : cards) {
            total += card.getValue();
        }

        return total;
    }

    public static class PaymentRequest {
        private Player receiver;
        private Player payer;
        private int amount;

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

    private boolean canFinishTwoColorRent(Player currentPlayer, ActionCards card, PropertyColor selectedColor) {
        if (card == null || selectedColor == null) {
            return false;
        }

        if (!canPlayCard(currentPlayer, card)) {
            return false;
        }

        return switch (card.getActionCardType()) {
            case RENT_WITH_RED_AND_YELLOW ->
                    selectedColor == PropertyColor.RED || selectedColor == PropertyColor.YELLOW;
            case RENT_WITH_ORANGE_AND_PINK ->
                    selectedColor == PropertyColor.ORANGE || selectedColor == PropertyColor.PINK;
            case RENT_WITH_BROWN_AND_LIGHT_BLUE ->
                    selectedColor == PropertyColor.BROWN || selectedColor == PropertyColor.LIGHT_BLUE;
            case RENT_WITH_BLACK_AND_LIGHT_GREEN ->
                    selectedColor == PropertyColor.BLACK || selectedColor == PropertyColor.LIGHT_GREEN;
            case RENT_WITH_DARK_BLUE_AND_DARK_GREEN ->
                    selectedColor == PropertyColor.DARK_BLUE || selectedColor == PropertyColor.DARK_GREEN;
            default -> false;
        };
    }

    private void moveActionCardToDiscard(Player currentPlayer, ActionCards card) {
        currentPlayer.getHandCards().remove(card);
        drawCards.getDiscardPile().add(card);
    }

    private void increaseUseCardTimes(Player player) {
        player.setUseCardTimes(player.getUseCardTimes() + 1);
    }

    private void addPlayer() {
        for (int i = 0; i < 4; i++) {
            players.add(new Player(drawCards));
        }

        addEnemiesForPlayers();
        dealInitialCards();
    }

    private void addEnemiesForPlayers() {
        for (Player player : players) {
            player.getEnemy().clear();

            for (Player otherPlayer : players) {
                if (player != otherPlayer) {
                    player.getEnemy().add(otherPlayer);
                }
            }
        }
    }

    private void dealInitialCards() {
        for (Player player : players) {
            player.takeCard(5);
        }
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public ArrayList<Player> getPlayers() {
        return players;
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
        return isDiscard;
    }
}
