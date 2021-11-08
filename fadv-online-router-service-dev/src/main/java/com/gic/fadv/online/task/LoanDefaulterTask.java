package com.gic.fadv.online.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.online.controller.OnlineAPILoanController;
import com.gic.fadv.online.model.PersonInfoSearch;
import com.gic.fadv.online.service.OnlineApiService;
import com.gic.fadv.online.service.ParseAPIResponseService;

public class LoanDefaulterTask implements Callable<ObjectNode> {
	private final String payload;
	private final CountDownLatch latch;
	private OnlineApiService onlineApiService;
	private ParseAPIResponseService parseAPIResponseService;
	private static final Logger logger = LoggerFactory.getLogger(LoanDefaulterTask.class);
	public LoanDefaulterTask(String payload, CountDownLatch latch,
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
		ObjectNode resultLoan = mapper.createObjectNode();
		ObjectNode finalResultNode = mapper.createObjectNode();
		ObjectNode personalSearchNode= mapper.createObjectNode();
		/*------------------credit_reputational_cibil (Loan Call) ----------------------------------------*/
		/*
		 * Call credit_reputational_cibil API
		 */
		personalInfoSearch.setServices("credit_reputational_cibil");
		personalSearchNode.put("name", personalInfoSearch.getName());
		//personalSearchNode.put("address", personalInfoSearch.getAddress());
		personalSearchNode.put("services", personalInfoSearch.getServices());
		
		LocalDateTime startTime = LocalDateTime.now();
		logger.info("Call from Task.First Search String.Personal Search String" + personalSearchNode.toString());
		try {
			verifyId = onlineApiService.sendDataToServicePersonalRest(personalSearchNode.toString());
		} catch (Exception e) {
			logger.error("Error in Calling API of credit_reputational_cibil" + e.getMessage());
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
			logger.error("Error in Calling API of credit_reputational_cibil" + e.getMessage());
		}
		logger.info("Personal Rest Response credit_reputational_cibil" + personalResponse);
		
		// Put Time out Code here
		//timeOut();
		/*
		 * Parsing Result For Loan Defaulter (credit_reputational_cibil) and Add  Result to Final Result
		 */
		logger.info("Assign Input to Loan Defaulter Input");
		resultLoan.put("Loan Defaulter Input", personalSearchNode.toString());
		//Put Extra Field for keeping verifyID
		resultLoan.put("verify_id", verifyId);
		ObjectNode parseResult= mapper.createObjectNode();
		parseResult=parseAPIResponseService.parseLoanDefaulterResponse(mapper, personalInfoSearch, personalResponse, resultLoan);
		System.out.println("11111111"+parseResult);
		LocalDateTime endTime = LocalDateTime.now();
		 Duration duration = Duration.between(startTime, endTime);
		  logger.info(duration.getSeconds() + " seconds");
		  resultLoan.put("Start Time",startTime.toString());
		  resultLoan.put("End Time",endTime.toString());
		  resultLoan.put("Time Taken",duration.getSeconds());
		finalResultNode.set("Loan Defaulter", resultLoan);
		logger.info("Value of Final Result" + finalResultNode);
		/*------------------credit_reputational_cibil (Loan Call End) ----------------------------------------*/	
		
		System.out.println(this.getClass().getSimpleName()+" is completed----------------------------");
		latch.countDown(); // reduce count of CountDownLatch by 1
		System.out.println("After Countdown");
		return finalResultNode;
	}
}
