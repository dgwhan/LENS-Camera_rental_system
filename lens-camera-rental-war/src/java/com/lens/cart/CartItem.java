package com.lens.cart;

import com.lens.cart.entity.CartItems;
import com.lens.device_model.entity.DeviceModels;
import java.io.Serializable;
import java.util.Date;

/**
 * Represents a rental item in the customer's cart.
 *
 * @author Duong Ngoc Han
 */
public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private DeviceModels deviceModels;
    private String cartItemId;
    private Date startDate;
    private Date endDate;
    private Integer duration;
    private long rentalPriceSnapshot;
    private long depositAmountSnapshot;
    private long subtotal;
    private long itemTotal;

    /**
     * Converts a persistent entity into a cart item UI model.
     *
     * @param entity persistent CartItems entity
     * @return cart item model
     */
    public static CartItem fromEntity(CartItems entity) {
        if (entity == null) {
            return null;
        }
        CartItem item = new CartItem();
        item.updateFromEntity(entity);
        return item;
    }

    /**
     * Updates this cart item's fields from a persistent entity.
     *
     * @param entity persistent CartItems entity
     */
    public void updateFromEntity(CartItems entity) {
        if (entity == null) {
            return;
        }
        this.cartItemId = entity.getId() != null ? String.valueOf(entity.getId()) : null;
        this.deviceModels = entity.getDeviceModelId();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.duration = entity.getDuration();
        this.rentalPriceSnapshot = entity.getRentalPrice();
        this.depositAmountSnapshot = entity.getDepositAmount();
        this.subtotal = (long) entity.getDuration() * entity.getRentalPrice();
        this.itemTotal = this.subtotal + entity.getDepositAmount();
    }

    /**
     * Checks if the rental start date is in the past compared to today.
     *
     * @return true if the rental start date is before today
     */
    public boolean isExpired() {
        if (startDate == null) {
            return false;
        }
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return startDate.before(cal.getTime());
    }

    public boolean getExpired() {
        return isExpired();
    }

    public DeviceModels getDeviceModels() {
        return deviceModels;
    }

    public void setDeviceModels(DeviceModels deviceModels) {
        this.deviceModels = deviceModels;
    }

    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public long getRentalPriceSnapshot() {
        return rentalPriceSnapshot;
    }

    public void setRentalPriceSnapshot(long rentalPriceSnapshot) {
        this.rentalPriceSnapshot = rentalPriceSnapshot;
    }

    public long getDepositAmountSnapshot() {
        return depositAmountSnapshot;
    }

    public void setDepositAmountSnapshot(long depositAmountSnapshot) {
        this.depositAmountSnapshot = depositAmountSnapshot;
    }

    public long getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(long subtotal) {
        this.subtotal = subtotal;
    }

    public long getItemTotal() {
        return itemTotal;
    }

    public void setItemTotal(long itemTotal) {
        this.itemTotal = itemTotal;
    }
}