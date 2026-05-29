package logic;

import model.Player;
import model.PropertyColor;

public class DoubleRentDecorator extends RentCalculatorDecorator {
    public DoubleRentDecorator(RentCalculation wrappedCalculator) {
        super(wrappedCalculator);
    }

    @Override
    public int calculateRent(Player player, PropertyColor color) {
        return super.calculateRent(player, color) * 2;
    }
}
