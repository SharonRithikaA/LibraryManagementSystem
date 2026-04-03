package controller;

import exception.DuplicateEntryException;
import exception.UserNotFoundException;
import model.Member;
import model.User;
import service.UserService;

import java.util.List;
import java.util.Scanner;

/**
 * Handles all CLI interactions related to user management.
 * Admins can view all users, register admins, and remove members.
 * Members can view their own profile.
 */
public class UserController {

    private final UserService userService;
    private final Scanner     scanner;

    public UserController(UserService userService, Scanner scanner) {
        this.userService = userService;
        this.scanner     = scanner;
    }

    // ── Admin Menu ────────────────────────────────────────────────────────────

    public void showAdminUserMenu() {
        while (true) {
            System.out.println("\n╔════════════════════════════════╗");
            System.out.println("║       USER MANAGEMENT          ║");
            System.out.println("╠════════════════════════════════╣");
            System.out.println("║  1. View All Users             ║");
            System.out.println("║  2. View All Members           ║");
            System.out.println("║  3. Add Admin Account          ║");
            System.out.println("║  4. Remove a User              ║");
            System.out.println("║  0. Back to Dashboard          ║");
            System.out.println("╚════════════════════════════════╝");
            System.out.print("  Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1": viewAllUsers();    break;
                case "2": viewAllMembers();  break;
                case "3": addAdmin();        break;
                case "4": removeUser();      break;
                case "0": return;
                default:  System.out.println("  [!] Invalid choice.");
            }
        }
    }

    /** Displays the logged-in member's profile details. */
    public void showMemberProfile(User currentUser) {
        System.out.println("\n  ══ My Profile ══");
        System.out.printf("  User ID  : %s%n", currentUser.getUserId());
        System.out.printf("  Name     : %s%n", currentUser.getName());
        System.out.printf("  Email    : %s%n", currentUser.getEmail());
        System.out.printf("  Role     : %s%n", currentUser.getRole());

        if (currentUser instanceof Member) {
            Member member = (Member) currentUser;
            System.out.printf("  Active Loans   : %d / %d%n",
                    member.getActiveIssueCount(), Member.MAX_BOOKS_ALLOWED);
            System.out.printf("  All-time Issued: %d book(s)%n",
                    member.getTotalBooksIssuedEver());
        }
    }

    // ── Private Action Methods ────────────────────────────────────────────────

    private void viewAllUsers() {
        List<User> users = userService.getAllUsers();
        System.out.println("\n  ═══ ALL REGISTERED USERS ═══");
        if (users.isEmpty()) { System.out.println("  (No users found)"); return; }
        System.out.println(getUserHeader());
        System.out.println(divider(76));
        users.forEach(System.out::println);
        System.out.println(divider(76));
        System.out.println("  Total: " + users.size() + " user(s)");
    }

    private void viewAllMembers() {
        List<Member> members = userService.getAllMembers();
        System.out.println("\n  ═══ ALL MEMBERS ═══");
        if (members.isEmpty()) { System.out.println("  (No members found)"); return; }
        System.out.println(getUserHeader());
        System.out.println(divider(76));
        members.forEach(m -> System.out.printf(
                "| %-8s | %-20s | %-28s | %-7s | Loans: %d/%d |%n",
                m.getUserId(), m.getName(), m.getEmail(), m.getRole(),
                m.getActiveIssueCount(), Member.MAX_BOOKS_ALLOWED));
        System.out.println(divider(76));
        System.out.println("  Total: " + members.size() + " member(s)");
    }

    private void addAdmin() {
        System.out.println("\n  ── Register New Admin ──");
        try {
            System.out.print("  Name     : "); String name  = scanner.nextLine().trim();
            System.out.print("  Email    : "); String email = scanner.nextLine().trim();
            System.out.print("  Password : "); String pass  = scanner.nextLine().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                System.out.println("  [!] All fields are required.");
                return;
            }

            var admin = userService.registerAdmin(name, email, pass);
            System.out.println("  [✓] Admin registered! ID: " + admin.getUserId());

        } catch (DuplicateEntryException e) {
            System.out.println("  [!] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
    }

    private void removeUser() {
        System.out.println("\n  ── Remove User ──");
        System.out.print("  Enter User ID: ");
        String userId = scanner.nextLine().trim().toUpperCase();

        try {
            User user = userService.getUserById(userId);
            System.out.println("  User: " + user.getName() + " [" + user.getRole() + "]");
            System.out.print("  Confirm removal? (yes/no): ");
            if ("yes".equalsIgnoreCase(scanner.nextLine().trim())) {
                userService.removeUser(userId);
                System.out.println("  [✓] User removed.");
            } else {
                System.out.println("  [~] Cancelled.");
            }
        } catch (UserNotFoundException e) {
            System.out.println("  [!] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    // ── Display Helpers ───────────────────────────────────────────────────────

    private String getUserHeader() {
        return String.format("  | %-8s | %-20s | %-28s | %-7s |",
                "User ID", "Name", "Email", "Role");
    }

    private String divider(int len) {
        return "  " + "-".repeat(len);
    }
}
