package com.library.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    
    @GetMapping("/status")
    public String status() {
        return "Library Management System is running!";
    }
    
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}