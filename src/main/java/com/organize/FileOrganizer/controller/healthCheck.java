package com.organize.FileOrganizer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class healthCheck {
    @GetMapping("/")
    public String health() {
        return "DocFlow API is live! 🚀";
    }
}
