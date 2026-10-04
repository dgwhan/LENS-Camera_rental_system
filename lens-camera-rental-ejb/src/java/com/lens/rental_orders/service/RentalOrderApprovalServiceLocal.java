package com.lens.rental_orders.service;

/**
 *
 * @author Duong Ngoc Han
 */
public interface RentalOrderApprovalServiceLocal {

    void approveOrder(Integer orderId);

    void rejectOrder(Integer orderId);
    
}
