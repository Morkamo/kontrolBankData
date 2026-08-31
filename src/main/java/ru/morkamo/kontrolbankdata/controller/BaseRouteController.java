package ru.morkamo.kontrolbankdata.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.morkamo.kontrolbankdata.security.AppUserPrincipal;
import ru.morkamo.kontrolbankdata.security.PermissionService;
import ru.morkamo.kontrolbankdata.state.ApplicationState;

@Controller
@RequiredArgsConstructor
public class BaseRouteController {

    private final PermissionService permissionService;
    private final ApplicationState applicationState;

    @GetMapping("/")
    public String index(@AuthenticationPrincipal AppUserPrincipal user, Model model) {
        model.addAttribute("isAdministrator", permissionService.isAdministrator(user.getDepartmentId()));
        model.addAttribute("databaseLocked", applicationState.isDatabaseLocked());
        return "index";
    }
}
