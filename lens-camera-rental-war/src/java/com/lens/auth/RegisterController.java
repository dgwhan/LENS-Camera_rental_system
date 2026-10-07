package com.lens.auth;

import com.lens.auth.dto.RegisterRequest;
import com.lens.common.util.FacesUtil;
import com.lens.user.entity.Users;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.logging.Logger;

/**
 * Handles user account registration form interactions.
 *
 * @author Duong Ngoc Han
 */
@Named(value = "registerController")
@RequestScoped
public class RegisterController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(RegisterController.class.getName());

    @EJB
    private AuthServiceLocal authService;

    private String fullName;
    private String username;
    private String phone;
    private String email;
    private String password;

    public RegisterController() {
    }

    public String register() {
        RegisterRequest request = new RegisterRequest(
                username != null ? username.trim() : null,
                password,
                fullName != null ? fullName.trim() : null,
                (email != null && !email.trim().isEmpty()) ? email.trim() : null,
                phone != null ? phone.trim() : null
        );

        try {
            Users user = authService.register(request);
            if (user == null) {
                FacesUtil.addErrorMessage("Registration failed due to a system error. Please try again.");
                return null;
            }

            LOGGER.info("User registered successfully: " + user.getUsername());
            return "/auth/login?faces-redirect=true";

        } catch (IllegalArgumentException ex) {
            showRegistrationError(ex);
            return null;
        } catch (Exception ex) {
            // EJB proxies wrap application exceptions in EJBException. Unwrap
            // it so validation feedback is shown in the form instead of a 500.
            showRegistrationError(ex);
            return null;
        }
    }

    private void showRegistrationError(Throwable exception) {
        Throwable rootCause = exception;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }

        String message = rootCause.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = "Registration failed due to a system error. Please try again.";
        }
        LOGGER.warning("Registration failed: " + message);

        if (message.contains("Username")) {
            FacesUtil.addFieldError("registerForm:username", message);
        } else if (message.contains("Password")) {
            FacesUtil.addFieldError("registerForm:password", message);
        } else if (message.toLowerCase().contains("phone")) {
            FacesUtil.addFieldError("registerForm:phone", message);
        } else if (message.toLowerCase().contains("email")) {
            FacesUtil.addFieldError("registerForm:email", message);
        } else {
            FacesUtil.addErrorMessage(message);
        }
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
