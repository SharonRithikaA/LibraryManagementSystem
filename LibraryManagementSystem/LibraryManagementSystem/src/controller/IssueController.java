package controller;

import model.IssuedBook;
import model.User;
import service.IssueService;
import util.DateUtil;
import util.FineCalculator;

import java.util.List;
import java.util.Scanner;

/**
 * Handles all CLI interactions for issuing and returning books.
 * Shows admins the full operations menu; members can see their own borrowed books.
 */
public class IssueController {

    private final IssueService issueService;
    private final Scanner      scanner;

    public IssueController(IssueService issueService, Scanner scanner) {
        this.issueService = issueService;
        this.scanner      = scanner;
    }

    // ── Admin Menu ────────────────────────────────────────────────────────────

    public void showAdminIssueMenu() {
        while (true) {
            System.out.println("\n╔════════════════════════════════╗");
            System.out.println("║     ISSUE / RETURN BOOKS       ║");
            System.out.println("╠════════════════════════════════╣");
            System.out.println("║  1. Issue a Book               ║");
            System.out.println("║  2. Return a Book              ║");
            System.out.println("║  3. View All Active Issues     ║");
            System.out.println("║  4. View All Overdue Books     ║");
            System.out.println("║  5. View Complete History      ║");
            System.out.println("║  6. View Issues by Member      ║");
            System.out.println("║  0. Back to Dashboard          ║");
            System.out.println("╚════════════════════════════════╝");
            System.out.print("  Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1": issueBook();                                break;
                case "2": returnBook();                               break;
                case "3": displayIssues(issueService.getActiveIssues(),    "ACTIVE ISSUES");   break;
                case "4": displayOverdueIssues();                    break;
                case "5": displayIssues(issueService.getAllIssues(),       "FULL HISTORY");    break;
                case "6": viewByMember();                             break;
                case "0": return;
                default:  System.out.println("  [!] Invalid choice.");
            }
        }
    }

    // ── Member Menu ───────────────────────────────────────────────────────────

    /** Lets a member issue a book and view their own loans. */
    public void showMemberIssueMenu(User currentUser) {
        while (true) {
            System.out.println("\n╔════════════════════════════════╗");
            System.out.println("║      MY BORROWED BOOKS         ║");
            System.out.println("╠════════════════════════════════╣");
            System.out.println("║  1. Borrow a Book              ║");
            System.out.println("║  2. Return a Book              ║");
            System.out.println("║  3. View My Current Loans      ║");
            System.out.println("║  4. View My Full History       ║");
            System.out.println("║  0. Back to Dashboard          ║");
            System.out.println("╚════════════════════════════════╝");
            System.out.print("  Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1": issueSelf(currentUser.getUserId()); break;
                case "2": returnBook();                       break;
                case "3":
                    displayIssues(
                        issueService.getIssuesByMember(currentUser.getUserId())
                            .stream()
                            .filter(r -> !r.isReturned())
                            .collect(java.util.stream.Collectors.toList()),
                        "MY CURRENT LOANS");
                    break;
                case "4":
                    displayIssues(
                        issueService.getIssuesByMember(currentUser.getUserId()),
                        "MY BORROW HISTORY");
                    break;
                case "0": return;
                default: System.out.println("  [!] Invalid choice.");
            }
        }
    }

    // ── Private Action Methods ────────────────────────────────────────────────

