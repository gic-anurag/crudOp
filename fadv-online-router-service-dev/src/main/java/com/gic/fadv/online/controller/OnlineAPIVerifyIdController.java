package com.gic.fadv.online.controller;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/india")
@Api(value = "Cross Reference", description = "Operations pertaining to Online API Controller (Cross Reference master data)")
public class OnlineAPIVerifyIdController {

	@Autowired
	private Environment env;
	@Autowired
	private OnlineApiService onlineApiService;
	@Autowired
	private ParseAPIResponseService parseAPIResponseService;
	
	private static final Logger logger = LoggerFactory.getLogger(OnlineAPIVerifyIdController.class);

	@ApiOperation(value = "This service is used to process Records at Online API and return the result ", response = List.class)
	@PostMapping(path = "/async/onlineapi/verify-result", consumes = "application/json", produces = "application/json")
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
	@PostMapping(path = "/onlineapi/verify-result", consumes = "application/json", produces = "application/json")
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
			logger.info(inStr);
			// Creating the ObjectMapper object
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			String finalResponse=null;
			//String to Json
			JsonNode json = mapper.readTree(inStr);
			PersonInfoSearch personalInfoSearch=new PersonInfoSearch();
			ObjectNode resultMCA = mapper.createObjectNode();
			ObjectNode resultAdverseMedia = mapper.createObjectNode();
			ObjectNode resultLoan = mapper.createObjectNode();
			ObjectNode finalResultNode= mapper.createObjectNode();
			if(json.has("verify_id")) {
				ObjectNode verifyNode= mapper.createObjectNode();
				verifyNode.put("verify_id", json.get("verify_id").asText());
				logger.info("Second Seearch String.Personal Search String" + verifyNode.toString());
				try {
					finalResponse = onlineApiService.sendDataToFinalPersonalRest(verifyNode.toString());
					String apiName="";
					if(json.has("api")) {
						apiName =json.get("api").asText();
					}
					if(json.has("name")) {
						personalInfoSearch.setName(json.get("name").asText());
					}
					if(json.has("din")) {
						personalInfoSearch.setDin(json.get("din").asText());
					}
					if(json.has("dob")) {
						personalInfoSearch.setDob(json.get("dob").asText());
					}
					if(json.has("contexts")) {
						personalInfoSearch.setContexts(json.get("contexts").asText());
					}
					if(json.has("address")) {
						personalInfoSearch.setAddress(json.get("address").asText());
					}
					if(json.has("father_name")) {
						personalInfoSearch.setFather_name(json.get("father_name").asText());
					}
					if(json.has("state")) {
						personalInfoSearch.setState(json.get("state").asText());
					}
					if(json.has("startdate")) {
						personalInfoSearch.setStartdate(json.get("startdate").asText());
					}
					
					if(apiName.equalsIgnoreCase("mca")) {
						LocalDateTime startTime = LocalDateTime.now();
						//onlineApiMCAController.parseMCAResponse(mapper, personalInfoSearch, finalResponse,resultMCA);
						parseAPIResponseService.parseMCAResponse(mapper, personalInfoSearch, finalResponse,
								resultMCA);
						LocalDateTime endTime = LocalDateTime.now();
						Duration duration = Duration.between(startTime, endTime);
						resultMCA.put("Start Time",startTime.toString());
						resultMCA.put("End Time",endTime.toString());
						resultMCA.put("Time Taken",duration.getSeconds());
						finalResultNode.set("MCA", resultMCA);
						finalResponse=finalResultNode.toString();	
					}else if(apiName.equalsIgnoreCase("adversemedia")){
						LocalDateTime startTime = LocalDateTime.now();
						//onlineAPIAdverseMediaController.parseAdverseMediaResponse(mapper, personalInfoSearch, finalResponse, resultAdverseMedia);
						parseAPIResponseService.parseAdverseMediaResponse(mapper, personalInfoSearch, finalResponse, resultAdverseMedia);
						LocalDateTime endTime = LocalDateTime.now();
						Duration duration = Duration.between(startTime, endTime);
						resultAdverseMedia.put("Start Time",startTime.toString());
						resultAdverseMedia.put("End Time",endTime.toString());
						resultAdverseMedia.put("Time Taken",duration.getSeconds());
						finalResultNode.set("AdverseMedia", resultAdverseMedia);
						finalResponse=finalResultNode.toString();	
					}else if(apiName.equalsIgnoreCase("loan")){
						LocalDateTime startTime = LocalDateTime.now();
						//onlineAPILoanController.parseLoanDefaulterResponse(mapper, personalInfoSearch, finalResponse, resultLoan);
						parseAPIResponseService.parseLoanDefaulterResponse(mapper, personalInfoSearch, finalResponse, resultLoan);
						LocalDateTime endTime = LocalDateTime.now();
						Duration duration = Duration.between(startTime, endTime);
						resultLoan.put("Start Time",startTime.toString());
						resultLoan.put("End Time",endTime.toString());
						resultLoan.put("Time Taken",duration.getSeconds());
						finalResultNode.set("loanDefaulter", resultLoan);
						finalResponse=finalResultNode.toString();
					}else {
						finalResultNode.put("Result", "Please provide appropriate api name:- mca,loan or adversemedia");
						finalResponse=finalResultNode.toString();
					}
					
				} catch (Exception e) {
					logger.error("Error in Calling API of credit_reputational_cibil" + e.getMessage());
				}
				logger.info("Personal Rest Response credit_reputational_cibil" + finalResponse);
			}else {
				finalResultNode.put("Result", "Json is not appropriate. It must have verify_id");
				finalResponse=finalResultNode.toString();
			}
			if (asyncStatus)
			{
				callback(finalResponse);
			}
			return finalResponse;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			if (asyncStatus)
				callback(e.getMessage());
			return e.getMessage();
		}
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
