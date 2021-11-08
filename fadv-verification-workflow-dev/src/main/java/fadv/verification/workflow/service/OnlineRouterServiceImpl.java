package fadv.verification.workflow.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fadv.verification.workflow.model.AttemptHistory;
import fadv.verification.workflow.model.AttemptStatusData;
import fadv.verification.workflow.model.CaseSpecificInfo;
import fadv.verification.workflow.model.CaseSpecificRecordDetail;
import fadv.verification.workflow.model.ManupatraOutput;
import fadv.verification.workflow.model.OnlineManualVerification;
import fadv.verification.workflow.model.OnlineVerificationChecks;
import fadv.verification.workflow.model.RouterHistory;
import fadv.verification.workflow.model.VerificationEventStatus;
import fadv.verification.workflow.pojo.CheckVerificationPOJO;
import fadv.verification.workflow.pojo.EducationDocTypePOJO;
import fadv.verification.workflow.pojo.FileUploadPOJO;
import fadv.verification.workflow.pojo.L3CaseReferencePOJO;
import fadv.verification.workflow.pojo.TaskSpecsPOJO;
import fadv.verification.workflow.repository.AttemptHistoryRepository;
import fadv.verification.workflow.repository.AttemptStatusDataRepository;
import fadv.verification.workflow.repository.CaseSpecificRecordDetailRepository;
import fadv.verification.workflow.repository.ManupatraOutputRepository;
import fadv.verification.workflow.repository.OnlineManualVerificationRepository;
import fadv.verification.workflow.repository.OnlineVerificationChecksRepository;
import fadv.verification.workflow.repository.RouterHistoryRepository;
import fadv.verification.workflow.repository.VerificationEventStatusRepository;
import fadv.verification.workflow.utility.Utility;

@Service
public class OnlineRouterServiceImpl implements OnlineRouterService {

	private static final String DAY_0 = "Day 0";

	private static final String RESULT2 = "result";

	private static final String TITLE = "title";

	private static final String MANUPATRA = "Manupatra";

	@Autowired
	private ApiService apiService;

	@Autowired
	private RouterHistoryRepository routerHistoryRepository;

	@Autowired
	private AttemptHistoryRepository attemptHistoryRepository;

	@Autowired
	private AttemptStatusDataRepository attemptStatusDataRepository;

	@Autowired
	private VerificationEventStatusRepository verificationEventStatusRepository;

	@Autowired
	private OnlineVerificationChecksRepository onlineVerificationChecksRepository;

	@Autowired
	private OnlineManualVerificationRepository onlineManualVerificationRepository;

	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;

	@Autowired
	private ManupatraOutputRepository manupatraOutputRepository;

	@Value("${componentwisemapping.rest.url}")
	private String componentWiseApiUrl;
	@Value("${router.online.rest.url}")
	private String onlineRouterUrl;
	@Value("${verification.status.url.l3}")
	private String verificationStatusL3Url;
	@Value("${verification.url.checkid.l3}")
	private String verificationStatusUrlL3;
	@Value("${education.doc.type.list}")
	private String educationDocTypeStr;

	private static final Logger logger = LoggerFactory.getLogger(OnlineRouterServiceImpl.class);
	private static final String PERSONAL_DETAILS_BVF = "personaldetailsasperbvf";
	private static final String PASSPORT_AS_DOCUMENT = "passportasperdocument";
	private static final String PAN_AS_DOCUMENT = "pancardasperdocument";
	private static final String DL_AS_DOCUMENT = "drivinglicenseasperdocument";
	private static final String VOTER_ID_AS_DOCUMENT = "voteridasperdocument";
	private static final String CANDIDATE_NAME = "candidatename";
	private static final String FATHERS_NAME = "fathersname";
	private static final String DOB = "dob";
	private static final String PRIMARY_NAME = "primaryName";
	private static final String SECONDARY_NAME = "secondaryName";
	private static final String ADDRESS = "address";
	private static final String NAME_PAN_CARD = "nameasperpancard";
	private static final String FIRST_NAME = "firstname";
	private static final String MIDDLE_NAME = "middlename";
	private static final String LAST_NAME = "lastname";
	private static final String RESPONSE = "response";
	private static final String ADVERSE_MEDIA = "Adverse Media";
	private static final String GOOGLE = "Google";
	private static final String LOAN_DEFAULTER_CIBIL = "Loan Defaulter (Cibil)";
	private static final String LOAN_DEFAULTER = "Loan Defaulter";
	private static final String CHECK_ID_OBJ_LIST = "checkIds";
	private static final String DATA_ENTRY = "dataEntry";
	private static final String L3_ERROR_RESPONSE = "{\"Error\": \"Error as reponse from L3\"}";
	private static final String CHECK_ID = "checkId";
	private static final String SUCCESS = "success";
	private static final String RESULT = RESULT2;
	private static final String PROCESSED = "Processed";
	private static final String API_NAME = "apiName";
	private static final String INPUT_FILE = "inputFile";
	private static final String MATCHED_IDENTIFIER = "matchedIdentifier";
	private static final String OUTPUT_FILE = "outputFile";
	private static final String FINAL_STATUS = "finalStatus";
	private static final String CASE_SPECIFIC_RECORD_ID = "caseSpecificRecordId";
	private static final String MANUAL = "Manual";
	private static final String RECORD_FOUND = "Record Found";
	private static final String CLEAR = "Clear";
	private static final String ONLINE = "Online";
	private static final String AUTO = "Auto";
	private static final String PRODUCT_NAME = "Product Name: ";
	private static final String APINAME = ", API Name: ";
	private static final String VERIFYID = "verifyId";
	private static final String FILE_DATA = "fileData";
	private static final String NAMEOFTHECANDIDATEWHILEATTENDINGTHEBELOWQUALIFICATION = "nameofthecandidatewhileattendingthebelowqualification";

