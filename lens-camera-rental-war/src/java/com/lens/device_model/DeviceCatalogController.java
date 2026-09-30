package com.lens.device_model;

import com.lens.availability.service.AvailabilityServiceLocal;
import com.lens.common.util.FormatUtil;
import com.lens.common.util.ImageUtil;
import com.lens.config.PaginationConfig;
import com.lens.device_model.entity.DeviceModels;
import com.lens.device_model.facade.DeviceModelsFacadeLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Duong Ngoc Han
 */
@Named(value = "deviceCatalogController")
@ViewScoped
public class DeviceCatalogController implements Serializable {

    private static final long serialVersionUID = 1L;

    //bộ lọc giá
    public static final String PRICE_RANGE_UNDER_100 = "under_100";
    public static final String PRICE_RANGE_100_TO_300 = "100_300";
    public static final String PRICE_RANGE_300_TO_500 = "300_500";
    public static final String PRICE_RANGE_OVER_500 = "over_500";

    public static final long THRESHOLD_LOW_RATE = 100_000L;
    public static final long THRESHOLD_MID_RATE = 300_000L;
    public static final long THRESHOLD_HIGH_RATE = 500_000L;

    @EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    @EJB
    private AvailabilityServiceLocal availabilityService;

    //bộ lọc
    private String search = "";
    private String type = "";
    private String brand = "";
    private String rate = "";

    //phân trang
    private int page = 1;
    private int pageSize;

    private int totalCount = 0;
    private int filteredCount = 0;
    private int totalPages = 1;

    private List<String> types = new ArrayList<>();
    private List<String> brands = new ArrayList<>();
    private List<DeviceModels> allDevices = new ArrayList<>();
    private List<DeviceModels> filteredDevices = new ArrayList<>();
    private List<DeviceModels> pagedDevices = new ArrayList<>();

    public DeviceCatalogController() {
    }

    @PostConstruct
    public void init() {
        loadFilterOptions();
        applyFilter();
    }

    public void loadFilterOptions() {
        try {
            types = deviceModelsFacade.findDistinctTypes();
            brands = deviceModelsFacade.findDistinctBrands();
        } catch (Exception e) {
            types = Collections.emptyList();
            brands = Collections.emptyList();
        }
    }

    //áp dụng bộ lọc
    public void applyFilter() {
        try {
            allDevices = deviceModelsFacade.findAll();
            if (allDevices == null) {
                allDevices = Collections.emptyList();
            }
            totalCount = allDevices.size();

            filteredDevices = allDevices.stream().filter(deviceModel -> {
                //thanh search thiết bị
                if (search != null && !search.trim().isEmpty()) {
                    String searchKeyword = search.trim().toLowerCase();
                    boolean isNameMatched = deviceModel.getName() != null && deviceModel.getName().toLowerCase().contains(searchKeyword);
                    boolean isBrandMatched = deviceModel.getBrand() != null && deviceModel.getBrand().toLowerCase().contains(searchKeyword);
                    boolean isModelMatched = deviceModel.getModel() != null && deviceModel.getModel().toLowerCase().contains(searchKeyword);
                    boolean isTypeMatched = deviceModel.getType() != null && deviceModel.getType().toLowerCase().contains(searchKeyword);
                    if (!isNameMatched && !isBrandMatched && !isModelMatched && !isTypeMatched) {
                        return false;
                    }
                }

                //lọc phân loại
                if (type != null && !type.trim().isEmpty()) {
                    if (deviceModel.getType() == null || !deviceModel.getType().equalsIgnoreCase(type.trim())) {
                        return false;
                    }
                }

                //lọc brand
                if (brand != null && !brand.trim().isEmpty()) {
                    if (deviceModel.getBrand() == null || !deviceModel.getBrand().equalsIgnoreCase(brand.trim())) {
                        return false;
                    }
                }

                //lọc giá
                if (rate != null && !rate.trim().isEmpty()) {
                    long rentalPricePerDay = deviceModel.getRentalPrice();
                    String selectedRateRange = rate.trim();
                    switch (selectedRateRange) {
                        case PRICE_RANGE_UNDER_100:
                            if (rentalPricePerDay >= THRESHOLD_LOW_RATE) return false;
                            break;
                        case PRICE_RANGE_100_TO_300:
                            if (rentalPricePerDay < THRESHOLD_LOW_RATE || rentalPricePerDay > THRESHOLD_MID_RATE) return false;
                            break;
                        case PRICE_RANGE_300_TO_500:
                            if (rentalPricePerDay < THRESHOLD_MID_RATE || rentalPricePerDay > THRESHOLD_HIGH_RATE) return false;
                            break;
                        case PRICE_RANGE_OVER_500:
                            if (rentalPricePerDay <= THRESHOLD_HIGH_RATE) return false;
                            break;
                        default:
                            break;
                    }
                }

                return true;
            }).collect(Collectors.toList());



            // Sắp xếp danh sách thiết bị: AVAILABLE lên trước, OUT OF STOCK xếp xuống dưới cùng
            filteredDevices.sort((d1, d2) -> {
                boolean a1 = isProductAvailable(d1.getId());
                boolean a2 = isProductAvailable(d2.getId());
                return Boolean.compare(!a1, !a2);
            });

            filteredCount = filteredDevices.size();

            pageSize = PaginationConfig.resolvePageSize(pageSize);

            if (pageSize > 0) {
                totalPages = PaginationConfig.calculateTotalPages(filteredCount, pageSize);
                page = PaginationConfig.clampPage(page, totalPages);
                pagedDevices = PaginationConfig.paginate(filteredDevices, page, pageSize);
            } else {
                page = 1;
                totalPages = 1;
                pagedDevices = filteredDevices;
            }

        } catch (Exception e) {
            e.printStackTrace();
            allDevices = Collections.emptyList();
            filteredDevices = Collections.emptyList();
            pagedDevices = Collections.emptyList();
            totalCount = 0;
            filteredCount = 0;
            totalPages = 1;
        }
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

    ///thiết bị khả dụng
    public boolean isProductAvailable(Integer id) {
        if (availabilityService == null || id == null) {
            return false;
        }
        return availabilityService.isProductAvailable(id);
    }

    //thiết bị hết hàng
    public String getProductAvailabilityStatus(Integer id) {
        if (availabilityService == null || id == null) {
            return "OUT OF STOCK";
        }
        return availabilityService.getProductAvailabilityStatus(id);
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
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

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        if (pageSize <= 0) {
            pageSize = PaginationConfig.getDefaultPageSize();
        }
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getFilteredCount() {
        return filteredCount;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public List<String> getTypes() {
        return types;
    }

    public List<String> getBrands() {
        return brands;
    }

    public List<DeviceModels> getFilteredDevices() {
        return filteredDevices;
    }

    public List<DeviceModels> getPagedDevices() {
        return pagedDevices;
    }
}
