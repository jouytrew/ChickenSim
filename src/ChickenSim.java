/**
 * Created by Benjamin on 9/18/2017.
 */

public class ChickenSim {
    public static boolean COOPERATE = true;
    public static boolean DEFECT = false;

    public ChickenSim() {
    }

    public static void main(String[] args) {
        Environment simulation = new Environment();
        simulation.initialise();
    }
}
