package exception;

/**
 * Thrown when a book with the given ID or search criteria is not found.
 */
public class BookNotFoundException extends Exception {
    public BookNotFoundException(String message) {
        super(message);
    }
}
