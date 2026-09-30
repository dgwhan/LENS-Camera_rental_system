package com.lens.cart.facade;

import com.lens.cart.entity.CartItems;
import com.lens.device_model.entity.DeviceModels;
import jakarta.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * Declares business persistence operations for cart items.
 *
 * @author Duong Ngoc Han
 */
@Local
public interface CartItemsFacadeLocal {

    void create(CartItems cartItems);

    void edit(CartItems cartItems);

    void remove(CartItems cartItems);

    CartItems find(Object id);

    List<CartItems> findAll();

    List<CartItems> findRange(int[] range);

    int count();

    List<CartItems> findByUserId(Integer userId);

    CartItems findDuplicate(Integer userId, Integer deviceModelId, Date startDate, Date endDate);

    CartItems createCartItem(Integer userId, DeviceModels deviceModel, Date startDate, Date endDate, int duration);

    boolean deleteByIdAndUserId(Integer id, Integer userId);

    void deleteByUserId(Integer userId);

    CartItems updateRentalPeriod(Integer id, Integer userId, Date startDate, Date endDate, int duration);

}
