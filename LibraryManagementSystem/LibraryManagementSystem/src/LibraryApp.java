import controller.BookController;
import controller.IssueController;
import controller.UserController;
import exception.DuplicateEntryException;
import model.User;
import service.BookService;
import service.IssueService;
import service.UserService;
import util.AutoSaveThread;

import java.util.Scanner;

/**
 * ╔══════════════════════════════════════════════════╗
 * ║        LIBRARY MANAGEMENT SYSTEM v1.0            ║
 * ║        Java Mini Project — CSE 1st Year          ║
 * ╚══════════════════════════════════════════════════╝
 *
 * Entry point of the application.
 * Bootstraps all services, starts the auto-save thread,
 * handles login, and routes to the correct dashboard.
 *
 * Java Features Used:
 *  1. OOP       — Encapsulation, Inheritance (Admin/Member extends User), Polymorphism (getRole())
 *  2. Collections — HashMap and ArrayList throughout services
 *  3. Exceptions  — Custom exceptions in all service layers
 *  4. File I/O    — FileHandler with ObjectOutputStream / ObjectInputStream
 *  5. Threads     — AutoSaveThread (daemon background thread with volatile flag)
 */
public class LibraryApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        printBanner();

        // ── Bootstrap Services ─────────────────────────────────────────────
        System.out.println("  Initialising system...");
        BookService  bookService  = new BookService();
        UserService  userService  = new UserService();
        IssueService issueService = new IssueService(bookService, userService);

        // ── Start Background Auto-Save Thread ─────────────────────────────
        // Demonstrates MULTITHREADING — saves data every 2 minutes automatically
        AutoSaveThread autoSave = new AutoSaveThread(bookService, userService, issueService);
        autoSave.start();

        // ── Build Controllers ─────────────────────────────────────────────
        BookController  bookController  = new BookController(bookService, scanner);
        UserController  userController  = new UserController(userService, scanner);
        IssueController issueController = new IssueController(issueService, scanner);

        // ── Main Application Loop ─────────────────────────────────────────
        boolean appRunning = true;
        while (appRunning) {
            int choice = showMainMenu(scanner);
            switch (choice) {
                case 1:
                    // Login flow
                    User loggedInUser = handleLogin(scanner, userService);
                    if (loggedInUser != null) {
                        routeToDashboard(loggedInUser, scanner,
                                bookController, userController, issueController);
                    }
                    break;

                case 2:
                    // Self-registration for new members
                    handleRegistration(scanner, userService);
                    break;

                case 0:
                    appRunning = false;
                    break;

                default:
                    System.out.println("  [!] Invalid option. Try again.");
            }
        }

        // ── Graceful Shutdown ─────────────────────────────────────────────
        System.out.println("\n  Saving all data before exit...");
        autoSave.stopAutoSave();
        try {
            bookService.saveBooks();
            userService.saveUsers();
            issueService.saveIssuedBooks();
            System.out.println("  [✓] All data saved.");
        } catch (Exception e) {
            System.err.println("  [!] Could not save data: " + e.getMessage());
        }

        scanner.close();
        System.out.println("\n  Thank you for using Library Management System. Goodbye!");
    }

    // ── Main Menu ─────────────────────────────────────────────────────────────

    private static int showMainMenu(Scanner scanner) {
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║           MAIN MENU            ║");
        System.out.println("╠════════════════════════════════╣");
        System.out.println("║  1. Login                      ║");
        System.out.println("║  2. Register as Member         ║");
        System.out.println("║  0. Exit                       ║");
        System.out.println("╚════════════════════════════════╝");
        System.out.print("  Choice: ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    private static User handleLogin(Scanner scanner, UserService userService) {
        System.out.println("\n  ── Login ──");
        System.out.print("  Email    : ");
        String email = scanner.nextLine().trim();
        System.out.print("  Password : ");
        String password = scanner.nextLine().trim();

        try {
            User user = userService.login(email, password);
            System.out.println("\n  Welcome, " + user.getName() + "! [" + user.getRole() + "]");
            return user;
        } catch (Exception e) {
            System.out.println("  [!] Login failed: " + e.getMessage());
            return null;
        }
    }

    // ── Registration ──────────────────────────────────────────────────────────

    private static void handleRegistration(Scanner scanner, UserService userService) {
        System.out.println("\n  ── Member Registration ──");
        try {
            System.out.print("  Name     : "); String name  = scanner.nextLine().trim();
            System.out.print("  Email    : "); String email = scanner.nextLine().trim();
            System.out.print("  Password : "); String pass  = scanner.nextLine().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                System.out.println("  [!] All fields are required.");
                return;
            }

            var member = userService.registerMember(name, email, pass);
            System.out.println("  [✓] Registered successfully! Your User ID: " + member.getUserId());
            System.out.println("  You can now log in with your email and password.");

        } catch (DuplicateEntryException e) {
            System.out.println("  [!] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [!] Registration error: " + e.getMessage());
        }
    }

    // ── Dashboard Routing (Polymorphism in action) ────────────────────────────

    /**
     * Routes the logged-in user to their respective dashboard.
     * Demonstrates OOP Polymorphism — getRole() behaves differently for Admin vs Member.
     */
    private static void routeToDashboard(User user, Scanner scanner,
                                          BookController bookCtrl,
                                          UserController userCtrl,
                                          IssueController issueCtrl) {
        switch (user.getRole()) {
            case "ADMIN":  showAdminDashboard(user, scanner, bookCtrl, userCtrl, issueCtrl); break;
            case "MEMBER": showMemberDashboard(user, scanner, bookCtrl, userCtrl, issueCtrl); break;
            default: System.out.println("  [!] Unknown role. Access denied.");
        }
    }

    // ── Admin Dashboard ────────────────────────────────────────────────────────

    private static void showAdminDashboard(User admin, Scanner scanner,
                                            BookController bookCtrl,
                                            UserController userCtrl,
                                            IssueController issueCtrl) {
        boolean active = true;
        while (active) {
            System.out.println("\n╔════════════════════════════════╗");
            System.out.println("║       ADMIN DASHBOARD          ║");
            System.out.printf( "║  Logged in: %-18s ║%n", admin.getName());
            System.out.println("╠════════════════════════════════╣");
            System.out.println("║  1. Book Management            ║");
            System.out.println("║  2. User Management            ║");
            System.out.println("║  3. Issue / Return Books       ║");
            System.out.println("║  0. Logout                     ║");
            System.out.println("╚════════════════════════════════╝");
            System.out.print("  Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1": bookCtrl.showAdminBookMenu();   break;
                case "2": userCtrl.showAdminUserMenu();   break;
                case "3": issueCtrl.showAdminIssueMenu(); break;
                case "0": active = false; System.out.println("  Logged out."); break;
                default:  System.out.println("  [!] Invalid option.");
            }
        }
    }

    // ── Member Dashboard ───────────────────────────────────────────────────────

    private static void showMemberDashboard(User member, Scanner scanner,
                                             BookController bookCtrl,
                                             UserController userCtrl,
                                             IssueController issueCtrl) {
        boolean active = true;
        while (active) {
            System.out.println("\n╔════════════════════════════════╗");
            System.out.println("║       MEMBER DASHBOARD         ║");
            System.out.printf( "║  Logged in: %-18s ║%n", member.getName());
            System.out.println("╠════════════════════════════════╣");
            System.out.println("║  1. Browse Books               ║");
            System.out.println("║  2. Borrow / Return Books      ║");
            System.out.println("║  3. My Profile                 ║");
            System.out.println("║  0. Logout                     ║");
            System.out.println("╚════════════════════════════════╝");
            System.out.print("  Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1": bookCtrl.showMemberBrowseMenu();           break;
                case "2": issueCtrl.showMemberIssueMenu(member);     break;
                case "3": userCtrl.showMemberProfile(member);        break;
                case "0": active = false; System.out.println("  Logged out."); break;
                default:  System.out.println("  [!] Invalid option.");
            }
        }
    }

    // ── Banner ────────────────────────────────────────────────────────────────

    private static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════╗");
        System.out.println("  ║      📚 LIBRARY MANAGEMENT SYSTEM v1.0          ║");
        System.out.println("  ║      Java Mini Project  |  CSE — 1st Year       ║");
        System.out.println("  ╠══════════════════════════════════════════════════╣");
        System.out.println("  ║  Default Admin  →  admin@library.com            ║");
        System.out.println("  ║  Password       →  admin123                     ║");
        System.out.println("  ╚══════════════════════════════════════════════════╝");
        System.out.println();
    }
}
