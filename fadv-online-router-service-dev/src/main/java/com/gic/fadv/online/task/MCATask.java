package com.gic.fadv.online.task;

import java.time.Duration;
import java.time.LocalDateTime;
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

public class MCATask implements Callable<ObjectNode> {
	private final String payload;
	private final CountDownLatch latch;
	private OnlineApiService onlineApiService;
	private ParseAPIResponseService parseAPIResponseService;
	private static final Logger logger = LoggerFactory.getLogger(MCATask.class);
	public MCATask(String payload, CountDownLatch latch,
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
		String personalResponse = null, verifyId=null,fulFillmentSearchString=null,fulfillmentResponse=null;
		ObjectNode resultMCA = mapper.createObjectNode();
		ObjectNode resultWatchOut = mapper.createObjectNode();
		ObjectNode finalResultNode = mapper.createObjectNode();
		ObjectNode personalSearchNode= mapper.createObjectNode();
		
		/*----------------------MCA Call ----------------------------------------*/
		/*
		 * Call credit_reputational_mca API And Search For DIN
		 */
		//This will be used in future. For now it's commented
		//personalInfoSearch.setServices("credit_reputational_mca");
		personalInfoSearch.setServices("mca_dir_combo");
		personalSearchNode.put("name", personalInfoSearch.getName());
		personalSearchNode.put("dob", personalInfoSearch.getDob());
		personalSearchNode.put("services", personalInfoSearch.getServices());
		logger.info("Making Search String For mca_dir_combo");
		logger.info("Personal Search String" + personalSearchNode.toString()); 
		
		LocalDateTime startTime = LocalDateTime.now();			
		logger.info("Call from Task.First Search String.Personal(MCA) Search String" + personalSearchNode.toString());
		Boolean mcaExceptionFlag=false;
		Boolean mcaVerifyIDFlag=false;
		String mcaExceptionStr=null;
		String mcaVerifyIDStr=null;
		try {
			verifyId = onlineApiService.sendDataToServicePersonalRest(personalSearchNode.toString());
		} catch (Exception e) {
			logger.error("Error in Calling API of MCA" + e.getMessage());
			mcaExceptionFlag=true;
			mcaExceptionStr=e.getMessage();
		}
		logger.info("Call from Task.Parse String and Take out VerifyID"+verifyId);
		logger.info("Call from Task.Got VerifyID. Make Json and hit rest End points for final response");
		
		/*
		 * 2nd API Call
		 */
		if(StringUtils.isNotEmpty(verifyId)) {
			ObjectNode verifyNode= mapper.createObjectNode();
			verifyNode.put("verify_id", verifyId);
			logger.info("Second Seearch String.Personal Search String" + verifyNode.toString());
			
			try {
				personalResponse = onlineApiService.sendDataToFinalPersonalRest(verifyNode.toString());
				//personalResponse = onlineApiService.sendDataToPersonalVerifyRest(verifyNode.toString());
			} catch (Exception e) {
				logger.error("Error in Calling API of MCA" + e.getMessage());
				mcaVerifyIDFlag=true;
				mcaVerifyIDStr=e.getMessage();
			}
		}
		
		logger.info("Personal Rest Response MCA" + personalResponse);
		// Put Time out Code here
		//timeOut();
		/*
		 * Parsing Result For Personal Info Search (credit_reputational_mca) and look
		 * for din and Add MCA Result to Final Result
		 */
		  logger.info("Assign Input to MCA Input"); 
		  resultMCA.put("MCA Input",personalSearchNode.toString()); 
		  //Put Extra Field for keeping verifyID
		  resultMCA.put("verify_id", verifyId);
		  String din=null;
		  if(mcaExceptionFlag || mcaVerifyIDFlag) {
			  if(mcaExceptionFlag) {
				  resultMCA.put("MCA Output", mcaExceptionStr);
			  }else {
				  resultMCA.put("MCA Output", mcaVerifyIDStr);
			  }
			  resultMCA.put("status", "Exception");
			  LocalDateTime endTime = LocalDateTime.now();
			  Duration duration = Duration.between(startTime, endTime);
			  logger.info(duration.getSeconds() + " seconds");
			  resultMCA.put("Start Time",startTime.toString());
			  resultMCA.put("End Time",endTime.toString());
			  resultMCA.put("Time Taken",duration.getSeconds());
			  finalResultNode.set("MCA", resultMCA);
			  
		  }else {
			  din=parseAPIResponseService.parseMCAResponse(mapper, personalInfoSearch, personalResponse, resultMCA);
			  logger.info("Value of din from MCA Response"+din);
			  LocalDateTime endTime = LocalDateTime.now();
			  Duration duration = Duration.between(startTime, endTime);
			  logger.info(duration.getSeconds() + " seconds");
			  resultMCA.put("Start Time",startTime.toString());
			  resultMCA.put("End Time",endTime.toString());
			  resultMCA.put("Time Taken",duration.getSeconds());
			  finalResultNode.set("MCA", resultMCA);
			  logger.info("Value of Final Result After MCA"+finalResultNode);
		  }
		  /*----------------------MCA Call End----------------------------------------*/
		  
		/*------------------WatchOut Call----------------------------------------*/
		/*
		 * Call Watchout API And With DIN found in MCA Result
		 */
		logger.info("Making call for Fulfilment(Watch Out)"); 
		String [] names=personalInfoSearch.getName().split(" "); 
		if(names.length==3) {
		  if(din==null) { 
			  fulFillmentSearchString = "{\"firstname\":\"" + names[0] +
		  "\",\"lastname\":\"" + names[2] + "\",\"middlename\":\"" + names[1]+ "\"}";
		  }else { 
			  fulFillmentSearchString = "{\"firstname\":\"" + names[0] +
		  "\",\"lastname\":\"" + names[2]+ "\",\"middlename\":\"" + names[1] +
		  "\",\"din\":\"" + din+"\"}"; 
		  } 
		 }else if(names.length==2) { 
			 if(din==null) {
		  fulFillmentSearchString = "{\"firstname\":\"" + names[0] +
		  "\",\"lastname\":\"" + names[1]+ "\"}"; 
		  }else { 
			  fulFillmentSearchString =
		  "{\"firstname\":\"" + names[0] + "\",\"lastname\":\"" + names[1] +
		  "\",\"din\":\"" + din+"\"}"; 
			  } 
			 }else 
			 { if(din==null) {
				 fulFillmentSearchString = "{\"firstname\":\"" + personalInfoSearch.getName()+"\"}"; 
		  }else { 
			  fulFillmentSearchString = "{\"firstname\":\"" +personalInfoSearch.getName() +"\",\"din\":\"" + din+"\"}"; 
			  } 
			 }
		
			startTime = LocalDateTime.now();
		  logger.info("Fulfillment Search String" +fulFillmentSearchString); 
		  Boolean watchoutExceptionFlag=false;
		  String watchoutExceptionStr=null;
		  try {
			  fulfillmentResponse =onlineApiService.sendDataTofulfillmentRest(fulFillmentSearchString);
		  }catch(Exception e) {
			  logger.error("Error in Calling API of FulFillment (WatchOut API)"+e.getMessage());
			  watchoutExceptionFlag=true;
		  }
		  logger.info("Fulfillment Rest Response"+fulfillmentResponse);
		  logger.info("Some sleep after fulfillment API");
		 
		// Put Time out Code here
		//timeOut();
		resultWatchOut.put("WatchOut Input", fulFillmentSearchString);
		parseAPIResponseService.parseWatchoutResponse(mapper, personalInfoSearch, fulfillmentResponse, din, resultWatchOut);
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
		System.out.println("After Countdown");
		return finalResultNode;
	}
}
