package GUI;

import logic.GameFacade;
import model.ActionCardType;
import model.ActionCards;
import model.Player;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class GameClickActionAdapter implements GameClickActions {
    protected final GameFacade game;
    protected final GameScreen gameScreen;
    private final Map<ActionCardType, Consumer<ActionCards>> actionFlows;

    // Creates a GameClickActionAdapter instance.
    protected GameClickActionAdapter(GameFacade game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.actionFlows = createActionFlows();
    }

    // Starts action card flow.
    @Override
    public void startActionCardFlow(ActionCards actionCard) {
        Consumer<ActionCards> flow = actionFlows.get(actionCard.getActionCardType());

        if (flow != null) {
            flow.accept(actionCard);
        }
    }

    // Creates action flows.
    private Map<ActionCardType, Consumer<ActionCards>> createActionFlows() {
        Map<ActionCardType, Consumer<ActionCards>> flows = new EnumMap<>(ActionCardType.class);
        flows.put(ActionCardType.SLY_DEAL, gameScreen::startSlyDealSelection);
        flows.put(ActionCardType.RENT_WITH_MULTIPLE_COLOR, gameScreen::startMultipleColorRentSelection);
        flows.put(ActionCardType.HOUSE, gameScreen::startBuildingSelection);
        flows.put(ActionCardType.HOTEL, gameScreen::startBuildingSelection);
        flows.put(ActionCardType.FORCED_DEAL, gameScreen::startForcedDealSelection);
        flows.put(ActionCardType.BIRTHDAY, this::finishImmediateAction);
        flows.put(ActionCardType.PASS_GO, this::finishImmediateAction);
        flows.put(ActionCardType.DEBT_COLLECTOR, gameScreen::startDebtCollectorSelection);
        flows.put(ActionCardType.DEAL_BREAKER, gameScreen::startDealBreakerSelection);

        for (ActionCardType type : ActionCardType.values()) {
            if (type.isTwoColorRentCard()) {
                flows.put(type, gameScreen::startTwoColorRentSelection);
            }
        }

        return flows;
    }

    @Override
    public void onMultipleColorRentTargetPicked(Player target) {
        gameScreen.showMultipleColorRentTargetDetail(target);
    }

    @Override
    public void onForcedDealTargetPicked(Player target) {
        gameScreen.showForcedDealTargetDetail(target);
    }

    @Override
    public void onDebtCollectorTargetPicked(ActionCards card, Player target) {
        gameScreen.setSelectedDebtCollectorTarget(target);
    }

    @Override
    public void onDealBreakerSetPicked(GameScreen.DealBreakerChoice choice) {
        gameScreen.showDealBreakerDetailChoice(choice);
    }

    protected abstract void finishImmediateAction(ActionCards actionCard);
}
