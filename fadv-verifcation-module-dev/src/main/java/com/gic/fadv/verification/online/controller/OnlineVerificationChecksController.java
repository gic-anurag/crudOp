package com.gic.fadv.verification.online.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.verification.attempts.service.L3APIService;
import com.gic.fadv.verification.online.model.CaseReference;
import com.gic.fadv.verification.online.model.CheckVerification;
import com.gic.fadv.verification.online.model.FileUpload;
import com.gic.fadv.verification.online.model.OnlineVerificationChecks;
import com.gic.fadv.verification.online.model.TaskSpecs;
import com.gic.fadv.verification.online.repository.OnlineVerificationChecksRepository;
import com.gic.fadv.verification.online.service.OnlineVerificationChecksService;
import com.gic.fadv.verification.pojo.OnlineChecksLogPOJO;
import com.gic.fadv.verification.pojo.OnlineVerificationChecksPOJO;
import com.gic.fadv.verification.pojo.QuestionnairePOJO;
import com.gic.fadv.verification.pojo.VerifyChecksPOJO;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class OnlineVerificationChecksController {

	@Autowired
	private OnlineVerificationChecksRepository onlineVerificationChecksRepository;

	@Autowired
	private OnlineVerificationChecksService onlineVerificationChecksService;

	@Autowired
	private L3APIService l3APIService;
	@Value("${attemptstatusdata.rest.url}")
	private String attemptStatusDataRestUrl;
	@Value("${attempthistory.rest.url}")
	private String attemptHistoryRestUrl;

	@Value("${online.rerun-api.rest.url}")
	private String onlineReRunApiRestURL;

	private static final Logger logger = LoggerFactory.getLogger(OnlineVerificationChecksController.class);

	@GetMapping("/get-retry-results")
	public List<OnlineVerificationChecks> getRetryVerifyChecks() {
		List<String> status = new ArrayList<>();
		status.add("Manual");
		status.add("Clear");
		status.add("Record Found");
		return onlineVerificationChecksRepository.getVerifyChecksByResult(status);
	}

	@PostMapping("/update-verify-checks")
	public void updateVerifyChecks(@RequestBody OnlineVerificationChecks onlineVerificationChecks) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		String checkId = onlineVerificationChecks.getCheckId() != null ? onlineVerificationChecks.getCheckId() : "";
		String finalResult = onlineVerificationChecks.getResult() != null ? onlineVerificationChecks.getResult() : "";
		String isPending = onlineVerificationChecks.getPendingStatus() != null
				? onlineVerificationChecks.getPendingStatus()
				: "";
		String retryNo = onlineVerificationChecks.getRetryNo() != null ? onlineVerificationChecks.getRetryNo() : "";
		String verifyId = onlineVerificationChecks.getVerifyId() != null ? onlineVerificationChecks.getVerifyId() : "";
		String matchedIdentifier = onlineVerificationChecks.getMatchedIdentifiers() != null
				? onlineVerificationChecks.getMatchedIdentifiers()
				: "";
		String outputFile = onlineVerificationChecks.getOutputFile() != null ? onlineVerificationChecks.getOutputFile()
				: "";
		String updateDate = new Date().toString();
		String apiName = onlineVerificationChecks.getApiName() != null ? onlineVerificationChecks.getApiName() : "";
		if (!StringUtils.isEmpty(apiName) && !StringUtils.isEmpty(checkId)) {
			onlineVerificationChecksRepository.updateVerifyChecks(checkId, finalResult, updateDate, isPending, retryNo,
					verifyId, matchedIdentifier, outputFile, apiName);
		}

	}

	@GetMapping("/online-verification-checks")
	public List<OnlineVerificationChecks> getAllOnlineVerificationChecks() {
		return onlineVerificationChecksRepository.findAll();
	}
	
	@GetMapping("/onlineverificationchecks/{id}")
	public OnlineVerificationChecks getOnlineVerificationChecksByOnlineCheckId(
			@PathVariable(value = "id") Long onlineManualVerificationId) {		
		Optional<OnlineVerificationChecks> onlineVerificationChecksOpt = onlineVerificationChecksRepository
				.findById(onlineManualVerificationId);
		return onlineVerificationChecksOpt.isPresent() ? onlineVerificationChecksOpt.get() : new OnlineVerificationChecks();
	}

