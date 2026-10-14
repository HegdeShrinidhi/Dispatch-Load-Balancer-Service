package com.dispatch.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden // Hides this utility controller from the Swagger interface itself
@Controller
public class WebRedirectController {

    @GetMapping("/")
    public String redirectToSwagger() {
        // Automatically redirects the base root URL straight to the interactive UI context path
        return "redirect:/swagger-ui/index.html";
    }
}