    /** Admin-initiated issue: asks for both Book ID and Member ID. */
    private void issueBook() {
        System.out.println("\n  ── Issue Book ──");
        System.out.print("  Book ID   : "); String bookId   = scanner.nextLine().trim().toUpperCase();
        System.out.print("  Member ID : "); String memberId = scanner.nextLine().trim().toUpperCase();

        try {
            IssuedBook record = issueService.issueBook(bookId, memberId);
            System.out.println("  [✓] Book issued successfully!");
            System.out.println("  Issue ID   : " + record.getIssueId());
            System.out.println("  Book       : " + record.getBookTitle());
            System.out.println("  Member     : " + record.getMemberName());
            System.out.println("  Issue Date : " + DateUtil.format(record.getIssueDate()));
            System.out.println("  Due Date   : " + DateUtil.format(record.getDueDate())
                    + "  (" + IssuedBook.LOAN_PERIOD_DAYS + " days)");
        } catch (Exception e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    /** Member-initiated issue: member ID is already known from login. */
    private void issueSelf(String memberId) {
        System.out.println("\n  ── Borrow a Book ──");
        System.out.print("  Enter Book ID: "); String bookId = scanner.nextLine().trim().toUpperCase();

        try {
            IssuedBook record = issueService.issueBook(bookId, memberId);
            System.out.println("  [✓] Book borrowed successfully!");
            System.out.println("  Issue ID : " + record.getIssueId());
            System.out.println("  Book     : " + record.getBookTitle());
            System.out.println("  Due Date : " + DateUtil.format(record.getDueDate())
                    + "  (return by this date to avoid fines)");
        } catch (Exception e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    private void returnBook() {
        System.out.println("\n  ── Return Book ──");
        System.out.print("  Enter Issue ID: "); String issueId = scanner.nextLine().trim().toUpperCase();

        try {
            IssuedBook record = issueService.returnBook(issueId);
            System.out.println("  [✓] Book returned successfully!");
            System.out.println("  Book        : " + record.getBookTitle());
            System.out.println("  Return Date : " + DateUtil.format(record.getReturnDate()));

            if (record.getFine() > 0) {
                long overdue = FineCalculator.daysOverdue(record.getDueDate());
                System.out.println("  ┌─────────────────────────────────────┐");
                System.out.printf("  │  ⚠  OVERDUE FINE: Rs. %-6.2f        │%n", record.getFine());
                System.out.println("  │  Days Overdue : " + overdue + " day(s)              │");
                System.out.printf("  │  Rate         : Rs.%.1f/day            │%n", FineCalculator.FINE_PER_DAY);
                System.out.println("  └─────────────────────────────────────┘");
            } else {
                System.out.println("  No fine. Returned on time. ✓");
            }
        } catch (Exception e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    private void viewByMember() {
        System.out.print("  Member ID: "); String memberId = scanner.nextLine().trim().toUpperCase();
        displayIssues(issueService.getIssuesByMember(memberId), "ISSUES FOR " + memberId);
    }

    private void displayOverdueIssues() {
        List<IssuedBook> overdue = issueService.getOverdueIssues();
        System.out.println("\n  ═══ OVERDUE BOOKS ═══");
        if (overdue.isEmpty()) {
            System.out.println("  No overdue books. 🎉");
            return;
        }
        System.out.println(getIssueHeader());
        System.out.println(divider(112));
        for (IssuedBook r : overdue) {
            System.out.println(r);
            long days = FineCalculator.daysOverdue(r.getDueDate());
            double projectedFine = FineCalculator.calculateFine(r.getDueDate(), null);
            System.out.printf("        → Overdue by %d day(s) | Projected fine: Rs.%.2f%n",
                    days, projectedFine);
        }
        System.out.println(divider(112));
        System.out.println("  Total overdue: " + overdue.size());
    }

    // ── Display Helpers ───────────────────────────────────────────────────────

    private void displayIssues(List<IssuedBook> issues, String heading) {
        System.out.println("\n  ═══ " + heading + " ═══");
        if (issues.isEmpty()) { System.out.println("  (No records found)"); return; }
        System.out.println(getIssueHeader());
        System.out.println(divider(112));
        issues.forEach(System.out::println);
        System.out.println(divider(112));
        System.out.println("  Total: " + issues.size() + " record(s)");
    }

    private String getIssueHeader() {
        return String.format("  | %-7s | %-8s | %-22s | %-8s | %-10s | %-10s | %-12s | %-10s |",
                "IssueID", "Book ID", "Book Title", "MemberID",
                "IssueDate", "DueDate", "ReturnDate", "Fine(Rs.)");
    }

    private String divider(int len) {
        return "  " + "-".repeat(len);
    }
}
