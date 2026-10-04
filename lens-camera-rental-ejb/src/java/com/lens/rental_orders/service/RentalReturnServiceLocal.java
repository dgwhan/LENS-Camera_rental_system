package com.lens.rental_orders.service;

/**
 *
 * @author Duong Ngoc Han
 */
public interface RentalReturnServiceLocal {

    void processReturn (Integer orderId, String deviceCondition, String inspectionNote);
}
