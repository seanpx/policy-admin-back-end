package com.pas.policy_admin_back_end.service;

import com.pas.policy_admin_back_end.dto.GreetingResponse;
import org.springframework.stereotype.Service;

@Service
public class DefaultGreetingService implements GreetingService {

    @Override
    public GreetingResponse getGreeting() {
        return new GreetingResponse(true, "Policy Admin backend is running");
    }
}
