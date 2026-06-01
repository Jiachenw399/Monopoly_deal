package logic;

import model.Player;
import model.PropertyColor;

public abstract class RentCalculatorDecorator implements RentCalculation {
    private final RentCalculation wrappedCalculator;

    // Creates a RentCalculatorDecorator instance.
    protected RentCalculatorDecorator(RentCalculation wrappedCalculator) {
        this.wrappedCalculator = wrappedCalculator;
    }

    // Calculates rent.
    @Override
    public int calculateRent(Player player, PropertyColor color) {
        return wrappedCalculator.calculateRent(player, color);
    }
}
