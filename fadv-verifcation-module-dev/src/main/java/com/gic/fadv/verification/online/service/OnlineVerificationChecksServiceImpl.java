package com.gic.fadv.verification.online.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.gic.fadv.verification.attempts.model.AttemptStatusData;
import com.gic.fadv.verification.attempts.model.CaseSpecificInfo;
import com.gic.fadv.verification.attempts.model.CaseSpecificRecordDetail;
import com.gic.fadv.verification.attempts.repository.AttemptMasterRepository;
import com.gic.fadv.verification.attempts.repository.AttemptStatusDataRepository;
import com.gic.fadv.verification.attempts.repository.CaseSpecificInfoRepository;
import com.gic.fadv.verification.attempts.repository.CaseSpecificRecordDetailRepository;
import com.gic.fadv.verification.event.model.VerificationEventStatus;
import com.gic.fadv.verification.online.model.CaseReference;
import com.gic.fadv.verification.online.model.CheckVerification;
import com.gic.fadv.verification.online.model.FileUpload;
import com.gic.fadv.verification.online.model.OnlineManualVerification;
import com.gic.fadv.verification.online.model.OnlineVerificationChecks;
import com.gic.fadv.verification.online.model.TaskSpecs;
import com.gic.fadv.verification.online.repository.OnlineManualVerificationRepository;
import com.gic.fadv.verification.online.repository.OnlineVerificationChecksRepository;
import com.gic.fadv.verification.pojo.L3CaseDetails;
import com.gic.fadv.verification.pojo.OnlineVerificationChecksPOJO;
import com.gic.fadv.verification.repository.event.VerificationEventStatusRepository;

@Service
public class OnlineVerificationChecksServiceImpl implements OnlineVerificationChecksService {

	private static final String FILE_DATA = "fileData";

	private static final String RESULT = "Result";

	private static final String TITLE = "title";

	private static final String PROCESSED = "Processed";

	private static final String RESULT_RECEIVED_WITH_NO_RECORDS = "Result received with No records";

	private static final String L3_VERIFICATION_JSON = "l3 verification json : {} ";

	private static final String INDIA = "India";

	private static final String PRODUCT_NAME = "Product Name:";

	private static final String RECORD_FOUND = "Record Found";

	private static final String VERIFY_CHECKS = "verifyChecks";

	private static final String DATABASE = "Database";

	private static final String DD_MMM_YYYY = "dd/MMM/yyyy";

	private static final String RESULT_FOUND = "Result Found";

	private static final String CLEAR = "Clear";

	private static final String MANUAL = "Manual";
	private static final String L3_ERROR_RESPONSE = "{\"Error\": \"Error as reponse from L3\"}";
	private static final String ONLINE = "Online";
	private static final String AUTO = "Auto";
	private static final String SYSTEM = "System";
	private static final String APINAME = ", API Name: ";

	@Autowired
	private OnlineVerificationChecksRepository onlineVerificationChecksRepository;

	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;

	@Autowired
	private CaseSpecificInfoRepository caseSpecificInfoRepository;

	@Autowired
	private OnlineManualVerificationRepository onlineManualVerificationRepository;

	@Autowired
	private AttemptMasterRepository attemptMasterRepository;

	@Autowired
	private AttemptStatusDataRepository attemptStatusDataRepository;

	@Autowired
	private VerificationEventStatusRepository verificationEventStatusRepository;

	@Autowired
	private OnlineApiService onlineApiService;

	@Value("${verification.status.url.l3}")
	private String verificationStatusL3Url;

	@Value("${online.rerun-api.rest.url}")
	private String onlineReRunApiRestURL;

	@Value("${multicheck.verification.url.l3}")
	private String multiCheckVerificationUrlL3;

	private static final Logger logger = LoggerFactory.getLogger(OnlineVerificationChecksServiceImpl.class);

	@Override
	public ArrayNode processManuptraOutput(ObjectMapper mapper,
			List<OnlineVerificationChecks> onlineVerificationCheckList) throws JsonProcessingException {
		ArrayNode outputObjectArr = mapper.createArrayNode();
		int i = 1;
		for (OnlineVerificationChecks onlineVerificationChecks : onlineVerificationCheckList) {
			if (i < 5) {
				ObjectNode outputObject = mapper.createObjectNode();
				outputObject.set(onlineVerificationChecks.getOnlineVerificationCheckId().toString(),
						processOutputFile(mapper, onlineVerificationChecks.getOutputFile()));
				outputObjectArr.add(outputObject);
			}
			i++;
		}
		return outputObjectArr;
	}

