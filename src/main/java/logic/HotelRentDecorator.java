package logic;

import model.Player;
import model.PropertyColor;

public class HotelRentDecorator extends RentCalculatorDecorator {
    private static final int HOTEL_RENT_BONUS = 4;

    public HotelRentDecorator(RentCalculation wrappedCalculator) {
        super(wrappedCalculator);
    }

    @Override
    public int calculateRent(Player player, PropertyColor color) {
        int rent = super.calculateRent(player, color);

        if (PlayerInfoHelper.hasHotel(player, color)) {
            rent += HOTEL_RENT_BONUS;
        }

        return rent;
    }
}
