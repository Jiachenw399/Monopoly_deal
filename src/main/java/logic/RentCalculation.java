package logic;

import model.Player;
import model.PropertyColor;

public interface RentCalculation {
    int calculateRent(Player player, PropertyColor color);
}
