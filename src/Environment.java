/**
 * Created by Benjamin on 9/18/2017.
 */

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Benjamin on 7/29/2017.
 */
public class Environment implements MoveHistoryIntf {

    //<editor-fold desc="Constants">
    private final SimulationMode DEFAULT_SIM_MODE = SimulationMode.ONE_VS_ONE;

    private final int NUMBER_OF_STRATEGIES = StrategyType.values().length;

    // number of cycles if DYNAMIC_POPULATION_MODEL is chosen
    private final int CYCLES     = 100;
    // number of opponents a strategy will play (to be used when population is implemented)
    private final int ENCOUNTERS = 10;
    // number of rounds a pair of opponents will play
    private final int ROUNDS     = 50;

    // Constants for the score that will be received by a certain strategy. Strat's move is on the left, opp's move is on the right. Syntax like shown --> STRATMOVE_OPPMOVE
    private final int COOP_COOP     = 30;
    private final int COOP_DEFECT   = 0;
    private final int DEFECT_COOP   = 50;
    private final int DEFECT_DEFECT = 10;

    // ONE_VS_ONE match up
    private final StrategyType strategyOne = StrategyType.TESTER;
    private final StrategyType strategyTwo = StrategyType.GRUDGER;

    //<editor-fold desc="Population Constants">
    // Constants for POPULATION MODEL
    private final int STARTING_POP_PER = 100;

    // Custom population model initialization values
    private final int RANDOM_STARTING_POP        = 100;
    private final int GRUDGER_STARTING_POP       = 100;
    private final int TIT_FOR_TAT_STARTING_POP   = 100;
    private final int ALWAYS_DEFECT_STARTING_POP = 100;
    private final int ALWAYS_COOP_STARTING_POP   = 100;
    private final int TESTER_STARTING_POP        = 0;
    private final int TWO_TITS_FOR_TAT_POP       = 100;
    //</editor-fold>

    //<editor-fold desc="Settings">
    // Running settings, should move this to a more user accessible place
    private boolean CUSTOM_POPULATION      = true;
    private boolean PRINT_LOG              = false;
    private boolean PRINT_SCORES           = false;
    private boolean PRINT_LINEUP           = false;
    private boolean PRINT_AVERAGE_SCORE    = false;
    private boolean PRINT_POPULATION_COUNT = true;
    //</editor-fold>

    private int TOTAL_POP;

    private final int STRAT = 0; // Column denominations for 2D array moveHistory
    private final int OPP   = 1;
    //</editor-fold>

    public Environment() {
    }

    public void initialise() {
        System.out.println("Welcome to ChickenSim");
        switch (simulationMode) {
            case ROUND_ROBIN:
                System.out.println("Simulation Mode is 'ROUND_ROBIN'");

                runRoundRobin();
                break;
            case STATIC_POPULATION_MODEL:
                System.out.println("Simulation Mode is 'STATIC_POPULATION_MODEL'");
                setPopulationValues();

                runStaticPopulationModel();
                break;

            case DYNAMIC_POPULATION_MODEL:
                System.out.println("Simulation Mode is 'DYNAMIC_POPULATION_MODEL'");
                setPopulationValues();

                runDynamicPopulationValues();
                break;

            case ONE_VS_ONE:
                System.out.println("Simulation Mode is 'ONE_VS_ONE'");
                PRINT_LOG = true;

                currentStrategy.setStrategyType(strategyOne);
                opponentStrategy.setStrategyType(strategyTwo);

                System.out.printf("\n%s VS %s", strategyOne, strategyTwo);

                play(currentStrategy, opponentStrategy);
                break;

        }
    }

    //<editor-fold desc="Fields">
    private SimulationMode simulationMode = DEFAULT_SIM_MODE;

    // Stores move history for the current match up
    private boolean[][] moveHistory;

    // Stores information for different StrategyTypes
    private double cumulativeScores[] = new double[NUMBER_OF_STRATEGIES];
    private int    populationCount[]  = new int[NUMBER_OF_STRATEGIES];

    private Strategy currentStrategy  = new Strategy(Strategy.DEFAULT_STRATEGY_TYPE, Strategy.CURRENT, this);
    private Strategy opponentStrategy = new Strategy(Strategy.DEFAULT_STRATEGY_TYPE, Strategy.OPP, this);

    private int stratScore;
    private int oppScore;
    private int moveNumber;
    //</editor-fold>

    //<editor-fold desc="Run Methods">
    private void runRoundRobin() {
        resetCumulativeScores();
        for (StrategyType stratType : StrategyType.values()) {
            currentStrategy.setStrategyType(stratType);

            for (StrategyType oppStrat : StrategyType.values()) {
                opponentStrategy.setStrategyType(oppStrat);

                // Play strategies against one another for number of ROUNDS
                play(currentStrategy, opponentStrategy);
            }
        }

        printAverageScore();
    }

