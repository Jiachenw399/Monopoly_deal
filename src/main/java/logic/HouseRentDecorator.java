package logic;

import model.Player;
import model.PropertyColor;

public class HouseRentDecorator extends RentCalculatorDecorator {
    private static final int HOUSE_RENT_BONUS = 3;

    public HouseRentDecorator(RentCalculation wrappedCalculator) {
        super(wrappedCalculator);
    }

    @Override
    public int calculateRent(Player player, PropertyColor color) {
        int rent = super.calculateRent(player, color);

        if (PlayerInfoHelper.hasHouse(player, color)) {
            rent += HOUSE_RENT_BONUS;
        }

        return rent;
    }
}
