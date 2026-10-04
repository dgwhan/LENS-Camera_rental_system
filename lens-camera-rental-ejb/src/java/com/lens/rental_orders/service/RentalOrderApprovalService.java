package com.lens.rental_orders.service;

import com.lens.rental_orders.entity.RentalOrders;
import com.lens.rental_orders.facade.RentalOrdersFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.Date;
import java.util.logging.Logger;

/**
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class RentalOrderApprovalService implements RentalOrderApprovalServiceLocal {

    private static final Logger LOGGER = Logger.getLogger(RentalOrderApprovalService.class.getName());

    @EJB
    private RentalOrdersFacadeLocal rentalOrdersFacade;

    @Override
    public void approveOrder(Integer orderId) {
        RentalOrders order = findPendingOrder(orderId);

        order.setStatus("APPROVED");
        order.setUpdatedAt(new Date());

        rentalOrdersFacade.edit(order);
    }

    @Override
    public void rejectOrder(Integer orderId) {
        RentalOrders order = findPendingOrder(orderId);

        order.setStatus("REJECTED");
        order.setUpdatedAt(new Date());

        rentalOrdersFacade.edit(order);
    }

    private RentalOrders findPendingOrder(Integer orderId) {
        RentalOrders order = rentalOrdersFacade.find(orderId);

        if (order == null) {
            String message = "rental order not found: " + orderId;
            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        if (!"PENDING".equals(order.getStatus())) {
            String message = "order " + orderId + " must be PENDING. Current status: " + order.getStatus();

            LOGGER.warning(message);
            throw new IllegalArgumentException(message);
        }

        return order;
    }
}