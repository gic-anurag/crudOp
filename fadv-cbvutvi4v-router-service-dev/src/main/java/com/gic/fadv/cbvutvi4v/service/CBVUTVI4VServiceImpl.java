package com.gic.fadv.cbvutvi4v.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.gic.fadv.cbvutvi4v.model.CBVUTVI4V;
import com.gic.fadv.cbvutvi4v.model.VerificationSLA;
import com.gic.fadv.cbvutvi4v.pojo.CaseReferencePOJO;
import com.gic.fadv.cbvutvi4v.pojo.CaseSpecificInfoPOJO;
import com.gic.fadv.cbvutvi4v.pojo.CaseSpecificRecordDetailPOJO;
import com.gic.fadv.cbvutvi4v.pojo.CheckVerificationPOJO;
import com.gic.fadv.cbvutvi4v.pojo.FileUploadPOJO;
import com.gic.fadv.cbvutvi4v.pojo.QuestionPOJO;
import com.gic.fadv.cbvutvi4v.pojo.QuestionnairePOJO;
import com.gic.fadv.cbvutvi4v.pojo.TaskSpecsPOJO;

@Service
public class CBVUTVI4VServiceImpl implements CBVUTVI4VService {

	private static final String MOVE_TO_NEXT_ENGINE = "Move to next engine";

	@Autowired
	private ApiService apiService;

	private static final Logger logger = LoggerFactory.getLogger(CBVUTVI4VServiceImpl.class);

	@Value("${verificationsla.rest.url}")
	private String verificationSLARestUrl;
	@Value("${cbvutvi4v.rest.url}")
	private String cbvUtvI4vRestUrl;
	@Value("${questionaire.list.l3.url}")
	private String questionaireURL;
	@Value("${verification.status.url.l3}")
	private String verificationStatusL3Url;

	private static final String ENGINE = "engine";
	private static final String SUCCESS = "success";
	private static final String MESSAGE = "message";
	private static final String STATUS = "status";
	private static final String RESULT = "result";
	private static final String CBVUTV_RESULT = "cbvutvi4vResult";
	private static final String SUCCEEDED = "SUCCEEDED";
	private static final String FAILED = "FAILED";
	private static final String CANNOT_VERIFY = "Cannot be verified";
	private static final String UNABLE_VERIFY = "Unable to Verify";
	private static final String L3_ERROR_RESPONSE = "{\"Error\": \"Error as reponse from L3\"}";
	private static final String L3_STATUS = "l3Status";
	private static final String ERROR = "error";
	private static final String L3_RESPONSE = "l3Response";
	private static final String RETURN_MESSAGE = "returnMessage";

	@Override
	public ObjectNode processRequestBody(JsonNode requestNode) {
		logger.info("CBVUTV Process Request Body Start");
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		Map<String, String> responseMap = new HashMap<>();

		if (!requestNode.isEmpty()) {
			JsonNode caseSpecificInfoNode = requestNode.has("caseSpecificInfo") ? requestNode.get("caseSpecificInfo")
					: mapper.createObjectNode();
			JsonNode caseSpecificRecordDetailNode = requestNode.has("caseSpecificRecordDetail")
					? requestNode.get("caseSpecificRecordDetail")
					: mapper.createObjectNode();

			CaseSpecificInfoPOJO caseSpecificInfoPOJO = mapper.convertValue(caseSpecificInfoNode,
					CaseSpecificInfoPOJO.class);
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO = mapper
					.convertValue(caseSpecificRecordDetailNode, CaseSpecificRecordDetailPOJO.class);

			responseMap = processRecords(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO);

		}
		logger.info("CBVUTV Process Request Body End");
		return generateResponseStr(mapper, responseMap);
	}

	private ObjectNode generateResponseStr(ObjectMapper mapper, Map<String, String> responseMap) {
		ObjectNode responseNode = mapper.createObjectNode();
		logger.info("CBVUTV generateResponseStr Request Body Start");
		responseNode.put(ENGINE, CBVUTV_RESULT);
		if (responseMap != null) {
			responseNode.put(SUCCESS, true);
			responseNode.put(MESSAGE, SUCCEEDED);
			responseNode.put(STATUS, 200);
			if (StringUtils.equalsIgnoreCase(responseMap.get(L3_RESPONSE), L3_ERROR_RESPONSE)) {
				responseNode.put(L3_STATUS, ERROR);
			} else {
				responseNode.put(L3_STATUS, SUCCESS);
			}
			responseNode.put(L3_RESPONSE, responseMap.get(L3_RESPONSE));
			responseNode.put(RESULT, responseMap.get(RETURN_MESSAGE));
		} else {
			responseNode.put(SUCCESS, false);
			responseNode.put(MESSAGE, FAILED);
			responseNode.put(STATUS, 200);
			responseNode.put(RESULT, "Unable to process engine");
		}
		logger.info("CBVUTV generateResponseStr Request Body End");
		return responseNode;
	}

