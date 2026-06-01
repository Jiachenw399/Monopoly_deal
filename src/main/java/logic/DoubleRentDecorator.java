package logic;

import model.Player;
import model.PropertyColor;

public class DoubleRentDecorator extends RentCalculatorDecorator {
    // Creates a DoubleRentDecorator instance.
    public DoubleRentDecorator(RentCalculation wrappedCalculator) {
        super(wrappedCalculator);
    }

    // Calculates rent.
    @Override
    public int calculateRent(Player player, PropertyColor color) {
        return super.calculateRent(player, color) * 2;
    }
}
