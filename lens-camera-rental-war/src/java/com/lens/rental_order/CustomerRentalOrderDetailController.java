package com.lens.rental_order;

import com.lens.auth.UserSessionService;
import com.lens.rental_items.entity.RentalItems;
import com.lens.rental_items.facade.RentalItemsFacadeLocal;
import com.lens.rental_orders.entity.RentalOrders;
import com.lens.rental_orders.facade.RentalOrdersFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Duong Ngoc Han
 */
@Named(value = "customerRentalOrderDetailController")
@RequestScoped
public class CustomerRentalOrderDetailController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private RentalOrdersFacadeLocal rentalOrdersFacade;

    @EJB
    private RentalItemsFacadeLocal rentalItemsFacade;

    @Inject
    private UserSessionService userSessionService;

    private RentalOrders rentalOrder;
    private List<RentalItems> rentalItems;
    private boolean orderFound;

    public void init() {
        loadOrder();
    }

    private void loadOrder() {
        Integer userId = userSessionService.getCurrentUserId();
        Integer orderId = getOrderId();

        if (userId == null || orderId == null) {
            rentalOrder = null;
            rentalItems = Collections.emptyList();
            orderFound = false;
            return;
        }

        rentalOrder = rentalOrdersFacade.findByIdAndUserId(orderId, userId);

        if (rentalOrder == null) {
            rentalItems = Collections.emptyList();
            orderFound = false;
            return;
        }

        rentalItems = rentalItemsFacade.findByRentalOrderId(rentalOrder.getId());
        orderFound = true;
    }

    private Integer getOrderId() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (context == null) {
            return null;
        }

        Map<String, String> parameters = context
                .getExternalContext()
                .getRequestParameterMap();

        String value = parameters.get("orderId");

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public RentalOrders getRentalOrder() {
        return rentalOrder;
    }

    public List<RentalItems> getRentalItems() {
        return rentalItems;
    }

    public boolean isOrderFound() {
        return orderFound;
    }
}