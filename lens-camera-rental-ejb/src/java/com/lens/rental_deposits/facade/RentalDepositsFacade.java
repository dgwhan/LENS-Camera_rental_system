package com.lens.rental_deposits.facade;

import com.lens.common.facade.AbstractFacade;
import com.lens.rental_deposits.entity.RentalDeposits;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

/**
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class RentalDepositsFacade extends AbstractFacade<RentalDeposits> implements RentalDepositsFacadeLocal {

    @PersistenceContext(unitName = "lens-camera-rental-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public RentalDepositsFacade() {
        super(RentalDeposits.class);
    }

    @Override
    public RentalDeposits findByRentalOrderId(Integer orderId) {
        if (orderId == null) {
            return null;
        }
        try {
            return em.createQuery("SELECT d FROM RentalDeposits d WHERE d.rentalOrderId.id = :orderId", RentalDeposits.class)
                    .setParameter("orderId", orderId)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

}
