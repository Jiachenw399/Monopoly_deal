package GUI;

import javafx.scene.Scene;
import model.Card;
import model.Player;
import model.PropertiesCards;
import model.PropertyColor;

public class GameListener {
    private MainMenu menu;
    private GameScreen gameScreen;
    private logic.Game game;

    public GameListener(MainMenu menu, GameScreen gameScreen, logic.Game game) {
        this.menu = menu;
        this.gameScreen = gameScreen;
        this.game = game;
    }

    public void addListener(Scene scene) {
        scene.setOnMouseClicked(event -> {
            if (!gameScreen.isShow()) {
                return;
            }

            double x = event.getX();
            double y = event.getY();

            if (game.isWin()) {
                return;
            }

            PropertyColor selectedColor = gameScreen.getClickedWildColorButton(x, y);

            if (selectedColor != null) {
                PropertiesCards selectedWildCard = gameScreen.getSelectedWildCard();

                if (selectedWildCard != null) {
                    selectedWildCard.setCurrentColor(selectedColor);
                }

                gameScreen.setSelectedWildCard(null);
                return;
            }

            PropertiesCards clickedWildCard = gameScreen.getClickedWildCard(x, y);

            if (clickedWildCard != null) {
                gameScreen.setSelectedWildCard(clickedWildCard);
                return;
            }

            if (gameScreen.isEndTurnClicked(x, y)) {
                game.guiEndTurn();
                return;
            }

            if (gameScreen.isBackMenuClicked(x, y)) {
                gameScreen.setShow(false);
                menu.setShow(true);
                return;
            }

            int viewedPlayerIndex = gameScreen.getClickedPlayerViewButtonIndex(x, y);

            if (viewedPlayerIndex != -1) {
                gameScreen.setViewedPlayerIndex(viewedPlayerIndex);
                return;
            }

            int handIndex = gameScreen.getClickedHandCardIndex(x, y);

            if (handIndex == -1) {
                return;
            }

            Player currentPlayer = game.getCurrentPlayer();

            if (handIndex >= currentPlayer.getHandCards().size()) {
                return;
            }

            Card selectedCard = currentPlayer.getHandCards().get(handIndex);

            if (game.isDiscard()) {
                game.discard(selectedCard);
                return;
            }

            game.playCard(selectedCard);

            if (currentPlayer.checkIfWin()) {
                game.setWin(true);
            }
        });
    }
}