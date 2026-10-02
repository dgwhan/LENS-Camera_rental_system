package com.lens.checkout.service;

import com.lens.rental_orders.entity.RentalOrders;
import java.util.Date;

/**
 *
 * @author Duong Ngoc Han
 */
public interface CheckoutServiceLocal {
    RentalOrders processCheckout(Integer userId, String customerName, String customerPhone);

    RentalOrders processCheckout(Integer userId, String customerName, String customerPhone, java.util.List<Integer> selectedCartItemIds);
}
