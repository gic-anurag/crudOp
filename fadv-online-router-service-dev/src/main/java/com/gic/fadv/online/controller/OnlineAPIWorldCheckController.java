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
public class OnlineAPIWorldCheckController {

	@Autowired
	private Environment env;
	@Autowired
	private OnlineApiService onlineApiService;
	@Autowired
	private ParseAPIResponseService parseAPIResponseService;
	
	private static final Logger logger = LoggerFactory.getLogger(OnlineAPIWorldCheckController.class);

	@ApiOperation(value = "This service is used to process Records at Online worldcheck API and return the result ", response = List.class)
	@PostMapping(path = "/async/onlineapi/worldcheck", consumes = "application/json", produces = "application/json")
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

	@ApiOperation(value = "This service is used to process Records at Online worldcheck API and return the result ", response = List.class)
	@PostMapping(path = "/onlineapi/worldcheck", consumes = "application/json", produces = "application/json")
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
			String worldCheckSearchString = null, worldCheckResponse = null;
			//Need to Set Din Also
			String din = personalInfoSearch.getDin()!=null?personalInfoSearch.getDin():"";
			
			ObjectNode resultWorldCheck = mapper.createObjectNode();
			ObjectNode finalResultNode = mapper.createObjectNode();
						
			/*------------------World Check Call ----------------------------------------*/
			/* World Check API */
			logger.info("Making Search World Check");
			//worldCheckSearchString ="{\"name\":" +"\""+ names[0]+" "+names[2]+"\""+ "}";
			ObjectNode worldSearchNode = mapper.createObjectNode();
			if(personalInfoSearch.getName()!=null) {
				worldSearchNode.put("name",personalInfoSearch.getName());
			}
			if(personalInfoSearch.getDob()!=null) {
				String formattedDob=Utility.formatDateUtil(personalInfoSearch.getDob());
				//worldSearchNode.put("dob",personalInfoSearch.getDob());
				worldSearchNode.put("dob",formattedDob);
			}
			if(personalInfoSearch.getFather_name()!=null) {
				worldSearchNode.put("father_name",personalInfoSearch.getFather_name());
			}
			if(personalInfoSearch.getGender()!=null) {
				worldSearchNode.put("gender",personalInfoSearch.getGender());
			}
			if(personalInfoSearch.getCountryAcr()!=null) {
				worldSearchNode.put("countryAcr",personalInfoSearch.getCountryAcr());
			}
			worldCheckSearchString=worldSearchNode.toString();
			/*
			 * worldCheckSearchString = "{\"name\":" + "\"" + personalInfoSearch.getName()
			 * +"\",\"dob\":\"" + personalInfoSearch.getDob() + "\",\"father_name\":\"" +
			 * personalInfoSearch.getFather_name()+ "\",\"gender\":\"" +
			 * personalInfoSearch.getGender() + "\",\"countryAcr\":\"" +
			 * personalInfoSearch.getCountryAcr()+ "\"" + "}";
			 */
			
			LocalDateTime startTime = LocalDateTime.now();
			logger.info("World Check Search String" + worldCheckSearchString);
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
			resultWorldCheck.put("Raw Output", worldCheckResponse);
			//resultWorldCheck.put("status", "Manual");
			//resultWorldCheck.put("World Check Output", worldCheckResponse);
			parseAPIResponseService.parseWorldCheckResponse(mapper, personalInfoSearch, worldCheckResponse, din, resultWorldCheck);
			//parseWorldCheckResponse(mapper, personalInfoSearch, worldCheckResponse, din, resultWorldCheck);
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

