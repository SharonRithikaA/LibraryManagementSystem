package controller;

import exception.BookNotFoundException;
import exception.DuplicateEntryException;
import model.Book;
import service.BookService;

import java.util.List;
import java.util.Scanner;

/**
 * Handles all CLI interactions related to books.
 * Admin gets the full menu; members get a read-only browse menu.
 */
public class BookController {

    private final BookService bookService;
    private final Scanner     scanner;

    public BookController(BookService bookService, Scanner scanner) {
        this.bookService = bookService;
        this.scanner     = scanner;
    }

    // ── Admin Menu ────────────────────────────────────────────────────────────

    /** Full book management menu — for admins only. */
    public void showAdminBookMenu() {
        while (true) {
            System.out.println("\n╔════════════════════════════════╗");
            System.out.println("║       BOOK MANAGEMENT          ║");
            System.out.println("╠════════════════════════════════╣");
            System.out.println("║  1. Add New Book               ║");
            System.out.println("║  2. Remove a Book              ║");
            System.out.println("║  3. Update Book Details        ║");
            System.out.println("║  4. View All Books             ║");
            System.out.println("║  5. View Available Books       ║");
            System.out.println("║  6. Search Books               ║");
            System.out.println("║  0. Back to Dashboard          ║");
            System.out.println("╚════════════════════════════════╝");
            System.out.print("  Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1": addBook();              break;
                case "2": removeBook();           break;
                case "3": updateBook();           break;
                case "4": viewAllBooks();         break;
                case "5": viewAvailableBooks();   break;
                case "6": searchMenu();           break;
                case "0": return;
                default:  System.out.println("  [!] Invalid choice.");
            }
        }
    }

    /** Read-only browse menu — for members. */
    public void showMemberBrowseMenu() {
        while (true) {
            System.out.println("\n╔════════════════════════════════╗");
            System.out.println("║         BROWSE BOOKS           ║");
            System.out.println("╠════════════════════════════════╣");
            System.out.println("║  1. View All Books             ║");
            System.out.println("║  2. View Available Books       ║");
            System.out.println("║  3. Search Books               ║");
            System.out.println("║  0. Back to Dashboard          ║");
            System.out.println("╚════════════════════════════════╝");
            System.out.print("  Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1": viewAllBooks();       break;
                case "2": viewAvailableBooks(); break;
                case "3": searchMenu();         break;
                case "0": return;
                default:  System.out.println("  [!] Invalid choice.");
            }
        }
    }

    // ── Private Action Methods ────────────────────────────────────────────────

    private void addBook() {
        System.out.println("\n  ── Add New Book ──");
        try {
            System.out.print("  Title        : "); String title  = scanner.nextLine().trim();
            System.out.print("  Author       : "); String author = scanner.nextLine().trim();
            System.out.print("  Genre        : "); String genre  = scanner.nextLine().trim();
            System.out.print("  ISBN         : "); String isbn   = scanner.nextLine().trim();
            System.out.print("  Total Copies : ");
            int copies = Integer.parseInt(scanner.nextLine().trim());

            if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
                System.out.println("  [!] Title, Author, and ISBN cannot be empty.");
                return;
            }
            if (copies <= 0) {
                System.out.println("  [!] Copies must be at least 1.");
                return;
            }

            bookService.addBook(title, author, genre, isbn, copies);
            System.out.println("  [✓] Book added successfully!");

        } catch (DuplicateEntryException e) {
            System.out.println("  [!] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [!] Copies must be a valid number.");
        } catch (Exception e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
    }

    private void removeBook() {
        System.out.println("\n  ── Remove Book ──");
        System.out.print("  Enter Book ID: ");
        String bookId = scanner.nextLine().trim().toUpperCase();

        try {
            Book book = bookService.getBookById(bookId);
            System.out.println("  Book: " + book.getTitle() + " by " + book.getAuthor());
            System.out.print("  Confirm removal? (yes/no): ");
            if ("yes".equalsIgnoreCase(scanner.nextLine().trim())) {
                bookService.removeBook(bookId);
                System.out.println("  [✓] Book removed.");
            } else {
                System.out.println("  [~] Cancelled.");
            }
        } catch (BookNotFoundException e) {
            System.out.println("  [!] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    private void updateBook() {
        System.out.println("\n  ── Update Book ──");
        System.out.print("  Enter Book ID: ");
        String bookId = scanner.nextLine().trim().toUpperCase();

        try {
            Book book = bookService.getBookById(bookId);
            System.out.println("  Current → " + book.getTitle() + " | " + book.getAuthor()
                    + " | " + book.getGenre() + " | Copies: " + book.getTotalCopies());
            System.out.println("  (Press Enter to keep existing value)");

            System.out.print("  New Title  : "); String title  = scanner.nextLine().trim();
            System.out.print("  New Author : "); String author = scanner.nextLine().trim();
            System.out.print("  New Genre  : "); String genre  = scanner.nextLine().trim();
            System.out.print("  New Copies (0 = keep): ");
            int copies = Integer.parseInt(scanner.nextLine().trim());

            bookService.updateBook(bookId, title, author, genre, copies);
            System.out.println("  [✓] Book updated successfully!");

        } catch (BookNotFoundException e) {
            System.out.println("  [!] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [!] Please enter a valid number for copies.");
        } catch (Exception e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    private void viewAllBooks() {
        displayBooks(bookService.getAllBooks(), "ALL BOOKS IN LIBRARY");
    }

    private void viewAvailableBooks() {
        displayBooks(bookService.getAvailableBooks(), "AVAILABLE FOR BORROWING");
    }

    private void searchMenu() {
        System.out.println("\n  ── Search Books ──");
        System.out.println("  1. By Title");
        System.out.println("  2. By Author");
        System.out.println("  3. By Book ID");
        System.out.print("  Choice: ");

        try {
            switch (scanner.nextLine().trim()) {
                case "1":
                    System.out.print("  Title keyword: ");
                    displayBooks(bookService.searchByTitle(scanner.nextLine().trim()), "RESULTS");
                    break;
                case "2":
                    System.out.print("  Author name: ");
                    displayBooks(bookService.searchByAuthor(scanner.nextLine().trim()), "RESULTS");
                    break;
                case "3":
                    System.out.print("  Book ID: ");
                    String id = scanner.nextLine().trim().toUpperCase();
                    Book b = bookService.getBookById(id);
                    System.out.println("\n" + getBookHeader());
                    System.out.println(b);
                    System.out.println(divider(88));
                    break;
                default:
                    System.out.println("  [!] Invalid choice.");
            }
        } catch (BookNotFoundException e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    // ── Display Helpers ───────────────────────────────────────────────────────

    private void displayBooks(List<Book> books, String heading) {
        System.out.println("\n  ═══ " + heading + " ═══");
        if (books.isEmpty()) {
            System.out.println("  (No books found)");
            return;
        }
        System.out.println(getBookHeader());
        System.out.println(divider(88));
        books.forEach(System.out::println);
        System.out.println(divider(88));
        System.out.println("  Total: " + books.size() + " book(s)");
    }

    private String getBookHeader() {
        return String.format("  | %-8s | %-30s | %-20s | %-13s | %-9s |",
                "Book ID", "Title", "Author", "Genre", "Avail/Tot");
    }

    private String divider(int len) {
        return "  " + "-".repeat(len);
    }
}
