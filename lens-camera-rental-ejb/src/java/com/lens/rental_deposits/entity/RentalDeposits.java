package com.lens.rental_deposits.entity;

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
@Table(name = "RentalDeposits")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RentalDeposits.findAll", query = "SELECT r FROM RentalDeposits r"),
    @NamedQuery(name = "RentalDeposits.findById", query = "SELECT r FROM RentalDeposits r WHERE r.id = :id"),
    @NamedQuery(name = "RentalDeposits.findByAmount", query = "SELECT r FROM RentalDeposits r WHERE r.amount = :amount"),
    @NamedQuery(name = "RentalDeposits.findByStatus", query = "SELECT r FROM RentalDeposits r WHERE r.status = :status"),
    @NamedQuery(name = "RentalDeposits.findByNote", query = "SELECT r FROM RentalDeposits r WHERE r.note = :note"),
    @NamedQuery(name = "RentalDeposits.findByCreatedAt", query = "SELECT r FROM RentalDeposits r WHERE r.createdAt = :createdAt"),
    @NamedQuery(name = "RentalDeposits.findByUpdatedAt", query = "SELECT r FROM RentalDeposits r WHERE r.updatedAt = :updatedAt")})
public class RentalDeposits implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Min(value = 0, message = "Deposit amount must not be negative.")
    @Max(value = 9999999999L, message = "Deposit amount must not exceed 10 digits.")
    @Column(name = "amount")
    private long amount;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "status")
    private String status;
    @Size(max = 1000)
    @Column(name = "note")
    private String note;
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

    public RentalDeposits() {
    }

    public RentalDeposits(Integer id) {
        this.id = id;
    }

    public RentalDeposits(Integer id, long amount, String status, Date createdAt, Date updatedAt) {
        this.id = id;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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
        if (!(object instanceof RentalDeposits)) {
            return false;
        }
        RentalDeposits other = (RentalDeposits) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.lens.rental_deposits.entity.RentalDeposits[ id=" + id + " ]";
    }

}
