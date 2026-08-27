package ru.morkamo.kontrolbankdata.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.morkamo.kontrolbankdata.service.UserAccountService;

@Controller
@RequiredArgsConstructor
public class LoginController {

    public static final String AUTH_SESSION_KEY = "authenticated";

    private final UserAccountService userAccountService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        if (userAccountService.canLogin(username, password)) {
            session.setAttribute(AUTH_SESSION_KEY, true);
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
