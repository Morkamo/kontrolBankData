package ru.morkamo.kontrolbankdata.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.morkamo.kontrolbankdata.model.ManualWork;
import ru.morkamo.kontrolbankdata.constants.RecordValues;
import ru.morkamo.kontrolbankdata.service.ManualWorkService;
import ru.morkamo.kontrolbankdata.security.JournalType;
import ru.morkamo.kontrolbankdata.security.PermissionService;

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

    @GetMapping
    public String manualWork(
            @RequestParam(required = false) String pensionCaseNumber,
            @RequestParam(required = false) String pensionerName,
            @RequestParam(required = false) String period,
            HttpSession session,
            Model model) {

        model.addAttribute("records", manualWorkService.search(pensionCaseNumber, pensionerName, period));
        model.addAttribute("pensionCaseNumber", pensionCaseNumber);
        model.addAttribute("pensionerName", pensionerName);
        model.addAttribute("selectedPeriod", period);
        addPermissions(model, departmentId(session));
        return "ManualWork";
    }

    @PostMapping("/create")
    public String create(
            ManualWork manualWork,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd) {

        validateManualWork(manualWork);
        ManualWork newRecord = new ManualWork();
        copyAllFields(newRecord, manualWork);
        newRecord.setPeriod(formatPeriod(periodStart, periodEnd));
        manualWorkService.save(newRecord);
        return "redirect:/manualwork";
    }

    @PostMapping("/update/{id}")
    public String update(
            @PathVariable Integer id,
            ManualWork manualWork,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            HttpSession session) {

        Integer departmentId = departmentId(session);
        requireUpdatePermission(departmentId);
        validateUsageMark(manualWork.getExecutionMark());
        ManualWork existing = manualWorkService.getById(id);
        if (permissionService.canEditAnyRecord(departmentId)) {
            validateManualWork(manualWork);
            requirePeriod(periodStart, periodEnd);
            copyAllFields(existing, manualWork);
            existing.setPeriod(formatPeriod(periodStart, periodEnd));
        } else {
            applyPermittedFields(existing, manualWork, departmentId);
        }
        manualWorkService.save(existing);
        return "redirect:/manualwork";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, HttpSession session) {
        if (!permissionService.canDeleteRecords(departmentId(session))) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        manualWorkService.delete(id);
        return "redirect:/manualwork";
    }

    private String formatPeriod(LocalDate periodStart, LocalDate periodEnd) {
        return "с " + periodStart.format(DATE_FORMATTER) + " по " + periodEnd.format(DATE_FORMATTER);
    }

    private void requirePeriod(LocalDate periodStart, LocalDate periodEnd) {
        if (periodStart == null || periodEnd == null) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Необходимо указать период");
        }
    }

    private void addPermissions(Model model, Integer departmentId) {
        model.addAttribute("canDelete", permissionService.canDeleteRecords(departmentId));
        model.addAttribute("canEditRecord", permissionService.canEditAnyRecord(departmentId));
        model.addAttribute("canOpenEdit", permissionService.canEditAnyRecord(departmentId)
                || permissionService.canEditSomeFields(departmentId, JournalType.MANUAL));
        model.addAttribute("editableFields", permissionService.editableFields(departmentId, JournalType.MANUAL));
    }

    private void requireUpdatePermission(Integer departmentId) {
        if (!permissionService.canEditAnyRecord(departmentId)
                && !permissionService.canEditSomeFields(departmentId, JournalType.MANUAL)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private Integer departmentId(HttpSession session) {
        return (Integer) session.getAttribute(LoginController.DEPARTMENT_ID_SESSION_KEY);
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
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Недопустимый район");
        }
    }

    private void validateRequiredChoice(String value, Set<String> allowedValues, String fieldName) {
        if (value == null || !allowedValues.contains(value)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Недопустимое значение: " + fieldName);
        }
    }

    private void validateUsageMark(String value) {
        if (value != null && !value.isBlank() && !RecordValues.ALLOWED_EXECUTION_MARKS.contains(value)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Недопустимое значение отметки об использовании");
        }
    }
}
