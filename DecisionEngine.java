import java.util.ArrayList;

/**
 * The algorithm that automatically scores a scenario based on various factors
 * COMP90041, Sem2, 2021: Final Project
 * @author Xing Yang Goh
 * student id: 1001969
 * student email: xingyangg@student.unimelb.edu.au
 */
public class DecisionEngine {

    // Decision constants
    public static final String[] USER_SAVE_PEDESTRIANS = {"pedestrians", "pedestrian", "2"};
    public static final String[] USER_SAVE_PASSENGERS = {"passengers", "passenger", "1"};
    public static final String SAVE_PEDESTRIAN = "pedestrians";
    public static final String SAVE_PASSENGER = "passengers";

    // Profession constants for algorithm calculation
    private static final int DOCTOR_SCORE = 80;
    private static final int CEO_SCORE = 80;
    private static final int FIREFIGHTER_SCORE = 60;
    private static final int ENGINEER_SCORE = 50;
    private static final int TEACHER_SCORE = 50;
    private static final int CELEBRITY_SCORE = 30;
    private static final int ATHLETE_SCORE = 30;
    private static final int NONE_SCORE = 10;
    private static final int UNEMPLOYED_SCORE = 5;
    private static final int PATIENT_SCORE = 5;
    private static final int HOMELESS_SCORE = 0;
    private static final int CRIMINAL_SCORE = -20;

    // Animal constants for algorithm calculation
    private static final int ENDANGERED_SCORE = 30;
    private static final int PET_SCORE = 15;
    private static final int ANIMAL_SCORE = 5;
    private static final int INVASIVE_SCORE = -10;

    // Age groups constants, babies and children have higher scores as they do not have professions yet.
    private static final int BABY_SCORE = 80;
    private static final int CHILD_SCORE = 100;
    private static final int ADULT_SCORE = 50;
    private static final int SENIOR_SCORE = 20;

    // Body type constants for algorithm calculation
    private static final int OVERWEIGHT_SCORE = 5;
    private static final int AVERAGE_SCORE = 20;
    private static final int ATHLETIC_SCORE = 20;

    // Pregnancy constant for algorithm calculation
    private static final int PREGNANCY_SCORE = 70;

    // Legality constants for algorithm calculation
    private static final int LEGAL_CROSSING = 100;

    // Running count instances for the score
    protected int pedestrianScore = 0;
    protected int passengerScore = 0;
    /**
     * Calculates the scores based on the provided entities
     * @param entityList: the passenger or pedestrian entities to calculate a score for
     * @return the aggregated score for the entity
     */
    protected int calculateScore(ArrayList<Entity> entityList) {
        int score = 0;
        for (Entity entities : entityList) {
            // Calculate score for a human entity
            if (entities instanceof HumanEntity) {
                score += calculateProfession(((HumanEntity) entities).getProfession());
                score += calculateAgeGroup(((HumanEntity) entities).getAgeCategory());
                score += calculateBodyType(((HumanEntity) entities).getBodyType());
                score += calculatePregnancyScore((HumanEntity) entities);
            }
            // Calculate score for an animal entity
            else if (entities instanceof AnimalEntity) {
                score += calculateAnimal(((AnimalEntity) entities));
            }
        }
        return score;
    }

    /**
     * Calculate scores based on the scenario crossing lights
     * @param scenario: the given scenario
     */
    protected void calculateLegality(Scenario scenario) {
        if (scenario.legalCrossing.equals("yes")) {
            pedestrianScore += LEGAL_CROSSING;
        }
        else {
            passengerScore += LEGAL_CROSSING;
        }
    }
    /**
     * Calculates the scores for an animal entity
     * @param animalEntity: an animal entity
     * @return the score for the animal entity
     */
    private int calculateAnimal(AnimalEntity animalEntity) {
        int score = 0;
        if (animalEntity.isPet()) {
            score += PET_SCORE;
        }
        switch (animalEntity.getSpecies()) {
            case "endangered" -> score += ENDANGERED_SCORE;
            case "invasive" -> score += INVASIVE_SCORE;
            default -> score += ANIMAL_SCORE;
        }
        return score;
    }
    /**
     * Calculates the scores based on the profession of the human entity
     * @param profession: the profession of the human entity
     * @return the updated score for the given profession
     */
    private int calculateProfession(String profession) {
        int score;
        switch (profession) {
            case "ceo" -> score = CEO_SCORE;
            case "doctor" -> score = DOCTOR_SCORE;
            case "engineer" -> score = ENGINEER_SCORE;
            case "teacher" -> score = TEACHER_SCORE;
            case "celebrity" -> score = CELEBRITY_SCORE;
            case "athlete" -> score = ATHLETE_SCORE;
            case "patient" -> score = PATIENT_SCORE;
            case "homeless" -> score = HOMELESS_SCORE;
            case "criminal" -> score = CRIMINAL_SCORE;
            case "unemployed" -> score = UNEMPLOYED_SCORE;
            case "firefighter" -> score = FIREFIGHTER_SCORE;
            default -> score = NONE_SCORE;
        }
        return score;
    }
    /**
     * Calculates the scores based on the age group of the human entity
     * @param ageGroup: the age group of the human entity
     * @return the updated score for the given age group
     */
    private int calculateAgeGroup(String ageGroup) {
        int score;
        switch (ageGroup) {
            case "baby" -> score = BABY_SCORE;
            case "child" -> score = CHILD_SCORE;
            case "adult" -> score = ADULT_SCORE;
            default -> score = SENIOR_SCORE;
        }
        return score;
    }
    /**
     * Calculates the scores based on the body type of the human entity
     * @param bodyType: the body type of the human entity
     * @return the updated score for the given body type
     */
    private int calculateBodyType(String bodyType) {
        int score;
        switch (bodyType) {
            case "overweight" -> score = OVERWEIGHT_SCORE;
            case "average" -> score = AVERAGE_SCORE;
            case "athletic" -> score = ATHLETIC_SCORE;
            default -> score = AVERAGE_SCORE;
        }
        return score;
    }
    /**
     * Calculates the scores based on the pregnancy status of the human female entity
     * @param entity: the human entity
     * @return the updated score for the given pregnancy status
     */
    private int calculatePregnancyScore(HumanEntity entity) {
        int score = 0;
        // Only female human entities can be pregnant
        if (entity.getGender().equals("female") && entity.getPregnant()) {
            score += PREGNANCY_SCORE;
        }
        return score;
    }
}