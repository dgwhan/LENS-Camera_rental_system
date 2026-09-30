package com.lens.auth;

import com.lens.auth.dto.RegisterRequest;
import com.lens.auth.security.SecurityRoles;
import com.lens.cart.model.CartService;
import com.lens.common.util.FacesUtil;
import com.lens.common.util.ValidationUtil;
import com.lens.user.entity.Users;
import com.lens.user.facade.UsersFacadeLocal;
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
 * Manages user authentication, registration, and logout operations.
 *
 * @author Duong Ngoc Han
 */
@Named(value = "authController")
@RequestScoped
public class AuthController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());

    @jakarta.ejb.EJB
    private AuthServiceLocal authService;

    @jakarta.ejb.EJB
    private UsersFacadeLocal usersFacade;

    @Inject
    private CartService cartService;

    @Inject
    private SecurityContext securityContext;

    private String username;
    private String password;

    private String fullName;
    private String email;
    private String phone;

    public AuthController() {
    }

    /**
     * Authenticates the user and loads the user's persistent cart.
     *
     * @return redirect page after authentication
     */
    public String login() {
        FacesContext context = FacesContext.getCurrentInstance();

        //get the current request and response
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        //create credentials from the submitted login information
        AuthenticationParameters authenticationParameters = AuthenticationParameters.withParams()
                .credential(new UsernamePasswordCredential(username, password));

        //authenticate the user through Jakarta Security
        AuthenticationStatus status = securityContext.authenticate(request, response, authenticationParameters);

        if (status == AuthenticationStatus.SUCCESS) {
            Users user = usersFacade.findByUsername(username);

            if (user != null) {
                cartService.loadUserCart(user.getId());
            }

            if (user != null && user.getFullName() != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("fullName", user.getFullName());
            }

            if (request.isUserInRole(SecurityRoles.ADMIN) || securityContext.isCallerInRole(SecurityRoles.ADMIN)) {
                return "/admin/dashboard?faces-redirect=true";
            }

            return "/client/pages/index?faces-redirect=true";
        }

        //continue the authentication flow when required
        if (status == AuthenticationStatus.SEND_CONTINUE) {
            return null;
        }

        LOGGER.warning("Authentication failed: Invalid username or password.");
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid username or password.", "Invalid username or password."));

        return null;
    }

    /**
     * Logs out the current user and invalidates the session.
     *
     * @return login page after logout
     */
    public String logout() {
        FacesContext context = FacesContext.getCurrentInstance();

        // get the current request
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        // logout through Jakarta Security
        try {
            request.logout();

            HttpSession session = request.getSession(false);

            if (session != null) {
                session.invalidate();
            }
        } catch (ServletException e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Logout failed", "Unable to logout."));
            return null;
        }

        return "/client/pages/login?faces-redirect=true";
    }

    /**
     * Returns the current authenticated user's full name.
     *
     * @return current user's full name
     */
    public String getCurrentUserFullName() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (context == null || context.getExternalContext() == null) {
            return "";
        }

        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        if (request == null || request.getUserPrincipal() == null) {
            return "";
        }

        HttpSession session = request.getSession(false);

        if (session != null) {
            String cachedFullName = (String) session.getAttribute("fullName");

            if (cachedFullName != null && !cachedFullName.trim().isEmpty()) {
                return cachedFullName;
            }
        }

        Users user = usersFacade.findByUsername(request.getUserPrincipal().getName());

        if (user != null && user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
            if (session != null) {
                session.setAttribute("fullName", user.getFullName());
            }

            return user.getFullName();
        }

        return request.getUserPrincipal().getName();
    }

    /**
     * Registers a new customer account.
     *
     * @return login page after successful registration
     */
    public String register() {
        boolean hasError = false;

        //validate username
        if (username == null || username.trim().isEmpty()) {
            FacesUtil.addFieldError("registerForm:username", "Username is required.");
            LOGGER.warning("Username is empty.");
            hasError = true;
        } else if (usersFacade.isUsernameExists(username.trim())) {
            FacesUtil.addFieldError("registerForm:username", "Username already exists.");
            LOGGER.warning("Username already exists.");
            hasError = true;
        }

        //validate password
        if (password == null || password.trim().isEmpty()) {
            FacesUtil.addFieldError("registerForm:password", "Password is required.");
            LOGGER.warning("Password is empty.");
            hasError = true;
        } else if (!ValidationUtil.isValidPassword(password)) {
            FacesUtil.addFieldError("registerForm:password", "Password must be at least 8 characters and contain both letters and numbers.");
            LOGGER.warning("Invalid password format.");
            hasError = true;
        }

        //validate phone number
        if (phone == null || phone.trim().isEmpty()) {
            FacesUtil.addFieldError("registerForm:phone", "Phone number is required.");
            LOGGER.warning("Phone number is empty.");
            hasError = true;
        } else {
            String trimmedPhone = phone.trim();

            if (!ValidationUtil.isValidPhone(trimmedPhone)) {
                FacesUtil.addFieldError("registerForm:phone", "Invalid phone number format (must be 10 digits starting with 0).");
                LOGGER.warning("Invalid phone number format.");
                hasError = true;
            } else if (usersFacade.isPhoneExists(trimmedPhone, null)) {
                FacesUtil.addFieldError("registerForm:phone", "Phone number is already in use.");
                LOGGER.warning("Phone number is already in use.");
                hasError = true;
            }
        }

        //validate email if provided
        if (email != null && !email.trim().isEmpty()) {
            String trimmedEmail = email.trim();

            if (!ValidationUtil.isValidEmail(trimmedEmail)) {
                FacesUtil.addFieldError("registerForm:email", "Invalid email format.");
                LOGGER.warning("Invalid email format.");
                hasError = true;
            } else if (usersFacade.isEmailExists(trimmedEmail, null)) {
                FacesUtil.addFieldError("registerForm:email", "Email is already in use.");
                LOGGER.warning("Email is already in use.");
                hasError = true;
            }
        }

        if (hasError) {
            return null;
        }

        //create registration request
        RegisterRequest request = new RegisterRequest(username.trim(), password, fullName != null ? fullName.trim() : null,
                (email != null && !email.trim().isEmpty()) ? email.trim() : null,
                phone.trim()
        );

        //register the user through the service
        Users user = authService.register(request);

        if (user == null) {
            LOGGER.warning("Registration failed: Registration rejected by service.");
            FacesUtil.addErrorMessage("Registration failed due to a system error. Please try again.");
            return null;
        }

        LOGGER.info("User '" + username.trim() + "' registered successfully.");

        return "/client/pages/login?faces-redirect=true";
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}