	@Override
	public void processOnlineRouterService(ObjectMapper mapper,
			List<CaseSpecificRecordDetail> caseSpecificRecordDetails, CaseSpecificInfo caseSpecificInfo) {
		ObjectNode dataEntryNode = getDetailsFromDataEntry(mapper, caseSpecificInfo);
		ArrayNode checkIdArrNode = mapper.createArrayNode();
		String onlineResponseStr = "{}";
		try {
			generateOnlineRequest(mapper, caseSpecificRecordDetails, checkIdArrNode, caseSpecificInfo);
			ObjectNode requestNode = mapper.createObjectNode();
			if (!checkIdArrNode.isEmpty() && !dataEntryNode.isEmpty()) {
				requestNode.set(DATA_ENTRY, dataEntryNode);
				requestNode.set(CHECK_ID_OBJ_LIST, checkIdArrNode);
			}
			if (!requestNode.isEmpty()) {
				List<RouterHistory> routerHistories = saveRouterHistory(caseSpecificRecordDetails, requestNode,
						caseSpecificInfo);
				onlineResponseStr = apiService.sendDataToPost(onlineRouterUrl, mapper.writeValueAsString(requestNode));
				if (onlineResponseStr != null) {
					processOnlineResponse(mapper, onlineResponseStr, routerHistories, dataEntryNode, caseSpecificInfo);
				} else {
					saveFailedResponse(mapper, routerHistories);
				}
			}
		} catch (JsonProcessingException e) {
			logger.error("Exception while making online request : {}", e.getMessage());
			e.printStackTrace();
		}
	}

	private void saveFailedResponse(ObjectMapper mapper, List<RouterHistory> routerHistories) {
		String routerStatus = "Failed";
		List<RouterHistory> newRouterHistories = new ArrayList<>();
		for (RouterHistory routerHistory : routerHistories) {
			routerHistory.setCurrentEngineStatus(routerStatus);
			routerHistory.setEndTime(new Date());
			routerHistory.setEngineResponse(mapper.createObjectNode());
			newRouterHistories.add(routerHistory);
		}
		if (CollectionUtils.isNotEmpty(newRouterHistories)) {
			routerHistoryRepository.saveAll(newRouterHistories);
		}
	}

	private void processOnlineResponse(ObjectMapper mapper, String responseStr, List<RouterHistory> routerHistories,
			JsonNode dataEntryNode, CaseSpecificInfo caseSpecificInfo) throws JsonProcessingException {
		JsonNode responseNode = mapper.readValue(responseStr, JsonNode.class);
		List<RouterHistory> newRouterHistories = new ArrayList<>();

		boolean success = responseNode.has(SUCCESS) ? responseNode.get(SUCCESS).asBoolean() : Boolean.FALSE;
		String routerResult = responseNode.has(RESULT) ? responseNode.get(RESULT).asText() : "";
		ArrayNode childResponseNode = responseNode.has(RESPONSE) ? (ArrayNode) responseNode.get(RESPONSE)
				: mapper.createArrayNode();

		String routerStatus = "";
		if (Boolean.TRUE.equals(success) && StringUtils.isNotEmpty(routerResult) && !childResponseNode.isEmpty()) {
			processChildResponseNode(mapper, childResponseNode, caseSpecificInfo, dataEntryNode);
			routerStatus = PROCESSED;
		} else {
			routerStatus = "Failed";
		}
		for (RouterHistory routerHistory : routerHistories) {
			routerHistory.setCurrentEngineStatus(routerStatus);
			routerHistory.setEndTime(new Date());
			routerHistory.setEngineResponse(responseNode);
			newRouterHistories.add(routerHistory);
		}
		if (CollectionUtils.isNotEmpty(newRouterHistories)) {
			routerHistoryRepository.saveAll(newRouterHistories);
		}
	}

	private void processChildResponseNode(ObjectMapper mapper, ArrayNode childResponseNode,
			CaseSpecificInfo caseSpecificInfo, JsonNode dataEntryNode) {
		List<OnlineVerificationChecks> onlineVerificationChecks = new ArrayList<>();
		List<CaseSpecificRecordDetail> caseSpecificRecordDetails = new ArrayList<>();
		OnlineManualVerification onlineManualVerification = saveOnlineVerifyCheck(mapper, caseSpecificInfo,
				dataEntryNode);
		for (JsonNode response : childResponseNode) {
			OnlineVerificationChecks onlineVerificationCheck = new OnlineVerificationChecks();
			Long caseSpecificRecordId = response.has(CASE_SPECIFIC_RECORD_ID)
					? response.get(CASE_SPECIFIC_RECORD_ID).asLong()
					: 0;
			Optional<CaseSpecificRecordDetail> caseSpecificRecordDetail = caseSpecificRecordDetailRepository
					.findById(caseSpecificRecordId);
			if (caseSpecificRecordDetail.isPresent()) {
				CaseSpecificRecordDetail newCaseSpecificRecordDetail = caseSpecificRecordDetail.get();
				setOnlineVerificationChecks(mapper, onlineManualVerification, response, onlineVerificationCheck,
						caseSpecificInfo.getCaseNumber(), newCaseSpecificRecordDetail);
				caseSpecificRecordDetails.add(newCaseSpecificRecordDetail);
				onlineVerificationChecks.add(onlineVerificationCheck);
			}

		}
		if (CollectionUtils.isNotEmpty(onlineVerificationChecks)) {

			List<OnlineVerificationChecks> newOnlineVerificationChecks = onlineVerificationChecksRepository
					.saveAll(onlineVerificationChecks);
			if (CollectionUtils.isNotEmpty(newOnlineVerificationChecks)) {
				caseSpecificRecordDetailRepository.saveAll(caseSpecificRecordDetails);

				List<String> checkIdList = caseSpecificRecordDetails.stream()
						.map(CaseSpecificRecordDetail::getInstructionCheckId).collect(Collectors.toList());
				checkIdList = new ArrayList<>(new HashSet<>(checkIdList));
				apiService.sendDataToPost(verificationStatusUrlL3, checkIdList.toString());
			}
		}
	}

