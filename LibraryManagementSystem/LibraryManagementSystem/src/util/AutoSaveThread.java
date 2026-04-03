package util;

import service.BookService;
import service.IssueService;
import service.UserService;

/**
 * A background daemon thread that periodically auto-saves all data to disk.
 *
 * Demonstrates MULTITHREADING:
 *  - Extends Thread (one of two ways to create threads in Java)
 *  - Uses sleep() and volatile for safe inter-thread communication
 *  - Set as a daemon thread — it automatically stops when the JVM exits
 */
public class AutoSaveThread extends Thread {

    private static final long INTERVAL_MS = 120_000; // Auto-save every 2 minutes

    private final BookService  bookService;
    private final UserService  userService;
    private final IssueService issueService;

    // volatile ensures visibility: when main thread sets this to false,
    // the background thread sees the update immediately without caching.
    private volatile boolean running = true;

    public AutoSaveThread(BookService bookService, UserService userService, IssueService issueService) {
        this.bookService  = bookService;
        this.userService  = userService;
        this.issueService = issueService;
        setName("AutoSave-Thread");
        setDaemon(true); // Dies automatically when the main program exits
    }

    @Override
    public void run() {
        System.out.println("[AutoSave] Background save thread started (every 2 minutes).");
        while (running) {
            try {
                Thread.sleep(INTERVAL_MS); // Pause for 2 minutes
                performSave();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt flag
                break; // Exit cleanly
            }
        }
        System.out.println("[AutoSave] Thread stopped.");
    }

    /** Called by the main thread to gracefully shut down. */
    public void stopAutoSave() {
        running = false;
        interrupt(); // Wake up from sleep immediately
    }

    /** Saves all services — wrapped in individual try-catch so one failure doesn't block others. */
    private synchronized void performSave() {
        try { bookService.saveBooks();        } catch (Exception e) { logError("books", e); }
        try { userService.saveUsers();        } catch (Exception e) { logError("users", e); }
        try { issueService.saveIssuedBooks(); } catch (Exception e) { logError("issues", e); }
        System.out.println("[AutoSave] Data saved at " + DateUtil.today());
    }

    private void logError(String what, Exception e) {
        System.err.println("[AutoSave] Failed to save " + what + ": " + e.getMessage());
    }
}
