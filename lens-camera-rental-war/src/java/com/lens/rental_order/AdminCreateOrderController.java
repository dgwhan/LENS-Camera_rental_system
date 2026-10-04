package com.lens.rental_order;

import com.lens.availability.dto.AvailabilityResult;
import com.lens.availability.service.AvailabilityServiceLocal;
import com.lens.device_model.entity.DeviceModels;
import com.lens.device_model.facade.DeviceModelsFacadeLocal;
import com.lens.rental_orders.entity.RentalOrders;
import com.lens.rental_orders.service.RentalOrderCreationServiceLocal;
import com.lens.user.entity.Users;
import com.lens.user.facade.UsersFacadeLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Named
@ViewScoped
public class AdminCreateOrderController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String STORE_PICKUP = "STORE_PICKUP";
    private static final String DELIVERY = "DELIVERY";

    @EJB
    private AvailabilityServiceLocal availabilityService;

    @EJB
    private RentalOrderCreationServiceLocal rentalOrderCreationService;

    @EJB
    private UsersFacadeLocal usersFacade;

    @EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    private List<Users> customers;
    private List<DeviceModels> deviceModels;
    private Set<Integer> outOfStockDeviceModelIds = new HashSet<>();

    private Integer customerId;
    private Integer deviceModelId;
    private Date startDate;
    private Date endDate;
    private String handoverMethod = STORE_PICKUP;
    private String customerAddress;
    private String note;

    private boolean available;

    @PostConstruct
    public void init() {
        customers = usersFacade.search("", "CUSTOMER", "ACTIVE");
        deviceModels = deviceModelsFacade.findAll();
        outOfStockDeviceModelIds = new HashSet<>();
        if (deviceModels != null) {
            for (DeviceModels deviceModel : deviceModels) {
                if (deviceModel != null && !availabilityService.isProductAvailable(deviceModel.getId())) {
                    outOfStockDeviceModelIds.add(deviceModel.getId());
                }
            }
        }
    }

    public void checkAvailability() {
        available = false;

        if (deviceModelId == null) {
            addErrorMessage("rentalOrderForm:deviceModel", "Select a device model before checking availability.");
            return;
        }

        if (startDate == null && endDate == null) {
            addErrorMessage("Select a start and end date before checking availability.");
            return;
        }

        if (startDate == null) {
            addErrorMessage("Select a start date before checking availability.");
            return;
        }

        if (endDate == null) {
            addErrorMessage("Select an end date before checking availability.");
            return;
        }

        if (!validateRentalPeriod()) {
            return;
        }

        try {
            AvailabilityResult result = availabilityService.checkAvailability(deviceModelId, startDate, endDate);

            if (result == null) {
                addErrorMessage("Unable to check device availability.");
                return;
            }

            available = result.isAvailable();

            if (!available) {
                addErrorMessage(result.getMessage() == null ? "Device is not available for the selected rental period." : result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage(getRootCauseMessage(e));
        }
    }

    public String createOrder() {
        if (!validateForm()) {
            return null;
        }

        try {
            RentalOrders rentalOrder = rentalOrderCreationService.createOrder(customerId, deviceModelId, startDate, endDate, handoverMethod, customerAddress, note);

            if (rentalOrder == null) {
                addErrorMessage("Unable to create rental order.");
                return null;
            }

            addSuccessMessage("Rental order created successfully.");

            return "/admin/rentalorders-management/detail.xhtml?id=" + rentalOrder.getId() + "&faces-redirect=true";
        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
            addErrorMessage(getRootCauseMessage(e));
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage(getRootCauseMessage(e));
            return null;
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        if (customerId == null) {
            addErrorMessage("Customer is required.");
            valid = false;
        }

        if (deviceModelId == null) {
            addErrorMessage("Device model is required.");
            valid = false;
        }

        if (!validateRentalPeriod()) {
            valid = false;
        }

        if (handoverMethod == null || handoverMethod.trim().isEmpty()) {
            addErrorMessage("Handover method is required.");
            valid = false;
        } else if (!STORE_PICKUP.equalsIgnoreCase(handoverMethod) && !DELIVERY.equalsIgnoreCase(handoverMethod)) {
            addErrorMessage("Invalid handover method.");
            valid = false;
        }

        if (DELIVERY.equalsIgnoreCase(handoverMethod) && (customerAddress == null || customerAddress.trim().isEmpty())) {
            addErrorMessage("Delivery address is required.");
            valid = false;
        }

        if (customerAddress != null && customerAddress.length() > 255) {
            addErrorMessage("Delivery address must not exceed 255 characters.");
            valid = false;
        }

        if (note != null && note.length() > 1000) {
            addErrorMessage("Note must not exceed 1000 characters.");
            valid = false;
        }

        return valid;
    }

    private boolean validateRentalPeriod() {
        if (startDate == null) {
            addErrorMessage("Start date is required.");
            return false;
        }

        if (endDate == null) {
            addErrorMessage("End date is required.");
            return false;
        }

        Date normalizedStartDate = normalizeDate(startDate);
        Date normalizedEndDate = normalizeDate(endDate);
        Date today = normalizeDate(new Date());

        if (normalizedStartDate.before(today)) {
            addErrorMessage("Start date cannot be in the past.");
            return false;
        }

        if (!normalizedEndDate.after(normalizedStartDate)) {
            addErrorMessage("End date must be after start date.");
            return false;
        }

        return true;
    }

    private Date normalizeDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private String getRootCauseMessage(Throwable throwable) {
        Throwable rootCause = throwable;

        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        String message = rootCause.getMessage();

        if (message == null || message.trim().isEmpty()) {
            return rootCause.getClass().getSimpleName();
        }

        return rootCause.getClass().getSimpleName() + ": " + message;
    }

    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    private void addErrorMessage(String clientId, String message) {
        FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    public List<Users> getCustomers() {
        return customers;
    }

    public List<DeviceModels> getDeviceModels() {
        return deviceModels;
    }

    public boolean isDeviceModelOutOfStock(Integer deviceModelId) {
        return deviceModelId != null && outOfStockDeviceModelIds.contains(deviceModelId);
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getDeviceModelId() {
        return deviceModelId;
    }

    public void setDeviceModelId(Integer deviceModelId) {
        this.deviceModelId = deviceModelId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getHandoverMethod() {
        return handoverMethod;
    }

    public void setHandoverMethod(String handoverMethod) {
        this.handoverMethod = handoverMethod;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isAvailable() {
        return available;
    }
}
