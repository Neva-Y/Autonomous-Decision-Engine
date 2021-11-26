/**
 * An exception for an invalid characteristic in the config file
 * COMP90041, Sem2, 2021: Final Project
 * @author Xing Yang Goh
 * student id: 1001969
 * student email: xingyangg@student.unimelb.edu.au
 */

public class InvalidCharacteristicException extends Exception {
    /** Warning for an invalid characteristic exception
     * @param lineNumber: line in the config file where the error is found
     */
    public InvalidCharacteristicException (int lineNumber) {
        super("WARNING: invalid characteristic in config file in line " + lineNumber);
    }
}