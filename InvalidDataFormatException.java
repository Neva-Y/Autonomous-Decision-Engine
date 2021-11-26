import java.io.IOException;
/**
 * An exception for an unrecognised config file format that extends the IOException
 * COMP90041, Sem2, 2021: Final Project
 * @author Xing Yang Goh
 * student id: 1001969
 * student email: xingyangg@student.unimelb.edu.au
 */
public class InvalidDataFormatException extends IOException {
    /** Warning for an invalid data format exception
     * @param lineNumber: line in the config file where the error is found
     */
    public InvalidDataFormatException(int lineNumber) {
        super("WARNING: invalid data format in config file in line " + lineNumber);
    }
}