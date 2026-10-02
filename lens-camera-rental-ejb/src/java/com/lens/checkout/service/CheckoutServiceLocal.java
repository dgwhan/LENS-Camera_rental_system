package com.lens.checkout.service;

import com.lens.rental_orders.entity.RentalOrders;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
public interface CheckoutServiceLocal {
    List<RentalOrders> processCheckout(Integer userId, String customerName, String customerPhone, String customerAddress);

    List<RentalOrders> processCheckout(Integer userId, String customerName, String customerPhone, String customerAddress,
            List<Integer> selectedCartItemIds);
}
