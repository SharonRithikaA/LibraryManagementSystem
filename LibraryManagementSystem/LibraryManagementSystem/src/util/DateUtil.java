package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for consistent date formatting throughout the application.
 */
public class DateUtil {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /** Formats a LocalDate to a readable string like "15-04-2025". */
    public static String format(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DISPLAY_FORMAT);
    }

    /** Returns today's date as a formatted string. */
    public static String today() {
        return format(LocalDate.now());
    }
}
