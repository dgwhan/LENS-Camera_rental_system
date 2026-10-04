package com.lens.rental_order;

import com.lens.rental_orders.service.RentalHandoverServiceLocal;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;

/**
 * Controller for Admin rental handover management.
 *
 * @author Duong Ngoc Han
 */
@Named(value = "adminRentalHandoverController")
@ViewScoped
public class AdminRentalHandoverController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private RentalHandoverServiceLocal rentalHandoverService;

    private String failureReason;

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String completeStorePickup(Integer orderId) {
        try {
            rentalHandoverService.completeStorePickup(orderId);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert", "Store pickup completed successfully.");
        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert", e.getMessage());
        }
        return redirectToDetail(orderId);
    }

    public String failStorePickup(Integer orderId, String failureReason) {
        if (isFailureReasonEmpty(failureReason)) {
            return null;
        }

        try {
            rentalHandoverService.failStorePickup(orderId, failureReason.trim());
            this.failureReason = null;
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert", "Store pickup cancelled successfully.");
        } catch (IllegalArgumentException e) {
            addErrorMessage(e.getMessage());
            return null;
        }

        return redirectToDetail(orderId);
    }

    public String completeDelivery(Integer orderId) {
        try {
            rentalHandoverService.completeDelivery(orderId);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert", "Delivery completed successfully.");
        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert", e.getMessage());
        }
        return redirectToDetail(orderId);
    }

    public String failDelivery(Integer orderId, String failureReason) {
        if (isFailureReasonEmpty(failureReason)) {
            return null;
        }

        try {
            rentalHandoverService.failDelivery(orderId, failureReason.trim());
            this.failureReason = null;
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert", "Delivery cancelled successfully.");
        } catch (IllegalArgumentException e) {
            addErrorMessage(e.getMessage());
            return null;
        }

        return redirectToDetail(orderId);
    }

    private boolean isFailureReasonEmpty(String failureReason) {
        return failureReason == null || failureReason.trim().isEmpty();
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    private String redirectToDetail(Integer orderId) {
        return "/admin/rentalorders-management/detail.xhtml?faces-redirect=true&id=" + orderId;
    }
}