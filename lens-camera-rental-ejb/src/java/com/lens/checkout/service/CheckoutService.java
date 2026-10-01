package com.lens.checkout.service;

import com.lens.availability.dto.AvailabilityResult;
import com.lens.availability.service.AvailabilityServiceLocal;
import com.lens.cart.entity.CartItems;
import com.lens.cart.facade.CartItemsFacadeLocal;
import com.lens.device_model.entity.DeviceModels;
import com.lens.device_model.facade.DeviceModelsFacadeLocal;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles checkout operations for both cart-based checkout and direct device checkout.
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class CheckoutService implements CheckoutServiceLocal {

    private static final Set<String> CAPACITY_CONSUMING_STATUSES = Set.of("PENDING", "APPROVED", "ACTIVE");

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

    @EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    /**
     * Processes cart-based checkout, validates items and capacity, creates rental
     * order, and clears cart.
     *
     * @param userId        the id of the authenticated user
     * @param customerName  the customer name
     * @param customerPhone the customer phone number
     * @return the created rental order
     */
    @Override
    public RentalOrders processCheckout(Integer userId, String customerName, String customerPhone) {
        return processCheckout(userId, customerName, customerPhone, null);
    }

    /**
     * Processes cart-based checkout for selected items, validates items and capacity,
     * creates rental order, and removes the selected items from the cart.
     *
     * @param userId              the id of the authenticated user
     * @param customerName        the customer name
     * @param customerPhone       the customer phone number
     * @param selectedCartItemIds list of selected cart item ids, or null for all items
     * @return the created rental order
     */
    @Override
    public RentalOrders processCheckout(Integer userId, String customerName, String customerPhone, List<Integer> selectedCartItemIds) {
        Users user = validateUser(userId);

        // load cart
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

        // validate cart items
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

            // check availability for each cart item
            Integer deviceModelId = item.getDeviceModelId().getId();
            AvailabilityResult availability = availabilityService.checkAvailability(deviceModelId, startDate, endDate);

            if (!availability.isAvailable()) {
                throw new IllegalStateException("Device model is not available for the selected rental period.");
            }
        }

        // check combined capacity
        validateCombinedCapacity(cartItems);

        // calculate order totals from snapshot values
        long subtotal = 0;
        long depositTotal = 0;

        for (CartItems item : cartItems) {
            long itemSubtotal = (long) item.getDuration() * item.getRentalPrice();
            subtotal += itemSubtotal;
            depositTotal += item.getDepositAmount();
        }

        long totalPayable = subtotal + depositTotal;

        // create rental order
        RentalOrders rentalOrder = createRentalOrder(user, customerName, customerPhone, subtotal, depositTotal,
                totalPayable, bookingDate);

        // create rental items
        for (CartItems cartItem : cartItems) {
            long itemSubtotal = (long) cartItem.getDuration() * cartItem.getRentalPrice();
            createRentalItem(rentalOrder, cartItem.getDeviceModelId(), cartItem.getStartDate(), cartItem.getEndDate(),
                    cartItem.getDuration(), cartItem.getRentalPrice(), cartItem.getDepositAmount(), itemSubtotal);
        }

        // clear only processed cart items after successful order creation
        if (selectedCartItemIds != null && !selectedCartItemIds.isEmpty()) {
            for (CartItems cartItem : cartItems) {
                cartItemsFacade.deleteByIdAndUserId(cartItem.getId(), userId);
            }
        } else {
            cartItemsFacade.deleteByUserId(userId);
        }

        return rentalOrder;
    }

    /**
     * Processes direct checkout for a single device model from device detail
     * without touching user's cart.
     *
     * @param userId        the id of the authenticated user
     * @param customerName  the customer name
     * @param customerPhone the customer phone number
     * @param deviceModelId the id of the rented device model
     * @param startDate     the rental start date
     * @param endDate       the rental end date
     * @return the created rental order
     */
    @Override
    public RentalOrders processDirectCheckout(Integer userId, String customerName, String customerPhone,
            Integer deviceModelId, Date startDate, Date endDate) {
        Users user = validateUser(userId);

        if (deviceModelId == null) {
            throw new IllegalArgumentException("Device model is required.");
        }

        DeviceModels deviceModel = deviceModelsFacade.find(deviceModelId);
        if (deviceModel == null) {
            throw new IllegalArgumentException("Device model not found.");
        }

        Date bookingDate = new Date();
        validateRentalPeriod(startDate, endDate, bookingDate);

        int duration = calculateDuration(startDate, endDate);

        long rentalPrice = deviceModel.getRentalPrice();
        long depositAmount = deviceModel.getDepositAmount();

        if (rentalPrice < 0 || depositAmount < 0) {
            throw new IllegalArgumentException("Invalid rental price or deposit amount.");
        }

        // check availability
        AvailabilityResult availability = availabilityService.checkAvailability(deviceModelId, startDate, endDate);
        if (!availability.isAvailable()) {
            throw new IllegalStateException("Device model is not available for the selected rental period.");
        }

        long subtotal = (long) duration * rentalPrice;
        long depositTotal = depositAmount;
        long totalPayable = subtotal + depositTotal;

        // create rental order
        RentalOrders rentalOrder = createRentalOrder(user, customerName, customerPhone, subtotal, depositTotal,
                totalPayable, bookingDate);

        // create rental item
        createRentalItem(rentalOrder, deviceModel, startDate, endDate, duration, rentalPrice, depositAmount, subtotal);

        // Cart is untouched
        return rentalOrder;
    }

    // -------------------------------------------------------------------------
    // Private Helper Methods
    // -------------------------------------------------------------------------

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

    private int calculateDuration(Date startDate, Date endDate) {
        long diffInMillis = endDate.getTime() - startDate.getTime();
        int duration = (int) Math.ceil((double) diffInMillis / (1000 * 60 * 60 * 24));
        return duration > 0 ? duration : 1;
    }

    private RentalOrders createRentalOrder(Users user, String customerName, String customerPhone,
            long subtotal, long depositTotal, long totalPayable, Date createdAt) {
        Date orderTimestamp = createdAt != null ? createdAt : new Date();
        RentalOrders rentalOrder = new RentalOrders();
        rentalOrder.setUserId(user);
        rentalOrder.setCustomerName(customerName);
        rentalOrder.setCustomerPhone(customerPhone);
        rentalOrder.setStatus("PENDING");
        rentalOrder.setSubtotal(subtotal);
        rentalOrder.setDepositTotal(depositTotal);
        rentalOrder.setTotalPayable(totalPayable);
        rentalOrder.setCreatedAt(orderTimestamp);
        rentalOrder.setUpdatedAt(orderTimestamp);
        rentalOrder.setPaymentMethod("CASH");
        rentalOrder.setPaymentStatus("UNPAID");

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

    /**
     * validates combined capacity for cart items and existing rental items.
     *
     * @param cartItems the current user's cart items
     */
    private void validateCombinedCapacity(List<CartItems> cartItems) {
        Map<Integer, List<CartItems>> itemsByModel = new HashMap<>();

        for (CartItems item : cartItems) {
            Integer deviceModelId = item.getDeviceModelId().getId();

            itemsByModel.computeIfAbsent(deviceModelId, key -> new ArrayList<>()).add(item);
        }

        for (Map.Entry<Integer, List<CartItems>> entry : itemsByModel.entrySet()) {
            Integer deviceModelId = entry.getKey();
            List<CartItems> modelCartItems = entry.getValue();

            if (modelCartItems.size() <= 1) {
                continue;
            }

            Date earliestStart = modelCartItems.get(0).getStartDate();
            Date latestEnd = modelCartItems.get(0).getEndDate();

            for (CartItems item : modelCartItems) {
                if (item.getStartDate().before(earliestStart)) {
                    earliestStart = item.getStartDate();
                }

                if (item.getEndDate().after(latestEnd)) {
                    latestEnd = item.getEndDate();
                }
            }

            AvailabilityResult availability = availabilityService.checkAvailability(deviceModelId, earliestStart,
                    latestEnd);

            int totalPhysicalDevices = availability.getTotalPhysicalDevices();

            List<RentalItems> conflictingRentalItems = rentalItemsFacade.findConflictingRentalItems(deviceModelId,
                    earliestStart, latestEnd, List.copyOf(CAPACITY_CONSUMING_STATUSES));

            if (conflictingRentalItems == null) {
                conflictingRentalItems = Collections.emptyList();
            }

            // create capacity events
            List<CapacityEvent> events = new ArrayList<>();

            for (RentalItems rentalItem : conflictingRentalItems) {
                events.add(new CapacityEvent(rentalItem.getStartDate(), 1));

                events.add(new CapacityEvent(rentalItem.getEndDate(), -1));
            }

            for (CartItems cartItem : modelCartItems) {
                events.add(new CapacityEvent(cartItem.getStartDate(), 1));

                events.add(new CapacityEvent(cartItem.getEndDate(), -1));
            }

            // sort events by date and process end before start
            events.sort((first, second) -> {
                int dateCompare = first.getDate().compareTo(second.getDate());

                if (dateCompare != 0) {
                    return dateCompare;
                }

                return Integer.compare(first.getChange(), second.getChange());
            });

            int occupiedCapacity = 0;
            int maxOccupiedCapacity = 0;

            for (CapacityEvent event : events) {
                occupiedCapacity += event.getChange();

                if (occupiedCapacity > maxOccupiedCapacity) {
                    maxOccupiedCapacity = occupiedCapacity;
                }
            }

            if (maxOccupiedCapacity > totalPhysicalDevices) {
                throw new IllegalStateException("Not enough devices available for the selected rental periods.");
            }
        }
    }

    /**
     * represents a capacity change at a specific date.
     */
    private static class CapacityEvent {

        private final Date date;
        private final int change;

        private CapacityEvent(Date date, int change) {
            this.date = date;
            this.change = change;
        }

        private Date getDate() {
            return date;
        }

        private int getChange() {
            return change;
        }
    }
}