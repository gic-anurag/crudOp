package com.gic.fadv.vendor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.gic.fadv.vendor.output.scheduler.VendorOutputScheduler;

@SpringBootApplication
@EnableScheduling
public class FadvVendorServiceApplication implements CommandLineRunner {

	@Autowired
	private VendorOutputScheduler vendorOutputScheduler;

	public static void main(String[] args) {
		SpringApplication.run(FadvVendorServiceApplication.class, args);
	}

	public void run(String... arg0) throws Exception {
		vendorOutputScheduler.runParallel();
	}
}
