package com.lens.rental_orders.service;

import com.lens.availability.dto.AvailabilityResult;
import com.lens.availability.service.AvailabilityServiceLocal;
import com.lens.device_model.entity.DeviceModels;
import com.lens.device_model.facade.DeviceModelsFacadeLocal;
import com.lens.rental_deposits.entity.RentalDeposits;
import com.lens.rental_deposits.facade.RentalDepositsFacadeLocal;
import com.lens.rental_handovers.entity.RentalHandovers;
import com.lens.rental_handovers.facade.RentalHandoversFacadeLocal;
import com.lens.rental_items.entity.RentalItems;
import com.lens.rental_items.facade.RentalItemsFacadeLocal;
import com.lens.rental_orders.entity.RentalOrders;
import com.lens.rental_orders.facade.RentalOrdersFacadeLocal;
import com.lens.rental_payments.entity.RentalPayments;
import com.lens.rental_payments.facade.RentalPaymentsFacadeLocal;
import com.lens.user.entity.Users;
import com.lens.user.facade.UsersFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.Calendar;
import java.util.Date;

/**
 * Handles admin rental order creation.
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class RentalOrderCreationService implements RentalOrderCreationServiceLocal {

    private static final String CUSTOMER_ROLE = "CUSTOMER";
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String PENDING_STATUS = "PENDING";

    private static final String STORE_PICKUP = "STORE_PICKUP";
    private static final String DELIVERY = "DELIVERY";

    private static final String PAYMENT_METHOD = "CASH";
    private static final String PAYMENT_UNPAID = "UNPAID";
    private static final String DEPOSIT_STATUS_NOT_REFUNDED = "NOT_REFUNDED";

    @EJB
    private RentalOrdersFacadeLocal rentalOrdersFacade;

    @EJB
    private RentalItemsFacadeLocal rentalItemsFacade;

    @EJB
    private RentalHandoversFacadeLocal rentalHandoversFacade;

    @EJB
    private RentalPaymentsFacadeLocal rentalPaymentsFacade;

    @EJB
    private RentalDepositsFacadeLocal rentalDepositsFacade;

    @EJB
    private AvailabilityServiceLocal availabilityService;

    @EJB
    private UsersFacadeLocal usersFacade;

    @EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    @Override
    public RentalOrders createOrder(Integer userId, Integer deviceModelId, Date startDate, Date endDate, String handoverMethod, String customerAddress, String note) {
        Users customer = validateCustomer(userId);
        DeviceModels deviceModel = validateDeviceModel(deviceModelId);
        validateRentalPeriod(startDate, endDate);
        validateHandover(handoverMethod, customerAddress);
        validateAvailability(deviceModelId, startDate, endDate);

        int duration = calculateRentalDays(startDate, endDate);
        long rentalPrice = deviceModel.getRentalPrice();
        long depositAmount = deviceModel.getDepositAmount();
        long subtotal = rentalPrice * duration;
        long totalPayable = subtotal;
        Date now = new Date();

        RentalOrders rentalOrder = createRentalOrder(customer, subtotal, depositAmount, totalPayable, customerAddress, note, now);
        createRentalItem(rentalOrder, deviceModel, startDate, endDate, duration, rentalPrice, depositAmount, subtotal);
        createRentalHandover(rentalOrder, handoverMethod, customerAddress, now);
        createRentalPayment(rentalOrder, totalPayable, now);
        createRentalDeposit(rentalOrder, depositAmount);

        return rentalOrder;
    }

    private Users validateCustomer(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Customer is required.");
        }

        Users customer = usersFacade.find(userId);

        if (customer == null) {
            throw new IllegalArgumentException("Customer not found.");
        }

        if (!CUSTOMER_ROLE.equalsIgnoreCase(customer.getRole())) {
            throw new IllegalArgumentException("Selected user is not a customer.");
        }

        if (!ACTIVE_STATUS.equalsIgnoreCase(customer.getStatus())) {
            throw new IllegalArgumentException("Selected customer is not active.");
        }

        return customer;
    }

    private DeviceModels validateDeviceModel(Integer deviceModelId) {
        if (deviceModelId == null) {
            throw new IllegalArgumentException("Device model is required.");
        }

        DeviceModels deviceModel = deviceModelsFacade.find(deviceModelId);

        if (deviceModel == null) {
            throw new IllegalArgumentException("Device model not found.");
        }

        return deviceModel;
    }

    private void validateRentalPeriod(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Rental start date and end date are required.");
        }

        Date normalizedStartDate = normalizeDate(startDate);
        Date normalizedEndDate = normalizeDate(endDate);

        if (normalizedStartDate.before(startOfDay(new Date()))) {
            throw new IllegalArgumentException("Rental start date cannot be in the past.");
        }

        if (!normalizedEndDate.after(normalizedStartDate)) {
            throw new IllegalArgumentException("End date must be after start date.");
        }
    }

    private void validateHandover(String handoverMethod, String customerAddress) {
        if (handoverMethod == null || handoverMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Handover method is required.");
        }

        if (!STORE_PICKUP.equalsIgnoreCase(handoverMethod) && !DELIVERY.equalsIgnoreCase(handoverMethod)) {
            throw new IllegalArgumentException("Invalid handover method.");
        }

        if (DELIVERY.equalsIgnoreCase(handoverMethod) && (customerAddress == null || customerAddress.trim().isEmpty())) {
            throw new IllegalArgumentException("Delivery address is required.");
        }
    }

    private void validateAvailability(Integer deviceModelId, Date startDate, Date endDate) {
        AvailabilityResult result = availabilityService.checkAvailability(deviceModelId, startDate, endDate);

        if (result == null) {
            throw new IllegalStateException("Unable to check device availability.");
        }

        if (!result.isAvailable()) {
            String message = result.getMessage();

            if (message == null || message.trim().isEmpty()) {
                message = "Device is not available for the selected rental period.";
            }

            throw new IllegalStateException(message);
        }
    }

    private RentalOrders createRentalOrder(Users customer, long subtotal, long depositAmount, long totalPayable, String customerAddress, String note, Date now) {
        RentalOrders rentalOrder = new RentalOrders();
        rentalOrder.setUserId(customer);
        rentalOrder.setCustomerName(customer.getFullName());
        rentalOrder.setCustomerPhone(customer.getPhone());
        rentalOrder.setCustomerAddress(customerAddress);
        rentalOrder.setStatus(PENDING_STATUS);
        rentalOrder.setSubtotal(subtotal);
        rentalOrder.setDepositTotal(depositAmount);
        rentalOrder.setTotalPayable(totalPayable);
        rentalOrder.setNote(note);
        rentalOrder.setCreatedAt(now);
        rentalOrder.setUpdatedAt(now);

        rentalOrdersFacade.create(rentalOrder);

        return rentalOrder;
    }

    private RentalItems createRentalItem(RentalOrders rentalOrder, DeviceModels deviceModel, Date startDate, Date endDate, int duration, long rentalPrice, long depositAmount, long subtotal) {
        RentalItems rentalItem = new RentalItems();
        rentalItem.setRentalOrderId(rentalOrder);
        rentalItem.setDeviceModelId(deviceModel);
        rentalItem.setStartDate(normalizeDate(startDate));
        rentalItem.setEndDate(normalizeDate(endDate));
        rentalItem.setDuration(duration);
        rentalItem.setRentalPrice(rentalPrice);
        rentalItem.setDepositAmount(depositAmount);
        rentalItem.setSubtotal(subtotal);
        rentalItem.setAssignedDeviceId(null);

        rentalItemsFacade.create(rentalItem);

        return rentalItem;
    }

    private RentalHandovers createRentalHandover(RentalOrders rentalOrder, String handoverMethod, String customerAddress, Date now) {
        RentalHandovers rentalHandover = new RentalHandovers();
        rentalHandover.setRentalOrderId(rentalOrder);
        rentalHandover.setMethod(handoverMethod);
        rentalHandover.setStatus(PENDING_STATUS);

        if (DELIVERY.equalsIgnoreCase(handoverMethod)) {
            rentalHandover.setDeliveryAddress(customerAddress);
        }

        rentalHandover.setCreatedAt(now);
        rentalHandover.setUpdatedAt(now);

        rentalHandoversFacade.create(rentalHandover);

        return rentalHandover;
    }

    private RentalPayments createRentalPayment(RentalOrders rentalOrder, long totalPayable, Date now) {
        RentalPayments rentalPayment = new RentalPayments();
        rentalPayment.setRentalOrderId(rentalOrder);
        rentalPayment.setPaymentMethod(PAYMENT_METHOD);
        rentalPayment.setPaymentStatus(PAYMENT_UNPAID);
        rentalPayment.setAmount(totalPayable);
        rentalPayment.setCreatedAt(now);
        rentalPayment.setUpdatedAt(now);

        rentalPaymentsFacade.create(rentalPayment);

        return rentalPayment;
    }

    private RentalDeposits createRentalDeposit(RentalOrders rentalOrder, long depositAmount) {
        Date now = new Date();
        RentalDeposits rentalDeposit = new RentalDeposits();
        rentalDeposit.setRentalOrderId(rentalOrder);
        rentalDeposit.setAmount(depositAmount);
        rentalDeposit.setStatus(DEPOSIT_STATUS_NOT_REFUNDED);
        rentalDeposit.setCreatedAt(now);
        rentalDeposit.setUpdatedAt(now);

        rentalDepositsFacade.create(rentalDeposit);

        return rentalDeposit;
    }

    private int calculateRentalDays(Date startDate, Date endDate) {
        Date normalizedStartDate = normalizeDate(startDate);
        Date normalizedEndDate = normalizeDate(endDate);
        long millisecondsPerDay = 24L * 60L * 60L * 1000L;
        long difference = normalizedEndDate.getTime() - normalizedStartDate.getTime();

        return (int) (difference / millisecondsPerDay);
    }

    private Date normalizeDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date startOfDay(Date date) {
        return normalizeDate(date);
    }
}
