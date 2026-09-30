package com.lens.cart.entity;

import com.lens.device_model.entity.DeviceModels;
import com.lens.user.entity.Users;
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
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Duong Ngoc Han
 */
@Entity
@Table(name = "CartItems")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CartItems.findAll", query = "SELECT c FROM CartItems c"),
    @NamedQuery(name = "CartItems.findById", query = "SELECT c FROM CartItems c WHERE c.id = :id"),
    @NamedQuery(name = "CartItems.findByStartDate", query = "SELECT c FROM CartItems c WHERE c.startDate = :startDate"),
    @NamedQuery(name = "CartItems.findByEndDate", query = "SELECT c FROM CartItems c WHERE c.endDate = :endDate"),
    @NamedQuery(name = "CartItems.findByDuration", query = "SELECT c FROM CartItems c WHERE c.duration = :duration"),
    @NamedQuery(name = "CartItems.findByRentalPrice", query = "SELECT c FROM CartItems c WHERE c.rentalPrice = :rentalPrice"),
    @NamedQuery(name = "CartItems.findByDepositAmount", query = "SELECT c FROM CartItems c WHERE c.depositAmount = :depositAmount"),
    @NamedQuery(name = "CartItems.findByCreatedAt", query = "SELECT c FROM CartItems c WHERE c.createdAt = :createdAt"),
    @NamedQuery(name = "CartItems.findByUpdatedAt", query = "SELECT c FROM CartItems c WHERE c.updatedAt = :updatedAt")})
public class CartItems implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
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
    @Column(name = "duration")
    private int duration;
    @Basic(optional = false)
    @NotNull
    @Column(name = "rental_price")
    private long rentalPrice;
    @Basic(optional = false)
    @NotNull
    @Column(name = "deposit_amount")
    private long depositAmount;
    @Basic(optional = false)
    @NotNull
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Basic(optional = false)
    @NotNull
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @JoinColumn(name = "device_model_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private DeviceModels deviceModelId;
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Users userId;

    public CartItems() {
    }

    public CartItems(Integer id) {
        this.id = id;
    }

    public CartItems(Integer id, Date startDate, Date endDate, int duration, long rentalPrice, long depositAmount, Date createdAt, Date updatedAt) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.rentalPrice = rentalPrice;
        this.depositAmount = depositAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public DeviceModels getDeviceModelId() {
        return deviceModelId;
    }

    public void setDeviceModelId(DeviceModels deviceModelId) {
        this.deviceModelId = deviceModelId;
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
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
        if (!(object instanceof CartItems)) {
            return false;
        }
        CartItems other = (CartItems) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.lens.cart.entity.CartItems[ id=" + id + " ]";
    }

}
