package ru.morkamo.kontrolbankdata.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.morkamo.kontrolbankdata.controller.LoginController;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, @NonNull Object handler)
            throws Exception {

        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession(false);
        Object authenticated = session == null ? null : session.getAttribute(LoginController.AUTH_SESSION_KEY);

        if (Boolean.TRUE.equals(authenticated)) {
            return true;
        }

        response.sendRedirect("/login");
        return false;
    }
}
