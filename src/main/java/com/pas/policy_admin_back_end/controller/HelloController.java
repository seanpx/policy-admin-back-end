package com.pas.policy_admin_back_end.controller;

import com.pas.policy_admin_back_end.dto.GreetingResponse;
import com.pas.policy_admin_back_end.service.GreetingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hello")
public class HelloController {

    private final GreetingService greetingService;

    public HelloController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping
    public GreetingResponse ping() {
        return greetingService.getGreeting();
    }
}
