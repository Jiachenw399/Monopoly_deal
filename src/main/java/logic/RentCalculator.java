package logic;

import model.Player;
import model.PropertyColor;

public class RentCalculator {
    public int calculateRent(Player player, PropertyColor color) {
        return calculateRent(player, color, false);
    }

    public int calculateRent(Player player, PropertyColor color, boolean useDoubleRent) {
        return createCalculator(useDoubleRent).calculateRent(player, color);
    }

    private RentCalculation createCalculator(boolean useDoubleRent) {
        RentCalculation calculator = new BaseRentCalculator();
        calculator = new HouseRentDecorator(calculator);
        calculator = new HotelRentDecorator(calculator);

        if (useDoubleRent) {
            calculator = new DoubleRentDecorator(calculator);
        }

        return calculator;
    }
}
