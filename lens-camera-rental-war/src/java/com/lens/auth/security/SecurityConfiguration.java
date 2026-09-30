package com.lens.auth.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;

/**
 *
 * @author Duong Ngoc Han
 */
@ApplicationScoped

//bảo vệ trường hợp user đang muốn truy cập một trang được bảo vệ trước khi login
@CustomFormAuthenticationMechanismDefinition(
    loginToContinue = @LoginToContinue(
        loginPage = "/faces/client/pages/login.xhtml",
        errorPage = "/faces/client/pages/login.xhtml"
    )
)
public class SecurityConfiguration {
}