	private void setOnlineVerificationChecks(ObjectMapper mapper, OnlineManualVerification onlineManualVerification,
			JsonNode response, OnlineVerificationChecks onlineVerificationCheck, String caseNumber,
			CaseSpecificRecordDetail caseSpecificRecordDetail) {
		String apiName = response.has(API_NAME) ? response.get(API_NAME).asText() : "";
		String inputFile = response.has(INPUT_FILE) ? response.get(INPUT_FILE).toString() : "{}";
		String matchedIdentifier = response.has(MATCHED_IDENTIFIER) ? response.get(MATCHED_IDENTIFIER).toString()
				: "{}";
		String outputFile = response.has(OUTPUT_FILE) ? response.get(OUTPUT_FILE).toString() : "{}";
		String finalStatus = response.has(FINAL_STATUS) ? response.get(FINAL_STATUS).asText() : "";
		String verifyId = response.has(VERIFYID) ? response.get(VERIFYID).asText() : "";
		String checkId = response.has(CHECK_ID) ? response.get(CHECK_ID).asText() : "";

		List<OnlineVerificationChecks> newOnlineVerificationChecks = onlineVerificationChecksRepository
				.findByCheckIdOrderByOnlineVerificationCheckIdDesc(checkId);

		inputFile = StringUtils.isEmpty(inputFile) ? "{}" : inputFile;
		ArrayNode outputResult = getOutputResult(mapper, outputFile, apiName, checkId);

		onlineVerificationCheck.setOnlineManualVerificationId(onlineManualVerification.getOnlineManualVerificationId());
		onlineVerificationCheck.setApiName(apiName);
		onlineVerificationCheck.setInitialResult(finalStatus);
		if (StringUtils.equalsAnyIgnoreCase(finalStatus, MANUAL, CLEAR, RECORD_FOUND)) {
			onlineVerificationCheck.setPendingStatus("0");
		} else {
			onlineVerificationCheck.setPendingStatus("1");
		}
		onlineVerificationCheck.setComponentName(caseSpecificRecordDetail.getComponentName());
		onlineVerificationCheck.setSubComponentName(caseSpecificRecordDetail.getProduct());
		onlineVerificationCheck.setCheckId(checkId);
		onlineVerificationCheck.setRetryNo("0");
		onlineVerificationCheck.setResult(finalStatus);
		onlineVerificationCheck.setMatchedIdentifiers(matchedIdentifier);
		onlineVerificationCheck.setOutputFile(outputFile);
		onlineVerificationCheck.setOutputResult(outputResult);
		onlineVerificationCheck.setInputFile(inputFile);
		onlineVerificationCheck.setVerifyId(verifyId);
		onlineVerificationCheck.setCreatedDate(new Date().toString());
		onlineVerificationCheck.setUpdatedDate(new Date().toString());

		if (CollectionUtils.isNotEmpty(newOnlineVerificationChecks)) {
			OnlineVerificationChecks newOnlineVerificationCheck = newOnlineVerificationChecks.get(0);
			onlineVerificationCheck
					.setOnlineVerificationCheckId(newOnlineVerificationCheck.getOnlineVerificationCheckId());
			onlineVerificationCheck.setCreatedDate(newOnlineVerificationCheck.getCreatedDate());
		}
		processAttemtSave(finalStatus, apiName, caseSpecificRecordDetail, caseNumber);
	}

	private ArrayNode getOutputResult(ObjectMapper mapper, String outputFile, String apiName, String checkId) {
		ArrayNode outputResult = mapper.createArrayNode();
		if (StringUtils.equalsIgnoreCase(apiName, MANUPATRA)) {
			try {
				outputResult = StringUtils.isNotEmpty(outputFile) ? processOutputFile(mapper, outputFile, checkId)
						: mapper.createArrayNode();
			} catch (JsonProcessingException e) {
				logger.error("Exception while mapping output result : {}", e.getMessage());
				outputResult = mapper.createArrayNode();
				e.printStackTrace();
			}
		}

		outputResult = outputResult != null ? outputResult : mapper.createArrayNode();
		return outputResult;
	}

	private ArrayNode processOutputFile(ObjectMapper mapper, String outputFile, String checkId)
			throws JsonProcessingException {

		JsonNode outputNode = mapper.readValue(outputFile, JsonNode.class);
		String primaryStr = outputNode.has("primary") ? outputNode.get("primary").asText() : "";
		JsonNode primaryNode = StringUtils.isNotEmpty(primaryStr) ? mapper.readValue(primaryStr, JsonNode.class)
				: mapper.createObjectNode();
		String rawDataStr = primaryNode.has("rawData") ? primaryNode.get("rawData").asText() : "";
		JsonNode rawDataNewNode = StringUtils.isNotEmpty(rawDataStr) ? mapper.readValue(rawDataStr, JsonNode.class)
				: mapper.createObjectNode();

		String dataStr = rawDataNewNode.has("data") ? rawDataNewNode.get("data").asText() : "";
		JsonNode dataNode = StringUtils.isNotEmpty(dataStr) ? mapper.readValue(dataStr, JsonNode.class)
				: mapper.createObjectNode();
		ArrayNode dataObjectArr = mapper.createArrayNode();

		if (dataNode.isArray()) {
			for (JsonNode data : dataNode) {
				dataObjectArr.add(processDataNode(mapper, data, checkId));
			}
		} else {
			dataObjectArr.add(processDataNode(mapper, dataNode, checkId));
		}
		return dataObjectArr;
	}

	private ObjectNode processDataNode(ObjectMapper mapper, JsonNode dataNode, String checkId)
			throws JsonProcessingException {
		ManupatraOutput manupatraOutput = new ManupatraOutput();

		ObjectNode dataObject = mapper.createObjectNode();
		String titleStr = dataNode.has(TITLE) ? dataNode.get(TITLE).asText() : "";
		dataObject.put(TITLE, titleStr);

		String fileDataStr = dataNode.has(FILE_DATA) ? dataNode.get(FILE_DATA).asText() : "";
		JsonNode fileDataNode = StringUtils.isNotEmpty(fileDataStr) ? mapper.readValue(fileDataStr, JsonNode.class)
				: mapper.createObjectNode();
		String resultStr = fileDataNode.has(RESULT) ? fileDataNode.get(RESULT).asText() : "";
		if (resultStr == null || StringUtils.isEmpty(resultStr)) {
			resultStr = fileDataNode.has("Result") ? fileDataNode.get("Result").asText() : "";
		}
		if (StringUtils.isNotEmpty(resultStr)) {
			resultStr = StringUtils.replace(resultStr, "\n", resultStr);
			resultStr = StringUtils.replace(resultStr, "\r", resultStr);
			resultStr = StringUtils.replace(resultStr, "\\\"", resultStr);
		}
		manupatraOutput.setCheckId(checkId);
		manupatraOutput.setTitle(titleStr);
		manupatraOutput.setResult(resultStr);
		manupatraOutput.setCreatedDate(new Date());
		manupatraOutput.setUpdatedDate(new Date());

		List<ManupatraOutput> manupatraOutputs = manupatraOutputRepository.findByCheckIdAndTitle(checkId, titleStr);
		if (CollectionUtils.isNotEmpty(manupatraOutputs)) {
			ManupatraOutput oldManupatraOutput = manupatraOutputs.get(0);
			manupatraOutput.setManupatraOutputId(oldManupatraOutput.getManupatraOutputId());
			manupatraOutput.setCreatedDate(oldManupatraOutput.getCreatedDate());
		}

		manupatraOutputRepository.save(manupatraOutput);

		return dataObject;
	}

