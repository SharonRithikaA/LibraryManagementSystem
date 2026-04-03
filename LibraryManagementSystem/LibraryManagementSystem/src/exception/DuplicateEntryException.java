package exception;

/**
 * Thrown when adding a book or user that already exists (duplicate ISBN or email).
 */
public class DuplicateEntryException extends Exception {
    public DuplicateEntryException(String message) {
        super(message);
    }
}
