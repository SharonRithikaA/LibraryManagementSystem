package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a regular library member who can borrow books.
 * Extends User — demonstrates Inheritance.
 * Uses ArrayList — demonstrates Collections Framework.
 */
public class Member extends User {

    private static final long serialVersionUID = 1L;

    /** Maximum number of books a member can have at a time. */
    public static final int MAX_BOOKS_ALLOWED = 3;

    /** Tracks the IDs of actively issued (not yet returned) books. */
    private List<String> activeIssueIds;

    /** Lifetime count — for statistics/display. */
    private int totalBooksIssuedEver;

    public Member(String userId, String name, String email, String password) {
        super(userId, name, email, password);
        this.activeIssueIds       = new ArrayList<>(); // ArrayList usage
        this.totalBooksIssuedEver = 0;
    }

    @Override
    public String getRole() {
        return "MEMBER";
    }

    // ── Issue tracking helpers ────────────────────────────────────────────────

    /** Adds an issue ID when a book is issued to this member. */
    public void addIssueId(String issueId) {
        activeIssueIds.add(issueId);
        totalBooksIssuedEver++;
    }

    /** Removes an issue ID when the book is returned. */
    public void removeIssueId(String issueId) {
        activeIssueIds.remove(issueId);
    }

    /** Returns true if this member can still borrow more books. */
    public boolean canIssueMore() {
        return activeIssueIds.size() < MAX_BOOKS_ALLOWED;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public List<String> getActiveIssueIds()   { return activeIssueIds; }
    public int          getActiveIssueCount() { return activeIssueIds.size(); }
    public int          getTotalBooksIssuedEver() { return totalBooksIssuedEver; }
}
