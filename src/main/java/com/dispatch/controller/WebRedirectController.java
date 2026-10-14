package com.dispatch.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@RestController // Returns pure JSON data for backend checking
public class WebRedirectController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getApiStatus() {
        // Returns a clean backend status payload while pointing to the Web UI location
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Dispatch Load Balancer Service",
                "message", "Welcome! The API is running successfully.",
                "interactive_documentation", "/swagger-ui/index.html"
        ));
    }
}
