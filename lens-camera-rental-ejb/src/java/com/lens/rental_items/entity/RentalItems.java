package com.lens.rental_items.entity;

import com.lens.device_model.entity.DeviceModels;
import com.lens.device.entity.Devices;
import com.lens.rental_orders.entity.RentalOrders;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Duong Ngoc Han
 */
@Entity
@Table(name = "RentalItems")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RentalItems.findAll", query = "SELECT r FROM RentalItems r"),
    @NamedQuery(name = "RentalItems.findById", query = "SELECT r FROM RentalItems r WHERE r.id = :id"),
    @NamedQuery(name = "RentalItems.findByStartDate", query = "SELECT r FROM RentalItems r WHERE r.startDate = :startDate"),
    @NamedQuery(name = "RentalItems.findByEndDate", query = "SELECT r FROM RentalItems r WHERE r.endDate = :endDate"),
    @NamedQuery(name = "RentalItems.findByDuration", query = "SELECT r FROM RentalItems r WHERE r.duration = :duration"),
    @NamedQuery(name = "RentalItems.findByRentalPrice", query = "SELECT r FROM RentalItems r WHERE r.rentalPrice = :rentalPrice"),
    @NamedQuery(name = "RentalItems.findByDepositAmount", query = "SELECT r FROM RentalItems r WHERE r.depositAmount = :depositAmount"),
    @NamedQuery(name = "RentalItems.findBySubtotal", query = "SELECT r FROM RentalItems r WHERE r.subtotal = :subtotal")})
public class RentalItems implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    private Date startDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date endDate;
    @Basic(optional = false)
    @NotNull
    @Min(value = 0, message = "Rental duration must not be negative.")
    @Column(name = "duration")
    private int duration;
    @Basic(optional = false)
    @NotNull
    @Min(value = 0, message = "Rental price must not be negative.")
    @Max(value = 9999999999L, message = "Rental price must not exceed 10 digits.")
    @Column(name = "rental_price")
    private long rentalPrice;
    @Basic(optional = false)
    @NotNull
    @Min(value = 0, message = "Deposit amount must not be negative.")
    @Max(value = 9999999999L, message = "Deposit amount must not exceed 10 digits.")
    @Column(name = "deposit_amount")
    private long depositAmount;
    @Basic(optional = false)
    @NotNull
    @Min(value = 0, message = "Subtotal must not be negative.")
    @Max(value = 9999999999L, message = "Subtotal must not exceed 10 digits.")
    @Column(name = "subtotal")
    private long subtotal;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @JoinColumn(name = "device_model_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private DeviceModels deviceModelId;
    @JoinColumn(name = "assigned_device_id", referencedColumnName = "id")
    @ManyToOne
    private Devices assignedDeviceId;
    @JoinColumn(name = "rental_order_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private RentalOrders rentalOrderId;

    public RentalItems() {
    }

    public RentalItems(Integer id) {
        this.id = id;
    }

    public RentalItems(Integer id, Date startDate, Date endDate, int duration, long rentalPrice, long depositAmount, long subtotal) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.rentalPrice = rentalPrice;
        this.depositAmount = depositAmount;
        this.subtotal = subtotal;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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


    public long getRentalPrice() {
        return rentalPrice;
    }

    public void setRentalPrice(long rentalPrice) {
        this.rentalPrice = rentalPrice;
    }

    public long getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(long depositAmount) {
        this.depositAmount = depositAmount;
    }


    public DeviceModels getDeviceModelId() {
        return deviceModelId;
    }

    public void setDeviceModelId(DeviceModels deviceModelId) {
        this.deviceModelId = deviceModelId;
    }

    public Devices getAssignedDeviceId() {
        return assignedDeviceId;
    }

    public void setAssignedDeviceId(Devices assignedDeviceId) {
        this.assignedDeviceId = assignedDeviceId;
    }

    public RentalOrders getRentalOrderId() {
        return rentalOrderId;
    }

    public void setRentalOrderId(RentalOrders rentalOrderId) {
        this.rentalOrderId = rentalOrderId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RentalItems)) {
            return false;
        }
        RentalItems other = (RentalItems) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.lens.rental_items.entity.RentalItems[ id=" + id + " ]";
    }



    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(long subtotal) {
        this.subtotal = subtotal;
    }

}
