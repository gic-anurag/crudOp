package com.gic.fadv.vendor.input.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gic.fadv.vendor.input.service.VendorInputScheduledService;

@Component
public class VendorInputScheduler {
	private static final Logger logger = LoggerFactory.getLogger(VendorInputScheduler.class);

	@Autowired
	private VendorInputScheduledService vendorInputScheduledService;

	@Scheduled(cron = "${scheduling.job.input.cron}")
	private void vendorInputHourlyScheduler() {
		logger.info(
				"============================Scheduler Started -> Vendor Input=====================================");
		vendorInputScheduledService.getVendorRequests();
		logger.info(
				"=============================Scheduler Ended -> Vendor Input======================================");
	}
}