	@Override
	public ArrayNode processOutputFile(ObjectMapper mapper, String outputFile) throws JsonProcessingException {

		JsonNode outputNode = mapper.readValue(outputFile, JsonNode.class);
		String rawDataStr = outputNode.has("rawData") ? outputNode.get("rawData").asText() : "";
		JsonNode rawDataNode = StringUtils.isNotEmpty(rawDataStr) ? mapper.readValue(rawDataStr, JsonNode.class)
				: mapper.createObjectNode();
		String dataStr = rawDataNode.has("data") ? rawDataNode.get("data").asText() : "";
		JsonNode dataNode = StringUtils.isNotEmpty(dataStr) ? mapper.readValue(dataStr, JsonNode.class)
				: mapper.createObjectNode();
		ArrayNode dataObjectArr = mapper.createArrayNode();

		if (dataNode.isArray()) {
			for (JsonNode data : dataNode) {
				dataObjectArr.add(processDataNode(mapper, data));
			}
		} else {
			dataObjectArr.add(processDataNode(mapper, dataNode));
		}
		return dataObjectArr;
	}

	private ObjectNode processDataNode(ObjectMapper mapper, JsonNode dataNode) throws JsonProcessingException {
		ObjectNode dataObject = mapper.createObjectNode();
		String titleStr = dataNode.has(TITLE) ? dataNode.get(TITLE).asText() : "";
		String fileDataStr = dataNode.has(FILE_DATA) ? dataNode.get(FILE_DATA).asText() : "";
		JsonNode fileDataNode = StringUtils.isNotEmpty(fileDataStr) ? mapper.readValue(fileDataStr, JsonNode.class)
				: mapper.createObjectNode();
		String resultStr = fileDataNode.has(RESULT) ? fileDataNode.get(RESULT).asText() : "";
		if (StringUtils.isNotEmpty(resultStr)) {
			resultStr = StringUtils.replace(resultStr, "\n", resultStr);
			resultStr = StringUtils.replace(resultStr, "\r", resultStr);
			resultStr = StringUtils.replace(resultStr, "\\\"", resultStr);
		}
		dataObject.put(TITLE, titleStr);
		dataObject.put("result", resultStr);

		return dataObject;
	}

	@Override
	public void processL3VerifyCheckIds(ObjectMapper mapper, List<String> checkIdList) {
		boolean isClientQuestionnaire = Boolean.FALSE;

		if (Boolean.TRUE.equals(isClientQuestionnaire)) {
			try {
				processQuestionnaireClient(mapper, checkIdList);
			} catch (JsonProcessingException e) {
				logger.error("Exception while sending verify checks data to L3 : {}", e.getMessage());
				e.printStackTrace();
			}
		} else {
			try {
				processNonQuestionnaireClient(mapper, checkIdList);
			} catch (JsonProcessingException e) {
				logger.error("Exception while sending verify checks data to L3 : {}", e.getMessage());
				e.printStackTrace();
			}
		}

	}

	private void processQuestionnaireClient(ObjectMapper mapper, List<String> checkIdList)
			throws JsonProcessingException {
		for (String checkId : checkIdList) {
			List<String> resultList = onlineVerificationChecksRepository.getAllChecksByCheckId(checkId);
			if (!resultList.isEmpty() && !resultList.contains(MANUAL)) {
				List<String> clearList = resultList.stream().filter(e -> e.equalsIgnoreCase(CLEAR))
						.collect(Collectors.toList());
				if (resultList.contains(RESULT_FOUND)) {
					String l3Response = sendDataToL3DiscrepantProcessAttempt(mapper, checkId);
					updateAttemptHistory(checkId, getL3Status(l3Response), l3Response);
				} else if (clearList.size() == resultList.size()) {
					String l3Response = sendDataToL3ClearOutcomeAttempt(mapper, checkId);
					updateAttemptHistory(checkId, getL3Status(l3Response), l3Response);
				}
			}
		}
	}

