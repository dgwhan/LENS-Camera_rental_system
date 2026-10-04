package com.lens.device.facade;

import com.lens.common.facade.AbstractFacade;
import com.lens.device.entity.Devices;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class DevicesFacade extends AbstractFacade<Devices> implements DevicesFacadeLocal {

    @PersistenceContext(unitName = "lens-camera-rental-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DevicesFacade() {
        super(Devices.class);
    }

    // kiểm tra trùng lặp serial number
    @Override
    public boolean isSerialNumber(String serialNumber, Integer id) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            return false;
        }

        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(d) FROM Devices d WHERE LOWER(TRIM(d.serialNumber)) = LOWER(TRIM(:serialNumber))");
        if (id != null) {
            jpql.append(" AND d.id != :id");
        }

        var query = em.createQuery(jpql.toString(), Long.class).setParameter("serialNumber", serialNumber.trim());

        if (id != null) {
            query.setParameter("id", id);
        }

        Long count = query.getSingleResult();
        return count != null && count > 0;
    }

    // lấy toàn bộ danh sách thiết bị sắp xếp theo ID giảm dần
    @Override
    public java.util.List<Devices> findAll() {
        return em.createQuery("SELECT d FROM Devices d ORDER BY d.id DESC", Devices.class)
                .getResultList();
    }

    // tìm kiếm thiết bị theo từ khóa (serial, model, brand), trạng thái và brand
    @Override
    public java.util.List<Devices> search(String keyword, String status) {
        return search(keyword, status, null);
    }

    @Override
    public java.util.List<Devices> search(String keyword, String status, String brand) {
        StringBuilder jpql = new StringBuilder("SELECT d FROM Devices d WHERE 1=1");
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty();
        boolean hasBrand = brand != null && !brand.trim().isEmpty();

        if (hasKeyword) {
            jpql.append(" AND (LOWER(d.serialNumber) LIKE :keyword")
                    .append(" OR LOWER(d.deviceModelId.name) LIKE :keyword")
                    .append(" OR LOWER(d.deviceModelId.brand) LIKE :keyword")
                    .append(" OR LOWER(d.deviceModelId.model) LIKE :keyword)");
        }
        if (hasStatus) {
            jpql.append(" AND d.status = :status");
        }
        if (hasBrand) {
            jpql.append(" AND LOWER(d.deviceModelId.brand) = LOWER(:brand)");
        }

        jpql.append(" ORDER BY d.id DESC");

        var query = em.createQuery(jpql.toString(), Devices.class);

        if (hasKeyword) {
            query.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }
        if (hasStatus) {
            query.setParameter("status", status.trim());
        }
        if (hasBrand) {
            query.setParameter("brand", brand.trim());
        }

        return query.getResultList();
    }

    @Override
    public java.util.List<String> findDistinctBrands() {
        return em.createQuery(
                "SELECT DISTINCT d.deviceModelId.brand FROM Devices d " +
                        "WHERE d.deviceModelId.brand IS NOT NULL AND TRIM(d.deviceModelId.brand) != '' " +
                        "ORDER BY d.deviceModelId.brand ASC",
                String.class).getResultList();
    }

    @Override
    public int totalDevices() {
        Long count = em.createQuery("SELECT COUNT(d) FROM Devices d", Long.class).getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public int totalDevicesAvailable() {
        Long count = em.createQuery("SELECT COUNT(d) FROM Devices d WHERE UPPER(d.status) = 'AVAILABLE'", Long.class)
                .getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public int totalDevicesRenting() {
        Long count = em.createQuery("SELECT COUNT(d) FROM Devices d WHERE UPPER(d.status) = 'RENTING'", Long.class)
                .getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public List<Devices> findByDeviceModelId(Integer modelId) {
        if (modelId == null) {
            return java.util.Collections.emptyList();
        }
        return em
                .createQuery("SELECT d FROM Devices d WHERE d.deviceModelId.id = :modelId ORDER BY d.id DESC",
                        Devices.class)
                .setParameter("modelId", modelId)
                .getResultList();
    }

    @Override
    public List<Devices> findAvailableForPeriod(Integer modelId, Date startDate, Date endDate,
            Integer excludeRentalItemId) {
        if (modelId == null || startDate == null || endDate == null) {
            return java.util.Collections.emptyList();
        }

        // find physical devices of the given model that are AVAILABLE and not already
        // assigned to another overlapping rental item in an APPROVED or ACTIVE order.
        StringBuilder jpql = new StringBuilder(
                "SELECT d FROM Devices d WHERE d.deviceModelId.id = :modelId AND d.status = 'AVAILABLE' AND d.id NOT IN (SELECT ri.assignedDeviceId.id FROM RentalItems ri WHERE ri.assignedDeviceId IS NOT NULL AND ri.rentalOrderId.status IN ('PENDING', 'APPROVED', 'ACTIVE') AND ri.startDate < :endDate AND ri.endDate > :startDate");

        if (excludeRentalItemId != null) {
            jpql.append(" AND ri.id != :excludeId");
        }

        jpql.append(") ORDER BY d.id ASC");

        TypedQuery<Devices> query = em.createQuery(jpql.toString(), Devices.class)
                .setParameter("modelId", modelId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate);

        if (excludeRentalItemId != null) {
            query.setParameter("excludeId", excludeRentalItemId);
        }

        return query.getResultList();
    }
    
    
}
