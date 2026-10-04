package com.lens.rental_orders.facade;

import com.lens.rental_orders.entity.RentalOrders;
import jakarta.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Local
public interface RentalOrdersFacadeLocal {

    void create(RentalOrders rentalOrders);

    void edit(RentalOrders rentalOrders);

    void remove(RentalOrders rentalOrders);

    RentalOrders find(Object id);

    List<RentalOrders> findAll();

    List<RentalOrders> findRange(int[] range);

    int count();

    List<RentalOrders> search(String keyword, String status);

    List<RentalOrders> searchOrders(String tab, String keyword, String status);

    int totalRentalOrders();

    int countByStatus(String status);

    int countNeedsActionOrders();

    int countCreatedBetween(Date start, Date end);

    int countCreatedBetweenByStatus(String status, Date start, Date end);

    List<Object[]> countGroupByStatus();

    List<RentalOrders> findByUserId(Integer userId);

    RentalOrders findByIdAndUserId(Integer orderId, Integer userId);

}
