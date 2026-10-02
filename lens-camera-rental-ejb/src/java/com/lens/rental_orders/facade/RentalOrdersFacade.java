package com.lens.rental_orders.facade;

import com.lens.common.facade.AbstractFacade;
import com.lens.rental_orders.entity.RentalOrders;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

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

    @Override
    public List<RentalOrders> search(String keyword, String status) {
        StringBuilder jpql = new StringBuilder("SELECT r FROM RentalOrders r WHERE 1=1 ");

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty();

        if (hasKeyword) {
            jpql.append(" AND (LOWER(r.customerName) LIKE :keyword ")
                    .append(" OR LOWER(r.customerPhone) LIKE :keyword)");
        }

        if (hasStatus) {
            jpql.append(" AND r.status = :status");
        }

        jpql.append(" ORDER BY r.id DESC");

        TypedQuery<RentalOrders> query = em.createQuery(jpql.toString(), RentalOrders.class);

        if (hasKeyword) {
            query.setParameter("keyword","%" + keyword.trim().toLowerCase() + "%");
        }

        if (hasStatus) {
            query.setParameter("status", status.trim());
        }

        return query.getResultList();
    }

    @Override
    public int totalRentalOrders() {
        Long count = em.createQuery("SELECT COUNT(ro) FROM RentalOrders ro", Long.class).getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public List<RentalOrders> searchOrders(String tab, String keyword, String status) {
        StringBuilder jpql = new StringBuilder("SELECT r FROM RentalOrders r WHERE 1=1 ");

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty();

        if (hasKeyword) {
            jpql.append(" AND (LOWER(r.customerName) LIKE :keyword ")
                    .append(" OR LOWER(r.customerPhone) LIKE :keyword)");
        }

        if ("needs_action".equalsIgnoreCase(tab)) {
            if (hasStatus) {
                jpql.append(" AND r.status = :status");
            } else {
                jpql.append(" AND r.status IN ('PENDING', 'APPROVED')");
            }
        } else if ("active".equalsIgnoreCase(tab)) {
            jpql.append(" AND r.status = 'ACTIVE'");
        } else {
            if (hasStatus) {
                jpql.append(" AND r.status = :status");
            }
        }

        jpql.append(" ORDER BY r.id DESC");

        TypedQuery<RentalOrders> query = em.createQuery(jpql.toString(), RentalOrders.class);

        if (hasKeyword) {
            query.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }

        if (hasStatus && !"active".equalsIgnoreCase(tab)) {
            query.setParameter("status", status.trim());
        }

        return query.getResultList();
    }

    @Override
    public int countByStatus(String status) {
        Long count = em.createQuery("SELECT COUNT(ro) FROM RentalOrders ro WHERE ro.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public int countNeedsActionOrders() {
        Long count = em.createQuery("SELECT COUNT(ro) FROM RentalOrders ro WHERE ro.status IN ('PENDING', 'APPROVED')", Long.class)
                .getSingleResult();
        return count != null ? count.intValue() : 0;
    }

}
