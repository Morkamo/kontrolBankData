package ru.morkamo.kontrolbankdata.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.morkamo.kontrolbankdata.controller.LoginController;
import ru.morkamo.kontrolbankdata.model.UserAccount;
import ru.morkamo.kontrolbankdata.service.UserAccountService;

import java.util.Objects;
import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final UserAccountService userAccountService;

    public AuthInterceptor(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, @NonNull Object handler)
            throws Exception {

        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession(false);
        if (session == null
                || !Boolean.TRUE.equals(session.getAttribute(LoginController.AUTH_SESSION_KEY))) {
            response.sendRedirect("/login");
            return false;
        }

        Object userIdAttribute = session.getAttribute(LoginController.USER_ID_SESSION_KEY);
        if (!(userIdAttribute instanceof Short userId)) {
            invalidateAndRedirect(session, response, "/login?sessionChanged=true");
            return false;
        }

        Optional<UserAccount> userAccount = userAccountService.findById(userId);
        if (userAccount.isEmpty()) {
            invalidateAndRedirect(session, response, "/login?sessionChanged=true");
            return false;
        }

        Integer sessionDepartment = (Integer) session.getAttribute(LoginController.DEPARTMENT_ID_SESSION_KEY);
        Integer databaseDepartment = userAccount.get().getDepartmentId();
        if (!Objects.equals(databaseDepartment, sessionDepartment)) {
            invalidateAndRedirect(session, response, "/login?permissionsChanged=true");
            return false;
        }

        if (requiresCsrfCheck(request.getMethod())) {
            String sessionToken = (String) session.getAttribute(LoginController.CSRF_SESSION_KEY);
            String requestToken = request.getParameter("_csrf");
            if (sessionToken == null || !Objects.equals(sessionToken, requestToken)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
                return false;
            }
        }

        return true;
    }

    private boolean requiresCsrfCheck(String method) {
        return !("GET".equalsIgnoreCase(method)
                || "HEAD".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method));
    }

    private void invalidateAndRedirect(HttpSession session, HttpServletResponse response, String location)
            throws Exception {
        session.invalidate();
        response.sendRedirect(location);
    }
}
