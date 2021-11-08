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

public class WatchOutTask implements Callable<ObjectNode> {
	private final String payload;
	private final CountDownLatch latch;
	private OnlineApiService onlineApiService;
	private ParseAPIResponseService parseAPIResponseService;
	private static final Logger logger = LoggerFactory.getLogger(ManupatraTask.class);
	public WatchOutTask(String payload, CountDownLatch latch,OnlineApiService onlineApiService,ParseAPIResponseService parseAPIResponseService) {
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
		String fulFillmentSearchString=null,fulfillmentResponse=null;
		ObjectNode resultWatchOut = mapper.createObjectNode();
		ObjectNode finalResultNode = mapper.createObjectNode();
		
		/*------------------WatchOut Call----------------------------------------*/
		/*
		 * Call Watchout API And With DIN found in MCA Result
		 */
		logger.info("Making call for Fulfilment(Watch Out)"); 
		String [] names=personalInfoSearch.getName().split(" "); 
		if(names.length==3) {
		  if(personalInfoSearch.getDin()==null) { 
			  fulFillmentSearchString = "{\"firstname\":\"" + names[0] +
		  "\",\"lastname\":\"" + names[2] + "\",\"middlename\":\"" + names[1]+ "\"}";
		  }else { 
			  fulFillmentSearchString = "{\"firstname\":\"" + names[0] +
		  "\",\"lastname\":\"" + names[2]+ "\",\"middlename\":\"" + names[1] +
		  "\",\"din\":\"" + personalInfoSearch.getDin()+"\"}"; 
		  } 
		 }else if(names.length==2) { 
			 if(personalInfoSearch.getDin()==null) {
		  fulFillmentSearchString = "{\"firstname\":\"" + names[0] +
		  "\",\"lastname\":\"" + names[1]+ "\"}"; 
		  }else { 
			  fulFillmentSearchString =
		  "{\"firstname\":\"" + names[0] + "\",\"lastname\":\"" + names[1] +
		  "\",\"din\":\"" + personalInfoSearch.getDin()+"\"}"; 
			  } 
			 }else 
			 { if(personalInfoSearch.getDin()==null) {
				 fulFillmentSearchString = "{\"firstname\":\"" + personalInfoSearch.getName()+"\"}"; 
		  }else { 
			  fulFillmentSearchString = "{\"firstname\":\"" +personalInfoSearch.getName() +"\",\"din\":\"" + personalInfoSearch.getDin()+"\"}"; 
			  } 
			 }
		
			startTime = LocalDateTime.now();
		  logger.info("Fulfillment Search String" +fulFillmentSearchString); 
		  try {
			  fulfillmentResponse =onlineApiService.sendDataTofulfillmentRest(fulFillmentSearchString);
		  }catch(Exception e) {
			  logger.error("Error in Calling API of FulFillment (WatchOut API)"+e.getMessage()); 
		  }
		  logger.info("Fulfillment Rest Response"+fulfillmentResponse);
		  logger.info("Some sleep after fulfillment API");
		 
		// Put Time out Code here
		//timeOut();
		resultWatchOut.put("WatchOut Input", fulFillmentSearchString);
		parseAPIResponseService.parseWatchoutResponse(mapper, personalInfoSearch, fulfillmentResponse, personalInfoSearch.getDin(), resultWatchOut);
		LocalDateTime endTime = LocalDateTime.now();
		 Duration duration = Duration.between(startTime, endTime);
		  logger.info(duration.getSeconds() + " seconds");
		  resultWatchOut.put("Start Time",startTime.toString());
		  resultWatchOut.put("End Time",endTime.toString());
		  resultWatchOut.put("Time Taken",duration.getSeconds());
		finalResultNode.set("WatchOut", resultWatchOut);
		logger.info("Value of Final Result After WatchOut"+finalResultNode);
		/*------------------WatchOut Call End ----------------------------------------*/
		
		
		System.out.println(this.getClass().getSimpleName()+" is completed----------------------------");
		latch.countDown(); // reduce count of CountDownLatch by 1
		return finalResultNode;
	}
}
