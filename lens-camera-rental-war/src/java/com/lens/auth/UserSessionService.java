package com.lens.auth;

import com.lens.user.entity.Users;
import com.lens.user.facade.UsersFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.security.Principal;

/**
 * Centralized session-scoped service for resolving and managing current user identity
 *
 * @author Duong Ngoc Han
 */
@Named(value = "userSessionService")
@SessionScoped
public class UserSessionService implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private UsersFacadeLocal usersFacade;

    private Integer currentUserId;
    private String currentUserFullName;
    private transient Users cachedUser;

    public Integer getCurrentUserId() {
        if (currentUserId != null) {
            return currentUserId;
        }

        Users user = resolveUserFromContext();
        if (user != null) {
            this.currentUserId = user.getId();
            this.currentUserFullName = user.getFullName();
            this.cachedUser = user;
            return this.currentUserId;
        }

        return null;
    }


    public Users getCurrentUser() {
        if (cachedUser != null) {
            return cachedUser;
        }

        Integer userId = getCurrentUserId();
        if (userId != null && usersFacade != null) {
            cachedUser = usersFacade.find(userId);
            return cachedUser;
        }

        return null;
    }

    public String getCurrentUserFullName() {
        if (currentUserFullName != null && !currentUserFullName.trim().isEmpty()) {
            return currentUserFullName;
        }

        Users user = getCurrentUser();
        if (user != null && user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
            this.currentUserFullName = user.getFullName();
            return this.currentUserFullName;
        }

        Principal principal = getPrincipalFromRequest();
        return principal != null ? principal.getName() : "";
    }

    public boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }

    public void onLogin(Users user) {
        if (user != null) {
            this.currentUserId = user.getId();
            this.currentUserFullName = user.getFullName();
            this.cachedUser = user;
        }
    }

    public void invalidate() {
        this.currentUserId = null;
        this.currentUserFullName = null;
        this.cachedUser = null;
    }

    private Users resolveUserFromContext() {
        Principal principal = getPrincipalFromRequest();
        if (principal != null && principal.getName() != null && !principal.getName().trim().isEmpty()) {
            if (usersFacade != null) {
                return usersFacade.findByUsername(principal.getName().trim());
            }
        }
        return null;
    }

    private Principal getPrincipalFromRequest() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null && context.getExternalContext() != null) {
                HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
                if (request != null) {
                    return request.getUserPrincipal();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
