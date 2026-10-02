package com.lens.cart.model;

import com.lens.auth.UserSessionService;
import com.lens.cart.CartItem;
import com.lens.cart.entity.CartItems;
import com.lens.cart.facade.CartItemsFacadeLocal;
import com.lens.common.util.DateUtil;
import com.lens.device_model.entity.DeviceModels;
import com.lens.user.entity.Users;
import com.lens.user.facade.UsersFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provides business operations for the rental cart.
 *
 * @author Duong Ngoc Han
 */
@SessionScoped
public class CartService implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private CartItemsFacadeLocal cartItemsFacade;

    @EJB
    private UsersFacadeLocal usersFacade;

    @Inject
    private UserSessionService userSessionService;

    @Inject
    private CartItemAvailabilityService availabilityService;

    private final List<CartItem> cartItems = new ArrayList<>();
    private Integer currentUserId;

    public void checkItemAvailability(CartItem item) {
        availabilityService.checkItemAvailability(item);
    }

    public Integer resolveCurrentUserId() {
        if (currentUserId != null) {
            return currentUserId;
        }

        if (userSessionService != null) {
            Integer userId = userSessionService.getCurrentUserId();
            if (userId != null) {
                currentUserId = userId;

                // Auto-hydrate cart from database if currently empty
                if (cartItems.isEmpty()) {
                    cartItemsFacade.findByUserId(currentUserId)
                            .stream()
                            .map(CartItem::fromEntity)
                            .forEach(cartItems::add);
                }

                return currentUserId;
            }
        }

        return null;
    }

    public List<CartItem> getCartItems() {
        if (currentUserId == null) {
            resolveCurrentUserId();
        }
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                checkItemAvailability(item);
            }
        }
        return cartItems;
    }


    public void loadUserCart(Integer userId) {
        cartItems.clear();
        currentUserId = userId;

        if (userId != null) {
            cartItemsFacade.findByUserId(userId)
                    .stream()
                    .map(CartItem::fromEntity)
                    .forEach(cartItems::add);
        }
    }

    public boolean addItem(CartItem cartItem) {
        if (cartItem == null || findDuplicateItem(cartItem) != null) {
            return false;
        }
        cartItems.add(cartItem);
        return true;
    }


    public boolean addToCart(DeviceModels deviceModel, Date startDate, Date endDate) {
        Integer userId = resolveCurrentUserId();

        if (userId == null || deviceModel == null || startDate == null || endDate == null) {
            return false;
        }

        int duration = DateUtil.calculateRentalDuration(startDate, endDate);
        if (duration <= 0) {
            return false;
        }

        CartItems persistentItem = cartItemsFacade.createCartItem(userId, deviceModel, startDate, endDate, duration);
        if (persistentItem == null) {
            return false;
        }

        cartItems.add(CartItem.fromEntity(persistentItem));
        return true;
    }

    //updates the rental period and recalculates subtotal for an existing cartitem
    public boolean updateRentalPeriod(String cartItemId, Date startDate, Date endDate) {
        Integer userId = resolveCurrentUserId();

        if (userId == null || cartItemId == null || startDate == null || endDate == null) {
            return false;
        }

        int duration = DateUtil.calculateRentalDuration(startDate, endDate);
        if (duration <= 0) {
            return false;
        }

        CartItems updatedEntity = cartItemsFacade.updateRentalPeriod(Integer.valueOf(cartItemId), userId, startDate,
                endDate, duration);
        if (updatedEntity == null) {
            return false;
        }

        CartItem cartItem = findItemById(cartItemId);
        if (cartItem != null) {
            cartItem.updateFromEntity(updatedEntity);
        }

        return true;
    }

    public boolean removeItem(String cartItemId) {
        Integer userId = resolveCurrentUserId();

        if (userId == null || cartItemId == null) {
            return false;
        }

        boolean deleted = cartItemsFacade.deleteByIdAndUserId(Integer.valueOf(cartItemId), userId);
        if (deleted) {
            cartItems.removeIf(item -> cartItemId.equals(item.getCartItemId()));
            return true;
        }

        return false;
    }

    public void clearCart() {
        Integer userId = resolveCurrentUserId();
        if (userId != null) {
            cartItemsFacade.deleteByUserId(userId);
        }
        cartItems.clear();
    }


    public CartItem findItemById(String cartItemId) {
        if (cartItemId == null) {
            return null;
        }
        return cartItems.stream()
                .filter(item -> cartItemId.equals(item.getCartItemId()))
                .findFirst()
                .orElse(null);
    }


    public CartItem findDuplicateItem(CartItem cartItem) {
        if (cartItem == null || cartItem.getDeviceModels() == null) {
            return null;
        }

        Integer modelId = cartItem.getDeviceModels().getId();

        return cartItems.stream()
                .filter(i -> i.getDeviceModels() != null && modelId != null
                        && modelId.equals(i.getDeviceModels().getId())
                        && sameDate(i.getStartDate(), cartItem.getStartDate())
                        && sameDate(i.getEndDate(), cartItem.getEndDate()))
                .findFirst()
                .orElse(null);
    }

    public CartItem findItemByModelAndDates(Integer modelId, Date startDate, Date endDate) {
        if (modelId == null) {
            return null;
        }
        return cartItems.stream()
                .filter(i -> i.getDeviceModels() != null
                        && modelId.equals(i.getDeviceModels().getId())
                        && sameDate(i.getStartDate(), startDate)
                        && sameDate(i.getEndDate(), endDate))
                .findFirst()
                .orElse(null);
    }

     //selects only the specified target item.
    public void selectOnly(CartItem targetItem) {
        if (targetItem == null) {
            return;
        }
        String targetId = targetItem.getCartItemId();
        for (CartItem item : cartItems) {
            boolean isTarget = targetId != null && targetId.equals(item.getCartItemId());
            item.setSelected(isTarget);
        }
    }

    //returns only the cart items that are currently selected by the customer.
    public List<CartItem> getSelectedCartItems() {
        List<CartItem> allItems = getCartItems();
        if (allItems == null) {
            return java.util.Collections.emptyList();
        }
        List<CartItem> selected = new ArrayList<>();
        for (CartItem item : allItems) {
            if (item.isSelected()) {
                selected.add(item);
            }
        }
        return selected;
    }

    public long calculateSelectedRentalSubtotal() {
        return getSelectedCartItems().stream().mapToLong(CartItem::getSubtotal).sum();
    }

    public long calculateSelectedDepositTotal() {
        return getSelectedCartItems().stream().mapToLong(CartItem::getDepositAmountSnapshot).sum();
    }

    public long calculateSelectedTotal() {
        return calculateSelectedRentalSubtotal() + calculateSelectedDepositTotal();
    }
    //returns the count of selected items in the cart.
    public int getSelectedItemsCount() {
        return getSelectedCartItems().size();
    }

    public void removeSelectedItems() {
        cartItems.removeIf(CartItem::isSelected);
    }

    public long calculateRentalSubtotal() {
        return cartItems.stream().mapToLong(CartItem::getSubtotal).sum();
    }

    public long calculateDepositTotal() {
        return cartItems.stream().mapToLong(CartItem::getDepositAmountSnapshot).sum();
    }

    public long calculateTotalPayable() {
        return calculateRentalSubtotal() + calculateDepositTotal();
    }

    public int getItemCount() {
        return cartItems.size();
    }

    private boolean sameDate(Date firstDate, Date secondDate) {
        if (firstDate == null || secondDate == null) {
            return firstDate == secondDate;
        }
        return firstDate.equals(secondDate);
    }
}