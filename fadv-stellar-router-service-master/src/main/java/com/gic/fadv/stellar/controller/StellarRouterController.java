package com.gic.fadv.stellar.controller;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.stellar.model.Component;
import com.gic.fadv.stellar.model.ComponentScoping;
import com.gic.fadv.stellar.model.StellarReq;
import com.gic.fadv.stellar.model.StellarRes;
import com.gic.fadv.stellar.pojo.StellarPOJO;
import com.gic.fadv.stellar.service.StellarApiService;
import com.google.gson.JsonParser;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

//@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/api/india")
@Api(value = "Cross Reference", description = "Operations pertaining to Stellar (Cross Reference master data)")
public class StellarRouterController {

	@Autowired
	private StellarApiService stellarApiService;

	@Autowired
	private Environment env;

	private static final Logger logger = LoggerFactory.getLogger(StellarRouterController.class);

	@PostMapping(path = "/async/stellarrouter", consumes = "application/json", produces = "application/json")
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

	@ApiOperation(value = "This service is used to process Records at stellar router and return the result to workflow router ", response = List.class)
	@PostMapping(path = "/stellarrouter", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> doProcess(@RequestBody String inStr) {

		try {
			logger.info("Got Request:\n" + inStr);
			String response = processRequest(inStr, false);
			logger.info("Processed Request");
			logger.info("Value of Stellar Response" + response);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}

	private String processRequest(String inStr, boolean asyncStatus) {
		LocalDateTime startTime = LocalDateTime.now();
		try {
			// Creating the ObjectMapper object
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			// Converting the JSONString to Object
			StellarReq stellarReq = mapper.readValue(inStr, StellarReq.class);
			JsonNode dataObj = null;
			String stellarResponse = "";
			// System.out.println(inStr);
			for (int i = 0; i < stellarReq.getData().size(); i++) {
				List<ComponentScoping> componentScoping = stellarReq.getData().get(i).getTaskSpecs()
						.getComponentScoping();
				for (int j = 0; j < componentScoping.size(); j++) {
					List<Component> components = componentScoping.get(j).getComponents();
					for (int k = 0; k < components.size(); k++) {
						List<JsonNode> records = components.get(k).getRecords();
						if (records.size() > 0) {
							for (int l = 0; l < records.size(); l++) {
								ArrayNode resultArray = mapper.createArrayNode();
								if (records.get(l).get("ruleResult").isArray()) {
									resultArray = (ArrayNode) records.get(l).get("ruleResult").get(0).get("result");
								} else {
									resultArray = (ArrayNode) records.get(l).get("ruleResult").get("result");
								}

								if (resultArray.get(0).toString().contains("Include")) {
									logger.info("Make an empty Json Array");
									ArrayNode ruleResultJsonNode = mapper.createArrayNode();
									logger.info("Write engine element as scopingEngine");
									((ObjectNode) records.get(l).get("ruleResult")).put("engine", "ScopingEngine");
									ruleResultJsonNode.add(records.get(l).get("ruleResult"));
									logger.info("Make ruleResult array");
									((ObjectNode) records.get(l)).set("ruleResult", ruleResultJsonNode);
									String result = "";
									/* Start Year and End Year is missing */
									System.out.println("value of College/Centre Name Aka Name" + records.get(l)
											.get("College/Centre Name Aka NameCollege/Centre Name Aka Name"));
									String stellarSearchString = "";
									JsonNode collegeAkaName = records.get(l).get("College/Centre Name Aka Name");
									if (collegeAkaName != null && !collegeAkaName.isNull()
											&& collegeAkaName.asText().trim().length() > 1) {
										stellarSearchString = "{\"akaName\":"
												+ records.get(l).get("College/Centre Name Aka Name")
												+ ",\"entityName\":" + records.get(l).get("University Name") + "}";
									} else {
										stellarSearchString = "{\"akaName\":"
												+ records.get(l).get("University Aka Name") + ", \"entityName\":"
												+ records.get(l).get("University Name") + "}";

									}
									logger.info("Stellar Search String" + stellarSearchString);
									// Calling Stellar Service
									stellarResponse = stellarApiService.sendDataToStellarRest(stellarSearchString);
									logger.info("Value of Education from Stellar Service" + stellarResponse);
									List<StellarPOJO> stellarList = mapper.readValue(stellarResponse,new TypeReference<List<StellarPOJO>>() {});
									List<StellarPOJO> stellarResultList = new ArrayList();
									StellarPOJO specificStellarResult =null;
									StellarPOJO allQualiStellarResult =null;
									StellarPOJO notAppliStellarresult=null;
									StellarPOJO allStellarResult=null;
									for(StellarPOJO stellar: stellarList) {
										String qualification = records.get(l).get("Qualification/course name").asText();
										String level = records.get(l).get("Qualification level").asText();
										String yearOfPassing = records.get(l).get("Year of Passing").asText();
										if(yearOfPassing.equals("") ) {
											yearOfPassing=stellar.getStartYear();
										}
										if(yearOfPassing.equalsIgnoreCase("All")) {
											yearOfPassing=stellar.getStartYear();
										}
										if(stellar.getStartYear().equalsIgnoreCase("All")) {
											stellar.setStartYear("1900");
										}
										if(stellar.getEndYear().equalsIgnoreCase("All")) {
											stellar.setEndYear("3000");
										}
										if(stellar.getQualification().equalsIgnoreCase(qualification) && 
											stellar.getLevel().equalsIgnoreCase(level) && 
											(Integer.parseInt(stellar.getStartYear())<= Integer.parseInt(yearOfPassing) &&
											Integer.parseInt(stellar.getEndYear())>= Integer.parseInt(yearOfPassing))
											) {
												specificStellarResult =stellar;
										}else if(stellar.getQualification().equalsIgnoreCase("All") &&
												stellar.getLevel().equalsIgnoreCase(level) && 
												(Integer.parseInt(stellar.getStartYear())<= Integer.parseInt(yearOfPassing) &&
												Integer.parseInt(stellar.getEndYear())>= Integer.parseInt(yearOfPassing))
												) {
													allQualiStellarResult=stellar;
											}else if(stellar.getQualification().equalsIgnoreCase("All") &&
												stellar.getLevel().equalsIgnoreCase("Not Applicable") && 
												(Integer.parseInt(stellar.getStartYear())<= Integer.parseInt(yearOfPassing) &&
												Integer.parseInt(stellar.getEndYear())>= Integer.parseInt(yearOfPassing))
												) {
												notAppliStellarresult=stellar;
											}else if(stellar.getQualification().equalsIgnoreCase("All") &&
													stellar.getLevel().equalsIgnoreCase("Not Applicable") && 
													stellar.getStartYear().equalsIgnoreCase("All")&&
														stellar.getEndYear().equalsIgnoreCase("All")) {
												allStellarResult=stellar;
											}
										}
									
									StellarPOJO finalResult=null;
									if(specificStellarResult!=null) {
										finalResult=specificStellarResult;
									}else if(allQualiStellarResult!=null) {
										finalResult=allQualiStellarResult;
									}else if(notAppliStellarresult!=null) {
										finalResult=notAppliStellarresult;
									}else if(allStellarResult!=null) {
										finalResult=allStellarResult;
									}
									List<StellarPOJO> finalstellarResultList = new ArrayList();
									if(finalResult!=null) {
										finalstellarResultList.add(finalResult);
									}
									stellarResponse = mapper.writeValueAsString(finalstellarResultList);
									// Sets the Jackson node on the ruleResult
									logger.info("Calling the stellar router");

									if (stellarResponse != null && !stellarResponse.equals("[]")) {
										logger.info("Stellar found");
										result = "{\"Stellar Result\":\"Stellar records found\"}";

										JsonNode fieldJsonNode = mapper.readTree(stellarResponse);
										JsonNode resultJsonNode = mapper.readTree(result);
										ruleResultJsonNode = (ArrayNode) records.get(l).get("ruleResult");
										ObjectNode stellarResult = mapper.createObjectNode();
										stellarResult.put("engine", "stellar");
										stellarResult.put("success", true);
										stellarResult.put("message", "SUCCEEDED");
										stellarResult.put("status", 200);
										ruleResultJsonNode.add(stellarResult);
										ArrayNode modules = mapper.createArrayNode();
										ObjectNode instaAdvantage = mapper.createObjectNode();
										instaAdvantage.put("module", "instaAdvantage");
										instaAdvantage.put("success", true);
										instaAdvantage.put("message", "SUCCEEDED");
										instaAdvantage.put("status", 200);
										instaAdvantage.set("fields", fieldJsonNode);
										instaAdvantage.set("result", resultJsonNode);
										modules.add(instaAdvantage);
										// ruleResultJsonNode.add(modules);

										String instraMRLResultStr = stellarApiService.callInstraStellarMRLRouter(inStr);
										logger.info("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");
										logger.info(instraMRLResultStr);
										StellarRes instraMRLstellarReq = mapper.readValue(instraMRLResultStr,
												StellarRes.class);
										List<Component> resultComponentList = instraMRLstellarReq.getData().get(0)
												.getResult().getComponentScoping().getComponents();
										for (Component component : resultComponentList) {
											String componentName = component.getComponentname();
											if (componentName.equals(components.get(k).getComponentname())) {
												List<JsonNode> compRecord = component.getRecords();
												for (JsonNode record : compRecord) {
													if (!records.get(l).get("Aka Name").isNull()
															&& !records.get(l).get("Qualification/course name").isNull()
															&& !records.get(l).get("Qualification level").isNull()) {
														String akaNameInner = "", qualificationInner = "",
																qualificationLevelInner = "";
														if (record.get("Aka Name") != null)
															akaNameInner = record.get("Aka Name").asText();
														if (record.get("Qualification/course name") != null)
															qualificationInner = record.get("Qualification/course name")
																	.asText();
														if (record.get("Qualification level") != null)
															qualificationLevelInner = record.get("Qualification level")
																	.asText();
														if (records.get(l).get("Aka Name").asText().equals(akaNameInner)
																&& records.get(l).get("Qualification/course name")
																		.asText().equals(qualificationInner)
																&& records.get(l).get("Qualification level").asText()
																		.equals(qualificationLevelInner)) {
															ArrayNode ruleResultJsonNode1 = (ArrayNode) record
																	.get("ruleResult").get("result");
															// JsonNode ruleResultJsonNode1=mapper.readTree(result);
															// JsonNode ruleResultJsonNode1 =
															// record.get("ruleResult").get("result").get(0);
															// Sets the Jackson node on the ruleResult
															ObjectNode instaMRLResult = mapper.createObjectNode();
															instaMRLResult.put("module", "instaMRL");
															fieldJsonNode = mapper.readTree("[]");
															instaMRLResult.put("success", true);
															instaMRLResult.put("message", "SUCCEEDED");
															instaMRLResult.put("status", 200);
															instaMRLResult.set("fields", fieldJsonNode);
															instaMRLResult.set("result", ruleResultJsonNode1);
															modules.add(instaMRLResult);

															((ObjectNode) components.get(k).getRecords().get(l))
																	.put("miRemarks", record.get("miRemarks").asText());
															((ObjectNode) components.get(k).getRecords().get(l))
																	.put("MI", record.get("MI").asText());
														}
													}
												}
											}
										}
										((ObjectNode) records.get(l).get("ruleResult").get(1)).set("modules", modules);
										stellarResponse = mapper.writeValueAsString(stellarReq);
									} else {
										result = "{\"result\":\"Stellar Record not Found\"}";
										JsonNode fieldJsonNode = mapper.readTree(stellarResponse);
										JsonNode resultJsonNode = mapper.readTree(result);
										ruleResultJsonNode = (ArrayNode) records.get(l).get("ruleResult");
										ObjectNode stellarResult = mapper.createObjectNode();
										stellarResult.put("engine", "stellar");
										stellarResult.put("success", true);
										stellarResult.put("message", "SUCCEEDED");
										stellarResult.put("status", 200);
										ruleResultJsonNode.add(stellarResult);
										ArrayNode modules = mapper.createArrayNode();

										ObjectNode instaAdvantage = mapper.createObjectNode();
										instaAdvantage.put("module", "instaAdvantage");
										instaAdvantage.put("success", true);
										instaAdvantage.put("message", "SUCCEEDED");
										instaAdvantage.put("status", 200);
										instaAdvantage.set("fields", fieldJsonNode);
										instaAdvantage.set("result", resultJsonNode);
										modules.add(instaAdvantage);
										// ruleResultJsonNode.add(modules);

										ObjectNode instaMRLResult = mapper.createObjectNode();
										instaMRLResult.put("module", "instaMRL");
										fieldJsonNode = mapper.readTree("[]");
										JsonNode ruleResultJsonNode1 = mapper.readTree("[]");
										instaMRLResult.put("success", true);
										instaMRLResult.put("message", "SUCCEEDED");
										instaMRLResult.put("status", 200);
										instaMRLResult.set("fields", fieldJsonNode);
										instaMRLResult.set("result", ruleResultJsonNode1);
										modules.add(instaMRLResult);
										((ObjectNode) records.get(l).get("ruleResult").get(1)).set("modules", modules);

									}
								} else {
									logger.info("Include not found");
								}
							}
						}
					}
				}
				// JsonObject = new JsonObject();
				stellarResponse = mapper.writeValueAsString(stellarReq);
				dataObj = mapper.readTree(stellarResponse);
				ObjectNode metricsObj = mapper.createObjectNode();

				LocalDateTime endTime = LocalDateTime.now();
				metricsObj.put("startTime", startTime.toString());
				metricsObj.put("endTime", endTime.toString());
				metricsObj.put("timeInMillis", "0");
				metricsObj.put("timeInSeconds", "0");
				metricsObj.put("statusCode", "OK");
				((ObjectNode) dataObj.get("data").get(0)).set("metrics", metricsObj);

				ObjectNode logs = mapper.createObjectNode();
				logs.put("field1", "No LOG");
				((ObjectNode) dataObj.get("data").get(0)).set("logs", logs);

			}
			// Adding Status in Meta Data Also
			ObjectNode statusObj = mapper.createObjectNode();
			statusObj.put("success", true);
			statusObj.put("message", "Executon done successfullly");
			statusObj.put("statusCode", "200");
			((ObjectNode) dataObj.get("metadata")).set("status", statusObj);

			/* Out side the first For loop */
			stellarResponse = mapper.writeValueAsString(dataObj);
			String returnStr = null;
			returnStr = stellarResponse.replace("taskSpecs", "result");
			logger.debug("Response:\n" + returnStr);
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

	private void callback(String postStr) {
		try {

			logger.debug("postStr\n" + postStr);
			URL url = new URL(env.getProperty("stellar.router.callback.url"));

			logger.debug("Using callback URL\n" + url);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");

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
