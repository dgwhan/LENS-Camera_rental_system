package com.lens.rental_orders.service;

import com.lens.device.entity.Devices;
import com.lens.device.facade.DevicesFacadeLocal;
import com.lens.rental_deposits.entity.RentalDeposits;
import com.lens.rental_deposits.facade.RentalDepositsFacadeLocal;
import com.lens.rental_handovers.entity.RentalHandovers;
import com.lens.rental_handovers.facade.RentalHandoversFacadeLocal;
import com.lens.rental_items.entity.RentalItems;
import com.lens.rental_items.facade.RentalItemsFacadeLocal;
import com.lens.rental_orders.entity.RentalOrders;
import com.lens.rental_orders.facade.RentalOrdersFacadeLocal;
import com.lens.rental_payments.entity.RentalPayments;
import com.lens.rental_payments.facade.RentalPaymentsFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service for rental handover management.
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class RentalHandoverService implements RentalHandoverServiceLocal {

    private static final Logger LOGGER = Logger.getLogger(RentalHandoverService.class.getName());

    @EJB
    private RentalOrdersFacadeLocal rentalOrdersFacade;

    @EJB
    private RentalItemsFacadeLocal rentalItemsFacade;

    @EJB
    private RentalHandoversFacadeLocal rentalHandoversFacade;

    @EJB
    private DevicesFacadeLocal devicesFacade;

    @EJB
    private RentalPaymentsFacadeLocal rentalPaymentsFacade;

    @EJB
    private RentalDepositsFacadeLocal rentalDepositsFacade;

    @Override
    public void completeStorePickup(Integer orderId) {
        completeHandover(orderId, "STORE_PICKUP");
    }

    @Override
    public void failStorePickup(Integer orderId, String failureReason) {
        failHandover(orderId, "STORE_PICKUP", failureReason);
    }

    @Override
    public void completeDelivery(Integer orderId) {
        completeHandover(orderId, "DELIVERY");
    }

    @Override
    public void failDelivery(Integer orderId, String failureReason) {
        failHandover(orderId, "DELIVERY", failureReason);
    }

    private void completeHandover(Integer orderId, String expectedMethod) {

        RentalOrders rentalOrder = findApprovedOrder(orderId);
        RentalHandovers handover = findPendingHandover(orderId, expectedMethod);
        Devices device = findAssignedDevice(orderId);
        RentalPayments payment = findPayment(orderId);
        RentalDeposits deposit = findDeposit(orderId);

        Date now = new Date();

        handover.setStatus("COMPLETED");
        handover.setConfirmedAt(now);

        rentalOrder.setStatus("ACTIVE");

        device.setStatus("RENTING");

        payment.setPaymentStatus("PAID");
        payment.setPaidAt(now);

        deposit.setStatus("NOT_REFUNDED");

        rentalHandoversFacade.edit(handover);
        rentalOrdersFacade.edit(rentalOrder);
        devicesFacade.edit(device);
        rentalPaymentsFacade.edit(payment);
        rentalDepositsFacade.edit(deposit);
    }

    private void failHandover(Integer orderId, String expectedMethod, String failureReason) {

        RentalOrders rentalOrder = findApprovedOrder(orderId);
        RentalHandovers handover = findPendingHandover(orderId, expectedMethod);
        RentalItems rentalItem = findRentalItem(orderId);
        Devices device = rentalItem.getAssignedDeviceId();
        RentalPayments payment = findPayment(orderId);
        RentalDeposits deposit = findDeposit(orderId);

        validateFailureReason(failureReason);

        if (device == null) {
            String message = "No device has been assigned to this order.";
            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        handover.setStatus("FAILED");
        handover.setFailureReason(failureReason.trim());

        rentalOrder.setStatus("CANCELLED");

        rentalItem.setAssignedDeviceId(null);

        device.setStatus("AVAILABLE");

        payment.setPaymentStatus("UNPAID");

        deposit.setStatus("NOT_REFUNDED");

        rentalHandoversFacade.edit(handover);
        rentalOrdersFacade.edit(rentalOrder);
        rentalItemsFacade.edit(rentalItem);
        devicesFacade.edit(device);
        rentalPaymentsFacade.edit(payment);
        rentalDepositsFacade.edit(deposit);
    }

    private RentalOrders findApprovedOrder(Integer orderId) {
        if (orderId == null) {
            String message = "Rental order id is required.";
            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        RentalOrders rentalOrder = rentalOrdersFacade.find(orderId);

        if (rentalOrder == null) {
            String message = "Rental order not found.";
            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        if (!"APPROVED".equals(rentalOrder.getStatus())) {
            String message = "Only approved orders can process handover.";
            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        return rentalOrder;
    }

    private RentalHandovers findPendingHandover(Integer orderId, String expectedMethod) {

        RentalHandovers handover
                = rentalHandoversFacade.findByRentalOrderId(orderId);

        if (handover == null) {
            String message = "Rental handover not found.";
            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        if (!expectedMethod.equals(handover.getMethod())) {
            String message;

            if ("STORE_PICKUP".equals(expectedMethod)) {
                message = "This order is not a store pickup order.";
            } else {
                message = "This order is not a delivery order.";
            }

            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        if (!"PENDING".equals(handover.getStatus())) {
            String message;

            if ("STORE_PICKUP".equals(expectedMethod)) {
                message = "Store pickup is no longer pending.";
            } else {
                message = "Delivery is no longer pending.";
            }

            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        return handover;
    }

    private RentalItems findRentalItem(Integer orderId) {
        List<RentalItems> rentalItems = rentalItemsFacade.findByRentalOrderId(orderId);

        if (rentalItems == null || rentalItems.isEmpty()) {
            String message = "Rental item not found.";
            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        if (rentalItems.size() != 1) {
            String message = "Rental order must contain exactly one rental item.";
            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        return rentalItems.get(0);
    }

    private Devices findAssignedDevice(Integer orderId) {
        RentalItems rentalItem = findRentalItem(orderId);
        Devices device = rentalItem.getAssignedDeviceId();

        if (device == null) {
            String message = "No device has been assigned to this order.";
            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        return device;
    }

    private RentalPayments findPayment(Integer orderId) {
        RentalPayments payment = rentalPaymentsFacade.findByRentalOrderId(orderId);

        if (payment == null) {
            String message = "Rental payment not found.";
            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        return payment;
    }

    private RentalDeposits findDeposit(Integer orderId) {
        RentalDeposits deposit = rentalDepositsFacade.findByRentalOrderId(orderId);

        if (deposit == null) {
            String message = "Rental deposit not found.";
            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        return deposit;
    }

    private void validateFailureReason(String failureReason) {
        if (failureReason == null || failureReason.trim().isEmpty()) {
            String message = "Failure reason is required.";
            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }
    }
}