package com.lens.rental_items.facade;

import com.lens.common.facade.AbstractFacade;
import com.lens.rental_items.entity.RentalItems;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class RentalItemsFacade extends AbstractFacade<RentalItems> implements RentalItemsFacadeLocal {

    @PersistenceContext(unitName = "lens-camera-rental-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public RentalItemsFacade() {
        super(RentalItems.class);
    }

    @Override
    public List<RentalItems> findConflictingRentalItems(Integer deviceModelId, Date requestedStartDate, Date requestedEndDate, List<String> statuses) {
        if (deviceModelId == null || requestedStartDate == null || requestedEndDate == null || statuses == null || statuses.isEmpty()) {
            return Collections.emptyList();
        }

        String jpql = "SELECT ri FROM RentalItems ri WHERE ri.deviceModelId.id = :deviceModelId AND ri.rentalOrderId.status IN :statuses AND ri.startDate < :requestedEndDate AND ri.endDate > :requestedStartDate";

        TypedQuery<RentalItems> query = em.createQuery(jpql, RentalItems.class)
                .setParameter("deviceModelId", deviceModelId)
                .setParameter("requestedStartDate", requestedStartDate)
                .setParameter("requestedEndDate", requestedEndDate)
                .setParameter("statuses", statuses);

        return query.getResultList();
    }

    @Override
    public long countActiveByDeviceModelId(Integer deviceModelId, List<String> statuses) {
        if (deviceModelId == null || statuses == null || statuses.isEmpty()) {
            return 0L;
        }

        String jpql = "SELECT COUNT(ri) FROM RentalItems ri WHERE ri.deviceModelId.id = :deviceModelId AND ri.rentalOrderId.status IN :statuses";

        TypedQuery<Long> query = em.createQuery(jpql, Long.class)
                .setParameter("deviceModelId", deviceModelId)
                .setParameter("statuses", statuses);

        Long count = query.getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    public List<RentalItems> findByRentalOrderId(Integer rentalOrderId) {
        return em.createQuery("SELECT ri FROM RentalItems ri WHERE ri.rentalOrderId.id = :rentalOrderId",RentalItems.class)
                .setParameter("rentalOrderId", rentalOrderId)
                .getResultList();
    }

    @Override
    public List<RentalItems> findByRentalOrderIds(List<Integer> rentalOrderIds) {
        if (rentalOrderIds == null || rentalOrderIds.isEmpty()) {
            return Collections.emptyList();
        }
        return em.createQuery("SELECT ri FROM RentalItems ri JOIN FETCH ri.deviceModelId LEFT JOIN FETCH ri.assignedDeviceId WHERE ri.rentalOrderId.id IN :rentalOrderIds", RentalItems.class)
                .setParameter("rentalOrderIds", rentalOrderIds)
                .getResultList();
    }

}
