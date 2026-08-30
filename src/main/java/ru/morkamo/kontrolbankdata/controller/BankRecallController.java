package ru.morkamo.kontrolbankdata.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.morkamo.kontrolbankdata.model.BankRecall;
import ru.morkamo.kontrolbankdata.constants.RecordValues;
import ru.morkamo.kontrolbankdata.service.BankRecallService;
import ru.morkamo.kontrolbankdata.service.DeliveryOrganizationService;
import ru.morkamo.kontrolbankdata.security.JournalType;
import ru.morkamo.kontrolbankdata.security.PermissionService;

@Controller
@RequestMapping("/bankrecall")
@RequiredArgsConstructor
public class BankRecallController {

    private static final DateTimeFormatter PERIOD_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final BankRecallService bankRecallService;
    private final DeliveryOrganizationService deliveryOrganizationService;
    private final PermissionService permissionService;

    @GetMapping
    public String bankRecall(
            @RequestParam(required = false) String pensionCaseNumber,
            @RequestParam(required = false) String pensionerName,
            @RequestParam(required = false) String bank,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            HttpSession session,
            Model model) {

        String period = formatPeriodSearch(periodStart, periodEnd);

        model.addAttribute("records", bankRecallService.search(
                pensionCaseNumber,
                pensionerName,
                bank,
                month,
                year,
                period));
        model.addAttribute("banks", deliveryOrganizationService.getAll());
        model.addAttribute("pensionCaseNumber", pensionCaseNumber);
        model.addAttribute("pensionerName", pensionerName);
        model.addAttribute("selectedBank", bank);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedPeriodStart", periodStart);
        model.addAttribute("selectedPeriodEnd", periodEnd);
        addPermissions(model, departmentId(session));
        return "BankRecall";
    }

    @PostMapping("/create")
    public String create(
            BankRecall bankRecall,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd) {

        validateBankRecall(bankRecall);
        BankRecall newRecord = new BankRecall();
        copyAllFields(newRecord, bankRecall);
        newRecord.setPeriod(formatPeriod(periodStart, periodEnd));
        bankRecallService.save(newRecord);
        return "redirect:/bankrecall";
    }

    @PostMapping("/update/{id}")
    public String update(
            @PathVariable Integer id,
            BankRecall bankRecall,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            HttpSession session) {

        Integer departmentId = departmentId(session);
        requireUpdatePermission(departmentId);
        validateUsageMark(bankRecall.getExecutionMark());
        BankRecall existing = bankRecallService.getById(id);

        if (permissionService.canEditAnyRecord(departmentId)) {
            validateBankRecall(bankRecall);
            requirePeriod(periodStart, periodEnd);
            copyAllFields(existing, bankRecall);
            existing.setPeriod(formatPeriod(periodStart, periodEnd));
        } else {
            applyPermittedFields(existing, bankRecall, departmentId);
        }

        bankRecallService.save(existing);
        return "redirect:/bankrecall";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, HttpSession session) {
        if (!permissionService.canDeleteRecords(departmentId(session))) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        bankRecallService.delete(id);
        return "redirect:/bankrecall";
    }

    private String formatPeriod(LocalDate periodStart, LocalDate periodEnd) {
        return "с " + periodStart.format(PERIOD_DATE_FORMATTER)
                + " по " + periodEnd.format(PERIOD_DATE_FORMATTER);
    }

    private void requirePeriod(LocalDate periodStart, LocalDate periodEnd) {
        if (periodStart == null || periodEnd == null) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Необходимо указать период");
        }
    }

    private String formatPeriodSearch(LocalDate periodStart, LocalDate periodEnd) {
        if (periodStart == null && periodEnd == null) {
            return null;
        }

        if (periodStart != null && periodEnd != null) {
            return formatPeriod(periodStart, periodEnd);
        }

        LocalDate selectedDate = periodStart != null ? periodStart : periodEnd;
        return selectedDate.format(PERIOD_DATE_FORMATTER);
    }

    private void addPermissions(Model model, Integer departmentId) {
        model.addAttribute("canDelete", permissionService.canDeleteRecords(departmentId));
        model.addAttribute("canEditRecord", permissionService.canEditAnyRecord(departmentId));
        model.addAttribute("canOpenEdit", permissionService.canEditAnyRecord(departmentId)
                || permissionService.canEditSomeFields(departmentId, JournalType.BANK));
        model.addAttribute("editableFields", permissionService.editableFields(departmentId, JournalType.BANK));
    }

    private void requireUpdatePermission(Integer departmentId) {
        if (!permissionService.canEditAnyRecord(departmentId)
                && !permissionService.canEditSomeFields(departmentId, JournalType.BANK)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private Integer departmentId(HttpSession session) {
        return (Integer) session.getAttribute(LoginController.DEPARTMENT_ID_SESSION_KEY);
    }

    private void applyPermittedFields(BankRecall existing, BankRecall incoming, Integer departmentId) {
        if (permissionService.canEditField(departmentId, JournalType.BANK, "ovidSpecialist")) {
            existing.setOvidSpecialist(incoming.getOvidSpecialist());
        }
        if (permissionService.canEditField(departmentId, JournalType.BANK, "ovidNote")) {
            existing.setOvidNote(incoming.getOvidNote());
        }
        if (permissionService.canEditField(departmentId, JournalType.BANK, "executionMark")) {
            existing.setExecutionMark(incoming.getExecutionMark());
        }
    }

    private void copyAllFields(BankRecall target, BankRecall source) {
        target.setPensionCaseNumber(source.getPensionCaseNumber());
        target.setPensionerName(source.getPensionerName());
        target.setBank(source.getBank());
        target.setRecallType(source.getRecallType());
        target.setPackageNumber(source.getPackageNumber());
        target.setMonth(source.getMonth());
        target.setYear(source.getYear());
        target.setReason(source.getReason());
        target.setDeathDate(source.getDeathDate());
        target.setUrgent(source.getUrgent());
        target.setPaymentType(source.getPaymentType());
        target.setRecallAmount(source.getRecallAmount());
        target.setOvpSpecialist(source.getOvpSpecialist());
        target.setDistrict(source.getDistrict());
        target.setNote(source.getNote());
        target.setExecutionMark(source.getExecutionMark());
        target.setOvidSpecialist(source.getOvidSpecialist());
        target.setOvidNote(source.getOvidNote());
        target.setAgreementDate(source.getAgreementDate());
    }

    private void validateBankRecall(BankRecall record) {
        validateRequiredChoice(record.getUrgent(), RecordValues.ALLOWED_URGENCY_VALUES, "срочность");
        validateRequiredChoice(record.getRecallType(), RecordValues.ALLOWED_BANK_RECALL_TYPES, "вид отзыва");
        validateRequiredChoice(record.getReason(), RecordValues.ALLOWED_BANK_AND_SED_REASONS, "причина");
        validateUsageMark(record.getExecutionMark());
        validateDistrict(record.getDistrict());
        validateMonthAndYear(record.getMonth(), record.getYear());
        if (!deliveryOrganizationService.existsByName(record.getBank())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Недопустимый банк");
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

    private void validateMonthAndYear(Integer month, Integer year) {
        if (month == null || month < 1 || month > 12 || year == null || year < 1900 || year > 2100) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Недопустимый месяц или год");
        }
    }
}
