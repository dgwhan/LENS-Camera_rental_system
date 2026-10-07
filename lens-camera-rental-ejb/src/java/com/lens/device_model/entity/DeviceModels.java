package com.lens.device_model.entity;

import com.lens.cart.entity.CartItems;
import com.lens.device.entity.Devices;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author Duong Ngoc Han
 */
@Entity
@Table(name = "DeviceModels")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DeviceModels.findAll", query = "SELECT d FROM DeviceModels d"),
    @NamedQuery(name = "DeviceModels.findById", query = "SELECT d FROM DeviceModels d WHERE d.id = :id"),
    @NamedQuery(name = "DeviceModels.findByName", query = "SELECT d FROM DeviceModels d WHERE d.name = :name"),
    @NamedQuery(name = "DeviceModels.findByType", query = "SELECT d FROM DeviceModels d WHERE d.type = :type"),
    @NamedQuery(name = "DeviceModels.findByBrand", query = "SELECT d FROM DeviceModels d WHERE d.brand = :brand"),
    @NamedQuery(name = "DeviceModels.findByImageUrl", query = "SELECT d FROM DeviceModels d WHERE d.imageUrl = :imageUrl"),
    @NamedQuery(name = "DeviceModels.findByModel", query = "SELECT d FROM DeviceModels d WHERE d.model = :model"),
    @NamedQuery(name = "DeviceModels.findByDescription", query = "SELECT d FROM DeviceModels d WHERE d.description = :description"),
    @NamedQuery(name = "DeviceModels.findByRentalPrice", query = "SELECT d FROM DeviceModels d WHERE d.rentalPrice = :rentalPrice"),
    @NamedQuery(name = "DeviceModels.findByDepositAmount", query = "SELECT d FROM DeviceModels d WHERE d.depositAmount = :depositAmount"),
    @NamedQuery(name = "DeviceModels.findByCreatedAt", query = "SELECT d FROM DeviceModels d WHERE d.createdAt = :createdAt"),
    @NamedQuery(name = "DeviceModels.findByUpdatedAt", query = "SELECT d FROM DeviceModels d WHERE d.updatedAt = :updatedAt")})
public class DeviceModels implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "type")
    private String type;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "brand")
    private String brand;
    @Size(max = 500)
    @Column(name = "image_url")
    private String imageUrl;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "model")
    private String model;
    @Size(max = 500)
    @Column(name = "description")
    private String description;
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
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Basic(optional = false)
    @NotNull
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "deviceModelId")
    private Collection<CartItems> cartItemsCollection;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "deviceModelId")
    private Collection<Devices> devicesCollection;

    public DeviceModels() {
    }

    public DeviceModels(Integer id) {
        this.id = id;
    }

    public DeviceModels(Integer id, String name, String type, String brand, String model, long rentalPrice, long depositAmount, Date createdAt, Date updatedAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.brand = brand;
        this.model = model;
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

    @XmlTransient
    public Collection<Devices> getDevicesCollection() {
        return devicesCollection;
    }

    public void setDevicesCollection(Collection<Devices> devicesCollection) {
        this.devicesCollection = devicesCollection;
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
        if (!(object instanceof DeviceModels)) {
            return false;
        }
        DeviceModels other = (DeviceModels) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.lens.device.entity.DeviceModels[ id=" + id + " ]";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @XmlTransient
    public Collection<CartItems> getCartItemsCollection() {
        return cartItemsCollection;
    }

    public void setCartItemsCollection(Collection<CartItems> cartItemsCollection) {
        this.cartItemsCollection = cartItemsCollection;
    }

}
