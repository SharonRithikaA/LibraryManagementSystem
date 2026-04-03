package exception;

/**
 * Thrown when a user cannot be found by ID, email, or other lookup.
 */
public class UserNotFoundException extends Exception {
    public UserNotFoundException(String message) {
        super(message);
    }
}