	private Map<String, String> processRecords(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO) {
		logger.info("CBVUTV processRecords Start");
		String cbvSlaSearchString = "{\"clientCode\":\"" + caseSpecificInfoPOJO.getClientCode() + "\"}";
		logger.info("Value of Sla Search String : {}", cbvSlaSearchString);
		String cbvVerificationSLAResponse = apiService.sendDataToPost(verificationSLARestUrl, cbvSlaSearchString);
		logger.info("Value of verification Response : {}", cbvVerificationSLAResponse);

		List<VerificationSLA> verificationSLAs = new ArrayList<>();
		if (cbvVerificationSLAResponse != null && !StringUtils.isEmpty(cbvVerificationSLAResponse)) {
			try {
				verificationSLAs = mapper.readValue(cbvVerificationSLAResponse,
						new TypeReference<List<VerificationSLA>>() {
						});
			} catch (JsonProcessingException e) {
				logger.info("Error while mapping verification SLA response : {}", e.getMessage());
				e.printStackTrace();
			}
		}
		if (CollectionUtils.isNotEmpty(verificationSLAs)) {
			VerificationSLA verificationSLA = verificationSLAs.get(0);
			return processVerificationSLA(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO, verificationSLA);
		}
		logger.info("CBVUTV ProcessRecords Body End");
		return getNextEngineMap();
	}

	private Map<String, String> processVerificationSLA(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, VerificationSLA verificationSLA) {
		logger.info("CBVUTV processVerification SLA Start");
		String htsVal = verificationSLA.getHTS() != null ? verificationSLA.getHTS() : "";
		logger.info("Value of HTS : {}", htsVal);

		if (StringUtils.equalsIgnoreCase(htsVal, "No")) {
			logger.info("Got verification SLA NO");

			String thirdAkaName = getThirdAkaName(mapper, caseSpecificRecordDetailPOJO);
			String cbvSearchString = "{\"companyAkaName\":" + "\"" + thirdAkaName + "\"" + ",\"productName\":" + "\""
					+ caseSpecificRecordDetailPOJO.getProduct() + "\"" + "}";
			logger.info("Value of CBV-UTV-I4V Search String :{}", cbvSearchString);

			String cbvResponse = apiService.sendDataToPost(cbvUtvI4vRestUrl, cbvSearchString);
			logger.info("Value of CBV-UTV-I4V : {}", cbvResponse);

			List<CBVUTVI4V> cbvutvi4vList = new ArrayList<>();
			if (cbvResponse != null && !StringUtils.isEmpty(cbvResponse)) {
				try {
					cbvutvi4vList = mapper.readValue(cbvResponse, new TypeReference<List<CBVUTVI4V>>() {
					});
				} catch (JsonProcessingException e) {
					logger.info("Error while mapping CBV-UTV-I4V response : {}", e.getMessage());
				}
			}
			if (CollectionUtils.isNotEmpty(cbvutvi4vList)) {
				CBVUTVI4V cbvutvi4v = cbvutvi4vList.get(0);
				return processCbvUtvResponse(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO, cbvutvi4v);
			}
		}
		logger.info("CBVUTV Process Verification SLA Start");
		return getNextEngineMap();
	}

	private Map<String, String> getNextEngineMap() {
		Map<String, String> returnMessage = new HashMap<>();
		logger.info(MOVE_TO_NEXT_ENGINE);
		returnMessage.put(RETURN_MESSAGE, MOVE_TO_NEXT_ENGINE);
		returnMessage.put(L3_RESPONSE, "{}");
		return returnMessage;
	}

	private String getThirdAkaName(ObjectMapper mapper, CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO) {
		logger.info("CBVUTV getThirdAkaName Start");
		String recordStr = caseSpecificRecordDetailPOJO.getComponentRecordField() != null
				? caseSpecificRecordDetailPOJO.getComponentRecordField()
				: "{}";
		String thirdAkaName = "";
		try {
			if (recordStr != null && StringUtils.isNotEmpty(recordStr)) {
				JsonNode recordNode = mapper.readValue(recordStr, JsonNode.class);
				thirdAkaName = recordNode.has("Third Party or Agency Name and Address")
						? recordNode.get("Third Party or Agency Name and Address").asText()
						: "";

				if (thirdAkaName == null || StringUtils.isEmpty(thirdAkaName)) {
					thirdAkaName = recordNode.has("Company Aka Name") ? recordNode.get("Company Aka Name").asText()
							: "";
				}
			}
		} catch (JsonProcessingException e) {
			logger.info("Error while mapping record node : {}", e.getMessage());
		}
		logger.info("CBVUTV ThirdAkaName End");
		return thirdAkaName;
	}

