package com.gic.fadv.verification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class })
@EnableScheduling
public class FadvVerifcationModuleApplication {

	public static void main(String[] args) {
		SpringApplication.run(FadvVerifcationModuleApplication.class, args);
	}

}
