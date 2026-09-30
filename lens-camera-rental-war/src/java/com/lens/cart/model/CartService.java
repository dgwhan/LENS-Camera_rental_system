package com.lens.cart.model;

import com.lens.cart.CartItem;
import com.lens.cart.entity.CartItems;
import com.lens.cart.facade.CartItemsFacadeLocal;
import com.lens.device_model.entity.DeviceModels;
import com.lens.user.entity.Users;
import com.lens.user.facade.UsersFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
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

    @EJB
    private com.lens.availability.service.AvailabilityServiceLocal availabilityService;

    private final List<CartItem> cartItems = new ArrayList<>();
    private Integer currentUserId;

    /**
     * Checks and updates the outOfStock status for a cart item based on current model availability
     * and rental dates.
     *
     * @param item cart item to check
     */
    public void checkItemAvailability(CartItem item) {
        if (item == null || item.getDeviceModels() == null || availabilityService == null) {
            return;
        }
        Integer modelId = item.getDeviceModels().getId();
        if (modelId == null) {
            return;
        }

        boolean available = availabilityService.isProductAvailable(modelId);
        if (available && item.getStartDate() != null && item.getEndDate() != null) {
            com.lens.availability.dto.AvailabilityResult result = availabilityService.checkAvailability(
                    modelId, item.getStartDate(), item.getEndDate());
            if (result != null && !result.isAvailable()) {
                available = false;
            }
        }
        item.setOutOfStock(!available);
    }

    /**
     * Resolves the current authenticated user identifier.
     * Recovers from the request principal if the session-scoped ID is null.
     *
     * @return current user identifier, or null if unauthenticated
     */
    public Integer resolveCurrentUserId() {
        if (currentUserId != null) {
            return currentUserId;
        }

        try {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null && context.getExternalContext() != null) {
                HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
                if (request != null && request.getUserPrincipal() != null) {
                    String username = request.getUserPrincipal().getName();
                    if (username != null && !username.trim().isEmpty() && usersFacade != null) {
                        Users user = usersFacade.findByUsername(username);
                        if (user != null) {
                            currentUserId = user.getId();

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
                }
            }
        } catch (Exception ex) {
            // Ignore recovery failure and return null
        }

        return null;
    }

    /**
     * Returns all items currently stored in the customer's cart.
     *
     * @return list of cart items
     */
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


    /**
     * Loads the authenticated user's cart from database.
     *
     * @param userId authenticated user identifier
     */
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

    /**
     * Adds an in-memory cart item if not duplicate.
     *
     * @param cartItem rental item to add
     * @return true if added
     */
    public boolean addItem(CartItem cartItem) {
        if (cartItem == null || findDuplicateItem(cartItem) != null) {
            return false;
        }
        cartItems.add(cartItem);
        return true;
    }

    /**
     * Creates and adds a rental item to the customer's persistent cart.
     *
     * @param deviceModel selected device model
     * @param startDate   rental start date
     * @param endDate     rental end date
     * @return true if the item was added successfully
     */
    public boolean addToCart(DeviceModels deviceModel, Date startDate, Date endDate) {
        Integer userId = resolveCurrentUserId();

        if (userId == null || deviceModel == null || startDate == null || endDate == null) {
            return false;
        }

        int duration = (int) ((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
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

    /**
     * Updates the rental period and recalculates subtotal for an existing cart
     * item.
     *
     * @param cartItemId identifier of the cart item
     * @param startDate  new rental start date
     * @param endDate    new rental end date
     * @return true if updated successfully
     */
    public boolean updateRentalPeriod(String cartItemId, Date startDate, Date endDate) {
        Integer userId = resolveCurrentUserId();

        if (userId == null || cartItemId == null || startDate == null || endDate == null) {
            return false;
        }

        int duration = (int) ((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
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

    /**
     * Removes a rental item from the persistent cart by its identifier.
     *
     * @param cartItemId identifier of the cart item
     * @return true if an item was removed
     */
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

    /**
     * Atomically removes all items from the customer's persistent cart.
     */
    public void clearCart() {
        Integer userId = resolveCurrentUserId();
        if (userId != null) {
            cartItemsFacade.deleteByUserId(userId);
        }
        cartItems.clear();
    }

    /**
     * Finds a cart item by its identifier.
     *
     * @param cartItemId identifier of the cart item
     * @return matching cart item or null if not found
     */
    public CartItem findItemById(String cartItemId) {
        if (cartItemId == null) {
            return null;
        }
        return cartItems.stream()
                .filter(item -> cartItemId.equals(item.getCartItemId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds an existing item with the same device model and rental period.
     *
     * @param cartItem cart item to compare
     * @return matching cart item or null if not found
     */
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

    /**
     * Calculates the total rental cost of all cart items.
     *
     * @return rental subtotal in VND
     */
    public long calculateRentalSubtotal() {
        return cartItems.stream().mapToLong(CartItem::getSubtotal).sum();
    }

    /**
     * Calculates the total deposit amount of all cart items.
     *
     * @return total deposit amount in VND
     */
    public long calculateDepositTotal() {
        return cartItems.stream().mapToLong(CartItem::getDepositAmountSnapshot).sum();
    }

    /**
     * Calculates the total amount payable for the cart.
     *
     * @return total payable amount in VND
     */
    public long calculateTotalPayable() {
        return calculateRentalSubtotal() + calculateDepositTotal();
    }

    /**
     * Returns the number of items currently stored in the cart.
     *
     * @return cart item count
     */
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