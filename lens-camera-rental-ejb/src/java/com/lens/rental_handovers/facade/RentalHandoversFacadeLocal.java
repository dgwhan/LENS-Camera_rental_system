package com.lens.rental_handovers.facade;

import com.lens.rental_handovers.entity.RentalHandovers;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Local
public interface RentalHandoversFacadeLocal {

    void create(RentalHandovers rentalHandovers);

    void edit(RentalHandovers rentalHandovers);

    void remove(RentalHandovers rentalHandovers);

    RentalHandovers find(Object id);

    List<RentalHandovers> findAll();

    List<RentalHandovers> findRange(int[] range);

    int count();

    RentalHandovers findByRentalOrderId(Integer orderId);

}
