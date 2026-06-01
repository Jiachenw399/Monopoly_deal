package logic;

import model.Player;
import model.PropertyColor;

public interface RentCalculation {
    // Calculates rent for a player's property color.
    int calculateRent(Player player, PropertyColor color);
}
