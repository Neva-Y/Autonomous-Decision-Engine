/**
 * An exception for a provided directory that does not exist
 * COMP90041, Sem2, 2021: Final Project
 * @author Xing Yang Goh
 * student id: 1001969
 * student email: xingyangg@student.unimelb.edu.au
 */
public class DirectoryNotFoundException extends Exception{
    /** Error when a provided directory does not exist
     */
    public DirectoryNotFoundException() {
        super("ERROR: could not print results. Target directory does not exist.");
    }

}
