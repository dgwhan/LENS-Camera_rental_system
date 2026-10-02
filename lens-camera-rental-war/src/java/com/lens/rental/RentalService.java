package com.lens.rental;

import com.lens.availability.dto.AvailabilityResult;
import com.lens.availability.service.AvailabilityServiceLocal;
import com.lens.cart.CartItem;
import com.lens.cart.model.CartService;
import com.lens.common.util.DateUtil;
import com.lens.device_model.entity.DeviceModels;
import com.lens.device_model.facade.DeviceModelsFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

/**
 * Orchestrates customer rental preparation workflows.
 *
 * @author Duong Ngoc Han
 */
@ApplicationScoped
public class RentalService implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private AvailabilityServiceLocal availabilityService;

    @EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    @Inject
    private CartService cartService;


    public void addToCart(Integer modelId, String startDateStr, String endDateStr) {
        DeviceModels model = resolveModel(modelId);
        Date[] period = parseAndValidatePeriod(startDateStr, endDateStr);
        checkAvailability(modelId, period[0], period[1]);

        boolean added = cartService.addToCart(model, period[0], period[1]);
        if (!added) {
            throw new IllegalStateException("This device is already in your cart for the selected rental period.");
        }
    }

    public void rentNow(Integer modelId, String startDateStr, String endDateStr) {
        DeviceModels model = resolveModel(modelId);
        Date[] period = parseAndValidatePeriod(startDateStr, endDateStr);
        checkAvailability(modelId, period[0], period[1]);

        // Add to cart; duplicate is ignored so the item is guaranteed to exist
        cartService.addToCart(model, period[0], period[1]);

        // Select only this item so that checkout scopes to this single rental
        CartItem target = cartService.findItemByModelAndDates(modelId, period[0], period[1]);
        if (target != null) {
            cartService.selectOnly(target);
        }
    }

    public void updateRentalPeriod(String cartItemId, String startDateStr, String endDateStr) {
        if (cartItemId == null || cartItemId.trim().isEmpty()) {
            throw new IllegalArgumentException("Cart item not specified.");
        }
        Date[] period = parseAndValidatePeriod(startDateStr, endDateStr);
        boolean updated = cartService.updateRentalPeriod(cartItemId, period[0], period[1]);
        if (!updated) {
            throw new IllegalStateException("Unable to update rental period.");
        }
    }

    private DeviceModels resolveModel(Integer modelId) {
        if (modelId == null) {
            throw new IllegalArgumentException("Device not found.");
        }
        DeviceModels model = deviceModelsFacade.find(modelId);
        if (model == null) {
            throw new IllegalArgumentException("Device not found.");
        }
        return model;
    }

    private Date[] parseAndValidatePeriod(String startDateStr, String endDateStr) {
        if (isBlank(startDateStr) || isBlank(endDateStr)) {
            throw new IllegalArgumentException("Please select both start date and end date.");
        }

        try {
            Date startDate = DateUtil.parseDate(startDateStr);
            Date endDate = DateUtil.parseDate(endDateStr);
            DateUtil.validateRentalPeriod(startDate, endDate);
            return new Date[]{startDate, endDate};
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Please select a valid rental period.");
        }
    }

    private void checkAvailability(Integer modelId, Date startDate, Date endDate) {
        if (availabilityService == null) {
            return;
        }
        AvailabilityResult result = availabilityService.checkAvailability(modelId, startDate, endDate);
        if (!result.isAvailable()) {
            throw new IllegalStateException("This device is not available for the selected dates.");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
