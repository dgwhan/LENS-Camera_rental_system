package com.lens.rental_deposits.facade;

import com.lens.rental_deposits.entity.RentalDeposits;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Local
public interface RentalDepositsFacadeLocal {

    void create(RentalDeposits rentalDeposits);

    void edit(RentalDeposits rentalDeposits);

    void remove(RentalDeposits rentalDeposits);

    RentalDeposits find(Object id);

    List<RentalDeposits> findAll();

    List<RentalDeposits> findRange(int[] range);

    int count();

    RentalDeposits findByRentalOrderId(Integer orderId);

}
