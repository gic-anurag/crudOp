package com.gic.fadv.online.controller;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

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
import com.gic.fadv.online.task.LoanDefaulterTask;
import com.gic.fadv.online.utility.Utility;
import com.google.gson.JsonParser;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/india")
@Api(value = "Cross Reference", description = "Operations pertaining to Online API Controller (Cross Reference master data)")
public class OnlineAPILoanController {

	@Autowired
	private Environment env;
	@Autowired
	private OnlineApiService onlineApiService;
	@Autowired
	private ParseAPIResponseService parseAPIResponseService;
	
	private static final Logger logger = LoggerFactory.getLogger(OnlineAPILoanController.class);

	@ApiOperation(value = "This service is used to process Records at Online API and return the result ", response = List.class)
	@PostMapping(path = "/async/onlineapi/loan", consumes = "application/json", produces = "application/json")
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
	@PostMapping(path = "/onlineapi/loan", consumes = "application/json", produces = "application/json")
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
				
			// Converting the JSONString to Object
			PersonInfoSearch personalInfoSearch = mapper.readValue(inStr, PersonInfoSearch.class);
			
			String personalResponse = null;
			String verifyId=null;
			
			ObjectNode resultLoan = mapper.createObjectNode();
			ObjectNode finalResultNode = mapper.createObjectNode();
			ObjectNode personalSearchNode= mapper.createObjectNode();
			/*------------------credit_reputational_cibil (Loan Call) ----------------------------------------*/
			/*
			 * Call credit_reputational_cibil API
			 */
			personalInfoSearch.setServices("credit_reputational_cibil");
			logger.info("Making Search String For credit_reputational_cibil");
			personalSearchNode.put("name", personalInfoSearch.getName());
			personalSearchNode.put("address", personalInfoSearch.getAddress());
			personalSearchNode.put("services", personalInfoSearch.getServices());
			LocalDateTime startTime = LocalDateTime.now();
			logger.info("Loan Personal Search String" + personalSearchNode.toString());
			try {
				verifyId = onlineApiService.sendDataToServicePersonalRest(personalSearchNode.toString());
			} catch (Exception e) {
				logger.error("Error in Calling API of credit_reputational_cibil" + e.getMessage());
			}
			logger.info("Parse String and Take out VerifyID"+verifyId);
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
			resultLoan.put("Verify_id", verifyId);
			resultLoan.put("Raw Output", personalResponse);
			parseAPIResponseService.parseLoanDefaulterResponse(mapper, personalInfoSearch, personalResponse, resultLoan);
			//parseLoanDefaulterResponse(mapper, personalInfoSearch, personalResponse, resultLoan);
			LocalDateTime endTime = LocalDateTime.now();
			 Duration duration = Duration.between(startTime, endTime);
			  logger.info(duration.getSeconds() + " seconds");
			  resultLoan.put("Start Time",startTime.toString());
			  resultLoan.put("End Time",endTime.toString());
			  resultLoan.put("Time Taken",duration.getSeconds());
			finalResultNode.set("Loan Defaulter", resultLoan);
			logger.info("Value of Final Result" + finalResultNode);
			/*------------------credit_reputational_cibil (Loan Call End) ----------------------------------------*/
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

	
	public void parseLoanDefaulterResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,
			String personalResponse, ObjectNode resultLoan) throws JsonProcessingException, JsonMappingException {
		boolean clear = false;
		boolean manual = false;

		String clearStr = "";
		String manualStr = "";
		ArrayNode links =mapper.createArrayNode();
		ArrayNode annexureList= mapper.createArrayNode();
		
		if (personalResponse == null) {
			//resultLoan.put("Loan Defaulter Output", "Auto Tag Loan Defaulter as Clear");
			clear = true;
			clearStr = "Auto Tag Loan Defaulter as Clear";
			logger.info("Auto Tag Loan Defaulter as Clear" + resultLoan);
		} else {
			logger.info("Auto Tag Loan Defaulter Else Part" + resultLoan);
			JsonNode resultResponse = (ObjectNode) mapper.readTree(personalResponse);
			ArrayNode results = (ArrayNode) resultResponse.get("results");
			ArrayNode data = (ArrayNode) results.get(0).get("data");
			if (data != null) {
				if (data.size() > 0) {
					for (int i = 0; i < data.size(); i++) {
						/* if (data.get(i).get("sub_type") != null) { */
							//if (data.get(i).get("sub_type").asText().equalsIgnoreCase("cibil defaulters")) {
								/*if (data.get(i).get("name").asText().equalsIgnoreCase(personalInfoSearch.getName())) {*/
								if (Utility.checkContains(data.get(i).get("name").asText(),personalInfoSearch.getName())) {
									if (data.get(i).get("address").asText().equalsIgnoreCase(personalInfoSearch.getAddress())) {
										logger.info(
												" (Primary Match)Send for Manual review.Provide copy of the hyperlink "
														+ "from FD : Link & FD: url for Annexure");
//										resultLoan.put("Loan Defaulter Output",
//												" (Primary Match)Send for Manual review.Provide copy of the hyperlink "
//														+ "from FD : Link & FD: url for Annexure");
										manual = true;
										manualStr = " (Primary Match)Send for Manual review.Provide copy of the hyperlink "
												+ "from FD : Link & FD: url for Annexure";
										//resultLoan.put("Link", data.get(i).get("link").asText());
										//resultLoan.put("Annexure", data.get(i).get("url").asText());
										if(data.get(i).get("link")!=null) {
											links.add(data.get(i).get("link").asText());
										}
										if(data.get(i).get("url")!=null) {
											annexureList.add(data.get(i).get("url").asText());
										}
										
										//break;
									} else {
										if (data.get(i).get("state_name").asText().equalsIgnoreCase(personalInfoSearch.getState())) {
											logger.info(
													"Output:- (Secondary Match). Send for Manual review. Provide copy of"
															+ " the hyperlink from FD : Link & FD: url for Annexure");
//											resultLoan.put("Loan Defaulter Output",
//													"Output:- (Secondary Match). Send for Manual review. Provide copy of"
//															+ "the hyperlink from FD : Link & FD: url for Annexure");
											manual = true;
											manualStr = "Output:- (Secondary Match). Send for Manual review. Provide copy of"
													+ "the hyperlink from FD : Link & FD: url for Annexure";
											//resultLoan.put("Link", data.get(i).get("link").asText());
											//resultLoan.put("Annexure", data.get(i).get("url").asText());
											if(data.get(i).get("link")!=null) {
												links.add(data.get(i).get("link").asText());
											}
											if(data.get(i).get("url")!=null) {
												annexureList.add(data.get(i).get("url").asText());
											}
											//break;
										} else {
											//resultLoan.put("Loan Defaulter Output",	"Auto Tag Loan Defaulter as Clear");
											logger.info(
													"Output:- (Tertiary Match). Send for Manual review. Provide copy of"
															+ " the hyperlink from FD : Link & FD: url for Annexure");
//											resultLoan.put("Loan Defaulter Output",
//													"Output:- (Secondary Match). Send for Manual review. Provide copy of"
//															+ "the hyperlink from FD : Link & FD: url for Annexure");
											manual = true;
											manualStr = "Output:- (Tertiary Match). Send for Manual review. Provide copy of"
													+ "the hyperlink from FD : Link & FD: url for Annexure";
											//resultLoan.put("Link", data.get(i).get("link").asText());
											//resultLoan.put("Annexure", data.get(i).get("url").asText());
											if(data.get(i).get("link")!=null) {
												links.add(data.get(i).get("link").asText());
											}
											if(data.get(i).get("url")!=null) {
												annexureList.add(data.get(i).get("url").asText());
											}
											//break;
										}
									}
								} else {
									//resultLoan.put("Loan Defaulter Output", "Auto Tag Loan Defaulter as Clear");
									clear = true;
									clearStr = 	"Auto Tag Loan Defaulter as Clear";	
									logger.info("Auto Tag Loan Defaulter as Clear" + resultLoan);
								}
								/*} 
								 * else { logger.info("Sub Type is not cibil"); }
								 */
						/*} else {
							logger.info("Sub Type is null");
						}*/
					}
				} else {
					clear = true;
					clearStr = 	"Data is Empty.Auto Tag Loan Defaulter as Clear";	
					//resultLoan.put("Loan Defaulter Output", "Data is Empty.Auto Tag Loan Defaulter as Clear");
					logger.info("Data is Empty");
				}
			} else {
				logger.info("Data is Null for cibil");
			}
			// resultLoan.put("Loan Defaulter Output", "Auto Tag Loan Defaulter Else Part");
		}
		String finalResult = "";
		String finalStatus = "";
		if(manual) {
			finalResult = manualStr;
			finalStatus = "Manual";
			resultLoan.set("Link", links);
			resultLoan.set("Annexure", annexureList);
		} else if(clear) {
			finalResult = clearStr;
			finalStatus = "Clear";
		}
				
		resultLoan.put("Loan Defaulter Output", finalResult);
		resultLoan.put("status", finalStatus);
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
