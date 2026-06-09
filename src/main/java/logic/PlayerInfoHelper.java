package logic;

import model.Card;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

import java.util.ArrayList;

public class PlayerInfoHelper {
    // Creates a PlayerInfoHelper instance.
    private PlayerInfoHelper() {
    }

    // Finds bank total.
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

    // Finds completed set count.
    public static int getCompletedSetCount(Player player) {
        int completedSets = 0;

        if (player == null) {
            return completedSets;
        }

        for (PropertyColor color : PropertyColor.values()) {
            if (getPropertyCountByCurrentColor(player, color) >= color.getAmountToCompleteSet()) {
                completedSets++;
            }
        }

        return completedSets;
    }

    // Finds property count by current color.
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

    // Checks whether this has property color.
    public static boolean hasPropertyColor(Player player, PropertyColor color) {
        return getPropertyCountByCurrentColor(player, color) > 0;
    }

    // Checks whether this has a complete property set.
    public static boolean isCompleteSet(Player player, PropertyColor color) {
        if (color == null) {
            return false;
        }

        return getPropertyCountByCurrentColor(player, color) >= color.getAmountToCompleteSet();
    }

    // Checks whether this has house.
    public static boolean hasHouse(Player player, PropertyColor color) {
        return hasBuilding(player, color, true);
    }

    // Checks whether this has hotel.
    public static boolean hasHotel(Player player, PropertyColor color) {
        return hasBuilding(player, color, false);
    }

    // Finds complete set by color.
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

        if (result.size() < color.getAmountToCompleteSet()) {
            result.clear();
        }

        return result;
    }

    // Checks whether this can be stolen by sly deal.
    public static boolean canBeStolenBySlyDeal(Player targetPlayer, PropertiesCards card) {
        if (targetPlayer == null || card == null) {
            return false;
        }

        PropertyColor color = card.getCurrentColor();

        if (color == null) {
            return true;
        }

        return getPropertyCountByCurrentColor(targetPlayer, color) < color.getAmountToCompleteSet();
    }

    // Checks whether this has building.
    private static boolean hasBuilding(Player player, PropertyColor color, boolean checkHouse) {
        if (player == null || color == null) {
            return false;
        }

        for (PropertiesCards card : player.getPropertyCards()) {
            if (card.getCurrentColor() == color && hasSelectedBuilding(card, checkHouse)) {
                return true;
            }
        }

        return false;
    }

    // Checks whether this has selected building.
    private static boolean hasSelectedBuilding(PropertiesCards card, boolean checkHouse) {
        if (checkHouse) {
            return card.hasHouse();
        }

        return card.hasHotel();
    }
}
