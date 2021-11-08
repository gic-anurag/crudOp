package com.gic.fadv.online.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.online.pojo.CheckIdObjPOJO;

@Service
public class OnlinePreProcessServiceImpl implements OnlinePreProcessService {
	
	@Autowired
	private OnlineProcessApiService onlineProcessApiService;
	
	@Autowired
	private OnlineFinalService onlineFinalService;

	private static final Logger logger = LoggerFactory.getLogger(OnlinePreProcessServiceImpl.class);
	private static final String ENGINE = "engine";
	private static final String SUCCESS = "success";
	private static final String MESSAGE = "message";
	private static final String STATUS = "status";
	private static final String RESULT = "result";
	private static final String ONLINE_RESULT = "onlineResult";
	private static final String SUCCEEDED = "SUCCEEDED";
	private static final String FAILED = "FAILED";
	private static final String CHECK_ID_OBJ_LIST = "checkIds";
	private static final String RESPONSE = "response";
	private static final String DATA_ENTRY = "dataEntry";
	private static final String WORLD_CHECK = "Worldcheck";
	private static final String WATCHOUT = "Watchout";
	private static final String MCA = "MCA";
	private static final String ADVERSE_MEDIA = "Adverse Media";
	private static final String MANUPATRA = "Manupatra";
	private static final String LOAN_DEFAULTER = "Loan Defaulter";
	
	@Override
	public ObjectNode processRequestBody(JsonNode requestNode) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		ArrayNode responseNode = mapper.createArrayNode();
		if (!requestNode.isEmpty()) {
			JsonNode dataEntryNode = requestNode.has(DATA_ENTRY) ? requestNode.get(DATA_ENTRY)
					: mapper.createObjectNode();
			
			String checkIdNode = requestNode.has(CHECK_ID_OBJ_LIST)
					? requestNode.get(CHECK_ID_OBJ_LIST).toString()
					: "[]";
			
			List<CheckIdObjPOJO> checkIdObjPOJOs = new ArrayList<>();
			try {
				checkIdObjPOJOs = mapper.readValue(checkIdNode, new TypeReference<List<CheckIdObjPOJO>>() { });
			} catch (JsonProcessingException e) {
				logger.error("Exception occured while mapping checkid list : {}", e.getMessage());
				e.printStackTrace();
			}

			if (CollectionUtils.isNotEmpty(checkIdObjPOJOs)) {
				responseNode = processCheckIdObjPojo(checkIdObjPOJOs, dataEntryNode);
			}
		}
		return generateResponseStr(mapper, responseNode);
	}
	
	private ArrayNode processCheckIdObjPojo(List<CheckIdObjPOJO> checkIdObjPOJOs, JsonNode dataEntryNode) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		Map<String, String> serviceResponseMap = new HashMap<>();
		
		List<String> apiServiceNameList = checkIdObjPOJOs.stream().map(CheckIdObjPOJO::getApiName)
				.collect(Collectors.toList());
		apiServiceNameList = new ArrayList<>(new HashSet<>(apiServiceNameList));
		
		for (String apiName : apiServiceNameList) {
			onlineProcessApiService.processApiService(mapper, apiName.trim(), serviceResponseMap, dataEntryNode);
		}
		
		ArrayNode responseNodeArr = mapper.createArrayNode();
		try {
			for (CheckIdObjPOJO checkIdObjPOJO : checkIdObjPOJOs) {
				String apiResponse = serviceResponseMap.get(checkIdObjPOJO.getApiName()); 
				String checkId = checkIdObjPOJO.getCheckId();
				Long caseSpecificRecordID = checkIdObjPOJO.getCaseSpecificRecordId();
				String apiName = checkIdObjPOJO.getApiName();
				JsonNode apiResponseNode=mapper.createObjectNode();
				try {
					apiResponseNode = mapper.readValue(apiResponse, JsonNode.class);
				}catch(Exception e ) {
					logger.info("Exception occurred.Assign apiResponse to empty:{}",e.getMessage());
				}
								
				ObjectNode responseNode = processApiResponse(mapper, apiName, apiResponseNode);
				
				if (responseNode != null && !responseNode.isEmpty()) {
					responseNode.put("checkId", checkId);
					responseNode.put("caseSpecificRecordId", caseSpecificRecordID);
				}
				
				responseNodeArr.add(responseNode);
			}
		} catch (Exception e1) {
			logger.error("Exception occured while processing api response : {}", e1.getMessage());
			e1.printStackTrace();
		}
		return responseNodeArr;
	}

	private ObjectNode processApiResponse(ObjectMapper mapper, String apiName, JsonNode apiResponseNode) {
		ObjectNode responseNode = mapper.createObjectNode();
		if (StringUtils.equalsIgnoreCase(apiName, MANUPATRA)) {
			responseNode = onlineFinalService.setManupatraResponse(mapper, apiResponseNode);
		} else if (StringUtils.equalsIgnoreCase(apiName, WORLD_CHECK)) {
			responseNode = onlineFinalService.setWorldCheckResponse(mapper, apiResponseNode);
		} else if (StringUtils.equalsIgnoreCase(apiName, WATCHOUT)) {
			responseNode = onlineFinalService.setWatchoutResponse(mapper, apiResponseNode);
		} else if (StringUtils.equalsIgnoreCase(apiName, MCA)) {
			responseNode = onlineFinalService.setMcaResponse(mapper, apiResponseNode);
		} else if (StringUtils.equalsIgnoreCase(apiName, LOAN_DEFAULTER)) {
			responseNode = onlineFinalService.setLoanDefaulterResponse(mapper, apiResponseNode);
		} else if (StringUtils.equalsIgnoreCase(apiName, ADVERSE_MEDIA)) {
			responseNode = onlineFinalService.setAdverseMediaResponse(mapper, apiResponseNode);
		}
		return responseNode;
	}
	
	private ObjectNode generateResponseStr(ObjectMapper mapper, ArrayNode apiResponseNode) {
		ObjectNode responseNode = mapper.createObjectNode();
		responseNode.put(ENGINE, ONLINE_RESULT);
		if (apiResponseNode != null && !apiResponseNode.isEmpty()) {
			responseNode.put(SUCCESS, true);
			responseNode.put(MESSAGE, SUCCEEDED);
			responseNode.put(STATUS, 200);
			responseNode.set(RESPONSE, apiResponseNode);
			responseNode.put(RESULT, "Processed");
		} else {
			responseNode.put(SUCCESS, false);
			responseNode.put(MESSAGE, FAILED);
			responseNode.put(STATUS, 200);
			responseNode.set(RESPONSE, apiResponseNode);
			responseNode.put(RESULT, "Unable to process engine");
		}
		return responseNode;
	}

}
