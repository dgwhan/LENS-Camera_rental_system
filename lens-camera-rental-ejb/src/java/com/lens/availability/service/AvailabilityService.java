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

}

