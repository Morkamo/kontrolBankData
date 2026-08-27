package ru.morkamo.kontrolbankdata.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.morkamo.kontrolbankdata.model.SedRecall;
import ru.morkamo.kontrolbankdata.service.SedRecallService;

@Controller
@RequestMapping("/sedrecall")
@RequiredArgsConstructor
public class SedRecallController {

    private final SedRecallService sedRecallService;

    @GetMapping
    public String sedRecall(
            @RequestParam(required = false) String pensionCaseNumber,
            @RequestParam(required = false) String pensionerName,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Model model) {

        model.addAttribute("records", sedRecallService.search(pensionCaseNumber, pensionerName, month, year));
        model.addAttribute("pensionCaseNumber", pensionCaseNumber);
        model.addAttribute("pensionerName", pensionerName);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        return "SEDRecall";
    }

    @PostMapping("/create")
    public String create(SedRecall sedRecall) {
        sedRecallService.create(sedRecall);
        return "redirect:/sedrecall";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        sedRecallService.delete(id);
        return "redirect:/sedrecall";
    }
}
