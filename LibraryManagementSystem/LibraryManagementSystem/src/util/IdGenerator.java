package util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates unique IDs for books, users, and issue records.
 * Uses AtomicInteger for thread safety (relevant with the AutoSave background thread).
 * Demonstrates Multithreading awareness — AtomicInteger is lock-free and thread-safe.
 */
public class IdGenerator {

    // AtomicInteger ensures no two threads ever generate the same ID simultaneously
    private static final AtomicInteger bookCounter  = new AtomicInteger(1001);
    private static final AtomicInteger userCounter  = new AtomicInteger(2001);
    private static final AtomicInteger issueCounter = new AtomicInteger(3001);

    /** Generates a unique Book ID like BK1001, BK1002, ... */
    public static String generateBookId() {
        return "BK" + bookCounter.getAndIncrement();
    }

    /** Generates a unique User ID like USR2001, USR2002, ... */
    public static String generateUserId() {
        return "USR" + userCounter.getAndIncrement();
    }

    /** Generates a unique Issue ID like ISS3001, ISS3002, ... */
    public static String generateIssueId() {
        return "ISS" + issueCounter.getAndIncrement();
    }

    // These methods sync the counters after loading saved data,
    // so new IDs never clash with existing ones.

    public static void syncBookCounter(int lastUsedNumber) {
        bookCounter.updateAndGet(current -> Math.max(current, lastUsedNumber + 1));
    }

    public static void syncUserCounter(int lastUsedNumber) {
        userCounter.updateAndGet(current -> Math.max(current, lastUsedNumber + 1));
    }

    public static void syncIssueCounter(int lastUsedNumber) {
        issueCounter.updateAndGet(current -> Math.max(current, lastUsedNumber + 1));
    }
}
