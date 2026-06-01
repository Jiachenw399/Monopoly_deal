package logic;

import model.Player;
import model.PropertyColor;

public class BaseRentCalculator implements RentCalculation {
    // Calculates rent.
    @Override
    public int calculateRent(Player player, PropertyColor color) {
        int propertyCount = PlayerInfoHelper.getPropertyCountByCurrentColor(player, color);
        int count = Math.min(propertyCount, color.getAmountToCompleteSet());
        return color.getRentForPropertyCount(count);
    }
}
