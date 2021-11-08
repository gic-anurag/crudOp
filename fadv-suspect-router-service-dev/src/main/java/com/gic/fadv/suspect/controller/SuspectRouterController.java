package com.gic.fadv.suspect.controller;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
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
import com.gic.fadv.suspect.model.SuspectReq;
import com.gic.fadv.suspect.model.Component;
import com.gic.fadv.suspect.model.ComponentScoping;
import com.gic.fadv.suspect.pojo.EmploymentSuspectListPOJO;
import com.gic.fadv.suspect.pojo.UniversitySuspectListPOJO;
import com.gic.fadv.suspect.service.SuspectApiService;
import com.gic.fadv.suspect.service.SuspectService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

//@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/india/")
@Api(value = "Cross Reference", description = "Operations pertaining to Employment Suspect List (Cross Reference master data)")
public class SuspectRouterController {

	@Autowired
	private Environment env;
	
	
	@Autowired
	SuspectApiService suspectApiService;
	
	@Autowired
	private SuspectService suspectService;

	private static final Logger logger = LoggerFactory.getLogger(SuspectRouterController.class);

	@PostMapping(path = "/async/suspectrouter", consumes = "application/json", produces = "application/json")
	public DeferredResult<ResponseEntity<String>> doAsyncProcess(@RequestBody String inStr) {
		DeferredResult<ResponseEntity<String>> ret = new DeferredResult<>();
		ForkJoinPool.commonPool().submit(() -> {
			logger.info(Marker.ANY_MARKER,"Got async Request: {}", inStr);
			processRequest(inStr, true);
			ret.setResult(ResponseEntity.ok("ok"));
		});
		ret.onCompletion(() -> logger.info("async process request done"));
		return ret;
	}

