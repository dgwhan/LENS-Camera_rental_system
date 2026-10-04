package com.lens.dashboard;

import com.lens.common.util.FormatUtil;
import com.lens.device.facade.DevicesFacadeLocal;
import com.lens.rental_orders.facade.RentalOrdersFacadeLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * controller for admin dashboard overview kpis and chart data.
 *
 * @author Duong Ngoc Han
 */
@Named(value = "adminDashboardController")
@RequestScoped
public class AdminDashboardController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String CHANGE_NEUTRAL = "No comparison";
    private static final String[] STATUS_ORDER = {
        "PENDING", "APPROVED", "ACTIVE", "COMPLETED", "REJECTED", "CANCELLED"
    };
    private static final Map<String, String> STATUS_LABELS = new LinkedHashMap<>();

    static {
        STATUS_LABELS.put("PENDING", "Pending");
        STATUS_LABELS.put("APPROVED", "Approved");
        STATUS_LABELS.put("ACTIVE", "Active");
        STATUS_LABELS.put("COMPLETED", "Completed");
        STATUS_LABELS.put("REJECTED", "Rejected");
        STATUS_LABELS.put("CANCELLED", "Cancelled");
    }

    @EJB
    private RentalOrdersFacadeLocal rentalOrdersFacade;

    @EJB
    private DevicesFacadeLocal devicesFacade;

    private int totalOrders;
    private int pendingOrders;
    private int activeRentals;
    private int availableDevices;

    private String totalOrdersChange = CHANGE_NEUTRAL;
    private String pendingOrdersChange = CHANGE_NEUTRAL;
    private String activeRentalsChange = CHANGE_NEUTRAL;
    private String availableDevicesChange = CHANGE_NEUTRAL;

    private String totalOrdersChangeClass = "";
    private String pendingOrdersChangeClass = "";
    private String activeRentalsChangeClass = "";
    private String availableDevicesChangeClass = "";

    private String statusChartJson = "{\"labels\":[],\"values\":[]}";
    private String activityChartJson = "{\"labels\":[],\"values\":[]}";

    @PostConstruct
    public void init() {
        loadKpis();
        loadCharts();
    }

    private void loadKpis() {
        totalOrders = rentalOrdersFacade.totalRentalOrders();
        pendingOrders = rentalOrdersFacade.countByStatus("PENDING");
        activeRentals = rentalOrdersFacade.countByStatus("ACTIVE");
        availableDevices = devicesFacade.totalDevicesAvailable();

        LocalDate today = LocalDate.now();
        Date todayStart = toDate(today);
        Date tomorrowStart = toDate(today.plusDays(1));
        Date yesterdayStart = toDate(today.minusDays(1));

        int ordersToday = rentalOrdersFacade.countCreatedBetween(todayStart, tomorrowStart);
        int ordersYesterday = rentalOrdersFacade.countCreatedBetween(yesterdayStart, todayStart);
        applyChange("total", ordersToday, ordersYesterday);

        int pendingToday = rentalOrdersFacade.countCreatedBetweenByStatus("PENDING", todayStart, tomorrowStart);
        int pendingYesterday = rentalOrdersFacade.countCreatedBetweenByStatus("PENDING", yesterdayStart, todayStart);
        applyChange("pending", pendingToday, pendingYesterday);
    }

    private void loadCharts() {
        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        for (int i = 0; i < STATUS_ORDER.length; i++) {
            statusCounts.put(STATUS_ORDER[i], 0);
        }

        List<Object[]> grouped = rentalOrdersFacade.countGroupByStatus();
        if (grouped != null) {
            for (int i = 0; i < grouped.size(); i++) {
                Object[] row = grouped.get(i);
                if (row == null || row.length < 2 || row[0] == null) {
                    continue;
                }
                String status = String.valueOf(row[0]);
                int count = row[1] instanceof Number ? ((Number) row[1]).intValue() : 0;
                if (!statusCounts.containsKey(status)) {
                    statusCounts.put(status, 0);
                }
                statusCounts.put(status, count);
            }
        }

        List<String> statusLabels = new ArrayList<>();
        List<Integer> statusValues = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            String label = STATUS_LABELS.containsKey(entry.getKey())
                    ? STATUS_LABELS.get(entry.getKey())
                    : entry.getKey();
            statusLabels.add(label);
            statusValues.add(entry.getValue());
        }
        statusChartJson = toChartJson(statusLabels, statusValues);

        List<String> activityLabels = new ArrayList<>();
        List<Integer> activityValues = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        LocalDate today = LocalDate.now();
        for (int offset = 6; offset >= 0; offset--) {
            LocalDate day = today.minusDays(offset);
            Date start = toDate(day);
            Date end = toDate(day.plusDays(1));
            activityLabels.add(day.format(formatter));
            activityValues.add(rentalOrdersFacade.countCreatedBetween(start, end));
        }
        activityChartJson = toChartJson(activityLabels, activityValues);
    }

    private void applyChange(String kpi, int todayCount, int yesterdayCount) {
        String text = CHANGE_NEUTRAL;
        String cssClass = "";

        if (yesterdayCount > 0) {
            int percent = Math.round(((float) (todayCount - yesterdayCount) * 100f) / yesterdayCount);
            if (percent > 0) {
                text = "+" + percent + "% vs yesterday";
                cssClass = "positive";
            } else if (percent < 0) {
                text = percent + "% vs yesterday";
                cssClass = "negative";
            } else {
                text = "0% vs yesterday";
            }
        }

        if ("total".equals(kpi)) {
            totalOrdersChange = text;
            totalOrdersChangeClass = cssClass;
        } else if ("pending".equals(kpi)) {
            pendingOrdersChange = text;
            pendingOrdersChangeClass = cssClass;
        }
    }

    private Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private String toChartJson(List<String> labels, List<Integer> values) {
        return "{\"labels\":" + toJsonStringArray(labels) + ",\"values\":" + toJsonNumberArray(values) + "}";
    }

    private String toJsonStringArray(List<String> items) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            String value = items.get(i) == null ? "" : items.get(i);
            json.append('"')
                    .append(value.replace("\\", "\\\\").replace("\"", "\\\""))
                    .append('"');
        }
        json.append(']');
        return json.toString();
    }

    private String toJsonNumberArray(List<Integer> items) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(items.get(i) == null ? 0 : items.get(i).intValue());
        }
        json.append(']');
        return json.toString();
    }

    public String getTotalOrders() {
        return FormatUtil.formatNumber(totalOrders);
    }

    public String getPendingOrders() {
        return FormatUtil.formatNumber(pendingOrders);
    }

    public String getActiveRentals() {
        return FormatUtil.formatNumber(activeRentals);
    }

    public String getAvailableDevices() {
        return FormatUtil.formatNumber(availableDevices);
    }

    public String getTotalOrdersChange() {
        return totalOrdersChange;
    }

    public String getPendingOrdersChange() {
        return pendingOrdersChange;
    }

    public String getActiveRentalsChange() {
        return activeRentalsChange;
    }

    public String getAvailableDevicesChange() {
        return availableDevicesChange;
    }

    public String getTotalOrdersChangeClass() {
        return totalOrdersChangeClass;
    }

    public String getPendingOrdersChangeClass() {
        return pendingOrdersChangeClass;
    }

    public String getActiveRentalsChangeClass() {
        return activeRentalsChangeClass;
    }

    public String getAvailableDevicesChangeClass() {
        return availableDevicesChangeClass;
    }

    public String getStatusChartJson() {
        return statusChartJson;
    }

    public String getActivityChartJson() {
        return activityChartJson;
    }
}
