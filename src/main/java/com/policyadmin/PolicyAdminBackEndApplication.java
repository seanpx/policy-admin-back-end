package com.policyadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.policyadmin")
public class PolicyAdminBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(PolicyAdminBackEndApplication.class, args);
	}

}
