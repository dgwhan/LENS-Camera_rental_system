package com.lens.auth;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

/**
 * Authentication navigation and session facade.
 *
 * @author Duong Ngoc Han
 */
@Named(value = "authController")
@RequestScoped
public class AuthController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private LoginController loginController;

    @Inject
    private UserSessionService userSessionService;

    private String username;
    private String password;

    public AuthController() {
    }

    public String login() {
        loginController.setUsername(username);
        loginController.setPassword(password);
        return loginController.login();
    }

    public String logout() {
        return loginController.logout();
    }

    public String getCurrentUserFullName() {
        return userSessionService != null ? userSessionService.getCurrentUserFullName() : "";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}