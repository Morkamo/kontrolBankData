package ru.morkamo.kontrolbankdata.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.morkamo.kontrolbankdata.security.AppUserPrincipal;
import ru.morkamo.kontrolbankdata.security.DepartmentIds;
import ru.morkamo.kontrolbankdata.state.ApplicationState;

@Component
public class DatabaseLockInterceptor implements HandlerInterceptor {

    private final ApplicationState applicationState;

    public DatabaseLockInterceptor(ApplicationState applicationState) {
        this.applicationState = applicationState;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, @NonNull Object handler)
            throws Exception {
        if (!applicationState.isDatabaseLocked()) {
            return true;
        }

        String path = request.getRequestURI();
        if (path.startsWith("/admin/users/") && !isAdministrator()) {
            return true;
        }
        response.sendRedirect(path.startsWith("/admin/users")
                ? "/admin/users?databaseLocked=true"
                : journalPath(path) + "?databaseLocked=true");
        return false;
    }

    private boolean isAdministrator() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication == null ? null : authentication.getPrincipal();
        return principal instanceof AppUserPrincipal user
                && user.getDepartmentId() == DepartmentIds.ADMINISTRATOR;
    }

    private String journalPath(String path) {
        int nextSlash = path.indexOf('/', 1);
        return nextSlash < 0 ? path : path.substring(0, nextSlash);
    }
}
