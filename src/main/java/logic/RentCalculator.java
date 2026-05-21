package logic;

import model.Player;
import model.PropertyColor;

public class RentCalculator {
    public int calculateRent(Player player, PropertyColor color) {

        int propertyCount = PlayerInfoHelper.getPropertyCountByCurrentColor(player, color);

        return calculateBaseRent(color,propertyCount)+calculateBuildingRent(player,color);
    }

    private int calculateBaseRent(PropertyColor color, int propertyCount) {
        int count = Math.min(propertyCount, color.getAmountToCompleteSet());

        int[] rents = switch (color) {
            case BROWN, LIGHT_GREEN -> new int[]{1, 2};
            case LIGHT_BLUE, PINK -> new int[]{1, 2, 3};
            case ORANGE -> new int[]{1, 3, 5};
            case RED -> new int[]{2, 3, 6};
            case YELLOW -> new int[]{2, 4, 6};
            case BLACK -> new int[]{1, 2, 3, 4};
            case DARK_GREEN -> new int[]{2, 4, 7};
            case DARK_BLUE -> new int[]{3, 8};
        };

        return rents[count - 1];
    }

    private int calculateBuildingRent(Player player, PropertyColor color) {
        int rent = 0;

        if (PlayerInfoHelper.hasHouse(player, color)) {
            rent += 3;
        }

        if (PlayerInfoHelper.hasHotel(player, color)) {
            rent += 4;
        }

        return rent;
    }
}