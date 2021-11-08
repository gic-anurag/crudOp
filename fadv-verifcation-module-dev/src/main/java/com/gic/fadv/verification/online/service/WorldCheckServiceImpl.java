package com.gic.fadv.verification.online.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.verification.attempts.model.AttemptHistory;
import com.gic.fadv.verification.attempts.repository.AttemptMasterRepository;
import com.gic.fadv.verification.online.model.CaseReference;
import com.gic.fadv.verification.online.model.FileUpload;
import com.gic.fadv.verification.online.repository.OnlineVerificationChecksRepository;
import com.gic.fadv.verification.pojo.L3CaseDetails;

@Service
public class WorldCheckServiceImpl implements WorldCheckService {

	private static final String SUCCESS = "success";
	private static final String STATUS = "status";
	private static final String MESSAGE = "message";
	private static final String RESULT_RECEIVED_WITH_NO_RECORDS = "Result received with No records";
	private static final String L3_VERIFICATION_JSON = "l3 verification json : {} ";
	private static final String L3_ERROR_RESPONSE = "{\"Error\": \"Error as reponse from L3\"}";
	private static final String VERIFY_CHECKS = "verifyChecks";
	private static final String DATABASE = "Database";
	private static final String RECORD_FOUND = "Record Found";
	private static final String CLEAR = "Clear";
	private static final String RESULT_FOUND = "Result Found";

	@Autowired
	private OnlineVerificationChecksRepository onlineVerificationChecksRepository;

	@Autowired
	private OnlineApiService onlineApiService;

	@Autowired
	private AttemptMasterRepository attemptMasterRepository;

	@Value("${multicheck.verification.url.l3}")
	private String multiCheckVerificationUrlL3;

	@Autowired
	private OnlineVerificationChecksService onlineVerificationChecksService;

	private static final Logger logger = LoggerFactory.getLogger(WorldCheckServiceImpl.class);

	@Override
	public Map<String, String> updateWorldCheckStatus(ArrayNode requestArrayNode) {
		Map<String, String> responseMap = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String apiName = "Worldcheck";
		try {
			logger.info("requestArrayNode: {}", requestArrayNode);
			for (JsonNode requestNode : requestArrayNode) {
				String crnNo = requestNode.has("crnNo") ? requestNode.get("crnNo").asText() : "";
				String worldCheckResult = requestNode.has("worldCheckResult")
						? requestNode.get("worldCheckResult").asText()
						: "";
				if (StringUtils.isEmpty(crnNo) || StringUtils.isEmpty(worldCheckResult)) {
					responseMap.put(MESSAGE, "Excel Column field name is invalid");
					responseMap.put(STATUS, "Fail");
					return responseMap;
				}

				List<String> checkIdList = onlineVerificationChecksRepository.getCheckIdList(crnNo, apiName);
				logger.info("{}", checkIdList);

//				updateWorldCheckDb(mapper, apiName, crnNo, worldCheckResult, checkIdList);
				onlineVerificationChecksRepository.updateVerifyChecks(crnNo, worldCheckResult, apiName);
				onlineVerificationChecksService.processL3VerifyCheckIds(mapper, checkIdList);
			}
			responseMap.put(MESSAGE, "Record Updated successfully");
			responseMap.put(STATUS, SUCCESS);
			return responseMap;
		} catch (Exception e) {
			logger.error("Exception : {}", e.getMessage());
			e.printStackTrace();
			responseMap.put(MESSAGE, "Update Fail");
			responseMap.put(STATUS, "Fail");
			return responseMap;
		}
	}
	private void updateWorldCheckDb(ObjectMapper mapper, String apiName, String crnNo, String worldCheckResult,
			List<String> checkIdList)
			throws JsonProcessingException {
		if(StringUtils.equalsIgnoreCase(worldCheckResult, RESULT_FOUND)) {
			for (String checkId : checkIdList) {
				String l3Response = onlineVerificationChecksService.sendDataToL3DiscrepantProcessAttempt(mapper, checkId);
				String l3Status= getL3Status(l3Response);
				updateAttemptHistory(checkId,l3Status, l3Response);
				if (StringUtils.equalsIgnoreCase(l3Status, SUCCESS)) {
					onlineVerificationChecksRepository.updateVerifyChecks(crnNo, worldCheckResult, apiName);
				}
			}
		}else if(StringUtils.equalsIgnoreCase(worldCheckResult, CLEAR)){
			String l3Response = onlineVerificationChecksService.sendAllClearDataToL3(mapper, checkIdList);
			String l3Status = getL3Status(l3Response);
			for (String checkId : checkIdList) {
				updateAttemptHistory(checkId, l3Status, l3Response);
				if (StringUtils.equalsIgnoreCase(l3Status, SUCCESS)) {
					onlineVerificationChecksRepository.updateVerifyChecks(crnNo, worldCheckResult, apiName);
				}
			}
		}
	}
	
//	private void updateWorldCheckDb(ObjectMapper mapper, String apiName, String crnNo, String worldCheckResult,
//			List<String> checkIdList) throws JsonProcessingException {
//		if (CollectionUtils.isNotEmpty(checkIdList) && (StringUtils.equalsIgnoreCase(worldCheckResult, CLEAR)
//				|| StringUtils.equalsIgnoreCase(worldCheckResult, RESULT_FOUND))) {
//			String l3Response = sendAllClearDataToL3(mapper, checkIdList, worldCheckResult);
//			String l3Status = getL3Status(l3Response);
//			for (String checkId : checkIdList) {
//				updateAttemptHistory(checkId, l3Status, l3Response);
//			}
//			if (StringUtils.equalsIgnoreCase(l3Status, SUCCESS)) {
//				onlineVerificationChecksRepository.updateVerifyChecks(crnNo, worldCheckResult, apiName);
//			}
//		}
//	}

