package model;

import java.io.Serializable;

/**
 * Abstract base class for all users in the system.
 * Demonstrates OOP Inheritance — Admin and Member both extend this class.
 * Demonstrates OOP Polymorphism — getRole() is overridden in subclasses.
 */
public abstract class User implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String userId;
    protected String name;
    protected String email;
    protected String password;

    public User(String userId, String name, String email, String password) {
        this.userId   = userId;
        this.name     = name;
        this.email    = email;
        this.password = password;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getUserId()  { return userId; }
    public String getName()    { return name; }
    public String getEmail()   { return email; }
    public String getPassword(){ return password; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setName(String name)       { this.name = name; }
    public void setEmail(String email)     { this.email = email; }
    public void setPassword(String pass)   { this.password = pass; }

    /**
     * Polymorphic method — each subclass returns its own role string.
     * @return "ADMIN" or "MEMBER"
     */
    public abstract String getRole();

    /** Formatted table row for display. */
    @Override
    public String toString() {
        return String.format("| %-8s | %-20s | %-28s | %-7s |",
                userId, name, email, getRole());
    }
}
