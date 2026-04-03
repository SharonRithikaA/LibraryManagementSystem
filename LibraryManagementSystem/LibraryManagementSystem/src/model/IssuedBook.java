package model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents a single book-issue transaction.
 * Stores who issued what book, when, and whether it's returned.
 */
public class IssuedBook implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Default loan duration in days. */
    public static final int LOAN_PERIOD_DAYS = 14;

    private String    issueId;
    private String    bookId;
    private String    bookTitle;    // Stored for display even if book is later deleted
    private String    memberId;
    private String    memberName;   // Stored for display even if member is later deleted
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;   // null if not yet returned
    private boolean   returned;
    private double    fine;         // Calculated on return

    public IssuedBook(String issueId, String bookId, String bookTitle,
                      String memberId, String memberName) {
        this.issueId    = issueId;
        this.bookId     = bookId;
        this.bookTitle  = bookTitle;
        this.memberId   = memberId;
        this.memberName = memberName;
        this.issueDate  = LocalDate.now();
        this.dueDate    = LocalDate.now().plusDays(LOAN_PERIOD_DAYS);
        this.returned   = false;
        this.fine       = 0.0;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String    getIssueId()    { return issueId; }
    public String    getBookId()     { return bookId; }
    public String    getBookTitle()  { return bookTitle; }
    public String    getMemberId()   { return memberId; }
    public String    getMemberName() { return memberName; }
    public LocalDate getIssueDate()  { return issueDate; }
    public LocalDate getDueDate()    { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public boolean   isReturned()    { return returned; }
    public double    getFine()       { return fine; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public void setReturned(boolean returned)       { this.returned = returned; }
    public void setFine(double fine)               { this.fine = fine; }

    /** Returns true if the due date has passed and the book is still not returned. */
    public boolean isOverdue() {
        return !returned && LocalDate.now().isAfter(dueDate);
    }

    @Override
    public String toString() {
        String retDateStr = (returnDate != null) ? returnDate.toString() : "Pending";
        return String.format("| %-7s | %-8s | %-22s | %-8s | %-10s | %-10s | %-12s | Rs.%-7.2f |",
                issueId, bookId, truncate(bookTitle, 22), memberId,
                issueDate, dueDate, retDateStr, fine);
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 2) + "..";
    }
}
