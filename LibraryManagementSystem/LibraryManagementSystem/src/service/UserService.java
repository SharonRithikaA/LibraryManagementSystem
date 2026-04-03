package service;

import exception.DuplicateEntryException;
import exception.UserNotFoundException;
import model.Admin;
import model.Member;
import model.User;
import util.FileHandler;
import util.IdGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Service layer for all user management operations.
 * Uses TWO HashMaps for fast lookups — one by ID, one by email.
 * Demonstrates COLLECTIONS FRAMEWORK and EXCEPTION HANDLING.
 */
public class UserService {

    private HashMap<String, User>   userById;    // userId  → User
    private HashMap<String, String> emailToId;   // email   → userId  (for login lookup)

    private static final String USERS_FILE = "users.dat";

    @SuppressWarnings("unchecked")
    public UserService() {
        userById    = new HashMap<>();
        emailToId   = new HashMap<>();
        loadUsers();
        ensureDefaultAdminExists();
    }

    // ── Authentication ────────────────────────────────────────────────────────

    /**
     * Validates credentials and returns the matching User.
     * @throws UserNotFoundException if email not found or password is wrong
     */
    public User login(String email, String password) throws UserNotFoundException {
        String userId = emailToId.get(email.toLowerCase());
        if (userId == null) {
            throw new UserNotFoundException("No account registered with: " + email);
        }
        User user = userById.get(userId);
        if (!user.getPassword().equals(password)) {
            throw new UserNotFoundException("Incorrect password. Please try again.");
        }
        return user;
    }

    // ── Registration ──────────────────────────────────────────────────────────

    /**
     * Registers a new regular member.
     * @throws DuplicateEntryException if the email is already in use
     */
    public Member registerMember(String name, String email, String password)
            throws DuplicateEntryException, IOException {
        validateEmailUnique(email);
        String userId = IdGenerator.generateUserId();
        Member member = new Member(userId, name, email.toLowerCase(), password);
        persist(member);
        return member;
    }

    /**
     * Registers a new admin. Can only be called by an existing admin.
     * @throws DuplicateEntryException if the email is already in use
     */
    public Admin registerAdmin(String name, String email, String password)
            throws DuplicateEntryException, IOException {
        validateEmailUnique(email);
        String userId = IdGenerator.generateUserId();
        Admin admin   = new Admin(userId, name, email.toLowerCase(), password);
        persist(admin);
        return admin;
    }

    // ── Lookup ────────────────────────────────────────────────────────────────

    public User getUserById(String userId) throws UserNotFoundException {
        User user = userById.get(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        return user;
    }

    /** Convenience method — also validates that the user is a Member, not an Admin. */
    public Member getMemberById(String userId) throws UserNotFoundException {
        User user = getUserById(userId);
        if (!(user instanceof Member)) {
            throw new UserNotFoundException("User [" + userId + "] is not a member.");
        }
        return (Member) user;
    }

    // ── Management ────────────────────────────────────────────────────────────

    public void removeUser(String userId) throws UserNotFoundException, IOException {
        User user = getUserById(userId);
        emailToId.remove(user.getEmail());
        userById.remove(userId);
        saveUsers();
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(userById.values());
    }

    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        for (User u : userById.values()) {
            if (u instanceof Member) members.add((Member) u);
        }
        return members;
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public void saveUsers() throws IOException {
        // Bundle both maps together into one object for a single file save
        HashMap<String, Object> bundle = new HashMap<>();
        bundle.put("userById",  userById);
        bundle.put("emailToId", emailToId);
        FileHandler.saveObject(bundle, USERS_FILE);
    }

    @SuppressWarnings("unchecked")
    private void loadUsers() {
        try {
            Object data = FileHandler.loadObject(USERS_FILE);
            if (data != null) {
                HashMap<String, Object> bundle = (HashMap<String, Object>) data;
                userById  = (HashMap<String, User>)   bundle.get("userById");
                emailToId = (HashMap<String, String>) bundle.get("emailToId");
                // Sync ID counter
                userById.keySet().forEach(id -> {
                    try {
                        int num = Integer.parseInt(id.replace("USR", ""));
                        IdGenerator.syncUserCounter(num);
                    } catch (NumberFormatException ignored) { }
                });
                System.out.println("[Info] Loaded " + userById.size() + " user(s) from disk.");
            }
        } catch (Exception e) {
            System.err.println("[Warning] Could not load user data: " + e.getMessage());
            userById  = new HashMap<>();
            emailToId = new HashMap<>();
        }
    }

    // ── Internal Helpers ──────────────────────────────────────────────────────

    /** Saves a user object and updates both maps. */
    private void persist(User user) throws IOException {
        userById.put(user.getUserId(), user);
        emailToId.put(user.getEmail(), user.getUserId());
        saveUsers();
    }

    private void validateEmailUnique(String email) throws DuplicateEntryException {
        if (emailToId.containsKey(email.toLowerCase())) {
            throw new DuplicateEntryException("Email [" + email + "] is already registered.");
        }
    }

    /** Creates the default admin on the very first run if none exists. */
    private void ensureDefaultAdminExists() {
        boolean hasAdmin = userById.values().stream()
                .anyMatch(u -> "ADMIN".equals(u.getRole()));
        if (!hasAdmin) {
            try {
                registerAdmin("System Admin", "admin@library.com", "admin123");
                System.out.println("[Info] Default admin created: admin@library.com / admin123");
            } catch (Exception e) {
                System.err.println("[Warning] Could not create default admin.");
            }
        }
    }
}
