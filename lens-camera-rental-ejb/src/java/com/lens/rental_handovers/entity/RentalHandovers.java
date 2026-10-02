package com.lens.rental_handovers.entity;

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
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Duong Ngoc Han
 */
@Entity
@Table(name = "RentalHandovers")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RentalHandovers.findAll", query = "SELECT r FROM RentalHandovers r"),
    @NamedQuery(name = "RentalHandovers.findById", query = "SELECT r FROM RentalHandovers r WHERE r.id = :id"),
    @NamedQuery(name = "RentalHandovers.findByMethod", query = "SELECT r FROM RentalHandovers r WHERE r.method = :method"),
    @NamedQuery(name = "RentalHandovers.findByStatus", query = "SELECT r FROM RentalHandovers r WHERE r.status = :status"),
    @NamedQuery(name = "RentalHandovers.findByDeliveryAddress", query = "SELECT r FROM RentalHandovers r WHERE r.deliveryAddress = :deliveryAddress"),
    @NamedQuery(name = "RentalHandovers.findByFailureReason", query = "SELECT r FROM RentalHandovers r WHERE r.failureReason = :failureReason"),
    @NamedQuery(name = "RentalHandovers.findByNote", query = "SELECT r FROM RentalHandovers r WHERE r.note = :note"),
    @NamedQuery(name = "RentalHandovers.findByConfirmedAt", query = "SELECT r FROM RentalHandovers r WHERE r.confirmedAt = :confirmedAt"),
    @NamedQuery(name = "RentalHandovers.findByCreatedAt", query = "SELECT r FROM RentalHandovers r WHERE r.createdAt = :createdAt"),
    @NamedQuery(name = "RentalHandovers.findByUpdatedAt", query = "SELECT r FROM RentalHandovers r WHERE r.updatedAt = :updatedAt")})
public class RentalHandovers implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "method")
    private String method;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "status")
    private String status;
    @Size(max = 255)
    @Column(name = "delivery_address")
    private String deliveryAddress;
    @Size(max = 50)
    @Column(name = "failure_reason")
    private String failureReason;
    @Size(max = 1000)
    @Column(name = "note")
    private String note;
    @Column(name = "confirmed_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date confirmedAt;
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

    public RentalHandovers() {
    }

    public RentalHandovers(Integer id) {
        this.id = id;
    }

    public RentalHandovers(Integer id, String method, String status, Date createdAt, Date updatedAt) {
        this.id = id;
        this.method = method;
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Date confirmedAt) {
        this.confirmedAt = confirmedAt;
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
        if (!(object instanceof RentalHandovers)) {
            return false;
        }
        RentalHandovers other = (RentalHandovers) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.lens.rental_handovers.entity.RentalHandovers[ id=" + id + " ]";
    }

}
