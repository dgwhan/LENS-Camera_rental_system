package com.lens.rental_orders.service;

import com.lens.device.entity.Devices;
import com.lens.device.facade.DevicesFacadeLocal;
import com.lens.rental_items.entity.RentalItems;
import com.lens.rental_items.facade.RentalItemsFacadeLocal;
import com.lens.rental_orders.entity.RentalOrders;
import com.lens.rental_orders.facade.RentalOrdersFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class RentalOrderAssignmentService implements RentalOrderAssignmentServiceLocal {

    private static final Logger LOGGER = Logger.getLogger(RentalOrderAssignmentService.class.getName());

    @EJB
    private DevicesFacadeLocal devicesFacade;

    @EJB
    private RentalItemsFacadeLocal rentalItemsFacade;

    @EJB
    private RentalOrdersFacadeLocal rentalOrdersFacade;

    @Override
    public void assignDevice(Integer orderId, Integer deviceId) {
        RentalOrders order = findApprovedOrder(orderId);
        RentalItems rentalItem = findRentalItem(orderId);

        validateDeviceId(deviceId);

        Devices device = findDevice(deviceId);
        validateDevice(device, rentalItem);

        List<Devices> availableDevices = findAvailableDevices(rentalItem);

        if (!containsDevice(availableDevices, deviceId)) {
            String message = "device is not available for the requested rental period.";

            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        rentalItem.setAssignedDeviceId(device);
        rentalItemsFacade.edit(rentalItem);
    }

    @Override
    public void unassignDevice(Integer orderId) {
        findApprovedOrder(orderId);

        RentalItems rentalItem = findRentalItem(orderId);

        rentalItem.setAssignedDeviceId(null);
        rentalItemsFacade.edit(rentalItem);
    }

    @Override
    public List<Devices> getAvailableDevices(Integer orderId) {
        findApprovedOrder(orderId);

        RentalItems rentalItem = findRentalItem(orderId);

        if (rentalItem.getDeviceModelId() == null || rentalItem.getStartDate() == null || rentalItem.getEndDate() == null) {
            return Collections.emptyList();
        }

        return findAvailableDevices(rentalItem);
    }

    private RentalOrders findApprovedOrder(Integer orderId) {
        RentalOrders order = rentalOrdersFacade.find(orderId);

        if (order == null) {
            String message = "rental order not found: " + orderId;
            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        if (!"APPROVED".equals(order.getStatus())) {
            String message = "order " + orderId + " must be APPROVED. Current status: " + order.getStatus();

            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        return order;
    }

    private RentalItems findRentalItem(Integer orderId) {
        RentalItems rentalItem = rentalItemsFacade.findByRentalOrderId(orderId).stream().findFirst().orElse(null);

        if (rentalItem == null) {
            String message = "rental item not found for order: " + orderId;
            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        return rentalItem;
    }

    private void validateDeviceId(Integer deviceId) {
        if (deviceId == null) {
            String message = "device id is required.";
            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }
    }

    private Devices findDevice(Integer deviceId) {
        Devices device = devicesFacade.find(deviceId);

        if (device == null) {
            String message = "device not found: " + deviceId;
            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        return device;
    }

    private void validateDevice(Devices device, RentalItems rentalItem) {

        if (!"AVAILABLE".equals(device.getStatus())) {
            String message = "device is not available.";
            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        if (device.getDeviceModelId() == null || rentalItem.getDeviceModelId() == null || !device.getDeviceModelId().getId()
                        .equals(rentalItem.getDeviceModelId().getId())) {

            String message = "device does not belong to the rental item device model.";

            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }
    }

    private List<Devices> findAvailableDevices(RentalItems rentalItem) {
        return devicesFacade.findAvailableForPeriod(
                rentalItem.getDeviceModelId().getId(),
                rentalItem.getStartDate(),
                rentalItem.getEndDate(),
                rentalItem.getId()
        );
    }

    private boolean containsDevice(
            List<Devices> devices,
            Integer deviceId) {

        for (Devices device : devices) {
            if (device.getId().equals(deviceId)) {
                return true;
            }
        }

        return false;
    }
}