package ru.morkamo.kontrolbankdata.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(
            @RequestParam(defaultValue = "false") boolean error,
            @RequestParam(defaultValue = "false") boolean permissionsChanged,
            @RequestParam(defaultValue = "false") boolean sessionChanged,
            @RequestParam(defaultValue = "false") boolean logout,
            Model model) {
        if (error) {
            model.addAttribute("error", "Неверный логин или пароль");
        }
        if (permissionsChanged) {
            model.addAttribute("sessionMessage", "Ваши права доступа были изменены. Выполните вход повторно.");
        } else if (sessionChanged) {
            model.addAttribute("sessionMessage", "Учетная запись была изменена или удалена. Выполните вход повторно.");
        } else if (logout) {
            model.addAttribute("sessionMessage", "Вы вышли из системы.");
        }
        return "login";
    }
}