	@ApiOperation(value = "This service is used to process Records at Suspect router and return the result to workflow router ", response = List.class)
	@PostMapping(path = "/suspectrouter", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> doProcess(@RequestBody String inStr) {
		try {
			logger.info(Marker.ANY_MARKER,"Got Request:", inStr);
			String response = processRequest(inStr, false);
			logger.info("Processed Request");
			logger.info(Marker.ANY_MARKER,"Value of Suspect Response",response);
			return new ResponseEntity<>(response, HttpStatus.OK);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	
	@ApiOperation(value = "This service is used to process Records at Suspect router and return the result to workflow router ", response = List.class)
	@PostMapping(path = "/suspectworkflowrouter", consumes = "application/json", produces = "application/json")
	public ObjectNode doRecordProcess(@RequestBody JsonNode requestBody) {
		logger.info("Got Request :\n {}", requestBody);
		ObjectNode response = suspectService.processRequestBody(requestBody);
		logger.info("Record request Processed");
		return response;
	}
	
	
	

	private String processRequest(String inStr, boolean asyncStatus) {
		try {
			logger.info(Marker.ANY_MARKER,"Inside the processRequest of Suspect Router");
			// Creating the ObjectMapper object
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			/*Used to assigned success message for each result*/
			
			// Converting the JSONString to Object
			logger.info(Marker.ANY_MARKER,"Request String of Suspect Router",inStr);
			
			SuspectReq suspectReq = mapper.readValue(inStr, SuspectReq.class);
			logger.info(Marker.ANY_MARKER,"Mapped to SuspectReq Class");
			for (int i = 0; i < suspectReq.getData().size(); i++) {
				List<ComponentScoping> componentScoping = suspectReq.getData().get(i).getTaskSpecs().getComponentScoping();
				for (int j = 0; j < componentScoping.size(); j++) {
					List<Component> components = componentScoping.get(j).getComponents();
					for (int k = 0; k < components.size(); k++) {
						List<JsonNode> records = components.get(k).getRecords();
						if (records.size() > 0) {
							for (int l = 0; l < records.size(); l++) {
								ArrayNode resultArray=mapper.createArrayNode();
								if(records.get(l).get("ruleResult").isArray()) {
									resultArray=(ArrayNode)records.get(l).get("ruleResult").get(0).get("result");
								}else {
									resultArray=(ArrayNode)records.get(l).get("ruleResult").get("result");
								}
								if (resultArray.get(0).toString().contains("Include")) {
									
									logger.info(Marker.ANY_MARKER,"Send to Suspect(Internal & External) Process/Service");
									String result="";

									if (components.get(k).getComponentname().equals("Education")) {
							
										logger.info(Marker.ANY_MARKER,"value of College/Centre Name Aka Name"+records.get(l).get("College/Centre Name Aka Name"));
										
										String suspectEducationSearchString="";
										JsonNode collegeAkaName=records.get(l).get("College/Centre Name Aka Name");
										if(collegeAkaName != null && !collegeAkaName.isNull() && collegeAkaName.asText().trim().length()>1 ) {
											suspectEducationSearchString = "{\"universityAkaName\":"
													+ records.get(l).get("College/Centre Name Aka Name") + ",\"universityName\":"
													+ records.get(l).get("University Name") + "}";
										}else {
											suspectEducationSearchString = "{\"universityAkaName\":"
													+ records.get(l).get("University Aka Name") + ",\"universityName\":"
													+ records.get(l).get("University Name") + "}";
										}
										
										logger.info(Marker.ANY_MARKER,"Value of Suspect Search String",suspectEducationSearchString);
										String suspectResponse = suspectApiService
												.sendDataToUniSuspectRest(suspectEducationSearchString);
										logger.info(Marker.ANY_MARKER,"Suspect Response", suspectResponse);
										
										List<UniversitySuspectListPOJO> universitySuspectList = mapper.readValue(suspectResponse, new TypeReference<List<UniversitySuspectListPOJO>>(){});
										suspectResponse = mapper.writeValueAsString(universitySuspectList);
										if (suspectResponse != null && !suspectResponse.equals("[]")) {
											logger.info(Marker.ANY_MARKER,"Suspect List Value" , suspectResponse);
											logger.info(Marker.ANY_MARKER,"Found Result In Suspect List");
											result= "{\"Suspect Result\":\"Suspect records found\"}";
										} else {
											logger.info(Marker.ANY_MARKER,"Suspect List Value",suspectResponse);
											logger.info(Marker.ANY_MARKER,"Not Found Result In Suspect List");
											result= "{\"Suspect Result\":\"Suspect records not found\"}";
										}
										logger.info(Marker.ANY_MARKER,"Making Result for Component Education");
										JsonNode fieldJsonNode = mapper.readTree(suspectResponse);
										JsonNode resultJsonNode= mapper.readTree(result);
										ArrayNode ruleResultJsonNode = (ArrayNode) records.get(l).get("ruleResult");
										ObjectNode suspectResult=mapper.createObjectNode();
										suspectResult.put("engine","suspectResult");
										suspectResult.put("success",true);
										suspectResult.put("message","SUCCEEDED");
										suspectResult.put("status",200);
										suspectResult.set("fields",fieldJsonNode);
										suspectResult.set("result",resultJsonNode);
										ruleResultJsonNode.add(suspectResult);

									} else if (components.get(k).getComponentname().equals("Employment")) {
										String suspectEmployeementSearchString="";
										JsonNode thirdAkaName=records.get(l).get("Third Party or Agency Name and Address");
										if(thirdAkaName!=null && !thirdAkaName.isNull() && thirdAkaName.asText().trim().length()>1) {
											suspectEmployeementSearchString = "{\"companyAkaName\":"
													+ records.get(l).get("Third Party or Agency Name and Address") + ",\"companyName\":"
													+  records.get(l).get("Company Name") + "}";
										}else {
											suspectEmployeementSearchString = "{\"companyAkaName\":"
													+ records.get(l).get("Company Aka Name") + ",\"companyName\":"
													+  records.get(l).get("Company Name") + "}";
										}
										logger.info(Marker.ANY_MARKER,"Value of Suspect Search String",suspectEmployeementSearchString);
										String suspectResponse = suspectApiService
												.sendDataToSuspectRest(suspectEmployeementSearchString);
										
										logger.info(Marker.ANY_MARKER,"Suspect Response",suspectResponse);
										List<EmploymentSuspectListPOJO> employmentSuspectList = mapper.readValue(suspectResponse, new TypeReference<List<EmploymentSuspectListPOJO>>(){});
										suspectResponse = mapper.writeValueAsString(employmentSuspectList);
										if (suspectResponse != null && !suspectResponse.equals("[]")) {
											logger.info(Marker.ANY_MARKER,"Suspect List Value",suspectResponse);
											logger.info(Marker.ANY_MARKER,"Found Result In Suspect List");
											result= "{\"Suspect Result\":\"Suspect records found\"}";
										} else {
											logger.info(Marker.ANY_MARKER,"Suspect List Value" , suspectResponse);
											logger.info(Marker.ANY_MARKER,"Not Found Result In Suspect List");
											result= "{\"Suspect Result\":\"Suspect records not found\"}";
										}
										logger.info(Marker.ANY_MARKER,"Making Result for Component Employment");
										JsonNode fieldJsonNode = mapper.readTree(suspectResponse);
										JsonNode resultJsonNode= mapper.readTree(result);

											ArrayNode ruleResultJsonNode = (ArrayNode) records.get(l).get("ruleResult");
											//JsonNode resultJsonNode=mapper.readTree("[]");
											//JsonNode fieldJsonNode=mapper.readTree("{}");
											ObjectNode suspectResult=mapper.createObjectNode();
											suspectResult.put("engine","suspectResult");
											suspectResult.put("success",true);
											suspectResult.put("message","SUCCEEDED");
											suspectResult.put("status",200);
											suspectResult.set("fields",fieldJsonNode);
											suspectResult.set("result",resultJsonNode);
											ruleResultJsonNode.add(suspectResult);
										}
								} else {
									logger.info("Include not found");
								}
							}
						}
					}
				}
			}

			/* Out side the first For loop */
			String suspectResponse = mapper.writeValueAsString(suspectReq);
			String returnStr=suspectResponse;
			
			logger.debug(Marker.ANY_MARKER,"Response:\n",returnStr);
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

			logger.debug(Marker.ANY_MARKER,"postStr\n", postStr);
			URL url =new URL(env.getProperty("suspect.router.callback.url"));
			
			logger.debug(Marker.ANY_MARKER,"Using callback URL\n", url);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			
			// Creating the ObjectMapper object
			ObjectMapper mapper = new ObjectMapper();
			// Converting the JSONString to Object
			SuspectReq suspectReq = mapper.readValue(postStr, SuspectReq.class);
			String authToken = suspectReq.getMetadata().getRequestAuthToken();
			logger.debug(Marker.ANY_MARKER,"Auth Token\n", authToken);
			con.setRequestProperty("tokenId", authToken);

			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			os.write(postStr.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();
			logger.debug(Marker.ANY_MARKER,"Callback POST Response Code: ", responseCode ," : " , con.getResponseMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
