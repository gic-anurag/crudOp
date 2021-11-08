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
@Api(value = "Cross Reference", description = "Operations pertaining to Online Manupatra API  Controller (Cross Reference master data)")
public class OnlineAPIManupatraController {

	@Autowired
	private Environment env;
	@Autowired
	private OnlineApiService onlineApiService;
	@Autowired
	private ParseAPIResponseService parseAPIResponseService;
	
	private static final Logger logger = LoggerFactory.getLogger(OnlineAPIManupatraController.class);

	@ApiOperation(value = "This service is used to process Records at Online Manupatra API and return the result ", response = List.class)
	@PostMapping(path = "/async/onlineapi/manupatra", consumes = "application/json", produces = "application/json")
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

	@ApiOperation(value = "This service is used to process Records at Online Manupatra API and return the result ", response = List.class)
	@PostMapping(path = "/onlineapi/manupatra", consumes = "application/json", produces = "application/json")
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
			String manuPatraSearchString = null;
			String manuPatraResponse = null;
			
			ObjectNode resultManupatra = mapper.createObjectNode();
			ObjectNode finalResultNode = mapper.createObjectNode();
			/*------------------Manupatra Call ----------------------------------------*/
			logger.info("Making call for Manupatra");
			ObjectNode manuPatraSearchNode=mapper.createObjectNode();
			/*
			 * if(personalInfoSearch.getName()!=null) {
			 * manuPatraSearchString="{\"formattedName\":\"" +personalInfoSearch.getName()+
			 * "\"}"; }
			 */
			if(personalInfoSearch.getName()!=null) {
				manuPatraSearchNode.put("formattedName", personalInfoSearch.getName());
			}
			if(personalInfoSearch.getDob()!=null) {
				manuPatraSearchNode.put("dob", personalInfoSearch.getDob());
			}
			if(personalInfoSearch.getFather_name()!=null) {
				manuPatraSearchNode.put("father_name", personalInfoSearch.getFather_name());
			}
			if(personalInfoSearch.getContexts()!=null) {
				manuPatraSearchNode.put("contexts", personalInfoSearch.getContexts());
			}
			manuPatraSearchString=manuPatraSearchNode.toString();
			logger.info("Manupatra Search String" + manuPatraSearchString);
			LocalDateTime startTime = LocalDateTime.now();
			try {
			manuPatraResponse = onlineApiService.sendDataToManupatraRest(manuPatraSearchString);
			} catch (Exception e) {
				logger.error("Error in Calling API of Manupatra" + e.getMessage());
			}
			logger.info("Manupatra Rest Response" + manuPatraResponse);
			logger.info("Some sleep after Manupatra API");
			// Put Time out Code here
			//timeOut();
			resultManupatra.put("ManuPatra Input", manuPatraSearchString);
			resultManupatra.put("Raw Output", manuPatraResponse);
			parseAPIResponseService.parseManupatraResponse(mapper, personalInfoSearch, manuPatraResponse, resultManupatra);
			//parseManupatraResponse(mapper, personalInfoSearch, manuPatraResponse, resultManupatra);
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

