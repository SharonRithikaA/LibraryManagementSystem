# 📚 Library Management System — Java Mini Project

A fully-featured, menu-driven CLI Library Management System built in **pure Java**.
No frameworks. No database. Runs everywhere Java runs.

---

## 🗂️ Project Structure

```
LibraryManagementSystem/
├── src/
│   ├── LibraryApp.java               ← Main entry point
│   │
│   ├── model/                        ← Data entities (OOP)
│   │   ├── Book.java                 ← Book entity (Encapsulation)
│   │   ├── User.java                 ← Abstract base user (Inheritance)
│   │   ├── Admin.java                ← Extends User (Polymorphism)
│   │   ├── Member.java               ← Extends User (uses ArrayList)
│   │   └── IssuedBook.java           ← Issue/return transaction record
│   │
│   ├── service/                      ← Business logic layer
│   │   ├── BookService.java          ← CRUD with HashMap
│   │   ├── UserService.java          ← Auth + dual-HashMap lookup
│   │   └── IssueService.java         ← Issue/return + fine logic
│   │
│   ├── controller/                   ← CLI interaction layer
│   │   ├── BookController.java       ← Menus for book operations
│   │   ├── UserController.java       ← Menus for user management
│   │   └── IssueController.java      ← Menus for issue/return
│   │
│   ├── util/                         ← Helper utilities
│   │   ├── FileHandler.java          ← File I/O (Serialization)
│   │   ├── IdGenerator.java          ← Thread-safe ID generation
│   │   ├── FineCalculator.java       ← Overdue fine logic
│   │   ├── DateUtil.java             ← Date formatting
│   │   └── AutoSaveThread.java       ← Background daemon thread
│   │
│   └── exception/                    ← Custom exceptions
│       ├── BookNotFoundException.java
│       ├── BookNotAvailableException.java
│       ├── UserNotFoundException.java
│       └── DuplicateEntryException.java
│
├── data/                             ← Auto-created; stores .dat files
│   ├── books.dat
│   ├── users.dat
│   └── issued.dat
│
└── README.md
```

---

## ✅ Java Features Demonstrated

| Feature | Where Used |
|---|---|
| **OOP – Encapsulation** | All fields private in `Book`, `User`, `IssuedBook` |
| **OOP – Inheritance** | `Admin` and `Member` both extend abstract `User` |
| **OOP – Polymorphism** | `getRole()` overridden in Admin/Member; dashboard routing uses it |
| **Collections – HashMap** | `BookService`, `UserService`, `IssueService` all use `HashMap` |
| **Collections – ArrayList** | `Member` tracks active loans in an `ArrayList` |
| **Exception Handling** | 4 custom exceptions; try-catch throughout all service layers |
| **File Handling** | `FileHandler` uses `ObjectOutputStream` / `ObjectInputStream` |
| **Multithreading** | `AutoSaveThread` extends `Thread`; uses `volatile`, `AtomicInteger` |

---

## 📋 Feature Walkthrough

### Admin can:
- Add / remove / update books
- View all books, available books, search by title/author/ID
- Register new admin accounts
- View and remove users/members
- Issue books to any member
- Process book returns
- View active issues, overdue books, full history
- View issues by a specific member

### Member can:
- Register a new account (from main menu)
- Browse and search all books
- Borrow a book (max 3 at a time)
- Return a book (fine shown if overdue)
- View their current loans and history
- View their own profile

### Fine System:
- **Loan period**: 14 days
- **Fine rate**: Rs. 5.00 per overdue day
- Fine is calculated and displayed on return

---



