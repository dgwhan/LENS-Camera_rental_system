package com.lens.rental_orders.facade;

import com.lens.common.facade.AbstractFacade;
import com.lens.rental_orders.entity.RentalOrders;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Date;
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
            query.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
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

    @Override
    public int countCreatedBetween(Date start, Date end) {
        return countCreatedBetweenByStatus(null, start, end);
    }

    @Override
    public int countCreatedBetweenByStatus(String status, Date start, Date end) {
        if (start == null || end == null) {
            return 0;
        }

        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(ro) FROM RentalOrders ro WHERE ro.createdAt >= :start AND ro.createdAt < :end");
        boolean hasStatus = status != null && !status.trim().isEmpty();
        if (hasStatus) {
            jpql.append(" AND ro.status = :status");
        }

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class)
                .setParameter("start", start)
                .setParameter("end", end);
        if (hasStatus) {
            query.setParameter("status", status.trim());
        }

        Long count = query.getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public List<Object[]> countGroupByStatus() {
        return em.createQuery(
                "SELECT ro.status, COUNT(ro) FROM RentalOrders ro GROUP BY ro.status",
                Object[].class).getResultList();
    }

    @Override
    public List<RentalOrders> findByUserId(Integer userId) {
        if (userId == null) {
            return List.of();
        }

        return em.createQuery("SELECT ro FROM RentalOrders ro WHERE ro.userId.id = :userId ORDER BY ro.id DESC", RentalOrders.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public RentalOrders findByIdAndUserId(Integer orderId, Integer userId) {
        if (orderId == null || userId == null) {
            return null;
        }

        List<RentalOrders> orders = em.createQuery("SELECT ro FROM RentalOrders ro WHERE ro.id = :orderId AND ro.userId.id = :userId", RentalOrders.class)
                .setParameter("orderId", orderId)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getResultList();

        return orders.isEmpty() ? null : orders.get(0);
    }

}