    private void runStaticPopulationModel() {
        resetCumulativeScores();
        // Iterate through the strategies by the number of pops that they have
        for (StrategyType strategyType : StrategyType.values()) {
            currentStrategy.setStrategyType(strategyType);

            // Play number of games corresponding the to the population of the StrategyType
            if (populationCount[strategyType.ordinal()] > 0) {
                for (int i = 0; i < populationCount[strategyType.ordinal()]; i++) {
                    playRandom(strategyType);
                }
            }
        }

        printAverageScore();
    }

    private void runDynamicPopulationValues() {
        for (int i = 0; i < StrategyType.values().length; i++) {
            TOTAL_POP += populationCount[i];
        }

        if (PRINT_POPULATION_COUNT) {
            System.out.printf("\nBefore beginning the simulation - ");
            printPopulationCount();
        }
        for (int cycle = 0; cycle < CYCLES; cycle++) {
            runStaticPopulationModel();
            adjustPopulationValues();

            if (PRINT_POPULATION_COUNT) {
                System.out.printf("\nAfter adjusting the population - ");
                printPopulationCount();
            }
        }

    }
    //</editor-fold>

    //<editor-fold desc="Utility Methods">
    private void play(Strategy strategy, Strategy opponent) {
        stratScore = 0;
        oppScore = 0;
        moveHistory = new boolean[2][ROUNDS];
        for (moveNumber = 0; moveNumber < ROUNDS; moveNumber++) {

            Boolean stratMove;
            Boolean oppMove;

            if (moveNumber == 0) {
                stratMove = strategy.playFirstMove();
                oppMove = opponent.playFirstMove();
            } else {
                stratMove = strategy.playMove();
                oppMove = opponent.playMove();
            }
            compare(stratMove, oppMove);

            moveHistory[STRAT][moveNumber] = stratMove;
            moveHistory[OPP][moveNumber] = oppMove;
        }
        cumulativeScores[strategy.getStrategyType().ordinal()] += stratScore;

        printLog();
        printScores();
    }

    private void playRandom(StrategyType strategy) {
        // Set at negative one to subtract itself from possibleOpponents
        int possibleOpponents = -1;
        for (int i = 0; i < NUMBER_OF_STRATEGIES; i++) {
            possibleOpponents += populationCount[i];
        }

        // find opponents
        if (PRINT_LINEUP) {
            System.out.printf("\n\n%s is playing against the following: ", strategy);
        }
        for (int j = 0; j < ENCOUNTERS; j++) {
            int roll = (int) (Math.random() * possibleOpponents);

            // pick a strat
            int count = 0;

            for (StrategyType strategyType : StrategyType.values()) {
                // If the strategyType is the currentStrategy's StrategyType, subtract one from population because it cannot play itself
                count += populationCount[strategyType.ordinal()];
                if (strategyType == currentStrategy.getStrategyType()) {
                    count -= 1;
                }

                if (roll < count) {
//                    System.out.printf("\nroll: %d, count: %d", roll, count);
                    opponentStrategy.setStrategyType(strategyType);
                    break;
                }
            }

            if (PRINT_LINEUP) {
                System.out.printf("\n%d) %s.", j + 1, opponentStrategy.getStrategyType());
            }

            play(currentStrategy, opponentStrategy);
        }
    }

    private void setPopulationValues() {
        // If CUSTOM_POPULATION is true, set to custom values, else set all StrategyType populations to be equal to STARTING_POP_PER value
        if (CUSTOM_POPULATION) {
            // A few custom settings
            populationCount[StrategyType.GRUDGER.ordinal()] = GRUDGER_STARTING_POP;
            populationCount[StrategyType.TIT_FOR_TAT.ordinal()] = TIT_FOR_TAT_STARTING_POP;
            populationCount[StrategyType.ALWAYS_DEFECT.ordinal()] = ALWAYS_DEFECT_STARTING_POP;
            populationCount[StrategyType.ALWAYS_COOP.ordinal()] = ALWAYS_COOP_STARTING_POP;
            populationCount[StrategyType.TESTER.ordinal()] = TESTER_STARTING_POP;
            populationCount[StrategyType.RANDOM.ordinal()] = RANDOM_STARTING_POP;
            populationCount[StrategyType.TWO_TITS_FOR_TAT.ordinal()] = TWO_TITS_FOR_TAT_POP;
        } else {
            Arrays.fill(populationCount, STARTING_POP_PER);
        }
    }

    private void resetCumulativeScores() {
        Arrays.fill(cumulativeScores, 0);
    }

