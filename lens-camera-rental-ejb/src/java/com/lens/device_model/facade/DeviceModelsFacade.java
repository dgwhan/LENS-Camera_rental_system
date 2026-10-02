package com.lens.device_model.facade;

import com.lens.common.facade.AbstractFacade;
import com.lens.device_model.entity.DeviceModels;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class DeviceModelsFacade extends AbstractFacade<DeviceModels> implements DeviceModelsFacadeLocal {

    @PersistenceContext(unitName = "lens-camera-rental-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DeviceModelsFacade() {
        super(DeviceModels.class);
    }

    @Override
    public boolean isBrandModelExists(String brand, String model, Integer id) {
        if (brand == null || brand.trim().isEmpty() || model == null || model.trim().isEmpty()) {
            return false;
        }
        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(dm) FROM DeviceModels dm WHERE LOWER(TRIM(dm.brand)) = LOWER(TRIM(:brand)) AND LOWER(TRIM(dm.model)) = LOWER(TRIM(:model)) ");
        if (id != null) {
            jpql.append("AND dm.id != :id");
        }
        var query = em.createQuery(jpql.toString(), Long.class)
                .setParameter("brand", brand.trim())
                .setParameter("model", model.trim());
        if (id != null) {
            query.setParameter("id", id);
        }
        Long count = query.getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public boolean isDeviceModelNameExists(String name, Integer id) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(dm) FROM DeviceModels dm WHERE LOWER(TRIM(dm.name)) = LOWER(TRIM(:name)) ");
        if (id != null) {
            jpql.append("AND dm.id != :id");
        }
        var query = em.createQuery(jpql.toString(), Long.class)
                .setParameter("name", name.trim());
        if (id != null) {
            query.setParameter("id", id);
        }
        Long count = query.getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public List<DeviceModels> search(String keyword) {
        StringBuilder jpql = new StringBuilder("SELECT dm FROM DeviceModels dm WHERE 1=1 ");

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        if (hasKeyword) {
            jpql.append(" AND (LOWER(dm.name) LIKE :keyword ")
                    .append(" OR LOWER(dm.brand) LIKE :keyword ")
                    .append(" OR LOWER(dm.model) LIKE :keyword ")
                    .append(" OR LOWER(dm.type) LIKE :keyword)");
        }

        jpql.append(" ORDER BY dm.id DESC");

        TypedQuery<DeviceModels> query = em.createQuery(jpql.toString(), DeviceModels.class);

        if (hasKeyword) {
            query.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }

        return query.getResultList();
    }

    @Override
    public int totalDeviceModels() {
        Long count = em.createQuery("SELECT COUNT(dm) from DeviceModels dm", Long.class).getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public int totalModelBrand() {
        Long count = em.createQuery("SELECT COUNT(DISTINCT dm.brand) FROM DeviceModels dm", Long.class).getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public int totalModelType() {
        Long count = em.createQuery("SELECT COUNT(DISTINCT dm.type) FROM DeviceModels dm", Long.class).getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public List<String> findDistinctTypes() {
        return em.createQuery("SELECT DISTINCT dm.type FROM DeviceModels dm WHERE dm.type IS NOT NULL AND TRIM(dm.type) != '' ORDER BY dm.type ASC", String.class).getResultList();
    }

    @Override
    public List<String> findDistinctBrands() {
        return em.createQuery("SELECT DISTINCT dm.brand FROM DeviceModels dm WHERE dm.brand IS NOT NULL AND TRIM(dm.brand) != '' ORDER BY dm.brand ASC", String.class).getResultList();
    }

}
