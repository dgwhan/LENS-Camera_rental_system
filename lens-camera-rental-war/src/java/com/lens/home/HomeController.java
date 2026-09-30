package com.lens.home;

import com.lens.availability.service.AvailabilityServiceLocal;
import com.lens.common.util.FormatUtil;
import com.lens.common.util.ImageUtil;
import com.lens.device_model.entity.DeviceModels;
import com.lens.device_model.facade.DeviceModelsFacadeLocal;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Named(value = "homeController")
@RequestScoped
public class HomeController implements Serializable {

    private static final long serialVersionUID = 1L;

    @jakarta.ejb.EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    // customer availability
    @jakarta.ejb.EJB
    private AvailabilityServiceLocal availabilityService;

    private List<DeviceModels> featuredDevices;

    public HomeController() {
    }

    @PostConstruct
    public void init() {
        loadFeaturedDevices();
    }

    public void loadFeaturedDevices() {
        try {
            List<DeviceModels> all = deviceModelsFacade.search("");
            if (all != null && !all.isEmpty()) {
                // Featured Devices chỉ hiển thị các thiết bị AVAILABLE, không hiện thiết bị OUT OF STOCK
                List<DeviceModels> availableList = all.stream()
                        .filter(d -> isProductAvailable(d.getId()))
                        .collect(java.util.stream.Collectors.toList());
                int limit = Math.min(4, availableList.size());
                featuredDevices = availableList.subList(0, limit);
            } else {
                featuredDevices = Collections.emptyList();
            }
        } catch (Exception e) {
            featuredDevices = Collections.emptyList();
        }
    }


    public List<DeviceModels> getFeaturedDevices() {
        if (featuredDevices == null) {
            loadFeaturedDevices();
        }
        return featuredDevices;
    }

    public void setFeaturedDevices(List<DeviceModels> featuredDevices) {
        this.featuredDevices = featuredDevices;
    }

    public String formatPrice(long price) {
        return FormatUtil.formatPrice(price);
    }

    public String getImageUrl(String imageName) {
        return ImageUtil.getDeviceImageUrl(imageName);
    }

    public String getDefaultImageUrl() {
        return ImageUtil.getDefaultImageUrl();
    }

    // customer availability
    // product availability
    public boolean isProductAvailable(Integer id) {
        if (availabilityService == null || id == null) {
            return false;
        }
        return availabilityService.isProductAvailable(id);
    }

    // customer availability
    // out of stock handling
    public String getProductAvailabilityStatus(Integer id) {
        if (availabilityService == null || id == null) {
            return "OUT OF STOCK";
        }
        return availabilityService.getProductAvailabilityStatus(id);
    }
}

