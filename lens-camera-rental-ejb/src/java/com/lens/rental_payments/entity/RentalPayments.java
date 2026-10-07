package com.lens.rental_payments.entity;

import com.lens.rental_orders.entity.RentalOrders;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Duong Ngoc Han
 */
@Entity
@Table(name = "RentalPayments")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RentalPayments.findAll", query = "SELECT r FROM RentalPayments r"),
    @NamedQuery(name = "RentalPayments.findById", query = "SELECT r FROM RentalPayments r WHERE r.id = :id"),
    @NamedQuery(name = "RentalPayments.findByPaymentMethod", query = "SELECT r FROM RentalPayments r WHERE r.paymentMethod = :paymentMethod"),
    @NamedQuery(name = "RentalPayments.findByPaymentStatus", query = "SELECT r FROM RentalPayments r WHERE r.paymentStatus = :paymentStatus"),
    @NamedQuery(name = "RentalPayments.findByAmount", query = "SELECT r FROM RentalPayments r WHERE r.amount = :amount"),
    @NamedQuery(name = "RentalPayments.findByPaidAt", query = "SELECT r FROM RentalPayments r WHERE r.paidAt = :paidAt"),
    @NamedQuery(name = "RentalPayments.findByCreatedAt", query = "SELECT r FROM RentalPayments r WHERE r.createdAt = :createdAt"),
    @NamedQuery(name = "RentalPayments.findByUpdatedAt", query = "SELECT r FROM RentalPayments r WHERE r.updatedAt = :updatedAt")})
public class RentalPayments implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "payment_method")
    private String paymentMethod;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "payment_status")
    private String paymentStatus;
    @Basic(optional = false)
    @NotNull
    @Min(value = 0, message = "Payment amount must not be negative.")
    @Max(value = 9999999999L, message = "Payment amount must not exceed 10 digits.")
    @Column(name = "amount")
    private long amount;
    @Column(name = "paid_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date paidAt;
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
    @JoinColumn(name = "rental_order_id", referencedColumnName = "id")
    @OneToOne(optional = false)
    private RentalOrders rentalOrderId;

    public RentalPayments() {
    }

    public RentalPayments(Integer id) {
        this.id = id;
    }

    public RentalPayments(Integer id, String paymentMethod, String paymentStatus, long amount, Date createdAt, Date updatedAt) {
        this.id = id;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.amount = amount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public Date getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Date paidAt) {
        this.paidAt = paidAt;
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
        if (!(object instanceof RentalPayments)) {
            return false;
        }
        RentalPayments other = (RentalPayments) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.lens.rental_payments.entity.RentalPayments[ id=" + id + " ]";
    }

}
