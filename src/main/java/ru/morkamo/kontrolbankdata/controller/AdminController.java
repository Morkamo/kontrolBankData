package ru.morkamo.kontrolbankdata.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import ru.morkamo.kontrolbankdata.model.UserAccount;
import ru.morkamo.kontrolbankdata.model.UserDepartment;
import ru.morkamo.kontrolbankdata.repository.UserDepartmentRepository;
import ru.morkamo.kontrolbankdata.security.AppUserPrincipal;
import ru.morkamo.kontrolbankdata.security.DepartmentIds;
import ru.morkamo.kontrolbankdata.security.PermissionService;
import ru.morkamo.kontrolbankdata.service.UserAccountService;
import ru.morkamo.kontrolbankdata.state.ApplicationState;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserAccountService userAccountService;
    private final UserDepartmentRepository userDepartmentRepository;
    private final PermissionService permissionService;
    private final ApplicationState applicationState;

    @GetMapping("/users")
    public String users(
            @AuthenticationPrincipal AppUserPrincipal administrator,
            @RequestParam(defaultValue = "false") boolean databaseLocked,
            Model model) {
        requireAdministrator(administrator);
        addUsersModel(model, databaseLocked);
        return "UserManagement";
    }

    private void addUsersModel(Model model, boolean databaseLockedMessage) {
        List<UserDepartment> departments = userDepartmentRepository.findAll().stream()
                .filter(department -> department.getId() != DepartmentIds.ADMINISTRATOR)
                .toList();
        List<DepartmentView> departmentViews = departments.stream()
                .map(department -> new DepartmentView(department.getId(), departmentDisplayName(department.getId())))
                .toList();
        Map<Integer, String> departmentNames = departmentViews.stream()
                .collect(Collectors.toMap(DepartmentView::id, DepartmentView::displayName));
        model.addAttribute("users", userAccountService.findAllManagedUsers());
        model.addAttribute("departments", departmentViews);
        model.addAttribute("departmentNames", departmentNames);
        model.addAttribute("formUser", new UserAccount());
        model.addAttribute("userFormError", null);
        model.addAttribute("openUserModal", false);
        model.addAttribute("userFormEditMode", false);
        model.addAttribute("userFormAction", "/admin/users/create");
        model.addAttribute("isAdministrator", true);
        model.addAttribute("databaseLocked", applicationState.isDatabaseLocked());
        model.addAttribute("databaseLockedMessage", databaseLockedMessage);
    }

    @PostMapping("/database-lock")
    public String databaseLock(
            @RequestParam boolean locked,
            @AuthenticationPrincipal AppUserPrincipal administrator) {
        requireAdministrator(administrator);
        applicationState.setDatabaseLocked(locked);
        return "redirect:/";
    }

    @PostMapping("/users/{id}/update")
    public String updateUser(
            @PathVariable Short id,
            UserAccount values,
            @AuthenticationPrincipal AppUserPrincipal administrator,
            Model model) {
        requireAdministrator(administrator);
        UserAccount target = managedUser(id);
        try {
            validate(values);
            if (!id.equals(values.getId()) && userAccountService.existsById(values.getId())) {
                throw new FormValidationException("Такой ID уже занят, выберите другой.");
            }
            userAccountService.updateUser(target.getId(), values);
        } catch (FormValidationException exception) {
            return userFormError(model, values, exception.getMessage(), true,
                    "/admin/users/" + id + "/update");
        } catch (DataIntegrityViolationException exception) {
            return userFormError(model, values,
                    "Не удалось сохранить пользователя: введенные данные нарушают ограничения базы данных.",
                    true, "/admin/users/" + id + "/update");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/create")
    public String createUser(
            UserAccount values,
            @AuthenticationPrincipal AppUserPrincipal administrator,
            Model model) {
        requireAdministrator(administrator);
        try {
            validate(values);
            if (userAccountService.existsById(values.getId())) {
                throw new FormValidationException("Такой ID уже занят, выберите другой.");
            }
            userAccountService.createUser(values);
        } catch (FormValidationException exception) {
            return userFormError(model, values, exception.getMessage(), false, "/admin/users/create");
        } catch (DataIntegrityViolationException exception) {
            return userFormError(model, values,
                    "Не удалось сохранить пользователя: введенные данные нарушают ограничения базы данных.",
                    false, "/admin/users/create");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(
            @PathVariable Short id,
            @AuthenticationPrincipal AppUserPrincipal administrator) {
        requireAdministrator(administrator);
        userAccountService.delete(managedUser(id));
        return "redirect:/admin/users";
    }

    private void requireAdministrator(AppUserPrincipal user) {
        if (user == null || !permissionService.isAdministrator(user.getDepartmentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private UserAccount managedUser(Short id) {
        UserAccount target = userAccountService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (permissionService.isAdministrator(target.getDepartmentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return target;
    }

    private void validate(UserAccount user) {
        if (user.getId() == null || user.getId() <= 0) {
            throw new FormValidationException("ID должен быть положительным числом");
        }
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new FormValidationException("Логин обязателен");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new FormValidationException("Пароль обязателен");
        }
        if (user.getPassword().length() > 8) {
            throw new FormValidationException("Пароль не может быть длиннее 8 символов");
        }
        if (user.getDepartmentId() == null
                || user.getDepartmentId() == DepartmentIds.ADMINISTRATOR
                || !userDepartmentRepository.existsById(user.getDepartmentId())) {
            throw new FormValidationException("Недопустимый отдел");
        }
        validateLength(user.getUsername(), 40, "Логин");
        validateLength(user.getFullName(), 40, "ФИО");
        validateLength(user.getComputerName(), 20, "Имя компьютера");
        validateLength(user.getStamp(), 100, "Штамп");
    }

    private void validateLength(String value, int maxLength, String field) {
        if (value != null && value.length() > maxLength) {
            throw new FormValidationException(field + " превышает допустимую длину");
        }
    }

    private String userFormError(
            Model model,
            UserAccount values,
            String error,
            boolean editMode,
            String action) {
        addUsersModel(model, false);
        model.addAttribute("formUser", values);
        model.addAttribute("userFormError", error);
        model.addAttribute("openUserModal", true);
        model.addAttribute("userFormEditMode", editMode);
        model.addAttribute("userFormAction", action);
        return "UserManagement";
    }

    private String departmentDisplayName(Integer departmentId) {
        return switch (departmentId) {
            case DepartmentIds.CONTROL -> "Контроль";
            case DepartmentIds.DB_MANAGEMENT -> "Управление базой данных";
            case DepartmentIds.BILLINGS_1 -> "Начисления №1";
            case DepartmentIds.BILLINGS_2 -> "Начисления №2";
            default -> String.valueOf(departmentId);
        };
    }

    private record DepartmentView(Integer id, String displayName) {
    }
}