	private void processAttemtSave(String finalStatus, String apiName,
			CaseSpecificRecordDetail newCaseSpecificRecordDetail, String caseNumber) {
		if (StringUtils.equalsIgnoreCase(finalStatus, CLEAR)) {
			createClearOutcomeAttempt(apiName, newCaseSpecificRecordDetail, caseNumber);
		} else if (StringUtils.equalsIgnoreCase(finalStatus, RECORD_FOUND)) {
			createDiscrepantProcessAttempt(apiName, newCaseSpecificRecordDetail, caseNumber);
		} else if (StringUtils.equalsIgnoreCase(finalStatus, MANUAL)) {
			createReviewProcessAttempt(apiName, newCaseSpecificRecordDetail, caseNumber);
		}
		newCaseSpecificRecordDetail.setOnlineStatus(PROCESSED);
		newCaseSpecificRecordDetail.setUpdatedDate(new Date());
	}

	private OnlineManualVerification saveOnlineVerifyCheck(ObjectMapper mapper, CaseSpecificInfo caseSpecificInfo,
			JsonNode dataEntryNode) {

		String secondaryName = dataEntryNode.has(SECONDARY_NAME) ? dataEntryNode.get(SECONDARY_NAME).asText() : "";
		OnlineManualVerification onlineManualVerification = new OnlineManualVerification();
		onlineManualVerification.setCandidateName(caseSpecificInfo.getCandidateName());
		onlineManualVerification.setSecondaryName(secondaryName);
		onlineManualVerification.setCaseNumber(caseSpecificInfo.getCaseNumber());
		onlineManualVerification.setPackageName(caseSpecificInfo.getPackageName());
		onlineManualVerification.setClientName(caseSpecificInfo.getClientName());
		onlineManualVerification.setSBU(caseSpecificInfo.getSbuName());
		onlineManualVerification.setCrnNo(caseSpecificInfo.getCaseRefNumber());
		onlineManualVerification.setStatus(MANUAL);
		onlineManualVerification.setTimeCreation(new Date().toString());
		onlineManualVerification.setUpdatedTime(new Date().toString());
		try {
			onlineManualVerification.setDataEntryResult(mapper.writeValueAsString(dataEntryNode));
		} catch (JsonProcessingException e) {
			onlineManualVerification.setDataEntryResult("{}");
		}

		List<OnlineManualVerification> newOnlineManualVerifications = onlineManualVerificationRepository
				.findByCaseNumberOrderByOnlineManualVerificationIdDesc(caseSpecificInfo.getCaseNumber());
		if (CollectionUtils.isNotEmpty(newOnlineManualVerifications)) {
			OnlineManualVerification newOnlineManualVerification = newOnlineManualVerifications.get(0);
			onlineManualVerification
					.setOnlineManualVerificationId(newOnlineManualVerification.getOnlineManualVerificationId());
			onlineManualVerification.setTimeCreation(newOnlineManualVerification.getTimeCreation());
		}

		return onlineManualVerificationRepository.save(onlineManualVerification);

	}

	private List<RouterHistory> saveRouterHistory(List<CaseSpecificRecordDetail> caseSpecificRecordDetails,
			JsonNode requestNode, CaseSpecificInfo caseSpecificInfo) {
		List<RouterHistory> routerHistoryList = new ArrayList<>();
		for (CaseSpecificRecordDetail caseSpecificRecordDetail : caseSpecificRecordDetails) {
			RouterHistory routerHistory = new RouterHistory();
			routerHistory.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
			routerHistory.setEngineName(ONLINE);
			routerHistory.setStartTime(new Date());
			routerHistory.setEngineRequest(requestNode);
			routerHistory.setCaseNumber(caseSpecificInfo.getCaseNumber());
			routerHistory.setCurrentEngineStatus("Initiated");
			routerHistory.setCaseSpecificRecordDetailId(caseSpecificRecordDetail.getCaseSpecificDetailId());
			routerHistory.setEngineResponse(null);
			routerHistory.setEndTime(null);

			routerHistoryList.add(routerHistory);
		}

		return routerHistoryRepository.saveAll(routerHistoryList);
	}

	private void generateOnlineRequest(ObjectMapper mapper, List<CaseSpecificRecordDetail> caseSpecificRecordDetails,
			ArrayNode checkIdArrNode, CaseSpecificInfo caseSpecificInfo) throws JsonProcessingException {
		for (CaseSpecificRecordDetail caseSpecificRecordDetail : caseSpecificRecordDetails) {
			String componentName = caseSpecificRecordDetail.getComponentName() != null
					? caseSpecificRecordDetail.getComponentName()
					: "";
			String productName = caseSpecificRecordDetail.getProduct() != null ? caseSpecificRecordDetail.getProduct()
					: "";
			String checkId = caseSpecificRecordDetail.getInstructionCheckId() != null
					? caseSpecificRecordDetail.getInstructionCheckId()
					: "";
			Long caseSpecificRecordId = caseSpecificRecordDetail.getCaseSpecificDetailId();
			if (StringUtils.isNotEmpty(checkId) && StringUtils.isNotEmpty(productName)
					&& StringUtils.isNotBlank(componentName) && caseSpecificRecordId != 0) {
				List<String> serviceNameList = getApiServiceName(mapper, componentName, productName);
				logger.info("Service name list : {}", serviceNameList);
				for (String serviceName : serviceNameList) {
					ObjectNode checkIdNode = mapper.createObjectNode();
					checkIdNode.put(CHECK_ID, checkId);
					checkIdNode.put(CASE_SPECIFIC_RECORD_ID, caseSpecificRecordId);
					checkIdNode.put(API_NAME, serviceName);
					checkIdArrNode.add(checkIdNode);
				}
				createInitiatedAttempt(String.join(", ", serviceNameList), caseSpecificRecordDetail);
				createInitiatedVerificationEvent(caseSpecificRecordDetail, caseSpecificInfo);
			}
		}
	}

	private List<String> getApiServiceName(ObjectMapper mapper, String componentName, String productName)
			throws JsonProcessingException {
		ObjectNode requestNode = mapper.createObjectNode();
		requestNode.put("productName", productName);
		requestNode.put("componentName", componentName);
		String requestNodeStr = mapper.writeValueAsString(requestNode);
		String apiServiceName = apiService.sendDataToPost(componentWiseApiUrl, requestNodeStr);

		List<String> serviceNameList = new ArrayList<>();
		if (apiServiceName != null && !StringUtils.isEmpty(apiServiceName)) {
			apiServiceName = StringUtils.replace(apiServiceName, LOAN_DEFAULTER_CIBIL, LOAN_DEFAULTER);
			apiServiceName = StringUtils.replace(apiServiceName, GOOGLE, ADVERSE_MEDIA);
			if (apiServiceName.contains(",")) {
				serviceNameList.addAll(Arrays.asList(apiServiceName.trim().split("\\s*,\\s*")));
			} else {
				serviceNameList.add(apiServiceName);
			}
		}
		serviceNameList = new ArrayList<>(new HashSet<>(serviceNameList));
		return serviceNameList;
	}