//	@GetMapping("/online-verification-checks/{id}")
//	public List<OnlineVerificationChecks> getOnlineVerificationChecksById(
//			@PathVariable(value = "id") Long onlineManualVerificationId) {
//		List<OnlineVerificationChecks> onlineVerificationChecksList = onlineVerificationChecksRepository
//				.findByOnlineManualVerificationIdOrderByApiNameAsc(onlineManualVerificationId);
//		List<OnlineVerificationChecks> newOnlineVerificationChecksList = new ArrayList<>();
//
//		ObjectMapper mapper = new ObjectMapper();
//		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//		for (OnlineVerificationChecks onlineVerificationChecks : onlineVerificationChecksList) {
//
//			try {
//				if (StringUtils.equalsIgnoreCase(onlineVerificationChecks.getApiName(), "Manupatra")
//						&& StringUtils.isNotEmpty(onlineVerificationChecks.getOutputFile())) {
//					ArrayNode objArr = onlineVerificationChecksService.processOutputFile(mapper,
//							onlineVerificationChecks.getOutputFile());
//					String resultStr = mapper.writeValueAsString(objArr);
//					onlineVerificationChecks.setOutputFile(resultStr);
//				}
//			} catch (JsonProcessingException e) {
//				logger.error("Exception while mapping output file : {}", e.getMessage());
//				e.printStackTrace();
//			}
//			newOnlineVerificationChecksList.add(onlineVerificationChecks);
//		}
//		return newOnlineVerificationChecksList;
//	}

	@GetMapping("/online-verification-checks/{id}")
	public List<OnlineVerificationChecks> getOnlineVerificationChecksById(
			@PathVariable(value = "id") Long onlineManualVerificationId) {
		return onlineVerificationChecksRepository
				.findByOnlineManualVerificationIdOrderByApiNameAsc(onlineManualVerificationId);
	}

	@PostMapping("/process-manupatra-output")
	public ArrayNode processManupatraOutput(@RequestBody String outputNode) throws JsonProcessingException {

		List<OnlineVerificationChecks> onlineVerificationCheckList = onlineVerificationChecksRepository
				.findByApiName("Manupatra");

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		return onlineVerificationChecksService.processManuptraOutput(mapper, onlineVerificationCheckList);

	}

	@PostMapping("/get-verify-checks-log")
	public ArrayNode getVerifyChecksLog(@RequestBody JsonNode requestBody) throws JsonProcessingException {

		String caseNumber = requestBody.has("caseNumber") ? requestBody.get("caseNumber").asText() : "";
		String crnNo = requestBody.has("CRNNumber") ? requestBody.get("CRNNumber").asText() : "";
		String fromDate = requestBody.has("fromDate") ? requestBody.get("fromDate").asText() : "";
		String toDate = requestBody.has("toDate") ? requestBody.get("toDate").asText() : "";

		toDate = StringUtils.isEmpty(toDate) ? fromDate : toDate;

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		ArrayNode resultArrayNode = mapper.createArrayNode();

		if (!StringUtils.isEmpty(fromDate) && !StringUtils.isEmpty(toDate)) {

			List<OnlineChecksLogPOJO> onlineChecksLogPOJOs = onlineVerificationChecksRepository
					.getVerifyChecksLog(fromDate, toDate, caseNumber, crnNo);

			List<String> checkIdList = onlineChecksLogPOJOs.stream().map(OnlineChecksLogPOJO::getCheckId)
					.collect(Collectors.toList());
			checkIdList = new ArrayList<>(new HashSet<>(checkIdList));

			for (String checkId : checkIdList) {
				List<OnlineChecksLogPOJO> tempOnlineChecksLogPOJOs = onlineChecksLogPOJOs.stream()
						.filter(onlineChecksLogPOJO -> onlineChecksLogPOJO.getCheckId().equals(checkId))
						.collect(Collectors.toList());
				ArrayNode apiNameArray = mapper.createArrayNode();
				List<String> finalResultList = new ArrayList<>();
				ObjectNode resultNode = mapper.createObjectNode();

				caseNumber = "";
				String candidateName = "";
				String clientName = "";
				String sbu = "";
				String packageName = "";
				crnNo = "";
				String checkCreatedDate = "";

				for (OnlineChecksLogPOJO onlineChecksLogPOJO : tempOnlineChecksLogPOJOs) {
					finalResultList.add(onlineChecksLogPOJO.getResult());
					/*
					 * if (!StringUtils.equalsIgnoreCase(onlineChecksLogPOJO.getApiName(),
					 * "Worldcheck")) { finalResultList.add(onlineChecksLogPOJO.getResult()); }
					 */
					ObjectNode apiNames = mapper.createObjectNode();
					apiNames.put("apiName", onlineChecksLogPOJO.getApiName());
					apiNames.put("intialResult", onlineChecksLogPOJO.getInitialResult());
					apiNameArray.add(apiNames);
					crnNo = StringUtils.isEmpty(crnNo) ? onlineChecksLogPOJO.getCrnNo() : crnNo;
					caseNumber = StringUtils.isEmpty(caseNumber) ? onlineChecksLogPOJO.getCaseNumber() : caseNumber;
					candidateName = StringUtils.isEmpty(candidateName) ? onlineChecksLogPOJO.getCandidateName()
							: candidateName;
					clientName = StringUtils.isEmpty(clientName) ? onlineChecksLogPOJO.getClientName() : clientName;
					sbu = StringUtils.isEmpty(sbu) ? onlineChecksLogPOJO.getSbu() : sbu;
					packageName = StringUtils.isEmpty(packageName) ? onlineChecksLogPOJO.getPackageName() : packageName;
					checkCreatedDate = StringUtils.isEmpty(checkCreatedDate) ? onlineChecksLogPOJO.getCreatedDate()
							: checkCreatedDate;
				}

				String apiNamesStr = mapper.writeValueAsString(apiNameArray);

				resultNode.put("caseNumber", caseNumber);
				resultNode.put("crnNo", crnNo);
				resultNode.put("candidateName", candidateName);
				resultNode.put("clientName", clientName);
				resultNode.put("sbu", sbu);
				resultNode.put("packageName", packageName);
				resultNode.put("checkId", checkId);
				resultNode.put("initialResult", apiNamesStr);
				resultNode.put("checkCreatedDate", checkCreatedDate);

				if (!finalResultList.isEmpty()) {
					List<String> clearList = finalResultList.stream().filter(e -> e.equalsIgnoreCase("Clear"))
							.collect(Collectors.toList());
					if (finalResultList.contains("Manual")) {
						resultNode.put("result", "Manual");
					} else if (finalResultList.contains("Result Found")) {
						resultNode.put("result", "Result Found");
					} else if (clearList.size() == finalResultList.size()) {
						resultNode.put("result", "Clear");
					} else {
						resultNode.put("result", "Processing request");
					}
				}
				resultArrayNode.add(resultNode);
			}
		}

		return resultArrayNode;
	}

	@GetMapping("/online-verification-checks-2/{checkId}")
	public TaskSpecs getOnlineVerificationChecks2ById(@PathVariable(value = "checkId") String checkId)
			throws JsonMappingException, JsonProcessingException {
		List<VerifyChecksPOJO> verifyChecksPOJOs = onlineVerificationChecksRepository.findAllVerifyChecks(checkId);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

//		Metadata metadata = new Metadata();
//		metadata.setProcessName("CSPi Case Creation");
//		metadata.setProcessId("3ba167f1-t558-8a95-mx19-9y97d303dt2f");
//		metadata.setStageId("9154f5g3-8g19-ff3d-7e53-g068h9n81ct9");
//		metadata.setRequestType("command");
//		metadata.setTask("verifyChecks");
//		metadata.setTaskDesc("Create CSPi MRL / Cost / Verification");
//		metadata.setTaskGroupId("803175e2-9b15-4f0c-9381-e084b21c0c08");
//		metadata.setRequestDate("1/4/2020 8:00:12 PM");
//		metadata.setRequestId("9aa167d1-2e5a-4a25-ac1e-fe97d706df2f");
//		metadata.setVersion("1.0.0");
//		metadata.setAttempt("1");
//		metadata.setMultiTask("No");
//		metadata.setRequestAuthToken("jwt.token");
//		
//		Datum datum = new Datum();
//		datum.setTaskName("verifyChecks");
//		datum.setTaskId("803175e2-9b15-4f0c-9381-e084b21c0c11");
//		datum.setTaskSerialNo("1");

		TaskSpecs taskSpecs = new TaskSpecs();

		if (!verifyChecksPOJOs.isEmpty()) {

			VerifyChecksPOJO verifyChecksPOJO = verifyChecksPOJOs.get(0);

			String expectedClosureDateStr = verifyChecksPOJO.getExpectedClosureDate() != null
					? verifyChecksPOJO.getExpectedClosureDate()
					: "";
			String followUpDateStr = verifyChecksPOJO.getFollowUpDate() != null ? verifyChecksPOJO.getFollowUpDate()
					: "";

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

			// Creating the ObjectMapper object

			CaseReference caseReference = mapper.readValue(verifyChecksPOJO.getCaseReference(), CaseReference.class);
			caseReference.setNgStatus(verifyChecksPOJO.getNgStatus());
			caseReference.setNgStatusDescription(verifyChecksPOJO.getNgStatusDescription());
			caseReference.setCheckId(checkId);
			caseReference.setComponentName(verifyChecksPOJO.getComponentName());
			caseReference.setProductName(verifyChecksPOJO.getProductName());
			caseReference.setSbuName(verifyChecksPOJO.getSbuName());
			caseReference.setPackageName(verifyChecksPOJO.getPackageName());

			CheckVerification checkVerification = new CheckVerification();

			checkVerification.setCountry("India");
			checkVerification.setExecutiveSummaryComments(verifyChecksPOJO.getExecutiveSummaryComments());
			checkVerification.setReportComments("");
			checkVerification.setResultCode("");
			if (verifyChecksPOJO.getEmailId() != null) {
				checkVerification.setEmailId(verifyChecksPOJO.getEmailId());
			} else {
				checkVerification.setEmailId("");
			}
			checkVerification.setVerifierName(verifyChecksPOJO.getVerifierName());
			checkVerification.setGeneralRemarks("");
			checkVerification.setModeOfVerification(verifyChecksPOJO.getModeOfVerification());
			checkVerification.setVerifiedDate("");
			checkVerification.setAction("verifyChecks");
			if (StringUtils.equalsAnyIgnoreCase(verifyChecksPOJO.getComponentName(), "Database")) {
				checkVerification.setSubAction("databaseVerification");
			} else {
				checkVerification.setSubAction("verifyChecks");
			}
			checkVerification.setActionCode("");
			checkVerification.setComponentName(verifyChecksPOJO.getComponentName());
			if (StringUtils.equalsIgnoreCase(verifyChecksPOJO.getVerificationAttemptType(), "Valid")) {
				checkVerification.setIsThisAVerificationAttempt("Yes");
				checkVerification.setAttempts("Internal");
			} else {
				checkVerification.setIsThisAVerificationAttempt("No");
				checkVerification.setAttempts("");
			}
			checkVerification.setDepartmentName("");
			checkVerification.setKeyWords("");
			checkVerification.setCost("");
			checkVerification.setInternalNotes(verifyChecksPOJO.getInternalNotes());
			if (StringUtils.equalsIgnoreCase(verifyChecksPOJO.getNgStatus(), "F1")) {
				checkVerification.setDateVerificationCompleted("");
			} else {
				checkVerification.setDateVerificationCompleted(todaysDateStr);
			}
			checkVerification.setDisposition(verifyChecksPOJO.getDisposition());
			checkVerification.setExpectedClosureDate(expectedClosureDateStr);
			checkVerification.setEndStatusOfTheVerification(verifyChecksPOJO.getEndStatusOfTheVerification());
			checkVerification.setVerifierDesignation(verifyChecksPOJO.getVerifierDesignation());
			checkVerification.setFollowUpDateAndTimes(followUpDateStr);
			checkVerification.setVerifierNumber(verifyChecksPOJO.getVerifierNumber());

			taskSpecs.setCaseReference(caseReference);
			taskSpecs.setCheckVerification(checkVerification);

			List<QuestionnairePOJO> questionnairePOJOs = onlineVerificationChecksRepository
					.findAllQuestionnaire(checkId);
			if (!questionnairePOJOs.isEmpty()) {
				taskSpecs.setQuestionaire(questionnairePOJOs);
			} else {
				taskSpecs.setQuestionaire(new ArrayList<>());
			}
			FileUpload fileUpload = new FileUpload();
			fileUpload.setVerificationReplyDocument(new ArrayList<>());
			taskSpecs.setFileUpload(fileUpload);
		} else {
			taskSpecs.setCaseReference(new CaseReference());
			taskSpecs.setCheckVerification(new CheckVerification());
			taskSpecs.setQuestionaire(new ArrayList<>());
			FileUpload fileUpload = new FileUpload();
			fileUpload.setVerificationReplyDocument(new ArrayList<>());
			taskSpecs.setFileUpload(fileUpload);
		}
//		datum.setTaskSpecs(taskSpecs);
//		
//		List<Datum> datums = new ArrayList<>();
//		datums.add(datum);
//		
//		VerificationStatus verificationStatus = new VerificationStatus();
//		verificationStatus.setData(datums);
//		verificationStatus.setMetadata(metadata);

		return taskSpecs;
	}
	/*
	 * @PostMapping("/online-verification-checks") public OnlineVerificationChecks
	 * createAttemptDeposition(@Valid @RequestBody OnlineVerificationChecks
	 * onlineVerificationChecks) {
	 * if(onlineVerificationChecks.getOnlineManualVerificationId()!=null) { //Logic
	 * for Update the Record onlineVerificationChecks.setOnlineManualVerificationId(
	 * onlineVerificationChecks.getOnlineVerificationCheckId());
	 * onlineVerificationChecks.setOnlineManualVerificationId(
	 * onlineVerificationChecks.getOnlineManualVerificationId());
	 * onlineVerificationChecks.setCheckId(onlineVerificationChecks.getCheckId());
	 * onlineVerificationChecks.setApiName(onlineVerificationChecks.getApiName());
	 * onlineVerificationChecks.setResult(onlineVerificationChecks.getInitialResult(
	 * )); onlineVerificationChecks.setMatchedIdentifiers(onlineVerificationChecks.
	 * getMatchedIdentifiers());
	 * onlineVerificationChecks.setInputFile(onlineVerificationChecks.getInputFile()
	 * );
	 * onlineVerificationChecks.setOutputFile(onlineVerificationChecks.getOutputFile
	 * ()); onlineVerificationChecks.setUpdatedDate(onlineVerificationChecks.
	 * getUpdatedDate()); }else { //Logic for Save the Record
	 * onlineVerificationChecks.setOnlineManualVerificationId(
	 * onlineVerificationChecks.getOnlineManualVerificationId());
	 * onlineVerificationChecks.setCheckId(onlineVerificationChecks.getCheckId());
	 * onlineVerificationChecks.setApiName(onlineVerificationChecks.getApiName());
	 * onlineVerificationChecks.setResult(onlineVerificationChecks.getInitialResult(
	 * )); onlineVerificationChecks.setMatchedIdentifiers(onlineVerificationChecks.
	 * getMatchedIdentifiers());
	 * onlineVerificationChecks.setInputFile(onlineVerificationChecks.getInputFile()
	 * );
	 * onlineVerificationChecks.setOutputFile(onlineVerificationChecks.getOutputFile
	 * ()); onlineVerificationChecks.setUpdatedDate(onlineVerificationChecks.
	 * getUpdatedDate()); onlineVerificationChecks.setCreatedDate(new
	 * Date().toString()); } return
	 * onlineVerificationChecksRepository.save(onlineVerificationChecks); }
	 */

	@PostMapping("/online-verification-checks")
	public Map<String, String> updateOnlineVerificationChecks(
			@Valid @RequestBody List<OnlineVerificationChecksPOJO> onlineVerificationChecks) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return onlineVerificationChecksService.createOnlineVerificationChecks(mapper, onlineVerificationChecks);
	}

	@PostMapping("/send-to-l3")
	public void sendDataToL3UsingCheckId(@RequestBody List<String> checkIdList) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		onlineVerificationChecksService.processL3VerifyCheckIds(mapper, checkIdList);
	}

	@PostMapping("/rerun-api")
	public Map<String, String> rerunOnlineApis(@RequestBody JsonNode requestBody) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return onlineVerificationChecksService.rerunOnlineRequests(mapper, requestBody);
	}

	@PostMapping("/rerun-api-bak")
	public Map<String, String> rerunChecks(@RequestBody JsonNode requestBody) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String onlineVerificationCheckId = requestBody.has("onlineVerificationCheckId")
				? requestBody.get("onlineVerificationCheckId").asText()
				: "";
		logger.info("onlineVerificationCheckId : {} ", onlineVerificationCheckId);
		logger.info("Long {}", Long.parseLong(onlineVerificationCheckId));
		Optional<OnlineVerificationChecks> onlineVerificationChecksOptList = onlineVerificationChecksRepository
				.findById(Long.parseLong(onlineVerificationCheckId));
		Map<String, String> retrunMap = new HashMap<>();
		if (onlineVerificationChecksOptList.isPresent()) {
			OnlineVerificationChecks onlineVerificationChecks = onlineVerificationChecksOptList.get();
			String secondaryName = "";
			if (StringUtils.equalsIgnoreCase(onlineVerificationChecks.getApiName(), "Manupatra")) {
				secondaryName = requestBody.has("secondaryName") ? requestBody.get("secondaryName").asText() : "";
				String inputFile = onlineVerificationChecks.getInputFile() != null
						? onlineVerificationChecks.getInputFile()
						: "{}";
				ObjectNode inputFileNode = (ObjectNode) mapper.readTree(inputFile);
				if (!StringUtils.isEmpty(secondaryName)) {
					if (inputFileNode.has("secondary")) {
						if (inputFileNode.get("secondary").has("formattedName")) {
							((ObjectNode) inputFileNode.get("secondary")).put("formattedName", secondaryName);
						} else {
							String inputFileNodeStr = StringUtils.isEmpty(inputFileNode.get("secondary").asText())
									? "{}"
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
					Long manualVerifyId = onlineVerificationChecks.getOnlineManualVerificationId();
					List<OnlineVerificationChecks> onlineVerificationChecksList = onlineVerificationChecksRepository
							.findByOnlineManualVerificationIdAndApiName(manualVerifyId, "Manupatra");
					for (OnlineVerificationChecks onlineVerificationChecks2 : onlineVerificationChecksList) {
						onlineVerificationChecks2.setInputFile(inputFile);
						onlineVerificationChecksRepository.save(onlineVerificationChecks2);
					}
					onlineVerificationChecks.setInputFile(inputFile);
				}
			}

			List<OnlineVerificationChecks> onlineVerificationChecksList = new ArrayList<>();
			onlineVerificationChecksList.add(onlineVerificationChecks);
			String onlineVerificationChecksOptListStr = mapper.writeValueAsString(onlineVerificationChecksList);
			logger.info("onlineVerificationChecksOptListStr: {}", onlineVerificationChecksOptListStr);
			logger.info(onlineReRunApiRestURL);
			String res = l3APIService.sendDataToRest(onlineReRunApiRestURL, onlineVerificationChecksOptListStr, null);
			if (StringUtils.isNotEmpty(res) && res.equalsIgnoreCase("success")) {
				retrunMap.put("status", "success");
			}
		} else {
			retrunMap.put("status", "fail");
		}
		return retrunMap;
	}

	@GetMapping("/check-secondary-name/{id}")
	public String checkSecondaryName(@PathVariable(value = "id") Long id) throws JsonProcessingException {
		String inputFileStr = onlineVerificationChecksRepository.getSecondaryName(id);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JsonNode inputFileNode = inputFileStr != null ? mapper.readTree(inputFileStr) : mapper.createObjectNode();
		JsonNode secondaryNode = inputFileNode.has("secondary") ? inputFileNode.get("secondary")
				: mapper.createObjectNode();
		String formattedName = secondaryNode.has("formattedName") ? secondaryNode.get("formattedName").asText() : "";
		if (StringUtils.isEmpty(formattedName)) {
			return "";
		}
		return formattedName;
	}

	@PostMapping("/online-case-report")
	public ArrayNode getOnlineCaseReport(@RequestBody JsonNode requestBody) throws JsonProcessingException {

		String caseNumber = requestBody.has("caseNumber") ? requestBody.get("caseNumber").asText() : "";
		String crnNo = requestBody.has("CRNNumber") ? requestBody.get("CRNNumber").asText() : "";
		String fromDate = requestBody.has("fromDate") ? requestBody.get("fromDate").asText() : "";
		String toDate = requestBody.has("toDate") ? requestBody.get("toDate").asText() : "";

		toDate = StringUtils.isEmpty(toDate) ? fromDate : toDate;

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		ArrayNode resultArrayNode = mapper.createArrayNode();

		if (!StringUtils.isEmpty(fromDate) && !StringUtils.isEmpty(toDate)) {

			List<OnlineChecksLogPOJO> onlineChecksLogPOJOs = onlineVerificationChecksRepository
					.getVerifyChecksLog(fromDate, toDate, caseNumber, crnNo);

			List<String> caseNoList = onlineChecksLogPOJOs.stream().map(OnlineChecksLogPOJO::getCaseNumber)
					.collect(Collectors.toList());
			caseNoList = new ArrayList<>(new HashSet<>(caseNoList));

			for (String caseNo : caseNoList) {
				List<String> checkIdList = onlineChecksLogPOJOs.stream()
						.filter(onlineChecksLogPOJO -> onlineChecksLogPOJO.getCaseNumber().equals(caseNo))
						.map(OnlineChecksLogPOJO::getCheckId).collect(Collectors.toList());

				checkIdList = new ArrayList<>(new HashSet<>(checkIdList));

				caseNumber = "";
				String candidateName = "";
				String secondaryName = "";
				String clientName = "";
				String sbu = "";
				String packageName = "";
				crnNo = "";
				String checkCreatedDate = "";
				String checkUpdatedDate ="";
				List<String> finalResultList = new ArrayList<>();
				ObjectNode resultNode = mapper.createObjectNode();

				Map<String, List<Map<String, String>>> initResultMap = new HashMap<>();

				for (String checkId : checkIdList) {
					List<OnlineChecksLogPOJO> tempOnlineChecksLogPOJOs = onlineChecksLogPOJOs.stream()
							.filter(onlineChecksLogPOJO -> onlineChecksLogPOJO.getCheckId().equals(checkId))
							.collect(Collectors.toList());

					for (OnlineChecksLogPOJO onlineChecksLogPOJO : tempOnlineChecksLogPOJOs) {
						finalResultList.add(onlineChecksLogPOJO.getResult());
						/*
						 * if (!StringUtils.equalsIgnoreCase(onlineChecksLogPOJO.getApiName(),
						 * "Worldcheck")) { finalResultList.add(onlineChecksLogPOJO.getResult()); }
						 */

						List<Map<String, String>> initResultListMap = new ArrayList<>();
						if (initResultMap.containsKey(onlineChecksLogPOJO.getApiName())) {
							initResultListMap = initResultMap.get(onlineChecksLogPOJO.getApiName());
						}

						Map<String, String> apiWiseResultMap = new HashMap<>();
						apiWiseResultMap.put("checkId", checkId);
						apiWiseResultMap.put("result", onlineChecksLogPOJO.getInitialResult());
						initResultListMap.add(apiWiseResultMap);
						initResultListMap = new ArrayList<>(new HashSet<>(initResultListMap));
						initResultMap.put(onlineChecksLogPOJO.getApiName(), initResultListMap);

						crnNo = StringUtils.isEmpty(crnNo) ? onlineChecksLogPOJO.getCrnNo() : crnNo;
						caseNumber = StringUtils.isEmpty(caseNumber) ? onlineChecksLogPOJO.getCaseNumber() : caseNumber;
						candidateName = StringUtils.isEmpty(candidateName) ? onlineChecksLogPOJO.getCandidateName()
								: candidateName;
						secondaryName = StringUtils.isEmpty(secondaryName) ? onlineChecksLogPOJO.getSecondaryName()
								: secondaryName;
						clientName = StringUtils.isEmpty(clientName) ? onlineChecksLogPOJO.getClientName() : clientName;
						sbu = StringUtils.isEmpty(sbu) ? onlineChecksLogPOJO.getSbu() : sbu;
						packageName = StringUtils.isEmpty(packageName) ? onlineChecksLogPOJO.getPackageName()
								: packageName;
						checkCreatedDate = StringUtils.isEmpty(checkCreatedDate) ? onlineChecksLogPOJO.getCreatedDate()
								: checkCreatedDate;
						checkUpdatedDate= StringUtils.isEmpty(checkUpdatedDate) ? onlineChecksLogPOJO.getUpdatedDate()
								: checkUpdatedDate;
					}
				}

				String initResultStr = mapper.writeValueAsString(initResultMap);
				JsonNode initResultNode = mapper.readTree(initResultStr);

				resultNode.put("caseNumber", caseNumber);
				resultNode.put("crnNo", crnNo);
				resultNode.put("checkId", String.join(", ", checkIdList));
				resultNode.put("candidateName", candidateName);
				resultNode.put("clientName", clientName);
				resultNode.put("sbu", sbu);
				resultNode.put("packageName", packageName);
				resultNode.set("initialResult", initResultNode);
				resultNode.put("initialResult", initResultStr);
				resultNode.put("checkCreatedDate", checkCreatedDate);
				resultNode.put("checkUpdatedDate", checkUpdatedDate);
				resultNode.put("secondaryName", secondaryName);

				if (!finalResultList.isEmpty()) {
					List<String> clearList = finalResultList.stream().filter(e -> e.equalsIgnoreCase("Clear"))
							.collect(Collectors.toList());
					if (finalResultList.contains("Manual")) {
						resultNode.put("result", "Manual");
					} else if (finalResultList.contains("Result Found")) {
						resultNode.put("result", "Result Found");
					} else if (clearList.size() == finalResultList.size()) {
						resultNode.put("result", "Clear");
					} else {
						resultNode.put("result", "Processing request");
					}
				}
				resultArrayNode.add(resultNode);
			}
		}
		return resultArrayNode;
	}

	@GetMapping("/send-to-l3/{checkId}")
	public String sendDataToL3UsingCheckId(@PathVariable(value = "checkId") String checkId) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			return onlineVerificationChecksService.sendDataToL3ClearOutcomeAttempt(mapper, checkId);
		} catch (JsonProcessingException e) {
			logger.info("Exception in calling /send-to-l3/{checkId}");
			return null;
		}
	}
}
