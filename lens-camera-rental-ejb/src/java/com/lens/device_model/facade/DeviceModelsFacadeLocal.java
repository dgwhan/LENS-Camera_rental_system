package com.lens.device_model.facade;

import com.lens.device_model.entity.DeviceModels;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Local
public interface DeviceModelsFacadeLocal {

    void create(DeviceModels deviceModels);

    void edit(DeviceModels deviceModels);

    void remove(DeviceModels deviceModels);

    DeviceModels find(Object id);

    List<DeviceModels> findAll();

    List<DeviceModels> findRange(int[] range);

    int count();

    boolean isBrandModelExists(String brand, String model, Integer id);

    boolean isDeviceModelNameExists(String name, Integer id);

    List<DeviceModels> search(String keyword);

    List<DeviceModels> search(String keyword, String brand);

    List<DeviceModels> search(String keyword, String brand, String type);

    int totalDeviceModels();

    int totalModelBrand();

    int totalModelType();

    List<String> findDistinctTypes();

    List<String> findDistinctBrands();

}
