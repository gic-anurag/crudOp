package com.gic.fadv.online.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.fadv.online.controller.OnlineRouterController;
import com.gic.fadv.online.model.OnlineVerificationChecks;
import com.gic.fadv.online.service.OnlineApiService;
import com.gic.fadv.online.service.OnlineService;

@Component
public class OnlineApiScheduler {
	
	@Autowired
	private OnlineApiService onlineApiService;

	@Autowired
	private OnlineService onlineService;
	
	@Value("${getretryverifychecks.rest.url}")
	private String getRetryChecksUrl;
	@Value("${verification.url.checkid.l3}")
	private String verificationStatusUrlL3;
	
	private static final Logger logger = LoggerFactory.getLogger(OnlineRouterController.class);
	
	@Scheduled(cron = "${scheduled.api.retries.cron}")
	public void runParallelApiRetries() throws JsonProcessingException, InterruptedException, ExecutionException {
		
		// Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		String response = onlineApiService.getDataFromLocalRest(getRetryChecksUrl);
		List<OnlineVerificationChecks> onlineVerificationChecksList = new ArrayList<>();
		if (response != null && !StringUtils.isEmpty(response)) {
			onlineVerificationChecksList = mapper.readValue(response, new TypeReference<List<OnlineVerificationChecks>>() { }); 
		}
		try {
			onlineService.runParallelService(onlineVerificationChecksList);
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}
	
}
