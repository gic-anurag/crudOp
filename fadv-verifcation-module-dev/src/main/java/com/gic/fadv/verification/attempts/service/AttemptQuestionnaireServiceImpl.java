package com.gic.fadv.verification.attempts.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.verification.attempts.model.AttemptHistory;
import com.gic.fadv.verification.attempts.model.AttemptQuestionnaire;
import com.gic.fadv.verification.attempts.model.AttemptStatusData;
import com.gic.fadv.verification.attempts.pojo.MiRqDataPOJO;
import com.gic.fadv.verification.attempts.repository.AttemptMasterRepository;
import com.gic.fadv.verification.attempts.repository.AttemptQuestionnaireRepository;
import com.gic.fadv.verification.attempts.repository.AttemptStatusDataRepository;
import com.gic.fadv.verification.online.model.CaseReference;
import com.gic.fadv.verification.online.model.CheckVerification;
import com.gic.fadv.verification.online.model.FileUpload;
import com.gic.fadv.verification.online.model.TaskSpecs;
import com.gic.fadv.verification.online.repository.OnlineVerificationChecksRepository;
import com.gic.fadv.verification.online.service.OnlineApiService;
import com.gic.fadv.verification.pojo.QuestionnairePOJO;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class AttemptQuestionnaireServiceImpl implements AttemptQuestionnaireService {
	private static final String MI_REMARKS = "miRemarks";
	private static final String FAILED = "failed";
	private static final String GET_VERIFICATION_STATUSFOR_L3_NODE = "getVerificationStatusforL3Node : {} ";
	private static final String INDIA = "India";
	private static final String SUCCESS = "success";
	private static final String MI_RQ = "MI-RQ";
	private static final String CA_RQ = "CA-RQ";
	@Autowired
	private AttemptMasterRepository attemptMasterRepository;
	@Autowired
	private AttemptQuestionnaireRepository attemptQuestionnaireRepository;
	@Autowired
	private OnlineVerificationChecksRepository onlineVerificationChecksRepository;
	@Autowired
	private AttemptStatusDataRepository attemptStatusDataRepository;
	@Autowired
	private L3APIService l3APIService;

	@Autowired
	private OnlineApiService onlineApiService;

	@Value("${verification.url.l3}")
	private String verificationL3Url;
	@Value("${verification.status.url.l3}")
	private String verificationStatusL3Url;
	@Value("${verbiase.rest.url}")
	private String verbiaseRestUrl;

	private static final Logger logger = LoggerFactory.getLogger(AttemptQuestionnaireServiceImpl.class);

	@Override
	public String processAttempQuestionnaire(String checkId, ObjectMapper mapper, ArrayNode attemptQuestionnaireArrNode)
			throws JsonProcessingException {
		if (!attemptQuestionnaireArrNode.isEmpty()) {
			List<AttemptQuestionnaire> attemptQuestionnaireList = mapper
					.readValue(attemptQuestionnaireArrNode.toString(), new TypeReference<List<AttemptQuestionnaire>>() {
					});
			attemptQuestionnaireRepository.saveAll(attemptQuestionnaireList);
		}
		List<String> followUpStatus = new ArrayList<>();
		followUpStatus.add("F1");
		followUpStatus.add("F2");
		followUpStatus.add("F3");
		followUpStatus.add("F4");
		followUpStatus.add("F5");
		followUpStatus.add("FF");
		List<MiRqDataPOJO> miRqDataPOJOList = attemptMasterRepository.getAllDataForMIReq(followUpStatus, checkId,
				SUCCESS);

		String sendInfol3VeriStatus = "";
		for (MiRqDataPOJO miRqDataPOJO : miRqDataPOJOList) {
			sendInfol3VeriStatus = sendToL3FollowUp(mapper, miRqDataPOJO);
		}

		followUpStatus = new ArrayList<>();
		followUpStatus.add(MI_RQ);
		miRqDataPOJOList = attemptMasterRepository.getAllDataForMIReq(followUpStatus, checkId, SUCCESS);
		logger.info("miRqDataPOJOList Lenght : {}", miRqDataPOJOList.size());
		for (MiRqDataPOJO miRqDataPOJO : miRqDataPOJOList) {
			sendInfol3VeriStatus = sendDataToL3Mi(mapper, miRqDataPOJO);
		}

		followUpStatus = new ArrayList<>();
		followUpStatus.add(CA_RQ);
		miRqDataPOJOList = attemptMasterRepository.getAllDataForMIReq(followUpStatus, checkId, SUCCESS);

		for (MiRqDataPOJO miRqDataPOJO : miRqDataPOJOList) {
			sendInfol3VeriStatus = sendDataToL3CaRq(mapper, miRqDataPOJO);
		}

		return sendInfol3VeriStatus;
	}

	@Override
	public String sendDataToL3Mi(ObjectMapper mapper, MiRqDataPOJO miRqDataPOJO) throws JsonProcessingException {
		logger.info("miRqDataPOJO : {}", miRqDataPOJO.getAdditionalInfo());
		String additionalInfoStr = miRqDataPOJO.getAdditionalInfo() != null ? miRqDataPOJO.getAdditionalInfo() : "[]";
		ArrayNode additionalInfoArr = (ArrayNode) mapper.readTree(additionalInfoStr);
		String executiveSummaryComments = getMIRQExecutiveComment(mapper, additionalInfoArr);

		executiveSummaryComments = executiveSummaryComments != null ? executiveSummaryComments : "";
		if (StringUtils.isEmpty(executiveSummaryComments) && !additionalInfoArr.isEmpty()) {
			executiveSummaryComments = additionalInfoArr.get(0).has("missingInformation") ?
					additionalInfoArr.get(0).get("missingInformation").asText() : "";
		}
//		JsonNode recordField = mapper.readValue(miRqDataPOJO.getComponentRecordField(), JsonNode.class);

		String checkId = miRqDataPOJO.getCheckId();
//		String miRemarks = recordField.has(MI_REMARKS) ? recordField.get(MI_REMARKS).asText() : "";
		String miRemarks = executiveSummaryComments;

		TaskSpecs taskSpecs = new TaskSpecs();
		CaseReference caseReference = mapper.readValue(miRqDataPOJO.getCaseReference(), CaseReference.class);

		caseReference.setCheckId(checkId);
		caseReference.setNgStatus(MI_RQ);
		caseReference.setNgStatusDescription("Missing Information - Requested");
		caseReference.setSbuName(miRqDataPOJO.getSbuName());
		caseReference.setProductName(miRqDataPOJO.getProductName());
		caseReference.setPackageName(miRqDataPOJO.getPackageName());
		caseReference.setComponentName(miRqDataPOJO.getComponentName());

		CheckVerification checkVerification = new CheckVerification();
		checkVerification.setCountry(INDIA);
		checkVerification.setExecutiveSummaryComments(executiveSummaryComments);
		checkVerification.setReportComments("");
		checkVerification.setResultCode("");
		checkVerification.setEmailId("");
		checkVerification.setVerifierName("");
		checkVerification.setGeneralRemarks("");
		checkVerification.setModeOfVerification(miRqDataPOJO.getModeOfVerification());
		checkVerification.setVerifiedDate("");
		checkVerification.setAction("checklevelmi");
		checkVerification.setSubAction("databasemi");
		checkVerification.setActionCode("");
		checkVerification.setComponentName(miRqDataPOJO.getComponentName());
		checkVerification.setProductName(miRqDataPOJO.getProductName());
		checkVerification.setAttempts("");
		checkVerification.setDepartmentName("");
		checkVerification.setKeyWords("");
		checkVerification.setCost("");
		checkVerification.setIsThisAVerificationAttempt("No");
		checkVerification.setInternalNotes(miRemarks);
		checkVerification.setDateVerificationCompleted("");
		checkVerification.setDisposition("");
		checkVerification.setExpectedClosureDate("");
		checkVerification.setEndStatusOfTheVerification("Additional Information Required");
		checkVerification.setVerifierDesignation("");
		checkVerification.setFollowUpDateAndTimes("");
		checkVerification.setVerifierNumber("");
		checkVerification.setMiRemarks(miRemarks);

		taskSpecs.setCaseReference(caseReference);
		taskSpecs.setCheckVerification(checkVerification);

		taskSpecs.setQuestionaire(new ArrayList<>());

		FileUpload fileUpload = new FileUpload();
		fileUpload.setVerificationReplyDocument(new ArrayList<>());
		taskSpecs.setFileUpload(fileUpload);

		String getVerificationStatusforL3 = mapper.writeValueAsString(taskSpecs);
		String sendInfol3VeriStatus = null;

		try {
			logger.info(GET_VERIFICATION_STATUSFOR_L3_NODE, getVerificationStatusforL3);
			sendInfol3VeriStatus = l3APIService.sendDataToRest(verificationStatusL3Url, getVerificationStatusforL3,
					null);

			JsonNode sendInfol3VeriStatusNode = mapper.readValue(sendInfol3VeriStatus, JsonNode.class);
			List<String> followUpStatus = new ArrayList<>();
			followUpStatus.add(MI_RQ);
			if (sendInfol3VeriStatus != null && StringUtils.isNotEmpty(sendInfol3VeriStatus)) {
				boolean success = sendInfol3VeriStatusNode.has(SUCCESS)
						? sendInfol3VeriStatusNode.get(SUCCESS).asBoolean()
						: Boolean.FALSE;
				if (success) {
					attemptMasterRepository.updateAttemptL3Status(followUpStatus, checkId, SUCCESS,
							sendInfol3VeriStatus);
				} else {
					attemptMasterRepository.updateAttemptL3Status(followUpStatus, checkId, FAILED,
							sendInfol3VeriStatus);
				}
			} else {
				attemptMasterRepository.updateAttemptL3Status(followUpStatus, checkId, FAILED, sendInfol3VeriStatus);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return sendInfol3VeriStatus;
	}

	@Override
	public String getMIRQExecutiveComment(ObjectMapper mapper, ArrayNode additionalInfoArr)
			throws JsonProcessingException {
		String executiveSummaryComments = "";
		if (!additionalInfoArr.isEmpty()) {
			JsonNode additionalInfo = additionalInfoArr.get(0);
			ArrayNode docNode = additionalInfo.has("document") ? additionalInfo.get("document").isArray()?
					(ArrayNode) additionalInfo.get("document"):mapper.createArrayNode()
					: mapper.createArrayNode();

			if (!docNode.isEmpty()) {
				ObjectNode verbiaseNode = mapper.createObjectNode();
				verbiaseNode.put("templateName", "case-level-mi");

				ObjectNode templateFields = mapper.createObjectNode();
				templateFields.set("missingDocs", docNode);
				templateFields.set("incompleteDocs", mapper.createArrayNode());

				verbiaseNode.set("templateFields", templateFields);
				String verbiaseStr = mapper.writeValueAsString(verbiaseNode);

				String response = onlineApiService.sendDataToPost(verbiaseRestUrl, verbiaseStr);
				response = response != null ? response : "{}";
				JsonNode responseNode = mapper.readTree(response);
				executiveSummaryComments = responseNode.has("verbiageMessages")
						? responseNode.get("verbiageMessages").asText()
						: "";
			}
		}
		return executiveSummaryComments;
	}

	private String sendToL3FollowUp(ObjectMapper mapper, MiRqDataPOJO miRqDataPOJO) throws JsonProcessingException {

		String checkId = miRqDataPOJO.getCheckId();

		TaskSpecs taskSpecs = new TaskSpecs();
		logger.info("Case Reference : {}", miRqDataPOJO.getCaseReference());

		JsonNode recordField = mapper.readValue(miRqDataPOJO.getComponentRecordField(), JsonNode.class);

		String miRemarks = recordField.has(MI_REMARKS) ? recordField.get(MI_REMARKS).asText() : "";
		CaseReference caseReference = getFollowUpCasereference(mapper, miRqDataPOJO, checkId);

		String expectedClosureDateStr = miRqDataPOJO.getExpectedClosureDate() != null
				? miRqDataPOJO.getExpectedClosureDate()
				: "";
		String followUpDateStr = miRqDataPOJO.getFollowUpDate() != null ? miRqDataPOJO.getFollowUpDate() : "";

		SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
		SimpleDateFormat formatter2 = new SimpleDateFormat("dd/MMM/yyyy");
		Date expectedClosureDate;
		Date followUpDate;
		try {
			if (StringUtils.isNotEmpty(expectedClosureDateStr)) {
				expectedClosureDate = formatter.parse(expectedClosureDateStr);
				expectedClosureDateStr = formatter2.format(expectedClosureDate);
			}
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		}
		try {
			if (StringUtils.isNotEmpty(followUpDateStr)) {
				followUpDate = formatter.parse(followUpDateStr);
				followUpDateStr = formatter2.format(followUpDate);
			}
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		}

		Date todaysDate = new Date();
		String todaysDateStr = formatter2.format(todaysDate);

		CheckVerification checkVerification = getFollowUpCheckVerification(miRqDataPOJO, miRemarks,
				expectedClosureDateStr, followUpDateStr, todaysDateStr);

		taskSpecs.setCaseReference(caseReference);
		taskSpecs.setCheckVerification(checkVerification);

		List<QuestionnairePOJO> questionnairePOJOs = onlineVerificationChecksRepository.findAllQuestionnaire(checkId);
		if (!questionnairePOJOs.isEmpty()) {
			taskSpecs.setQuestionaire(questionnairePOJOs);
		} else {
			taskSpecs.setQuestionaire(new ArrayList<>());
		}
		FileUpload fileUpload = new FileUpload();
		fileUpload.setVerificationReplyDocument(new ArrayList<>());
		taskSpecs.setFileUpload(fileUpload);

		String getVerificationStatusforL3 = mapper.writeValueAsString(taskSpecs);
		String sendInfol3VeriStatus = null;

		try {
			sendInfol3VeriStatus = checkFollowUpResponseAndUpdateAttempt(mapper, miRqDataPOJO, checkId,
					getVerificationStatusforL3);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return sendInfol3VeriStatus;
	}

	private String checkFollowUpResponseAndUpdateAttempt(ObjectMapper mapper, MiRqDataPOJO miRqDataPOJO, String checkId,
			String getVerificationStatusforL3) throws JsonProcessingException {
		String sendInfol3VeriStatus;
		logger.info(GET_VERIFICATION_STATUSFOR_L3_NODE, getVerificationStatusforL3);
		sendInfol3VeriStatus = l3APIService.sendDataToRest(verificationStatusL3Url, getVerificationStatusforL3, null);

		JsonNode sendInfol3VeriStatusNode = mapper.readValue(sendInfol3VeriStatus, JsonNode.class);
		List<String> followUpStatus = new ArrayList<>();
		followUpStatus.add(miRqDataPOJO.getFollowUpStatus());
		if (sendInfol3VeriStatus != null && StringUtils.isNotEmpty(sendInfol3VeriStatus)) {
			boolean success = sendInfol3VeriStatusNode.has(SUCCESS) ? sendInfol3VeriStatusNode.get(SUCCESS).asBoolean()
					: Boolean.FALSE;
			if (Boolean.TRUE.equals(success)) {
				attemptMasterRepository.updateAttemptL3Status(followUpStatus, checkId, SUCCESS, sendInfol3VeriStatus);
			} else {
				attemptMasterRepository.updateAttemptL3Status(followUpStatus, checkId, FAILED, sendInfol3VeriStatus);
			}
		} else {
			attemptMasterRepository.updateAttemptL3Status(followUpStatus, checkId, FAILED, sendInfol3VeriStatus);
		}
		return sendInfol3VeriStatus;
	}

	private CheckVerification getFollowUpCheckVerification(MiRqDataPOJO miRqDataPOJO, String miRemarks,
			String expectedClosureDateStr, String followUpDateStr, String todaysDateStr) {
		CheckVerification checkVerification = new CheckVerification();

		checkVerification.setCountry(INDIA);
		checkVerification.setExecutiveSummaryComments(miRqDataPOJO.getExecutiveSummaryComments());
		checkVerification.setReportComments("");
		checkVerification.setResultCode("");
		if (miRqDataPOJO.getEmailId() != null) {
			checkVerification.setEmailId(miRqDataPOJO.getEmailId());
		} else {
			checkVerification.setEmailId("");
		}
		checkVerification.setVerifierName(miRqDataPOJO.getVerifierName());
		checkVerification.setGeneralRemarks("");
		checkVerification.setModeOfVerification(miRqDataPOJO.getModeOfVerification());
		checkVerification.setVerifiedDate("");
		checkVerification.setAction("verifyChecks");
		if (StringUtils.equalsAnyIgnoreCase(miRqDataPOJO.getComponentName(), "Database")) {
			checkVerification.setSubAction("databaseVerification");
		} else {
			checkVerification.setSubAction("verifyChecks");
		}
		checkVerification.setActionCode("");
		checkVerification.setComponentName(miRqDataPOJO.getComponentName());
		if (StringUtils.equalsIgnoreCase(miRqDataPOJO.getVerificationAttemptType(), "Valid")) {
			checkVerification.setIsThisAVerificationAttempt("Yes");
			checkVerification.setAttempts("Internal");
		} else {
			checkVerification.setIsThisAVerificationAttempt("No");
			checkVerification.setAttempts("");
		}
		checkVerification.setDepartmentName("");
		checkVerification.setKeyWords("");
		checkVerification.setCost("");
		checkVerification.setInternalNotes(miRqDataPOJO.getInternalNotes());
		if (StringUtils.equalsIgnoreCase(miRqDataPOJO.getFollowUpStatus(), "F1")) {
			checkVerification.setDateVerificationCompleted("");
		} else {
			checkVerification.setDateVerificationCompleted(todaysDateStr);
		}
		checkVerification.setDisposition(miRqDataPOJO.getDisposition());
		checkVerification.setExpectedClosureDate(expectedClosureDateStr);
		checkVerification.setEndStatusOfTheVerification(miRqDataPOJO.getEndStatusOfTheVerification());
		checkVerification.setVerifierDesignation(miRqDataPOJO.getVerifierDesignation());
		checkVerification.setFollowUpDateAndTimes(followUpDateStr);
		checkVerification.setVerifierNumber(miRqDataPOJO.getVerifierNumber());
		checkVerification.setMiRemarks(miRemarks);
		return checkVerification;
	}

	private CaseReference getFollowUpCasereference(ObjectMapper mapper, MiRqDataPOJO miRqDataPOJO, String checkId)
			throws JsonProcessingException {
		CaseReference caseReference = mapper.readValue(miRqDataPOJO.getCaseReference(), CaseReference.class);
		caseReference.setCheckId(checkId);
		caseReference.setNgStatus(miRqDataPOJO.getFollowUpStatus());
		caseReference.setNgStatusDescription(miRqDataPOJO.getFollowUpDescription());
		caseReference.setSbuName(miRqDataPOJO.getSbuName());
		caseReference.setProductName(miRqDataPOJO.getProductName());
		caseReference.setPackageName(miRqDataPOJO.getPackageName());
		caseReference.setComponentName(miRqDataPOJO.getComponentName());
		return caseReference;
	}

	private String sendDataToL3CaRq(ObjectMapper mapper, MiRqDataPOJO miRqDataPOJO) throws JsonProcessingException {
		String additionalInfoStr = miRqDataPOJO.getAdditionalInfo() != null ? miRqDataPOJO.getAdditionalInfo() : "[]";
		ArrayNode additionalInfoArr = (ArrayNode) mapper.readTree(additionalInfoStr);

		String amount = "";
		if (!additionalInfoArr.isEmpty()) {
			JsonNode additionalInfo = additionalInfoArr.get(0);
			amount = additionalInfo.has("reqCostApproval") ? additionalInfo.get("reqCostApproval").asText() : "";
		}
		amount = amount != null ? amount : "";
		String executiveSummaryComments = "";
		String caRqRemarks = "";

		if (StringUtils.isNotEmpty(amount)) {
			executiveSummaryComments = "Additional cost of INR " + amount
					+ " approval is required to carry out the verification";
			caRqRemarks = "INR " + amount + " cost approval required";
		}

		String checkId = miRqDataPOJO.getCheckId();

		TaskSpecs taskSpecs = new TaskSpecs();
		CaseReference caseReference = mapper.readValue(miRqDataPOJO.getCaseReference(), CaseReference.class);

		caseReference.setCheckId(checkId);
		caseReference.setNgStatus(CA_RQ);
		caseReference.setNgStatusDescription("Cost Approval - Requested");
		caseReference.setSbuName(miRqDataPOJO.getSbuName());
		caseReference.setProductName(miRqDataPOJO.getProductName());
		caseReference.setPackageName(miRqDataPOJO.getPackageName());
		caseReference.setComponentName(miRqDataPOJO.getComponentName());

		CheckVerification checkVerification = new CheckVerification();
		checkVerification.setCountry(INDIA);
		checkVerification.setExecutiveSummaryComments(executiveSummaryComments);
		checkVerification.setReportComments("Employee Code Would be Required");
		checkVerification.setResultCode("VR - Verification received");
		checkVerification.setEmailId(miRqDataPOJO.getEmailId());
		checkVerification.setVerifierName("");
		checkVerification.setGeneralRemarks("");
		checkVerification.setModeOfVerification("Online");
		checkVerification.setVerifiedDate("");
		checkVerification.setAction("costapproval");
		checkVerification.setSubAction("");
		checkVerification.setActionCode("ONL - Online");
		checkVerification.setComponentName(miRqDataPOJO.getComponentName());
		checkVerification.setProductName(miRqDataPOJO.getProductName());
		checkVerification.setAttempts("");
		checkVerification.setDepartmentName("");
		checkVerification.setKeyWords("");
		checkVerification.setApproveOrRejectComment("Approve");
		checkVerification.setCost(amount);
		checkVerification.setIsThisAVerificationAttempt("No");
		checkVerification.setInternalNotes(caRqRemarks);
		checkVerification.setDateVerificationCompleted("");
		checkVerification.setDisposition(miRqDataPOJO.getDisposition());
		checkVerification.setExpectedClosureDate("");
		checkVerification.setEndStatusOfTheVerification("Pending Cost Approval");
		checkVerification.setVerifierDesignation("");
		checkVerification.setFollowUpDateAndTimes("");
		checkVerification.setVerifierNumber(miRqDataPOJO.getVerifierName());

		taskSpecs.setCaseReference(caseReference);
		taskSpecs.setCheckVerification(checkVerification);

		taskSpecs.setQuestionaire(new ArrayList<>());

		FileUpload fileUpload = new FileUpload();
		fileUpload.setVerificationReplyDocument(new ArrayList<>());
		taskSpecs.setFileUpload(fileUpload);

		String getVerificationStatusforL3 = mapper.writeValueAsString(taskSpecs);
		String sendInfol3VeriStatus = null;

		try {
			logger.info(GET_VERIFICATION_STATUSFOR_L3_NODE, getVerificationStatusforL3);
			sendInfol3VeriStatus = l3APIService.sendDataToRest(verificationStatusL3Url, getVerificationStatusforL3,
					null);

			JsonNode sendInfol3VeriStatusNode = mapper.readValue(sendInfol3VeriStatus, JsonNode.class);
			List<String> followUpStatus = new ArrayList<>();
			followUpStatus.add(MI_RQ);
			if (sendInfol3VeriStatus != null && StringUtils.isNotEmpty(sendInfol3VeriStatus)) {
				boolean success = sendInfol3VeriStatusNode.has(SUCCESS)
						? sendInfol3VeriStatusNode.get(SUCCESS).asBoolean()
						: Boolean.FALSE;
				if (success) {
					attemptMasterRepository.updateAttemptL3Status(followUpStatus, checkId, SUCCESS,
							sendInfol3VeriStatus);
				} else {
					attemptMasterRepository.updateAttemptL3Status(followUpStatus, checkId, FAILED,
							sendInfol3VeriStatus);
				}
			} else {
				attemptMasterRepository.updateAttemptL3Status(followUpStatus, checkId, FAILED, sendInfol3VeriStatus);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return sendInfol3VeriStatus;
	}

	@Override
	public void updateAttemptFollowUp(Long followUpId, String checkId, String followUpStatus) {
		List<AttemptHistory> attemptHistoryList = attemptMasterRepository.findByCheckidOrderByAttemptidDesc(checkId);

		if (CollectionUtils.isNotEmpty(attemptHistoryList)) {
			AttemptHistory attemptHistory = attemptHistoryList.get(0);

			attemptHistory.setFollowupId(followUpId);
			attemptMasterRepository.save(attemptHistory);

			List<AttemptStatusData> attemptStatusDataList = attemptStatusDataRepository
					.findByAttemptId(attemptHistory.getAttemptid());

			if (CollectionUtils.isNotEmpty(attemptStatusDataList)) {
				AttemptStatusData attemptStatusData = attemptStatusDataList.get(0);
				attemptStatusData.setEndstatusId(followUpId);
				attemptStatusDataRepository.save(attemptStatusData);
			} else {
				AttemptStatusData attemptStatusData = new AttemptStatusData();
				attemptStatusData.setAttemptId(attemptHistory.getAttemptid());
				attemptStatusData.setEndstatusId(followUpId);

				if (StringUtils.equalsIgnoreCase(MI_RQ, followUpStatus)) {
					attemptStatusData.setDepositionId(13);
					attemptStatusData.setModeId(6);
				} else if (StringUtils.equalsIgnoreCase(CA_RQ, followUpStatus)) {
					attemptStatusData.setDepositionId(2);
					attemptStatusData.setModeId(6);
				} else {
					attemptStatusData.setDepositionId(13);
					attemptStatusData.setModeId(14);
				}
				attemptStatusDataRepository.save(attemptStatusData);
			}
		}
	}

}
