package com.lens.rental_payments.facade;

import com.lens.rental_payments.entity.RentalPayments;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Local
public interface RentalPaymentsFacadeLocal {

    void create(RentalPayments rentalPayments);

    void edit(RentalPayments rentalPayments);

    void remove(RentalPayments rentalPayments);

    RentalPayments find(Object id);

    List<RentalPayments> findAll();

    List<RentalPayments> findRange(int[] range);

    int count();

    RentalPayments findByRentalOrderId(Integer orderId);

}
