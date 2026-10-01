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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date minimumStartDate = cal.getTime();

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        Date normalizedStart = startCal.getTime();

        if (normalizedStart.before(minimumStartDate)) {
            FacesUtil.addErrorMessage("Rental orders must be placed at least one day before the rental start date.");
            return;
        }

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MILLISECOND, 0);
        Date normalizedEnd = endCal.getTime();

        if (!endDate.after(startDate) || !normalizedEnd.after(normalizedStart)) {
            FacesUtil.addErrorMessage("Rental end date must be after start date.");
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
        return cartService != null ? cartService.getItemCount() : 0;
    }

    public int getSelectedCount() {
        return cartService != null ? cartService.getSelectedItemsCount() : 0;
    }

    public long getRentalSubtotal() {
        return cartService != null ? cartService.calculateSelectedRentalSubtotal() : 0L;
    }

    public long getDepositTotal() {
        return cartService != null ? cartService.calculateSelectedDepositTotal() : 0L;
    }

    public long getTotalPayable() {
        return cartService != null ? cartService.calculateSelectedTotal() : 0L;
    }

    /**
     * Toggles the selection state for a specific cart item.
     *
     * @param cartItemId identifier of the cart item
     */
    public void toggleItemSelection(String cartItemId) {
        if (cartService != null && cartItemId != null) {
            CartItem item = cartService.findItemById(cartItemId);
            if (item != null) {
                item.setSelected(!item.isSelected());
            }
        }
    }

    /**
     * Checks if all items currently in the cart are selected.
     *
     * @return true if cart has items and all of them are selected
     */
    public boolean isAllSelected() {
        if (cartService == null || cartService.getCartItems() == null || cartService.getCartItems().isEmpty()) {
            return false;
        }
        for (CartItem item : cartService.getCartItems()) {
            if (!item.isSelected()) {
                return false;
            }
        }
        return true;
    }

    public boolean getAllSelected() {
        return isAllSelected();
    }

    /**
     * Toggles selection for all items in the cart.
     * If all items are currently selected, deselects all.
     * Otherwise, selects all items.
     */
    public void toggleSelectAll() {
        if (cartService != null && cartService.getCartItems() != null && !cartService.getCartItems().isEmpty()) {
            boolean targetState = !isAllSelected();
            for (CartItem item : cartService.getCartItems()) {
                item.setSelected(targetState);
            }
        }
    }

    /**
     * Checks if any selected rental item has an expired start date.
     *
     * @return true if at least one selected item is expired
     */
    public boolean hasSelectedExpiredItems() {
        if (cartService == null) {
            return false;
        }
        for (CartItem item : cartService.getSelectedCartItems()) {
            if (item.isExpired()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if any selected rental item is currently out of stock.
     *
     * @return true if at least one selected item is out of stock
     */
    public boolean hasSelectedOutOfStockItems() {
        if (cartService == null) {
            return false;
        }
        for (CartItem item : cartService.getSelectedCartItems()) {
            if (isItemOutOfStock(item)) {
                return true;
            }
        }
        return false;
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
     * checks if any rental item in the cart is currently out of stock.
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
     * determines whether checkout should be blocked.
     * blocked if no items are selected, or if any selected item is expired or out of stock.
     *
     * @return true if checkout is blocked
     */
    public boolean isCheckoutBlocked() {
        if (getSelectedCount() == 0) {
            return true;
        }
        return hasSelectedExpiredItems() || hasSelectedOutOfStockItems();
    }

    public boolean getCheckoutBlocked() {
        return isCheckoutBlocked();
    }

    /**
     * returns cart items sorted so that available items appear on top,
     * followed by out-of-stock items and expired items.
     *
     * @return sorted list of cart items
     */
    public List<CartItem> getSortedCartItems() {
        if (cartService == null || cartService.getCartItems() == null) {
            return Collections.emptyList();
        }
        List<CartItem> sortedList = new ArrayList<>(cartService.getCartItems());
        //trigger availability check on all items
        for (CartItem item : sortedList) {
            isItemOutOfStock(item);
        }
        sortedList.sort(Comparator.comparingInt(item -> {
            boolean expired = item.isExpired();
            boolean outOfStock = item.isOutOfStock();
            if (!expired && !outOfStock) {
                return 0; //Available first
            } else if (outOfStock && !expired) {
                return 1; //Out of stock
            } else {
                return 2; //Expired
            }
        }));
        return sortedList;
    }
}

