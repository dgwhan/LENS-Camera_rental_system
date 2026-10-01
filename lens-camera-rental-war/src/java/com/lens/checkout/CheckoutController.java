package com.lens.checkout;

import com.lens.cart.CartItem;
import com.lens.cart.model.CartService;
import com.lens.checkout.service.CheckoutServiceLocal;
import com.lens.common.util.DateUtil;
import com.lens.common.util.FacesUtil;
import com.lens.device_model.entity.DeviceModels;
import com.lens.device_model.facade.DeviceModelsFacadeLocal;
import com.lens.rental_orders.entity.RentalOrders;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Handles customer interactions for the checkout page.
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

    @EJB
    private com.lens.user.facade.UsersFacadeLocal usersFacade;

    @EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    //customer Information
    private String customerName;
    private String customerPhone;
    private String customerAddress;

    //checkout type and direct rental parameters
    private String checkoutType;
    private Integer directModelId;
    private String directStartDateStr;
    private String directEndDateStr;

    //transient representation for direct checkout item
    private CartItem directItem;

    /**
     * Checks if the user is authenticated. If not, redirects to the login page.
     * pre-populates customer name, phone, and address from the authenticated user profile.
     * Initializes direct rental item if type is direct.
     *
     * @return navigation outcome to login page if unauthenticated, null otherwise
     */
    public String checkAuthentication() {
        if (!isAuthenticated()) {
            return "/client/pages/login?faces-redirect=true";
        }

        //pre-fill customer information from user profile if not yet set
        if (customerName == null && customerPhone == null && customerAddress == null) {
            Integer userId = cartService.resolveCurrentUserId();
            if (userId != null && usersFacade != null) {
                com.lens.user.entity.Users user = usersFacade.find(userId);
                if (user != null) {
                    this.customerName = user.getFullName();
                    this.customerPhone = user.getPhone();
                    this.customerAddress = user.getAddress();
                }
            }
        }

        //initialize direct checkout item if parameters are present
        initDirectItemIfNeeded();

        return null;
    }


    /**
     * Initializes the direct rental item model when coming from device detail.
     */
    private void initDirectItemIfNeeded() {
        if (isDirectCheckoutMode() && directItem == null && directModelId != null) {
            DeviceModels model = deviceModelsFacade.find(directModelId);
            if (model != null && directStartDateStr != null && directEndDateStr != null) {
                try {
                    Date startDate = DateUtil.parseDate(directStartDateStr);
                    Date endDate = DateUtil.parseDate(directEndDateStr);

                    if (startDate != null && endDate != null && endDate.after(startDate)) {
                        long diffInMillis = endDate.getTime() - startDate.getTime();
                        int duration = (int) Math.ceil((double) diffInMillis / (1000 * 60 * 60 * 24));
                        if (duration <= 0) {
                            duration = 1;
                        }

                        directItem = new CartItem();
                        directItem.setDeviceModels(model);
                        directItem.setStartDate(startDate);
                        directItem.setEndDate(endDate);
                        directItem.setDuration(duration);
                        directItem.setRentalPriceSnapshot(model.getRentalPrice());
                        directItem.setDepositAmountSnapshot(model.getDepositAmount());
                        directItem.setSubtotal((long) duration * model.getRentalPrice());
                        directItem.setItemTotal(directItem.getSubtotal() + model.getDepositAmount());
                    }
                } catch (ParseException ex) {
                    FacesUtil.addErrorMessage("Invalid direct rental dates.");
                }
            }
        }
    }

    /**
     * Determines whether the current user is authenticated.
     *
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return cartService != null
                && cartService.resolveCurrentUserId() != null;
    }

    /**
     * Checks if this is a direct checkout (from device detail) rather than a cart
     * checkout.
     *
     * @return true if direct checkout
     */
    public boolean isDirectCheckoutMode() {
        return "direct".equalsIgnoreCase(checkoutType);
    }

    /**
     * Checks whether the current checkout session has items to display/process.
     *
     * @return true if there are checkout items
     */
    public boolean hasCheckoutItems() {
        if (isDirectCheckoutMode()) {
            return directItem != null;
        }
        return cartService != null && !cartService.getSelectedCartItems().isEmpty();
    }

    /**
     * Processes the checkout request and creates the rental order.
     * Handles both direct checkout (without touching cart) and cart checkout.
     *
     * @return navigation outcome after successful checkout, null if checkout fails
     */
    public String processCheckout() {
        if (!isAuthenticated()) {
            return "/client/pages/login?faces-redirect=true";
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

        Integer userId = cartService.resolveCurrentUserId();

        try {
            // Update customer profile with any changes made during checkout
            if (userId != null && usersFacade != null) {
                com.lens.user.entity.Users user = usersFacade.find(userId);
                if (user != null) {
                    user.setFullName(customerName.trim());
                    user.setPhone(customerPhone.trim());
                    user.setAddress(customerAddress.trim());
                    user.setUpdatedAt(new Date());
                    usersFacade.edit(user);
                }
            }

            RentalOrders rentalOrder;

            if (isDirectCheckoutMode()) {
                // Direct Checkout: process single device rental without touching cart
                if (directItem == null || directModelId == null) {
                    FacesUtil.addErrorMessage("Direct rental details are missing or invalid.");
                    return null;
                }

                rentalOrder = checkoutService.processDirectCheckout(userId, customerName.trim(), customerPhone.trim(),
                        directModelId, directItem.getStartDate(), directItem.getEndDate());

                if (rentalOrder != null) {
                    // Reset direct item so repeated submissions are prevented
                    this.directItem = null;
                }
            } else {
                // Cart Checkout: process selected cart items
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
                        } catch (NumberFormatException ignored) {}
                    }
                }

                rentalOrder = checkoutService.processCheckout(
                        userId,
                        customerName.trim(),
                        customerPhone.trim(),
                        selectedIds);

                // remove selected items from in-memory cart
                cartService.removeSelectedItems();
            }

            if (rentalOrder == null) {
                FacesUtil.addErrorMessage("Unable to complete checkout.");
                return null;
            }

            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert",
                    "Rental order placed successfully.");
            return "/client/pages/index?faces-redirect=true";

        } catch (IllegalArgumentException | IllegalStateException ex) {
            FacesUtil.addErrorMessage(ex.getMessage());
            return null;
        }
    }

    public List<CartItem> getCartItems() {
        if (isDirectCheckoutMode()) {
            return directItem != null ? Collections.singletonList(directItem) : Collections.emptyList();
        }
        return cartService != null ? cartService.getSelectedCartItems() : Collections.emptyList();
    }

    public int getItemCount() {
        if (isDirectCheckoutMode()) {
            return directItem != null ? 1 : 0;
        }
        return cartService != null ? cartService.getSelectedItemsCount() : 0;
    }

    public long getRentalSubtotal() {
        if (isDirectCheckoutMode()) {
            return directItem != null ? directItem.getSubtotal() : 0L;
        }
        return cartService != null ? cartService.calculateSelectedRentalSubtotal() : 0L;
    }

    public long getDepositTotal() {
        if (isDirectCheckoutMode()) {
            return directItem != null ? directItem.getDepositAmountSnapshot() : 0L;
        }
        return cartService != null ? cartService.calculateSelectedDepositTotal() : 0L;
    }

    public long getTotalPayable() {
        if (isDirectCheckoutMode()) {
            return directItem != null ? directItem.getItemTotal() : 0L;
        }
        return cartService != null ? cartService.calculateSelectedTotal() : 0L;
    }

    // Getters and Setters
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

    public String getCheckoutType() {
        return checkoutType;
    }

    public void setCheckoutType(String checkoutType) {
        this.checkoutType = checkoutType;
    }

    public Integer getDirectModelId() {
        return directModelId;
    }

    public void setDirectModelId(Integer directModelId) {
        this.directModelId = directModelId;
    }

    public String getDirectStartDateStr() {
        return directStartDateStr;
    }

    public void setDirectStartDateStr(String directStartDateStr) {
        this.directStartDateStr = directStartDateStr;
    }

    public String getDirectEndDateStr() {
        return directEndDateStr;
    }

    public void setDirectEndDateStr(String directEndDateStr) {
        this.directEndDateStr = directEndDateStr;
    }

    public CartItem getDirectItem() {
        return directItem;
    }

    public void setDirectItem(CartItem directItem) {
        this.directItem = directItem;
    }
}
