/**
 * An exception for an unrecognised log file format
 * COMP90041, Sem2, 2021: Final Project
 * @author Xing Yang Goh
 * student id: 1001969
 * student email: xingyangg@student.unimelb.edu.au
 */
public class LogFileFormatException extends Exception {
    /** Warning for an unrecognised log file format
     */
    public LogFileFormatException() {
        super("WARNING: log file format is not recognised. Press enter to return to main menu.");
    }
}
