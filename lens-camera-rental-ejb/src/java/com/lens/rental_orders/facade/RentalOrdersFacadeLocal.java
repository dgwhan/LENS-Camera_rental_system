package com.lens.rental_orders.facade;

import com.lens.rental_orders.entity.RentalOrders;
import jakarta.ejb.Local;
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

}
