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
            if (start == null || end == null) {
                return 0;
            }
            long diff = end.getTime() - start.getTime();
            long days = diff / (1000L * 60 * 60 * 24);
            return days > 0 ? days : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
