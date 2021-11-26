import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.lang.Math;

/**
 * Handles the log file including writing statistics and reading for audits
 * COMP90041, Sem2, 2021: Final Project
 * @author Xing Yang Goh
 * student id: 1001969
 * student email: xingyangg@student.unimelb.edu.au
 */
public class LogStats {

    // Public static variable to determine if user consent if provided
    public static boolean userConsent = false;

    // Constants to handle writing and reading of the log file
    public static final boolean IS_USER = true;
    public static final boolean IS_ALGORITHM = false;
    public static final String LOG_FILE = "ethicalengine.log";
    private static final String USER_STAT = "(User Statistics)";
    private static final String ALGORITHM_STAT = "(Algorithm Statistics)";
    private static final int ATTRIBUTE_FIELD = 0;
    private static final int STATISTIC_FIELD = 1;
    private static final int LOG_FIELDS = 2;
    private static final int HEADER_LINES = 3;
    private static final String STATS_HEADER = """
                ======================================
                # Statistic
                ======================================""";
    private static final String ALGORITHM_HEADER = """
                ======================================
                # Algorithm Audit
                ======================================""";
    private static final String USER_HEADER = """
                ======================================
                # User Audit
                ======================================""";

    // Instances to determine the log file and path
    private String logPath = LOG_FILE;
    private File logFile = null;

    // Hashmap instance that stores the counts to generate the statistics
    private final HashMap<String, Integer> completeLog = new HashMap<>();
    private final HashMap<String, Integer> judgementLog = new HashMap<>();
    private final HashMap<String, Double> statisticsLog = new HashMap<>();

    // Hashmap instances that stores the sum to generate the audit
    private HashMap<String, Double> userLog = new HashMap<>();
    private HashMap<String, Double> algorithmLog = new HashMap<>();

    // Instances for running counts of variables
    private static int runNum = 0;
    public static int algorithmLogCount = 0;
    public static int userLogCount = 0;
    public static double algorithmAge = 0;
    public static double userAge = 0;

