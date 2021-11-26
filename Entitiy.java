/**
 * Abstract entity class for characters involved
 * COMP90041, Sem2, 2021: Final Project
 * @author Xing Yang Goh
 * student id: 1001969
 * student email: xingyangg@student.unimelb.edu.au
 */
abstract class Entity {

    // Constants to access config file fields
    public static final int CLASS = 0;
    public static final int GENDER = 1;
    public static final int AGE = 2;
    public static final int BODY_TYPE = 3;
    public static final int PROFESSION = 4;
    public static final int PREGNANT = 5;
    public static final int IS_YOU = 6;
    public static final int SPECIES = 7;
    public static final int IS_PET = 8;
    public static final int ROLE = 9;
    public static final int NUM_FIELDS = 10;

    public static final String DEFAULT_GENDER = "unknown";

    // Instances for all entities
    private String gender;
    private int age;
    private boolean isPregnant;
    private String role;

    /**
     * Get the gender of the entity
     * @return gender of the entity
     */
    public String getGender() {
        return gender;
    }
    /**
     *Get the age of the entity
     * @return age of the entity
     */
    public int getAge() {
        return age;
    }
    /**
     * Get the pregnancy status of the entity
     * @return pregnancy status of the entity
     */
    public boolean getPregnant() {
        return isPregnant;
    }
    /**
     * Get the role of the entity
     * @return passenger or pedestrian
     */
    public String getRole() {
        return role;
    }
    /**
     * Set the gender of the entity
     * @param gender: gender of the entity
     */
    public void setGender(String gender) {
        this.gender = gender;
    }
    /**
     * Set the age of the entity
     * @param age: age of the entity
     */
    public void setAge(int age) {
        this.age = age;
    }
    /**
     * Set the pregnancy status of the entity
     * @param pregnant: pregnancy status of the entity (true or false)
     */
    public void setPregnant(boolean pregnant) {
        isPregnant = pregnant;
    }
    /**
     * Set the role of the entity
     * @param role: pedestrian or passenger
     */
    public void setRole(String role) {
        this.role = role;
    }

    // Protected abstract methods to print description and random generation
    public abstract void printDescription();
    protected abstract void randomEntity();
}