	private String getL3Status(String l3Response) {
		if (StringUtils.equalsIgnoreCase(l3Response, L3_ERROR_RESPONSE)) {
			return "failed";
		} else {
			return SUCCESS;
		}
	}

	private void updateAttemptHistory(String checkId, String l3Status, String l3Response) {
		List<AttemptHistory> attemptHistories = attemptMasterRepository.findByCheckid(checkId);
		List<AttemptHistory> newAttemptHistories = new ArrayList<>();
		for (AttemptHistory attemptHistory : attemptHistories) {
			attemptHistory.setL3Status(l3Status);
			attemptHistory.setL3Response(l3Response);
			newAttemptHistories.add(attemptHistory);
		}
		attemptMasterRepository.saveAll(newAttemptHistories);
	}

	private String sendAllClearDataToL3(ObjectMapper mapper, List<String> checkIdList, String status)
			throws JsonProcessingException {

		String checkId = checkIdList.get(0);
		String componentName = DATABASE;
		L3CaseDetails l3CaseDetails = onlineVerificationChecksRepository.getAllL3Cases(checkId, componentName);

		ObjectNode taskSpecs = mapper.createObjectNode();
		CaseReference caseReference = new CaseReference();

		if (l3CaseDetails != null) {
			caseReference = mapper.readValue(l3CaseDetails.getCaseReference(), CaseReference.class);
			caseReference.setSbuName(l3CaseDetails.getSbuName());
			caseReference.setPackageName(l3CaseDetails.getPackageName());
			caseReference.setProductName(l3CaseDetails.getProductName());
		} else {
			caseReference.setSbuName("");
			caseReference.setPackageName("");
			caseReference.setProductName("");
		}
		caseReference.setCheckId("");
		if (StringUtils.equalsIgnoreCase(status, CLEAR)) {
			caseReference.setNgStatus("Result Review - Clear");
			caseReference.setNgStatusDescription(RESULT_RECEIVED_WITH_NO_RECORDS);
		} else {
			caseReference.setNgStatus("Result Review - Record Found");
			caseReference.setNgStatusDescription(RECORD_FOUND);
		}
		caseReference.setComponentName(componentName);

		JsonNode caseReferenceNode = mapper.convertValue(caseReference, JsonNode.class);

		ObjectNode checkVerification = mapper.createObjectNode();
		checkVerification.put("action", VERIFY_CHECKS);
		checkVerification.put("subAction", "datbaseVerificationForAllClear");
		checkVerification.put("verificationCheckId", checkIdList.toString());

		taskSpecs.set("caseReference", caseReferenceNode);
		taskSpecs.set("checkVerification", checkVerification);

		FileUpload fileUpload = new FileUpload();
		fileUpload.setVerificationReplyDocument(new ArrayList<>());
		fileUpload.setDirectory("");
		JsonNode fileUploadNode = mapper.convertValue(fileUpload, JsonNode.class);

		taskSpecs.set("caseReference", caseReferenceNode);
		taskSpecs.set("checkVerification", checkVerification);
		taskSpecs.set("questionaire", mapper.createArrayNode());
		taskSpecs.set("fileUpload", fileUploadNode);

		String taskSpecsStr = mapper.writeValueAsString(taskSpecs);

		logger.info(L3_VERIFICATION_JSON, taskSpecsStr);

		String l3VerifyResponse = onlineApiService.sendDataToL3Post(multiCheckVerificationUrlL3, taskSpecsStr, null);

		if (l3VerifyResponse == null) {
			return L3_ERROR_RESPONSE;
		}
		return l3VerifyResponse;
	}
}
