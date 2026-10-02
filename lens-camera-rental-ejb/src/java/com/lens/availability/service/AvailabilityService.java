package com.lens.availability.service;

import com.lens.availability.dto.AvailabilityResult;
import com.lens.device.entity.Devices;
import com.lens.device.facade.DevicesFacadeLocal;
import com.lens.device_model.entity.DeviceModels;
import com.lens.device_model.facade.DeviceModelsFacadeLocal;
import com.lens.rental_items.entity.RentalItems;
import com.lens.rental_items.facade.RentalItemsFacadeLocal;
import jakarta.ejb.Stateless;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class AvailabilityService implements AvailabilityServiceLocal {

    //Các trạng thái đơn thuê đang giữ/chiếm thiết bị, không cho người khác thuê
    private static final Set<String> CAPACITY_CONSUMING_STATUSES = Set.of("PENDING", "APPROVED", "ACTIVE");

    @jakarta.ejb.EJB
    private DevicesFacadeLocal devicesFacade;

    @jakarta.ejb.EJB
    private RentalItemsFacadeLocal rentalItemsFacade;

    @jakarta.ejb.EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    @Override
    public AvailabilityResult checkAvailability(Integer deviceModelId, Date requestedStartDate, Date requestedEndDate) {
        //validate khoảng thời gian thuê
        if (deviceModelId == null || requestedStartDate == null || requestedEndDate == null || !requestedEndDate.after(requestedStartDate)) {
            return new AvailabilityResult(false, 0, 0, 0, "Invalid rental period."
            );
        }

        //kiểm tra DeviceModel có tồn tại
        DeviceModels deviceModel = deviceModelsFacade.find(deviceModelId);
        if (deviceModel == null) {
            return new AvailabilityResult(false, 0, 0, 0, "Device model not found.");
        }

        //lấy tất cả thiết bị vật lý thuộc DeviceModel
        List<Devices> devices
                = devicesFacade.findByDeviceModelId(deviceModelId);

        if (devices == null) {
            devices = Collections.emptyList();
        }

        int totalPhysicalDevices = devices.size();
        int totalAvailableDevices = (int) devices.stream()
                .filter(d -> "AVAILABLE".equalsIgnoreCase(d.getStatus()))
                .count();

        //tìm các RentalItem bị conflict trong khoảng thời gian yêu cầu
        List<RentalItems> conflictingRentalItems = rentalItemsFacade.findConflictingRentalItems(deviceModelId, requestedStartDate, requestedEndDate, List.copyOf(CAPACITY_CONSUMING_STATUSES));
        if (conflictingRentalItems == null) {
            conflictingRentalItems = Collections.emptyList();
        }

        //tính số lượng thiết bị đang bị chiếm
        int occupiedCapacity = conflictingRentalItems.size();

        //tính số lượng thiết bị còn khả dụng dựa trên số lượng thiết bị vật lý có status AVAILABLE
        int availableCapacity = totalAvailableDevices - occupiedCapacity;

        //xác định khả năng cho thuê
        boolean available = availableCapacity > 0;
        String message = available ? "Device is available for the selected rental period." : "Device is not available for the selected rental period.";

        //trả về kết quả kiểm tra
        return new AvailabilityResult(available, totalPhysicalDevices, occupiedCapacity, availableCapacity, message);
    }

    // customer availability
    // product availability
    @Override
    public boolean isProductAvailable(Integer deviceModelId) {
        if (deviceModelId == null || deviceModelId <= 0) {
            return false;
        }

        List<Devices> devices = devicesFacade.findByDeviceModelId(deviceModelId);
        if (devices == null || devices.isEmpty()) {
            return false;
        }

        // Đếm số lượng thiết bị vật lý có trạng thái AVAILABLE
        long availablePhysicalCount = devices.stream()
                .filter(d -> "AVAILABLE".equalsIgnoreCase(d.getStatus()))
                .count();

        if (availablePhysicalCount <= 0) {
            return false;
        }

        // Đếm số đơn thuê đang giữ thiết bị (PENDING, APPROVED, ACTIVE)
        long activeRentalsCount = rentalItemsFacade.countActiveByDeviceModelId(
                deviceModelId, List.copyOf(CAPACITY_CONSUMING_STATUSES));

        // Một DeviceModel là AVAILABLE khi số thiết bị vật lý AVAILABLE nhiều hơn số đơn thuê đang chiếm giữ
        return availablePhysicalCount > activeRentalsCount;
    }


    // customer availability
    // out of stock handling
    @Override
    public String getProductAvailabilityStatus(Integer deviceModelId) {
        return isProductAvailable(deviceModelId) ? "AVAILABLE" : "OUT OF STOCK";
    }

    /**
     * Validates combined capacity for multiple cart items and existing rental items.
     *
     * @param cartItems the user's cart items
     */
    @Override
    public void validateCombinedCapacity(List<com.lens.cart.entity.CartItems> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return;
        }

        java.util.Map<Integer, List<com.lens.cart.entity.CartItems>> itemsByModel = new java.util.HashMap<>();

        for (com.lens.cart.entity.CartItems item : cartItems) {
            if (item.getDeviceModelId() != null && item.getDeviceModelId().getId() != null) {
                Integer deviceModelId = item.getDeviceModelId().getId();
                itemsByModel.computeIfAbsent(deviceModelId, key -> new java.util.ArrayList<>()).add(item);
            }
        }

        for (java.util.Map.Entry<Integer, List<com.lens.cart.entity.CartItems>> entry : itemsByModel.entrySet()) {
            Integer deviceModelId = entry.getKey();
            List<com.lens.cart.entity.CartItems> modelCartItems = entry.getValue();

            if (modelCartItems.size() <= 1) {
                continue;
            }

            Date earliestStart = modelCartItems.get(0).getStartDate();
            Date latestEnd = modelCartItems.get(0).getEndDate();

            for (com.lens.cart.entity.CartItems item : modelCartItems) {
                if (item.getStartDate().before(earliestStart)) {
                    earliestStart = item.getStartDate();
                }

                if (item.getEndDate().after(latestEnd)) {
                    latestEnd = item.getEndDate();
                }
            }

            AvailabilityResult availability = checkAvailability(deviceModelId, earliestStart, latestEnd);
            int totalPhysicalDevices = availability.getTotalPhysicalDevices();

            List<RentalItems> conflictingRentalItems = rentalItemsFacade.findConflictingRentalItems(deviceModelId,
                    earliestStart, latestEnd, List.copyOf(CAPACITY_CONSUMING_STATUSES));

            if (conflictingRentalItems == null) {
                conflictingRentalItems = Collections.emptyList();
            }

            // create capacity events
            List<CapacityEvent> events = new java.util.ArrayList<>();

            for (RentalItems rentalItem : conflictingRentalItems) {
                events.add(new CapacityEvent(rentalItem.getStartDate(), 1));
                events.add(new CapacityEvent(rentalItem.getEndDate(), -1));
            }

            for (com.lens.cart.entity.CartItems cartItem : modelCartItems) {
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
     * Represents a capacity change at a specific date.
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

