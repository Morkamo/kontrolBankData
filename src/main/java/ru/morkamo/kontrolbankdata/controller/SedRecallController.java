package ru.morkamo.kontrolbankdata.controller;

import java.util.Set;

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
import ru.morkamo.kontrolbankdata.model.SedRecall;
import ru.morkamo.kontrolbankdata.constants.RecordValues;
import ru.morkamo.kontrolbankdata.service.SedRecallService;
import ru.morkamo.kontrolbankdata.security.JournalType;
import ru.morkamo.kontrolbankdata.security.PermissionService;
import ru.morkamo.kontrolbankdata.security.AppUserPrincipal;
import ru.morkamo.kontrolbankdata.state.ApplicationState;

@Controller
@RequestMapping("/sedrecall")
@RequiredArgsConstructor
public class SedRecallController {

    private final SedRecallService sedRecallService;
    private final PermissionService permissionService;
    private final ApplicationState applicationState;

    @GetMapping
    public String sedRecall(
            @RequestParam(required = false) String pensionCaseNumber,
            @RequestParam(required = false) String pensionerName,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal AppUserPrincipal user,
            Model model) {

        model.addAttribute("records", sedRecallService.search(pensionCaseNumber, pensionerName, month, year));
        model.addAttribute("pensionCaseNumber", pensionCaseNumber);
        model.addAttribute("pensionerName", pensionerName);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        addPermissions(model, user.getDepartmentId());
        addRecordForm(model, new SedRecall(), null, false, false, "/sedrecall/create");
        return "SEDRecall";
    }

    @PostMapping("/create")
    public String create(
            SedRecall sedRecall,
            @AuthenticationPrincipal AppUserPrincipal user,
            Model model) {
        try {
            validateOptionalId(sedRecall.getId());
            validateSedRecall(sedRecall);
            if (sedRecallService.existsById(sedRecall.getId())) {
                throw new FormValidationException("Такой ID уже занят, выберите другой.");
            }
            SedRecall newRecord = new SedRecall();
            newRecord.setId(sedRecall.getId());
            copyAllFields(newRecord, sedRecall);
            sedRecallService.save(newRecord);
        } catch (FormValidationException exception) {
            return formError(sedRecall, user, model, exception.getMessage(), false, "/sedrecall/create");
        } catch (DataIntegrityViolationException exception) {
            return formError(sedRecall, user, model,
                    "Не удалось сохранить запись: введенные данные нарушают ограничения базы данных.",
                    false, "/sedrecall/create");
        }
        return "redirect:/sedrecall";
    }

    @PostMapping("/update/{id}")
    public String update(
            @PathVariable Integer id,
            SedRecall sedRecall,
            @AuthenticationPrincipal AppUserPrincipal user,
            Model model) {
        Integer departmentId = user.getDepartmentId();
        requireUpdatePermission(departmentId);
        SedRecall existing = sedRecallService.getById(id);
        try {
            validateUsageMark(sedRecall.getExecutionMark());
            if (permissionService.canEditAnyRecord(departmentId)) {
                validateSedRecall(sedRecall);
                copyAllFields(existing, sedRecall);
            } else {
                applyPermittedFields(existing, sedRecall, departmentId);
            }
            sedRecallService.save(existing);
        } catch (FormValidationException exception) {
            return formError(sedRecall, user, model, exception.getMessage(), true,
                    "/sedrecall/update/" + id);
        } catch (DataIntegrityViolationException exception) {
            return formError(sedRecall, user, model,
                    "Не удалось сохранить запись: введенные данные нарушают ограничения базы данных.",
                    true, "/sedrecall/update/" + id);
        }
        return "redirect:/sedrecall";
    }

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Integer id,
            @AuthenticationPrincipal AppUserPrincipal user) {
        if (!permissionService.canDeleteRecords(user.getDepartmentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        sedRecallService.delete(id);
        return "redirect:/sedrecall";
    }

    private void addPermissions(Model model, Integer departmentId) {
        model.addAttribute("canDelete", permissionService.canDeleteRecords(departmentId));
        model.addAttribute("canEditRecord", permissionService.canEditAnyRecord(departmentId));
        model.addAttribute("canOpenEdit", permissionService.canOpenEdit(departmentId, JournalType.SED));
        model.addAttribute("editableFields", permissionService.editableFields(departmentId, JournalType.SED));
        model.addAttribute("isAdministrator", permissionService.isAdministrator(departmentId));
        model.addAttribute("databaseLocked", applicationState.isDatabaseLocked());
    }

    private void addRecordForm(
            Model model,
            SedRecall record,
            String error,
            boolean open,
            boolean editMode,
            String action) {
        model.addAttribute("formRecord", record);
        model.addAttribute("recordFormError", error);
        model.addAttribute("openRecordModal", open);
        model.addAttribute("recordFormEditMode", editMode);
        model.addAttribute("recordFormAction", action);
    }

    private String formError(
            SedRecall record,
            AppUserPrincipal user,
            Model model,
            String error,
            boolean editMode,
            String action) {
        model.addAttribute("records", sedRecallService.search(null, null, null, null));
        addPermissions(model, user.getDepartmentId());
        addRecordForm(model, record, error, true, editMode, action);
        return "SEDRecall";
    }

    private void requireUpdatePermission(Integer departmentId) {
        if (!permissionService.canOpenEdit(departmentId, JournalType.SED)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private void applyPermittedFields(SedRecall existing, SedRecall incoming, Integer departmentId) {
        if (permissionService.canEditField(departmentId, JournalType.SED, "ovidSpecialist")) {
            existing.setOvidSpecialist(incoming.getOvidSpecialist());
        }
        if (permissionService.canEditField(departmentId, JournalType.SED, "ovidNote")) {
            existing.setOvidNote(incoming.getOvidNote());
        }
        if (permissionService.canEditField(departmentId, JournalType.SED, "executionMark")) {
            existing.setExecutionMark(incoming.getExecutionMark());
        }
    }

    private void copyAllFields(SedRecall target, SedRecall source) {
        target.setPensionCaseNumber(source.getPensionCaseNumber());
        target.setPensionerName(source.getPensionerName());
        target.setPackageNumber(source.getPackageNumber());
        target.setMonth(source.getMonth());
        target.setYear(source.getYear());
        target.setReason(source.getReason());
        target.setUrgent(source.getUrgent());
        target.setOvpSpecialist(source.getOvpSpecialist());
        target.setNote(source.getNote());
        target.setExecutionMark(source.getExecutionMark());
        target.setOvidSpecialist(source.getOvidSpecialist());
        target.setOvidNote(source.getOvidNote());
        target.setDistrict(source.getDistrict());
    }

    private void validateSedRecall(SedRecall record) {
        validateRequiredChoice(record.getUrgent(), RecordValues.ALLOWED_URGENCY_VALUES, "срочность");
        validateRequiredChoice(record.getReason(), RecordValues.ALLOWED_BANK_AND_SED_REASONS, "причина");
        validateUsageMark(record.getExecutionMark());
        validateDistrict(record.getDistrict());
        if (record.getMonth() == null || record.getMonth() < 1 || record.getMonth() > 12
                || record.getYear() == null || record.getYear() < 1900 || record.getYear() > 2100) {
            throw new FormValidationException("Недопустимый месяц или год");
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

    private void validateDistrict(Integer district) {
        if (district == null || district < 0 || district > 22) {
            throw new FormValidationException("Недопустимый район");
        }
    }

    private void validateOptionalId(Integer id) {
        if (id != null && id <= 0) {
            throw new FormValidationException("ID должен быть положительным числом");
        }
    }
}
