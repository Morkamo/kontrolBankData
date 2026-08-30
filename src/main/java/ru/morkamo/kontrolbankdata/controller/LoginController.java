package ru.morkamo.kontrolbankdata.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.morkamo.kontrolbankdata.model.UserAccount;
import ru.morkamo.kontrolbankdata.service.UserAccountService;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class LoginController {

    public static final String AUTH_SESSION_KEY = "authenticated";
    public static final String USER_ID_SESSION_KEY = "userId";
    public static final String USERNAME_SESSION_KEY = "username";
    public static final String DEPARTMENT_ID_SESSION_KEY = "departmentId";
    public static final String CSRF_SESSION_KEY = "csrfToken";

    private final UserAccountService userAccountService;

    @GetMapping("/login")
    public String login(
            @RequestParam(defaultValue = "false") boolean permissionsChanged,
            @RequestParam(defaultValue = "false") boolean sessionChanged,
            Model model) {
        if (permissionsChanged) {
            model.addAttribute("sessionMessage", "Ваши права доступа были изменены. Выполните вход повторно.");
        } else if (sessionChanged) {
            model.addAttribute("sessionMessage", "Учетная запись была изменена или удалена. Выполните вход повторно.");
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            HttpSession session,
            Model model) {

        UserAccount userAccount = userAccountService.findByCredentials(username, password).orElse(null);
        if (userAccount != null) {
            request.changeSessionId();
            session.setAttribute(AUTH_SESSION_KEY, true);
            session.setAttribute(USER_ID_SESSION_KEY, userAccount.getId());
            session.setAttribute(USERNAME_SESSION_KEY, userAccount.getUsername());
            session.setAttribute(DEPARTMENT_ID_SESSION_KEY, userAccount.getDepartmentId());
            session.setAttribute(CSRF_SESSION_KEY, UUID.randomUUID().toString());
            return "redirect:/";
        }

        model.addAttribute("error", "Неверный логин или пароль");
        model.addAttribute("username", username);
        return "login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
