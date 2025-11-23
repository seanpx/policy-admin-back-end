package com.pas.policy_admin_back_end;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.pas.policy_admin_back_end",
        "com.policyadmin"
})
public class PolicyAdminBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(PolicyAdminBackEndApplication.class, args);
	}

}
