package ru.morkamo.kontrolbankdata.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
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
import ru.morkamo.kontrolbankdata.model.ManualWork;
import ru.morkamo.kontrolbankdata.constants.RecordValues;
import ru.morkamo.kontrolbankdata.service.ManualWorkService;
import ru.morkamo.kontrolbankdata.security.JournalType;
import ru.morkamo.kontrolbankdata.security.PermissionService;
import ru.morkamo.kontrolbankdata.security.AppUserPrincipal;
import ru.morkamo.kontrolbankdata.state.ApplicationState;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Controller
@RequestMapping("/manualwork")
@RequiredArgsConstructor
public class ManualWorkController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final ManualWorkService manualWorkService;
    private final PermissionService permissionService;
    private final ApplicationState applicationState;

    @GetMapping
    public String manualWork(
            @RequestParam(required = false) String pensionCaseNumber,
            @RequestParam(required = false) String pensionerName,
            @RequestParam(required = false) String period,
            @AuthenticationPrincipal AppUserPrincipal user,
            Model model) {

        model.addAttribute("records", manualWorkService.search(pensionCaseNumber, pensionerName, period));
        model.addAttribute("pensionCaseNumber", pensionCaseNumber);
        model.addAttribute("pensionerName", pensionerName);
        model.addAttribute("selectedPeriod", period);
        addPermissions(model, user.getDepartmentId());
        addRecordForm(model, new ManualWork(), null, null, null, false, false, "/manualwork/create");
        return "ManualWork";
    }

    @PostMapping("/create")
    public String create(
            ManualWork manualWork,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            @AuthenticationPrincipal AppUserPrincipal user,
            Model model) {

        try {
            validateOptionalId(manualWork.getId());
            validateManualWork(manualWork);
            requirePeriod(periodStart, periodEnd);
            if (manualWorkService.existsById(manualWork.getId())) {
                throw new FormValidationException("Такой ID уже занят, выберите другой.");
            }
            ManualWork newRecord = new ManualWork();
            newRecord.setId(manualWork.getId());
            copyAllFields(newRecord, manualWork);
            newRecord.setPeriod(formatPeriod(periodStart, periodEnd));
            manualWorkService.save(newRecord);
        } catch (FormValidationException exception) {
            return formError(manualWork, periodStart, periodEnd, user, model, exception.getMessage(),
                    false, "/manualwork/create");
        } catch (DataIntegrityViolationException exception) {
            return formError(manualWork, periodStart, periodEnd, user, model,
                    "Не удалось сохранить запись: введенные данные нарушают ограничения базы данных.",
                    false, "/manualwork/create");
        }
        return "redirect:/manualwork";
    }

    @PostMapping("/update/{id}")
    public String update(
            @PathVariable Integer id,
            ManualWork manualWork,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            @AuthenticationPrincipal AppUserPrincipal user,
            Model model) {

        Integer departmentId = user.getDepartmentId();
        requireUpdatePermission(departmentId);
        ManualWork existing = manualWorkService.getById(id);
        try {
            validateUsageMark(manualWork.getExecutionMark());
            if (permissionService.canEditAnyRecord(departmentId)) {
                validateManualWork(manualWork);
                requirePeriod(periodStart, periodEnd);
                copyAllFields(existing, manualWork);
                existing.setPeriod(formatPeriod(periodStart, periodEnd));
            } else {
                applyPermittedFields(existing, manualWork, departmentId);
            }
            manualWorkService.save(existing);
        } catch (FormValidationException exception) {
            return formError(manualWork, periodStart, periodEnd, user, model, exception.getMessage(),
                    true, "/manualwork/update/" + id);
        } catch (DataIntegrityViolationException exception) {
            return formError(manualWork, periodStart, periodEnd, user, model,
                    "Не удалось сохранить запись: введенные данные нарушают ограничения базы данных.",
                    true, "/manualwork/update/" + id);
        }
        return "redirect:/manualwork";
    }

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Integer id,
            @AuthenticationPrincipal AppUserPrincipal user) {
        if (!permissionService.canDeleteRecords(user.getDepartmentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        manualWorkService.delete(id);
        return "redirect:/manualwork";
    }

    private String formatPeriod(LocalDate periodStart, LocalDate periodEnd) {
        return "с " + periodStart.format(DATE_FORMATTER) + " по " + periodEnd.format(DATE_FORMATTER);
    }

    private void requirePeriod(LocalDate periodStart, LocalDate periodEnd) {
        if (periodStart == null || periodEnd == null) {
            throw new FormValidationException("Необходимо указать период");
        }
        if (periodEnd.isBefore(periodStart)) {
            throw new FormValidationException("Дата ПО не может быть раньше даты С");
        }
    }

    private void addPermissions(Model model, Integer departmentId) {
        model.addAttribute("canDelete", permissionService.canDeleteRecords(departmentId));
        model.addAttribute("canEditRecord", permissionService.canEditAnyRecord(departmentId));
        model.addAttribute("canOpenEdit", permissionService.canOpenEdit(departmentId, JournalType.MANUAL));
        model.addAttribute("editableFields", permissionService.editableFields(departmentId, JournalType.MANUAL));
        model.addAttribute("isAdministrator", permissionService.isAdministrator(departmentId));
        model.addAttribute("databaseLocked", applicationState.isDatabaseLocked());
    }

    private void addRecordForm(
            Model model,
            ManualWork record,
            LocalDate periodStart,
            LocalDate periodEnd,
            String error,
            boolean open,
            boolean editMode,
            String action) {
        model.addAttribute("formRecord", record);
        model.addAttribute("formPeriodStart", periodStart);
        model.addAttribute("formPeriodEnd", periodEnd);
        model.addAttribute("recordFormError", error);
        model.addAttribute("openRecordModal", open);
        model.addAttribute("recordFormEditMode", editMode);
        model.addAttribute("recordFormAction", action);
    }

    private String formError(
            ManualWork record,
            LocalDate periodStart,
            LocalDate periodEnd,
            AppUserPrincipal user,
            Model model,
            String error,
            boolean editMode,
            String action) {
        model.addAttribute("records", manualWorkService.search(null, null, null));
        addPermissions(model, user.getDepartmentId());
        addRecordForm(model, record, periodStart, periodEnd, error, true, editMode, action);
        return "ManualWork";
    }

    private void requireUpdatePermission(Integer departmentId) {
        if (!permissionService.canOpenEdit(departmentId, JournalType.MANUAL)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private void applyPermittedFields(ManualWork existing, ManualWork incoming, Integer departmentId) {
        if (permissionService.canEditField(departmentId, JournalType.MANUAL, "ovidSpecialist")) {
            existing.setOvidSpecialist(incoming.getOvidSpecialist());
        }
        if (permissionService.canEditField(departmentId, JournalType.MANUAL, "ovidNote")) {
            existing.setOvidNote(incoming.getOvidNote());
        }
        if (permissionService.canEditField(departmentId, JournalType.MANUAL, "executionMark")) {
            existing.setExecutionMark(incoming.getExecutionMark());
        }
    }

    private void copyAllFields(ManualWork target, ManualWork source) {
        target.setPensionCaseNumber(source.getPensionCaseNumber());
        target.setPensionerName(source.getPensionerName());
        target.setAccounting(source.getAccounting());
        target.setReason(source.getReason());
        target.setPaymentType(source.getPaymentType());
        target.setSource(source.getSource());
        target.setAmount(source.getAmount());
        target.setUrgent(source.getUrgent());
        target.setOvpSpecialist(source.getOvpSpecialist());
        target.setNote(source.getNote());
        target.setExecutionMark(source.getExecutionMark());
        target.setOvidSpecialist(source.getOvidSpecialist());
        target.setOvidNote(source.getOvidNote());
        target.setControlResult1(source.getControlResult1());
        target.setControlSpecialist1(source.getControlSpecialist1());
        target.setControlResult2(source.getControlResult2());
        target.setControlSpecialist2(source.getControlSpecialist2());
        target.setDistrict(source.getDistrict());
    }

    private void validateManualWork(ManualWork record) {
        validateRequiredChoice(record.getUrgent(), RecordValues.ALLOWED_URGENCY_VALUES, "срочность");
        validateRequiredChoice(record.getAccounting(), RecordValues.ALLOWED_ACCOUNTING_VALUES, "учет");
        validateRequiredChoice(record.getReason(), RecordValues.ALLOWED_MANUAL_REASONS, "причина");
        validateUsageMark(record.getExecutionMark());
        if (record.getDistrict() == null || record.getDistrict() < 0 || record.getDistrict() > 22) {
            throw new FormValidationException("Недопустимый район");
        }
    }

    private void validateRequiredChoice(String value, Set<String> allowedValues, String fieldName) {
        if (value == null || !allowedValues.contains(value)) {
            throw new FormValidationException("Недопустимое значение: " + fieldName);
        }
    }

    private void validateUsageMark(String value) {
        if (value != null && !value.isBlank() && !RecordValues.ALLOWED_EXECUTION_MARKS.contains(value)) {
            throw new FormValidationException("Недопустимое значение отметки об использовании");
        }
    }

    private void validateOptionalId(Integer id) {
        if (id != null && id <= 0) {
            throw new FormValidationException("ID должен быть положительным числом");
        }
    }
}
