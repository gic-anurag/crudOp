package com.gic.fadv.online.controller;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.online.model.PersonInfoSearch;
import com.gic.fadv.online.service.OnlineApiService;
import com.gic.fadv.online.service.ParseAPIResponseService;
import com.gic.fadv.online.utility.Utility;
import com.google.gson.JsonParser;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/india")
@Api(value = "Cross Reference", description = "Operations pertaining to Online API Controller (Cross Reference master data)")
public class OnlineAPIWatchOutController {

	@Autowired
	private Environment env;
	@Autowired
	private OnlineApiService onlineApiService;
	@Autowired
	private ParseAPIResponseService parseAPIResponseService;
	

	private static final Logger logger = LoggerFactory.getLogger(OnlineAPIWatchOutController.class);

	@ApiOperation(value = "This service is used to process Records at Online API and return the result ", response = List.class)
	@PostMapping(path = "/async/onlineapi/watchout", consumes = "application/json", produces = "application/json")
	public DeferredResult<ResponseEntity<String>> doAsyncProcess(@RequestBody String inStr) {
		DeferredResult<ResponseEntity<String>> ret = new DeferredResult<>();
		ForkJoinPool.commonPool().submit(() -> {
			logger.info("Got async Request:\n" + inStr);
			processRequest(inStr, true);
			ret.setResult(ResponseEntity.ok("ok"));
		});
		ret.onCompletion(() -> logger.info("async process request done"));
		return ret;
	}

