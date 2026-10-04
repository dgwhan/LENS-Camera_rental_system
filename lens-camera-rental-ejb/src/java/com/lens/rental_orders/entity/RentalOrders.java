package com.lens.rental_orders.entity;

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
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Duong Ngoc Han
 */
@Entity
@Table(name = "RentalOrders")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RentalOrders.findAll", query = "SELECT r FROM RentalOrders r"),
    @NamedQuery(name = "RentalOrders.findById", query = "SELECT r FROM RentalOrders r WHERE r.id = :id"),
    @NamedQuery(name = "RentalOrders.findByCustomerName", query = "SELECT r FROM RentalOrders r WHERE r.customerName = :customerName"),
    @NamedQuery(name = "RentalOrders.findByCustomerPhone", query = "SELECT r FROM RentalOrders r WHERE r.customerPhone = :customerPhone"),
    @NamedQuery(name = "RentalOrders.findByStatus", query = "SELECT r FROM RentalOrders r WHERE r.status = :status"),
    @NamedQuery(name = "RentalOrders.findBySubtotal", query = "SELECT r FROM RentalOrders r WHERE r.subtotal = :subtotal"),
    @NamedQuery(name = "RentalOrders.findByDepositTotal", query = "SELECT r FROM RentalOrders r WHERE r.depositTotal = :depositTotal"),
    @NamedQuery(name = "RentalOrders.findByTotalPayable", query = "SELECT r FROM RentalOrders r WHERE r.totalPayable = :totalPayable"),
    @NamedQuery(name = "RentalOrders.findByNote", query = "SELECT r FROM RentalOrders r WHERE r.note = :note"),
    @NamedQuery(name = "RentalOrders.findByCreatedAt", query = "SELECT r FROM RentalOrders r WHERE r.createdAt = :createdAt"),
    @NamedQuery(name = "RentalOrders.findByUpdatedAt", query = "SELECT r FROM RentalOrders r WHERE r.updatedAt = :updatedAt"),
    @NamedQuery(name = "RentalOrders.findByCustomerAddress", query = "SELECT r FROM RentalOrders r WHERE r.customerAddress = :customerAddress")})
public class RentalOrders implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "customer_name")
    private String customerName;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "customer_phone")
    private String customerPhone;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "status")
    private String status;
    @Basic(optional = false)
    @NotNull
    @Column(name = "subtotal")
    private long subtotal;
    @Basic(optional = false)
    @NotNull
    @Column(name = "deposit_total")
    private long depositTotal;
    @Basic(optional = false)
    @NotNull
    @Column(name = "total_payable")
    private long totalPayable;
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
    @Size(max = 500)
    @Column(name = "customer_address")
    private String customerAddress;
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Users userId;

    public RentalOrders() {
    }

    public RentalOrders(Integer id) {
        this.id = id;
    }

    public RentalOrders(Integer id, String customerName, String customerPhone, String status, long subtotal, long depositTotal, long totalPayable, Date createdAt, Date updatedAt) {
        this.id = id;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.status = status;
        this.subtotal = subtotal;
        this.depositTotal = depositTotal;
        this.totalPayable = totalPayable;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(long subtotal) {
        this.subtotal = subtotal;
    }

    public long getDepositTotal() {
        return depositTotal;
    }

    public void setDepositTotal(long depositTotal) {
        this.depositTotal = depositTotal;
    }

    public long getTotalPayable() {
        return totalPayable;
    }

    public void setTotalPayable(long totalPayable) {
        this.totalPayable = totalPayable;
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

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
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
        if (!(object instanceof RentalOrders)) {
            return false;
        }
        RentalOrders other = (RentalOrders) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.lens.rental_orders.entity.RentalOrders[ id=" + id + " ]";
    }

}