    /**
     * Setter method for the log path
     * @param logPath: the string to set the log path to
     */
    protected void setLogPath(String logPath) {
        this.logPath = logPath;
    }
    /**
     * Load the log file
     */
    protected void loadLogFile() {
        try {
            String fileName;
            File directory;
            File absolutePath = new File(logPath);

            // Acquire the provided path of the log path (ignore the log file name)
            int pathSeparator = logPath.lastIndexOf(File.separator);
            if (pathSeparator > -1) {
                directory = new File(logPath.substring(0, pathSeparator));
                fileName = logPath.substring(pathSeparator + 1);
            } else {
                // No user specified path, use directory of program execution
                directory = new File(System.getProperty("user.dir"));
                fileName = logPath;
            }
            // Check directory exists, throw exception if provided directory is invalid
            if (!directory.isDirectory()) {
                throw new DirectoryNotFoundException();
            }
            // Path and file given and valid
            if (absolutePath.exists()) {
                logFile = absolutePath;
            }
            // Path not specified but file given
            else if (!fileName.isEmpty()) {
                logFile = new File(directory + File.separator + fileName);
            }
            // File not provided, use default log file
            else {
                logFile = new File(directory + File.separator + LOG_FILE);
            }
        } catch (DirectoryNotFoundException e) {
            String message = e.getMessage();
            System.err.println(message);
            System.exit(1);
        }
    }
    /**
     * Write the statistics to the log file
     * @param decisionMaker: decision made by the user or algorithm
     */
    protected void writeToLog(boolean decisionMaker) {
        try {
            // User decision requires consent, algorithm decision automatically writes to log file
            if ((logPath != null && userConsent && decisionMaker == IS_USER) ||
                    (logPath != null && decisionMaker == IS_ALGORITHM)) {
                // Open the log file in append mode
                PrintWriter outputStream = new PrintWriter(new FileOutputStream(logFile, true));
                // Print header to the log to indicate if the statistics are from algorithm or user decisions
                if (decisionMaker == IS_USER) {
                    outputStream.println(USER_STAT);
                } else {
                    outputStream.println(ALGORITHM_STAT);
                }
                // Print statistics to the log file
                outputStream.println(STATS_HEADER);
                outputStream.printf("- %% SAVED AFTER %d RUNS\n", runNum);
                Map<String, Double> sortedStatistics = generateSortedStats();
                for (String key : sortedStatistics.keySet()) {
                    outputStream.printf("%s: %.2f\n", key, roundUp(sortedStatistics.get(key)));
                }
                outputStream.println("--");
                outputStream.printf("average age: %.2f\n",
                        roundUp((double) judgementLog.get("sumAge") / judgementLog.get("survivors")));
                outputStream.close();
            }
        } catch (FileNotFoundException e) {
            String message = e.getMessage();
            System.err.println(message);
            System.exit(1);
        }
    }
    /**
     * Save statistics of a scenario to the hashmap
     * @param judgement: decision to save either passenger or pedestrian
     * @param scenario: the scenario where the decision is made
     */
    protected void saveStatistics(String judgement, Scenario scenario) {
        // Increment the number of runs for the statistic
        runNum++;
        // Update hash map statistics for passengers
        for (Entity entities : scenario.passengerEntities) {
            // Update legality of the crossing and passenger saved
            hashMapCountUpdater(DecisionEngine.SAVE_PASSENGER, judgement, DecisionEngine.SAVE_PASSENGER);
            updateHashMapLegality(scenario.legalCrossing, judgement, DecisionEngine.SAVE_PASSENGER);
            // Update based on human or animal entity
            if (entities instanceof HumanEntity) {
                updateHashMapHuman(judgement, (HumanEntity) entities, DecisionEngine.SAVE_PASSENGER);
            } else if (entities instanceof AnimalEntity) {
                updateHashMapAnimal(judgement, (AnimalEntity) entities, DecisionEngine.SAVE_PASSENGER);
            }
        }
        // Update hash map statistics for pedestrians
        for (Entity entities : scenario.pedestrianEntities) {
            // Update legality of the crossing and pedestrian saved
            hashMapCountUpdater(DecisionEngine.SAVE_PEDESTRIAN, judgement, DecisionEngine.SAVE_PEDESTRIAN);
            updateHashMapLegality(scenario.legalCrossing, judgement, DecisionEngine.SAVE_PEDESTRIAN);
            // Update based on human or animal entity
            if (entities instanceof HumanEntity) {
                updateHashMapHuman(judgement, (HumanEntity) entities, DecisionEngine.SAVE_PEDESTRIAN);
            } else if (entities instanceof AnimalEntity) {
                updateHashMapAnimal(judgement, (AnimalEntity) entities, DecisionEngine.SAVE_PEDESTRIAN);
            }
        }
    }
    /**
     * Update hash map with lights of the crossing
     * @param legalCrossing: if the crossing is legal or not
     * @param judgement: decision to save either the pedestrian or passenger
     * @param entityType: if the entity is a passenger or pedestrian
     */
    private void updateHashMapLegality(String legalCrossing, String judgement, String entityType) {
        if (!legalCrossing.isEmpty()) {
            if (legalCrossing.equalsIgnoreCase("yes")) {
                hashMapCountUpdater("green", judgement, entityType);
            }
            else if (legalCrossing.equalsIgnoreCase("no")) {
                hashMapCountUpdater("red", judgement, entityType);
            }
        }
    }
    /**
     * Update hashmap for a human entity
     * @param judgement: decision to save either passenger or pedestrian
     * @param humanEntity: the human entity
     * @param entityType: if the human entity is a passenger or pedestrian
     */
    private void updateHashMapHuman(String judgement, HumanEntity humanEntity, String entityType) {
        hashMapCountUpdater("human", judgement, entityType);
        hashMapCountUpdater(humanEntity.getAgeCategory(), judgement, entityType);
        if (!humanEntity.getBodyType().equalsIgnoreCase(Entity.DEFAULT_GENDER)) {
            hashMapCountUpdater(humanEntity.getGender(), judgement, entityType);
        }
        if (!humanEntity.getBodyType().equalsIgnoreCase(HumanEntity.DEFAULT_BODY)) {
            hashMapCountUpdater(humanEntity.getBodyType(), judgement, entityType);
        }
        if (!humanEntity.getProfession().equalsIgnoreCase(HumanEntity.DEFAULT_PROFESSION)) {
            hashMapCountUpdater(humanEntity.getProfession(), judgement, entityType);
        }
        if (humanEntity.getPregnant()) {
            hashMapCountUpdater("pregnant", judgement, entityType);
        }
        if (humanEntity.getYou()) {
            hashMapCountUpdater("you", judgement, entityType);
        }
        // Update survivor count and sum their age
        if (judgement.equalsIgnoreCase(entityType)) {
            if (judgementLog.containsKey("survivors")) {
                judgementLog.put("survivors", judgementLog.get("survivors") + 1);
            } else {
                judgementLog.put("survivors", 1);
            }
            if (judgementLog.containsKey("sumAge")) {
                judgementLog.put("sumAge", judgementLog.get("sumAge") + humanEntity.getAge());
            } else {
                judgementLog.put("sumAge", humanEntity.getAge());
            }
        }
    }
    /**
     * Update hashmap for an animal entity
     * @param judgement: decision to save either passenger or pedestrian
     * @param animalEntity: the animal entity
     * @param entityType: if the animal entity is a passenger or pedestrian
     */
    private void updateHashMapAnimal(String judgement, AnimalEntity animalEntity, String entityType) {
        hashMapCountUpdater("animal", judgement, entityType);
        if (!animalEntity.getSpecies().equals(AnimalEntity.DEFAULT_SPECIES)) {
            hashMapCountUpdater(animalEntity.getSpecies(), judgement, entityType);
        }
        if (animalEntity.isPet()) {
            hashMapCountUpdater("pet", judgement, entityType);
        }
    }
    /**
     * Update the count hashmaps for a given string
     * @param judgement: decision to save either passenger or pedestrian
     * @param factor: the string to add to the hashmaps
     * @param entityType: if the human entity is a passenger or pedestrian
     */
    private void hashMapCountUpdater(String factor, String judgement, String entityType) {
        if (!factor.isEmpty()) {
            // Add 1 to the count if initialised, else set to 1
            if (completeLog.containsKey(factor)) {
                completeLog.put(factor, completeLog.get(factor) + 1);
            } else {
                completeLog.put(factor, 1);
            }
            // Also add to the judgment log if the judgment is the entity type
            if (judgement.equalsIgnoreCase(entityType)) {
                if (judgementLog.containsKey(factor)) {
                    judgementLog.put(factor, judgementLog.get(factor) + 1);
                } else {
                    judgementLog.put(factor, 1);
                }
            }
        }
    }
    /**
     * Generate the statistics from the decisions made across scenarios and return the sorted hashmap
     * @return the sorted hashmap with finalised statistics
     */
    private HashMap<String, Double> generateSortedStats() {
        // Calculate statistic for each key in the complete log
        for (String key : completeLog.keySet()) {
            double val = judgementLog.getOrDefault(key, 0);
            statisticsLog.put(key, val / completeLog.get(key));
        }
        return sortByValueDescending(statisticsLog);
    }
    /**
     * Print the statistics to the console
     */
    protected void printStatistics() {
        System.out.println(STATS_HEADER);
        System.out.printf("- %% SAVED AFTER %d RUNS\n", runNum);
        Map<String, Double> sortedStatistics = generateSortedStats();
        for (String key : sortedStatistics.keySet()) {
            System.out.printf("%s: %.2f\n", key, roundUp(sortedStatistics.get(key)));
        }
        System.out.println("--");
        System.out.printf("average age: %.2f\n",
                roundUp((double) judgementLog.get("sumAge")/judgementLog.get("survivors")));
    }
    /**
     * Clear the statistics hashmaps and reset the scenario counter to 0
     */
    protected void clearStatistics() {
        runNum = 0;
        completeLog.clear();
        judgementLog.clear();
        statisticsLog.clear();
    }
    /**
     * Sort the hashmap in descending order based on value (numerical) and ascending order based on key (alphabetical)
     * @param statisticsLog: the hashmap with finalised statistics that needs to be sorted
     * @return the sorted hashmap
     */
    private HashMap<String, Double> sortByValueDescending(HashMap<String, Double> statisticsLog) {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Double>> list = new LinkedList<>(statisticsLog.entrySet());

        // Sort the list descending by value then ascending alphabetically
        list.sort(Map.Entry.comparingByKey());
        list.sort(Map.Entry.<String, Double>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()));

