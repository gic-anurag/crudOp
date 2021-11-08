package com.gic.fadv.wellknown.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.wellknown.pojo.CaseSpecificRecordDetailPOJO;
import com.gic.fadv.wellknown.pojo.EmploymentWellknownListPOJO;

@Service
public class WellknownServiceImpl implements WellknownService {

	@Autowired
	private ApiService apiService;

	@Value("${wellknown.rest.url}") 
	private String wellknownRestUrl;
	
	private static final Logger logger = LoggerFactory.getLogger(WellknownServiceImpl.class);
	private static final String ENGINE = "engine";
	private static final String SUCCESS = "success";
	private static final String MESSAGE = "message";
	private static final String STATUS = "status";
	private static final String RESULT = "result";
	private static final String WELLKNOW_RESULT = "wellknownResult";
	private static final String SUCCEEDED = "SUCCEEDED";
	private static final String FAILED = "FAILED";
	private static final String RETURN_MESSAGE = "returnMessage";

	@Override
	public ObjectNode processRequestBody(JsonNode requestNode) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		Map<String, String> responseMap = new HashMap<>();

		if (!requestNode.isEmpty()) {
			JsonNode caseSpecificRecordDetailNode = requestNode.has("caseSpecificRecordDetail")
					? requestNode.get("caseSpecificRecordDetail")
					: mapper.createObjectNode();

			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO = mapper
					.convertValue(caseSpecificRecordDetailNode, CaseSpecificRecordDetailPOJO.class);

			responseMap = processRecords(mapper, caseSpecificRecordDetailPOJO);
		}
		return generateResponseStr(mapper, responseMap);
	}
	
	private Map<String, String> processRecords(ObjectMapper mapper, 
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO) {
			
		String componentName = caseSpecificRecordDetailPOJO.getComponentName() != null 
				? caseSpecificRecordDetailPOJO.getComponentName() : "";
		String recordStr = caseSpecificRecordDetailPOJO.getComponentRecordField();
		JsonNode recordNode = mapper.createObjectNode();
		try {
			recordNode = mapper.readTree(recordStr);
		} catch (JsonProcessingException e1) {
			logger.error("Exception while mapping record node : {}", e1.getMessage());
			recordNode = mapper.createObjectNode();
		}
		
		Map<String, String> returnMessage = new HashMap<>();
		
		if (StringUtils.equalsIgnoreCase(componentName, "Employment")) {
			processEmployment(mapper, recordNode, returnMessage);
		} else {
			returnMessage.put(RETURN_MESSAGE, "Component is not Employment");
		}
			
		return returnMessage;
	}

	private void processEmployment(ObjectMapper mapper, JsonNode recordNode, Map<String, String> returnMessage) {
		String companyName = recordNode.has("Company Name") ? recordNode.get("Company Name").asText() : "";
		String companyAkaName = recordNode.has("Company Aka Name") ? recordNode.get("Company Aka Name").asText() : "";
		
		String wellknownEmployeementSearchString = "{\"companyAkaName\":\""
				+ companyAkaName + "\",\"companyName\":\""
				+  companyName + "\"}";
		logger.info("Value of WellKnown Search String : {}", wellknownEmployeementSearchString);
		
		String wellknownResponse = apiService.sendDataToPost(wellknownRestUrl, wellknownEmployeementSearchString);
		
		logger.info("Value of Well known response : {}", wellknownResponse);

		List<EmploymentWellknownListPOJO> employmentWellknownList = new ArrayList<>();
		if (wellknownResponse != null && !StringUtils.isEmpty(wellknownResponse)) {
			try {
				employmentWellknownList = mapper.readValue(wellknownResponse, new TypeReference<List<EmploymentWellknownListPOJO>>() {
				});
			} catch (JsonProcessingException e) {
				logger.info("Error while mapping well known response : {}", e.getMessage());
			}
		}
		if (CollectionUtils.isNotEmpty(employmentWellknownList)) {
			returnMessage.put(RETURN_MESSAGE, "WellKnown records found");
		} else {
			returnMessage.put(RETURN_MESSAGE, "Well known records not found");
		}
	}

	private ObjectNode generateResponseStr(ObjectMapper mapper, Map<String, String> responseMap) {
		ObjectNode responseNode = mapper.createObjectNode();
		responseNode.put(ENGINE, WELLKNOW_RESULT);
		if (responseMap != null) {
			responseNode.put(SUCCESS, true);
			responseNode.put(MESSAGE, SUCCEEDED);
			responseNode.put(STATUS, 200);
			responseNode.put(RESULT, responseMap.get(RETURN_MESSAGE));
		} else {
			responseNode.put(SUCCESS, false);
			responseNode.put(MESSAGE, FAILED);
			responseNode.put(STATUS, 200);
			responseNode.put(RESULT, "Unable to process engine");
		}
		return responseNode;
	}
	
}
