package com.gic.fadv.online.task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.online.model.PersonInfoSearch;
import com.gic.fadv.online.service.OnlineApiService;
import com.gic.fadv.online.service.ParseAPIResponseService;
import com.gic.fadv.online.utility.Utility;

public class WorldCheckTask implements Callable<ObjectNode> {
	private final String payload;
	private final CountDownLatch latch;
	private OnlineApiService onlineApiService;
	private ParseAPIResponseService parseAPIResponseService;
	private static final Logger logger = LoggerFactory.getLogger(WorldCheckTask.class);
	public WorldCheckTask(String payload, CountDownLatch latch,
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
		LocalDateTime startTime = LocalDateTime.now();		
		//Need to Set Din Also
		String din = personalInfoSearch.getDin()!=null?personalInfoSearch.getDin():"";
		ObjectNode finalResultNode = mapper.createObjectNode();
		ObjectNode resultWorldCheck = mapper.createObjectNode();
		/*------------------World Check Call ----------------------------------------*/
		/* World Check API */
		logger.info("Making Search World Check");
		//worldCheckSearchString ="{\"name\":" +"\""+ names[0]+" "+names[2]+"\""+ "}";
		//Change DOB Date from this format mm/dd/yyyy to yyyy-MM-dd  
		String formattedDob=Utility.formatDateUtil(personalInfoSearch.getDob());
		logger.info("Formatted DOB:{}",formattedDob);
		//String worldCheckSearchString = "{\"name\":" + "\"" + personalInfoSearch.getName() +"\",\"dob\":\"" + personalInfoSearch.getDob()
		//+ "\",\"father_name\":\"" + personalInfoSearch.getFather_name()+ "\"" + "}";
		String worldCheckSearchString = "{\"name\":" + "\"" + personalInfoSearch.getName() +"\",\"dob\":\"" + formattedDob
		+ "\",\"father_name\":\"" + personalInfoSearch.getFather_name()+ "\"" + "}";
		startTime=LocalDateTime.now();
		logger.info("World Check Search String" + worldCheckSearchString);
		String worldCheckResponse = "";
		try {
			worldCheckResponse = onlineApiService.sendDataToWorldCheckRest(worldCheckSearchString);
		} catch (Exception e) {
			logger.error("Error in Calling API of World Check API" + e.getMessage());
		}
		 
		// Put Time out Code here 
		logger.info("Some sleep after worldCheck API");
		//timeOut();
		logger.info("World Check Rest Response" + worldCheckResponse);
		resultWorldCheck.put("World Check Input", worldCheckSearchString);
		parseAPIResponseService.parseWorldCheckResponse(mapper, personalInfoSearch, worldCheckResponse, din, resultWorldCheck);
		/*
		 * Add World Check Result to Final Result
		 */
		LocalDateTime endTime = LocalDateTime.now();
		 Duration duration = Duration.between(startTime, endTime);
		  logger.info(duration.getSeconds() + " seconds");
		  resultWorldCheck.put("Start Time",startTime.toString());
		  resultWorldCheck.put("End Time",endTime.toString());
		  resultWorldCheck.put("Time Taken",duration.getSeconds());
		finalResultNode.set("WorldCheck", resultWorldCheck);
		logger.info("Value of Final Result After World Check" + finalResultNode);
		/*------------------World Check Call End ----------------------------------------*/
		
		System.out.println(this.getClass().getSimpleName()+" is completed----------------------------");
		latch.countDown(); // reduce count of CountDownLatch by 1
		System.out.println("After Countdown");
		return finalResultNode;
	}
	
}
