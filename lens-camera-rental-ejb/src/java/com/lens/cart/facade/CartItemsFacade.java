package com.lens.cart.facade;

import com.lens.cart.entity.CartItems;
import com.lens.common.facade.AbstractFacade;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

/**
 * Provides persistence operations for cart items.
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class CartItemsFacade extends AbstractFacade<CartItems> implements CartItemsFacadeLocal {

    @PersistenceContext(unitName = "lens-camera-rental-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CartItemsFacade() {
        super(CartItems.class);
    }

    /**
     * Finds all cart items belonging to a user.
     *
     * @param userId user identifier
     * @return list of cart items
     */
    @Override
    public List<CartItems> findByUserId(Integer userId) {
        if (userId == null) {
            return java.util.Collections.emptyList();
        }

        return em.createQuery("SELECT c FROM CartItems c WHERE c.userId.id = :userId ORDER BY c.id DESC", CartItems.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * Finds a cart item with the same user, device model, and rental period.
     *
     * @param userId user identifier
     * @param deviceModelId device model identifier
     * @param startDate rental start date
     * @param endDate rental end date
     * @return matching cart item or null if not found
     */
    @Override
    public CartItems findDuplicate(Integer userId, Integer deviceModelId, Date startDate, Date endDate) {
        if (userId == null || deviceModelId == null || startDate == null || endDate == null) {
            return null;
        }

        List<CartItems> results = em.createQuery("SELECT c FROM CartItems c WHERE c.userId.id = :userId AND c.deviceModelId.id = :deviceModelId AND c.startDate = :startDate AND c.endDate = :endDate", CartItems.class)
                .setParameter("userId", userId)
                .setParameter("deviceModelId", deviceModelId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setMaxResults(1)
                .getResultList();

        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Creates and persists a new cart item if no duplicate exists.
     *
     * @param userId user identifier
     * @param deviceModel device model
     * @param startDate start date
     * @param endDate end date
     * @param duration rental duration in days
     * @return newly created cart item entity, or null if duplicate
     */
    @Override
    public CartItems createCartItem(Integer userId, com.lens.device_model.entity.DeviceModels deviceModel, Date startDate, Date endDate, int duration) {
        if (userId == null || deviceModel == null || startDate == null || endDate == null || duration <= 0) {
            return null;
        }

        if (findDuplicate(userId, deviceModel.getId(), startDate, endDate) != null) {
            return null;
        }

        CartItems item = new CartItems();
        item.setUserId(new com.lens.user.entity.Users(userId));
        item.setDeviceModelId(deviceModel);
        item.setStartDate(startDate);
        item.setEndDate(endDate);
        item.setDuration(duration);
        item.setRentalPrice(deviceModel.getRentalPrice());
        item.setDepositAmount(deviceModel.getDepositAmount());

        Date now = new Date();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);

        create(item);
        return item;
    }

    /**
     * Atomically deletes a cart item scoped to the owning user.
     *
     * @param id cart item identifier
     * @param userId user identifier
     * @return true if an item was deleted
     */
    @Override
    public boolean deleteByIdAndUserId(Integer id, Integer userId) {
        if (id == null || userId == null) {
            return false;
        }

        int deletedCount = em.createQuery("DELETE FROM CartItems c WHERE c.id = :id AND c.userId.id = :userId")
                .setParameter("id", id)
                .setParameter("userId", userId)
                .executeUpdate();

        return deletedCount > 0;
    }

    /**
     * Atomically clears all cart items belonging to a user.
     *
     * @param userId user identifier
     */
    @Override
    public void deleteByUserId(Integer userId) {
        if (userId == null) {
            return;
        }

        em.createQuery("DELETE FROM CartItems c WHERE c.userId.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    /**
     * Updates rental period dates and duration for an existing cart item.
     *
     * @param id cart item identifier
     * @param userId user identifier
     * @param startDate new start date
     * @param endDate new end date
     * @param duration new duration in days
     * @return updated entity or null if not found or unauthorized
     */
    @Override
    public CartItems updateRentalPeriod(Integer id, Integer userId, Date startDate, Date endDate, int duration) {
        if (id == null || userId == null || startDate == null || endDate == null || duration <= 0) {
            return null;
        }

        CartItems item = find(id);

        if (item == null || item.getUserId() == null || !userId.equals(item.getUserId().getId())) {
            return null;
        }

        item.setStartDate(startDate);
        item.setEndDate(endDate);
        item.setDuration(duration);
        item.setUpdatedAt(new Date());

        edit(item);
        return item;
    }
}