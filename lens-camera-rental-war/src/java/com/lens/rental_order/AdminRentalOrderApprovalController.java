package com.lens.rental_order;

import com.lens.rental_orders.service.RentalOrderApprovalServiceLocal;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;

/**
 * Controller for Admin rental order approval management.
 *
 * @author Duong Ngoc Han
 */
@Named(value = "adminRentalOrderApprovalController")
@ViewScoped
public class AdminRentalOrderApprovalController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private RentalOrderApprovalServiceLocal rentalOrderApprovalService;

    public String approveOrder(Integer orderId) {
        try {
            rentalOrderApprovalService.approveOrder(orderId);

            FacesContext.getCurrentInstance().getExternalContext().getFlash()
                    .put("actionAlert","Order #" + orderId + " approved successfully.");

            return "/admin/rentalorders-management/detail.xhtml" + "?faces-redirect=true&id=" + orderId;

        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().getExternalContext().getFlash()
                    .put("actionAlert", e.getMessage());

            return "/admin/rentalorders-management/detail.xhtml" + "?faces-redirect=true&id=" + orderId;
        }
    }

    public String rejectOrder(Integer orderId) {
        try {
            rentalOrderApprovalService.rejectOrder(orderId);

            FacesContext.getCurrentInstance().getExternalContext().getFlash()
                    .put("actionAlert", "Order #" + orderId + " rejected successfully.");

            return "/admin/rentalorders-management/detail.xhtml" + "?faces-redirect=true&id=" + orderId;

        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().getExternalContext().getFlash()
                    .put("actionAlert", e.getMessage());

            return "/admin/rentalorders-management/detail.xhtml" + "?faces-redirect=true&id=" + orderId;
        }
    }
}