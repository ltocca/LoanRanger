package dev.ltocca.loanranger.Util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class DateRangeParser {

    private DateRangeParser() {}

    public record DateRange(LocalDateTime start, LocalDateTime end) {}

    public static DateRange parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Date string is null or empty");
        }

        String text = input.trim().toLowerCase();
        LocalDateTime now = LocalDateTime.now();

        // natural language shortcuts
        switch (text) {
            case "today":
                LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
                return new DateRange(todayStart, todayStart.plusDays(1));
            case "tomorrow":
                LocalDateTime tomorrowStart = now.toLocalDate().plusDays(1).atStartOfDay();
                return new DateRange(tomorrowStart, tomorrowStart.plusDays(1));
            case "this week":
                LocalDateTime weekStart = now.toLocalDate().with(DayOfWeek.MONDAY).atStartOfDay();
                return new DateRange(weekStart, weekStart.plusWeeks(1));
            case "this month":
                LocalDateTime monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
                return new DateRange(monthStart, monthStart.plusMonths(1));
        }

        // try ISO date-time
        try {
            LocalDateTime dt = LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return new DateRange(dt, dt.plusDays(1));
        } catch (DateTimeParseException ignored) {}

        // try ISO date yyyy-MM-dd
        try {
            LocalDate date = LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
            return new DateRange(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        } catch (DateTimeParseException ignored) {}

        // partial date dd MMMM (e.g., "16 September") current year
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH);
            LocalDate date = LocalDate.parse(text, formatter).withYear(now.getYear());
            return new DateRange(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        } catch (DateTimeParseException ignored) {}

        // partial month MMMM yyyy (e.g., "September 2025")
        try {
            LocalDate date = LocalDate.parse("01 " + text, DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH));
            return new DateRange(date.atStartOfDay(), date.plusMonths(1).withDayOfMonth(1).atStartOfDay());
        } catch (DateTimeParseException ignored) {}

        throw new IllegalArgumentException("Unsupported date format: " + input);
    }
}