    private void adjustPopulationValues() {
        // First need to find the average value for scores
        double runningTotal = 0;
        for (int i = 0; i < StrategyType.values().length; i++) {
            runningTotal += cumulativeScores[i];
        }

        int totalPop = 0;
        for (int i = 0; i < StrategyType.values().length; i++) {
            totalPop += populationCount[i];
        }
        double averageScore = runningTotal / totalPop;
        System.out.println();

        for (int i = 0; i < StrategyType.values().length; i++) {
            double difference = (cumulativeScores[i] / populationCount[i]) - averageScore;
//            System.out.printf("cS: %.2f, pC: %d, aS %.2f --> Diff: %.2f\n", cumulativeScores[i], populationCount[i], averageScore, difference);
//            int popChange = (int) Math.round(populationCount[i] * 0.05 * 100 * (difference / averageScore));
            double scoreShare = cumulativeScores[i] / runningTotal;
            if (populationCount[i] > 0) {
                System.out.printf("%s: \nCount: %d, Share: %.2f %%\n", StrategyType.values()[i], populationCount[i], scoreShare * 100);
//                populationCount[i] += popChange;
                populationCount[i] = (int) (Math.round(TOTAL_POP * scoreShare));
            } else {
                System.out.printf("%s is EXTINCT\n", StrategyType.values()[i]);
            }
        }

    }

    // This method is the purpose of this code
    private void compare(Boolean stratMove, Boolean oppMove) {
        if (stratMove == ChickenSim.COOPERATE) {
            stratScore += oppMove ? COOP_COOP : COOP_DEFECT;
            oppScore += oppMove ? COOP_COOP : DEFECT_COOP;
        } else if (stratMove == ChickenSim.DEFECT) {
            stratScore += oppMove ? DEFECT_COOP : DEFECT_DEFECT;
            oppScore += oppMove ? COOP_DEFECT : DEFECT_DEFECT;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Printing Methods">
    private void printLog() {
        if (PRINT_LOG) {
            printScores();

            //printing move history
            System.out.println("\nMove History:\n     S-O\n--------");
            for (int i = 0; i < ROUNDS; i++) {
                System.out.printf("%4d %s %s\n", i + 1, (moveHistory[STRAT][i] ? "C" : "D"), (moveHistory[OPP][i] ? "C" : "D"));
            }
        }
    }

    private void printScores() {
        if (PRINT_SCORES) {
            // Print line-up
            System.out.printf("\n%s vs. %s\n", currentStrategy.getStrategyType(), opponentStrategy.getStrategyType());

            //printing results
            System.out.printf("\n%s scored %d\n%s scored %d\n", currentStrategy.getStrategyType(), stratScore, opponentStrategy.getStrategyType(), oppScore);
        }
    }

    private void printPopulationCount() {
        System.out.printf("\nPopulation values: \n");
        for (StrategyType strategyType : StrategyType.values()) {
            System.out.printf("Population of %s: %d\n", strategyType, populationCount[strategyType.ordinal()]);
        }
    }

    // Printing average score
    private void printAverageScore() {
        System.out.println("\nAverage Score/Round\n");

        if (PRINT_AVERAGE_SCORE) {
            for (StrategyType strategy : StrategyType.values()) {
                switch (simulationMode) {
                    case ROUND_ROBIN:
                        System.out.printf("%s: %.2f\n", strategy, cumulativeScores[strategy.ordinal()] / (StrategyType.values().length * ROUNDS));
                        break;
                    case STATIC_POPULATION_MODEL:
                        System.out.printf("%s: %.2f\n", strategy, cumulativeScores[strategy.ordinal()] / (populationCount[strategy.ordinal()] * ENCOUNTERS * ROUNDS));
                        break;
                }
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Move History Intf Methods">
    @Override
    public Boolean previousOpponentMove(Boolean state) {
        return state ? moveHistory[OPP][moveNumber - 1] : moveHistory[STRAT][moveNumber - 1];
    }

    @Override
    public Boolean hasOpponentDef(Boolean state) {
        for (int i = 0; i < moveNumber; i++) {
            if (state) {
                if (moveHistory[OPP][i] == ChickenSim.DEFECT) {
                    return true;
                }
            } else {
                if (moveHistory[STRAT][i] == ChickenSim.DEFECT) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Boolean didOpponentDefTwiceInRow(Boolean state) {
        if (moveNumber > 1) {
            if (state == Strategy.CURRENT) {
                return !moveHistory[OPP][moveNumber - 1] && !moveHistory[OPP][moveNumber - 2] ? true : false;
            } else if (state == Strategy.OPP) {
                return !moveHistory[STRAT][moveNumber - 1] && !moveHistory[STRAT][moveNumber - 2] ? true : false;
            }
        }
//        System.out.println("Could not find if Opponent has DefTwice");
        return false;
    }
    //</editor-fold>

}