	private void parseManupatraResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch, String manuPatraResponse,
			ObjectNode resultManupatra) throws JsonProcessingException, JsonMappingException {
		
		boolean clear = false;
		boolean manual = false;
		boolean gateWayTimeout= false;
		String gateWayTimeoutStr="";
		String clearStr = "";
		String manualStr = "";
		
		if (manuPatraResponse == null) {
			//resultManupatra.put("ManuPatra Output", "Auto Tag Civil Litigation as clear");
			gateWayTimeout = true;
			gateWayTimeoutStr = "GateWay Time Out";
			logger.info("GateWay Time Out" + resultManupatra);
			/*
			 * clearStr = "Auto Tag Civil Litigation as clear";
			 * logger.info("Auto Tag Civil Litigation as clear" + resultManupatra);
			 */
		} else {
			logger.info("Auto Tag Civil Litigation Else Part" + resultManupatra);
			JsonNode resultResponse = (ObjectNode) mapper.readTree(manuPatraResponse);
			logger.info("Value of Raw Data" + resultResponse.get("rawData"));
			// System.out.println("Value of Raw Data"+resultResponse.get("rawData"));
			if (resultResponse.get("rawData") != null ) {
				logger.info("Length of Raw Data"+resultResponse.get("rawData").asText().length());
				if(resultResponse.get("rawData").asText().length()>1) {
					String rawDataStr = resultResponse.get("rawData").asText();
					JsonNode rawDataStrNode = (ObjectNode) mapper.readTree(rawDataStr);
					logger.info("Value of JsonNode rawData" + rawDataStrNode);
					//Take Data From RAW DATA
					JsonNode rawDataStrNodeDataNode=rawDataStrNode.get("data");
					if(rawDataStrNodeDataNode!=null) {
						logger.info("Size of ArrayNode"+rawDataStrNodeDataNode.size());
						logger.info("Value of ArrayNode"+rawDataStrNodeDataNode);
						ArrayNode dataListValue =(ArrayNode)mapper.readTree(rawDataStrNodeDataNode.asText());
						logger.info("Value of Data in Json"+dataListValue);
						/*
						 * Iterate for file data in each data ArrayNode
						 */
						logger.info("Size of Data List Value"+dataListValue.size());
						/*
						 * Split Name in First name, Middle Name and last Name
						 */
						String [] names=personalInfoSearch.getName().split(" ");
						String firstName=null,lastName=null,middleName=null;
						if(names.length==3) {
							firstName=names[0];
							lastName=names[2];
							middleName=names[1];
						 }else if(names.length==2) { 
							 	firstName=names[0];
							 	lastName=names[1];
						}else{ 
							firstName=personalInfoSearch.getName(); 
						  }
						Boolean nameMatchflag=false,firstNameMatchFlag=false,lastNameMatchFlag=false;
						ObjectNode firstNameMatchResultNode=mapper.createObjectNode();
						ObjectNode lastNameMatchResultNode=mapper.createObjectNode();
						for(int i=0;i<dataListValue.size();i++) {
							logger.info("Value of title in Data Json"+dataListValue.get(i).get("title"));
							logger.info("Value of title in Data Json"+dataListValue.get(i).get("fileData"));
							JsonNode fileDataNode=dataListValue.get(i).get("fileData");
							JsonNode titleNode=dataListValue.get(i).get("title");
							if(fileDataNode!=null) {
								if(fileDataNode.asText().contains(personalInfoSearch.getName())) {
									nameMatchflag=true;
									logger.info("Match Found!Search for Other Parameters (address / State/ City / Father’s name) in fileData");
									String fileDataStr=fileDataNode.asText();
									JsonNode fileDataValue =mapper.readTree(fileDataStr);
									logger.info("Value of Data in Json"+fileDataValue);
									logger.info("Value of Result in Data Json"+fileDataValue.get("Result"));
									//Since Result and File Data Doesn't have (address / State/ City / Father’s name)
									//So, putting the else condition in ManuPatra Output
									// (Secondary Match): Send for Manual review under India Civil Litigation & Criminal  
									//Provide copy of the hyperlink from FD : Filedata
									//resultManupatra.put("ManuPatra Output", "Send for Manual review under India Civil Litigation & Criminal");
									manual = true;
									manualStr = "Send for Manual review under India Civil Litigation & Criminal";
									resultManupatra.put("Matched Key Name", personalInfoSearch.getName());
									if(fileDataValue.get("Result")!=null) {
										resultManupatra.put("ManuPatra FileData", fileDataValue.get("Result").asText());
										
									}else {
										resultManupatra.put("ManuPatra FileData", "file data Result field is Empty/Null");
									}
									
									//break;
									//If address and other match found then
									//(Primary Match)  Send for Manual review under India Civil Litigation & Criminal.
									//Provide copy of the hyperlink from FD : Filedata	
									
								}else if(firstName!=null && fileDataNode.asText().contains(firstName)) {
									firstNameMatchFlag=true;
									/* Store all data in a Array Node*/
									logger.info("Match Found!Search for Other Parameters (address / State/ City / Father’s name) in fileData");
									String fileDataStr=fileDataNode.asText();
									JsonNode fileDataValue =mapper.readTree(fileDataStr);
									logger.info("Value of Data in Json"+fileDataValue);
									logger.info("Value of Result in Data Json"+fileDataValue.get("Result"));
									
									//firstNameMatchResultNode.put("ManuPatra Output", "Send for Manual review under India Civil Litigation & Criminal");
									manual = true;
									manualStr = "Send for Manual review under India Civil Litigation & Criminal";
									if(fileDataValue.get("Result")!=null) {
										firstNameMatchResultNode.put("ManuPatra FileData", fileDataValue.get("Result").asText());
										
									}else {
										firstNameMatchResultNode.put("ManuPatra FileData", "file data Result field is Empty/Null");
									}
									firstNameMatchResultNode.put("Matched Key Name", firstName);
								}else if(lastName!=null && fileDataNode.asText().contains(lastName)) {
									lastNameMatchFlag=true;
									logger.info("Match Found!Search for Other Parameters (address / State/ City / Father’s name) in fileData");
									String fileDataStr=fileDataNode.asText();
									JsonNode fileDataValue =mapper.readTree(fileDataStr);
									logger.info("Value of Data in Json"+fileDataValue);
									logger.info("Value of Result in Data Json"+fileDataValue.get("Result"));
									//lastNameMatchResultNode.put("ManuPatra Output", "Send for Manual review under India Civil Litigation & Criminal");
									manual = true;
									manualStr = "Send for Manual review under India Civil Litigation & Criminal";
									if(fileDataValue.get("Result")!=null) {
										lastNameMatchResultNode.put("ManuPatra FileData", fileDataValue.get("Result").asText());
										
									}else {
										lastNameMatchResultNode.put("ManuPatra FileData", "file data Result field is Empty/Null");
									}
									lastNameMatchResultNode.put("Matched Key Name", lastName);
								}
							}
						}
						if(!nameMatchflag) {
							/*
							 * Let's Check for first name Search
							 */
							if(firstNameMatchFlag) {
								resultManupatra.set("ManuPatra Output", firstNameMatchResultNode.get("ManuPatra Output"));
								resultManupatra.set("Matched Key Name", firstNameMatchResultNode.get("Matched Key Name"));
								resultManupatra.set("ManuPatra FileData", firstNameMatchResultNode.get("ManuPatra FileData"));
								
							}else if(lastNameMatchFlag) {
								resultManupatra.set("ManuPatra Output", lastNameMatchResultNode.get("ManuPatra Output"));
								resultManupatra.set("Matched Key Name", lastNameMatchResultNode.get("Matched Key Name"));
								resultManupatra.set("ManuPatra FileData", lastNameMatchResultNode.get("ManuPatra FileData"));
							}else {
								logger.info("nameMatchflag is false");
								clear = true;
								clearStr = "Auto Tag Civil Litigation as clear";
								//resultManupatra.put("ManuPatra Output", "Auto Tag Civil Litigation as clear");
							}
						}
					}else {
						clear = true;
						clearStr = "Auto Tag Civil Litigation as clear.Data field of Raw Data is Empty";
						//resultManupatra.put("ManuPatra Output", "Auto Tag Civil Litigation as clear.Data field of Raw Data is Empty");
						logger.info("Raw Data is Empty");
					}
				}else {
					clear = true;
					clearStr = "Raw Data is Empty.Auto Tag Civil Litigation as clear";
					//resultManupatra.put("ManuPatra Output", "Raw Data is Empty.Auto Tag Civil Litigation as clear");
					logger.info("Raw Data is Empty");
				}
			}else {
				clear = true;
				clearStr = "Raw Data is Null.Auto Tag Civil Litigation as clear";
				//resultManupatra.put("ManuPatra Output", "Raw Data is Null.Auto Tag Civil Litigation as clear");
				logger.info("Raw Data is Null");
			}
		}
		
		String finalResult = "";
		String finalStatus = "";
		if(gateWayTimeout) {
			finalResult = gateWayTimeoutStr;
			finalStatus = "GateWayTimeOut";
		}
		else if(manual) {
			finalResult = manualStr;
			finalStatus = "Manual";
		} else if(clear) {
			finalResult = clearStr;
			finalStatus = "Clear";
		}
		
		resultManupatra.put("ManuPatra Output", finalResult);
		resultManupatra.put("status", finalStatus);
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