	private Map<String, String> processCbvUtvResponse(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, CBVUTVI4V cbvutvi4v) {

		String cbvFlag = cbvutvi4v.getFlag() != null ? cbvutvi4v.getFlag() : "";
		String checkId = caseSpecificRecordDetailPOJO.getInstructionCheckId() != null
				? caseSpecificRecordDetailPOJO.getInstructionCheckId()
				: "";
		logger.info("Value of Flag : {}", cbvutvi4v.getFlag());

		Map<String, String> returnMessage = new HashMap<>();

		try {
			if (StringUtils.equalsIgnoreCase(cbvFlag, "C") && StringUtils.isNotEmpty(checkId)) {
				String l3Response = sendVerifyDataToL3(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO,
						checkId, true);
				logger.info("Execute Cannot be verified");
				returnMessage.put(RETURN_MESSAGE, CANNOT_VERIFY);
				returnMessage.put(L3_RESPONSE, l3Response);

			} else if (StringUtils.equalsIgnoreCase(cbvFlag, "U") && StringUtils.isNotEmpty(checkId)) {
				String l3Response = sendVerifyDataToL3(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO,
						checkId, false);
				logger.info("Excute Unabled to verified");
				returnMessage.put(RETURN_MESSAGE, UNABLE_VERIFY);
				returnMessage.put(L3_RESPONSE, l3Response);

			} else {
				logger.info(MOVE_TO_NEXT_ENGINE);
				returnMessage.put(RETURN_MESSAGE, MOVE_TO_NEXT_ENGINE);
				returnMessage.put(L3_RESPONSE, "{}");
			}
			return returnMessage;
		} catch (JsonProcessingException e) {
			logger.error("Exception while sending verify data to l3 : {}", e.getMessage());
			return null;
		}
	}

	private String sendVerifyDataToL3(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, String checkId, boolean cbvUtv)
			throws JsonProcessingException {

		TaskSpecsPOJO taskSpecs = new TaskSpecsPOJO();

		taskSpecs.setCaseReference(
				getCaseReference(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO, checkId, cbvUtv));
		taskSpecs.setCheckVerification(getCheckVerification(caseSpecificRecordDetailPOJO, cbvUtv));

		taskSpecs.setQuestionaire(getQuestionnaire(mapper, checkId, cbvUtv));

		FileUploadPOJO fileUpload = new FileUploadPOJO();
		fileUpload.setVerificationReplyDocument(new ArrayList<>());
		taskSpecs.setFileUpload(fileUpload);

		String taskSpecsStr = mapper.writeValueAsString(taskSpecs);
		logger.info("l3 verification json : {} ", taskSpecsStr);

		String l3VerifyResponse = apiService.sendDataToL3Post(verificationStatusL3Url, taskSpecsStr, null);

		if (l3VerifyResponse == null) {
			return L3_ERROR_RESPONSE;
		}
		return l3VerifyResponse;
	}

	private CaseReferencePOJO getCaseReference(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, String checkId, boolean cbvUtv)
			throws JsonProcessingException {
		CaseReferencePOJO caseReference = mapper.readValue(caseSpecificInfoPOJO.getCaseReference(),
				CaseReferencePOJO.class);

		caseReference.setCheckId(checkId);
		if (Boolean.TRUE.equals(cbvUtv)) {
			caseReference.setNgStatus("CBV");
			caseReference.setNgStatusDescription(CANNOT_VERIFY);
		} else {
			caseReference.setNgStatus("UTV");
			caseReference.setNgStatusDescription(UNABLE_VERIFY);
		}
		caseReference.setSbuName(caseSpecificInfoPOJO.getSbuName());
		caseReference.setProductName(caseSpecificRecordDetailPOJO.getProduct());
		caseReference.setPackageName(caseSpecificInfoPOJO.getPackageName());
		caseReference.setComponentName(caseSpecificRecordDetailPOJO.getComponentName());

		return caseReference;
	}

