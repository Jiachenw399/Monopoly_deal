package logic;

import model.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * A simple greedy AI player that makes reasonable game decisions.
 */
public class SimpleAIPlayer implements AIPlayer {
    private static final long THINK_TIME_MS = 600;
    private static final Random RANDOM = new Random();

    @Override
    public void onTurnStart(GameFacade game, Player player, Runnable onDone) {
        Thread.ofVirtual().start(() -> {
            if (game.isDiscard()) {
                onDiscardPhaseStarted(game, player, onDone);
                return;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(THINK_TIME_MS);
            } catch (InterruptedException ignored) {
            }

            executeTurn(game, player);

            try {
                TimeUnit.MILLISECONDS.sleep(THINK_TIME_MS / 2);
            } catch (InterruptedException ignored) {
            }

            onDone.run();
        });
    }

    @Override
    public void onPaymentRequested(GameFacade game, Player player,
                                   Game.PaymentRequest request,
                                   Runnable onPaymentDone) {
        Thread.ofVirtual().start(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(THINK_TIME_MS);
            } catch (InterruptedException ignored) {
            }

            List<Card> selected = selectPaymentCards(game, player, request.getAmount());

            try {
                TimeUnit.MILLISECONDS.sleep(THINK_TIME_MS / 2);
            } catch (InterruptedException ignored) {
            }

            if (selected != null && !selected.isEmpty()) {
                game.finishCurrentPayment(new ArrayList<>(selected));
            }
            onPaymentDone.run();
        });
    }

    private void executeTurn(GameFacade game, Player player) {
        setWildCardColors(game, player);
        buildHousesAndHotels(game, player);
        playRentCards(game, player);
        playBirthday(game, player);
        playPassGo(game, player);
        playStealCards(game, player);
        playPropertyCards(game, player);
        playMoneyCards(game, player);
        playActionCardsAsMoney(game, player);
    }

    private void setWildCardColors(GameFacade game, Player player) {
        for (PropertiesCards card : player.getPropertyCards()) {
            if (card.isWildCard() && card.getCurrentColor() == null) {
                PropertyColor bestColor = findBestWildColor(player);
                if (bestColor != null) {
                    game.setPropertyColor(player, card, bestColor);
                }
            }
        }
    }

    private PropertyColor findBestWildColor(Player player) {
        PropertyColor best = null;
        int bestCount = 0;
        int bestRent = 0;
        for (PropertyColor color : PropertyColor.values()) {
            int count = player.getPropertyCountByColor(color);
            if (count > bestCount) {
                bestCount = count;
                best = color;
                bestRent = getRentForCount(color, count);
            } else if (count == bestCount && count > 0) {
                int rent = getRentForCount(color, count);
                if (rent > bestRent) {
                    bestRent = rent;
                    best = color;
                }
            }
        }
        return best;
    }

    private int getRentForCount(PropertyColor color, int count) {
        int[] rents = getRentValues(color);
        int idx = Math.min(count, rents.length) - 1;
        return rents[Math.max(0, idx)];
    }

    private int[] getRentValues(PropertyColor color) {
        return switch (color) {
            case BROWN, LIGHT_BLUE -> new int[]{1, 2};
            case PINK, ORANGE -> new int[]{1, 2, 3};
            case RED, YELLOW -> new int[]{1, 2, 3, 4};
            case DARK_GREEN, BLACK -> new int[]{1, 2, 3, 4};
            case DARK_BLUE -> new int[]{2, 4};
            case LIGHT_GREEN -> new int[]{1, 2};
        };
    }

    private void buildHousesAndHotels(GameFacade game, Player player) {
        if (player.getUseCardTimes() >= 3) {
            return;
        }
        for (ActionCards card : collectActionCards(player)) {
            ActionCardType type = card.getActionCardType();
            if (type == ActionCardType.HOUSE) {
                for (PropertyColor color : PropertyColor.values()) {
                    if (canBuildHouse(game, player, color)) {
                        game.finishHouse(card, color);
                        break;
                    }
                }
            } else if (type == ActionCardType.HOTEL) {
                for (PropertyColor color : PropertyColor.values()) {
                    if (canBuildHotel(game, player, color)) {
                        game.finishHotel(card, color);
                        break;
                    }
                }
            }
            if (player.getUseCardTimes() >= 3) {
                break;
            }
        }
    }

    private boolean canBuildHouse(GameFacade game, Player player, PropertyColor color) {
        if (!player.isCompleteSet(color)) {
            return false;
        }
        if (PlayerInfoHelper.hasHouse(player, color) || PlayerInfoHelper.hasHotel(player, color)) {
            return false;
        }
        return hasHouseCard(player);
    }

    private boolean canBuildHotel(GameFacade game, Player player, PropertyColor color) {
        if (!player.isCompleteSet(color)) {
            return false;
        }
        if (!PlayerInfoHelper.hasHouse(player, color) || PlayerInfoHelper.hasHotel(player, color)) {
            return false;
        }
        return hasHotelCard(player);
    }

    private boolean hasHouseCard(Player player) {
        for (Card card : player.getHandCards()) {
            if (card instanceof ActionCards ac && ac.getActionCardType() == ActionCardType.HOUSE) {
                return true;
            }
        }
        return false;
    }

    private boolean hasHotelCard(Player player) {
        for (Card card : player.getHandCards()) {
            if (card instanceof ActionCards ac && ac.getActionCardType() == ActionCardType.HOTEL) {
                return true;
            }
        }
        return false;
    }

    private void playRentCards(GameFacade game, Player player) {
        if (player.getUseCardTimes() >= 3) {
            return;
        }
        for (ActionCards card : collectActionCards(player)) {
            ActionCardType type = card.getActionCardType();
            if (type == ActionCardType.RENT_WITH_MULTIPLE_COLOR) {
                Player target = selectRentTarget(game, player);
                if (target != null) {
                    PropertyColor color = selectBestRentColor(player);
                    if (color != null) {
                        boolean useDouble = shouldUseDoubleRent(player);
                        game.finishMultipleColorRent(card, target, color, useDouble);
                        if (player.getUseCardTimes() >= 3) {
                            return;
                        }
                    }
                }
            }
        }
        for (ActionCards card : collectActionCards(player)) {
            ActionCardType type = card.getActionCardType();
            if (type.name().startsWith("RENT_WITH_")) {
                List<PropertyColor> colors = getRentColors(type);
                if (colors == null || colors.isEmpty()) {
                    continue;
                }
                PropertyColor chosen = null;
                int best = 0;
                for (PropertyColor c : colors) {
                    int count = player.getPropertyCountByColor(c);
                    if (count > 0) {
                        int rent = getRentForCount(c, count);
                        if (count > best) {
                            best = count;
                            chosen = c;
                        }
                    }
                }
                if (chosen != null) {
                    boolean useDouble = shouldUseDoubleRent(player);
                    game.finishTwoColorRent(card, chosen, useDouble);
                    if (player.getUseCardTimes() >= 3) {
                        return;
                    }
                }
            }
        }
    }

    private List<PropertyColor> getRentColors(ActionCardType type) {
        return switch (type) {
            case RENT_WITH_RED_AND_YELLOW -> List.of(PropertyColor.RED, PropertyColor.YELLOW);
            case RENT_WITH_ORANGE_AND_PINK -> List.of(PropertyColor.ORANGE, PropertyColor.PINK);
            case RENT_WITH_BROWN_AND_LIGHT_BLUE -> List.of(PropertyColor.BROWN, PropertyColor.LIGHT_BLUE);
            case RENT_WITH_BLACK_AND_LIGHT_GREEN -> List.of(PropertyColor.BLACK, PropertyColor.LIGHT_GREEN);
            case RENT_WITH_DARK_BLUE_AND_DARK_GREEN -> List.of(PropertyColor.DARK_BLUE, PropertyColor.DARK_GREEN);
            default -> null;
        };
    }

    private boolean shouldUseDoubleRent(Player player) {
        if (player.getUseCardTimes() >= 2) {
            return false;
        }
        for (Card card : player.getHandCards()) {
            if (card instanceof ActionCards ac && ac.getActionCardType() == ActionCardType.DOUBLE_THE_RENT) {
                return true;
            }
        }
        return false;
    }

    private void playBirthday(GameFacade game, Player player) {
        if (player.getUseCardTimes() >= 3) {
            return;
        }
        for (ActionCards card : collectActionCards(player)) {
            if (card.getActionCardType() == ActionCardType.BIRTHDAY) {
                game.finishBirthday(card);
                if (player.getUseCardTimes() >= 3) {
                    return;
                }
            }
        }
    }

    private void playPassGo(GameFacade game, Player player) {
        if (player.getUseCardTimes() >= 3) {
            return;
        }
        for (ActionCards card : collectActionCards(player)) {
            if (card.getActionCardType() == ActionCardType.PASS_GO) {
                game.finishPassGo(card);
                if (player.getUseCardTimes() >= 3) {
                    return;
                }
            }
        }
    }

    private void playStealCards(GameFacade game, Player player) {
        if (player.getUseCardTimes() >= 3) {
            return;
        }
        for (ActionCards card : collectActionCards(player)) {
            ActionCardType type = card.getActionCardType();
            if (type == ActionCardType.SLY_DEAL) {
                PropertiesCards steal = findBestStealTarget(game, player);
                if (steal != null) {
                    Player target = findOwner(game, steal);
                    if (target != null) {
                        game.finishSlyDeal(card, target, steal);
                        if (player.getUseCardTimes() >= 3) {
                            return;
                        }
                    }
                }
            } else if (type == ActionCardType.DEAL_BREAKER) {
                List<PropertiesCards> stealSet = findBestDealBreakerTarget(game, player);
                if (stealSet != null) {
                    Player target = findOwner(game, stealSet.get(0));
                    if (target != null) {
                        game.finishDealBreaker(card, target, new ArrayList<>(stealSet));
                        if (player.getUseCardTimes() >= 3) {
                            return;
                        }
                    }
                }
            } else if (type == ActionCardType.FORCED_DEAL) {
                ForcedDealPair deal = findBestForcedDealTarget(game, player);
                if (deal != null) {
                    game.finishForcedDeal(card, deal.target, deal.myCard, deal.theirCard);
                    if (player.getUseCardTimes() >= 3) {
                        return;
                    }
                }
            } else if (type == ActionCardType.DEBT_COLLECTOR) {
                Player target = selectDebtCollectorTarget(game, player);
                if (target != null) {
                    game.finishDebtCollector(card, target);
                    if (player.getUseCardTimes() >= 3) {
                        return;
                    }
                }
            }
        }
    }

    private PropertiesCards findBestStealTarget(GameFacade game, Player player) {
        PropertiesCards best = null;
        int bestValue = 0;
        for (Player target : game.getPlayers()) {
            if (target == player || target.isAI()) {
                continue;
            }
            for (PropertiesCards card : target.getPropertyCards()) {
                if (target.canLosePropertyToSlyDeal(card)) {
                    int value = card.getValue();
                    if (value > bestValue) {
                        bestValue = value;
                        best = card;
                    }
                }
            }
        }
        return best;
    }

    private List<PropertiesCards> findBestDealBreakerTarget(GameFacade game, Player player) {
        List<PropertiesCards> best = null;
        int bestSetSize = 0;
        for (Player target : game.getPlayers()) {
            if (target == player || target.isAI()) {
                continue;
            }
            for (PropertyColor color : PropertyColor.values()) {
                List<PropertiesCards> set = PlayerInfoHelper.getCompleteSetByColor(target, color);
                if (!set.isEmpty()) {
                    if (set.size() > bestSetSize) {
                        bestSetSize = set.size();
                        best = set;
                    }
                }
            }
        }
        return best;
    }

    private Player findOwner(GameFacade game, PropertiesCards card) {
        for (Player p : game.getPlayers()) {
            if (p.getPropertyCards().contains(card)) {
                return p;
            }
        }
        return null;
    }

    private static class ForcedDealPair {
        Player target;
        PropertiesCards myCard;
        PropertiesCards theirCard;

        ForcedDealPair(Player target, PropertiesCards myCard, PropertiesCards theirCard) {
            this.target = target;
            this.myCard = myCard;
            this.theirCard = theirCard;
        }
    }

    private ForcedDealPair findBestForcedDealTarget(GameFacade game, Player player) {
        ForcedDealPair best = null;
        int bestGain = 0;
        for (Player target : game.getPlayers()) {
            if (target == player || target.isAI()) {
                continue;
            }
            for (PropertiesCards myCard : player.getPropertyCards()) {
                if (!PlayerInfoHelper.canBeStolenBySlyDeal(player, myCard)) {
                    continue;
                }
                for (PropertiesCards theirCard : target.getPropertyCards()) {
                    if (!PlayerInfoHelper.canBeStolenBySlyDeal(target, theirCard)) {
                        continue;
                    }
                    int gain = theirCard.getValue() - myCard.getValue();
                    if (gain > bestGain) {
                        bestGain = gain;
                        best = new ForcedDealPair(target, myCard, theirCard);
                    }
                }
            }
        }
        return best;
    }

    private Player selectRentTarget(GameFacade game, Player player) {
        Player best = null;
        int bestAssets = 0;
        for (Player target : game.getPlayers()) {
            if (target == player || target.isAI()) {
                continue;
            }
            int assets = game.getTotalAssetsValue(target);
            if (assets > bestAssets) {
                bestAssets = assets;
                best = target;
            }
        }
        return best;
    }

    private Player selectDebtCollectorTarget(GameFacade game, Player player) {
        Player best = null;
        int bestAssets = 0;
        for (Player target : game.getPlayers()) {
            if (target == player || target.isAI()) {
                continue;
            }
            int assets = game.getTotalAssetsValue(target);
            if (assets > bestAssets) {
                bestAssets = assets;
                best = target;
            }
        }
        return best;
    }

    private PropertyColor selectBestRentColor(Player player) {
        PropertyColor best = null;
        int bestCount = 0;
        int bestRent = 0;
        for (PropertyColor color : PropertyColor.values()) {
            if (player.getPropertyCountByColor(color) > 0) {
                int count = player.getPropertyCountByColor(color);
                int rent = getRentForCount(color, count);
                if (count > bestCount) {
                    bestCount = count;
                    bestRent = rent;
                    best = color;
                } else if (count == bestCount && rent > bestRent) {
                    bestRent = rent;
                    best = color;
                }
            }
        }
        return best;
    }

    private void playPropertyCards(GameFacade game, Player player) {
        if (player.getUseCardTimes() >= 3) {
            return;
        }
        List<Card> toPlay = new ArrayList<>();
        for (Card card : player.getHandCards()) {
            if (card instanceof PropertiesCards) {
                toPlay.add(card);
            }
        }
        for (Card card : toPlay) {
            if (player.getUseCardTimes() < 3) {
                game.playCard(card);
            }
        }
    }

    private void playMoneyCards(GameFacade game, Player player) {
        if (player.getUseCardTimes() >= 3) {
            return;
        }
        List<Card> toPlay = new ArrayList<>();
        for (Card card : player.getHandCards()) {
            if (card instanceof MoneyCards) {
                toPlay.add(card);
            }
        }
        for (Card card : toPlay) {
            if (player.getUseCardTimes() < 3) {
                game.playCard(card);
            }
        }
    }

    private void playActionCardsAsMoney(GameFacade game, Player player) {
        if (player.getUseCardTimes() >= 3) {
            return;
        }
        List<ActionCards> toPlay = new ArrayList<>();
        for (Card card : player.getHandCards()) {
            if (card instanceof ActionCards ac) {
                ActionCardType type = ac.getActionCardType();
                if (!isValuableAction(type)) {
                    toPlay.add(ac);
                }
            }
        }
        for (ActionCards card : toPlay) {
            if (player.getUseCardTimes() < 3) {
                game.playActionCardAsMoney(card);
            }
        }
    }

    private boolean isValuableAction(ActionCardType type) {
        return type == ActionCardType.SLY_DEAL
                || type == ActionCardType.DEAL_BREAKER
                || type == ActionCardType.FORCED_DEAL
                || type == ActionCardType.DEBT_COLLECTOR
                || type == ActionCardType.RENT_WITH_MULTIPLE_COLOR
                || type == ActionCardType.HOUSE
                || type == ActionCardType.HOTEL
                || type.name().startsWith("RENT_WITH_");
    }

    private List<ActionCards> collectActionCards(Player player) {
        List<ActionCards> result = new ArrayList<>();
        for (Card card : player.getHandCards()) {
            if (card instanceof ActionCards ac) {
                result.add(ac);
            }
        }
        return result;
    }

    private List<Card> selectPaymentCards(GameFacade game, Player player, int amount) {
        List<Card> result = new ArrayList<>();
        int totalAssets = game.getTotalAssetsValue(player);
        if (totalAssets <= amount) {
            result.addAll(player.getBankCards());
            result.addAll(new ArrayList<>(player.getPropertyCards()));
            return result;
        }
        int currentValue = 0;
        List<Card> bankSorted = new ArrayList<>(player.getBankCards());
        bankSorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        for (Card card : bankSorted) {
            if (currentValue >= amount) {
                break;
            }
            result.add(card);
            currentValue += card.getValue();
        }
        if (currentValue < amount) {
            List<Card> props = new ArrayList<>(player.getPropertyCards());
            props.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
            for (Card card : props) {
                if (currentValue >= amount) {
                    break;
                }
                result.add(card);
                currentValue += card.getValue();
            }
        }
        return result;
    }

    @Override
    public void onDiscardPhaseStarted(GameFacade game, Player player, Runnable onDone) {
        Thread.ofVirtual().start(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(THINK_TIME_MS);
            } catch (InterruptedException ignored) {
            }

            while (game.isDiscard() && player.getHandCards().size() > 7) {
                Card toDiscard = selectCardToDiscard(player);
                if (toDiscard != null) {
                    game.discard(toDiscard);
                }
            }

            try {
                TimeUnit.MILLISECONDS.sleep(THINK_TIME_MS / 2);
            } catch (InterruptedException ignored) {
            }

            onDone.run();
        });
    }

    private Card selectCardToDiscard(Player player) {
        List<Card> hand = player.getHandCards();
        if (hand.isEmpty()) {
            return null;
        }

        Card lowestValue = hand.get(0);
        int lowest = lowestValue.getValue();
        for (Card card : hand) {
            if (card.getValue() < lowest) {
                lowest = card.getValue();
                lowestValue = card;
            }
        }
        return lowestValue;
    }
}
