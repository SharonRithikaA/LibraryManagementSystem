package model;

import java.io.Serializable;

/**
 * Represents a Book in the library.
 * Demonstrates OOP Encapsulation — all fields are private with public getters/setters.
 * Implements Serializable for file-based persistence.
 */
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    private String bookId;
    private String title;
    private String author;
    private String genre;
    private String isbn;
    private int totalCopies;
    private int availableCopies;

    // Constructor: initializes a new book; availableCopies starts equal to totalCopies
    public Book(String bookId, String title, String author, String genre, String isbn, int totalCopies) {
        this.bookId          = bookId;
        this.title           = title;
        this.author          = author;
        this.genre           = genre;
        this.isbn            = isbn;
        this.totalCopies     = totalCopies;
        this.availableCopies = totalCopies;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getBookId()          { return bookId; }
    public String getTitle()           { return title; }
    public String getAuthor()          { return author; }
    public String getGenre()           { return genre; }
    public String getIsbn()            { return isbn; }
    public int    getTotalCopies()     { return totalCopies; }
    public int    getAvailableCopies() { return availableCopies; }

    // ── Setters (used during updates) ────────────────────────────────────────
    public void setTitle(String title)                     { this.title = title; }
    public void setAuthor(String author)                   { this.author = author; }
    public void setGenre(String genre)                     { this.genre = genre; }
    public void setIsbn(String isbn)                       { this.isbn = isbn; }
    public void setTotalCopies(int totalCopies)            { this.totalCopies = totalCopies; }
    public void setAvailableCopies(int availableCopies)    { this.availableCopies = availableCopies; }

    /** Returns true if at least one copy is available for issue. */
    public boolean isAvailable() {
        return availableCopies > 0;
    }

    /** Formatted table row for display. */
    @Override
    public String toString() {
        return String.format("| %-8s | %-30s | %-20s | %-13s | %3d / %-3d |",
                bookId, truncate(title, 30), truncate(author, 20),
                truncate(genre, 13), availableCopies, totalCopies);
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 2) + "..";
    }
}
