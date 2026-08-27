package ru.morkamo.kontrolbankdata.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BaseRouteController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
