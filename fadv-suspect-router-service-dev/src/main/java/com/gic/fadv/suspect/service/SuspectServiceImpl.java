package com.gic.fadv.suspect.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.suspect.pojo.CaseSpecificRecordDetailPOJO;
import com.gic.fadv.suspect.pojo.EmploymentSuspectListPOJO;
import com.gic.fadv.suspect.pojo.UniversitySuspectListPOJO;

@Service
public class SuspectServiceImpl implements SuspectService {

	@Autowired
	private ApiService apiService;

	@Value("${suspect.rest.url}")
	private String suspectRestUrl;

	@Value("${unisuspect.rest.url}")
	private String unisuspectRestUrl;

	private static final Logger logger = LoggerFactory.getLogger(SuspectServiceImpl.class);
	private static final String ENGINE = "engine";
	private static final String SUCCESS = "success";
	private static final String MESSAGE = "message";
	private static final String STATUS = "status";
	private static final String RESULT = "result";
	private static final String SUSPECT_RESULT = "suspectResult";
	private static final String SUCCEEDED = "SUCCEEDED";
	private static final String FAILED = "FAILED";
	private static final String RETURN_MESSAGE = "returnMessage";

	@Override
	public ObjectNode processRequestBody(JsonNode requestNode) {
		logger.info("Suspect Process Request Body Start");
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
		logger.info("Suspect Process Request Body End");
		return generateResponseStr(mapper, responseMap);
	}

	private Map<String, String> processRecords(ObjectMapper mapper,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO) {

		String componentName = caseSpecificRecordDetailPOJO.getComponentName() != null
				? caseSpecificRecordDetailPOJO.getComponentName()
				: "";
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
		} else if (StringUtils.equalsIgnoreCase(componentName, "Education")) {
			processEducation(mapper, recordNode, returnMessage);
		} else {
			returnMessage.put(RETURN_MESSAGE, "Component is not Employment or Education");
		}

		return returnMessage;
	}

	private void processEmployment(ObjectMapper mapper, JsonNode recordNode, Map<String, String> returnMessage) {
		String companyName = recordNode.has("Company Name") ? recordNode.get("Company Name").asText() : "";
		String thirdAkaName = recordNode.has("Third Party or Agency Name and Address")
				? recordNode.get("Third Party or Agency Name and Address").asText()
				: "";

		if (thirdAkaName == null || StringUtils.isEmpty(thirdAkaName)) {
			thirdAkaName = recordNode.has("Company Aka Name") ? recordNode.get("Company Aka Name").asText() : "";
		}

		String suspectEmployementSearchString = "{\"companyAkaName\":\"" + thirdAkaName + "\",\"companyName\":\""
				+ companyName + "\"}";
		logger.info("Value of suspect Search String : {}", suspectEmployementSearchString);

		String suspectResponse = apiService.sendDataToPost(suspectRestUrl, suspectEmployementSearchString);

		logger.info("Value of suspect response : {}", suspectResponse);

		List<EmploymentSuspectListPOJO> employmentSuspectList = new ArrayList<>();
		if (suspectResponse != null && !StringUtils.isEmpty(suspectResponse)) {
			try {
				employmentSuspectList = mapper.readValue(suspectResponse,
						new TypeReference<List<EmploymentSuspectListPOJO>>() {
						});
			} catch (JsonProcessingException e) {
				logger.info("Error while mapping suspect response : {}", e.getMessage());
			}
		}
		if (CollectionUtils.isNotEmpty(employmentSuspectList)) {
			logger.info(Marker.ANY_MARKER, "Found Result In Suspect List");
			returnMessage.put(RETURN_MESSAGE, "Suspect records found");
		} else {
			logger.info(Marker.ANY_MARKER, "Not Found Result In Suspect List");
			returnMessage.put(RETURN_MESSAGE, "Suspect records not found");
		}
	}

	private void processEducation(ObjectMapper mapper, JsonNode recordNode, Map<String, String> returnMessage) {
		String collegeName = recordNode.has("University Name") ? recordNode.get("University Name").asText() : "";
		String collegeAkaName = recordNode.has("College/Centre Name Aka Name")
				? recordNode.get("College/Centre Name Aka Name").asText()
				: "";

		if (collegeAkaName == null || StringUtils.isEmpty(collegeAkaName)) {
			collegeAkaName = recordNode.has("University Aka Name") ? recordNode.get("University Aka Name").asText()
					: "";
		}

		String suspectEducationSearchString = "{\"universityAkaName\":\"" + collegeAkaName + "\",\"universityName\":\""
				+ collegeName + "\"}";

		logger.info("Value of suspect Search String : {}", suspectEducationSearchString);

		String suspectResponse = apiService.sendDataToPost(unisuspectRestUrl, suspectEducationSearchString);

		logger.info("Value of suspect response : {}", suspectResponse);

		List<UniversitySuspectListPOJO> universitySuspectList = new ArrayList<>();
		if (suspectResponse != null && !StringUtils.isEmpty(suspectResponse)) {
			try {
				universitySuspectList = mapper.readValue(suspectResponse,
						new TypeReference<List<UniversitySuspectListPOJO>>() {
						});
			} catch (JsonProcessingException e) {
				logger.info("Error while mapping suspect response : {}", e.getMessage());
			}
		}
		if (CollectionUtils.isNotEmpty(universitySuspectList)) {
			logger.info(Marker.ANY_MARKER, "Found Result In Suspect List");
			returnMessage.put(RETURN_MESSAGE, "Suspect records found");
		} else {
			logger.info(Marker.ANY_MARKER, "Not Found Result In Suspect List");
			returnMessage.put(RETURN_MESSAGE, "Suspect records not found");
		}
	}

	private ObjectNode generateResponseStr(ObjectMapper mapper, Map<String, String> responseMap) {
		ObjectNode responseNode = mapper.createObjectNode();
		responseNode.put(ENGINE, SUSPECT_RESULT);
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
