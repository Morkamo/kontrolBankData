package ru.morkamo.kontrolbankdata.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import ru.morkamo.kontrolbankdata.model.BankRecall;
import ru.morkamo.kontrolbankdata.constants.RecordValues;
import ru.morkamo.kontrolbankdata.service.BankRecallService;
import ru.morkamo.kontrolbankdata.service.DeliveryOrganizationService;
import ru.morkamo.kontrolbankdata.security.JournalType;
import ru.morkamo.kontrolbankdata.security.PermissionService;
import ru.morkamo.kontrolbankdata.security.AppUserPrincipal;
import ru.morkamo.kontrolbankdata.state.ApplicationState;

@Controller
@RequestMapping("/bankrecall")
@RequiredArgsConstructor
public class BankRecallController {

    private static final DateTimeFormatter PERIOD_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final BankRecallService bankRecallService;
    private final DeliveryOrganizationService deliveryOrganizationService;
    private final PermissionService permissionService;
    private final ApplicationState applicationState;

    @GetMapping
    public String bankRecall(
            @RequestParam(required = false) String pensionCaseNumber,
            @RequestParam(required = false) String pensionerName,
            @RequestParam(required = false) String bank,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            @AuthenticationPrincipal AppUserPrincipal user,
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
        addPermissions(model, user.getDepartmentId());
        addRecordForm(model, new BankRecall(), null, null, null, false, false, "/bankrecall/create");
        return "BankRecall";
    }

    @PostMapping("/create")
    public String create(
            BankRecall bankRecall,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            @AuthenticationPrincipal AppUserPrincipal user,
            Model model) {

        try {
            validateOptionalId(bankRecall.getId());
            validateBankRecall(bankRecall);
            requirePeriod(periodStart, periodEnd);
            if (bankRecallService.existsById(bankRecall.getId())) {
                throw new FormValidationException("Такой ID уже занят, выберите другой.");
            }
            BankRecall newRecord = new BankRecall();
            newRecord.setId(bankRecall.getId());
            copyAllFields(newRecord, bankRecall);
            newRecord.setPeriod(formatPeriod(periodStart, periodEnd));
            bankRecallService.save(newRecord);
        } catch (FormValidationException exception) {
            return formError(bankRecall, periodStart, periodEnd, user, model, exception.getMessage(),
                    false, "/bankrecall/create");
        } catch (DataIntegrityViolationException exception) {
            return formError(bankRecall, periodStart, periodEnd, user, model,
                    "Не удалось сохранить запись: введенные данные нарушают ограничения базы данных.",
                    false, "/bankrecall/create");
        }
        return "redirect:/bankrecall";
    }

    @PostMapping("/update/{id}")
    public String update(
            @PathVariable Integer id,
            BankRecall bankRecall,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            @AuthenticationPrincipal AppUserPrincipal user,
            Model model) {

        Integer departmentId = user.getDepartmentId();
        requireUpdatePermission(departmentId);
        BankRecall existing = bankRecallService.getById(id);
        try {
            validateUsageMark(bankRecall.getExecutionMark());
            if (permissionService.canEditAnyRecord(departmentId)) {
                validateBankRecall(bankRecall);
                requirePeriod(periodStart, periodEnd);
                copyAllFields(existing, bankRecall);
                existing.setPeriod(formatPeriod(periodStart, periodEnd));
            } else {
                applyPermittedFields(existing, bankRecall, departmentId);
            }
            bankRecallService.save(existing);
        } catch (FormValidationException exception) {
            return formError(bankRecall, periodStart, periodEnd, user, model, exception.getMessage(),
                    true, "/bankrecall/update/" + id);
        } catch (DataIntegrityViolationException exception) {
            return formError(bankRecall, periodStart, periodEnd, user, model,
                    "Не удалось сохранить запись: введенные данные нарушают ограничения базы данных.",
                    true, "/bankrecall/update/" + id);
        }
        return "redirect:/bankrecall";
    }

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Integer id,
            @AuthenticationPrincipal AppUserPrincipal user) {
        if (!permissionService.canDeleteRecords(user.getDepartmentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
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
            throw new FormValidationException("Необходимо указать период");
        }
        if (periodEnd.isBefore(periodStart)) {
            throw new FormValidationException("Дата ПО не может быть раньше даты С");
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
        model.addAttribute("canOpenEdit", permissionService.canOpenEdit(departmentId, JournalType.BANK));
        model.addAttribute("editableFields", permissionService.editableFields(departmentId, JournalType.BANK));
        model.addAttribute("isAdministrator", permissionService.isAdministrator(departmentId));
        model.addAttribute("databaseLocked", applicationState.isDatabaseLocked());
    }

    private void addRecordForm(
            Model model,
            BankRecall record,
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
            BankRecall record,
            LocalDate periodStart,
            LocalDate periodEnd,
            AppUserPrincipal user,
            Model model,
            String error,
            boolean editMode,
            String action) {
        model.addAttribute("records", bankRecallService.search(null, null, null, null, null, null));
        model.addAttribute("banks", deliveryOrganizationService.getAll());
        model.addAttribute("selectedPeriodStart", null);
        model.addAttribute("selectedPeriodEnd", null);
        addPermissions(model, user.getDepartmentId());
        addRecordForm(model, record, periodStart, periodEnd, error, true, editMode, action);
        return "BankRecall";
    }

    private void requireUpdatePermission(Integer departmentId) {
        if (!permissionService.canOpenEdit(departmentId, JournalType.BANK)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
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
            throw new FormValidationException("Недопустимый банк");
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

    private void validateMonthAndYear(Integer month, Integer year) {
        if (month == null || month < 1 || month > 12 || year == null || year < 1900 || year > 2100) {
            throw new FormValidationException("Недопустимый месяц или год");
        }
    }

    private void validateOptionalId(Integer id) {
        if (id != null && id <= 0) {
            throw new FormValidationException("ID должен быть положительным числом");
        }
    }
}