	private void parseWorldCheckResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,
			String worldCheckResponse, String din, ObjectNode resultWorldCheck)
			throws JsonProcessingException, JsonMappingException {
		boolean clear = false;
		boolean manual = false;
		boolean recordFound = false;

		String clearStr = "";
		String manualStr = "";
		String recordFoundStr = "";
		
		if (worldCheckResponse == null) {
			//resultWorldCheck.put("World Check Output", "Auto Tag Loan Defaulter as Clear");
			clear = true;
			clearStr = "Auto Tag Loan Defaulter as Clear";
			logger.info("Auto Tag Loan Defaulter as Clear" + resultWorldCheck);
		} else {
			logger.info("Auto Tag Loan Defaulter Else Part "+worldCheckResponse);
			JsonNode resultResponse = (ObjectNode) mapper.readTree(worldCheckResponse);
			if (resultResponse.has("name")) {
				if (resultResponse.get("name").asText().equalsIgnoreCase(personalInfoSearch.getName())) {
					logger.info("FD: Event Action: Match Date of Birth");
					ArrayNode resultRecords = (ArrayNode) resultResponse.get("results");
					if (resultRecords != null) {
						logger.info("Result is not null");
						logger.info("Size of Result" + resultRecords.size());
						Boolean matchStrengthFlag=false;
						for (int i = 0; i < resultRecords.size(); i++) {
							if (resultRecords.get(i).has("matchStrength")) {
								matchStrengthFlag=true;
								if (resultRecords.get(i).get("matchStrength").asText().equalsIgnoreCase("EXACT")) {
									ArrayNode eventList = (ArrayNode) resultRecords.get(i).get("events");
									if (eventList != null && eventList.size() > 0) {
										for (int j = 0; j < eventList.size(); j++) {
											if (eventList.get(j).get("fullDate").asText()
													.equalsIgnoreCase(personalInfoSearch.getDob())) {
												logger.info("Date Birth is Matched! FD: Identity Document Action: Match DIN from Cross Directorship from Step 1");
												ArrayNode identityDocumentList = (ArrayNode) resultRecords.get(i).get("identityDocuments");
												if (identityDocumentList != null && identityDocumentList.size() > 0) {
													Boolean identityDocumentFlag = false;
													for (int k = 0; k < identityDocumentList.size(); k++) {
														if (identityDocumentList.get(k).get("type") != null) {
															if (identityDocumentList.get(k).get("type").asText()
																	.equalsIgnoreCase("din")) {
																if (identityDocumentList.get(k).get("number")
																		.asText().equalsIgnoreCase(din)) {
																	identityDocumentFlag = true;
																}
															}
														}
													}
													if (identityDocumentFlag) {
														logger.info("Din found in Identity Document List");
														resultWorldCheck.put("World Check Output","Auto Tag Disqualified directorship as records found");
														recordFound = true;
														recordFoundStr = "Auto Tag Disqualified directorship as records found";		
													} else {
														logger.info("Din not found in Identity Document List. Search for categories");
														ArrayNode categories = (ArrayNode) resultRecords.get(i).get("categories");
														if (categories != null && categories.size() > 0) {
															Boolean categoriesFlag = false;
															for (int l = 0; l < categories.size(); l++) {
																if (categories.get(l).asText()
																		.equalsIgnoreCase("SANCTIONS")) {
																	categoriesFlag = true;
																	//break;
																}
															}
															if (categoriesFlag) {
																//resultWorldCheck.put("World Check Output","(Primary Match) Send for Manual review under check type “Global Database Checks”");
																manual = true;
																manualStr = "(Primary Match) Send for Manual review under check type “Global Database Checks”";
															} else {
																//resultWorldCheck.put("World Check Output","( Primary Match) Send for Manual review under check type “Web & Media”");
																manual = true;
																manualStr = "( Primary Match) Send for Manual review under check type “Web & Media”";
															}
														}else {
															//resultWorldCheck.put("World Check Output","Search for categories is empty");
															clear = true;
															clearStr = "Search for categories is empty"; 
															logger.info("Search for categories is empty");
														}
													}
												} else {
													clear = true;
													clearStr = "IdentityDocumentList is Empty";
													//resultWorldCheck.put("World Check Output","IdentityDocumentList is Empty");
													logger.info("Value of IdentityDocumentList is Empty");
												}
											} else {
												logger.info("Date Birth is not Matched! Check for address in event");
												if (eventList.get(j).get("address").asText()
														.equalsIgnoreCase(personalInfoSearch.getAddress())) {
													ArrayNode categories = (ArrayNode) resultRecords.get(i)
															.get("categories");
													if (categories != null && categories.size() > 0) {
														Boolean categoriesFlag = false;
														for (int l = 0; l < categories.size(); l++) {
															if (categories.get(l).asText()
																	.equalsIgnoreCase("SANCTIONS")) {
																categoriesFlag = true;
																//break;
															}
														}
														if (categoriesFlag) {
															manual = true;
															manualStr = "( Secondary Match)  Send for Manual review under check type “Global Database Checks”";
//															resultWorldCheck.put("World Check Output",
//																	"( Secondary Match)  Send for Manual review under check type “Global Database Checks”");
														} else {
															manual = true;
															manualStr = "( Secondary Match)  Send for Manual review under check type “Web & Media”";
//															resultWorldCheck.put("World Check Output",
//																	"( Secondary Match)  Send for Manual review under check type “Web & Media”");
														}
													}else {
														//resultWorldCheck.put("World Check Output","categories is empty");
														clear = true;
														clearStr = "categories is empty";
														logger.info("categories is null or empty");
													}
												} else {
													clear = true;
													clearStr = "Address didn't match";
													//resultWorldCheck.put("World Check Output","Address didn't match");
													logger.info("Address didn't match");
												}
											}
										}
									} else {
										clear = true;
										clearStr = "Event List is Empty";
										//resultWorldCheck.put("World Check Output","Event List is Empty");
										logger.info("Event List is Empty");
									}
								} else if (resultRecords.get(i).get("matchStrength").asText()
										.equalsIgnoreCase("MEDIUM")) {
									ArrayNode eventList = (ArrayNode) resultRecords.get(i).get("events");
									if (eventList != null && eventList.size() > 0) {
										for (int j = 0; j < eventList.size(); j++) {
											if (eventList.get(j).get("fullDate").asText()
													.equalsIgnoreCase(personalInfoSearch.getDob())) {
												logger.info(
														"Date Birth is Matched! FD: Identity Document Action: Match DIN from Cross Directorship from Step 1");
												ArrayNode identityDocumentList = (ArrayNode) resultRecords.get(i)
														.get("identityDocuments");
												if (identityDocumentList != null
														&& identityDocumentList.size() > 0) {
													Boolean identityDocumentFlag = false;
													for (int k = 0; k < identityDocumentList.size(); k++) {
														if (identityDocumentList.get(k).get("type") != null) {
															if (identityDocumentList.get(k).get("type").asText()
																	.equalsIgnoreCase("din")) {
																if (identityDocumentList.get(k).get("number")
																		.asText().equalsIgnoreCase(din)) {
																	identityDocumentFlag = true;
																}
															}
														}
													}
													if (identityDocumentFlag) {
														logger.info("Din found in Identity Document List");
														//resultWorldCheck.put("World Check Output","Auto Tag Disqualified directorship as records found");
														recordFound = true;
														recordFoundStr = "Auto Tag Disqualified directorship as records found";
													} else {
														logger.info(
																"Din not found in Identity Document List. Search for categories");
														ArrayNode categories = (ArrayNode) resultRecords.get(i)
																.get("categories");
														if (categories != null && categories.size() > 0) {
															Boolean categoriesFlag = false;
															for (int l = 0; l < categories.size(); l++) {
																if (categories.get(l).asText()
																		.equalsIgnoreCase("SANCTIONS")) {
																	categoriesFlag = true;
																	//break;
																}
															}
															if (categoriesFlag) {
																manual = true;
																manualStr = "(Secondary Match) Send for Manual review under check type “Global Database Checks”";
//																resultWorldCheck.put("World Check Output",
//																		"(Secondary Match) Send for Manual review under check type “Global Database Checks”");
															} else {
																manual = true;
																manualStr = "( Secondary Match) Send for Manual review under check type “Web & Media”";
//																resultWorldCheck.put("World Check Output",
//																		"( Secondary Match) Send for Manual review under check type “Web & Media”");
															}
														}
													}
												} else {
													clear = true;
													clearStr = "IdentityDocumentList is Empty";
													//resultWorldCheck.put("World Check Output","IdentityDocumentList is Empty");
													logger.info("Value of IdentityDocumentList is Empty");
												}
											} else {
												logger.info(
														"Date Birth is not Matched! Check for address in event");
												if (eventList.get(j).get("address").asText()
														.equalsIgnoreCase(personalInfoSearch.getAddress())) {
													ArrayNode categories = (ArrayNode) resultRecords.get(i)
															.get("categories");
													if (categories != null && categories.size() > 0) {
														Boolean categoriesFlag = false;
														for (int l = 0; l < categories.size(); l++) {
															if (categories.get(l).asText()
																	.equalsIgnoreCase("SANCTIONS")) {
																categoriesFlag = true;
																//break;
															}
														}
														if (categoriesFlag) {
															manual = true;
															manualStr = "( Secondary Match)  Send for Manual review under check type “Global Database Checks”";
//															resultWorldCheck.put("World Check Output",
//																	"( Secondary Match)  Send for Manual review under check type “Global Database Checks”");
														} else {
															manual = true;
															manualStr = "( Secondary Match)  Send for Manual review under check type “Web & Media”";
//															resultWorldCheck.put("World Check Output",
//																	"( Secondary Match)  Send for Manual review under check type “Web & Media”");
														}
													}
												} else {
													clear = true;
													clearStr = "Address didn't match";
													//resultWorldCheck.put("World Check Output","Address didn't match");
													logger.info("Address didn't match");
												}
											}
										}
									} else {
										clear = true;
										clearStr = "Event List is Empty";
										//resultWorldCheck.put("World Check Output","Event List is Empty");
										logger.info("Event List is Empty");
									}
								} else {
									logger.info("match Strength is not EXACT and MEDIUM");
									clear = true;
									clearStr = "Match Strength is not EXACT and MEDIUM";
									//resultWorldCheck.put("World Check Output", "Match Strength is not EXACT and MEDIUM");
								}
							}
						}
						if(!matchStrengthFlag) {
							clear = true;
							clearStr = "matchStrength tag is not found";
							//resultWorldCheck.put("World Check Output", "matchStrength tag is not found");
						}
					} else {
						clear = true;
						clearStr = "Result list is empty";
						//resultWorldCheck.put("World Check Output", "Result list is empty");
					}
				} else {
					recordFound = true;
					recordFoundStr = "Auto Tag “Web & Media & Global Database check” as No records found";
					//resultWorldCheck.put("World Check Output","Auto Tag “Web & Media & Global Database check” as No records found");
				}
			} else {
				clear = true;
				clearStr = "name tag is not found in worldCheck response";
				resultWorldCheck.put("World Check Output", "name tag is not found in worldCheck response");
			}
			// resultWorldCheck.put("World Check Output", "Auto Tag Loan Defaulter Else Part");
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
				
		resultWorldCheck.put("World Check Output", finalResult);
		resultWorldCheck.put("status", finalStatus);
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
