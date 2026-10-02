package com.lens.auth;

import com.lens.auth.security.SecurityRoles;
import com.lens.cart.model.CartService;
import com.lens.user.entity.Users;
import com.lens.user.facade.UsersFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.logging.Logger;

/**
 * Handles customer and administrator login operations.
 *
 * @author Duong Ngoc Han
 */
@Named(value = "loginController")
@RequestScoped
public class LoginController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @EJB
    private UsersFacadeLocal usersFacade;

    @Inject
    private CartService cartService;

    @Inject
    private UserSessionService userSessionService;

    @Inject
    private SecurityContext securityContext;

    private String username;
    private String password;

    public LoginController() {
    }

    public String login() {
        FacesContext context = FacesContext.getCurrentInstance();

        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        AuthenticationParameters authenticationParameters = AuthenticationParameters.withParams()
                .credential(new UsernamePasswordCredential(username, password));

        AuthenticationStatus status = securityContext.authenticate(request, response, authenticationParameters);

        if (status == AuthenticationStatus.SUCCESS) {
            Users user = usersFacade.findByUsername(username);

            if (user != null) {
                cartService.loadUserCart(user.getId());
                userSessionService.onLogin(user);

                HttpSession session = request.getSession(true);
                session.setAttribute("fullName", user.getFullName());
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRole());
            }

            if (request.isUserInRole(SecurityRoles.ADMIN) || securityContext.isCallerInRole(SecurityRoles.ADMIN)) {
                return "/admin/dashboard?faces-redirect=true";
            }

            return "/client/pages/index?faces-redirect=true";
        }

        if (status == AuthenticationStatus.SEND_CONTINUE) {
            return null;
        }

        LOGGER.warning("Authentication failed: Invalid username or password.");
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Invalid username or password.", "Invalid username or password."));

        return null;
    }

    public String logout() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        try {
            request.logout();

            if (userSessionService != null) {
                userSessionService.invalidate();
            }

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
        } catch (ServletException e) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Logout failed", "Unable to logout."));
            return null;
        }

        return "/auth/login?faces-redirect=true";
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
