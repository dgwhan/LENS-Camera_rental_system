package com.lens.rental_orders.service;

import com.lens.device.entity.Devices;
import com.lens.device.facade.DevicesFacadeLocal;
import com.lens.rental_deposits.entity.RentalDeposits;
import com.lens.rental_deposits.facade.RentalDepositsFacadeLocal;
import com.lens.rental_items.entity.RentalItems;
import com.lens.rental_items.facade.RentalItemsFacadeLocal;
import com.lens.rental_orders.entity.RentalOrders;
import com.lens.rental_orders.facade.RentalOrdersFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class RentalReturnService implements RentalReturnServiceLocal {

    private static final Logger LOGGER
            = Logger.getLogger(RentalReturnService.class.getName());

    @EJB
    private RentalOrdersFacadeLocal rentalOrdersFacade;

    @EJB
    private RentalItemsFacadeLocal rentalItemsFacade;

    @EJB
    private DevicesFacadeLocal devicesFacade;

    @EJB
    private RentalDepositsFacadeLocal rentalDepositsFacade;

    @Override
    public void processReturn(Integer orderId, String deviceCondition, String inspectionNote) {

        validateInput(orderId, deviceCondition);

        RentalOrders rentalOrder = rentalOrdersFacade.find(orderId);

        if (rentalOrder == null) {
            throw new IllegalArgumentException("Rental order not found.");
        }

        if (!"ACTIVE".equals(rentalOrder.getStatus())) {
            throw new IllegalArgumentException(
                    "Only active rental orders can be returned.");
        }

        List<RentalItems> rentalItems = rentalItemsFacade.findByRentalOrderId(orderId);

        if (rentalItems == null || rentalItems.isEmpty()) {
            throw new IllegalArgumentException("Rental item not found.");
        }

        RentalItems rentalItem = rentalItems.get(0);

        if (rentalItem == null) {
            throw new IllegalArgumentException("Rental item not found.");
        }

        Devices device = rentalItem.getAssignedDeviceId();

        if (device == null) {
            throw new IllegalArgumentException("No device is assigned to this order.");
        }

        RentalDeposits rentalDeposit = rentalDepositsFacade.findByRentalOrderId(orderId);

        if (rentalDeposit == null) {
            throw new IllegalArgumentException("Rental deposit not found.");
        }

        updateDeviceStatus(device, deviceCondition);

        rentalDeposit.setNote(inspectionNote);
        rentalDeposit.setStatus("LOST".equalsIgnoreCase(deviceCondition.trim())
            ? "NOT_REFUNDED"
            : "REFUNDED");

        rentalOrder.setStatus("COMPLETED");

        devicesFacade.edit(device);
        rentalDepositsFacade.edit(rentalDeposit);
        rentalOrdersFacade.edit(rentalOrder);

        LOGGER.info("Rental order #" + orderId + " completed with device condition: " + deviceCondition);
    }

    private void validateInput(Integer orderId, String deviceCondition) {

        if (orderId == null) {
            throw new IllegalArgumentException("Rental order ID is required.");
        }

        if (deviceCondition == null || deviceCondition.trim().isEmpty()) {
            throw new IllegalArgumentException("Device condition is required.");
        }

        String condition = deviceCondition.trim().toUpperCase();

        if (!"NORMAL".equals(condition) && !"DAMAGED".equals(condition) && !"LOST".equals(condition)) {

            throw new IllegalArgumentException("Invalid device condition.");
        }
    }

    private void updateDeviceStatus(Devices device, String deviceCondition) {

        String condition = deviceCondition.trim().toUpperCase();

        device.setStatus(switch (condition) {
            case "NORMAL" ->
                "AVAILABLE";
            case "DAMAGED" ->
                "MAINTENANCE";
            case "LOST" ->
                "LOST";
            default ->
                throw new IllegalArgumentException("Invalid device condition.");
        });
    }
}
