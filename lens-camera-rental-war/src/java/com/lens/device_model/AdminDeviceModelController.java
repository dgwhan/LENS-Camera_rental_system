package com.lens.device_model;

import com.lens.device_model.entity.DeviceModels;
import com.lens.device.entity.Devices;
import com.lens.device_model.facade.DeviceModelsFacadeLocal;
import com.lens.device.facade.DevicesFacadeLocal;
import com.lens.common.util.ImageUtil;
import com.lens.common.util.FacesUtil;
import com.lens.common.util.FormatUtil;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Controller for Admin device model catalog management.
 *
 * @author Duong Ngoc Han
 */
@Named(value = "adminDeviceModelController")
@ViewScoped
public class AdminDeviceModelController implements Serializable {

    private static final long serialVersionUID = 1L;

    @jakarta.ejb.EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    @jakarta.ejb.EJB
    private DevicesFacadeLocal devicesFacade;

    private DeviceModels deviceModels = new DeviceModels();
    private Integer id;
    private boolean editMode;
    private String keyword = "";
    private String brand = "";
    private String type = "";
    private Part imagePart;
    private boolean removeCurrentImage;

    public AdminDeviceModelController() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void initDetail() {
        if (id != null && (deviceModels == null || deviceModels.getId() == null || !deviceModels.getId().equals(id))) {
            deviceModels = deviceModelsFacade.find(id);
            if (deviceModels == null) {
                try {
                    FacesContext.getCurrentInstance().getExternalContext().redirect(
                            FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/faces/404.xhtml");
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void initForm() {
        if (id != null && (deviceModels == null || deviceModels.getId() == null || !deviceModels.getId().equals(id))) {
            deviceModels = deviceModelsFacade.find(id);
            if (deviceModels != null) {
                editMode = true;
                imagePart = null;
                removeCurrentImage = false;
            }
        } else if (id == null && !editMode) {
            if (deviceModels == null) {
                deviceModels = new DeviceModels();
            }
            if (deviceModels.getImageUrl() == null) {
                deviceModels.setImageUrl(ImageUtil.DEFAULT_IMAGE_NAME);
            }
        }
    }

    public String newDeviceModel() {
        deviceModels = new DeviceModels();
        deviceModels.setImageUrl(ImageUtil.DEFAULT_IMAGE_NAME);
        imagePart = null;
        removeCurrentImage = false;
        editMode = false;
        id = null;
        return "/admin/devicemodels-management/form?faces-redirect=true";
    }

    public String insertDeviceModel() {
        boolean hasError = false;
        if (isDuplicateName(null)) {
            hasError = true;
        }
        if (isDuplicateBrandModel(null)) {
            hasError = true;
        }
        if (hasError) {
            return null;
        }

        if (imagePart != null && imagePart.getSize() > 0) {
            String uploadedImage = ImageUtil.processUpload(imagePart, "device", "deviceModelForm:imageFile", "model_");
            if (uploadedImage == null || FacesContext.getCurrentInstance().isValidationFailed()) {
                return null;
            }
            deviceModels.setImageUrl(uploadedImage);
        } else {
            deviceModels.setImageUrl(ImageUtil.DEFAULT_IMAGE_NAME);
        }

        try {
            Date now = new Date();
            deviceModels.setCreatedAt(now);
            deviceModels.setUpdatedAt(now);

            deviceModelsFacade.create(deviceModels);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert",
                    "Device model created successfully.");
            return "/admin/devicemodels-management/list?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            FacesUtil.addErrorMessage("Failed to create device model.");
            return null;
        }
    }

    public String editDeviceModel(Integer id) {
        this.id = id;
        deviceModels = deviceModelsFacade.find(id);
        if (deviceModels == null) {
            return "/404?faces-redirect=true";
        }

        imagePart = null;
        removeCurrentImage = false;
        editMode = true;
        return "/admin/devicemodels-management/form?faces-redirect=true&id=" + id;
    }

    public String updateDeviceModel() {
        if (deviceModels == null || (deviceModels.getId() == null && id == null)) {
            FacesUtil.addErrorMessage("Device model not found.");
            return null;
        }
        if (deviceModels.getId() == null && id != null) {
            deviceModels.setId(id);
        }

        boolean hasError = false;
        if (isDuplicateName(deviceModels.getId())) {
            hasError = true;
        }
        if (isDuplicateBrandModel(deviceModels.getId())) {
            hasError = true;
        }
        if (hasError) {
            return null;
        }

        String oldImage = deviceModels.getImageUrl();

        try {
            if (deviceModels.getCreatedAt() == null && deviceModels.getId() != null) {
                DeviceModels existing = deviceModelsFacade.find(deviceModels.getId());
                if (existing != null) {
                    deviceModels.setCreatedAt(existing.getCreatedAt());
                    if (oldImage == null || oldImage.isBlank()) {
                        oldImage = existing.getImageUrl();
                        deviceModels.setImageUrl(oldImage);
                    }
                }
            }

            String uploadedImage = null;
            if (imagePart != null && imagePart.getSize() > 0) {
                uploadedImage = ImageUtil.processUpload(imagePart, "device", "deviceModelForm:imageFile", "model_");
                if (uploadedImage == null || FacesContext.getCurrentInstance().isValidationFailed()) {
                    return null;
                }
                deviceModels.setImageUrl(uploadedImage);
            } else if (removeCurrentImage) {
                deviceModels.setImageUrl(ImageUtil.DEFAULT_IMAGE_NAME);
            }

            if (deviceModels.getImageUrl() == null || deviceModels.getImageUrl().isBlank()) {
                deviceModels.setImageUrl(ImageUtil.DEFAULT_IMAGE_NAME);
            }

            deviceModels.setUpdatedAt(new Date());

            deviceModelsFacade.edit(deviceModels);

            if ((uploadedImage != null || removeCurrentImage) && oldImage != null
                    && !oldImage.equals(ImageUtil.DEFAULT_IMAGE_NAME)) {
                ImageUtil.deleteImage(oldImage, "device");
            }

            imagePart = null;
            removeCurrentImage = false;

            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert",
                    "Device model updated successfully.");
            return "/admin/devicemodels-management/list?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            FacesUtil.addErrorMessage("Failed to update device model.");
            return null;
        }
    }

    public String detailDeviceModel(Integer id) {
        this.id = id;
        deviceModels = deviceModelsFacade.find(id);
        if (deviceModels == null) {
            return "/404?faces-redirect=true";
        }
        return "/admin/devicemodels-management/detail?faces-redirect=true&id=" + id;
    }

    public void deleteDeviceModel(Integer id) {
        try {
            DeviceModels target = deviceModelsFacade.find(id);
            if (target != null) {
                deviceModelsFacade.remove(target);
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesUtil.addErrorMessage("Failed to delete device model.");
        }
    }

    public List<DeviceModels> showAllDeviceModel() {
        return deviceModelsFacade.search(keyword, brand, type);
    }

    public List<DeviceModels> getDeviceModelsList() {
        return deviceModelsFacade.search(keyword, brand, type);
    }

    public void resetFilter() {
        this.keyword = "";
        this.brand = "";
        this.type = "";
    }

    private boolean isDuplicateBrandModel(Integer excludeId) {
        if (deviceModelsFacade.isBrandModelExists(deviceModels.getBrand(), deviceModels.getModel(), excludeId)) {
            FacesUtil.addFieldError("deviceModelForm:brand", "");
            FacesUtil.addFieldError("deviceModelForm:model", "Brand and Model combination already exists.");
            return true;
        }
        return false;
    }

    private boolean isDuplicateName(Integer excludeId) {
        if (deviceModelsFacade.isDeviceModelNameExists(deviceModels.getName(), excludeId)) {
            FacesUtil.addFieldError("deviceModelForm:name", "Device model name already exists.");
            return true;
        }
        return false;
    }

    public boolean isDeviceModelNameExists(String name, Integer id) {
        return deviceModelsFacade.isDeviceModelNameExists(name, id);
    }

    public String getDefaultImageUrl() {
        return ImageUtil.getDefaultImageUrl();
    }

    public String getImageUrl(String imageUrl) {
        return ImageUtil.getDeviceImageUrl(imageUrl);
    }

    public String getModelImageUrl() {
        return ImageUtil.getDeviceImageUrl(deviceModels != null ? deviceModels.getImageUrl() : null);
    }

    public DeviceModels getDeviceModels() {
        return deviceModels;
    }

    public void setDeviceModels(DeviceModels deviceModels) {
        this.deviceModels = deviceModels;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public List<String> getDistinctBrands() {
        return deviceModelsFacade.findDistinctBrands();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getDistinctTypes() {
        return deviceModelsFacade.findDistinctTypes();
    }

    public Part getImagePart() {
        return imagePart;
    }

    public void setImagePart(Part imagePart) {
        this.imagePart = imagePart;
    }

    public boolean isRemoveCurrentImage() {
        return removeCurrentImage;
    }

    public void setRemoveCurrentImage(boolean removeCurrentImage) {
        this.removeCurrentImage = removeCurrentImage;
    }

    public boolean isHasCustomImage() {
        if (deviceModels == null) {
            return false;
        }
        String img = deviceModels.getImageUrl();
        return img != null && !img.trim().isEmpty() && !img.equalsIgnoreCase(ImageUtil.DEFAULT_IMAGE_NAME);
    }

    public boolean getHasCustomImage() {
        return isHasCustomImage();
    }

    public int getTotalDeviceModels() {
        return deviceModelsFacade.totalDeviceModels();
    }

    public int getTotalModelBrand() {
        return deviceModelsFacade.totalModelBrand();
    }

    public int getTotalModelType() {
        return deviceModelsFacade.totalModelType();
    }

    public List<Devices> getModelDevices() {
        if (deviceModels == null || deviceModels.getId() == null) {
            return java.util.Collections.emptyList();
        }
        return devicesFacade.findByDeviceModelId(deviceModels.getId());
    }

    public List<DeviceModels> getFeaturedDevices() {
        try {
            List<DeviceModels> all = deviceModelsFacade.search("");
            if (all != null && !all.isEmpty()) {
                int limit = Math.min(8, all.size());
                return all.subList(0, limit);
            }
            return java.util.Collections.emptyList();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    public String formatPrice(long price) {
        return FormatUtil.formatPrice(price);
    }
}
