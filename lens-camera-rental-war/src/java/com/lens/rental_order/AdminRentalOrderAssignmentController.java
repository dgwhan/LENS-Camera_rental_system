package com.lens.rental_order;

import com.lens.device.entity.Devices;
import com.lens.rental_orders.service.RentalOrderAssignmentServiceLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

/**
 * Controller for Admin rental order device assignment management.
 *
 * @author Duong Ngoc Han
 */
@Named(value = "adminRentalOrderAssignmentController")
@ViewScoped
public class AdminRentalOrderAssignmentController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private RentalOrderAssignmentServiceLocal orderAssignmentService;

    private List<Devices> availableDevices;
    private Integer selectedDeviceId;

    public Integer getSelectedDeviceId() {
        return selectedDeviceId;
    }

    public void setSelectedDeviceId(Integer selectedDeviceId) {
        this.selectedDeviceId = selectedDeviceId;
    }

    public String assignDevice(Integer orderId, Integer deviceId) {
        if (deviceId == null) {
            FacesContext.getCurrentInstance().getExternalContext().getFlash()
                    .put("actionAlert", "Please select a device to assign.");

            return "/admin/rentalorders-management/detail.xhtml?faces-redirect=true&id=" + orderId;
        }

        try {
            orderAssignmentService.assignDevice(orderId, deviceId);

            this.selectedDeviceId = null;

            FacesContext.getCurrentInstance().getExternalContext().getFlash()
                    .put("actionAlert", "Device assigned successfully.");

        } catch (EJBException | IllegalArgumentException e) {
            String msg = (e instanceof EJBException && e.getCause() != null)
                    ? e.getCause().getMessage()
                    : e.getMessage();

            FacesContext.getCurrentInstance().getExternalContext().getFlash()
                    .put("actionAlert", msg);
        }

        return "/admin/rentalorders-management/detail.xhtml?faces-redirect=true&id=" + orderId;
    }

    public String unassignDevice(Integer orderId) {
        try {
            orderAssignmentService.unassignDevice(orderId);

            FacesContext.getCurrentInstance().getExternalContext().getFlash()
                    .put("actionAlert", "Device unassigned successfully.");

        } catch (EJBException | IllegalArgumentException e) {
            String msg = (e instanceof EJBException && e.getCause() != null)
                    ? e.getCause().getMessage()
                    : e.getMessage();

            FacesContext.getCurrentInstance().getExternalContext().getFlash()
                    .put("actionAlert", msg);
        }

        return "/admin/rentalorders-management/detail.xhtml?faces-redirect=true&id=" + orderId;
    }

    public List<Devices> getAvailableDevices() {
        return availableDevices;
    }

    public void loadAvailableDevices(Integer orderId) {
        try {
            availableDevices = orderAssignmentService.getAvailableDevices(orderId);

        } catch (EJBException | IllegalArgumentException e) {
            // Silently clear — order may not be APPROVED (expected case)
            availableDevices = null;
        }
    }
}
