package com.lens.rental_orders.service;

/**
 *
 * @author Duong Ngoc Han
 */
public interface RentalHandoverServiceLocal {

    //if customer select store pickup
    void completeStorePickup(Integer orderId);

    void failStorePickup(Integer orderId, String failureReason);
    
    //if customer select deliver address
    void completeDelivery(Integer orderId);

    void failDelivery(Integer orderId, String failureReason);
}
