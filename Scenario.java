import java.util.ArrayList;
import java.util.Random;

/**
 * Represents a scenario for a decision to be made
 * COMP90041, Sem2, 2021: Final Project
 * @author Xing Yang Goh
 * student id: 1001969
 * student email: xingyangg@student.unimelb.edu.au
 */
public class Scenario {

    // Private constants to handle printing and random generation of scenarios
    private static final String SCENARIO_HEADER = """
            ======================================
            # Scenario
            ======================================""";
    private static final int MIN_ENTITIES = 1;
    private static final int MAX_ENTITIES = 5;
    private static final double YOU_CHANCE = 0.2;
    private static final double LEGAL_CHANCE = 0.5;
    private static final double ANIMAL_CHANCE = 0.2;
    public static final int CONSECUTIVE_SCENARIOS = 3;
    public static final int GENERATE_SCENARIOS = 100;

    // Only instantiate random class once
    private static final Random rand = new Random();

    // Static instance to determine if a scenario is imported or generated
    public static boolean randomScenarios = false;

    // Instances of a scenario class
    public boolean youDefined = false;
    public String legalCrossing;
    public ArrayList<Entity> passengerEntities = new ArrayList<>();
    public ArrayList<Entity> pedestrianEntities = new ArrayList<>();
    /**
     * Print a scenario to the console
     */
    public void printScenario() {
        System.out.println(SCENARIO_HEADER);
        System.out.println("Legal Crossing: " + legalCrossing);
        // Print passenger entities
        System.out.printf("Passengers (%d)\n", passengerEntities.size());
        for (Entity entity : passengerEntities) {
            entity.printDescription();
        }
        // Print pedestrian entities
        System.out.printf("Pedestrians (%d)\n", pedestrianEntities.size());
        for (Entity entity : pedestrianEntities) {
            entity.printDescription();
        }
    }
    /**
     * Generate a random scenario and add random passenger and pedestrian entities
     */
    protected void generateScenario() {
        // Set static instance to indicate randomly generated scenario
        randomScenarios = true;

        int numPassengers = rand.nextInt(MAX_ENTITIES-MIN_ENTITIES)+MIN_ENTITIES;
        int numPedestrians = rand.nextInt(MAX_ENTITIES-MIN_ENTITIES)+MIN_ENTITIES;
        if (Math.random() <= LEGAL_CHANCE) {
            legalCrossing = "yes";
        }
        else {
            legalCrossing = "no";
        }
        // At least have one human passenger, randomise the rest
        HumanEntity newHuman = new HumanEntity();
        newHuman.randomEntity();
        if (!youDefined && Math.random() <= YOU_CHANCE) {
            newHuman.setYou(true);
            youDefined = true;
        }
        passengerEntities.add(newHuman);
        // Add one less random entity to passenger since one human is added by default
        addRandomEntities(numPassengers-1, passengerEntities);

        // Add one human pedestrian to keep statistics fair, randomise the rest
        newHuman = new HumanEntity();
        newHuman.randomEntity();
        if (!youDefined && Math.random() <= YOU_CHANCE) {
            newHuman.setYou(true);
            youDefined = true;
        }
        pedestrianEntities.add(newHuman);
        addRandomEntities(numPedestrians-1, pedestrianEntities);
    }
    /**
     * Add random entities to the array list
     * @param entityList: either the passenger or pedestrian entity array list
     * @param numEntities: number of entities to be generated and added
     */
    protected void addRandomEntities(int numEntities, ArrayList<Entity> entityList) {
        for (int i = 0; i < numEntities; i++) {
            if (Math.random() <= ANIMAL_CHANCE) {
                AnimalEntity newAnimal = new AnimalEntity();
                newAnimal.randomEntity();
                entityList.add(newAnimal);
            }
            else {
                HumanEntity newHuman = new HumanEntity();
                newHuman.randomEntity();
                // Chance for the human entity to be you if you have not been defined in the scenario yet
                if (!youDefined && Math.random() <= YOU_CHANCE) {
                    newHuman.setYou(true);
                    youDefined = true;
                }
                entityList.add(newHuman);
            }
        }
    }
}