package com.lens.user.facade;

import com.lens.user.entity.Users;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Local
public interface UsersFacadeLocal {

    void create(Users users);

    void edit(Users users);

    void remove(Users users);

    Users find(Object id);

    List<Users> findAll();

    List<Users> findRange(int[] range);

    int count();
    
    List<Users> search(String keyword, String role);

    List<Users> search(String keyword, String role, String status);

    Users findByUsername(String username);

    boolean isUsernameExists(String username);

    boolean isEmailExists(String email, Integer id);

    boolean isPhoneExists(String phone, Integer id);
    
    int totalUsers();

    int totalAdminRole();

    int totalCustomerRole();

    int totalActiveUsers();

    int totalInactiveUsers();

}
