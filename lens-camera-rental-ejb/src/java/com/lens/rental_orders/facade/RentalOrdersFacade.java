package com.lens.rental_orders.facade;

import com.lens.common.facade.AbstractFacade;
import com.lens.rental_orders.entity.RentalOrders;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class RentalOrdersFacade extends AbstractFacade<RentalOrders> implements RentalOrdersFacadeLocal {

    @PersistenceContext(unitName = "lens-camera-rental-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public RentalOrdersFacade() {
        super(RentalOrders.class);
    }

}
