package com.lens.checkout;

import com.lens.auth.UserSessionService;
import com.lens.cart.CartItem;
import com.lens.cart.model.CartService;
import com.lens.checkout.service.CheckoutServiceLocal;
import com.lens.common.util.FacesUtil;
import com.lens.rental_orders.entity.RentalOrders;
import com.lens.user.entity.Users;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles customer presentation interactions for the checkout page
 *
 * @author Duong Ngoc Han
 */
@Named(value = "checkoutController")
@ViewScoped
public class CheckoutController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private CheckoutServiceLocal checkoutService;

    @Inject
    private CartService cartService;

    @Inject
    private UserSessionService userSessionService;

    //Customer form inputs
    private String customerName;
    private String customerPhone;
    private String customerAddress;

    public CheckoutController() {
    }

    public String checkAuthentication() {
        if (!isAuthenticated()) {
            return "/auth/login?faces-redirect=true";
        }

        if (customerName == null && customerPhone == null && customerAddress == null) {
            Users user = userSessionService.getCurrentUser();
            if (user != null) {
                this.customerName = user.getFullName();
                this.customerPhone = user.getPhone();
                this.customerAddress = user.getAddress();
            }
        }

        return null;
    }

    public boolean isAuthenticated() {
        return userSessionService != null && userSessionService.isAuthenticated();
    }


    //checks whether the current checkout session has items to display and process.
    public boolean hasCheckoutItems() {
        return cartService != null && !cartService.getSelectedCartItems().isEmpty();
    }

    public String processCheckout() {
        if (!isAuthenticated()) {
            return "/auth/login?faces-redirect=true";
        }

        if (customerName == null || customerName.trim().isEmpty()) {
            FacesUtil.addErrorMessage("Customer name is required.");
            return null;
        }

        if (customerPhone == null || customerPhone.trim().isEmpty()) {
            FacesUtil.addErrorMessage("Customer phone is required.");
            return null;
        }

        if (customerAddress == null || customerAddress.trim().isEmpty()) {
            FacesUtil.addErrorMessage("Customer address is required.");
            return null;
        }

        Integer userId = userSessionService.getCurrentUserId();
        List<CartItem> selectedItems = cartService.getSelectedCartItems();

        if (selectedItems.isEmpty()) {
            FacesUtil.addErrorMessage("No items selected for checkout.");
            return null;
        }

        List<Integer> selectedIds = new ArrayList<>();
        for (CartItem ci : selectedItems) {
            if (ci.getCartItemId() != null) {
                try {
                    selectedIds.add(Integer.valueOf(ci.getCartItemId()));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        try {
            List<RentalOrders> rentalOrders = checkoutService.processCheckout(userId, customerName.trim(), customerPhone.trim(),  customerAddress.trim(), selectedIds);

            if (rentalOrders == null || rentalOrders.isEmpty()) {
                FacesUtil.addErrorMessage("Unable to complete checkout.");
                return null;
            }

            cartService.removeSelectedItems();

            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert", "Rental order placed successfully.");
            return "/client/pages/index?faces-redirect=true";

        } catch (IllegalArgumentException | IllegalStateException ex) {
            FacesUtil.addErrorMessage(ex.getMessage());
            return null;
        }
    }

    public List<CartItem> getCartItems() {
        return cartService != null ? cartService.getSelectedCartItems() : Collections.emptyList();
    }

    public int getItemCount() {
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }
}
