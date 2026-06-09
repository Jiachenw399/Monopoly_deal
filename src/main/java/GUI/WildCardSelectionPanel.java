package GUI;

import logic.Game;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

// Handles choosing and changing wild card colors.
public class WildCardSelectionPanel {
    private final Game game;

    private PropertiesCards selectedWildCard;

    private final double colorButtonX = 520;
    private final double colorButtonY = 255;
    private final double colorButtonWidth = 115;
    private final double colorButtonHeight = 28;
    private final double colorButtonGapX = 10;
    private final double colorButtonGapY = 8;
    private final int colorButtonsPerRow = 2;

    // Creates the panel with no selected wild card.
    public WildCardSelectionPanel(Game game) {
        this.game = game;
        this.selectedWildCard = null;
    }

    // Returns the currently selected wild card.
    public PropertiesCards getSelectedWildCard() {
        return selectedWildCard;
    }

    // Stores the wild card selected by the user.
    public void setSelectedWildCard(PropertiesCards selectedWildCard) {
        this.selectedWildCard = selectedWildCard;
    }

    // Clears the selected wild card.
    public void clearSelection() {
        selectedWildCard = null;
    }

    // Returns the clicked color option for the selected wild card.
    public PropertyColor getClickedWildColorButton(double mouseX, double mouseY) {
        if (selectedWildCard == null) {
            return null;
        }

        for (int i = 0; i < selectedWildCard.getType().getColors().size(); i++) {
            int row = i / colorButtonsPerRow;
            int col = i % colorButtonsPerRow;

            double buttonX = colorButtonX + col * (colorButtonWidth + colorButtonGapX);
            double buttonY = colorButtonY + row * (colorButtonHeight + colorButtonGapY);

            if (ScreenDrawHelper.handleButtonClick(mouseX, mouseY, buttonX, buttonY, colorButtonWidth, colorButtonHeight)) {
                return selectedWildCard.getType().getColors().get(i);
            }
        }

        return null;
    }
}