	private CheckVerificationPOJO getCheckVerification(CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO,
			boolean cbvUtv) {
		CheckVerificationPOJO checkVerification = new CheckVerificationPOJO();

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
		Date todaysDate = new Date();
		String todaysDateStr = formatter.format(todaysDate);

		checkVerification.setCountry("India");

		if (Boolean.TRUE.equals(cbvUtv)) {
			checkVerification.setExecutiveSummaryComments("Cannot be verified (Not disclosed as per company policy)");
			checkVerification.setInternalNotes(
					"Employment details not disclosed as per Company policy. Hence cannot be verified");
			checkVerification.setEndStatusOfTheVerification(CANNOT_VERIFY);
		} else {
			checkVerification.setExecutiveSummaryComments(UNABLE_VERIFY);
			checkVerification.setInternalNotes(
					"Name not disclosed, Official from the Human Resource Department verbally stated that employment details will not be disclosed to First Advantage Private Limited citing mutual process disagreement. Hence Unable to verify");
			checkVerification.setEndStatusOfTheVerification(UNABLE_VERIFY);
		}

		checkVerification.setReportComments("");
		checkVerification.setResultCode("");
		checkVerification.setEmailId("");
		checkVerification.setVerifierName("Name not disclosed");
		checkVerification.setGeneralRemarks("");
		checkVerification.setModeOfVerification("Email ID-Official");
		checkVerification.setVerifiedDate("");
		checkVerification.setAction("verifyChecks");
		checkVerification.setSubAction("verifyChecks");
		checkVerification.setActionCode("");
		checkVerification.setComponentName(caseSpecificRecordDetailPOJO.getComponentName());
		checkVerification.setAttempts("Internal");
		checkVerification.setDepartmentName("");
		checkVerification.setKeyWords("");
		checkVerification.setCost("");
		checkVerification.setIsThisAVerificationAttempt("Yes");
		checkVerification.setDateVerificationCompleted(todaysDateStr);
		checkVerification.setDisposition("");
		checkVerification.setExpectedClosureDate("");
		checkVerification.setVerifierDesignation("Official");
		checkVerification.setFollowUpDateAndTimes("");
		checkVerification.setVerifierNumber("");

		return checkVerification;
	}

	private List<QuestionnairePOJO> getQuestionnaire(ObjectMapper mapper, String checkId, boolean cbvUtv)
			throws JsonProcessingException {
		List<String> quetionRefIDList = new ArrayList<>();
		quetionRefIDList.add("500110");
		quetionRefIDList.add("807981");
		quetionRefIDList.add("800054");

		List<QuestionPOJO> questionPOJOList = new ArrayList<>();
		List<QuestionnairePOJO> questionnairePOJOList = new ArrayList<>();

		String requestUrl = questionaireURL + checkId;
		String questionResponse = apiService.sendDataToL3Get(requestUrl);
		ObjectNode attemptQuestionnaireNode = mapper.createObjectNode();
		JsonNode questionnaire = mapper.createObjectNode();

		logger.info("Questionnaire response : {}", questionResponse);
		if (questionResponse != null && StringUtils.isNotEmpty(questionResponse)) {
			attemptQuestionnaireNode = (ObjectNode) mapper.readTree(questionResponse);
		}
		if (attemptQuestionnaireNode != null && !attemptQuestionnaireNode.isEmpty()
				&& attemptQuestionnaireNode.has("response")) {
			questionnaire = attemptQuestionnaireNode.get("response");
		}
		if (questionnaire != null && !questionnaire.isEmpty()) {
			questionPOJOList = mapper.readValue(questionnaire.toString(), new TypeReference<List<QuestionPOJO>>() {
			});
		}

		for (QuestionPOJO questionPOJO : questionPOJOList) {
			String globalQuestionId = questionPOJO.getGlobalQuestionId() != null ? questionPOJO.getGlobalQuestionId()
					: "";
			if (quetionRefIDList.contains(globalQuestionId)) {
				QuestionnairePOJO questionnairePOJO = new QuestionnairePOJO();
				questionnairePOJO.setCaseQuestionRefID(globalQuestionId);
				questionnairePOJO.setAnswer(questionPOJO.getAnswere());
				questionnairePOJO.setQuestion(questionPOJO.getQuestionName());
				if (Boolean.TRUE.equals(cbvUtv)) {
					questionnairePOJO.setReportData(
							"Employment details not disclosed as per Company policy.Hence cannot verify.");
				} else {
					questionnairePOJO.setReportData("Name not disclosed, Official from the Human "
							+ "Resource Department verbally stated that employment details will not "
							+ "be disclosed to First Advantage Private Limited citing mutual process"
							+ " disagreement.Hence Unable to verify");
				}
				// Employment details not disclosed as per Company policy.Hence cannot verify.
				questionnairePOJO.setStatus("");
				questionnairePOJO.setVerifiedData("");
				questionnairePOJOList.add(questionnairePOJO);
			}
		}

		return questionnairePOJOList;
	}
}
