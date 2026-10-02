package com.lens.device_model;

import com.lens.availability.dto.AvailabilityResult;
import com.lens.availability.service.AvailabilityServiceLocal;
import com.lens.common.util.DateUtil;
import com.lens.common.util.FacesUtil;
import com.lens.common.util.FormatUtil;
import com.lens.common.util.ImageUtil;
import com.lens.device_model.entity.DeviceModels;
import com.lens.device_model.facade.DeviceModelsFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * Manages presentation state for the device detail page.
 *
 * @author Duong Ngoc Han
 */
@Named(value = "deviceDetailController")
@ViewScoped
public class DeviceDetailController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_STORE_PHONE = "+84 xxx xxx xxx";

    @EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    @EJB
    private AvailabilityServiceLocal availabilityService;

    private DeviceModels deviceModel;
    private Integer id;
    private String startDate;
    private String endDate;
    private AvailabilityResult availabilityResult;

    public DeviceDetailController() {
    }

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

    public void checkAvailability() {
        if (id == null || id <= 0) {
            return;
        }

        if (deviceModel == null) {
            deviceModel = deviceModelsFacade.find(id);
        }

        resolveDatesFromRequestIfNeeded();

        if (isBlank(startDate) || isBlank(endDate)) {
            availabilityResult = new AvailabilityResult(false, 0, 0, 0, "Please select both start date and end date.");
            return;
        }

        try {
            Date requestedStartDate = DateUtil.parseDate(startDate);
            Date requestedEndDate = DateUtil.parseDate(endDate);

            DateUtil.validateRentalPeriod(requestedStartDate, requestedEndDate);

            availabilityResult = availabilityService.checkAvailability(id, requestedStartDate, requestedEndDate);
        } catch (ParseException ex) {
            availabilityResult = new AvailabilityResult(false, 0, 0, 0, "Please select a valid rental period.");
        } catch (IllegalArgumentException ex) {
            availabilityResult = new AvailabilityResult(false, 0, 0, 0, ex.getMessage());
        }
    }

    private void resolveDatesFromRequestIfNeeded() {
        if (isBlank(startDate) || isBlank(endDate)) {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null && context.getExternalContext() != null) {
                Map<String, String> params = context.getExternalContext().getRequestParameterMap();
                if (isBlank(startDate)) {
                    startDate = params.get("startDate");
                    if (isBlank(startDate)) {
                        startDate = params.get("rentalStartDate");
                    }
                }
                if (isBlank(endDate)) {
                    endDate = params.get("endDate");
                    if (isBlank(endDate)) {
                        endDate = params.get("rentalEndDate");
                    }
                }
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public boolean isProductAvailable() {
        return id != null && availabilityService != null && availabilityService.isProductAvailable(id);
    }

    public String getProductAvailabilityStatus() {
        return (id != null && availabilityService != null)
                ? availabilityService.getProductAvailabilityStatus(id)
                : "OUT OF STOCK";
    }

    public boolean isAvailable() {
        return isProductAvailable();
    }

    public String getAvailabilityStatus() {
        return getProductAvailabilityStatus();
    }

    public String getStorePhone() {
        return DEFAULT_STORE_PHONE;
    }

    public long getRentalDays() {
        return DateUtil.calculateDaysBetween(startDate, endDate);
    }

    public long getCalculatedRentalFee() {
        if (deviceModel == null) {
            return 0L;
        }
        return getRentalDays() * deviceModel.getRentalPrice();
    }

    public String getFormattedRentalFee() {
        return formatPrice(getCalculatedRentalFee());
    }

    public long getEstimatedTotalAmount() {
        if (deviceModel == null) {
            return 0L;
        }
        return getCalculatedRentalFee() + deviceModel.getDepositAmount();
    }

    public String getFormattedEstimatedTotal() {
        return formatPrice(getEstimatedTotalAmount());
    }

    public String getDisplayStartDate() {
        return DateUtil.formatDisplayDate(startDate);
    }

    public String getDisplayEndDate() {
        return DateUtil.formatDisplayDate(endDate);
    }

    public String formatDisplayDate(String dateStr) {
        return DateUtil.formatDisplayDate(dateStr);
    }

    public String formatPrice(long price) {
        return FormatUtil.formatPrice(price);
    }

    public String formatNumber(long number) {
        return FormatUtil.formatNumber(number);
    }

    public String getImageUrl(String imageName) {
        return ImageUtil.getDeviceImageUrl(imageName);
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