	private ObjectNode getDetailsFromDataEntry(ObjectMapper mapper, CaseSpecificInfo caseSpecificInfo) {
		ObjectNode dataEntryNode = mapper.createObjectNode();
		getDataEntryResult(mapper, caseSpecificInfo, dataEntryNode);
		return dataEntryNode;
	}

	private void getDataEntryResult(ObjectMapper mapper, CaseSpecificInfo caseSpecificInfo, ObjectNode dataEntryNode) {
		JsonNode deDataNode = getDeDataNode(mapper, caseSpecificInfo);
		JsonNode personalDetailsNode = deDataNode.has(PERSONAL_DETAILS_BVF) ? deDataNode.get(PERSONAL_DETAILS_BVF)
				: mapper.createObjectNode();
		JsonNode passportNode = deDataNode.has(PASSPORT_AS_DOCUMENT) ? deDataNode.get(PASSPORT_AS_DOCUMENT)
				: mapper.createObjectNode();
		JsonNode panCardNode = deDataNode.has(PAN_AS_DOCUMENT) ? deDataNode.get(PAN_AS_DOCUMENT)
				: mapper.createObjectNode();
		JsonNode dlNode = deDataNode.has(DL_AS_DOCUMENT) ? deDataNode.get(DL_AS_DOCUMENT) : mapper.createObjectNode();
		JsonNode voterIdNode = deDataNode.has(VOTER_ID_AS_DOCUMENT) ? deDataNode.get(VOTER_ID_AS_DOCUMENT)
				: mapper.createObjectNode();
		String primaryName = personalDetailsNode.has(CANDIDATE_NAME) ? personalDetailsNode.get(CANDIDATE_NAME).asText()
				: "";

		primaryName = getPrimaryName(personalDetailsNode, primaryName);
		String fathersName = personalDetailsNode.has(FATHERS_NAME) ? personalDetailsNode.get(FATHERS_NAME).asText()
				: "";
		String dob = personalDetailsNode.has(DOB) ? personalDetailsNode.get(DOB).asText() : "";
		String secondaryName = extractSecondaryName(passportNode, panCardNode, dlNode, voterIdNode, primaryName);

		if (StringUtils.isEmpty(secondaryName)) {
			secondaryName = parseSecondaryNameForEducation(mapper, deDataNode, primaryName);
		}

		String address = "";

		dataEntryNode.put(PRIMARY_NAME, primaryName);
		dataEntryNode.put(SECONDARY_NAME, secondaryName);
		dataEntryNode.put(DOB, dob);
		dataEntryNode.put(FATHERS_NAME, fathersName);
		dataEntryNode.put(ADDRESS, address);
	}

	private String parseSecondaryNameForEducation(ObjectMapper mapper, JsonNode deDataNode, String primaryName) {
		String[] educationDocTypeList = educationDocTypeStr.split("\\s*,\\s*");

		Map<String, Integer> educationDocOrder = new HashMap<>();
		int index = 1;
		for (String docType : educationDocTypeList) {
			educationDocOrder.put(docType, index);
			index++;
		}
		return setSecondaryEduName(mapper, deDataNode, educationDocOrder, primaryName);
	}

	private String setSecondaryEduName(ObjectMapper mapper, JsonNode deDataNode, Map<String, Integer> educationDocOrder,
			String primaryName) {

		Map<String, Object> deDataMap = mapper.convertValue(deDataNode, new TypeReference<Map<String, Object>>() {
		});
		List<EducationDocTypePOJO> educationDocTypePOJOs = new ArrayList<>();
		for (Map.Entry<String, Object> deDataEntrySet : deDataMap.entrySet()) {
			educationalDocSecondaryName(mapper, educationDocOrder, primaryName, educationDocTypePOJOs, deDataEntrySet);
		}
		logger.info("educationDocTypePOJOs : {}", educationDocTypePOJOs);
		if (CollectionUtils.isNotEmpty(educationDocTypePOJOs)) {
			educationDocTypePOJOs.sort((a, b) -> a.getDocId() - b.getDocId());
			educationDocTypePOJOs
					.sort((a, b) -> b.getSeconadryName().trim().length() - a.getSeconadryName().trim().length());
			return educationDocTypePOJOs.get(0).getSeconadryName();
		}
		return "";
	}

	private void educationalDocSecondaryName(ObjectMapper mapper, Map<String, Integer> educationDocOrder,
			String primaryName, List<EducationDocTypePOJO> educationDocTypePOJOs,
			Map.Entry<String, Object> deDataEntrySet) {
		if (educationDocOrder.containsKey(deDataEntrySet.getKey())) {
			String secondaryName = "";
			JsonNode docNode = mapper.convertValue(deDataEntrySet.getValue(), new TypeReference<JsonNode>() {
			});
			if (docNode.isArray() && !docNode.isEmpty()) {
				JsonNode docNodeChild = docNode.get(0);
				secondaryName = docNodeChild.has(NAMEOFTHECANDIDATEWHILEATTENDINGTHEBELOWQUALIFICATION)
						? docNodeChild.get(NAMEOFTHECANDIDATEWHILEATTENDINGTHEBELOWQUALIFICATION).asText()
						: "";
			} else if (!docNode.isEmpty()) {
				secondaryName = docNode.has(NAMEOFTHECANDIDATEWHILEATTENDINGTHEBELOWQUALIFICATION)
						? docNode.get(NAMEOFTHECANDIDATEWHILEATTENDINGTHEBELOWQUALIFICATION).asText()
						: "";
			}
			secondaryName = Utility.compareName(primaryName, secondaryName);
			if (StringUtils.isNotEmpty(secondaryName)) {
				EducationDocTypePOJO educationDocTypePOJO = new EducationDocTypePOJO();
				educationDocTypePOJO.setDocId(educationDocOrder.get(deDataEntrySet.getKey()));
				educationDocTypePOJO.setDocType(deDataEntrySet.getKey());
				educationDocTypePOJO.setSeconadryName(secondaryName);
				educationDocTypePOJOs.add(educationDocTypePOJO);
			}
		}
	}

