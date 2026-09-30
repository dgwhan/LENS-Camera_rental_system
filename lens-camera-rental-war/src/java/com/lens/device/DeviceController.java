package com.lens.device;

import com.lens.device.entity.Devices;
import com.lens.device_model.entity.DeviceModels;
import com.lens.device.facade.DevicesFacadeLocal;
import com.lens.device_model.facade.DeviceModelsFacadeLocal;
import com.lens.common.util.FacesUtil;
import com.lens.common.util.ImageUtil;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Named(value = "deviceController")
@ViewScoped
public class DeviceController implements Serializable {

    @jakarta.ejb.EJB
    private DevicesFacadeLocal devicesFacade;

    @jakarta.ejb.EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    private Devices devices = new Devices();
    private Integer selectedDeviceModelId;
    private boolean editMode;
    private String keyword = "";
    private String status = "";
    private Integer id;

    public DeviceController() {
        devices.setStatus("AVAILABLE");
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void initDetail() {
        if (id != null && (devices == null || devices.getId() == null || !devices.getId().equals(id))) {
            devices = devicesFacade.find(id);
        }
    }

    public void initForm() {
        if (id != null && (devices == null || devices.getId() == null || !devices.getId().equals(id))) {
            devices = devicesFacade.find(id);
            if (devices != null) {
                editMode = true;
                if (devices.getDeviceModelId() != null) {
                    selectedDeviceModelId = devices.getDeviceModelId().getId();
                }
            }
        }
    }

    //insert 
    public String newDevice() {
        devices = new Devices();
        devices.setStatus("AVAILABLE");
        selectedDeviceModelId = null;
        editMode = false;
        id = null;
        return "form?faces-redirect=true";
    }

    public String insertDevice() {
        boolean hasError = false;
        if (selectedDeviceModelId == null) {
            FacesUtil.addFieldError("deviceForm:deviceModel", "Device model is required.");
            hasError = true;
        }
        if (isDuplicateSerialNumber(null)) {
            hasError = true;
        }
        if (hasError) {
            return null;
        }
        try {
            DeviceModels dm = deviceModelsFacade.find(selectedDeviceModelId);
            devices.setDeviceModelId(dm);
            devices.setStatus("AVAILABLE");

            Date now = new Date();
            devices.setCreatedAt(now);
            devices.setUpdatedAt(now);

            devicesFacade.create(devices);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert", "Device created successfully.");
            return "list?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            FacesUtil.addErrorMessage("Failed to create device.");
            return null;
        }
    }
    
    // Mở form chỉnh sửa thiết bị
    public String editDevice(Integer id) {
        this.id = id;
        devices = devicesFacade.find(id);
        if (devices == null) {
            return "list?faces-redirect=true";
        }
        if (devices.getDeviceModelId() != null) {
            selectedDeviceModelId = devices.getDeviceModelId().getId();
        } else {
            selectedDeviceModelId = null;
        }
        editMode = true;
        return "form?faces-redirect=true&id=" + id;
    }


    // Cập nhật thông tin thiết bị
    public String updateDevice() {
        if (devices == null || (devices.getId() == null && id == null)) {
            FacesUtil.addErrorMessage("Device not found.");
            return null;
        }
        if (devices.getId() == null && id != null) {
            devices.setId(id);
        }

        boolean hasError = false;
        if (selectedDeviceModelId == null && devices.getDeviceModelId() == null) {
            FacesUtil.addFieldError("deviceForm:deviceModel", "Device model is required.");
            hasError = true;
        }
        if (isDuplicateSerialNumber(devices.getId())) {
            hasError = true;
        }

        if (hasError) {
            return null;
        }

        try {
            if (selectedDeviceModelId != null) {
                DeviceModels dm = deviceModelsFacade.find(selectedDeviceModelId);
                devices.setDeviceModelId(dm);
            }
            devices.setUpdatedAt(new Date());
            devicesFacade.edit(devices);

            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert", "Device updated successfully.");
            return "list?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            FacesUtil.addErrorMessage("Failed to update device.");
            return null;
        }
    }

    // Xem chi tiết thiết bị
    public String detailDevice(Integer id) {
        this.id = id;
        devices = devicesFacade.find(id);
        if (devices == null) {
            return "list?faces-redirect=true";
        }
        return "detail?faces-redirect=true&id=" + id;
    }


    // Xóa thiết bị
    public void deleteDevice(Integer id) {
        try {
            Devices d = devicesFacade.find(id);
            if (d != null) {
                devicesFacade.remove(d);
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesUtil.addErrorMessage("Failed to delete device.");
        }
    }

    // Lấy danh sách thiết bị (hỗ trợ tìm kiếm theo từ khóa và trạng thái)
    public List<Devices> showAllDevices() {
        return devicesFacade.findAll();
    }

    public List<Devices> getDevicesList() {
        return devicesFacade.search(keyword, status);
    }

    // Đặt lại bộ lọc tìm kiếm
    public void resetFilter() {
        this.keyword = "";
        this.status = "";
    }

    public List<DeviceModels> getAllDeviceModels() {
        return deviceModelsFacade.findAll();
    }

    // Hỗ trợ hiển thị ảnh của model thiết bị
    public String getImageUrl(String imageUrl) {
        return ImageUtil.getDeviceImageUrl(imageUrl);
    }

    public String getDefaultImageUrl() {
        return ImageUtil.getDefaultImageUrl();
    }

    public String getModelImageUrl() {
        if (devices != null && devices.getDeviceModelId() != null) {
            return ImageUtil.getDeviceImageUrl(devices.getDeviceModelId().getImageUrl());
        }
        return ImageUtil.getDefaultImageUrl();
    }

    //validate 
    private boolean isDuplicateSerialNumber(Integer excludeId) {
        if (devicesFacade.isSerialNumber(devices.getSerialNumber(), excludeId)) {
            FacesUtil.addFieldError("deviceForm:serialNumber", "SerialNumber already exits.");
            return true;
        }
        return false;
    }

    public Devices getDevices() {
        return devices;
    }

    public void setDevices(Devices devices) {
        this.devices = devices;
    }

    public Integer getSelectedDeviceModelId() {
        if (selectedDeviceModelId != null) {
            return selectedDeviceModelId;
        }
        if (devices != null && devices.getDeviceModelId() != null) {
            return devices.getDeviceModelId().getId();
        }
        return null;
    }

    public void setSelectedDeviceModelId(Integer selectedDeviceModelId) {
        this.selectedDeviceModelId = selectedDeviceModelId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalDevices() {
        return devicesFacade.totalDevices();
    }

    public int getTotalDevicesAvailable() {
        return devicesFacade.totalDevicesAvailable();
    }

    public int getTotalDevicesRenting() {
        return devicesFacade.totalDevicesRenting();
    }

}
