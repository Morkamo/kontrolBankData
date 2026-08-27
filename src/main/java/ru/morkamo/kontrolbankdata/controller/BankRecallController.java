package ru.morkamo.kontrolbankdata.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.morkamo.kontrolbankdata.model.BankRecall;
import ru.morkamo.kontrolbankdata.service.BankRecallService;
import ru.morkamo.kontrolbankdata.service.DeliveryOrganizationService;

@Controller
@RequestMapping("/bankrecall")
@RequiredArgsConstructor
public class BankRecallController {

    private static final DateTimeFormatter PERIOD_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final BankRecallService bankRecallService;
    private final DeliveryOrganizationService deliveryOrganizationService;

    @GetMapping
    public String bankRecall(
            @RequestParam(required = false) String pensionCaseNumber,
            @RequestParam(required = false) String pensionerName,
            @RequestParam(required = false) String bank,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
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
        return "BankRecall";
    }

    @PostMapping("/create")
    public String create(
            @RequestParam String pensionCaseNumber,
            @RequestParam String pensionerName,
            @RequestParam String bank,
            @RequestParam Integer district,
            @RequestParam String urgent,
            @RequestParam String recallType,
            @RequestParam String reason,
            @RequestParam(required = false) String deathDate,
            @RequestParam String packageNumber,
            @RequestParam Integer month,
            @RequestParam Integer year,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            @RequestParam(required = false) String paymentType,
            @RequestParam(required = false) String recallAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate agreementDate,
            @RequestParam(required = false) String ovpSpecialist,
            @RequestParam(required = false) String executionMark,
            @RequestParam(required = false) String ovidSpecialist,
            @RequestParam(required = false) String ovidNote,
            @RequestParam(required = false) String note) {

        BankRecall bankRecall = new BankRecall();
        fillBankRecall(bankRecall, pensionCaseNumber, pensionerName, bank, district, urgent, recallType, reason,
                deathDate, packageNumber, month, year, periodStart, periodEnd, paymentType, recallAmount,
                agreementDate, ovpSpecialist, executionMark, ovidSpecialist, ovidNote, note);

        bankRecallService.create(bankRecall);
        return "redirect:/bankrecall";
    }

    @PostMapping("/update/{id}")
    public String update(
            @PathVariable Integer id,
            @RequestParam String pensionCaseNumber,
            @RequestParam String pensionerName,
            @RequestParam String bank,
            @RequestParam Integer district,
            @RequestParam String urgent,
            @RequestParam String recallType,
            @RequestParam String reason,
            @RequestParam(required = false) String deathDate,
            @RequestParam String packageNumber,
            @RequestParam Integer month,
            @RequestParam Integer year,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            @RequestParam(required = false) String paymentType,
            @RequestParam(required = false) String recallAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate agreementDate,
            @RequestParam(required = false) String ovpSpecialist,
            @RequestParam(required = false) String executionMark,
            @RequestParam(required = false) String ovidSpecialist,
            @RequestParam(required = false) String ovidNote,
            @RequestParam(required = false) String note) {

        BankRecall bankRecall = new BankRecall();
        bankRecall.setId(id);
        fillBankRecall(bankRecall, pensionCaseNumber, pensionerName, bank, district, urgent, recallType, reason,
                deathDate, packageNumber, month, year, periodStart, periodEnd, paymentType, recallAmount,
                agreementDate, ovpSpecialist, executionMark, ovidSpecialist, ovidNote, note);

        bankRecallService.create(bankRecall);
        return "redirect:/bankrecall";
    }

    private void fillBankRecall(
            BankRecall bankRecall,
            String pensionCaseNumber,
            String pensionerName,
            String bank,
            Integer district,
            String urgent,
            String recallType,
            String reason,
            String deathDate,
            String packageNumber,
            Integer month,
            Integer year,
            LocalDate periodStart,
            LocalDate periodEnd,
            String paymentType,
            String recallAmount,
            LocalDate agreementDate,
            String ovpSpecialist,
            String executionMark,
            String ovidSpecialist,
            String ovidNote,
            String note) {

        bankRecall.setPensionCaseNumber(pensionCaseNumber);
        bankRecall.setPensionerName(pensionerName);
        bankRecall.setBank(bank);
        bankRecall.setDistrict(district);
        bankRecall.setUrgent(urgent);
        bankRecall.setRecallType(recallType);
        bankRecall.setReason(reason);
        bankRecall.setDeathDate(deathDate);
        bankRecall.setPackageNumber(packageNumber);
        bankRecall.setMonth(month);
        bankRecall.setYear(year);
        bankRecall.setPeriod(formatPeriod(periodStart, periodEnd));
        bankRecall.setPaymentType(paymentType);
        bankRecall.setRecallAmount(recallAmount);
        bankRecall.setAgreementDate(agreementDate);
        bankRecall.setOvpSpecialist(ovpSpecialist);
        bankRecall.setExecutionMark(executionMark);
        bankRecall.setOvidSpecialist(ovidSpecialist);
        bankRecall.setOvidNote(ovidNote);
        bankRecall.setNote(note);
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        bankRecallService.delete(id);
        return "redirect:/bankrecall";
    }

    private String formatPeriod(LocalDate periodStart, LocalDate periodEnd) {
        return "с " + periodStart.format(PERIOD_DATE_FORMATTER)
                + " по " + periodEnd.format(PERIOD_DATE_FORMATTER);
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
}
