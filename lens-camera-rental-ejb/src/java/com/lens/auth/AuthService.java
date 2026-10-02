/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package com.lens.auth;

import com.lens.common.util.PasswordService;
import com.lens.user.facade.UsersFacadeLocal;
import com.lens.auth.dto.LoginRequest;
import com.lens.auth.dto.RegisterRequest;
import com.lens.user.entity.Users;
import jakarta.ejb.Stateless;
import java.util.Date;
import java.util.logging.Logger;
import com.lens.common.util.ValidationUtil;

/**
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class AuthService implements AuthServiceLocal {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());

    @jakarta.ejb.EJB
    private UsersFacadeLocal usersFacade;

    private PasswordService passwordService = new PasswordService();

    @Override
    public Users login(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return null;
        }

        Users user = usersFacade.findByUsername(request.getUsername());
        if (user == null) {
            return null;
        }

        //kiểm tra trạng thái tài khoản
        if (!"ACTIVE".equals(user.getStatus())) {
            return null;
        }

        //kiểm tra password
        if (!passwordService.verify(request.getPassword(), user.getPassword())) {
            return null;
        }
        return user;
    }

    @Override
    public Users register(RegisterRequest request) {
        if (request == null) {
            LOGGER.warning("Registration failed: RegisterRequest is null.");
            throw new IllegalArgumentException("Registration request cannot be null.");
        }

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            LOGGER.warning("Username is empty.");
            throw new IllegalArgumentException("Username is required.");
        }

        // kiểm tra username đã tồn tại
        if (usersFacade.isUsernameExists(request.getUsername().trim())) {
            LOGGER.warning("Username already exists.");
            throw new IllegalArgumentException("Username already exists.");
        }

        // kiểm tra format password
        if (request.getPassword() == null || !ValidationUtil.isValidPassword(request.getPassword())) {
            LOGGER.warning("Invalid password format.");
            throw new IllegalArgumentException("Password must be at least 8 characters and contain both letters and numbers.");
        }

        // kiểm tra format phone
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            LOGGER.warning("Phone is empty.");
            throw new IllegalArgumentException("Phone number is required.");
        }

        if (!ValidationUtil.isValidPhone(request.getPhone().trim())) {
            LOGGER.warning("Invalid phone number format.");
            throw new IllegalArgumentException("Invalid phone number format (must be 10 digits starting with 0).");
        }

        // kiểm tra phone đã tồn tại
        if (usersFacade.isPhoneExists(request.getPhone().trim(), null)) {
            LOGGER.warning("Phone number is already in use.");
            throw new IllegalArgumentException("Phone number is already in use.");
        }

        // kiểm tra format email nếu có giá trị
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!ValidationUtil.isValidEmail(request.getEmail().trim())) {
                LOGGER.warning("Invalid email format.");
                throw new IllegalArgumentException("Invalid email format.");
            }
            if (usersFacade.isEmailExists(request.getEmail().trim(), null)) {
                LOGGER.warning("Email is already in use.");
                throw new IllegalArgumentException("Email is already in use.");
            }
        }

        //tạo user mới
        Users user = new Users();

        //gán username
        user.setUsername(request.getUsername());

        //hash password trước khi lưu
        String hashedPassword = passwordService.hash(request.getPassword());

        //gán password đã hash
        user.setPassword(hashedPassword);

        //gán thông tin người dùng
        user.setFullName(request.getFullName());
        String email = (request.getEmail() != null && !request.getEmail().trim().isEmpty())
                ? request.getEmail().trim() : null;
        user.setEmail(email);
        user.setPhone(request.getPhone());

        //gán role mặc định cho user đăng ký
        user.setRole("CUSTOMER");

        //kích hoạt tài khoản
        user.setStatus("ACTIVE");

        Date now = new Date();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        //lưu db thông qua facade
        try {
            usersFacade.create(user);
            LOGGER.info("User registered successfully: " + user.getUsername());
            return user;
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Registration failed: Could not persist user '" + user.getUsername() + "' to database.", e);
            return null;
        }
    }

}
