package service;

import exception.BookNotAvailableException;
import exception.BookNotFoundException;
import exception.UserNotFoundException;
import model.Book;
import model.IssuedBook;
import model.Member;
import util.FileHandler;
import util.FineCalculator;
import util.IdGenerator;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer that manages book issue and return transactions.
 * Ties together BookService and UserService to perform business operations.
 * Demonstrates EXCEPTION HANDLING and COLLECTIONS FRAMEWORK (HashMap).
 */
public class IssueService {

    private HashMap<String, IssuedBook> issueMap; // issueId → IssuedBook
    private static final String ISSUED_FILE = "issued.dat";

    private final BookService bookService;
    private final UserService userService;

    @SuppressWarnings("unchecked")
    public IssueService(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
        issueMap = new HashMap<>();
        loadIssuedBooks();
    }

    // ── Core Operations ───────────────────────────────────────────────────────

    /**
     * Issues a book to a member.
     * Validates: book exists, book is available, member can borrow more, not already borrowed.
     *
     * @return The created IssuedBook record
     * @throws BookNotFoundException     if bookId doesn't exist
     * @throws BookNotAvailableException if no copies are free or member limit reached
     * @throws UserNotFoundException     if memberId doesn't exist or isn't a member
     */
    public IssuedBook issueBook(String bookId, String memberId)
            throws BookNotFoundException, BookNotAvailableException, UserNotFoundException, IOException {

        Book   book   = bookService.getBookById(bookId);      // throws BookNotFoundException
        Member member = userService.getMemberById(memberId);   // throws UserNotFoundException

        // Business rule: book must have at least one available copy
        if (!book.isAvailable()) {
            throw new BookNotAvailableException(
                    "No copies available for: \"" + book.getTitle() + "\"");
        }

        // Business rule: member can't exceed their borrowing limit
        if (!member.canIssueMore()) {
            throw new BookNotAvailableException(
                    member.getName() + " has already borrowed the maximum of "
                    + Member.MAX_BOOKS_ALLOWED + " book(s).");
        }

        // Business rule: member can't borrow the same book twice
        for (String existingIssueId : member.getActiveIssueIds()) {
            IssuedBook existing = issueMap.get(existingIssueId);
            if (existing != null && existing.getBookId().equals(bookId)) {
                throw new BookNotAvailableException(
                        member.getName() + " has already borrowed this book.");
            }
        }

        // All checks passed — create the issue record
        String     issueId    = IdGenerator.generateIssueId();
        IssuedBook issuedBook = new IssuedBook(
                issueId, bookId, book.getTitle(), memberId, member.getName());

        issueMap.put(issueId, issuedBook);
        bookService.decrementAvailability(bookId);
        member.addIssueId(issueId);
        userService.saveUsers();
        saveIssuedBooks();

        return issuedBook;
    }

    /**
     * Processes the return of an issued book.
     * Calculates any fine and updates all related records.
     *
     * @param issueId The issue transaction ID
     * @return The updated IssuedBook with fine information
     */
    public IssuedBook returnBook(String issueId) throws Exception {
        IssuedBook record = issueMap.get(issueId);

        if (record == null) {
            throw new Exception("No issue record found with ID: " + issueId);
        }
        if (record.isReturned()) {
            throw new Exception("Book [" + record.getBookTitle() + "] was already returned on "
                    + record.getReturnDate());
        }

        LocalDate today = LocalDate.now();
        double    fine  = FineCalculator.calculateFine(record.getDueDate(), today);

        // Update the issue record
        record.setReturnDate(today);
        record.setReturned(true);
        record.setFine(fine);

        // Restore book availability
        bookService.incrementAvailability(record.getBookId());

        // Remove from member's active issue list
        Member member = userService.getMemberById(record.getMemberId());
        member.removeIssueId(issueId);
        userService.saveUsers();

        saveIssuedBooks();
        return record;
    }

    // ── Query Methods ─────────────────────────────────────────────────────────

    public IssuedBook getIssueById(String issueId) throws Exception {
        IssuedBook record = issueMap.get(issueId);
        if (record == null) throw new Exception("Issue ID not found: " + issueId);
        return record;
    }

    /** All issue records (past and present). */
    public List<IssuedBook> getAllIssues() {
        return new ArrayList<>(issueMap.values());
    }

    /** Records where the book hasn't been returned yet. */
    public List<IssuedBook> getActiveIssues() {
        return issueMap.values().stream()
                .filter(r -> !r.isReturned())
                .collect(Collectors.toList());
    }

    /** Active issues where today's date is past the due date. */
    public List<IssuedBook> getOverdueIssues() {
        return issueMap.values().stream()
                .filter(IssuedBook::isOverdue)
                .collect(Collectors.toList());
    }

    /** All records (active + returned) for a specific member. */
    public List<IssuedBook> getIssuesByMember(String memberId) {
        return issueMap.values().stream()
                .filter(r -> r.getMemberId().equals(memberId))
                .collect(Collectors.toList());
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public void saveIssuedBooks() throws IOException {
        FileHandler.saveObject(issueMap, ISSUED_FILE);
    }

    @SuppressWarnings("unchecked")
    private void loadIssuedBooks() {
        try {
            Object data = FileHandler.loadObject(ISSUED_FILE);
            if (data != null) {
                issueMap = (HashMap<String, IssuedBook>) data;
                issueMap.keySet().forEach(id -> {
                    try {
                        int num = Integer.parseInt(id.replace("ISS", ""));
                        IdGenerator.syncIssueCounter(num);
                    } catch (NumberFormatException ignored) { }
                });
                System.out.println("[Info] Loaded " + issueMap.size() + " issue record(s) from disk.");
            }
        } catch (Exception e) {
            System.err.println("[Warning] Could not load issue records: " + e.getMessage());
            issueMap = new HashMap<>();
        }
    }
}
