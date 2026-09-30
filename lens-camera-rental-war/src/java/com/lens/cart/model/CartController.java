package com.lens.cart.model;

import com.lens.cart.CartItem;
import com.lens.common.util.DateUtil;
import com.lens.common.util.FacesUtil;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

/**
 * Handles customer interactions for the rental cart page.
 *
 * @author Duong Ngoc Han
 */
@Named(value = "cartController")
@ViewScoped
public class CartController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private CartService cartService;

    @Inject
    private HttpServletRequest request;

    /**
     * Checks if the user is authenticated. If not, redirects to the login page
     * so that guest users are never shown an empty cart page.
     *
     * @return navigation outcome to login page if unauthenticated, null otherwise
     */
    public String checkAuthentication() {
        if (!isAuthenticated()) {
            return "/client/pages/login?faces-redirect=true";
        }
        return null;
    }

    /**
     * Determines whether the current user is authenticated.
     *
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        if (request != null && request.getUserPrincipal() != null) {
            return true;
        }
        return cartService != null && cartService.resolveCurrentUserId() != null;
    }

    public CartService getCartService() {
        return cartService;
    }

    public void removeItem(String cartItemId) {
        boolean removed = cartService.removeItem(cartItemId);

        if (removed) {
            FacesUtil.addSuccessMessage("Item removed successfully.");
        } else {
            FacesUtil.addErrorMessage("The selected item could not be found.");
        }
    }

    /**
     * Removes all items from the customer's cart.
     */
    public void clearCart() {
        cartService.clearCart();
        FacesUtil.addSuccessMessage("All items removed from cart.");
    }

    /**
     * Backward-compatible alias for clearCart.
     */
    public void clearCard() {
        clearCart();
    }

    /**
     * Updates the rental period for a cart item.
     *
     * @param cartItemId identifier of the cart item
     * @param startDate  new start date
     * @param endDate    new end date
     */
    public void updateRentalPeriod(String cartItemId, Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            FacesUtil.addErrorMessage("Please select valid rental start and end dates.");
            return;
        }

        boolean updated = cartService.updateRentalPeriod(cartItemId, startDate, endDate);

        if (updated) {
            FacesUtil.addSuccessMessage("Rental period updated successfully.");
        } else {
            FacesUtil.addErrorMessage("Unable to update rental period. End date must be after start date.");
        }
    }

    /**
     * Updates the rental period for a cart item using date strings in yyyy-MM-dd
     * format.
     *
     * @param cartItemId   identifier of the cart item
     * @param startDateStr new start date string
     * @param endDateStr   new end date string
     */
    public void updateRentalPeriod(String cartItemId, String startDateStr, String endDateStr) {
        if (startDateStr == null || startDateStr.trim().isEmpty() || endDateStr == null
                || endDateStr.trim().isEmpty()) {
            FacesUtil.addErrorMessage("Please select valid rental start and end dates.");
            return;
        }

        try {
            Date startDate = DateUtil.parseDate(startDateStr);
            Date endDate = DateUtil.parseDate(endDateStr);
            updateRentalPeriod(cartItemId, startDate, endDate);
        } catch (ParseException ex) {
            FacesUtil.addErrorMessage("Invalid date format. Please use valid dates.");
        }
    }

    private String editCartItemId;
    private String editStartDate;
    private String editEndDate;

    public void saveRentalPeriodUpdate() {
        updateRentalPeriod(editCartItemId, editStartDate, editEndDate);
    }

    public String formatDateForInput(Date date) {
        if (date == null) {
            return "";
        }
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    public String formatDisplayDate(Date date) {
        return DateUtil.formatDisplayDate(date);
    }

    public String getEditCartItemId() {
        return editCartItemId;
    }

    public void setEditCartItemId(String editCartItemId) {
        this.editCartItemId = editCartItemId;
    }

    public String getEditStartDate() {
        return editStartDate;
    }

    public void setEditStartDate(String editStartDate) {
        this.editStartDate = editStartDate;
    }

    public String getEditEndDate() {
        return editEndDate;
    }

    public void setEditEndDate(String editEndDate) {
        this.editEndDate = editEndDate;
    }

    public int getItemCount() {
        return cartService.getItemCount();
    }

    public long getRentalSubtotal() {
        return cartService.calculateRentalSubtotal();
    }

    public long getDepositTotal() {
        return cartService.calculateDepositTotal();
    }

    public long getTotalPayable() {
        return cartService.calculateTotalPayable();
    }

    /**
     * Checks if any rental item in the cart has an expired start date.
     *
     * @return true if at least one cart item has a past start date
     */
    public boolean hasExpiredItems() {
        if (cartService == null || cartService.getCartItems() == null) {
            return false;
        }
        for (CartItem item : cartService.getCartItems()) {
            if (item.isExpired()) {
                return true;
            }
        }
        return false;
    }

    public boolean isHasExpiredItems() {
        return hasExpiredItems();
    }

    public boolean getHasExpiredItems() {
        return hasExpiredItems();
    }

    /**
     * Checks whether an individual cart item is currently out of stock.
     *
     * @param item cart item to test
     * @return true if out of stock
     */
    public boolean isItemOutOfStock(CartItem item) {
        if (item == null) {
            return false;
        }
        if (cartService != null) {
            cartService.checkItemAvailability(item);
        }
        return item.isOutOfStock();
    }

    /**
     * Checks if any rental item in the cart is currently out of stock.
     *
     * @return true if at least one cart item is out of stock
     */
    public boolean hasOutOfStockItems() {
        if (cartService == null || cartService.getCartItems() == null) {
            return false;
        }
        for (CartItem item : cartService.getCartItems()) {
            if (isItemOutOfStock(item)) {
                return true;
            }
        }
        return false;
    }

    public boolean isHasOutOfStockItems() {
        return hasOutOfStockItems();
    }

    public boolean getHasOutOfStockItems() {
        return hasOutOfStockItems();
    }

    /**
     * Determines whether checkout should be blocked due to expired dates or out of stock items.
     *
     * @return true if checkout is blocked
     */
    public boolean isCheckoutBlocked() {
        return hasExpiredItems() || hasOutOfStockItems();
    }

    public boolean getCheckoutBlocked() {
        return isCheckoutBlocked();
    }
}

