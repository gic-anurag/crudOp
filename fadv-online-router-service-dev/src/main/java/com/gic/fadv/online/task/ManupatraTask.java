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

public class ManupatraTask implements Callable<ObjectNode> {
	private final String payload;
	private final CountDownLatch latch;
	private OnlineApiService onlineApiService;
	private ParseAPIResponseService parseAPIResponseService;
	private static final Logger logger = LoggerFactory.getLogger(ManupatraTask.class);
	public ManupatraTask(String payload, CountDownLatch latch,
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
		ObjectNode finalResultNode = mapper.createObjectNode();
		ObjectNode resultManupatra = mapper.createObjectNode();
		/*------------------Manupatra Call ----------------------------------------*/
		logger.info("Making call for Manupatra");
		//String[] names1 = personalInfoSearch.getName().split(" ");
		String manuPatraSearchString = "";
		if(personalInfoSearch.getName()!=null) {
			manuPatraSearchString="{\"formattedName\":\"" +personalInfoSearch.getName()+ "\"}";
		}
		/*
		 * if (names1.length == 3) { manuPatraSearchString = "{\"firstname\":\"" +
		 * names1[0] + "\",\"lastname\":\"" + names1[2] + "\",\"middlename\":\"" +
		 * names1[1] + "\",\"state\":\"" + personalInfoSearch.getState() +
		 * "\",\"startdate\":\"" + personalInfoSearch.getStartdate() + "\"}"; } else if
		 * (names1.length == 2) { manuPatraSearchString = "{\"firstname\":\"" +
		 * names1[0] + "\",\"lastname\":\"" + names1[1] + "\",\"middlename\":\"" +
		 * "\",\"state\":\"" + personalInfoSearch.getState() + "\",\"startdate\":\"" +
		 * personalInfoSearch.getStartdate() + "\"}"; } else { manuPatraSearchString =
		 * "{\"firstname\":\"" + personalInfoSearch.getName() + "\",\"lastname\":\"" +
		 * "\",\"middlename\":\"" + "\",\"state\":\"" + personalInfoSearch.getState() +
		 * "\",\"startdate\":\"" + personalInfoSearch.getStartdate() + "\"}"; }
		 */
		logger.info("Manupatra Search String" + manuPatraSearchString);
		String manuPatraResponse = "";
		try {
		manuPatraResponse = onlineApiService.sendDataToManupatraRest(manuPatraSearchString);
		} catch (Exception e) {
			logger.error("Error in Calling API of adverse_media" + e.getMessage());
		}
		logger.info("Manupatra Rest Response" + manuPatraResponse);
		logger.info("Some sleep after Manupatra API");
		// Put Time out Code here
		//timeOut();
		resultManupatra.put("ManuPatra Input", manuPatraSearchString);
		parseAPIResponseService.parseManupatraResponse(mapper, personalInfoSearch, manuPatraResponse, resultManupatra);
		/*
		 * Add Manupatra Result to Final Result
		 */
		LocalDateTime endTime = LocalDateTime.now();
		Duration duration = Duration.between(startTime, endTime);
		  logger.info(duration.getSeconds() + " seconds");
		  resultManupatra.put("Start Time",startTime.toString());
		  resultManupatra.put("End Time",endTime.toString());
		  resultManupatra.put("Time Taken",duration.getSeconds());
		finalResultNode.set("Manupatra", resultManupatra);
		logger.info("Value of Final Result after manupatra" + finalResultNode);
		/*------------------Manupatra Call Ends ----------------------------------------*/
		
		System.out.println(this.getClass().getSimpleName()+" is completed----------------------------");
		latch.countDown(); // reduce count of CountDownLatch by 1
		System.out.println("After Countdown");
		return finalResultNode;
	}
}
