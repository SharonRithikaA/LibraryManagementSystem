package service;

import exception.BookNotFoundException;
import exception.DuplicateEntryException;
import model.Book;
import util.FileHandler;
import util.IdGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for all book-related operations.
 * Demonstrates COLLECTIONS FRAMEWORK — uses HashMap<String, Book> as the main data store.
 * Demonstrates EXCEPTION HANDLING — throws custom exceptions for invalid operations.
 * Demonstrates FILE HANDLING — persists data through FileHandler.
 */
public class BookService {

    // HashMap<bookId, Book> — O(1) lookup by ID
    private HashMap<String, Book> bookMap;
    private static final String BOOKS_FILE = "books.dat";

    @SuppressWarnings("unchecked")
    public BookService() {
        bookMap = new HashMap<>();
        loadBooks(); // Load persisted data on startup
    }

    // ── CRUD Operations ───────────────────────────────────────────────────────

    /**
     * Adds a new book to the library.
     * @throws DuplicateEntryException if ISBN already exists
     */
    public void addBook(String title, String author, String genre, String isbn, int copies)
            throws DuplicateEntryException, IOException {

        // Validate for duplicate ISBN
        for (Book book : bookMap.values()) {
            if (book.getIsbn().equalsIgnoreCase(isbn)) {
                throw new DuplicateEntryException(
                        "A book with ISBN [" + isbn + "] already exists: " + book.getTitle());
            }
        }

        String bookId = IdGenerator.generateBookId();
        Book newBook  = new Book(bookId, title, author, genre, isbn, copies);
        bookMap.put(bookId, newBook);
        saveBooks();
        System.out.println("[Info] Book added with ID: " + bookId);
    }

    /**
     * Removes a book by ID.
     * @throws BookNotFoundException if the book doesn't exist
     */
    public void removeBook(String bookId) throws BookNotFoundException, IOException {
        if (!bookMap.containsKey(bookId)) {
            throw new BookNotFoundException("Book not found with ID: " + bookId);
        }
        bookMap.remove(bookId);
        saveBooks();
    }

    /**
     * Updates book details. Empty strings or 0 mean "keep existing value".
     */
    public void updateBook(String bookId, String newTitle, String newAuthor,
                           String newGenre, int newTotalCopies)
            throws BookNotFoundException, IOException {

        Book book = getBookById(bookId); // throws if not found

        if (newTitle != null && !newTitle.isEmpty())  book.setTitle(newTitle);
        if (newAuthor != null && !newAuthor.isEmpty()) book.setAuthor(newAuthor);
        if (newGenre != null && !newGenre.isEmpty())   book.setGenre(newGenre);

        if (newTotalCopies > 0) {
            int delta = newTotalCopies - book.getTotalCopies();
            book.setTotalCopies(newTotalCopies);
            // Adjust available copies proportionally — never go below 0
            book.setAvailableCopies(Math.max(0, book.getAvailableCopies() + delta));
        }

        saveBooks();
    }

    // ── Search / Retrieve ─────────────────────────────────────────────────────

    /**
     * Finds a book by exact ID.
     * @throws BookNotFoundException if not found
     */
    public Book getBookById(String bookId) throws BookNotFoundException {
        Book book = bookMap.get(bookId);
        if (book == null) {
            throw new BookNotFoundException("Book not found with ID: " + bookId);
        }
        return book;
    }

    /** Case-insensitive partial-match search by title. */
    public List<Book> searchByTitle(String keyword) {
        String lower = keyword.toLowerCase();
        return bookMap.values().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    /** Case-insensitive partial-match search by author. */
    public List<Book> searchByAuthor(String keyword) {
        String lower = keyword.toLowerCase();
        return bookMap.values().stream()
                .filter(b -> b.getAuthor().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    /** Returns all books in the library. */
    public List<Book> getAllBooks() {
        return new ArrayList<>(bookMap.values());
    }

    /** Returns only books with at least one available copy. */
    public List<Book> getAvailableBooks() {
        return bookMap.values().stream()
                .filter(Book::isAvailable)
                .collect(Collectors.toList());
    }

    // ── Availability Management (called by IssueService) ──────────────────────

    public void decrementAvailability(String bookId) throws BookNotFoundException, IOException {
        Book book = getBookById(bookId);
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        saveBooks();
    }

    public void incrementAvailability(String bookId) throws BookNotFoundException, IOException {
        Book book = getBookById(bookId);
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        saveBooks();
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    /** Saves the entire book map to disk. */
    public void saveBooks() throws IOException {
        FileHandler.saveObject(bookMap, BOOKS_FILE);
    }

    /** Loads book data from disk. Safe to call even if file doesn't exist yet. */
    @SuppressWarnings("unchecked")
    private void loadBooks() {
        try {
            Object data = FileHandler.loadObject(BOOKS_FILE);
            if (data != null) {
                bookMap = (HashMap<String, Book>) data;
                // Sync the ID generator so new IDs don't clash with loaded ones
                bookMap.keySet().forEach(id -> {
                    try {
                        int num = Integer.parseInt(id.replace("BK", ""));
                        IdGenerator.syncBookCounter(num);
                    } catch (NumberFormatException ignored) { }
                });
                System.out.println("[Info] Loaded " + bookMap.size() + " book(s) from disk.");
            }
        } catch (Exception e) {
            System.err.println("[Warning] Could not load books data: " + e.getMessage());
            bookMap = new HashMap<>();
        }
    }
}
