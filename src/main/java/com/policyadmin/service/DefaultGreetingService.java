package com.policyadmin.service;

import com.policyadmin.dto.GreetingResponse;
import org.springframework.stereotype.Service;

@Service
public class DefaultGreetingService implements GreetingService {

    @Override
    public GreetingResponse getGreeting() {
        return new GreetingResponse(true, "Policy Admin backend is running");
    }
}
