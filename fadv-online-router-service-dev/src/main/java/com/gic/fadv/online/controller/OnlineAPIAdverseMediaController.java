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
public class OnlineAPIAdverseMediaController {

	@Autowired
	private Environment env;
	@Autowired
	private OnlineApiService onlineApiService;
	@Autowired
	private ParseAPIResponseService parseAPIResponseService;
	
	private static final Logger logger = LoggerFactory.getLogger(OnlineAPIAdverseMediaController.class);

	@ApiOperation(value = "This service is used to process Records at Online Adverse Media API and return the result ", response = List.class)
	@PostMapping(path = "/async/onlineapi/adversemedia", consumes = "application/json", produces = "application/json")
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

	@ApiOperation(value = "This service is used to process Records at Online Adverse Media API and return the result ", response = List.class)
	@PostMapping(path = "/onlineapi/adversemedia", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> doProcess(@RequestBody String inStr) {
		try {
			logger.info("Got Request:\n {}" , inStr);
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
			
			String personalResponse = null;
			String verifyId=null;
			
			ObjectNode resultAdverseMedia = mapper.createObjectNode();
			ObjectNode finalResultNode = mapper.createObjectNode();
			ObjectNode personalSearchNode= mapper.createObjectNode();
			/*------------------Adverse Media Call ----------------------------------------*/
			/*
			 * Call adverse_media API
			 */
			personalInfoSearch.setServices("adverse_media");
			logger.info("Making Search String For adverse_media");
			personalSearchNode.put("name", personalInfoSearch.getName());
			personalSearchNode.put("dob", personalInfoSearch.getDob());
			personalSearchNode.put("services", personalInfoSearch.getServices());
			personalSearchNode.put("contexts", personalInfoSearch.getContexts());
//			logger.info("Personal Search String" + personalSearchNode.toString());
			LocalDateTime startTime = LocalDateTime.now();
			try {
				verifyId = onlineApiService.sendDataToServicePersonalRest(personalSearchNode.toString());
			} catch (Exception e) {
				logger.error("Error in Calling API of adverse_media" + e.getMessage());
			}
			logger.info("Parse String and Take out VerifyID"+verifyId);
			/*
			 * 2nd API Call
			 */
			ObjectNode verifyNode= mapper.createObjectNode();
			verifyNode.put("verify_id", verifyId);
//			logger.info("Second Seearch String.Personal Search String" + verifyNode.toString());
			try {
				personalResponse = onlineApiService.sendDataToFinalPersonalRest(verifyNode.toString());
				//personalResponse = onlineApiService.sendDataToPersonalVerifyRest(verifyNode.toString());
			} catch (Exception e) {
				logger.error("Error in Calling API of credit_reputational_cibil" + e.getMessage());
			}
			// Put Time out Code here
			//timeOut();
//			logger.info("Personal Rest Response adverse Media" + personalResponse);
			/*
			 * Parsing Result For Personal Info Search (adverse_media) and Add Adverse Media Result to Final Result
			 */
			logger.info("Assign Input to Adverse media");
			resultAdverseMedia.put("Adverse media Input", personalSearchNode.toString());
			//Put Extra Field for keeping verifyID
			resultAdverseMedia.put("Verify_id", verifyId);
			resultAdverseMedia.put("Raw Output", personalResponse);
			//parseAdverseMediaResponse(mapper, personalInfoSearch, personalResponse, resultAdverseMedia);
			parseAPIResponseService.parseAdverseMediaResponse(mapper, personalInfoSearch, personalResponse, resultAdverseMedia);
			LocalDateTime endTime = LocalDateTime.now();
			 Duration duration = Duration.between(startTime, endTime);
			  logger.info(duration.getSeconds() + " seconds");
			  resultAdverseMedia.put("Start Time",startTime.toString());
			  resultAdverseMedia.put("End Time",endTime.toString());
			  resultAdverseMedia.put("Time Taken",duration.getSeconds());
			finalResultNode.set("Adverse Media", resultAdverseMedia);
//			logger.info("Value of Final Result after adverse media" + finalResultNode);
			/*------------------Adverse Media Call End----------------------------------------*/
						
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

	public void parseAdverseMediaResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,
			String personalResponse, ObjectNode resultAdverseMedia)
			throws JsonProcessingException, JsonMappingException {
		
		boolean clear = false;
		boolean manual = false;

		String clearStr = "";
		String manualStr = "";
		ArrayNode links = mapper.createArrayNode();
		if (personalResponse == null) {
			//resultAdverseMedia.put("Adverse media Output", "Auto Tag Web & Media as clear");
			clear = true;
			clearStr = "Auto Tag Web & Media as clear";
//			logger.info("Auto Tag Web & Media as clear" + resultAdverseMedia);
		} else {
			logger.info("Auto Tag Web & Media Else Part");
			JsonNode resultResponse = (ObjectNode) mapper.readTree(personalResponse);
			ArrayNode results = (ArrayNode) resultResponse.get("results");
			ArrayNode data = (ArrayNode) results.get(0).get("data");
			if (data.size() > 0) {
				for(int i=0;i<data.size();i++) {
				//JsonNode nameNode = data.get(0).get("name");
				JsonNode nameNode = data.get(i).get("short_article");
				if (nameNode != null) {
					//String name = data.get(0).get("name").asText();
					String shortArticle = data.get(i).get("short_article").asText();
					//Split on the basis of ,
		
							//if (shortArticle.toLowerCase().contains(personalInfoSearch.getName().toLowerCase())) {
					       if (Utility.checkContains(shortArticle, personalInfoSearch.getName())) {
								/*if (data.get(i).get("address").asText().equalsIgnoreCase(personalInfoSearch.getAddress())) {
									logger.info(
											"(Primary Match) Send for Manual review under web and Media Provide copy of the"
													+ " hyperlink from FD : Link");
									resultAdverseMedia.put("Adverse media Output",
											"Send for Manual review under web and Media Provide copy of the"
													+ " hyperlink from FD : Link");
									resultAdverseMedia.put("Link", data.get(0).get("link").asText());
									resultAdverseMedia.put("Annexure", data.get(0).get("url").asText());
									resultAdverseMedia.put("Link", data.get(i).get("link").asText());
									resultAdverseMedia.put("Annexure", data.get(i).get("url").asText());
									break;
								} else {*/
									logger.info("(Secondary Match) Send for Manual review under Web & Media "
											+ " Provide copy of the hyperlink from FD : Link");
//									resultAdverseMedia.put("Adverse media Output", "Send for Manual review "
//											+ "under Web & Media  Provide copy of the hyperlink from FD :" + "Link");
									manual = true;
									manualStr = "Send for Manual review "
											+ "under Web & Media  Provide copy of the hyperlink from FD :" + "Link";
									//resultAdverseMedia.put("Link", data.get(0).get("link").asText());
									//resultAdverseMedia.put("Annexure", data.get(0).get("url").asText());
									
									if(data.get(i).get("link")!=null) {
										//resultAdverseMedia.put("Link", data.get(i).get("link").asText());
										links.add(data.get(i).get("link").asText());
									}
									/*
									 * if(data.get(i).get("url")!=null) { resultAdverseMedia.put("Annexure",
									 * data.get(i).get("url").asText()); }else {
									 * resultAdverseMedia.put("Annexure","Data for url is Empty/Null"); }
									 */
							} else {
								clear = true;
								clearStr = "Auto Tag Web & Media as clear ";
								//resultAdverseMedia.put("Adverse media Output", "Auto Tag Web & Media as clear ");
//								logger.info("Auto Tag Cross directorship as clear" + resultAdverseMedia);
							}
					

					/*
					 * if (name.equalsIgnoreCase(personalInfoSearch.getName())) { if
					 * (data.get(0).get("address").asText().equalsIgnoreCase(personalInfoSearch.
					 * getAddress())) { logger.info(
					 * "(Primary Match) Send for Manual review under web and Media Provide copy of the"
					 * + " hyperlink from FD : Link");
					 * resultAdverseMedia.put("Adverse media Output",
					 * "Send for Manual review under web and Media Provide copy of the" +
					 * " hyperlink from FD : Link"); resultAdverseMedia.put("Link",
					 * data.get(0).get("link").asText()); resultAdverseMedia.put("Annexure",
					 * data.get(0).get("url").asText()); } else {
					 * logger.info("(Secondary Match) Send for Manual review under Web & Media " +
					 * " Provide copy of the hyperlink from FD : Link");
					 * resultAdverseMedia.put("Adverse media Output", "Send for Manual review " +
					 * "under Web & Media  Provide copy of the hyperlink from FD :" + "Link");
					 * resultAdverseMedia.put("Link", data.get(0).get("link").asText());
					 * resultAdverseMedia.put("Annexure", data.get(0).get("url").asText()); } } else
					 * { resultAdverseMedia.put("Adverse media Output",
					 * "Auto Tag Web & Media as clear ");
					 * logger.info("Auto Tag Cross directorship as clear" + resultAdverseMedia); }
					 */
				} else {
					logger.info("Name is Null");
					clear = true;
					clearStr = "Auto Tag Web & Media as clear";
					//resultAdverseMedia.put("Adverse media Output", "Auto Tag Web & Media as clear");
//					logger.info("Auto Tag Cross directorship as clear" + resultAdverseMedia);
				}
			}
			} else {
				logger.info("data is Empty");
				clear = true;
				clearStr = "Auto Tag Web & Media. Data is Empty";
				resultAdverseMedia.put("Adverse Media Output", "Auto Tag Web & Media. Data is Empty");
			}
		}
		
		String finalResult = "";
		String finalStatus = "";
		 if(manual) {
			finalResult = manualStr;
			finalStatus = "Manual";
			resultAdverseMedia.set("Links", links);
		} else if(clear) {
			finalResult = clearStr;
			finalStatus = "Clear";
		}
				
		resultAdverseMedia.put("Adverse Media Output", finalResult);
		resultAdverseMedia.put("status", finalStatus);
		
	}

	private void callback(String postStr) {
		try {

//			logger.debug("postStr\n" + postStr);
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
