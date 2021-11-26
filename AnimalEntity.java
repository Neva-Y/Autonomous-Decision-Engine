import java.util.Random;

/**
 * An animal entity that is present in the scenario, can be randomly generated
 * COMP90041, Sem2, 2021: Final Project
 * @author Xing Yang Goh
 * student id: 1001969
 * student email: xingyangg@student.unimelb.edu.au
 */
public class AnimalEntity extends Entity{

    // Constants to generate a random animal entity and possible attributes
    public static final String DEFAULT_SPECIES = "creature";
    public static final String[] SPECIES_LIST = {"cat", "dog", "bird", "deer", "horse", "moose", "endangered",
                                                    "kangaroo", "koala", "invasive"};
    private static final double PET_CHANCE = 0.3;

    // Only instantiate random class once
    private static final Random rand = new Random();

    // Instances for an animal entity
    private String species;
    private boolean isPet;

    /**
     * Get the species of the animal entity
     * @return species of the animal entity
     */
    public String getSpecies() {
        return species;
    }
    /**
     * Set the species of the animal entity
     * @param species: species of the animal entity
     */
    public void setSpecies(String species) {
        this.species = species;
    }
    /**
     * Check if the animal entity is a pet
     * @return the pet status of the animal entity
     */
    public boolean isPet() {
        return isPet;
    }
    /**
     * Set the animal entity pet status
     * @param pet: pet status of the animal entity
     */
    public void setPet(boolean pet) {
        isPet = pet;
    }
    /**
     * Print the description of the animal entity
     */
    @Override
     public void printDescription() {
        if (isPet()) {
            System.out.printf("- %s is pet\n", getSpecies());
        }
        else {
            System.out.println("- " + getSpecies());
        }
    }
    /**
     * Generate a random animal entity
     */
    @Override
     protected void randomEntity() {
        setSpecies(SPECIES_LIST[rand.nextInt(SPECIES_LIST.length)]);
        isPet = Math.random() <= PET_CHANCE;
    }

}
