package logic;

import model.Player;
import model.PropertyColor;

public class RentCalculator {
    public int calculateRent(Player player, PropertyColor color) {
        if (player == null || color == null) {
            return 0;
        }

        int propertyCount = countPropertiesByColor(player, color);

        if (propertyCount == 0) {
            return 0;
        }

        int baseRent = calculateBaseRent(color, propertyCount);
        int buildingRent = calculateBuildingRent(player, color);

        return baseRent + buildingRent;
    }

    private int countPropertiesByColor(Player player, PropertyColor color) {
        return PlayerInfoHelper.getPropertyCountByCurrentColor(player, color);
    }

    private int calculateBaseRent(PropertyColor color, int propertyCount) {
        int safeCount = Math.min(propertyCount, color.getAmountToCompleteSet());

        return switch (color) {
            case BROWN -> calculateBrownRent(safeCount);
            case LIGHT_BLUE -> calculateLightBlueRent(safeCount);
            case PINK -> calculatePinkRent(safeCount);
            case ORANGE -> calculateOrangeRent(safeCount);
            case RED -> calculateRedRent(safeCount);
            case YELLOW -> calculateYellowRent(safeCount);
            case BLACK -> calculateBlackRent(safeCount);
            case LIGHT_GREEN -> calculateLightGreenRent(safeCount);
            case DARK_GREEN -> calculateDarkGreenRent(safeCount);
            case DARK_BLUE -> calculateDarkBlueRent(safeCount);
        };
    }

    private int calculateBrownRent(int count) {
        if (count == 1) {
            return 1;
        }

        return 2;
    }

    private int calculateLightBlueRent(int count) {
        if (count == 1) {
            return 1;
        }

        if (count == 2) {
            return 2;
        }

        return 3;
    }

    private int calculatePinkRent(int count) {
        if (count == 1) {
            return 1;
        }

        if (count == 2) {
            return 2;
        }

        return 3;
    }

    private int calculateOrangeRent(int count) {
        if (count == 1) {
            return 1;
        }

        if (count == 2) {
            return 3;
        }

        return 5;
    }

    private int calculateRedRent(int count) {
        if (count == 1) {
            return 2;
        }

        if (count == 2) {
            return 3;
        }

        return 6;
    }

    private int calculateYellowRent(int count) {
        if (count == 1) {
            return 2;
        }

        if (count == 2) {
            return 4;
        }

        return 6;
    }

    private int calculateBlackRent(int count) {
        if (count == 1) {
            return 1;
        }

        if (count == 2) {
            return 2;
        }

        if (count == 3) {
            return 3;
        }

        return 4;
    }

    private int calculateLightGreenRent(int count) {
        if (count == 1) {
            return 1;
        }

        return 2;
    }

    private int calculateDarkGreenRent(int count) {
        if (count == 1) {
            return 2;
        }

        if (count == 2) {
            return 4;
        }

        return 7;
    }

    private int calculateDarkBlueRent(int count) {
        if (count == 1) {
            return 3;
        }

        return 8;
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