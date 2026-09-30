package com.lens.device_model;

import com.lens.availability.dto.AvailabilityResult;
import com.lens.availability.service.AvailabilityServiceLocal;
import com.lens.cart.model.CartService;
import com.lens.common.util.DateUtil;
import com.lens.common.util.FacesUtil;
import com.lens.common.util.FormatUtil;
import com.lens.device_model.entity.DeviceModels;
import com.lens.device_model.facade.DeviceModelsFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Manages the device detail page and rental availability operations.
 *
 * @author Duong Ngoc Han
 */
@Named(value = "deviceDetailController")
@ViewScoped
public class DeviceDetailController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    @EJB
    private AvailabilityServiceLocal availabilityService;

    @Inject
    private CartService cartService;

    @Inject
    private HttpServletRequest request;

    private DeviceModels deviceModel;
    private Integer id;
    private String startDate;
    private String endDate;
    private AvailabilityResult availabilityResult;

    public DeviceDetailController() {
    }

    /**
     * Loads the selected device model.
     */
    public void loadDeviceDetail() {
        if (id == null || id <= 0) {
            FacesUtil.redirectTo404("Device not found");
            return;
        }

        deviceModel = deviceModelsFacade.find(id);

        if (deviceModel == null) {
            FacesUtil.redirectTo404("Device not found");
        }
    }

    /**
     * Checks whether the selected rental period is available.
     */
    public void checkAvailability() {
        if (id == null || id <= 0) {
            return;
        }

        if (deviceModel == null) {
            deviceModel = deviceModelsFacade.find(id);
        }

        if (startDate == null || startDate.trim().isEmpty() || endDate == null || endDate.trim().isEmpty()) {
            FacesContext context = FacesContext.getCurrentInstance();

            if (context != null) {
                if (startDate == null || startDate.trim().isEmpty()) {
                    startDate = context.getExternalContext().getRequestParameterMap().get("startDate");

                    if (startDate == null || startDate.trim().isEmpty()) {
                        startDate = context.getExternalContext().getRequestParameterMap().get("rentalStartDate");
                    }
                }

                if (endDate == null || endDate.trim().isEmpty()) {
                    endDate = context.getExternalContext().getRequestParameterMap().get("endDate");

                    if (endDate == null || endDate.trim().isEmpty()) {
                        endDate = context.getExternalContext().getRequestParameterMap().get("rentalEndDate");
                    }
                }
            }
        }

        if (startDate == null || startDate.trim().isEmpty() || endDate == null || endDate.trim().isEmpty()) {
            availabilityResult = new AvailabilityResult(false, 0, 0, 0, "Please select both start date and end date.");
            return;
        }

        try {
            // convert string from form to date
            Date requestedStartDate = DateUtil.parseDate(startDate);
            Date requestedEndDate = DateUtil.parseDate(endDate);

            // check rental period availability
            availabilityResult = availabilityService.checkAvailability(id, requestedStartDate, requestedEndDate);
        } catch (ParseException ex) {
            // handle invalid date format
            availabilityResult = new AvailabilityResult(false, 0, 0, 0, "Please select a valid rental period.");
        }
    }

    /**
     * Adds the selected rental item to the customer's cart.
     *
     * @return login page if the user is not authenticated; otherwise stays on
     *         the current page
     */
    public String addToCart() {
        if (request.getUserPrincipal() == null) {
            return "/client/pages/login?faces-redirect=true";
        }

        if (deviceModel == null) {
            FacesUtil.addErrorMessage("Device not found.");
            return null;
        }

        if (startDate == null || startDate.trim().isEmpty() || endDate == null || endDate.trim().isEmpty()) {
            FacesUtil.addErrorMessage("Please select both start date and end date.");
            return null;
        }

        try {
            Date requestedStartDate = DateUtil.parseDate(startDate);
            Date requestedEndDate = DateUtil.parseDate(endDate);

            boolean added = cartService.addToCart(deviceModel, requestedStartDate, requestedEndDate);

            if (!added) {
                FacesUtil.addErrorMessage("This device is already in your cart for the selected rental period.");
                return null;
            }

            FacesUtil.addSuccessMessage("Added to cart successfully!");
        } catch (ParseException ex) {
            FacesUtil.addErrorMessage("Please select a valid rental period.");
        }

        return null;
    }

    /**
     * Navigates directly to the checkout page for the selected device model without
     * adding to cart.
     *
     * @return checkout navigation outcome or login if unauthenticated
     */
    public String rentNow() {
        if (request.getUserPrincipal() == null) {
            return "/client/pages/login?faces-redirect=true";
        }

        if (deviceModel == null) {
            FacesUtil.addErrorMessage("Device not found.");
            return null;
        }

        if (startDate == null || startDate.trim().isEmpty() || endDate == null || endDate.trim().isEmpty()) {
            FacesUtil.addErrorMessage("Please select both start date and end date.");
            return null;
        }

        try {
            Date requestedStartDate = DateUtil.parseDate(startDate);
            Date requestedEndDate = DateUtil.parseDate(endDate);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date today = calendar.getTime();

            if (requestedStartDate.before(today)) {
                FacesUtil.addErrorMessage("Rental start date cannot be in the past.");
                return null;
            }

            if (!requestedEndDate.after(requestedStartDate)) {
                FacesUtil.addErrorMessage("Rental end date must be after start date.");
                return null;
            }

            if (availabilityService != null) {
                com.lens.availability.dto.AvailabilityResult availability = availabilityService
                        .checkAvailability(deviceModel.getId(), requestedStartDate, requestedEndDate);
                if (!availability.isAvailable()) {
                    FacesUtil.addErrorMessage("This device is not available for the selected dates.");
                    return null;
                }
            }

            String startFormatted = DateUtil.formatDate(requestedStartDate, DateUtil.DEFAULT_INPUT_PATTERN);
            String endFormatted = DateUtil.formatDate(requestedEndDate, DateUtil.DEFAULT_INPUT_PATTERN);

            return "/client/pages/checkout?faces-redirect=true&type=direct&modelId="
                    + deviceModel.getId()
                    + "&startDate=" + startFormatted
                    + "&endDate=" + endFormatted;
        } catch (ParseException ex) {
            FacesUtil.addErrorMessage("Please select a valid rental period.");
            return null;
        }
    }

    /**
     * Checks whether the device model currently has available physical devices.
     *
     * @return true if the device model is available
     */
    public boolean isProductAvailable() {
        if (id == null || availabilityService == null) {
            return false;
        }
        return availabilityService.isProductAvailable(id);
    }

    /**
     * Returns the current availability status of the device model.
     *
     * @return availability status
     */
    public String getProductAvailabilityStatus() {
        if (id == null || availabilityService == null) {
            return "OUT OF STOCK";
        }
        return availabilityService.getProductAvailabilityStatus(id);
    }

    /**
     * Returns whether the device model is available.
     *
     * @return true if the device model is available
     */
    public boolean isAvailable() {
        return isProductAvailable();
    }

    /**
     * Returns the availability status for display.
     *
     * @return availability status
     */
    public String getAvailabilityStatus() {
        return getProductAvailabilityStatus();
    }

    /**
     * Returns the store contact phone number.
     *
     * @return store phone number
     */
    public String getStorePhone() {
        return "+84 xxx xxx xxx";
    }

    /**
     * Formats a price value for display.
     *
     * @param price price value
     * @return formatted price
     */
    public String formatPrice(long price) {
        return FormatUtil.formatPrice(price);
    }

    /**
     * Formats a number for display.
     *
     * @param number number value
     * @return formatted number
     */
    public String formatNumber(long number) {
        return FormatUtil.formatNumber(number);
    }

    /**
     * Returns the device image URL.
     *
     * @param imageName image file name
     * @return device image URL
     */
    public String getImageUrl(String imageName) {
        return com.lens.common.util.ImageUtil.getDeviceImageUrl(imageName);
    }

    /**
     * Calculates the rental duration between the selected dates.
     *
     * @return rental duration in days
     */
    public long getRentalDays() {
        return DateUtil.calculateDaysBetween(startDate, endDate);
    }

    /**
     * Calculates the rental fee based on the selected rental period.
     *
     * @return calculated rental fee
     */
    public long getCalculatedRentalFee() {
        if (deviceModel == null) {
            return 0;
        }
        return getRentalDays() * deviceModel.getRentalPrice();
    }

    /**
     * Returns the formatted rental fee for display.
     *
     * @return formatted rental fee
     */
    public String getFormattedRentalFee() {
        return formatPrice(getCalculatedRentalFee());
    }

    /**
     * Calculates the estimated total amount including the deposit.
     *
     * @return estimated total amount
     */
    public long getEstimatedTotalAmount() {
        if (deviceModel == null) {
            return 0;
        }
        return getCalculatedRentalFee() + deviceModel.getDepositAmount();
    }

    /**
     * Returns the formatted estimated total amount for display.
     *
     * @return formatted estimated total amount
     */
    public String getFormattedEstimatedTotal() {
        return formatPrice(getEstimatedTotalAmount());
    }

    /**
     * Returns the formatted rental start date for display.
     *
     * @return formatted start date
     */
    public String getDisplayStartDate() {
        return DateUtil.formatDisplayDate(startDate);
    }

    /**
     * Returns the formatted rental end date for display.
     *
     * @return formatted end date
     */
    public String getDisplayEndDate() {
        return DateUtil.formatDisplayDate(endDate);
    }

    /**
     * Formats a date string for display.
     *
     * @param dateStr date string to format
     * @return formatted date
     */
    public String formatDisplayDate(String dateStr) {
        return DateUtil.formatDisplayDate(dateStr);
    }

    public DeviceModels getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(DeviceModels deviceModel) {
        this.deviceModel = deviceModel;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public AvailabilityResult getAvailabilityResult() {
        return availabilityResult;
    }

}
