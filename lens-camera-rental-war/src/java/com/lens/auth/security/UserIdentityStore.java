package com.lens.auth.security;

import com.lens.common.util.PasswordService;
import com.lens.user.facade.UsersFacadeLocal;
import com.lens.user.entity.Users;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.CallerPrincipal;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author Duong Ngoc Han
 */
@ApplicationScoped
public class UserIdentityStore implements IdentityStore {

    private static final Logger LOGGER = Logger.getLogger(UserIdentityStore.class.getName());

    @EJB
    private UsersFacadeLocal usersFacade;

    private PasswordService passwordService = new PasswordService();

    @Override
    public CredentialValidationResult validate(Credential credential) {

        //chỉ xử lý username/password credential
        if (!(credential instanceof UsernamePasswordCredential)) {
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }

        UsernamePasswordCredential userCredential = (UsernamePasswordCredential) credential;

        //lấy username và password
        String username = userCredential.getCaller();
        String password = userCredential.getPasswordAsString();

        if (username == null || password == null) {
            LOGGER.warning("Authentication failed: Invalid username or password.");
            return CredentialValidationResult.INVALID_RESULT;
        }

        //tìm user
        Users user = usersFacade.findByUsername(username);

        //kiểm tra user tồn tại, status ACTIVE, và verify password
        if (user == null || !"ACTIVE".equals(user.getStatus()) || !passwordService.verify(password, user.getPassword())) {
            LOGGER.warning("Authentication failed: Invalid username or password.");
            return CredentialValidationResult.INVALID_RESULT;
        }

        //authentication thành công - trả về CallerPrincipal(đại diện cho username) + role
        Set<String> roles = (user.getRole() != null && !user.getRole().trim().isEmpty())
                ? Set.of(user.getRole().trim())
                : Set.of();

        return new CredentialValidationResult(new CallerPrincipal(user.getUsername()), roles);
    }
}
