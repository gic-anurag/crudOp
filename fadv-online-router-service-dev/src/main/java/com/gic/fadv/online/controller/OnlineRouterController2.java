package com.gic.fadv.online.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.online.model.OnlineReq;
import com.gic.fadv.online.model.OnlineVerificationChecks;
import com.gic.fadv.online.pojo.ApiServiceResultPOJO;
import com.gic.fadv.online.service.OnlineService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/india")
@Api(value = "Cross Reference", description = "Operations pertaining to Employment Suspect List (Cross Reference master data)")
public class OnlineRouterController2 {

	@Autowired
	private OnlineService onlineService;

	@Value("${data.entry.l3.url}")
	private String l3DataEntryURL;
	
	@Value("${verification.url.checkid.l3}")
	private String verificationStatusUrlL3;

	@Value("${verification.status.url.l3}")
	private String verificationStatusL3Url;
	
	private static final Logger logger = LoggerFactory.getLogger(OnlineRouterController2.class);

	@ApiOperation(value = "This service is used to process Records at CBV-UTV-I4V router and return the result to workflow router ", response = List.class)
	@PostMapping(path = "/async/onlinerouter", consumes = "application/json", produces = "application/json")
	public DeferredResult<ResponseEntity<String>> doAsyncProcess(@RequestBody String inStr) {
		DeferredResult<ResponseEntity<String>> ret = new DeferredResult<>();
		ForkJoinPool.commonPool().submit(() -> {
			logger.info("Got async Request:\n {}", inStr);
			processRequest(inStr, true);
			ret.setResult(ResponseEntity.ok("ok"));
		});
		ret.onCompletion(() -> logger.info("async process request done"));
		return ret;
	}


	@ApiOperation(value = "This service is used to process Records at CBV-UTV-I4V router and return the result to workflow router ", response = List.class)
	@PostMapping(path = "/onlinerouter", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> doProcess(@RequestBody String inStr) {
		try {
			logger.info("Got Request:\n {}", inStr);
			String response = processRequest(inStr, false);
			logger.info("Processed Request");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	@PostMapping(path = "/test-scheduler", consumes = "application/json", produces = "application/json")
	public void testProcess(@RequestBody List<OnlineVerificationChecks> onlineVerificationChecksList) {
		
		// Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		List<String> serviceNames = onlineVerificationChecksList.stream().map(OnlineVerificationChecks::getApiName).collect(Collectors.toList());
		serviceNames = new ArrayList<>(new HashSet<>(serviceNames));
		List<String> checkIdList = new ArrayList<>();
		
		for (String serviceName : serviceNames) {
			OnlineVerificationChecks onlineVerificationCheck = onlineVerificationChecksList.stream()
					.filter(p -> StringUtils.equalsIgnoreCase(p.getApiName(), serviceName)).collect(Collectors.toList()).get(0);
			try {
				checkIdList = onlineService.processScheduledApiService(mapper, onlineVerificationCheck, checkIdList);
			} catch (JsonProcessingException e) {
				logger.error("Exceptiton : {}", e.getMessage());
				e.printStackTrace();
			}
		}
	}
	

	private String processRequest(String inStr, boolean asyncStatus) {
		LocalDateTime.now();
		try {
			// Creating the ObjectMapper object
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			// Converting the JSONString to Object
			OnlineReq onlineReq = mapper.readValue(inStr, OnlineReq.class);
			String onlineResponse = "";
			
			Map<String, String> resultMap = onlineService.getDetailsFromDataEntry(onlineReq);
			logger.info("data entry result : {}", resultMap);
			
			ApiServiceResultPOJO apiServiceResultPOJO = onlineService.getApiServiceNames(mapper, onlineReq);
			
			apiServiceResultPOJO.setDataEntryMap(resultMap);
			
			List<String> serviceNames = apiServiceResultPOJO.getServiceNameList();
			
			logger.info("api services : {}", serviceNames);
			
			for (String serviceName : serviceNames) {
				apiServiceResultPOJO = onlineService.processApiService(mapper, serviceName.trim(), resultMap, apiServiceResultPOJO);
			}
			
			JsonNode onlineReqNode = onlineService.writeRecordResultMap(mapper, onlineReq, apiServiceResultPOJO);
			
			// Adding Status in Meta Data Also
			ObjectNode statusObj = mapper.createObjectNode();
			statusObj.put("success", true);
			statusObj.put("message", "Executon done successfullly");
			statusObj.put("statusCode", "200");
			((ObjectNode) onlineReqNode.get("metadata")).set("status", statusObj);

			onlineResponse = mapper.writeValueAsString(onlineReqNode);
			logger.debug("Response:\n {}", onlineResponse);
			if (asyncStatus)
				onlineService.callBack(onlineResponse);
			return onlineResponse;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			if (asyncStatus)
				onlineService.callBack(e.getMessage());
			return e.getMessage();
		}
	}
	@PostMapping(path = "/rerun-api", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> rerunAPI(@RequestBody List<OnlineVerificationChecks> onlineVerificationChecksList) {
		
		// Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			onlineService.runParallelService(onlineVerificationChecksList);
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>("success", HttpStatus.OK);
	}
	
}