/**
 * An exception for an invalid number format in the config file
 * COMP90041, Sem2, 2021: Final Project
 * @author Xing Yang Goh
 * student id: 1001969
 * student email: xingyangg@student.unimelb.edu.au
 */
public class NumberFormatException extends Exception{
    /** Warning for an invalid numerical format exception
     * @param lineNumber: line in the config file where the error is found
     */
    public NumberFormatException(int lineNumber) {
        super("WARNING: invalid number format in config file in line " + lineNumber);
    }
}
