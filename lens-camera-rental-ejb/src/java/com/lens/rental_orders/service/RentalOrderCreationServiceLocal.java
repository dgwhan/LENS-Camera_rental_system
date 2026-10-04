package com.lens.rental_orders.service;

import com.lens.rental_orders.entity.RentalOrders;
import java.util.Date;

/**
 * Handles rental order creation for admin.
 *
 * @author Duong Ngoc Han
 */
public interface RentalOrderCreationServiceLocal {

    RentalOrders createOrder(
            Integer userId,
            Integer deviceModelId,
            Date startDate,
            Date endDate,
            String handoverMethod,
            String customerAddress,
            String note);
}