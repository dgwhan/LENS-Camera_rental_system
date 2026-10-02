package com.lens.rental_payments.facade;

import com.lens.common.facade.AbstractFacade;
import com.lens.rental_payments.entity.RentalPayments;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

/**
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class RentalPaymentsFacade extends AbstractFacade<RentalPayments> implements RentalPaymentsFacadeLocal {

    @PersistenceContext(unitName = "lens-camera-rental-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public RentalPaymentsFacade() {
        super(RentalPayments.class);
    }

    @Override
    public RentalPayments findByRentalOrderId(Integer orderId) {
        if (orderId == null) {
            return null;
        }
        try {
            return em.createQuery("SELECT p FROM RentalPayments p WHERE p.rentalOrderId.id = :orderId", RentalPayments.class)
                    .setParameter("orderId", orderId)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

}