	private String extractSecondaryName(JsonNode passportNode, JsonNode panCardNode, JsonNode dlNode,
			JsonNode voterIdNode, String primaryName) {
		String passportName = passportNode.has(CANDIDATE_NAME) ? passportNode.get(CANDIDATE_NAME).asText() : "";
		String panName = panCardNode.has(NAME_PAN_CARD) ? panCardNode.get(NAME_PAN_CARD).asText() : "";
		String dlName = dlNode.has(CANDIDATE_NAME) ? dlNode.get(CANDIDATE_NAME).asText() : "";
		String voterIdName = voterIdNode.has(CANDIDATE_NAME) ? voterIdNode.get(CANDIDATE_NAME).asText() : "";

		return checkIdDocsForSecondaryName(primaryName, passportName, panName, dlName, voterIdName);
	}

	private JsonNode getDeDataNode(ObjectMapper mapper, CaseSpecificInfo caseSpecificInfo) {

		String deDataStr = caseSpecificInfo.getDataEntryInfo() != null ? caseSpecificInfo.getDataEntryInfo() : "{}";
		try {
			return mapper.readValue(deDataStr, JsonNode.class);
		} catch (JsonProcessingException e) {
			logger.error("Exception while mapping deDataStr to JsonNode : {}", e.getMessage());
			return mapper.createObjectNode();
		}
	}

	private String getPrimaryName(JsonNode personalDetailsNode, String primaryName) {
		if (StringUtils.isEmpty(primaryName)) {
			if (personalDetailsNode.has(MIDDLE_NAME)
					&& StringUtils.isNotEmpty(personalDetailsNode.get(MIDDLE_NAME).asText())) {
				primaryName = personalDetailsNode.get(FIRST_NAME).asText()
						+ personalDetailsNode.get(MIDDLE_NAME).asText() + personalDetailsNode.get(LAST_NAME).asText();
			} else {
				primaryName = personalDetailsNode.get(MIDDLE_NAME).asText()
						+ personalDetailsNode.get(LAST_NAME).asText();
			}
		}
		primaryName = StringUtils.replace(primaryName, "  ", " ");
		return primaryName;
	}

	private String checkIdDocsForSecondaryName(String primaryName, String passportName, String panName, String dlName,
			String voterIdName) {

		String secondaryName = "";
		passportName = Utility.compareName(primaryName, passportName);
		panName = Utility.compareName(primaryName, panName);
		dlName = Utility.compareName(primaryName, dlName);
		voterIdName = Utility.compareName(primaryName, voterIdName);

		int passportLength = passportName.trim().length();
		int panLength = panName.trim().length();
		int dlLength = dlName.trim().length();
		int voterIdLength = voterIdName.trim().length();

		List<Integer> docNameLengths = new ArrayList<>();
		docNameLengths.add(passportLength);
		docNameLengths.add(panLength);
		docNameLengths.add(dlLength);
		docNameLengths.add(voterIdLength);
		int maxVal = getStringMaxVal(docNameLengths);

		if (passportLength == maxVal) {
			secondaryName = passportName;
		} else if (panLength == maxVal) {
			secondaryName = panName;
		} else if (dlLength == maxVal) {
			secondaryName = dlName;
		} else if (voterIdLength == maxVal) {
			secondaryName = voterIdName;
		}
		return secondaryName;
	}

	private int getStringMaxVal(List<Integer> docNameLengths) {
		if (CollectionUtils.isNotEmpty(docNameLengths)) {
			Collections.sort(docNameLengths, Collections.reverseOrder());
			return docNameLengths.get(0);
		}
		return 0;
	}

	@Override
	public void processRecordsForMI(ObjectMapper mapper, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo) {
		String recordStr = caseSpecificRecordDetail.getComponentRecordField();
		try {
			JsonNode recordNode = mapper.readValue(recordStr, JsonNode.class);

			String miValue = recordNode.has("MI") ? recordNode.get("MI").asText() : "";

			if (StringUtils.equalsIgnoreCase(miValue, "Yes")) {
				sendDataToL3Mi(recordNode, caseSpecificRecordDetail, caseSpecificInfo);
			}
		} catch (JsonProcessingException e) {
			logger.error("Exception while mapping MI data to L3 : {}", e.getMessage());
			e.printStackTrace();
		}
	}

