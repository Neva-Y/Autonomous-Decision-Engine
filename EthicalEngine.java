import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * The EthicalEngine that handles the running of the program and config file provided
 * COMP90041, Sem2, 2021: Final Project
 * @author Xing Yang Goh
 * student id: 1001969
 * student email: xingyangg@student.unimelb.edu.au
 */
public class EthicalEngine {

    // Instances in the EthicalEngine class including the decision enum, logger, scanner and scenarios array list
    private final Scanner input = new Scanner(System.in);
    private final ArrayList<Scenario> scenarios = new ArrayList<>();
    private final LogStats logs = new LogStats();

    /**
     * Decisions the algorithm can make to save either the passenger or pedestrian
     */
    public enum Decision {PASSENGERS, PEDESTRIANS}
    /**
     * Algorithmically decide if the passenger or pedestrian should be saved
     * @param scenario: a given scenario to make a decision on
     * @return a decision to save either the passenger or pedestrian
     */
    public static Decision decide(Scenario scenario) {
        DecisionEngine DecisionAlgorithm = new DecisionEngine();

        // Calculate scores based on the scenario
        DecisionAlgorithm.passengerScore = DecisionAlgorithm.calculateScore(scenario.passengerEntities);
        DecisionAlgorithm.pedestrianScore = DecisionAlgorithm.calculateScore(scenario.pedestrianEntities);
        DecisionAlgorithm.calculateLegality(scenario);

        // Return decision based on the scores, make random decision if scores are equal
        if (DecisionAlgorithm.pedestrianScore > DecisionAlgorithm.passengerScore) {
            return Decision.PEDESTRIANS;
        }
        else if (DecisionAlgorithm.pedestrianScore < DecisionAlgorithm.passengerScore){
            return Decision.PASSENGERS;
        }
        else {
            return Math.random() > 0.5 ? Decision.PEDESTRIANS : Decision.PASSENGERS;
        }
    }
    /**
     * Main method that initialises the engine
     * @param args: input command line arguments in an array of strings
     */
    public static void main(String[] args) {

        // Creates an instance of the Ethical Engine
        EthicalEngine ethicalEngine = new EthicalEngine();

        // Initialises and runs the engine with the provided flags
        ethicalEngine.initialiseEngine(args);
        ethicalEngine.runEthicalEngineLoop();
    }
    /**
     * Runs the engine loop, printing the welcome message and awaiting a command from the user
     */
    private void runEthicalEngineLoop() {
        welcomeMessage();
        awaitCommand();
    }
    /**
     * Engine initialisation using the flags and paths provided by the user
     * @param commands: input command line arguments in an array of strings
     */
    private void initialiseEngine(String[] commands) {

        // Loop through the provided arguments
        for (int i = 0; i < commands.length; i++) {
            // Display instructions and exit the program if there is a help flag
            if ("--help".equalsIgnoreCase(commands[i]) || "-h".equalsIgnoreCase(commands[i])) {
                displayInstructions();
            }
            // Set the log path if a valid file path is provided, else display instructions and exit
            else if ("--log".equalsIgnoreCase(commands[i]) || "-l".equalsIgnoreCase(commands[i])) {
                if (commands.length > i+1 && commands[i+1].matches("^[^-][\\w\\-\\\\/:.]+$")) {
                    logs.setLogPath(commands[++i]);
                }
                else {
                    displayInstructions();
                }
            }
            // Set the config path if a valid config path is provided, else display instructions and exit
            else if ("--config".equalsIgnoreCase(commands[i]) || "-c".equalsIgnoreCase(commands[i])) {
                if (commands.length > i+1 && commands[i+1].matches("^[^-][\\w\\-\\\\/:.]+\\.[\\w\\-]+$")) {
                    readConfig(commands[++i]);
                }
                else {
                    displayInstructions();
                }
            }
        }
    }
    /**
     * Display instructions to the user when help is flagged then exit the program
     */
    private void displayInstructions() {
        String instructions = """
                EthicalEngine - COMP90041 - Final Project

                Usage: java EthicalEngine [arguments]

                Arguments:
                    -c or --config        Optional: path to config file
                    -h or --help          Optional: print Help (this message) and exit
                    -l or --log           Optional: path to data log file""";

        System.out.println(instructions);
        System.exit(1);
    }
    /**
     * Display the welcome message and instructions on how to use the program
     */
    private void welcomeMessage() {
        try {
            // Print out the program introduction from the welcome file
            BufferedReader welcome = new BufferedReader(new FileReader("welcome.ascii"));
            String line = welcome.readLine();
            while (line != null) {
                System.out.println(line);
                line = welcome.readLine();
            }
            welcome.close();

            // Print out the available commands to run the program
            System.out.println();
            System.out.println(scenarios.size() +" Scenarios imported.\n" +
                    "Please enter one of the following commands to continue:\n" +
                    "- judge scenarios: [judge] or [j]\n" +
                    "- run simulations with the in-built decision algorithm: [run] or [r]\n" +
                    "- show audit from history: [audit] or [a]\n" +
                    "- quit the program: [quit] or [q]");

        // Handle the exceptions for the welcome file (unable to read/find)
        } catch (FileNotFoundException e) {
            System.err.println("ERROR: could not find welcome file.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("ERROR: unable to read welcome file.");
            System.exit(1);
        }
    }
    /**
     * Await further command from the user
     */
    private void awaitCommand() {
        System.out.print("> ");
        String command = initialiseCommand();
        executeCommand(command);
    }
    /**
     * Acquire command from user, initialise the command as quit
     */
    private String initialiseCommand() {
        String command ="quit";
        if (input.hasNextLine()) {
            command = input.nextLine();
        }
        return command;
    }
    /**
     * Execute the command provided by the user
     * @param command: command provided by the user
     */
    private void executeCommand(String command) {
        // Judge Scenarios
        if ("judge".equalsIgnoreCase(command) || "j".equalsIgnoreCase(command)) {
            judgeScenarios();
            // Run simulations using decision algorithm
        } else if ("run".equalsIgnoreCase(command) || "r".equalsIgnoreCase(command)) {
            runScenarios();
            // Show audit
        } else if ("audit".equalsIgnoreCase(command) || "a".equalsIgnoreCase(command)) {
            showAudit();
            // Quit program and close input scanner
        } else if ("quit".equalsIgnoreCase(command) || "q".equalsIgnoreCase(command)) {
            input.close();
            System.exit(1);
            // Unexpected input, await another command
        } else {
            awaitCommand();
        }
    }
    /**
     * Runs the decision algorithm to judge the scenarios
     */
    private void runScenarios() {
        // If no scenarios are imported from the config file, randomly generate the specified number of scenarios
        if (!(scenarios.size() > 0)) {
            System.out.println("How many scenarios should be run?");
            if (input.hasNextLine()) {
                String command = input.nextLine();
                while (!isPositiveNumeric(command)) {
                    System.out.println("Invalid input. How many scenarios should be run?");
                    if (input.hasNextLine()) {
                        command = input.nextLine();
                    }
                }
                // Flag the scenarios as randomly generated ones
                Scenario.randomScenarios = true;
                for (int i = 0; i < (int)Double.parseDouble(command); i++) {
                    Scenario randomScenario = new Scenario();
                    randomScenario.generateScenario();
                    // Add the generated scenario to the scenarios array list
                    scenarios.add(randomScenario);
                }
            }
        }
        // Acquire judgement for each scenario using the decision algorithm
        for (Scenario scenario: scenarios) {
            if (decide(scenario)==Decision.PASSENGERS)
                logs.saveStatistics(DecisionEngine.SAVE_PASSENGER, scenario);
            else {
                logs.saveStatistics(DecisionEngine.SAVE_PEDESTRIAN, scenario);
            }
            // Save algorithm decision statistics
        }
        finishJudgement(LogStats.IS_ALGORITHM);
    }
    /**
     * Shows the audit statistics between the user and algorithm
     */
    private void showAudit() {
        try {
            logs.readLogFile();
            if (input.hasNextLine()) {
                input.nextLine();
            }
            runEthicalEngineLoop();
        } catch (FileNotFoundException e) {
            // Handle exception where the log file is not found
            System.err.println("No history found. Press enter to return to main menu.");
            runEthicalEngineLoop();
        } catch (LogFileFormatException e) {
            // Handle exception where the log file format is not recognised
            String message = e.getMessage();
            System.err.print(message);
            if (input.hasNextLine()) {
                input.nextLine();
            }
            runEthicalEngineLoop();
        }
    }
    /**
     * Present scenarios for the user to make a decision
     */
    private void judgeScenarios() {
        // Initialise the consecutive scenario counter and acquire user consent for logging
        int consecutiveScenariosCount = 0;
        acquireConsent();

        // Generate random scenarios if no config file is provided
        if (!(scenarios.size() > 0)) {
            for (int i = 0; i < Scenario.GENERATE_SCENARIOS; i++) {
                Scenario randomScenario = new Scenario();
                randomScenario.generateScenario();
                scenarios.add(randomScenario);
            }
            Scenario.randomScenarios = true;
        }

        // Print each scenario to the console and obtain user decision
        for (Scenario scenario : scenarios) {
            scenario.printScenario();
            String judgement = playerJudgement();

            // Save user decision to generate statistics
            if (judgement!=null) {
                logs.saveStatistics(judgement, scenario);
            }
            // Print statistics after 3 scenarios have been shown and prompt user for continuation
            if (++consecutiveScenariosCount == Scenario.CONSECUTIVE_SCENARIOS) {
                logs.printStatistics();
                System.out.println("Would you like to continue? (yes/no)");
                if (input.hasNextLine()) {
                    String command = input.nextLine();

                    // If user does not provide "yes" or "no response show instructions await response
                    while(!command.equalsIgnoreCase("yes") &&
                            !command.equalsIgnoreCase("no")) {
                        System.out.println("Invalid response. Would you like to continue? (yes/no)");
                        if (input.hasNextLine()) {
                            command = input.nextLine();
                        }
                    }

                    // If user does not want to continue, write to the log file (if consented), clear statistics and
                    // clear randomly generated scenarios then return to the main menu
                    if (command.equalsIgnoreCase("no")) {
                        logs.loadLogFile();
                        logs.writeToLog(LogStats.IS_USER);
                        logs.clearStatistics();
                        if (Scenario.randomScenarios) {
                            scenarios.clear();
                            Scenario.randomScenarios = false;
                        }
                        runEthicalEngineLoop();
                    }
                    // If user wants to continue reset counter to 0 and print more scenarios
                    else {
                        consecutiveScenariosCount=0;
                    }
                }
                // No judgement provided, exit program
                else{
                    System.exit(1);
                }
            }
        }
        // End of scenarios
        finishJudgement(LogStats.IS_USER);
    }
    /**
     * Execute end of scenarios procedure
     * @param judgementType: represents a user or algorithm decision
     */
    private void finishJudgement(boolean judgementType) {

        // Print the statistics to the console
        logs.printStatistics();

        // Clear the randomly generated scenarios
        if (Scenario.randomScenarios) {
            scenarios.clear();
            Scenario.randomScenarios = false;
        }
        // Write to the log file for the given judgement type (user requires consent) and clear statistics
        logs.loadLogFile();
        logs.writeToLog(judgementType);
        logs.clearStatistics();

        // Prompt user to return to main menu
        System.out.println("That's all. Press Enter to return to main menu.");
        if (input.hasNextLine()) {
            input.nextLine();
        }
        runEthicalEngineLoop();
    }
    /**
     * Execute end of scenarios procedure
     * @return the user input decision (passenger or pedestrian)
     */
    private String playerJudgement() {
        System.out.println("Who should be saved? (passenger(s) [1] or pedestrian(s) [2])");
        if (input.hasNextLine()) {
            String judgement = input.nextLine();
            // Unrecognised input, print instructions and await another response
            while(Arrays.stream(DecisionEngine.USER_SAVE_PASSENGERS).noneMatch(judgement.toLowerCase()::contains) &&
                    Arrays.stream(DecisionEngine.USER_SAVE_PEDESTRIANS).noneMatch(judgement.toLowerCase()::contains)) {
                System.out.println("Invalid response. (passenger(s) [1] or pedestrian(s) [2])");
                if (input.hasNextLine()) {
                    judgement = input.nextLine();
                }
            }
            // Return the decision to save pedestrians or passengers
            if (Arrays.stream(DecisionEngine.USER_SAVE_PASSENGERS).anyMatch(judgement.toLowerCase()::contains)) {
                return DecisionEngine.SAVE_PASSENGER;
            }
            else if (Arrays.stream(DecisionEngine.USER_SAVE_PEDESTRIANS).anyMatch(judgement.toLowerCase()::contains)){
                return DecisionEngine.SAVE_PEDESTRIAN;
            }
        }
        // No input provided
        return null;
    }
    /**
     * Acquire user consent for logging statistics on user decisions
     */
    private void acquireConsent() {
        // Only inquire user if user has not previously consented.
        if (!LogStats.userConsent) {
            System.out.println("Do you consent to have your decisions saved to a file? (yes/no)");
            if (input.hasNextLine()) {
                String command = input.nextLine();
                while (!command.equalsIgnoreCase("yes") && !command.equalsIgnoreCase("no")) {
                    System.out.println("Invalid response. Do you consent to " +
                            "have your decisions saved to a file? (yes/no)");
                    if (input.hasNextLine()) {
                        command = input.nextLine();
                    }
                }
                // Allow logging if the user consents
                LogStats.userConsent = command.equalsIgnoreCase("yes");
            }
        }
    }
    /**
     * Read the config file provided
     * @param configPath: the provided config path from the command line argument
     */
    private void readConfig(String configPath) {
        try {
            // Read a new scenario from the config file
            Scanner configStream = new Scanner(new FileInputStream(configPath));
            String line;
            // Regex pattern to match a given scenario in the config file
            Pattern beginScenario = Pattern.compile("scenario:[a-z]+");
            Scenario newScenario = null;
            Matcher matchScenario;
            int lineNumber = 0;

            // Throw away the header line
            if (configStream.hasNextLine()) {
                configStream.nextLine();
                lineNumber++;
            }
            else {
                throw new IOException();
            }
            // Read in a new scenario
            while (configStream.hasNextLine()) {
                line = configStream.nextLine();
                matchScenario = beginScenario.matcher(line);

                // New scenario in the config file has been found
                if (matchScenario.find()) {
                    // Add the previously read scenario to the scenarios array list
                    if (newScenario != null) {
                        scenarios.add(newScenario);
                    }
                    // Create a new scenario and indicate if the crossing is legal
                    newScenario = new Scenario();
                    if (matchScenario.group(0).equalsIgnoreCase("scenario:green")) {
                        newScenario.legalCrossing = "yes";
                    } else if (matchScenario.group(0).equalsIgnoreCase("scenario:red")) {
                        newScenario.legalCrossing = "no";
                    } else {
                        throw new IOException();
                    }
                    lineNumber++;
                }
                // Add entities into the scenario from the line provided
                else {
                    if (newScenario!=null) {
                        createEntity(line, newScenario, ++lineNumber);
                    }
                    else {
                        throw new IOException();
                    }
                }
            }
            // No more lines in the config file, add final scenario to array list and close the config file
            scenarios.add(newScenario);
            configStream.close();

        // Handle exceptions for errors pertaining to the config file
        } catch(FileNotFoundException e) {
            System.err.println("ERROR: could not find config file.");
            System.exit(1);

        } catch (IOException e) {
            System.err.println("ERROR: unable to find scenario in config file.");
            System.exit(1);
        }
    }
    /**
     * Read the config file provided
     * @param line: a line containing entity information from the config file
     * @param nextScenario: the scenario to add entities to
     * @param lineNumber: line number in the config corresponding to the entity information
     */
    private void createEntity(String line, Scenario nextScenario, int lineNumber) {
        // Split the line into an array of strings using ',' since it is a csv file
        String[] entityFields = line.split(",");
        try {
            // Check if there are 10 fields for the entity information
            if (entityFields.length == Entity.NUM_FIELDS) {
                // Initialise the warning flags to false
                boolean throwCharacterException = false;
                boolean throwNumericalException = false;

                // Human entity detected, create a new human entity
                if (entityFields[Entity.CLASS].equalsIgnoreCase("human")) {
                    HumanEntity newEntity = new HumanEntity();
                    // Fill in the body type of the human entity
                    if (Arrays.stream(HumanEntity.BODY_TYPES).
                            anyMatch(entityFields[Entity.BODY_TYPE].toLowerCase()::contains)) {
                        newEntity.setBodyType(entityFields[Entity.BODY_TYPE].toLowerCase());
                    }
                    else {
                        // Unrecognised body type, set as unspecified and flag a warning
                        newEntity.setBodyType(HumanEntity.DEFAULT_BODY);
                        throwCharacterException = true;
                    }
                    // Indicate if the human entity is you, default is false
                    switch (entityFields[Entity.IS_YOU].toLowerCase()) {
                        case "true" -> newEntity.setYou(true);
                        case "false" -> newEntity.setYou(false);
                        default -> {
                            newEntity.setYou(false);
                            if (!entityFields[Entity.IS_YOU].isEmpty()) {
                                throwCharacterException = true;
                            }
                        }
                    }
                    // Fill in the age of the human entity
                    if (isPositiveNumeric(entityFields[Entity.AGE])) {
                        newEntity.setAge(Integer.parseInt(entityFields[Entity.AGE]));
                    }
                    // Non-numeric age provided, set age to default human age and flag warning
                    else {
                        newEntity.setAge(HumanEntity.DEFAULT_AGE);
                        throwNumericalException = true;
                    }
                    // Fill in general entity details
                    throwCharacterException = fillGeneralEntity(entityFields, newEntity, throwCharacterException);

                    // Fill in profession of human only if human entity is an adult
                    if (Arrays.stream(HumanEntity.PROFESSION_LIST).
                            anyMatch(entityFields[Entity.PROFESSION].toLowerCase()::contains)
                            && newEntity.getAgeCategory().equalsIgnoreCase("adult")) {
                        newEntity.setProfession(entityFields[Entity.PROFESSION].toLowerCase());
                    }
                    // Not an adult, set profession as none
                    else if (!newEntity.getAgeCategory().equalsIgnoreCase("adult")) {
                        newEntity.setProfession(HumanEntity.DEFAULT_PROFESSION);
                    }
                    // Unrecognised profession, set as none and flag warning if a non-empty profession is provided
                    else {
                        newEntity.setProfession(HumanEntity.DEFAULT_PROFESSION);
                        if (!entityFields[Entity.PROFESSION].isEmpty()) {
                            throwCharacterException = true;
                        }
                    }
                    // Add the human entity to the pedestrian or passenger array list
                    if (newEntity.getRole().equalsIgnoreCase("pedestrian"))
                        nextScenario.pedestrianEntities.add(newEntity);
                    else if (newEntity.getRole().equalsIgnoreCase("passenger"))
                        nextScenario.passengerEntities.add(newEntity);
                }
                // Animal entity detected, create a new animal entity
                else if (entityFields[Entity.CLASS].equalsIgnoreCase("animal")) {
                    AnimalEntity newEntity = new AnimalEntity();
                    // Set species to provided alphabetical name
                    if (entityFields[Entity.SPECIES].matches("^[a-zA-Z]+$")) {
                        newEntity.setSpecies(entityFields[Entity.SPECIES].toLowerCase());
                    }
                    else {
                        // Non-alphabetic species name provided, set species to default "creature" and flag warning
                        newEntity.setSpecies(AnimalEntity.DEFAULT_SPECIES);
                        throwCharacterException = true;
                    }
                    // Indicate if the animal entity is a pet, default is false
                    switch (entityFields[Entity.IS_PET].toLowerCase()) {
                        case "true" -> newEntity.setPet(true);
                        case "false" -> newEntity.setPet(false);
                        default -> {
                            newEntity.setPet(false);
                            if (!entityFields[Entity.IS_PET].isEmpty()) {
                                throwCharacterException = true;
                            }
                        }
                    }
                    // Fill in general entity details
                    throwCharacterException = fillGeneralEntity(entityFields, newEntity, throwCharacterException);

                    // Add the animal entity to the pedestrian or passenger array list
                    if (newEntity.getRole().equalsIgnoreCase("pedestrian"))
                        nextScenario.pedestrianEntities.add(newEntity);
                    else if (newEntity.getRole().equalsIgnoreCase("passenger"))
                        nextScenario.passengerEntities.add(newEntity);
                }
                else {
                    // Cannot determine if entity is an animal or a human, skip line
                    throw new InvalidCharacteristicException(lineNumber);
                }
                // Throw exceptions if warnings are flagged, throw here such that the entity is first added
                if (throwNumericalException) {
                    throw new NumberFormatException(lineNumber);
                }
                if (throwCharacterException) {
                    throw new InvalidCharacteristicException(lineNumber);
                }
            }
            else {
                // 10 lines of data not found, skip line
                throw new InvalidDataFormatException(lineNumber);
            }

        } catch (InvalidDataFormatException | InvalidCharacteristicException | NumberFormatException e) {
            // Handle exceptions thrown
            String message = e.getMessage();
            System.err.println(message);
        }
    }
    /**
     * Fill in general entity details for animals and humans
     * @param entityFields: attributes of the entity
     * @param entity: the entity to fill attributes to
     * @param throwCharacterException: flag if a warning should be thrown
     * @return flag if a warning should be thrown
     */
    private boolean fillGeneralEntity(String[] entityFields, Entity entity, boolean throwCharacterException) {

        // Fill entity gender, default is "unknown"
        switch (entityFields[Entity.GENDER]) {
            case "male" -> entity.setGender("male");
            case "female" -> entity.setGender("female");
            default -> {
                entity.setGender(Entity.DEFAULT_GENDER);
                throwCharacterException = true;
            }
        }
        // Fill entity pregnancy, default is false
        if (entity.getGender().equalsIgnoreCase("female") &&
                entityFields[Entity.PREGNANT].equalsIgnoreCase("true")) {
            entity.setPregnant(true);
        }
        else if (entityFields[Entity.PREGNANT].equalsIgnoreCase("false")) {
            entity.setPregnant(false);
        }
        else {
            entity.setPregnant(false);
            if (!entityFields[Entity.PREGNANT].isEmpty()) {
                throwCharacterException = true;
            }
        }
        // Fill entity role, default as a pedestrian
        switch (entityFields[Entity.ROLE]) {
            case "passenger" -> entity.setRole("passenger");
            case "pedestrian" -> entity.setRole("pedestrian");
            default -> {
                entity.setRole("pedestrian");
                throwCharacterException = true;
            }
        }
        return throwCharacterException;
    }
    /**
     * Method to check if a string is a positive numerical value
     * @param str: string to check
     * @return true or false
     */
    private boolean isPositiveNumeric(String str) {
        return str != null && str.matches("[+]?\\d*\\.?\\d+");
    }
}