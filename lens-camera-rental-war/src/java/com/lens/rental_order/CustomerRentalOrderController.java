package com.lens.rental_order;

import com.lens.auth.UserSessionService;
import com.lens.rental_orders.entity.RentalOrders;
import com.lens.rental_orders.facade.RentalOrdersFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Named(value = "customerRentalOrderController")
@RequestScoped
public class CustomerRentalOrderController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private RentalOrdersFacadeLocal rentalOrdersFacade;

    @Inject
    private UserSessionService userSessionService;

    private List<RentalOrders> orders;

    public void init() {
        loadOrders();
    }

    private void loadOrders() {
        Integer userId = userSessionService.getCurrentUserId();

        if (userId == null) {
            orders = Collections.emptyList();
            return;
        }

        orders = rentalOrdersFacade.findByUserId(userId);
    }

    public List<RentalOrders> getOrders() {
        if (orders == null) {
            loadOrders();
        }

        return orders;
    }
}