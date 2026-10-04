package com.lens.rental_orders.service;

import com.lens.device.entity.Devices;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
public interface RentalOrderAssignmentServiceLocal {

    void assignDevice(Integer orderId, Integer deviceId);

    void unassignDevice(Integer orderId);
    
    List<Devices> getAvailableDevices(Integer orderId);
}
