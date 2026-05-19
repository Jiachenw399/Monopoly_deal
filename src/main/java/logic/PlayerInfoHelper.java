package logic;

import model.Card;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;

public class PlayerInfoHelper {
    private PlayerInfoHelper() {
    }

    public static int getBankTotal(Player player) {
        int total = 0;

        if (player == null) {
            return total;
        }

        for (Card card : player.getBankCards()) {
            total += card.getValue();
        }

        return total;
    }

    public static int getCompletedSetCount(Player player) {
        int completedSets = 0;

        if (player == null) {
            return completedSets;
        }

        for (PropertyColor color : PropertyColor.values()) {
            int current = getPropertyCountByCurrentColor(player, color);

            if (current >= color.getAmountToCompleteSet()) {
                completedSets++;
            }
        }

        return completedSets;
    }

    public static int getPropertyCountByCurrentColor(Player player, PropertyColor color) {
        int count = 0;

        if (player == null || color == null) {
            return count;
        }

        for (PropertiesCards card : player.getPropertyCards()) {
            if (card.getCurrentColor() == color) {
                count++;
            }
        }

        return count;
    }

    public static int getPropertyCountByColor(Player player, PropertyColor color) {
        return getPropertyCountByCurrentColor(player, color);
    }

    public static boolean hasPropertyColor(Player player, PropertyColor color) {
        return getPropertyCountByCurrentColor(player, color) > 0;
    }

    public static boolean hasHouse(Player player, PropertyColor color) {
        if (player == null || color == null) {
            return false;
        }

        for (PropertiesCards card : player.getPropertyCards()) {
            if (card.getCurrentColor() == color && card.hasHouse()) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasHotel(Player player, PropertyColor color) {
        if (player == null || color == null) {
            return false;
        }

        for (PropertiesCards card : player.getPropertyCards()) {
            if (card.getCurrentColor() == color && card.hasHotel()) {
                return true;
            }
        }

        return false;
    }

    public static ArrayList<PropertiesCards> getCompleteSetByColor(Player player, PropertyColor color) {
        ArrayList<PropertiesCards> result = new ArrayList<>();

        if (player == null || color == null) {
            return result;
        }

        for (PropertiesCards card : player.getPropertyCards()) {
            if (card.getCurrentColor() == color) {
                result.add(card);
            }
        }

        if (result.size() >= color.getAmountToCompleteSet()) {
            return result;
        }

        return new ArrayList<>();
    }

    public static boolean canBeStolenBySlyDeal(Player targetPlayer, PropertiesCards card) {
        if (targetPlayer == null || card == null) {
            return false;
        }

        PropertyColor color = card.getCurrentColor();

        if (color == null) {
            return true;
        }

        int count = getPropertyCountByCurrentColor(targetPlayer, color);
        return count < color.getAmountToCompleteSet();
    }
}