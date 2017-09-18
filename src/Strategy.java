/**
 * Created by Benjamin on 9/18/2017.
 */

public class Strategy {

    // TODO: Not sure that Tester works as intended. Perhaps allow to COOP right after being defected before switching to TIT_FOR_TAT

    public static boolean CURRENT = true;
    public static boolean OPP = false;

    public static StrategyType DEFAULT_STRATEGY_TYPE = StrategyType.RANDOM;

    public Strategy(StrategyType strategyType, boolean state, MoveHistoryIntf moveHistory) {
        setStrategyType(strategyType);
        setState(state);
        setMoveHistoryIntf(moveHistory);
    }

    private StrategyType strategyType;
    private MoveHistoryIntf moveHistory;
    private boolean state;
    private boolean wasDefectedAgainstBefore;

    // Play the first move for the corresponding strategy
    public boolean playFirstMove() {
        switch (strategyType) {
            case TIT_FOR_TAT:
                return ChickenSim.COOPERATE;

            case ALWAYS_DEFECT:
                return ChickenSim.DEFECT;

            case ALWAYS_COOP:
                return ChickenSim.COOPERATE;

            case GRUDGER:
                return ChickenSim.COOPERATE;

            case TESTER:
                return ChickenSim.COOPERATE;

            case TWO_TITS_FOR_TAT:
                return ChickenSim.COOPERATE;
        }
        // Default is random
        return Math.random() < .5 ? ChickenSim.COOPERATE : ChickenSim.DEFECT;
    }

    public Boolean playMove() {
        switch (strategyType) {
            case TIT_FOR_TAT:
                return moveHistory.previousOpponentMove(getState());

            case ALWAYS_DEFECT:
                return ChickenSim.DEFECT;

            case ALWAYS_COOP:
                return  ChickenSim.COOPERATE;

            case GRUDGER:
                return moveHistory.hasOpponentDef(getState()) ? ChickenSim.DEFECT : ChickenSim.COOPERATE;

            case TESTER: // Starts off cooperating and occasionally prods the opponent. If the opponent retaliates, set behaviour to be similar to TIT_FOR_TAT
                if  (!wasDefectedAgainstBefore) {
                    if (moveHistory.previousOpponentMove(getState()) == ChickenSim.COOPERATE) {
                        // Cooperate most of the time if opponent has not defected, but prod with defect 5% of the time
                        return (Math.random() < .05) ? ChickenSim.DEFECT : ChickenSim.COOPERATE;
                    } else {
                        wasDefectedAgainstBefore = true;
                        return ChickenSim.COOPERATE;
                    }
                } else {
                    return moveHistory.previousOpponentMove(getState());
                }
//                if (moveHistory.hasOpponentDef(getState())) {
//                    return moveHistory.previousOpponentMove(getState());
//                } else {
//                    return (Math.random() < .05) ? ChickenSim.DEFECT : ChickenSim.COOPERATE; // Cooperate most of the time if opponent has not defected, but prod with defect 5% of the time
//                }

            case TWO_TITS_FOR_TAT:
                return moveHistory.didOpponentDefTwiceInRow(getState()) ? ChickenSim.DEFECT : ChickenSim.COOPERATE;

        }
        // Default is random
        return Math.random() < .5 ? ChickenSim.COOPERATE : ChickenSim.DEFECT;
    }

    private void resetStrategy() {
        wasDefectedAgainstBefore = false;
    }

    //<editor-fold desc="Setters/Getters">
    public StrategyType getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(StrategyType strategyType) {
        this.strategyType = strategyType;
        resetStrategy();
    }

    public MoveHistoryIntf getMoveHistory() {
        return moveHistory;
    }

    public void setMoveHistoryIntf(MoveHistoryIntf moveHistory) {
        this.moveHistory = moveHistory;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
    //</editor-fold>

}