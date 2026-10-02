package com.lens.rental_handovers.facade;

import com.lens.common.facade.AbstractFacade;
import com.lens.rental_handovers.entity.RentalHandovers;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class RentalHandoversFacade extends AbstractFacade<RentalHandovers> implements RentalHandoversFacadeLocal {

    @PersistenceContext(unitName = "lens-camera-rental-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public RentalHandoversFacade() {
        super(RentalHandovers.class);
    }

    @Override
    public RentalHandovers findByRentalOrderId(Integer orderId) {
        if (orderId == null) {
            return null;
        }
        try {
            return em.createQuery("SELECT h FROM RentalHandovers h WHERE h.rentalOrderId.id = :orderId", RentalHandovers.class)
                    .setParameter("orderId", orderId)
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException ex) {
            return null;
        }
    }

}
