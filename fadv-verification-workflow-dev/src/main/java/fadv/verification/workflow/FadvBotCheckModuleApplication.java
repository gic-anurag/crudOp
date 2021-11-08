package fadv.verification.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class })
public class FadvBotCheckModuleApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(FadvBotCheckModuleApplication.class, args);
	}
}