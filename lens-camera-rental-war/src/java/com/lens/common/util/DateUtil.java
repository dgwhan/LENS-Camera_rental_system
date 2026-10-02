package com.lens.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Duong Ngoc Han
 */
public final class DateUtil {

    public static final String DEFAULT_INPUT_PATTERN = "yyyy-MM-dd";
    public static final String DEFAULT_DISPLAY_PATTERN = "dd/MM/yyyy";

    private DateUtil() {
    }

    /**
     * Formats a date string from 'yyyy-MM-dd' to display format 'dd/MM/yyyy'.
     *
     * @param dateStr the input date string in 'yyyy-MM-dd' format
     * @return formatted date string in 'dd/MM/yyyy', or empty string if null/empty
     */
    public static String formatDisplayDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat inFmt = new SimpleDateFormat(DEFAULT_INPUT_PATTERN);
            SimpleDateFormat outFmt = new SimpleDateFormat(DEFAULT_DISPLAY_PATTERN);
            return outFmt.format(inFmt.parse(dateStr.trim()));
        } catch (Exception e) {
            return dateStr;
        }
    }

    /**
     * Formats a Date object to display format 'dd/MM/yyyy'.
     *
     * @param date the Date object
     * @return formatted date string, or empty string if date is null
     */
    public static String formatDisplayDate(Date date) {
        if (date == null) {
            return "";
        }
        try {
            SimpleDateFormat outFmt = new SimpleDateFormat(DEFAULT_DISPLAY_PATTERN);
            return outFmt.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Formats a Date object with a specified pattern.
     *
     * @param date the Date object
     * @param pattern the date pattern
     * @return formatted date string, or empty string if date is null
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null || pattern == null || pattern.trim().isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat fmt = new SimpleDateFormat(pattern);
            return fmt.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Parses a date string using 'dd/MM/yyyy' or 'yyyy-MM-dd' format.
     *
     * @param dateStr the date string
     * @return Date object
     * @throws ParseException if parsing fails
     */
    public static Date parseDate(String dateStr) throws ParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        String trimmed = dateStr.trim();
        if (trimmed.contains("/")) {
            SimpleDateFormat formatter = new SimpleDateFormat(DEFAULT_DISPLAY_PATTERN);
            formatter.setLenient(false);
            return formatter.parse(trimmed);
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DEFAULT_INPUT_PATTERN);
        formatter.setLenient(false);
        return formatter.parse(trimmed);
    }

    /**
     * Calculates the number of whole days between two Date objects.
     *
     * @param start start date
     * @param end end date
     * @return number of whole days, or 0 if invalid / negative
     */
    public static long calculateDaysBetween(Date start, Date end) {
        if (start == null || end == null) {
            return 0;
        }
        long diff = end.getTime() - start.getTime();
        long days = diff / (1000L * 60 * 60 * 24);
        return days > 0 ? days : 0;
    }

    /**
     * Calculates the rental duration in days between start and end dates.
     * Guaranteed to return at least 1 day for valid rental periods.
     *
     * @param start rental start date
     * @param end rental end date
     * @return duration in days (minimum 1)
     */
    public static int calculateRentalDuration(Date start, Date end) {
        if (start == null || end == null || !end.after(start)) {
            return 0;
        }
        long diffInMillis = end.getTime() - start.getTime();
        int duration = (int) Math.ceil((double) diffInMillis / (1000L * 60 * 60 * 24));
        return duration > 0 ? duration : 1;
    }

    /**
     * Validates that a rental period complies with business rules:
     * <ul>
     *   <li>Both start and end dates must be non-null.</li>
     *   <li>Rental start date must be at least one day after booking date (tomorrow or later).</li>
     *   <li>Rental end date must be strictly after rental start date.</li>
     * </ul>
     *
     * @param startDate rental start date
     * @param endDate rental end date
     * @throws IllegalArgumentException on any rule violation
     */
    public static void validateRentalPeriod(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Please select both start date and end date.");
        }

        java.util.Calendar minStartCal = java.util.Calendar.getInstance();
        minStartCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        minStartCal.set(java.util.Calendar.MINUTE, 0);
        minStartCal.set(java.util.Calendar.SECOND, 0);
        minStartCal.set(java.util.Calendar.MILLISECOND, 0);
        minStartCal.add(java.util.Calendar.DAY_OF_MONTH, 1);

        java.util.Calendar startCal = java.util.Calendar.getInstance();
        startCal.setTime(startDate);
        startCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        startCal.set(java.util.Calendar.MINUTE, 0);
        startCal.set(java.util.Calendar.SECOND, 0);
        startCal.set(java.util.Calendar.MILLISECOND, 0);

        if (startCal.getTime().before(minStartCal.getTime())) {
            throw new IllegalArgumentException(
                    "Rental orders must be placed at least one day before the rental start date.");
        }

        if (!endDate.after(startDate)) {
            throw new IllegalArgumentException("Rental end date must be after start date.");
        }
    }

    /**
     * Calculates the number of whole days between two date strings (yyyy-MM-dd).
     *
     * @param startDateStr start date in 'yyyy-MM-dd'
     * @param endDateStr end date in 'yyyy-MM-dd'
     * @return number of days, or 0 if invalid / negative
     */
    public static long calculateDaysBetween(String startDateStr, String endDateStr) {
        if (startDateStr == null || endDateStr == null || startDateStr.trim().isEmpty() || endDateStr.trim().isEmpty()) {
            return 0;
        }
        try {
            Date start = parseDate(startDateStr);
            Date end = parseDate(endDateStr);
            return calculateDaysBetween(start, end);
        } catch (Exception e) {
            return 0;
        }
    }
}
