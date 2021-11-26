import java.util.Random;

/**
 * A human entity that is present in the scenario, can be randomly generated
 * COMP90041, Sem2, 2021: Final Project
 * @author Xing Yang Goh
 * student id: 1001969
 * student email: xingyangg@student.unimelb.edu.au
 */
public class HumanEntity extends Entity {

    // Age constants for a human entity
    public static final int DEFAULT_AGE = 30;
    public static final int BABY_AGE = 4;
    public static final int CHILD_AGE = 16;
    public static final int ADULT_AGE = 68;

    // Constants to generate a random human entity and possible attributes
    private static final int MIN_AGE = 1;
    private static final int MAX_AGE = 80;
    public static final String DEFAULT_PROFESSION = "none";
    public static final String DEFAULT_BODY = "unspecified";
    public static final String[] PROFESSION_LIST = {"none", "doctor", "ceo", "criminal", "unemployed", "homeless",
                                                        "engineer", "teacher", "patient", "athletes", "celebrity",
                                                        "firefighter"};
    public static final String[] BODY_TYPES = {"average", "athletic", "overweight"};
    public static final String[] GENDER_LIST = {"male", "female", "transgender"};
    private static final double PREGNANCY_CHANCE = 0.15;

    // Only instantiate random class once
    private static final Random rand = new Random();

    // Instances for a human entity
    private String profession = DEFAULT_PROFESSION;
    private String bodyType;
    private Boolean isYou = false;

    /**
     * Get the body type of the human entity
     * @return body type of the human entity
     */
    public String getBodyType() {
        return bodyType;
    }
    /**
     * Set the body type of the human entity
     * @param bodyType: body type of the human entity
     */
    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }
    /**
     * Get the profession of the human entity
     * @return profession of the human entity
     */
    public String getProfession() {
        return profession;
    }
    /**
     * Set the profession of the human entity
     * @param profession: profession of the human entity
     */
    public void setProfession(String profession) {
        this.profession = profession;
    }
    /**
     * Check if the human entity is you
     * @return if the human entity is you
     */
    public Boolean getYou() {
        return isYou;
    }
    /**
     * Set the human entity is you status
     * @param you: the human entity is you status
     */
    public void setYou(Boolean you) {
        isYou = you;
    }
    /**
     * Determine the age category of the given human entity based on their age
     * @return the age category of the human entity
     */
    public String getAgeCategory() {
        if (this.getAge() <= BABY_AGE) {
            return "baby";
        }
        else if (this.getAge() <= CHILD_AGE) {
            return "child";
        }
        else if (this.getAge() <= ADULT_AGE) {
            return "adult";
        }
        else {
            return "senior";
        }
    }
    /**
     * Print the description of the human entity
     */
    @Override
    public void printDescription() {
        System.out.print("- ");
        if (getYou()) {
            System.out.print("you ");
        }
        System.out.printf("%s %s ", getBodyType(), getAgeCategory());
        if (!profession.isEmpty() && !profession.equals(DEFAULT_PROFESSION)) {
            System.out.printf("%s ", getProfession().toLowerCase());
        }
        System.out.printf("%s", getGender());
        if (getGender().equals("female") && getPregnant()) {
            System.out.print(" pregnant");
        }
        System.out.println();
    }
    /**
     * Generate a random human entity
     */
    @Override
    protected void randomEntity() {
        setBodyType(BODY_TYPES[rand.nextInt(BODY_TYPES.length)]);
        setAge(rand.nextInt(MAX_AGE-MIN_AGE)+MIN_AGE);

        // Children and babies exempt from profession
        if (getAgeCategory().equals("adult")) {
            setProfession(PROFESSION_LIST[rand.nextInt(PROFESSION_LIST.length)]);
        }
        else {
            setProfession("none");
        }
        setGender(GENDER_LIST[rand.nextInt(GENDER_LIST.length)]);

        // Females have a chance to be pregnant
        if (getGender().equals("female")) {
            setPregnant(Math.random() <= PREGNANCY_CHANCE);
        }
    }
}
