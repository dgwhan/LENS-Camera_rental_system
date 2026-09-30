package com.lens.checkout.service;

import com.lens.rentail_orders.entity.RentalOrders;
import java.util.Date;

/**
 *
 * @author Duong Ngoc Han
 */
public interface CheckoutServiceLocal {
    RentalOrders processCheckout(Integer userId, String customerName, String customerPhone);

    RentalOrders processDirectCheckout(Integer userId, String customerName, String customerPhone, Integer deviceModelId, Date startDate, Date endDate);
}