	private void processNonQuestionnaireClient(ObjectMapper mapper, List<String> checkIdList)
			throws JsonProcessingException {
		List<String> clearCheckIds = new ArrayList<>();
		for (String checkId : checkIdList) {
			List<String> resultList = onlineVerificationChecksRepository.getAllChecksByCheckId(checkId);
			if (!resultList.isEmpty()) {
				List<String> clearList = resultList.stream().filter(e -> e.equalsIgnoreCase(CLEAR))
						.collect(Collectors.toList());
				if (resultList.contains(RESULT_FOUND)) {
					String l3Response = sendDataToL3DiscrepantProcessAttempt(mapper, checkId);
					updateAttemptHistory(checkId, getL3Status(l3Response), l3Response);
				} else if (clearList.size() == resultList.size()) {
					clearCheckIds.add(checkId);
				}
			}
		}
		if (CollectionUtils.isNotEmpty(clearCheckIds)) {
			String l3Response = sendAllClearDataToL3(mapper, clearCheckIds);
			String l3Status = getL3Status(l3Response);
			if (StringUtils.equalsIgnoreCase(l3Status, "success")) {
				onlineVerificationChecksRepository.updateL3StatusUsingCheckIdList(clearCheckIds);
			}
			for (String checkId : clearCheckIds) {
				updateAttemptHistory(checkId, l3Status, l3Response);
			}
		}
	}

