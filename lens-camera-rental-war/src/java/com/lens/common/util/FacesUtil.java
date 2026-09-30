package com.lens.common.util;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import java.io.IOException;

public class FacesUtil {

    private FacesUtil() {
    }

    public static void addFieldError(String clientId, String message) {
        FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    public static void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    public static void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    // Gửi phản hồi lỗi HTTP 404 về client để container hiển thị trang 404 chuẩn
    public static void redirectTo404(String message) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            ExternalContext ec = facesContext.getExternalContext();
            try {
                ec.responseSendError(404, message != null && !message.isBlank() ? message : "Resource not found");
                facesContext.responseComplete();
            } catch (IOException ex) {
                // Ignore fallback
            }
        }
    }

    public static void redirectTo404() {
        redirectTo404("Resource not found");
    }
}
