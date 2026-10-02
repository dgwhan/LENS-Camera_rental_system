package com.lens.availability.service;

import com.lens.availability.dto.AvailabilityResult;
import java.util.Date;

/**
 *
 * @author Duong Ngoc Han
 */
public interface AvailabilityServiceLocal {
    
    AvailabilityResult checkAvailability(Integer deviceModelId, Date requestedStartDate, Date requestedEndDate);

    boolean isProductAvailable(Integer deviceModelId);

    String getProductAvailabilityStatus(Integer deviceModelId);

    void validateCombinedCapacity(java.util.List<com.lens.cart.entity.CartItems> cartItems);
}

