package com.gic.fadv.online.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.online.model.PersonInfoSearch;
import com.gic.fadv.online.service.OnlineApiService;
import com.gic.fadv.online.service.ParseAPIResponseService;

public class AdverseMediaTask implements Callable<ObjectNode> {
	private final String payload;
	private final CountDownLatch latch;
	private OnlineApiService onlineApiService;
	private ParseAPIResponseService parseAPIResponseService;
	private static final Logger logger = LoggerFactory.getLogger(AdverseMediaTask.class);
	public AdverseMediaTask(String payload, CountDownLatch latch,
			OnlineApiService onlineApiService,ParseAPIResponseService parseAPIResponseService) {
		this.payload = payload;
		this.latch = latch;
		this.onlineApiService=onlineApiService;
		this.parseAPIResponseService= parseAPIResponseService;
	}

	@Override
	public ObjectNode call()throws Exception{
		//business logic
		ObjectMapper mapper = new ObjectMapper();
		// Converting the JSONString to Object
		PersonInfoSearch personalInfoSearch = mapper.readValue(payload, PersonInfoSearch.class);
		String personalResponse = null, verifyId=null;
		ObjectNode finalResultNode = mapper.createObjectNode();
		ObjectNode personalSearchNode= mapper.createObjectNode();
		ObjectNode resultAdverseMedia = mapper.createObjectNode();
		/*------------------Adverse Media Call ----------------------------------------*/
		/*
		 * Call adverse_media API
		 */
		personalInfoSearch.setServices("adverse_media");
		personalSearchNode.put("name", personalInfoSearch.getName());
		personalSearchNode.put("dob", personalInfoSearch.getDob());
		personalSearchNode.put("contexts", personalInfoSearch.getContexts());
		personalSearchNode.put("services", personalInfoSearch.getServices());
		logger.info("Making Search String For adverse_media");
		LocalDateTime startTime = LocalDateTime.now();			
		logger.info("Call from Task.First Search String.Personal(Adverse media) Search String" + personalSearchNode.toString());
		try {
			verifyId = onlineApiService.sendDataToServicePersonalRest(personalSearchNode.toString());
		} catch (Exception e) {
			logger.error("Error in Calling API of adverse Media" + e.getMessage());
		}
		logger.info("Call from Task.Parse String and Take out VerifyID"+verifyId);
		logger.info("Call from Task.Got VerifyID. Make Json and hit rest End points for final response");
		
		/*
		 * 2nd API Call
		 */
		ObjectNode verifyNode= mapper.createObjectNode();
		verifyNode.put("verify_id", verifyId);
		logger.info("Second Seearch String.Personal Search String" + verifyNode.toString());
		try {
			personalResponse = onlineApiService.sendDataToFinalPersonalRest(verifyNode.toString());
			//personalResponse = onlineApiService.sendDataToPersonalVerifyRest(verifyNode.toString());
		} catch (Exception e) {
			logger.error("Error in Calling API of Adverse Media" + e.getMessage());
		}
		logger.info("Personal Rest Response Adverse Media" + personalResponse);
		
		// Put Time out Code here
		//timeOut();
		/*
		 * Parsing Result For Personal Info Search (adverse_media) and Add Adverse Media Result to Final Result
		 */
		logger.info("Assign Input to Adverse media");
		resultAdverseMedia.put("Adverse media Input", personalSearchNode.toString());
		//Put Extra Field for keeping verifyID
		resultAdverseMedia.put("verify_id", verifyId);
		parseAPIResponseService.parseAdverseMediaResponse(mapper, personalInfoSearch, personalResponse, resultAdverseMedia);
		LocalDateTime endTime = LocalDateTime.now();
		Duration duration = Duration.between(startTime, endTime);
		  logger.info(duration.getSeconds() + " seconds");
		  resultAdverseMedia.put("Start Time",startTime.toString());
		  resultAdverseMedia.put("End Time",endTime.toString());
		  resultAdverseMedia.put("Time Taken",duration.getSeconds());
		finalResultNode.set("Adverse Media", resultAdverseMedia);
		logger.info("Value of Final Result after adverse media" + finalResultNode);
		/*------------------Adverse Media Call End----------------------------------------*/
		
		System.out.println(this.getClass().getSimpleName()+" is completed----------------------------");
		latch.countDown(); // reduce count of CountDownLatch by 1
		System.out.println("After Countdown");
		return finalResultNode;
	}
}
