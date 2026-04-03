package model;

/**
 * Represents an Admin user with full system privileges.
 * Extends User — demonstrates Inheritance.
 */
public class Admin extends User {

    private static final long serialVersionUID = 1L;

    public Admin(String userId, String name, String email, String password) {
        super(userId, name, email, password); // Call parent constructor
    }

    /**
     * Overrides the abstract method from User.
     * Demonstrates Polymorphism — same method, different behaviour.
     */
    @Override
    public String getRole() {
        return "ADMIN";
    }
}