	private String getL3Status(String l3Response) {
		if (StringUtils.equalsIgnoreCase(l3Response, L3_ERROR_RESPONSE)) {
			return "failed";
		} else {
			return "success";
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

	@Override
	public String sendAllClearDataToL3(ObjectMapper mapper, List<String> checkIdList) throws JsonProcessingException {

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
		caseReference.setNgStatus("Result Review - Clear");
		caseReference.setNgStatusDescription(RESULT_RECEIVED_WITH_NO_RECORDS);
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

	@Override
	public Map<String, String> createOnlineVerificationChecks(ObjectMapper mapper,
			List<OnlineVerificationChecksPOJO> onlineVerificationCheckList) {
		Long manualVerificationId = (long) 0;
		// Count the all Clear Result
		int countClear = 0;
		boolean sendMultiVerifyCheckId = false;
		List<String> checkIdList = new ArrayList<>();
		for (OnlineVerificationChecksPOJO onlineVerificationPOJO : onlineVerificationCheckList) {
			Optional<OnlineVerificationChecks> onlineVerificationCheck = onlineVerificationChecksRepository
					.findById(onlineVerificationPOJO.getOnlineVerificationCheckId());
			if (onlineVerificationCheck.isPresent()) {
				OnlineVerificationChecks newOnlineVerificationCheck = onlineVerificationCheck.get();
				logger.info("Value of Result : {}", newOnlineVerificationCheck.getResult());
				//logger.info("Value of API Name: {} {}", newOnlineVerificationCheck.getApiName(),newOnlineVerificationCheck.getSubComponentName());
				boolean sentToL3=newOnlineVerificationCheck.isSentToL3();
				manualVerificationId = newOnlineVerificationCheck.getOnlineManualVerificationId();
				if(!sentToL3) {
					checkIdList.add(newOnlineVerificationCheck.getCheckId());
					if(onlineVerificationPOJO.getResult()!=null) {
						if (StringUtils.equalsIgnoreCase(onlineVerificationPOJO.getResult(), CLEAR)) {
							countClear++;
						}
					}else {
						if (StringUtils.equalsIgnoreCase(newOnlineVerificationCheck.getResult(), CLEAR)) {
							countClear++;
						}
					}
				}
			}
		}
		// Take distinct check ID from checkId List
		List<String> distinctCheckIdList = checkIdList.stream().distinct().collect(Collectors.toList());
		logger.info("Size of Distinct CheckID List:{}", distinctCheckIdList.size());
		logger.info("Size of Distinct CheckID List:{}", checkIdList.size());

		if (checkIdList.size() == countClear) {
			sendMultiVerifyCheckId = true;
			// Call Function to Send All Check DATA as verify JSON
			String l3Response = null;
			try {
				l3Response = sendAllClearDataToL3(mapper, distinctCheckIdList);
			} catch (JsonProcessingException e) {
				logger.info("Exception occured in JSON parsing:{}", e.getMessage());
				// e.printStackTrace();
			}
			String l3Status = getL3Status(l3Response);
			if (StringUtils.equalsIgnoreCase(l3Status, "success")) {
				onlineVerificationChecksRepository.updateL3StatusUsingCheckIdList(distinctCheckIdList);
			}
			
			if (StringUtils.equalsIgnoreCase(l3Status, "success")) {
				onlineVerificationChecksRepository.updatedL3StatusUsingCheckIdList(distinctCheckIdList);
			}
			for (String checkId : distinctCheckIdList) {
				updateAttemptHistory(checkId, l3Status, l3Response);
			}
		}
		/*--------------Added Before this--------------------------------------------------*/
		if (!sendMultiVerifyCheckId) {
			for (OnlineVerificationChecksPOJO onlineVerification : onlineVerificationCheckList) {
				Optional<OnlineVerificationChecks> onlineVerificationCheck = onlineVerificationChecksRepository
						.findById(onlineVerification.getOnlineVerificationCheckId());
				try {
					manualVerificationId = processOnlineVerificationChecks(mapper, manualVerificationId,
							onlineVerificationCheck, onlineVerification);
				} catch (JsonProcessingException e) {
					logger.error("Exception occurred while sending to L3 : {}", e.getMessage());
					e.printStackTrace();
				}
			}
		}

		Map<String, String> result = new HashMap<>();

		if (manualVerificationId != 0) {
			Optional<OnlineManualVerification> onlineManualVerification = onlineManualVerificationRepository
					.findById(manualVerificationId);
			if (onlineManualVerification.isPresent()) {
				OnlineManualVerification newOnlineManualVerification = onlineManualVerification.get();
				newOnlineManualVerification.setStatus(PROCESSED);
				newOnlineManualVerification.setUpdatedTime(new Date().toString());
				onlineManualVerificationRepository.save(newOnlineManualVerification);
				result.put(RESULT, "Saved");
			}
			result.put(RESULT, "Not Saved");
		} else {
			result.put(RESULT, "Not Saved");
		}
		return result;
	}

	private Long processOnlineVerificationChecks(ObjectMapper mapper, Long manualVerificationId,
			Optional<OnlineVerificationChecks> onlineVerificationCheck, OnlineVerificationChecksPOJO onlineVerification)
			throws JsonProcessingException {
		if (onlineVerificationCheck.isPresent()) {
			OnlineVerificationChecks newOnlineVerificationCheck = onlineVerificationCheck.get();
			logger.info("Value of Result : {}", newOnlineVerificationCheck.getResult());
			logger.info("Value of API Name: {} {}", newOnlineVerificationCheck.getApiName(),
					newOnlineVerificationCheck.getSubComponentName());

			String result = newOnlineVerificationCheck.getResult() != null ? newOnlineVerificationCheck.getResult()
					: "";

			if (StringUtils.equalsIgnoreCase(result, MANUAL)) {
				newOnlineVerificationCheck.setResult(onlineVerification.getResult());
				newOnlineVerificationCheck.setUpdatedDate(new Date().toString());
				manualVerificationId = newOnlineVerificationCheck.getOnlineManualVerificationId();
				onlineVerificationChecksRepository.save(newOnlineVerificationCheck);
				result = onlineVerification.getResult();

				String checkId = newOnlineVerificationCheck.getCheckId();

				if (StringUtils.equalsIgnoreCase(result, CLEAR) && !newOnlineVerificationCheck.isSentToL3()) {
					String l3Response = sendDataToL3ClearOutcomeAttempt(mapper, checkId);
					String l3Status = getL3Status(l3Response);
					if (StringUtils.equalsIgnoreCase(l3Status, "success")) {
						newOnlineVerificationCheck.setSentToL3(true);
						onlineVerificationChecksRepository.save(newOnlineVerificationCheck);
					}
					createClearOutcomeAttempt(newOnlineVerificationCheck, getL3Status(l3Response), l3Response);
				} else if (StringUtils.equalsIgnoreCase(result, RESULT_FOUND)
						&& !newOnlineVerificationCheck.isSentToL3()) {
					String l3Response = sendDataToL3DiscrepantProcessAttempt(mapper, checkId);
					String l3Status = getL3Status(l3Response);
					if (StringUtils.equalsIgnoreCase(l3Status, "success")) {
						newOnlineVerificationCheck.setSentToL3(true);
						onlineVerificationChecksRepository.save(newOnlineVerificationCheck);
					}
					createDiscrepantProcessAttempt(newOnlineVerificationCheck, getL3Status(l3Response), l3Response);
				}
			} else {
				logger.info("Value of Result : {}", newOnlineVerificationCheck.getResult());
			}
		}
		return manualVerificationId;
	}

	private void createClearOutcomeAttempt(OnlineVerificationChecks onlineVerificationChecks, String l3Status,
			String l3Response) {
		String checkId = onlineVerificationChecks.getCheckId() != null ? onlineVerificationChecks.getCheckId() : "";

		List<CaseSpecificRecordDetail> caseSpecificRecordDetails = caseSpecificRecordDetailRepository
				.findByInstructionCheckId(checkId);

		if (CollectionUtils.isNotEmpty(caseSpecificRecordDetails)) {
			CaseSpecificRecordDetail caseSpecificRecordDetail = caseSpecificRecordDetails.get(0);
			Optional<CaseSpecificInfo> caseSpecificInfo = caseSpecificInfoRepository
					.findById(caseSpecificRecordDetail.getCaseSpecificId());

			if (caseSpecificInfo.isPresent()) {
				CaseSpecificInfo newCaseSpecificInfo = caseSpecificInfo.get();

				AttemptHistory attemptHistory = new AttemptHistory();
				attemptHistory.setAttemptStatusid((long) 67);
				attemptHistory.setAttemptDescription(RESULT_RECEIVED_WITH_NO_RECORDS);
				attemptHistory.setName(PRODUCT_NAME + onlineVerificationChecks.getSubComponentName() + APINAME
						+ onlineVerificationChecks.getApiName());
				attemptHistory.setCheckid(checkId);
				attemptHistory.setContactDate(new Date().toString());
				attemptHistory.setRequestid(caseSpecificRecordDetail.getCaseSpecificDetailId());
				attemptHistory.setFollowupId((long) 40);
				attemptHistory.setL3Response(l3Response);
				attemptHistory.setL3Status(l3Status);

				AttemptHistory newAttemptHistory = attemptMasterRepository.save(attemptHistory);

				AttemptStatusData attemptStatusData = new AttemptStatusData();
				attemptStatusData.setAttemptId(newAttemptHistory.getAttemptid());
				attemptStatusData.setDepositionId((long) 3);
				attemptStatusData.setEndstatusId((long) 40);
				attemptStatusData.setModeId((long) 14);
				attemptStatusDataRepository.save(attemptStatusData);

				VerificationEventStatus verificationEventStatus = new VerificationEventStatus();
				verificationEventStatus.setCheckId(checkId);
				verificationEventStatus.setEventName(ONLINE);
				verificationEventStatus.setEventType(AUTO);
				verificationEventStatus.setCaseNo(newCaseSpecificInfo.getCaseNumber());
				verificationEventStatus.setUser(SYSTEM);
				verificationEventStatus.setStatus("Day 0");
				verificationEventStatus.setRequestId(caseSpecificRecordDetail.getCaseSpecificDetailId());
				verificationEventStatusRepository.save(verificationEventStatus);
			}
		}
	}

	@Override
	public void createDiscrepantProcessAttempt(OnlineVerificationChecks onlineVerificationChecks, String l3Status,
			String l3Response) {
		String checkId = onlineVerificationChecks.getCheckId() != null ? onlineVerificationChecks.getCheckId() : "";

		List<CaseSpecificRecordDetail> caseSpecificRecordDetails = caseSpecificRecordDetailRepository
				.findByInstructionCheckId(checkId);

		if (CollectionUtils.isNotEmpty(caseSpecificRecordDetails)) {
			CaseSpecificRecordDetail caseSpecificRecordDetail = caseSpecificRecordDetails.get(0);
			Optional<CaseSpecificInfo> caseSpecificInfo = caseSpecificInfoRepository
					.findById(caseSpecificRecordDetail.getCaseSpecificId());

			if (caseSpecificInfo.isPresent()) {
				CaseSpecificInfo newCaseSpecificInfo = caseSpecificInfo.get();
				AttemptHistory attemptHistory = new AttemptHistory();
				attemptHistory.setAttemptDescription(RECORD_FOUND);
				attemptHistory.setAttemptStatusid((long) 69);
				attemptHistory.setName(PRODUCT_NAME + onlineVerificationChecks.getSubComponentName() + APINAME
						+ onlineVerificationChecks.getApiName());
				attemptHistory.setCheckid(checkId);
				attemptHistory.setFollowupId((long) 40);
				attemptHistory.setRequestid(caseSpecificRecordDetail.getCaseSpecificDetailId());
				attemptHistory.setExecutiveSummary(RECORD_FOUND);
				attemptHistory.setContactDate(new Date().toString());
				attemptHistory.setL3Response(l3Response);
				attemptHistory.setL3Status(l3Status);

				AttemptHistory newAttemptHistory = attemptMasterRepository.save(attemptHistory);

				AttemptStatusData attemptStatusData = new AttemptStatusData();
				attemptStatusData.setAttemptId(newAttemptHistory.getAttemptid());
				attemptStatusData.setDepositionId((long) 14);
				attemptStatusData.setEndstatusId((long) 40);
				attemptStatusData.setModeId((long) 14);
				attemptStatusDataRepository.save(attemptStatusData);

				VerificationEventStatus verificationEventStatus = new VerificationEventStatus();
				verificationEventStatus.setCheckId(checkId);
				verificationEventStatus.setEventName(ONLINE);
				verificationEventStatus.setEventType(AUTO);
				verificationEventStatus.setCaseNo(newCaseSpecificInfo.getCaseNumber());
				verificationEventStatus.setUser(SYSTEM);
				verificationEventStatus.setStatus("Day 0");
				verificationEventStatus.setRequestId(caseSpecificRecordDetail.getCaseSpecificDetailId());
				verificationEventStatusRepository.save(verificationEventStatus);
			}
		}
	}

	@Override
	public String sendDataToL3ClearOutcomeAttempt(ObjectMapper mapper, String checkId) throws JsonProcessingException {

		SimpleDateFormat formatter = new SimpleDateFormat(DD_MMM_YYYY);
		Date todaysDate = new Date();
		String todaysDateStr = formatter.format(todaysDate);
		String componentName = DATABASE;

		L3CaseDetails l3CaseDetails = onlineVerificationChecksRepository.getAllL3Cases(checkId, componentName);

		TaskSpecs taskSpecs = new TaskSpecs();
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

		caseReference.setCheckId(checkId);
		caseReference.setNgStatus("Result Review - Clear");
		caseReference.setNgStatusDescription(RESULT_RECEIVED_WITH_NO_RECORDS);
		caseReference.setComponentName(componentName);

		taskSpecs.setCaseReference(caseReference);
		taskSpecs.setCheckVerification(getClearOutCheckVerification(todaysDateStr, componentName));

		taskSpecs.setQuestionaire(new ArrayList<>());

		FileUpload fileUpload = new FileUpload();
		fileUpload.setVerificationReplyDocument(new ArrayList<>());
		taskSpecs.setFileUpload(fileUpload);

		String taskSpecsStr = mapper.writeValueAsString(taskSpecs);
		logger.info(L3_VERIFICATION_JSON, taskSpecsStr);

		String l3VerifyResponse = onlineApiService.sendDataToL3Post(verificationStatusL3Url, taskSpecsStr, null);

		if (l3VerifyResponse == null) {
			return L3_ERROR_RESPONSE;
		}
		return l3VerifyResponse;
	}

	private CheckVerification getClearOutCheckVerification(String todaysDateStr, String componentName) {
		CheckVerification checkVerification = new CheckVerification();

		checkVerification.setCountry(INDIA);
		checkVerification.setExecutiveSummaryComments("No Record");
		checkVerification.setReportComments("");
		checkVerification.setResultCode("");
		checkVerification.setEmailId("");
		checkVerification.setVerifierName("");
		checkVerification.setGeneralRemarks("");
		checkVerification.setModeOfVerification(ONLINE);
		checkVerification.setVerifiedDate("");
		checkVerification.setAction(VERIFY_CHECKS);
		if (StringUtils.equalsAnyIgnoreCase(componentName, DATABASE)) {
			checkVerification.setSubAction("databaseVerification");
		} else {
			checkVerification.setSubAction(VERIFY_CHECKS);
		}
		checkVerification.setActionCode("");
		checkVerification.setComponentName(componentName);
		checkVerification.setAttempts("");
		checkVerification.setDepartmentName("");
		checkVerification.setKeyWords("");
		checkVerification.setCost("");
		checkVerification.setIsThisAVerificationAttempt("No");
		checkVerification.setInternalNotes(RESULT_RECEIVED_WITH_NO_RECORDS);
		checkVerification.setDateVerificationCompleted(todaysDateStr);
		checkVerification.setDisposition("");
		checkVerification.setExpectedClosureDate("");
		checkVerification.setEndStatusOfTheVerification(CLEAR);
		checkVerification.setVerifierDesignation("");
		checkVerification.setFollowUpDateAndTimes("");
		checkVerification.setVerifierNumber("");
		return checkVerification;
	}

	@Override
	public String sendDataToL3DiscrepantProcessAttempt(ObjectMapper mapper, String checkId)
			throws JsonProcessingException {

		SimpleDateFormat formatter = new SimpleDateFormat(DD_MMM_YYYY);
		Date todaysDate = new Date();
		String todaysDateStr = formatter.format(todaysDate);
		String componentName = DATABASE;

		L3CaseDetails l3CaseDetails = onlineVerificationChecksRepository.getAllL3Cases(checkId, componentName);

		TaskSpecs taskSpecs = new TaskSpecs();
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

		caseReference.setCheckId(checkId);
		caseReference.setNgStatus("Result Review - Record Found");
		caseReference.setNgStatusDescription(RECORD_FOUND);
		caseReference.setComponentName(componentName);

		taskSpecs.setCaseReference(caseReference);
		taskSpecs.setCheckVerification(getDiscrepantCheckVerification(todaysDateStr, componentName));

		taskSpecs.setQuestionaire(new ArrayList<>());

		FileUpload fileUpload = new FileUpload();
		fileUpload.setVerificationReplyDocument(new ArrayList<>());
		taskSpecs.setFileUpload(fileUpload);
		String taskSpecsStr = mapper.writeValueAsString(taskSpecs);
		logger.info(L3_VERIFICATION_JSON, taskSpecsStr);

		String l3VerifyResponse = onlineApiService.sendDataToL3Post(verificationStatusL3Url, taskSpecsStr, null);

		if (l3VerifyResponse == null) {
			return L3_ERROR_RESPONSE;
		}
		return l3VerifyResponse;
	}

	private CheckVerification getDiscrepantCheckVerification(String todaysDateStr, String componentName) {
		CheckVerification checkVerification = new CheckVerification();

		checkVerification.setCountry(INDIA);
		checkVerification.setExecutiveSummaryComments("Possible Hit");
		checkVerification.setReportComments("");
		checkVerification.setResultCode("");
		checkVerification.setEmailId("");
		checkVerification.setVerifierName("");
		checkVerification.setGeneralRemarks("");
		checkVerification.setModeOfVerification(ONLINE);
		checkVerification.setVerifiedDate("");
		checkVerification.setAction(VERIFY_CHECKS);
		if (StringUtils.equalsAnyIgnoreCase(componentName, DATABASE)) {
			checkVerification.setSubAction("databaseVerification");
		} else {
			checkVerification.setSubAction(VERIFY_CHECKS);
		}
		checkVerification.setActionCode("");
		checkVerification.setComponentName(componentName);
		checkVerification.setAttempts("");
		checkVerification.setDepartmentName("");
		checkVerification.setKeyWords("");
		checkVerification.setCost("");
		checkVerification.setIsThisAVerificationAttempt("No");
		checkVerification.setInternalNotes(RECORD_FOUND);
		checkVerification.setDateVerificationCompleted(todaysDateStr);
		checkVerification.setDisposition("");
		checkVerification.setExpectedClosureDate("");
		checkVerification.setEndStatusOfTheVerification("Possible Hit");
		checkVerification.setVerifierDesignation("");
		checkVerification.setFollowUpDateAndTimes("");
		checkVerification.setVerifierNumber("");
		return checkVerification;
	}

	@Override
	public Map<String, String> rerunOnlineRequests(ObjectMapper mapper, JsonNode requestBody) {
		Map<String, String> retrunMap = new HashMap<>();
		retrunMap.put("status", "fail");

		String onlineVerificationCheckId = requestBody.has("onlineVerificationCheckId")
				? requestBody.get("onlineVerificationCheckId").asText()
				: "";
		logger.info("onlineVerificationCheckId : {} ", onlineVerificationCheckId);
		logger.info("Long {}", Long.parseLong(onlineVerificationCheckId));
		List<OnlineVerificationChecks> onlineVerificationChecksList = onlineVerificationChecksRepository
				.getOnlineVerificationChecksById(Long.parseLong(onlineVerificationCheckId));

		List<OnlineVerificationChecks> newOnlineVerificationChecksList = new ArrayList<>();

		for (OnlineVerificationChecks onlineVerificationChecks : onlineVerificationChecksList) {
			try {
				getInputFileForManupatra(mapper, onlineVerificationChecks, requestBody);
			} catch (JsonProcessingException e) {
				logger.error("Exception while processing secondary name in rerun api : {}", e.getMessage());
			}
			onlineVerificationChecks.setPendingStatus("1");
			newOnlineVerificationChecksList.add(onlineVerificationChecks);
		}
		if (CollectionUtils.isNotEmpty(newOnlineVerificationChecksList)) {
			onlineVerificationChecksRepository.saveAll(newOnlineVerificationChecksList);
		}
		String onlineVerificationChecksOptListStr;
		try {
			onlineVerificationChecksOptListStr = mapper.writeValueAsString(newOnlineVerificationChecksList);
			logger.info("onlineVerificationChecksOptListStr: {}", onlineVerificationChecksOptListStr);
			String res = onlineApiService.sendDataToPost(onlineReRunApiRestURL, onlineVerificationChecksOptListStr);
			if (res != null) {
				retrunMap.replace("status", "success");
			}
		} catch (JsonProcessingException e) {
			logger.error("Exception while processing rerun api request : {}", e.getMessage());
			e.printStackTrace();
		}

		return retrunMap;
	}

	private void getInputFileForManupatra(ObjectMapper mapper, OnlineVerificationChecks onlineVerificationChecks,
			JsonNode requestBody) throws JsonProcessingException {
		String secondaryName = "";
		if (StringUtils.equalsIgnoreCase(onlineVerificationChecks.getApiName(), "Manupatra")) {
			secondaryName = requestBody.has("secondaryName") ? requestBody.get("secondaryName").asText() : "";
			String inputFile = onlineVerificationChecks.getInputFile() != null ? onlineVerificationChecks.getInputFile()
					: "{}";
			ObjectNode inputFileNode = (ObjectNode) mapper.readTree(inputFile);
			if (!StringUtils.isEmpty(secondaryName)) {
				if (inputFileNode.has("secondary")) {
					if (inputFileNode.get("secondary").has("formattedName")) {
						((ObjectNode) inputFileNode.get("secondary")).put("formattedName", secondaryName);
					} else {
						String inputFileNodeStr = StringUtils.isEmpty(inputFileNode.get("secondary").asText()) ? "{}"
								: inputFileNode.get("secondary").asText();
						logger.info("{}", inputFileNodeStr);
						ObjectNode secondaryNode = (ObjectNode) mapper.readTree(inputFileNodeStr);
						secondaryNode.put("formattedName", secondaryName);
						inputFileNode.set("secondary", secondaryNode);
					}
				} else {
					ObjectNode nameNode = mapper.createObjectNode();
					nameNode.put("formattedName", secondaryName);
					inputFileNode.set("secondary", nameNode);
				}
				inputFile = mapper.writeValueAsString(inputFileNode);
				onlineVerificationChecks.setInputFile(inputFile);
				onlineVerificationChecks.setPendingStatus("1");
			}
			if (StringUtils.isNotEmpty(secondaryName)) {
				onlineManualVerificationRepository.updateSecondaryName(secondaryName,
						onlineVerificationChecks.getOnlineManualVerificationId());
			}
		}

	}

	public Map<String, String> createOnlineVerificationChecksAllClear(ObjectMapper mapper,
			List<OnlineVerificationChecksPOJO> onlineVerificationCheckList) {
		Long manualVerificationId = (long) 0;

		for (OnlineVerificationChecksPOJO onlineVerification : onlineVerificationCheckList) {
			Optional<OnlineVerificationChecks> onlineVerificationCheck = onlineVerificationChecksRepository
					.findById(onlineVerification.getOnlineVerificationCheckId());
			if (onlineVerificationCheck.isPresent()) {

			}
			try {
				manualVerificationId = processOnlineVerificationChecks(mapper, manualVerificationId,
						onlineVerificationCheck, onlineVerification);
			} catch (JsonProcessingException e) {
				logger.error("Exception occurred while sending to L3 : {}", e.getMessage());
				e.printStackTrace();
			}
		}
		Map<String, String> result = new HashMap<>();

		if (manualVerificationId != 0) {
			Optional<OnlineManualVerification> onlineManualVerification = onlineManualVerificationRepository
					.findById(manualVerificationId);
			if (onlineManualVerification.isPresent()) {
				OnlineManualVerification newOnlineManualVerification = onlineManualVerification.get();
				newOnlineManualVerification.setStatus(PROCESSED);
				newOnlineManualVerification.setUpdatedTime(new Date().toString());
				onlineManualVerificationRepository.save(newOnlineManualVerification);
				result.put(RESULT, "Saved");
			}
			result.put(RESULT, "Not Saved");
		} else {
			result.put(RESULT, "Not Saved");
		}
		return result;
	}
}
