package ru.morkamo.kontrolbankdata.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.morkamo.kontrolbankdata.model.UserAccount;
import ru.morkamo.kontrolbankdata.security.AppUserPrincipal;
import ru.morkamo.kontrolbankdata.service.UserAccountService;

import java.util.Objects;
import java.util.Optional;

@Component
public class UserStateInterceptor implements HandlerInterceptor {

    private final UserAccountService userAccountService;
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    public UserStateInterceptor(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, @NonNull Object handler)
            throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal user)) {
            return true;
        }

        Optional<UserAccount> userAccount = userAccountService.findById(user.getId());
        if (userAccount.isEmpty()) {
            logoutAndRedirect(request, response, authentication, "/login?sessionChanged=true");
            return false;
        }

        if (!Objects.equals(userAccount.get().getDepartmentId(), user.getDepartmentId())) {
            logoutAndRedirect(request, response, authentication, "/login?permissionsChanged=true");
            return false;
        }

        return true;
    }

    private void logoutAndRedirect(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication,
            String location) throws Exception {
        logoutHandler.logout(request, response, authentication);
        response.sendRedirect(location);
    }
}
