package com.lens.checkout.service;

import com.lens.availability.dto.AvailabilityResult;
import com.lens.availability.service.AvailabilityServiceLocal;
import com.lens.cart.entity.CartItems;
import com.lens.cart.facade.CartItemsFacadeLocal;
import com.lens.device_model.entity.DeviceModels;
import com.lens.rental_orders.entity.RentalOrders;
import com.lens.rental_orders.facade.RentalOrdersFacadeLocal;
import com.lens.rental_items.entity.RentalItems;
import com.lens.rental_items.facade.RentalItemsFacadeLocal;
import com.lens.user.entity.Users;
import com.lens.user.facade.UsersFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Handles checkout operations for both cart-based checkout and direct device checkout.
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class CheckoutService implements CheckoutServiceLocal {

    @EJB
    private RentalItemsFacadeLocal rentalItemsFacade;

    @EJB
    private RentalOrdersFacadeLocal rentalOrdersFacade;

    @EJB
    private AvailabilityServiceLocal availabilityService;

    @EJB
    private CartItemsFacadeLocal cartItemsFacade;

    @EJB
    private UsersFacadeLocal usersFacade;


    @Override
    public List<RentalOrders> processCheckout(Integer userId, String customerName, String customerPhone, String customerAddress) {
        return processCheckout(userId, customerName, customerPhone, customerAddress, null);
    }

    @Override
    public List<RentalOrders> processCheckout(Integer userId, String customerName, String customerPhone, String customerAddress, List<Integer> selectedCartItemIds) {
        Users user = validateUser(userId);

        //load cart
        List<CartItems> allCartItems = cartItemsFacade.findByUserId(userId);
        if (allCartItems == null || allCartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty.");
        }

        List<CartItems> cartItems;
        if (selectedCartItemIds != null && !selectedCartItemIds.isEmpty()) {
            cartItems = allCartItems.stream()
                    .filter(item -> selectedCartItemIds.contains(item.getId()))
                    .collect(java.util.stream.Collectors.toList());
            if (cartItems.isEmpty()) {
                throw new IllegalStateException("No selected items found in cart.");
            }
        } else {
            cartItems = allCartItems;
        }

        Date bookingDate = new Date();

        //validate cart items
        for (CartItems item : cartItems) {
            Date startDate = item.getStartDate();
            Date endDate = item.getEndDate();
            int duration = item.getDuration();

            validateRentalPeriod(startDate, endDate, bookingDate);

            if (duration <= 0) {
                throw new IllegalArgumentException("Rental duration must be greater than zero.");
            }

            if (item.getDeviceModelId() == null) {
                throw new IllegalArgumentException("Device model is required.");
            }

            if (item.getRentalPrice() < 0 || item.getDepositAmount() < 0) {
                throw new IllegalArgumentException("Invalid rental price or deposit amount.");
            }

            //check availability for each cart item
            Integer deviceModelId = item.getDeviceModelId().getId();
            AvailabilityResult availability = availabilityService.checkAvailability(deviceModelId, startDate, endDate);

            if (!availability.isAvailable()) {
                throw new IllegalStateException("Device model is not available for the selected rental period.");
            }
        }

        //check combined capacity via AvailabilityService (single source of truth)
        availabilityService.validateCombinedCapacity(cartItems);

       //create one rental order and rental item for each cart item
       List<RentalOrders> rentalOrders = new ArrayList<>();
       
       for (CartItems cartItem : cartItems) {
           long itemSubtotal = (long) cartItem.getDuration() * cartItem.getRentalPrice();
           long itemDeposit = cartItem.getDepositAmount();
           long itemTotal = itemSubtotal + itemDeposit;
           
           RentalOrders rentalOrder = createRentalOrder(user, customerName, customerPhone, customerAddress, itemSubtotal, itemDeposit, itemTotal, bookingDate);
           createRentalItem(
                   rentalOrder, 
                   cartItem.getDeviceModelId(), 
                   cartItem.getStartDate(),
                   cartItem.getEndDate(),
                   cartItem.getDuration(),
                   cartItem.getRentalPrice(),
                   cartItem.getDepositAmount(),
                   itemSubtotal
           );
           
           rentalOrders.add(rentalOrder);
       }

        //clear only processed cart items after successful order creation (DB handle)
        if (selectedCartItemIds != null && !selectedCartItemIds.isEmpty()) {
            for (CartItems cartItem : cartItems) {
                cartItemsFacade.deleteByIdAndUserId(cartItem.getId(), userId);
            }
        } else {
            cartItemsFacade.deleteByUserId(userId);
        }

        return rentalOrders; 
    }

    private Users validateUser(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User is required.");
        }

        Users user = usersFacade.find(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException("User account is not active.");
        }

        return user;
    }

    private Date normalizeDate(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date getMinimumStartDate(Date bookingDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(bookingDate != null ? bookingDate : new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    private void validateRentalPeriod(Date startDate, Date endDate, Date bookingDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Rental dates are required.");
        }

        Date normalizedStartDate = normalizeDate(startDate);
        Date normalizedEndDate = normalizeDate(endDate);
        Date minimumStartDate = getMinimumStartDate(bookingDate);

        if (normalizedStartDate.before(minimumStartDate)) {
            throw new IllegalArgumentException("Rental orders must be placed at least one day before the rental start date.");
        }

        if (!endDate.after(startDate) || !normalizedEndDate.after(normalizedStartDate)) {
            throw new IllegalArgumentException("Rental end date must be after start date.");
        }
    }

    private RentalOrders createRentalOrder(Users user, String customerName, String customerPhone, String customerAddress,
            long subtotal, long depositTotal, long totalPayable, Date createdAt) {
        Date orderTimestamp = createdAt != null ? createdAt : new Date();
        RentalOrders rentalOrder = new RentalOrders();
        rentalOrder.setUserId(user);
        rentalOrder.setCustomerName(customerName);
        rentalOrder.setCustomerPhone(customerPhone);
        rentalOrder.setDeliveryAddress(customerAddress);
        rentalOrder.setStatus("PENDING");
        rentalOrder.setSubtotal(subtotal);
        rentalOrder.setDepositTotal(depositTotal);
        rentalOrder.setTotalPayable(totalPayable);
        rentalOrder.setCreatedAt(orderTimestamp);
        rentalOrder.setUpdatedAt(orderTimestamp);
        rentalOrder.setPaymentMethod("CASH");
        rentalOrder.setPaymentStatus("UNPAID");
        rentalOrder.setDepositRefundStatus("NOT_REFUNDED");

        rentalOrdersFacade.create(rentalOrder);
        return rentalOrder;
    }

    private RentalItems createRentalItem(RentalOrders rentalOrder, DeviceModels deviceModel,
            Date startDate, Date endDate, int duration, long rentalPrice, long depositAmount, long subtotal) {
        RentalItems rentalItem = new RentalItems();
        rentalItem.setRentalOrderId(rentalOrder);
        rentalItem.setDeviceModelId(deviceModel);
        rentalItem.setAssignedDeviceId(null);
        rentalItem.setStartDate(startDate);
        rentalItem.setEndDate(endDate);
        rentalItem.setDuration(duration);
        rentalItem.setRentalPrice(rentalPrice);
        rentalItem.setDepositAmount(depositAmount);
        rentalItem.setSubtotal(subtotal);

        rentalItemsFacade.create(rentalItem);
        return rentalItem;
    }
}