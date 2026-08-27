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
            BankRecall bankRecall,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd) {

        return save(bankRecall, periodStart, periodEnd);
    }

    @PostMapping("/update/{id}")
    public String update(
            @PathVariable Integer id,
            BankRecall bankRecall,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd) {

        bankRecall.setId(id);
        return save(bankRecall, periodStart, periodEnd);
    }

    private String save(BankRecall bankRecall, LocalDate periodStart, LocalDate periodEnd) {
        bankRecall.setPeriod(formatPeriod(periodStart, periodEnd));
        bankRecallService.save(bankRecall);
        return "redirect:/bankrecall";
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