	@ApiOperation(value = "This service is used to process Records at Online API and return the result ", response = List.class)
	@PostMapping(path = "/onlineapi/watchout", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> doProcess(@RequestBody String inStr) {
		try {
			logger.info("Got Request:\n" + inStr);
			String response = processRequest(inStr, false);
			logger.info("Processed Request");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}

	private String processRequest(String inStr, boolean asyncStatus) {
		try {
			// Creating the ObjectMapper object
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			// Converting the JSONString to Object
			PersonInfoSearch personalInfoSearch = mapper.readValue(inStr, PersonInfoSearch.class);
			
			String fulfillmentResponse = null;
			String din = personalInfoSearch.getDin();
			
			ObjectNode resultWatchOut = mapper.createObjectNode();
			ObjectNode finalResultNode = mapper.createObjectNode();
			ObjectNode fulFillmentSearchNode =mapper.createObjectNode();
			/*------------------WatchOut Call----------------------------------------*/
			/*
			 * Call Watchout API And With DIN found in MCA Result
			 */
			logger.info("Making call for Fulfilment(Watch Out)"); 
			String [] names=personalInfoSearch.getName().split(" "); 
			if(names.length==3) {
			  if(din==null) { 
				  fulFillmentSearchNode.put("firstname", names[0]);
				  fulFillmentSearchNode.put("lastname", names[2]);
				  fulFillmentSearchNode.put("middlename", names[1]);
			  }else { 
				  fulFillmentSearchNode.put("firstname", names[0]);
				  fulFillmentSearchNode.put("lastname", names[2]);
				  fulFillmentSearchNode.put("middlename", names[1]);
				  fulFillmentSearchNode.put("din", din); 
			  } 
			 }else if(names.length==2) { 
				 if(din==null) {
					fulFillmentSearchNode.put("firstname", names[0]);
					fulFillmentSearchNode.put("lastname", names[1]);
			  }else { 
				  fulFillmentSearchNode.put("firstname", names[0]);
				  fulFillmentSearchNode.put("lastname", names[1]);
				  fulFillmentSearchNode.put("din", din); 
				  } 
			}else{ 
				if(din==null) {
					 fulFillmentSearchNode.put("firstname", personalInfoSearch.getName()); 
			  }else { 
				  fulFillmentSearchNode.put("firstname", personalInfoSearch.getName());
				  fulFillmentSearchNode.put("din", din);  
				 } 
			}
			
			LocalDateTime startTime = LocalDateTime.now();
			  logger.info("Fulfillment Search String" +fulFillmentSearchNode.toString()); 
			  try {
				  fulfillmentResponse =onlineApiService.sendDataTofulfillmentRest(fulFillmentSearchNode.toString());
			  }catch(Exception e) {
				  logger.error("Error in Calling API of FulFillment (WatchOut API)"+e.getMessage()); 
			  }
			  logger.info("Fulfillment Rest Response"+fulfillmentResponse);
			  logger.info("Some sleep after fulfillment API");
			 
			// Put Time out Code here
			//timeOut();
			resultWatchOut.put("WatchOut Input", fulFillmentSearchNode.toString());
			resultWatchOut.put("Raw Output", fulfillmentResponse);
			parseAPIResponseService.parseWatchoutResponse(mapper, personalInfoSearch, fulfillmentResponse, din, resultWatchOut);
			//parseWatchoutResponse(mapper, personalInfoSearch, fulfillmentResponse, din, resultWatchOut);
			LocalDateTime endTime = LocalDateTime.now();
			 Duration duration = Duration.between(startTime, endTime);
			  logger.info(duration.getSeconds() + " seconds");
			  resultWatchOut.put("Start Time",startTime.toString());
			  resultWatchOut.put("End Time",endTime.toString());
			  resultWatchOut.put("Time Taken",duration.getSeconds());
			finalResultNode.set("WatchOut", resultWatchOut);
			logger.info("Value of Final Result After WatchOut"+finalResultNode);
			/*------------------WatchOut Call End ----------------------------------------*/
			
			String onlineResponse = mapper.writeValueAsString(finalResultNode);
			String returnStr = onlineResponse;
			if (asyncStatus)
				callback(returnStr);
			return returnStr;
			} catch (Exception e) {
			logger.error(e.getMessage(), e);
			if (asyncStatus)
				callback(e.getMessage());
			return e.getMessage();
		}
	}

	private void parseWatchoutResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,
			String fulfillmentResponse, String din, ObjectNode resultWatchOut)
			throws JsonProcessingException, JsonMappingException {
		boolean clear = false;
		boolean manual = false;
		boolean recordFound = false;

		String clearStr = "";
		String manualStr = "";
		String recordFoundStr = "";
		
		if(fulfillmentResponse==null) 
		{ 
			//resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship as clear");
			clear = true;
			clearStr = "Auto Tag Disqualified directorship as clear";
		  logger.info("Auto Tag Disqualified directorship as clear"); 
		  }else {
			  logger.info("Auto Tag Disqualified directorship Else Part"+resultWatchOut);
			  JsonNode resultResponse=(ObjectNode) mapper.readTree(fulfillmentResponse);
			  logger.info("Value of Raw Data"+resultResponse.get("rawData"));
			  logger.info("Value of raw data Empty"+resultResponse.get("rawData").isEmpty());
			  
			 if(resultResponse.get("rawData")!=null)
			 { 
				 logger.info("Length of Raw Data"+resultResponse.get("rawData").asText().length());
					if(resultResponse.get("rawData").asText().length()>1) {
						String rawDataStr=resultResponse.get("rawData").asText(); 
						 JsonNode rawDataStrNode=(ObjectNode) mapper.readTree(rawDataStr);
						 logger.info("Value of JsonNode rawData"+rawDataStrNode);
						 //Logic for Matching the PIN_CIN_DIN
				   	  Boolean panFlag=false; 
					   	  if(rawDataStrNode.get("Result").has("Table")) {
					   	  if(rawDataStrNode.get("Result").get("Table").isArray()) {
					   		ArrayNode tableList=(ArrayNode)rawDataStrNode.get("Result").get("Table"); 
						   	  for(int i=0;i<tableList.size();i++) { 
						   		  if(tableList.get(i).get("PAN_CIN_DIN")!=null) 
						   		  {
						   			  String pan_cin_din=tableList.get(i).get("PAN_CIN_DIN").asText();
						   			  if(pan_cin_din.equalsIgnoreCase("DIN:"+din)) 
						   			  { 
						   				  panFlag=true;
						   				  logger.info("Auto Tag Disqualified directorship as records found");
						   				  //resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship as records found");
						   				  recordFound = true;
						   				  recordFoundStr = "Auto Tag Disqualified directorship as records found";
						   				  resultWatchOut.put("WatchOut Annexure",tableList.get(i).get("Regulatory_Action_Source1").asText());
						   				  resultWatchOut.put("WatchOut Summary",tableList.get(i).get("Regulatory_Charges").asText()); 
						   				  //Provide copy of the hyperlink from FD : Regulatory_Action_Source1 for Annexure 
						   				  //Provide summary of hit from FD Regulatory_Charges 
						   				  } 
						   			  } 
						   		  }
						  	  //Logic for Matching the Defaulter_Name
						  Boolean defaulterFlag=false; 
						  if(!panFlag) { 
							  for(int i=0;i<tableList.size();i++) {
								  if(tableList.get(i).get("Defaulter_Name")!=null) { 
									  String defaulterName=tableList.get(i).get("Defaulter_Name").asText();
									  if(defaulterName.equalsIgnoreCase(personalInfoSearch.getName())) {
										  logger.info("Send for Manual review"); defaulterFlag=true;
										  //resultWatchOut.put("WatchOut Output", "Send for Manual review");
										  manual = true;
										  manualStr = "Send for Manual review";
										  resultWatchOut.put("WatchOut Annexure",tableList.get(i).get("Regulatory_Action_Source1").asText());
										  resultWatchOut.put("WatchOut Summary",tableList.get(i).get("Regulatory_Charges").asText()); 
										  } 
									  } 
								  } 
							  }
						  if(!defaulterFlag) { 
							  //resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship as clear "); 
							  clear = true;
							  clearStr = "Auto Tag Disqualified directorship as clear ";
							  }
					   	  }else {
					   		JsonNode tableList=rawDataStrNode.get("Result").get("Table"); 
					   		if(tableList.get("PAN_CIN_DIN")!=null) 
					   		  {
					   			  String pan_cin_din=tableList.get("PAN_CIN_DIN").asText();
					   			  if(pan_cin_din.equalsIgnoreCase("DIN:"+din)) 
					   			  { 
					   				  panFlag=true;
					   				  logger.info("Auto Tag Disqualified directorship as records found");
					   				  //resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship as records found");
					   				  recordFound = true;
					   				  recordFoundStr = "Auto Tag Disqualified directorship as records found";
					   				  resultWatchOut.put("WatchOut Annexure",tableList.get("Regulatory_Action_Source1").asText());
					   				  resultWatchOut.put("WatchOut Summary",tableList.get("Regulatory_Charges").asText()); 
					   				  //Provide copy of the hyperlink from FD : Regulatory_Action_Source1 for Annexure 
					   				  //Provide summary of hit from FD Regulatory_Charges 
					   				  } 
					   			  } 
					   		Boolean defaulterFlag=false; 
							  if(!panFlag) { 
								  if(tableList.get("Defaulter_Name")!=null) { 
										  String defaulterName=tableList.get("Defaulter_Name").asText();
										  if(defaulterName.equalsIgnoreCase(personalInfoSearch.getName())) {
											  logger.info("Send for Manual review"); defaulterFlag=true;
											  manual = true;
											  manualStr = "Send for Manual review";
											  //resultWatchOut.put("WatchOut Output", "Send for Manual review");
											  resultWatchOut.put("WatchOut Annexure",tableList.get("Regulatory_Action_Source1").asText());
											  resultWatchOut.put("WatchOut Summary",tableList.get("Regulatory_Charges").asText()); 
											  } 
										  }  
								  		}
							  if(!defaulterFlag) { 
								  clear = true;
								  clearStr = "Auto Tag Disqualified directorship as clear ";		  
								  //resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship as clear "); 
								  }
					   		
					   		
					   	  }
					   }else {
						clear = true;
						clearStr = "Auto Tag Disqualified directorship as clear.Result has no Table Fields";		  
				   		//resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship as clear.Result has no Table Fields");
				   		 logger.info("Result has no Table Fields");
				   	 }
					}else {
						clear = true;
						clearStr = "Raw Data is Empty. Auto Tag Disqualified directorship as clear";
						//resultWatchOut.put("WatchOut Output","Raw Data is Empty. Auto Tag Disqualified directorship as clear");
						logger.info("raw Data is empty");
					}
		  }else {
			  logger.info("Value of Raw data is null");
			  clear = true;
			  clearStr = "Raw Data is null. Auto Tag Disqualified directorship as clear";
			  //resultWatchOut.put("WatchOut Output","Raw Data is null. Auto Tag Disqualified directorship as clear");
		  }
		  //resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship Else Part"); 
		}
		
		String finalResult = "";
		String finalStatus = "";
		if(recordFound) {
			finalResult = recordFoundStr;
			finalStatus = "Record Found";
		}else if(manual) {
			finalResult = manualStr;
			finalStatus = "Manual";
		} else if(clear) {
			finalResult = clearStr;
			finalStatus = "Clear";
		}
				
		resultWatchOut.put("WatchOut Output", finalResult);
		resultWatchOut.put("status", finalStatus);
	}

	private void callback(String postStr) {
		try {

			logger.debug("postStr\n" + postStr);
			URL url = new URL(env.getProperty("online.router.callback.url"));
			logger.debug("Using callback URL\n" + url);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			// Creating the ObjectMapper object
			String authToken = JsonParser.parseString(postStr).getAsJsonObject().get("metadata").getAsJsonObject()
					.get("requestAuthToken").getAsString();

			logger.debug("Auth Token\n" + authToken);
			con.setRequestProperty("tokenId", authToken);

			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			os.write(postStr.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();
			logger.debug("Callback POST Response Code: " + responseCode + " : " + con.getResponseMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
