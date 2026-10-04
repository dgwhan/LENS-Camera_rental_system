package com.lens.rental_order;

import com.lens.common.util.ImageUtil;
import com.lens.rental_deposits.entity.RentalDeposits;
import com.lens.rental_deposits.facade.RentalDepositsFacadeLocal;
import com.lens.rental_handovers.entity.RentalHandovers;
import com.lens.rental_handovers.facade.RentalHandoversFacadeLocal;
import com.lens.rental_items.entity.RentalItems;
import com.lens.rental_items.facade.RentalItemsFacadeLocal;
import com.lens.rental_orders.entity.RentalOrders;
import com.lens.rental_orders.facade.RentalOrdersFacadeLocal;
import com.lens.rental_payments.entity.RentalPayments;
import com.lens.rental_payments.facade.RentalPaymentsFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Admin Rental Order management.
 *
 * @author Duong Ngoc Han
 */
@Named(value = "adminRentalOrderController")
@ViewScoped
public class AdminRentalOrderController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private RentalOrdersFacadeLocal rentalOrdersFacade;

    @EJB
    private RentalItemsFacadeLocal rentalItemsFacade;

    @EJB
    private RentalHandoversFacadeLocal rentalHandoversFacade;

    @EJB
    private RentalPaymentsFacadeLocal rentalPaymentsFacade;

    @EJB
    private RentalDepositsFacadeLocal rentalDepositsFacade;

    private RentalOrders rentalOrders = new RentalOrders();
    private RentalOrders selectedOrder;
    private RentalItems selectedRentalItem;
    private RentalHandovers selectedHandover;
    private RentalPayments selectedPayment;
    private RentalDeposits selectedDeposit;
    private Integer id;
    private boolean editMode;
    private String keyword = "";
    private String status = "";
    private String currentTab = "needs_action";

    private final Map<Integer, RentalItems> rentalItemsMap = new HashMap<>();
    private final Map<Integer, RentalPayments> rentalPaymentsMap = new HashMap<>();

    public AdminRentalOrderController() {
    }

    public RentalOrders getRentalOrders() {
        return rentalOrders;
    }

    public void setRentalOrders(RentalOrders rentalOrders) {
        this.rentalOrders = rentalOrders;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<RentalOrders> showAllRentalOrder() {
        return rentalOrdersFacade.findAll();
    }

    public List<RentalOrders> showRentalOrderList() {
        // get orders based on current tab and filters
        List<RentalOrders> orders = rentalOrdersFacade.searchOrders(currentTab, keyword, status);

        if (orders != null && !orders.isEmpty()) {
            List<Integer> orderIds = new ArrayList<>();

            // collect order ids
            for (RentalOrders order : orders) {
                if (order.getId() != null) {
                    orderIds.add(order.getId());
                }
            }

            // load rental items for the orders
            List<RentalItems> items = rentalItemsFacade.findByRentalOrderIds(orderIds);

            rentalItemsMap.clear();

            // map rental items by order id
            for (RentalItems item : items) {
                if (item.getRentalOrderId() != null
                        && !rentalItemsMap.containsKey(
                                item.getRentalOrderId().getId())) {

                    rentalItemsMap.put(
                            item.getRentalOrderId().getId(),
                            item);
                }
            }
        } else {
            // clear cache when there are no orders
            rentalItemsMap.clear();
        }

        return orders;
    }

    public RentalItems getRentalItem(Integer rentalOrderId) {
        if (rentalOrderId == null) {
            return null;
        }

        if (rentalItemsMap.containsKey(rentalOrderId)) {
            return rentalItemsMap.get(rentalOrderId);
        }

        List<RentalItems> rentalItems = rentalItemsFacade.findByRentalOrderId(rentalOrderId);

        if (rentalItems.isEmpty()) {
            return null;
        }

        RentalItems item = rentalItems.get(0);
        rentalItemsMap.put(rentalOrderId, item);

        return item;
    }

    public boolean isDeviceAssigned(Integer orderId) {
        RentalItems item = getRentalItem(orderId);
        return item != null && item.getAssignedDeviceId() != null;
    }

    public boolean isPreparationOpen() {
        if (selectedRentalItem == null || selectedRentalItem.getStartDate() == null) {
            return false;
        }

        LocalDate startDate = selectedRentalItem.getStartDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate preparationDate = startDate.minusDays(1);
        LocalDate today = LocalDate.now();

        return !today.isBefore(preparationDate);
    }

    public boolean isHandoverOpen() {
        return isPreparationOpen()
                && selectedRentalItem != null
                && selectedRentalItem.getAssignedDeviceId() != null;
    }

    public boolean isOverdue(Integer orderId) {
        RentalItems item = getRentalItem(orderId);

        if (item == null || item.getEndDate() == null) {
            return false;
        }

        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);

        Date today = todayCal.getTime();

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(item.getEndDate());
        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MILLISECOND, 0);

        Date normalizedEnd = endCal.getTime();

        return normalizedEnd.before(today);
    }

    public void changeTab(String tab) {
        this.currentTab = tab;
        this.status = "";
    }

    public void resetFilter() {
        this.keyword = "";
        this.status = "";
    }

    public RentalPayments getRentalPayment(Integer rentalOrderId) {
        if (rentalOrderId == null) {
            return null;
        }

        if (rentalPaymentsMap.containsKey(rentalOrderId)) {
            return rentalPaymentsMap.get(rentalOrderId);
        }

        RentalPayments payment = rentalPaymentsFacade.findByRentalOrderId(rentalOrderId);

        if (payment != null) {
            rentalPaymentsMap.put(rentalOrderId, payment);
        }

        return payment;
    }

    public String getPaymentStatus(Integer rentalOrderId) {
        RentalPayments payment = getRentalPayment(rentalOrderId);
        return payment != null
                ? payment.getPaymentStatus()
                : "UNPAID";
    }

    public String getDeviceImageUrl(String imageUrl) {
        return ImageUtil.getDeviceImageUrl(imageUrl);
    }

    public int getTotalRentalOrders() {
        return rentalOrdersFacade.totalRentalOrders();
    }

    public int getTotalNeedsActionOrders() {
        return rentalOrdersFacade.countNeedsActionOrders();
    }

    public int getTotalPendingOrders() {
        return rentalOrdersFacade.countByStatus("PENDING");
    }

    public int getTotalActiveOrders() {
        return rentalOrdersFacade.countByStatus("ACTIVE");
    }

    public int getTotalCompletedOrders() {
        return rentalOrdersFacade.countByStatus("COMPLETED");
    }

    public String getCurrentTab() {
        return currentTab;
    }

    public void setCurrentTab(String currentTab) {
        this.currentTab = currentTab;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String openDetail(Integer orderId) {
        return "/admin/rentalorders-management/detail.xhtml" + "?id=" + orderId + "&faces-redirect=true";
    }

    public void loadDetail() {
        if (id == null) {
            closeDetail();
            return;
        }

        this.selectedOrder = rentalOrdersFacade.find(id);

        if (this.selectedOrder == null) {
            closeDetail();
            return;
        }

        this.selectedRentalItem = getRentalItem(id);
        this.selectedHandover = rentalHandoversFacade.findByRentalOrderId(id);
        this.selectedPayment = rentalPaymentsFacade.findByRentalOrderId(id);
        this.selectedDeposit = rentalDepositsFacade.findByRentalOrderId(id);
    }

    public void closeDetail() {
        this.selectedOrder = null;
        this.selectedRentalItem = null;
        this.selectedHandover = null;
        this.selectedPayment = null;
        this.selectedDeposit = null;
    }

    public RentalOrders getSelectedOrder() {
        return selectedOrder;
    }

    public void setSelectedOrder(RentalOrders selectedOrder) {
        this.selectedOrder = selectedOrder;
    }

    public RentalItems getSelectedRentalItem() {
        return selectedRentalItem;
    }

    public void setSelectedRentalItem(RentalItems selectedRentalItem) {
        this.selectedRentalItem = selectedRentalItem;
    }

    public RentalHandovers getSelectedHandover() {
        return selectedHandover;
    }

    public void setSelectedHandover(RentalHandovers selectedHandover) {
        this.selectedHandover = selectedHandover;
    }

    public RentalPayments getSelectedPayment() {
        return selectedPayment;
    }

    public void setSelectedPayment(RentalPayments selectedPayment) {
        this.selectedPayment = selectedPayment;
    }

    public RentalDeposits getSelectedDeposit() {
        return selectedDeposit;
    }

    public void setSelectedDeposit(RentalDeposits selectedDeposit) {
        this.selectedDeposit = selectedDeposit;
    }
}