	private String sendDataToL3Mi(JsonNode recordNode, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo) throws JsonProcessingException {
		// Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		String checkId = recordNode.has(CHECK_ID) ? recordNode.get(CHECK_ID).asText() : "";
		String miRemarks = recordNode.has("miRemarks") ? recordNode.get("miRemarks").asText() : "";

		TaskSpecsPOJO taskSpecs = new TaskSpecsPOJO();
		L3CaseReferencePOJO caseReference = getL3CaseReference(caseSpecificRecordDetail, caseSpecificInfo, mapper,
				checkId);

		CheckVerificationPOJO checkVerification = getCheckVerification(caseSpecificRecordDetail, miRemarks);

		taskSpecs.setCaseReference(caseReference);
		taskSpecs.setCheckVerification(checkVerification);

		taskSpecs.setQuestionaire(new ArrayList<>());

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

	private CheckVerificationPOJO getCheckVerification(CaseSpecificRecordDetail caseSpecificRecordDetail,
			String miRemarks) {
		CheckVerificationPOJO checkVerification = new CheckVerificationPOJO();
		checkVerification.setCountry("India");
		checkVerification.setExecutiveSummaryComments("");
		checkVerification.setReportComments("");
		checkVerification.setResultCode("");
		checkVerification.setEmailId("");
		checkVerification.setVerifierName("");
		checkVerification.setGeneralRemarks("");
		checkVerification.setModeOfVerification(ONLINE);
		checkVerification.setVerifiedDate("");
		checkVerification.setAction("checklevelmi");
		checkVerification.setSubAction("databasemi");
		checkVerification.setActionCode("");
		checkVerification.setComponentName("Database");
		checkVerification.setProductName(caseSpecificRecordDetail.getProduct());
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
		return checkVerification;
	}

	private L3CaseReferencePOJO getL3CaseReference(CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo, ObjectMapper mapper, String checkId) throws JsonProcessingException {
		String caseRefenceStr = caseSpecificInfo.getCaseReference() != null ? caseSpecificInfo.getCaseReference()
				: "{}";
		JsonNode caseReferenceNode = mapper.readTree(caseRefenceStr);
		L3CaseReferencePOJO caseReference = mapper.convertValue(caseReferenceNode, L3CaseReferencePOJO.class);

		caseReference.setCheckId(checkId);
		caseReference.setNgStatus("MI-RQ");
		caseReference.setNgStatusDescription("Missing Information - Requested");
		caseReference.setSbuName(caseSpecificInfo.getSbuName());
		caseReference.setProductName(caseSpecificRecordDetail.getProduct());
		caseReference.setPackageName(caseSpecificInfo.getPackageName());
		caseReference.setComponentName("Database");
		return caseReference;
	}

	private void createInitiatedAttempt(String apiName, CaseSpecificRecordDetail caseSpecificRecordDetail) {
		AttemptHistory attemptHistory = new AttemptHistory();

		String l3Response = "";
		String l3Status = "";

		attemptHistory.setAttemptDescription("Request Initiated");
		attemptHistory.setAttemptStatusid((long) 66);
		attemptHistory.setName(PRODUCT_NAME + caseSpecificRecordDetail.getProduct() + APINAME + apiName);
		attemptHistory.setCheckid(caseSpecificRecordDetail.getInstructionCheckId());
		attemptHistory.setFollowupId((long) 39);
		attemptHistory.setRequestid(caseSpecificRecordDetail.getCaseSpecificDetailId());
		attemptHistory.setContactDate(new Date().toString());
		attemptHistory.setL3Response(l3Response);
		attemptHistory.setL3Status(l3Status);

		AttemptHistory newAttemptHistory = attemptHistoryRepository.save(attemptHistory);
		createInitiatedAttemptStatusData(newAttemptHistory);
	}

	private void createInitiatedAttemptStatusData(AttemptHistory attemptHistory) {
		Long attemptId = attemptHistory.getAttemptid() != null ? attemptHistory.getAttemptid() : 0;
		if (attemptId != 0) {
			AttemptStatusData attemptStatusData = new AttemptStatusData();
			attemptStatusData.setAttemptId(attemptId);
			attemptStatusData.setDepositionId((long) 13);
			attemptStatusData.setEndstatusId((long) 39);
			attemptStatusData.setModeId((long) 14);
			attemptStatusDataRepository.save(attemptStatusData);
		}
	}

	private void createInitiatedVerificationEvent(CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo) {
		VerificationEventStatus verificationEventStatus = new VerificationEventStatus();
		verificationEventStatus.setStatus(DAY_0);
		verificationEventStatus.setEvent("Request Initiated");
		verificationEventStatus.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		verificationEventStatus.setStage(ONLINE);
		verificationEventStatus.setEventType(AUTO);
		verificationEventStatus.setCaseNo(caseSpecificInfo.getCaseNumber());
		verificationEventStatus.setRequestId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		verificationEventStatusRepository.save(verificationEventStatus);
	}

	private void createClearOutcomeAttempt(String apiName, CaseSpecificRecordDetail newCaseSpecificRecordDetail,
			String caseNumber) {

		AttemptHistory attemptHistory = new AttemptHistory();

		String l3Response = "";
		String l3Status = "";

		attemptHistory.setAttemptDescription("Result received with No records");
		attemptHistory.setAttemptStatusid((long) 67);
		attemptHistory.setName(PRODUCT_NAME + newCaseSpecificRecordDetail.getProduct() + APINAME + apiName);
		attemptHistory.setCheckid(newCaseSpecificRecordDetail.getInstructionCheckId());
		attemptHistory.setFollowupId((long) 40);
		attemptHistory.setRequestid(newCaseSpecificRecordDetail.getCaseSpecificDetailId());
		attemptHistory.setContactDate(new Date().toString());
		attemptHistory.setL3Response(l3Response);
		attemptHistory.setL3Status(l3Status);

		AttemptHistory newAttemptHistory = attemptHistoryRepository.save(attemptHistory);

		AttemptStatusData attemptStatusData = new AttemptStatusData();
		attemptStatusData.setAttemptId(newAttemptHistory.getAttemptid());
		attemptStatusData.setDepositionId((long) 3);
		attemptStatusData.setEndstatusId((long) 40);
		attemptStatusData.setModeId((long) 14);
		attemptStatusDataRepository.save(attemptStatusData);

		VerificationEventStatus verificationEventStatus = new VerificationEventStatus();
		verificationEventStatus.setStatus(DAY_0);
		verificationEventStatus.setEvent("Result received with No records");
		verificationEventStatus.setCheckId(newCaseSpecificRecordDetail.getInstructionCheckId());
		verificationEventStatus.setStage(ONLINE);
		verificationEventStatus.setEventType(AUTO);
		verificationEventStatus.setCaseNo(caseNumber);
		verificationEventStatus.setRequestId(newCaseSpecificRecordDetail.getCaseSpecificDetailId());
		verificationEventStatusRepository.save(verificationEventStatus);
	}

	private void createReviewProcessAttempt(String apiName, CaseSpecificRecordDetail newCaseSpecificRecordDetail,
			String caseNumber) {

		AttemptHistory attemptHistory = new AttemptHistory();

		String l3Response = "";
		String l3Status = "";

		attemptHistory.setAttemptDescription("Result received to be Assigned");
		attemptHistory.setAttemptStatusid((long) 68);
		attemptHistory.setName(PRODUCT_NAME + newCaseSpecificRecordDetail.getProduct() + APINAME + apiName);
		attemptHistory.setCheckid(newCaseSpecificRecordDetail.getInstructionCheckId());
		attemptHistory.setFollowupId((long) 40);
		attemptHistory.setRequestid(newCaseSpecificRecordDetail.getCaseSpecificDetailId());
		attemptHistory.setContactDate(new Date().toString());
		attemptHistory.setL3Response(l3Response);
		attemptHistory.setL3Status(l3Status);

		AttemptHistory newAttemptHistory = attemptHistoryRepository.save(attemptHistory);

		AttemptStatusData attemptStatusData = new AttemptStatusData();
		attemptStatusData.setAttemptId(newAttemptHistory.getAttemptid());
		attemptStatusData.setDepositionId((long) 14);
		attemptStatusData.setEndstatusId((long) 40);
		attemptStatusData.setModeId((long) 14);
		attemptStatusDataRepository.save(attemptStatusData);

		VerificationEventStatus verificationEventStatus = new VerificationEventStatus();
		verificationEventStatus.setStatus(DAY_0);
		verificationEventStatus.setEvent("Result received to be Assigned");
		verificationEventStatus.setCheckId(newCaseSpecificRecordDetail.getInstructionCheckId());
		verificationEventStatus.setStage(ONLINE);
		verificationEventStatus.setEventType(AUTO);
		verificationEventStatus.setCaseNo(caseNumber);
		verificationEventStatus.setRequestId(newCaseSpecificRecordDetail.getCaseSpecificDetailId());
		verificationEventStatusRepository.save(verificationEventStatus);
	}

	private void createDiscrepantProcessAttempt(String apiName, CaseSpecificRecordDetail newCaseSpecificRecordDetail,
			String caseNumber) {

		AttemptHistory attemptHistory = new AttemptHistory();

		String l3Response = "";
		String l3Status = "";

		attemptHistory.setAttemptDescription(RECORD_FOUND);
		attemptHistory.setAttemptStatusid((long) 69);
		attemptHistory.setName(PRODUCT_NAME + newCaseSpecificRecordDetail.getProduct() + APINAME + apiName);
		attemptHistory.setCheckid(newCaseSpecificRecordDetail.getInstructionCheckId());
		attemptHistory.setFollowupId((long) 40);
		attemptHistory.setRequestid(newCaseSpecificRecordDetail.getCaseSpecificDetailId());
		attemptHistory.setExecutiveSummary(RECORD_FOUND);
		attemptHistory.setContactDate(new Date().toString());
		attemptHistory.setL3Response(l3Response);
		attemptHistory.setL3Status(l3Status);

		AttemptHistory newAttemptHistory = attemptHistoryRepository.save(attemptHistory);

		AttemptStatusData attemptStatusData = new AttemptStatusData();
		attemptStatusData.setAttemptId(newAttemptHistory.getAttemptid());
		attemptStatusData.setDepositionId((long) 14);
		attemptStatusData.setEndstatusId((long) 40);
		attemptStatusData.setModeId((long) 14);
		attemptStatusDataRepository.save(attemptStatusData);

		VerificationEventStatus verificationEventStatus = new VerificationEventStatus();
		verificationEventStatus.setStatus(DAY_0);
		verificationEventStatus.setEvent(RECORD_FOUND);
		verificationEventStatus.setCheckId(newCaseSpecificRecordDetail.getInstructionCheckId());
		verificationEventStatus.setStage(ONLINE);
		verificationEventStatus.setEventType(AUTO);
		verificationEventStatus.setCaseNo(caseNumber);
		verificationEventStatus.setRequestId(newCaseSpecificRecordDetail.getCaseSpecificDetailId());
		verificationEventStatusRepository.save(verificationEventStatus);
	}

	@Override
	public void processRecordsForCost(ObjectMapper mapper, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo) {
		
		try {
			sendDataToL3Cost(caseSpecificRecordDetail, caseSpecificInfo);
		} catch (JsonProcessingException e) {
			logger.info("Exception while sending for cost approval:{}",e.getMessage());
		}
		
	}
	
	private String sendDataToL3Cost(CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo) throws JsonProcessingException {
		// Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JsonNode recordNode = caseSpecificRecordDetail.getCostApprovalRecord();
		String checkId = caseSpecificRecordDetail.getInstructionCheckId();
		String costRemarks = recordNode.has("Cost_Remarks") ? recordNode.get("Cost_Remarks").asText() : "";
		//Take Cost key
		String costAmmount = recordNode.has("Cost_Amt") ? recordNode.get("Cost_Amt").asText() : "";
		TaskSpecsPOJO taskSpecs = new TaskSpecsPOJO();
		L3CaseReferencePOJO caseReference = getL3CaseReferenceCost(caseSpecificRecordDetail, caseSpecificInfo, mapper,
				checkId);

		CheckVerificationPOJO checkVerification = getCheckVerificationCost(caseSpecificRecordDetail, costRemarks,costAmmount);

		taskSpecs.setCaseReference(caseReference);
		taskSpecs.setCheckVerification(checkVerification);

		taskSpecs.setQuestionaire(new ArrayList<>());

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

	private CheckVerificationPOJO getCheckVerificationCost(CaseSpecificRecordDetail caseSpecificRecordDetail,
			String costRemarks,String costAmmount) {
		CheckVerificationPOJO checkVerification = new CheckVerificationPOJO();
		checkVerification.setCountry("India");
		checkVerification.setExecutiveSummaryComments(costRemarks);
		checkVerification.setReportComments(costRemarks);
		checkVerification.setResultCode("");
		checkVerification.setEmailId("archana.bala@fadv.com");
		checkVerification.setVerifierName("");
		checkVerification.setGeneralRemarks("");
		checkVerification.setModeOfVerification(ONLINE);
		checkVerification.setVerifiedDate("");
		checkVerification.setAction("costapproval");
		checkVerification.setSubAction("");
		checkVerification.setActionCode("");
		checkVerification.setComponentName(caseSpecificRecordDetail.getComponentName());
		checkVerification.setProductName(caseSpecificRecordDetail.getProduct());
		checkVerification.setAttempts("");
		checkVerification.setDepartmentName("");
		checkVerification.setKeyWords("");
		checkVerification.setCost(costAmmount);
		checkVerification.setIsThisAVerificationAttempt("No");
		checkVerification.setInternalNotes(costRemarks);
		checkVerification.setDateVerificationCompleted("");
		checkVerification.setDisposition("");
		checkVerification.setExpectedClosureDate("");
		checkVerification.setEndStatusOfTheVerification("Work in Progress");
		checkVerification.setVerifierDesignation("");
		checkVerification.setFollowUpDateAndTimes("");
		checkVerification.setVerifierNumber("");
		checkVerification.setMiRemarks("");
		return checkVerification;
	}

	private L3CaseReferencePOJO getL3CaseReferenceCost(CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo, ObjectMapper mapper, String checkId) throws JsonProcessingException {
		String caseRefenceStr = caseSpecificInfo.getCaseReference() != null ? caseSpecificInfo.getCaseReference()
				: "{}";
		JsonNode caseReferenceNode = mapper.readTree(caseRefenceStr);
		L3CaseReferencePOJO caseReference = mapper.convertValue(caseReferenceNode, L3CaseReferencePOJO.class);

		caseReference.setCheckId(checkId);
		caseReference.setNgStatus("CA-RQ");
		caseReference.setNgStatusDescription("Cost Approval - Requested");
		caseReference.setSbuName(caseSpecificInfo.getSbuName());
		caseReference.setProductName(caseSpecificRecordDetail.getProduct());
		caseReference.setPackageName(caseSpecificInfo.getPackageName());
		caseReference.setComponentName(caseSpecificRecordDetail.getComponentName());
		return caseReference;
	}
}