        // Make a new hashmap from the sorted list
        HashMap<String, Double> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : list) {
            temp.put(entry.getKey(), entry.getValue());
        }
        return temp;
    }
    /**
     * Read the log file and print the audit to the log file
     * @throws FileNotFoundException unable to find the log file
     * @throws LogFileFormatException log file format unrecognised
     */
    public void readLogFile() throws FileNotFoundException, LogFileFormatException {
        File f = new File(logPath);
        if (!f.exists() || f.isDirectory() || f.length()==0) {
            throw new FileNotFoundException();
        }
        Scanner inputStream = new Scanner(new FileInputStream(f));
        aggregateAudit(inputStream);
        printAudit();
    }
    /**
     * Read the log file and update the
     * @param inputStream: scanner reading the log file
     * @throws LogFileFormatException unable to find the log file
     */
    private void aggregateAudit(Scanner inputStream) throws LogFileFormatException {
        String line;
        String logType;
        String [] dataLine;
        int numRuns = 0;
        while (inputStream.hasNextLine()) {
            logType = inputStream.nextLine();
            // Check if the statistic is an algorithm or user statistic
            if (!logType.equals(ALGORITHM_STAT) && !logType.equals(USER_STAT)) {
                throw new LogFileFormatException();
            }
            throwHeader(inputStream);

            // Throw away words until number of runs integer is reached
            if (inputStream.hasNextLine()) {
                inputStream.next();
                inputStream.next();
                inputStream.next();
                inputStream.next();
                if (logType.equals(ALGORITHM_STAT)) {
                    numRuns = inputStream.nextInt();
                    algorithmLogCount += numRuns;
                }
                else if (logType.equals(USER_STAT)){
                    numRuns = inputStream.nextInt();
                    userLogCount += numRuns;
                }
                inputStream.nextLine();

                // Read log file line by line
                while (inputStream.hasNextLine()) {
                    line = inputStream.nextLine();

                    // End of statistic, sum average age to running count and break
                    if (line.equals("--")) {
                        inputStream.next();
                        inputStream.next();
                        if (logType.equals(USER_STAT)) {
                            userAge += inputStream.nextDouble()*numRuns;
                            inputStream.nextLine();
                        }
                        else if (logType.equals(ALGORITHM_STAT)){
                            algorithmAge += inputStream.nextDouble()*numRuns;
                            inputStream.nextLine();
                        }
                        else {
                            throw new LogFileFormatException();
                        }
                        break;
                    }

                    // Remove whitespaces from the statistic and split by ":", then update the hash map for
                    // each attribute. Throw exception if value is non-numeric or if line is an unrecognised format
                    line = line.replaceAll("\\s+", "");
                    dataLine = line.split(":");
                    if (!isPositiveNumeric(dataLine[STATISTIC_FIELD]) || dataLine.length != LOG_FIELDS) {
                        throw new LogFileFormatException();
                    }
                    if (logType.equals(ALGORITHM_STAT)) {
                        hashMapStatsUpdater(dataLine[ATTRIBUTE_FIELD],
                                Double.parseDouble(dataLine[STATISTIC_FIELD])*numRuns, IS_ALGORITHM);
                    }
                    else if (logType.equals(USER_STAT)) {
                        hashMapStatsUpdater(dataLine[ATTRIBUTE_FIELD],
                                Double.parseDouble(dataLine[STATISTIC_FIELD])*numRuns, IS_USER);
                    }
                }
            }
            // No line found, close input stream and throw exception
            else {
                inputStream.close();
                throw new LogFileFormatException();
            }
        }
        // End of log file, close input stream
        inputStream.close();
    }
    /**
     * Convert the running sums of the audit logs to a statistic, rounding up
     */
    private void calculateAuditStats() {
        algorithmLog.replaceAll((k, v) -> roundUp(algorithmLog.get(k) / algorithmLogCount));
        userLog.replaceAll((k, v) -> roundUp(userLog.get(k) / userLogCount));
    }
    /**
     * Remove header line from the log file
     * @param inputStream: log file input stream
     * @throws LogFileFormatException log file format unrecognised
     */
    private void throwHeader(Scanner inputStream) throws LogFileFormatException{
        for (int i=0; i < HEADER_LINES; i++) {
            if (inputStream.hasNextLine()) {
                inputStream.nextLine();
            }
            else {
                throw new LogFileFormatException();
            }
        }
    }
    /**
     * Update the audit statistic hashmaps for a given string
     * @param factor: the string to add to the hashmaps
     * @param decimal: value to add to the decimal
     * @param statType: user or algorithm statistic
     * @throws LogFileFormatException log file format unrecognised
     */
    private void hashMapStatsUpdater(String factor, double decimal, boolean statType) throws LogFileFormatException {
        if (!factor.isEmpty()) {
            // Add decimal to the user log if initialised, else set to the decimal
            if (statType == IS_USER) {
                if (userLog.containsKey(factor)) {
                    userLog.put(factor, userLog.get(factor) + decimal);
                } else {
                    userLog.put(factor, decimal);
                }
            }
            // Add decimal to the algorithm log if initialised, else set to the decimal
            else {
                if (algorithmLog.containsKey(factor)) {
                    algorithmLog.put(factor, algorithmLog.get(factor) + decimal);
                } else {
                    algorithmLog.put(factor, decimal);
                }
            }
        }
        else {
            throw new LogFileFormatException();
        }
    }
    /**
     * Encompassing method to print the audit to the console, then clear the audit
     * @throws LogFileFormatException log file format unrecognised
     */
    private void printAudit() throws LogFileFormatException{
        calculateAuditStats();
        ArrayList<String> commonKeys = getCommonKeys();
        if (commonKeys.size() > 0) {
            algorithmLog = sortByValueDescending(algorithmLog);
            userLog = sortByValueDescending(userLog);
            System.out.println(ALGORITHM_HEADER);
            printAuditStats(commonKeys, algorithmLog, algorithmLogCount, algorithmAge);
            System.out.println();
            System.out.println(USER_HEADER);
            printAuditStats(commonKeys, userLog, userLogCount, userAge);
        }
        else {
            throw new LogFileFormatException();
        }
        clearAudit();
        System.out.println("That's all. Press Enter to return to main menu.");
    }
    /**
     * Print the audit statistics for common attributes in the user and algorithm hashmaps
     */
    private void printAuditStats(ArrayList<String> commonKeys, HashMap<String, Double> log,
                                 int logCount, double ageSum) {
        System.out.printf("- %% SAVED AFTER %d RUNS\n", logCount);
        // Only print values for common keys in the user and algorithm hashmaps
        for (String key : log.keySet()) {
            if (commonKeys.contains(key)) {
                System.out.printf("%s: %.2f\n", key, roundUp(log.get(key)));
            }
        }
        // Print the average age statistic
        System.out.println("--");
        System.out.printf("average age: %.2f\n", roundUp(ageSum / logCount));
    }
    /**
     * Get common keys from the algorithm and user audit hashmaps
     * @return an array list of the common keys
     */
    private ArrayList<String> getCommonKeys() {
        ArrayList<String> commonKeys = new ArrayList<>();
        for (String key : algorithmLog.keySet()) {
            if (userLog.containsKey(key)) {
                commonKeys.add(key);
            }
        }
        return commonKeys;
    }
    /**
     * Clear the hashmaps and running count instances for the audit
     */
    private void clearAudit() {
        userLog.clear();
        algorithmLog.clear();
        algorithmAge = 0;
        userAge = 0;
        userLogCount = 0;
        algorithmLogCount = 0;
    }
    /**
     * Method to check if a string is a positive numerical value
     * @param str: string to check
     * @return true or false
     */
    public boolean isPositiveNumeric(String str) {
        return str != null && str.matches("[+]?\\d*\\.?\\d+");
    }
    /**
     * Method to round up a double to 2 decimal places
     * @param val: a double value
     * @return value rounded to 2 decimal places
     */
    public double roundUp(double val) {
        return Math.ceil(val*100)/100;
    }
}