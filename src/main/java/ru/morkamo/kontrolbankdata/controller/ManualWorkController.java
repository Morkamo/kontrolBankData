package ru.morkamo.kontrolbankdata.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.morkamo.kontrolbankdata.model.ManualWork;
import ru.morkamo.kontrolbankdata.service.ManualWorkService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/manualwork")
@RequiredArgsConstructor
public class ManualWorkController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final ManualWorkService manualWorkService;

    @GetMapping
    public String manualWork(
            @RequestParam(required = false) String pensionCaseNumber,
            @RequestParam(required = false) String pensionerName,
            @RequestParam(required = false) String period,
            Model model) {

        model.addAttribute("records", manualWorkService.search(pensionCaseNumber, pensionerName, period));
        model.addAttribute("pensionCaseNumber", pensionCaseNumber);
        model.addAttribute("pensionerName", pensionerName);
        model.addAttribute("selectedPeriod", period);
        return "ManualWork";
    }

    @PostMapping("/create")
    public String create(
            ManualWork manualWork,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd) {

        manualWork.setPeriod(formatPeriod(periodStart, periodEnd));
        manualWorkService.create(manualWork);
        return "redirect:/manualwork";
    }

    @PostMapping("/update/{id}")
    public String update(
            @PathVariable Integer id,
            ManualWork manualWork,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd) {

        manualWork.setId(id);
        manualWork.setPeriod(formatPeriod(periodStart, periodEnd));
        manualWorkService.create(manualWork);
        return "redirect:/manualwork";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        manualWorkService.delete(id);
        return "redirect:/manualwork";
    }

    private String formatPeriod(LocalDate periodStart, LocalDate periodEnd) {
        return "с " + periodStart.format(DATE_FORMATTER) + " по " + periodEnd.format(DATE_FORMATTER);
    }
}
