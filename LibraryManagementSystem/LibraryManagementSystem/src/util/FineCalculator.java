package util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Calculates fines for overdue books.
 * Fine = overdue days × FINE_PER_DAY (flat rate).
 */
public class FineCalculator {

    /** Fine amount in Rs. per overdue day. */
    public static final double FINE_PER_DAY = 5.0;

    /**
     * Calculates the fine based on due date and actual return date.
     * If returnDate is null (book not yet returned), today's date is used.
     *
     * @param dueDate    The date by which the book should have been returned
     * @param returnDate The actual return date (or null to calculate as of today)
     * @return Fine amount in Rs. (0.0 if returned on time)
     */
    public static double calculateFine(LocalDate dueDate, LocalDate returnDate) {
        LocalDate checkDate = (returnDate != null) ? returnDate : LocalDate.now();

        // ChronoUnit.DAYS.between gives negative value if dueDate > checkDate
        long overdueDays = ChronoUnit.DAYS.between(dueDate, checkDate);

        if (overdueDays <= 0) {
            return 0.0; // No fine — returned on time
        }

        return overdueDays * FINE_PER_DAY;
    }

    /**
     * Returns how many days are remaining before the due date.
     * Returns 0 (or negative) if already overdue.
     */
    public static long daysRemaining(LocalDate dueDate) {
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }

    /**
     * Returns how many days a book is overdue (0 if not overdue yet).
     */
    public static long daysOverdue(LocalDate dueDate) {
        long days = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        return Math.max(0, days);
    }
}
