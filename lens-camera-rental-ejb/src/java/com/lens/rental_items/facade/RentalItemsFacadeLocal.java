package com.lens.rental_items.facade;

import com.lens.rental_items.entity.RentalItems;
import jakarta.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Local
public interface RentalItemsFacadeLocal {

    void create(RentalItems rentalItems);

    void edit(RentalItems rentalItems);

    void remove(RentalItems rentalItems);

    RentalItems find(Object id);

    List<RentalItems> findAll();

    List<RentalItems> findRange(int[] range);

    int count();

    List<RentalItems> findConflictingRentalItems(
            Integer deviceModelId,
            Date requestedStartDate,
            Date requestedEndDate,
            List<String> statuses);

    long countActiveByDeviceModelId(Integer deviceModelId, List<String> statuses);
}

