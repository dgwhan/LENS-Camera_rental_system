package com.lens.rental_order;

import com.lens.rental_orders.service.RentalReturnServiceLocal;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;

/**
 * Controller for Admin rental return management.
 *
 * @author Duong Ngoc Han
 */
@Named(value = "adminRentalReturnController")
@ViewScoped
public class AdminRentalReturnController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private RentalReturnServiceLocal rentalReturnService;

    private String deviceCondition = "NORMAL";
    private String inspectionNote;
    private boolean depositRefundConfirmed;

    public boolean isDepositRefundConfirmed() {
        return depositRefundConfirmed;
    }

    public void setDepositRefundConfirmed(boolean depositRefundConfirmed) {
        this.depositRefundConfirmed = depositRefundConfirmed;
    }

    public void validateDepositRefundConfirmation(
            FacesContext context,
            UIComponent component,
            Object value) throws ValidatorException {
        if ("LOST".equals(deviceCondition)) {
            depositRefundConfirmed = false;
            return;
        }

        if (!Boolean.TRUE.equals(value)) {
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Confirm that the deposit has been returned to the customer.",
                    null));
        }
    }

    public String getReturnStatus() {
        return "LOST".equals(deviceCondition) ? "NOT_RETURNED" : "RETURNED";
    }

    public void setReturnStatus(String returnStatus) {
        if ("NOT_RETURNED".equals(returnStatus)) {
            deviceCondition = "LOST";
            depositRefundConfirmed = false;
        } else if ("RETURNED".equals(returnStatus)
                && (deviceCondition == null || "LOST".equals(deviceCondition))) {
            deviceCondition = "NORMAL";
        }
    }

    public String getDeviceCondition() {
        return deviceCondition;
    }

    public void setDeviceCondition(String deviceCondition) {
        this.deviceCondition = deviceCondition;
    }

    public String getInspectionNote() {
        return inspectionNote;
    }

    public void setInspectionNote(String inspectionNote) {
        this.inspectionNote = inspectionNote;
    }

    public String processReturn(Integer orderId) {
        try {
            rentalReturnService.processReturn(orderId, deviceCondition, inspectionNote);

            clearForm();

            FacesContext.getCurrentInstance().getExternalContext().getFlash()
                    .put("actionAlert", "Rental return processed successfully.");

        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().getExternalContext().getFlash()
                    .put("actionAlert", e.getMessage());
        }

        return redirectToDetail(orderId);
    }

    private void clearForm() {
        deviceCondition = null;
        inspectionNote = null;
        depositRefundConfirmed = false;
    }

    private String redirectToDetail(Integer orderId) {
        return "/admin/rentalorders-management/detail.xhtml" + "?faces-redirect=true&id=" + orderId;
    }
}