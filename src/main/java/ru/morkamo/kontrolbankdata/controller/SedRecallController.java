package ru.morkamo.kontrolbankdata.controller;

import java.util.Set;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.morkamo.kontrolbankdata.model.SedRecall;
import ru.morkamo.kontrolbankdata.constants.RecordValues;
import ru.morkamo.kontrolbankdata.service.SedRecallService;
import ru.morkamo.kontrolbankdata.security.JournalType;
import ru.morkamo.kontrolbankdata.security.PermissionService;

@Controller
@RequestMapping("/sedrecall")
@RequiredArgsConstructor
public class SedRecallController {

    private final SedRecallService sedRecallService;
    private final PermissionService permissionService;

    @GetMapping
    public String sedRecall(
            @RequestParam(required = false) String pensionCaseNumber,
            @RequestParam(required = false) String pensionerName,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            HttpSession session,
            Model model) {

        model.addAttribute("records", sedRecallService.search(pensionCaseNumber, pensionerName, month, year));
        model.addAttribute("pensionCaseNumber", pensionCaseNumber);
        model.addAttribute("pensionerName", pensionerName);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        addPermissions(model, departmentId(session));
        return "SEDRecall";
    }

    @PostMapping("/create")
    public String create(SedRecall sedRecall) {
        validateSedRecall(sedRecall);
        SedRecall newRecord = new SedRecall();
        copyAllFields(newRecord, sedRecall);
        sedRecallService.save(newRecord);
        return "redirect:/sedrecall";
    }

    @PostMapping("/update/{id}")
    public String update(
            @PathVariable Integer id,
            SedRecall sedRecall,
            HttpSession session) {
        Integer departmentId = departmentId(session);
        requireUpdatePermission(departmentId);
        validateUsageMark(sedRecall.getExecutionMark());
        SedRecall existing = sedRecallService.getById(id);

        if (permissionService.canEditAnyRecord(departmentId)) {
            validateSedRecall(sedRecall);
            copyAllFields(existing, sedRecall);
        } else {
            applyPermittedFields(existing, sedRecall, departmentId);
        }

        sedRecallService.save(existing);
        return "redirect:/sedrecall";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, HttpSession session) {
        if (!permissionService.canDeleteRecords(departmentId(session))) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        sedRecallService.delete(id);
        return "redirect:/sedrecall";
    }

    private void addPermissions(Model model, Integer departmentId) {
        model.addAttribute("canDelete", permissionService.canDeleteRecords(departmentId));
        model.addAttribute("canEditRecord", permissionService.canEditAnyRecord(departmentId));
        model.addAttribute("canOpenEdit", permissionService.canEditAnyRecord(departmentId)
                || permissionService.canEditSomeFields(departmentId, JournalType.SED));
        model.addAttribute("editableFields", permissionService.editableFields(departmentId, JournalType.SED));
    }

    private void requireUpdatePermission(Integer departmentId) {
        if (!permissionService.canEditAnyRecord(departmentId)
                && !permissionService.canEditSomeFields(departmentId, JournalType.SED)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private Integer departmentId(HttpSession session) {
        return (Integer) session.getAttribute(LoginController.DEPARTMENT_ID_SESSION_KEY);
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
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Недопустимый месяц или год");
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

    private void validateDistrict(Integer district) {
        if (district == null || district < 0 || district > 22) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Недопустимый район");
        }
    }
}
