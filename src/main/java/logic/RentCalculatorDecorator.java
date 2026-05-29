package logic;

import model.Player;
import model.PropertyColor;

public abstract class RentCalculatorDecorator implements RentCalculation {
    private final RentCalculation wrappedCalculator;

    protected RentCalculatorDecorator(RentCalculation wrappedCalculator) {
        this.wrappedCalculator = wrappedCalculator;
    }

    @Override
    public int calculateRent(Player player, PropertyColor color) {
        return wrappedCalculator.calculateRent(player, color);
    }
}
