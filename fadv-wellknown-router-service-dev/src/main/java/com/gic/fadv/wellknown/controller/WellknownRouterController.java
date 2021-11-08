package com.gic.fadv.wellknown.controller;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ForkJoinPool;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.wellknown.exception.ResourceNotFoundException;
import com.gic.fadv.wellknown.model.Component;
import com.gic.fadv.wellknown.model.ComponentScoping;
import com.gic.fadv.wellknown.model.WellknownReq;
import com.gic.fadv.wellknown.pojo.EmploymentWellknownListPOJO;
import com.gic.fadv.wellknown.service.WellknownApiService;
import com.gic.fadv.wellknown.service.WellknownService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

//@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/india")
@Api(value = "Cross Reference", description = "Operations pertaining to Employment WellKnown List (Cross Reference master data)")
public class WellknownRouterController {

	@Autowired
	WellknownApiService wellknownApiService;
	
	@Autowired 
	WellknownService wellknwonService;
	
	@Autowired
	private Environment env;

	private static final Logger logger = LoggerFactory.getLogger(WellknownRouterController.class);

	@PostMapping(path = "/async/wellknownrouter", consumes = "application/json", produces = "application/json")
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

	@ApiOperation(value = "This service is used to process Records at Suspect router and return the result to workflow router ", response = List.class)
	@PostMapping(path = "/wellknownrouter", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> doProcess(@RequestBody String inStr) {
		try {
			logger.info("Got Request:\n" + inStr);
			String response = processRequest(inStr, false);
			logger.info("Processed Request");
			logger.info("Value of Suspect Response"+response);
			return new ResponseEntity<>(response, HttpStatus.OK);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	@ApiOperation(value = "This service is used to process Records at Suspect router and return the result to workflow router ", response = List.class)
	@PostMapping(path = "/wellknownworkflowrouter", consumes = "application/json", produces = "application/json")
	public ObjectNode doRecordProcess(@RequestBody JsonNode requestBody) {
		logger.info("Got Request :\n {}", requestBody);
		ObjectNode response = wellknwonService.processRequestBody(requestBody);
		logger.info("Record request Processed");
		return response;
	}

	private String processRequest(String inStr, boolean asyncStatus) {
		try {
			// Creating the ObjectMapper object
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			// Converting the JSONString to Object
			WellknownReq wellknownReq = mapper.readValue(inStr, WellknownReq.class);

			for (int i = 0; i < wellknownReq.getData().size(); i++) {
				List<ComponentScoping> componentScoping = wellknownReq.getData().get(i).getTaskSpecs().getComponentScoping();
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
									logger.info("Send to Wellknown(Internal & External) Process/Service");
									String result="";
									if (components.get(k).getComponentname().equals("Employment")) {
										String wellknownEmployeementSearchString = "{\"companyAkaName\":"
												+ records.get(l).get("Company Aka Name") + ",\"companyName\":"
												+  records.get(l).get("Company Name") + "}";
										logger.info("Value of WellKnown Search String" + wellknownEmployeementSearchString);
										String wellknownResponse = wellknownApiService
												.sendDataToWellknownRest(wellknownEmployeementSearchString);
										logger.info("WellKnown Response" + wellknownResponse);
										List<EmploymentWellknownListPOJO> employmentWellknownList = mapper.readValue(wellknownResponse, new TypeReference<List<EmploymentWellknownListPOJO>>(){});
										wellknownResponse = mapper.writeValueAsString(employmentWellknownList);
										if (wellknownResponse != null && !wellknownResponse.equals("[]")) {
											logger.info("WellKnown List Value" + wellknownResponse);
											logger.info("Found Result In Well Known List");
											result= "{\"Well Known Result\":\"WellKnown records found\"}";
										} else {
											logger.info("WellKnown List Value" + wellknownResponse);
											logger.info("Not Found Result In WellKnown List");
											result= "{\"Well known Result\":\"Well known records not found\"}";
										}
										JsonNode fieldJsonNode = mapper.readTree(wellknownResponse);
										JsonNode resultJsonNode= mapper.readTree(result);
										
										ArrayNode ruleResultJsonNode = (ArrayNode) records.get(l).get("ruleResult");
										ObjectNode wellknownResult=mapper.createObjectNode();
										wellknownResult.put("engine","wellknownResult");
										wellknownResult.put("success",true);
										wellknownResult.put("message","SUCCEEDED");
										wellknownResult.put("status",200);
										wellknownResult.set("fields",fieldJsonNode);
										wellknownResult.set("result",resultJsonNode);
										ruleResultJsonNode.add(wellknownResult);
										
									} else {
										ArrayNode ruleResultJsonNode = (ArrayNode) records.get(l).get("ruleResult");
										JsonNode resultJsonNode=mapper.readTree("[]");
										JsonNode fieldJsonNode=mapper.readTree("{}");
										ObjectNode wellknownResult=mapper.createObjectNode();
										wellknownResult.put("engine","wellknownResult");
										wellknownResult.put("success",true);
										wellknownResult.put("message","SUCCEEDED");
										wellknownResult.put("status",200);
										wellknownResult.set("fields",fieldJsonNode);
										wellknownResult.set("result",resultJsonNode);
										ruleResultJsonNode.add(wellknownResult);// Sets the Jackson node on the ruleResult
										
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

			/* Convert Information to String */
			String wellknownResponse = mapper.writeValueAsString(wellknownReq);
			String returnStr=wellknownResponse;
			
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
			URL url = new URL(env.getProperty("wellknown.router.callback.url"));
			
			logger.debug("Using callback URL\n" + url);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			
			// Creating the ObjectMapper object
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			// Converting the JSONString to Object
			WellknownReq wellknownReq = mapper.readValue(postStr, WellknownReq.class);
			String authToken=wellknownReq.getMetadata().getRequestAuthToken();
			/*String authToken = JsonParser.parseString(postStr).getAsJsonObject().get("metadata").getAsJsonObject()
					.get("requestAuthToken").getAsString();*/
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
