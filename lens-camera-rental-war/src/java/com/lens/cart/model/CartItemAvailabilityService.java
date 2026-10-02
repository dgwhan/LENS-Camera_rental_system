package com.lens.cart.model;

import com.lens.availability.dto.AvailabilityResult;
import com.lens.availability.service.AvailabilityServiceLocal;
import com.lens.cart.CartItem;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;

/**
 * Handles availability checking for individual cart items.
 *
 * @author Duong Ngoc Han
 */
@ApplicationScoped
public class CartItemAvailabilityService implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private AvailabilityServiceLocal availabilityService;

    public void checkItemAvailability(CartItem item) {
        if (item == null || item.getDeviceModels() == null || availabilityService == null) {
            return;
        }

        Integer modelId = item.getDeviceModels().getId();
        if (modelId == null) {
            return;
        }

        //stock check
        boolean available = availabilityService.isProductAvailable(modelId);

        //period-specific capacity check (if when dates are provided)
        if (available && item.getStartDate() != null && item.getEndDate() != null) {
            try {
                com.lens.common.util.DateUtil.validateRentalPeriod(item.getStartDate(), item.getEndDate());
                AvailabilityResult result = availabilityService.checkAvailability(
                        modelId, item.getStartDate(), item.getEndDate());
                if (result != null && !result.isAvailable()) {
                    available = false;
                }
            } catch (Exception ex) {
                available = false;
            }
        }

        item.setOutOfStock(!available);
    }

    public boolean isOutOfStock(CartItem item) {
        checkItemAvailability(item);
        return item != null && item.isOutOfStock();
    }
}
