package GUI;

import logic.GameFacade;
import model.ActionCards;

public abstract class GameClickActionAdapter implements GameClickActions {
    protected final GameFacade game;
    protected final GameScreen gameScreen;

    protected GameClickActionAdapter(GameFacade game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
    }

    @Override
    public void startActionCardFlow(ActionCards actionCard) {
        switch (actionCard.getActionCardType()) {
            case SLY_DEAL -> gameScreen.startSlyDealSelection(actionCard);
            case RENT_WITH_MULTIPLE_COLOR -> gameScreen.startMultipleColorRentSelection(actionCard);
            case HOUSE, HOTEL -> gameScreen.startBuildingSelection(actionCard);
            case FORCED_DEAL -> gameScreen.startForcedDealSelection(actionCard);
            case BIRTHDAY, PASS_GO -> finishImmediateAction(actionCard);
            case DEBT_COLLECTOR -> gameScreen.startDebtCollectorSelection(actionCard);
            case DEAL_BREAKER -> gameScreen.startDealBreakerSelection(actionCard);
            case RENT_WITH_DARK_BLUE_AND_DARK_GREEN,
                 RENT_WITH_BROWN_AND_LIGHT_BLUE,
                 RENT_WITH_BLACK_AND_LIGHT_GREEN,
                 RENT_WITH_RED_AND_YELLOW,
                 RENT_WITH_ORANGE_AND_PINK -> gameScreen.startTwoColorRentSelection(actionCard);
            default -> {
            }
        }
    }

    protected abstract void finishImmediateAction(ActionCards actionCard);
}
