package fadv.verification.workflow.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

import fadv.verification.workflow.model.BotRequestHistory;
import fadv.verification.workflow.model.CaseSpecificInfo;
import fadv.verification.workflow.model.CaseSpecificRecordDetail;
import fadv.verification.workflow.model.L3ApiRequestHistory;
import fadv.verification.workflow.pojo.CaseReferencePOJO;
import fadv.verification.workflow.pojo.CheckPOJO;
import fadv.verification.workflow.pojo.DatumPOJO;
import fadv.verification.workflow.pojo.ResultPOJO;
import fadv.verification.workflow.pojo.RootPOJO;
import fadv.verification.workflow.repository.BotRequestHistoryRepository;
import fadv.verification.workflow.repository.CaseSpecificInfoRepository;
import fadv.verification.workflow.repository.CaseSpecificRecordDetailRepository;
import fadv.verification.workflow.repository.L3ApiRequestHistoryRepository;
import fadv.verification.workflow.utility.Utility;

@Service
public class BotRequestServiceImpl implements BotRequestService {

	private static final String RECORDS = "Records";

	private static final String REQUEST_RECEIVED = "Request received";

	private static final String FAILED = "Failed";

	private static final String SUCCESS = "success";

	@Value("${l3.checkid.url}")
	private String l3CheckIdUrl;

	@Value("${l3.casenumber.url}")
	private String l3CaseNumberUrl;

	@Value("${l3.checkid.url2}")
	private String l3CheckIdUrl2;
	@Value("${costing.url}")
	private String costingUrl;
	@Value("${waiting.time}")
	private Integer waitingTime;

	@Autowired
	private ApiService apiService;

	@Autowired
	private RouterService routerService;

	@Autowired
	private CaseSpecificInfoRepository caseSpecificInfoRepository;

	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;

	@Autowired
	private BotRequestHistoryRepository botRequestHistoryRepository;

	@Autowired
	private L3ApiRequestHistoryRepository l3ApiRequestHistoryRepository;

	@Value("${data.entry.l3.url}")
	private String l3DataEntryURL;

	private static final String RESPONSE = "response";
	private static final String COMPONENT_CHECK_ID_DATA = "componentCheckIdData";
	private static final String COMPONENT_SCOPING = "ComponentScoping";
	private static final String DATA = "data";
	private static final String RESULT = "result";
	private static final String COMPONENTS = "Components";
	private static final String DE_DATA = "deData";

	private static final Logger logger = LoggerFactory.getLogger(BotRequestServiceImpl.class);

	@Override
	public void submitBotRequest(ObjectMapper mapper, RootPOJO rootPOJO) {
		logger.info("Wait for a minute before proceeding with request");
		try {

			TimeUnit.SECONDS.sleep(waitingTime);
		} catch (InterruptedException e) {
			logger.error("Waiting Time Exception: {}", e.getMessage());
			Thread.currentThread().interrupt();
		}
		logger.info("Bot request started");
		processBotRequest(mapper, rootPOJO);
	}

	public BotRequestHistory saveIncomingRequest(RootPOJO rootPOJO, String caseNumber) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JsonNode rootNode = mapper.convertValue(rootPOJO, JsonNode.class);

		BotRequestHistory botRequestHistory = new BotRequestHistory();
		botRequestHistory.setCaseNumber(caseNumber);
		botRequestHistory.setRequestBody(rootNode);
		botRequestHistory.setRequestStatus(REQUEST_RECEIVED);
		botRequestHistory.setCreatedDate(new Date());
		botRequestHistory.setUpdatedDate(new Date());
		botRequestHistory.setEmailDate(new Date());
		botRequestHistory.setRetryCount(0);

		ObjectNode statusNode = mapper.createObjectNode();
		statusNode.put(SUCCESS, true);
		statusNode.put("message", SUCCESS);
		statusNode.put("statusCode", 200);

		botRequestHistory.setResponseBody(statusNode);

		List<BotRequestHistory> botRequestHistories = botRequestHistoryRepository.findByCaseNumber(caseNumber);
		if (CollectionUtils.isNotEmpty(botRequestHistories)) {
			BotRequestHistory newBotRequestHistory = botRequestHistories.get(0);
			botRequestHistory.setBotRequestId(newBotRequestHistory.getBotRequestId());
			botRequestHistory.setCreatedDate(newBotRequestHistory.getCreatedDate());
			botRequestHistory.setRetryCount(newBotRequestHistory.getRetryCount() + 1);
		}
		return botRequestHistoryRepository.save(botRequestHistory);
	}

	@Override
	public void updateIncomingRequestStatus(String caseNumber, String requestStatus) {
		List<BotRequestHistory> botRequestHistories = botRequestHistoryRepository.findByCaseNumber(caseNumber);
		if (CollectionUtils.isNotEmpty(botRequestHistories)) {
			BotRequestHistory newBotRequestHistory = botRequestHistories.get(0);
			newBotRequestHistory.setRequestStatus(requestStatus);
			newBotRequestHistory.setUpdatedDate(new Date());
			botRequestHistoryRepository.save(newBotRequestHistory);
		}
	}

	@Override
	public ObjectNode respondBotRequest(RootPOJO rootPOJO) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		List<DatumPOJO> datumPOJOs = rootPOJO.getData();
		BotRequestHistory botRequestHistory = new BotRequestHistory();
		if (CollectionUtils.isNotEmpty(datumPOJOs)) {
			DatumPOJO datumPOJO = datumPOJOs.get(0);
			ResultPOJO resultPOJO = datumPOJO.getResult();
			CaseReferencePOJO caseReferencePOJO = resultPOJO.getCaseReference();
			botRequestHistory = saveIncomingRequest(rootPOJO, caseReferencePOJO.getCaseNo());
		}
		ObjectNode statusNode = mapper.createObjectNode();
		if (botRequestHistory.getResponseBody() == null) {
			statusNode.put(SUCCESS, false);
			statusNode.put("message", "failed");
			statusNode.put("statusCode", 400);
		} else {
			statusNode = (ObjectNode) botRequestHistory.getResponseBody();
		}
		return statusNode;
	}

	@Override
	public RootPOJO processBotRequest(ObjectMapper mapper, RootPOJO rootPOJO) {
		logger.info("Got request in processBotRequest at : {}", new Date());
		List<DatumPOJO> datumPOJOs = rootPOJO.getData();
		if (CollectionUtils.isNotEmpty(datumPOJOs)) {
			DatumPOJO datumPOJO = datumPOJOs.get(0);
			ResultPOJO resultPOJO = datumPOJO.getResult();
			CaseReferencePOJO caseReferencePOJO = resultPOJO.getCaseReference();
			List<CheckPOJO> checkPOJOs = resultPOJO.getChecks();

			if (checkPOJOs != null && CollectionUtils.isNotEmpty(checkPOJOs)) {
				CaseSpecificInfo caseSpecificInfo = processCaseReference(mapper, caseReferencePOJO);

				ObjectNode otherDetails = mapper.createObjectNode();
				otherDetails.put("End Time", datumPOJO.getMetrics().getEndTime());
				otherDetails.put("No. of Checks", checkPOJOs.size());

				if (caseSpecificInfo != null) {
					List<CaseSpecificRecordDetail> caseSpecificRecordDetails = processChecksPojos(mapper, checkPOJOs,
							caseSpecificInfo);
					logger.info("case specific record details list : {}", caseSpecificRecordDetails);
					if (CollectionUtils.isNotEmpty(caseSpecificRecordDetails)) {
						routerService.processCaseRecordDetails(caseSpecificRecordDetails, caseSpecificInfo,
								otherDetails);
					} else {
						updateIncomingRequestStatus(caseReferencePOJO.getCaseNo(), FAILED);
					}
				} else {
					updateIncomingRequestStatus(caseReferencePOJO.getCaseNo(), FAILED);
				}
			} else {
				updateIncomingRequestStatus(caseReferencePOJO.getCaseNo(), FAILED);
			}
		}
		return rootPOJO;
	}

	private CaseSpecificInfo processCaseReference(ObjectMapper mapper, CaseReferencePOJO caseReferencePOJO) {
		logger.info("Got request in processCaseReference at : {}", new Date());
		String caseNumber = caseReferencePOJO.getCaseNo() != null ? caseReferencePOJO.getCaseNo() : "";
		if (StringUtils.isNotEmpty(caseNumber)) {
			String requestUrl = l3CaseNumberUrl + caseNumber + l3CheckIdUrl2;
			String caseNumberResponse = apiService.sendDataToL3Get(requestUrl);
			caseNumberResponse = Utility.formatString(caseNumberResponse);

			saveL3RequestResponse(mapper, caseNumberResponse, caseNumber, "NONE", requestUrl);
			logger.info("Response from l3 by using case number : {}", caseNumberResponse);

			String dataEntryURL = l3DataEntryURL + caseNumber;
			String dataEntryResponse = apiService.sendDataToL3Get(dataEntryURL);
			dataEntryResponse = Utility.formatString(dataEntryResponse);

			saveL3RequestResponse(mapper, dataEntryResponse, caseNumber, "NONE", dataEntryURL);
			logger.info("Response data entry from l3 by using case number : {}", dataEntryResponse);

			if (caseNumberResponse != null && dataEntryResponse != null) {
				return processCaseNumberResponse(caseNumberResponse, dataEntryResponse);
			}
		}
		return null;
	}

	private void saveL3RequestResponse(ObjectMapper mapper, String responseStr, String caseNumber, String checkId,
			String requestUrl) {
		JsonNode l3Response = mapper.createObjectNode();
		try {
			l3Response = responseStr != null ? mapper.readTree(responseStr) : mapper.createObjectNode();
		} catch (JsonProcessingException e) {
			logger.error("Exception occurred while mapping l3response : {}", e.getMessage());
			l3Response = mapper.createObjectNode();
		}

		L3ApiRequestHistory l3ApiRequestHistory = new L3ApiRequestHistory();
		l3ApiRequestHistory.setCaseNumber(caseNumber);
		l3ApiRequestHistory.setL3Response(l3Response);
		l3ApiRequestHistory.setRequestUrl(requestUrl);
		l3ApiRequestHistory.setCheckId(checkId);
		l3ApiRequestHistory.setCreatedDate(new Date());
		l3ApiRequestHistory.setUpdatedDate(new Date());

		l3ApiRequestHistoryRepository.save(l3ApiRequestHistory);
	}

	private CaseSpecificInfo processCaseNumberResponse(String caseNumberResponse, String dataEntryResponse) {
		logger.info("Got request in processCaseNumberResponse at : {}", new Date());
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		try {
			JsonNode caseRefNode = mapper.readValue(caseNumberResponse, JsonNode.class);

			JsonNode responseNode = caseRefNode.has(RESPONSE) ? caseRefNode.get(RESPONSE) : mapper.createObjectNode();

			JsonNode componentCheckIdData = responseNode.has(COMPONENT_CHECK_ID_DATA)
					? responseNode.get(COMPONENT_CHECK_ID_DATA)
					: mapper.createObjectNode();

			ArrayNode dataList = componentCheckIdData.has(DATA) ? (ArrayNode) componentCheckIdData.get(DATA)
					: mapper.createArrayNode();

			JsonNode dataNode = dataList.isEmpty() ? mapper.createObjectNode() : dataList.get(0);

			JsonNode resultNode = dataNode.has(RESULT) ? dataNode.get(RESULT) : mapper.createObjectNode();

			ArrayNode componentScopingList = resultNode.has(COMPONENT_SCOPING)
					? (ArrayNode) resultNode.get(COMPONENT_SCOPING)
					: mapper.createArrayNode();

			JsonNode componentScoping = componentScopingList.isEmpty() ? mapper.createObjectNode()
					: componentScopingList.get(0);

			return processCaseComponentScopingNode(mapper, componentScoping, dataEntryResponse);

		} catch (JsonProcessingException e) {
			logger.info("Exception occure while mapping response from l3 by using case number : {}", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	private CaseSpecificInfo processCaseComponentScopingNode(ObjectMapper mapper, JsonNode componentScoping,
			String dataEntryResponse) throws JsonProcessingException {

		logger.info("Got request in processCaseComponentScopingNode : {}", new Date());
		CaseSpecificInfo caseSpecificInfo = new CaseSpecificInfo();
		String caseNumber = componentScoping.has("CASE_NUMBER") ? componentScoping.get("CASE_NUMBER").asText() : "";

		if (StringUtils.isNotEmpty(caseNumber)) {
			setCaseSpecificInfo(mapper, componentScoping, caseSpecificInfo, caseNumber, dataEntryResponse);

			List<CaseSpecificInfo> newCaseSpecificInfos = caseSpecificInfoRepository
					.findByCaseNumberOrderByCaseSpecificIdDesc(caseNumber);

			if (CollectionUtils.isNotEmpty(newCaseSpecificInfos)) {
				CaseSpecificInfo newCaseSpecificInfo = newCaseSpecificInfos.get(0);
				caseSpecificInfo.setCaseSpecificId(newCaseSpecificInfo.getCaseSpecificId());
				caseSpecificInfo.setCreatedDate(newCaseSpecificInfo.getCreatedDate());
			} else {
				caseSpecificInfo.setCreatedDate(new Date());
			}

			return caseSpecificInfoRepository.save(caseSpecificInfo);
		}
		return null;

	}

	private void setCaseSpecificInfo(ObjectMapper mapper, JsonNode componentScoping, CaseSpecificInfo caseSpecificInfo,
			String caseNumber, String dataEntryResponse) throws JsonProcessingException {

		logger.info("Got request in setCaseSpecificInfo at : {}", new Date());
		String deDataStr = extractDeDataStr(mapper, dataEntryResponse);

		caseSpecificInfo.setCaseReference(
				componentScoping.has("caseReference") ? mapper.writeValueAsString(componentScoping.get("caseReference"))
						: "{}");
		caseSpecificInfo.setCaseMoreInfo(
				componentScoping.has("caseMoreInfo") ? mapper.writeValueAsString(componentScoping.get("caseMoreInfo"))
						: "{}");
		caseSpecificInfo.setCaseDetails(
				componentScoping.has("caseDetails") ? mapper.writeValueAsString(componentScoping.get("caseDetails"))
						: "{}");
		caseSpecificInfo.setDataEntryInfo(deDataStr);

		caseSpecificInfo.setClientSpecificFields(componentScoping.has("clientSpecificFields")
				? mapper.writeValueAsString(componentScoping.get("clientSpecificFields"))
				: "{}");
		caseSpecificInfo.setCaseNumber(caseNumber);
		caseSpecificInfo.setCaseRefNumber(
				componentScoping.has("CASE_REF_NUMBER") ? componentScoping.get("CASE_REF_NUMBER").asText() : "");
		caseSpecificInfo
				.setClientCode(componentScoping.has("CLIENT_CODE") ? componentScoping.get("CLIENT_CODE").asText() : "");
		caseSpecificInfo
				.setClientName(componentScoping.has("CLIENT_NAME") ? componentScoping.get("CLIENT_NAME").asText() : "");
		caseSpecificInfo.setSbuName(componentScoping.has("SBU_NAME") ? componentScoping.get("SBU_NAME").asText() : "");
		caseSpecificInfo.setCandidateName(
				componentScoping.has("Candidate_Name") ? componentScoping.get("Candidate_Name").asText() : "");
		caseSpecificInfo.setPackageName(
				componentScoping.has("Package Name") ? componentScoping.get("Package Name").asText() : "");
		caseSpecificInfo.setCrnCreationDate(
				componentScoping.has("CRNCreationDate") ? componentScoping.get("CRNCreationDate").asText() : "");
		caseSpecificInfo.setStatus("");
		caseSpecificInfo.setUpdatedDate(new Date());
	}

	private String extractDeDataStr(ObjectMapper mapper, String dataEntryResponse) {
		logger.info("Got request in extractDeDataStr at : {}", new Date());

		JsonNode dataEntryNode = mapper.createObjectNode();
		try {
			dataEntryNode = mapper.readTree(dataEntryResponse);
		} catch (JsonProcessingException e) {
			logger.error("Error while mapping data entry response : {}", e.getMessage());
			dataEntryNode = mapper.createObjectNode();
		}
		dataEntryNode = dataEntryNode != null ? dataEntryNode : mapper.createObjectNode();

		JsonNode responseNode = dataEntryNode.has(RESPONSE) ? dataEntryNode.get(RESPONSE) : mapper.createObjectNode();
		JsonNode deDataNode = responseNode.has(DE_DATA) ? responseNode.get(DE_DATA) : mapper.createObjectNode();

		try {
			return mapper.writeValueAsString(deDataNode);
		} catch (JsonProcessingException e) {
			logger.error("Exception occurred while converting deData to String : {}", e.getMessage());
			return "{}";
		}
	}

	private List<CaseSpecificRecordDetail> processChecksPojos(ObjectMapper mapper, List<CheckPOJO> checkPOJOs,
			CaseSpecificInfo caseSpecificInfo) {
		logger.info("Got request in processChecksPojos at : {}", new Date());
		List<CaseSpecificRecordDetail> caseSpecificRecordDetails = new ArrayList<>();
		Long caseSpecificInfoId = caseSpecificInfo.getCaseSpecificId();

		for (CheckPOJO checkPOJO : checkPOJOs) {
			String checkId = checkPOJO.getCheckID() != null ? checkPOJO.getCheckID() : "";
			if (StringUtils.isNotEmpty(checkId)) {
				String requestUrl = l3CheckIdUrl + checkId + l3CheckIdUrl2;
				String checkIdResponse = apiService.sendDataToL3Get(requestUrl);
				checkIdResponse = Utility.formatString(checkIdResponse);

				saveL3RequestResponse(mapper, checkIdResponse, caseSpecificInfo.getCaseNumber(), checkId, requestUrl);
				logger.info("Response from l3 by using check id : {}", checkIdResponse);

				if (checkIdResponse != null) {

					CaseSpecificRecordDetail caseSpecificRecordDetail = processCheckIdResponse(checkIdResponse,
							caseSpecificInfoId);
					if (caseSpecificRecordDetail != null) {
						caseSpecificRecordDetail.setIsCheckManual(false);
						caseSpecificRecordDetails.add(caseSpecificRecordDetail);
					}
				}
			}
		}
		if (CollectionUtils.isNotEmpty(caseSpecificRecordDetails)) {
			return caseSpecificRecordDetailRepository.saveAll(caseSpecificRecordDetails);
		}
		return new ArrayList<>();
	}

	private CaseSpecificRecordDetail processCheckIdResponse(String checkIdResponse, Long caseSpecificInfoId) {
		logger.info("Got request in processCheckIdResponse at : {}", new Date());
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		try {
			JsonNode checkIdResNode = mapper.readValue(checkIdResponse, JsonNode.class);

			JsonNode responseNode = checkIdResNode.has(RESPONSE) ? checkIdResNode.get(RESPONSE)
					: mapper.createObjectNode();

			JsonNode componentCheckIdData = responseNode.has(COMPONENT_CHECK_ID_DATA)
					? responseNode.get(COMPONENT_CHECK_ID_DATA)
					: mapper.createObjectNode();

			ArrayNode dataList = componentCheckIdData.has(DATA) ? (ArrayNode) componentCheckIdData.get(DATA)
					: mapper.createArrayNode();

			JsonNode dataNode = dataList.isEmpty() ? mapper.createObjectNode() : dataList.get(0);

			JsonNode resultNode = dataNode.has(RESULT) ? dataNode.get(RESULT) : mapper.createObjectNode();

			ArrayNode componentScopingList = resultNode.has(COMPONENT_SCOPING)
					? (ArrayNode) resultNode.get(COMPONENT_SCOPING)
					: mapper.createArrayNode();

			JsonNode componentScoping = componentScopingList.isEmpty() ? mapper.createObjectNode()
					: componentScopingList.get(0);

			ArrayNode componentList = componentScoping.has(COMPONENTS) ? (ArrayNode) componentScoping.get(COMPONENTS)
					: mapper.createArrayNode();

			JsonNode component = componentList.isEmpty() ? mapper.createObjectNode() : componentList.get(0);

			return processCheckComponentNode(mapper, component, caseSpecificInfoId, componentCheckIdData);

		} catch (JsonProcessingException e) {
			logger.info("Exception occure while mapping response from l3 by using check id : {}", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	private CaseSpecificRecordDetail processCheckComponentNode(ObjectMapper mapper, JsonNode component,
			Long caseSpecificId, JsonNode componentCheckIdData) throws JsonProcessingException {
		logger.info("Got request in processCheckComponentNode at : {}", new Date());
		// Logic for calling costing Engine URL
		JsonNode costApprovalNode = processComponentCheckIdData(mapper, componentCheckIdData);
		String costApproval = costApprovalNode.has("Cost") ? costApprovalNode.get("Cost").asText() : "";

		ArrayNode recordList = component.has(RECORDS) ? (ArrayNode) component.get(RECORDS) : mapper.createArrayNode();

		JsonNode recordNode = recordList.isEmpty() ? mapper.createObjectNode() : recordList.get(0);

		String checkId = recordNode.has("checkId") ? recordNode.get("checkId").asText() : "";

		String checkStatus = "Open";

		String isCeDmn = recordNode.has("isCe_DMN") ? recordNode.get("isCe_DMN").asText() : "";
		String isCePending = recordNode.has("Is CE Pending?") ? recordNode.get("Is CE Pending?").asText() : "";

		String miTag = recordNode.has("MI") ? recordNode.get("MI").asText() : "";
		if (costApproval.equalsIgnoreCase("Yes")) {
			checkStatus = "Cost Pending";
		} else {
			if (StringUtils.containsIgnoreCase(isCeDmn, "Yes") || StringUtils.containsIgnoreCase(isCePending, "Yes")) {
				checkStatus = "CE Pending";
			}
			if (StringUtils.equalsIgnoreCase(miTag, "Yes")) {
				checkStatus = "Pending";
			}
		}
		

		if (StringUtils.isNotEmpty(checkId)) {
			return setCaseSpecificDetails(mapper, component, caseSpecificId, recordNode, checkId, checkStatus,
					costApprovalNode);
		}
		return null;
	}

	private CaseSpecificRecordDetail setCaseSpecificDetails(ObjectMapper mapper, JsonNode component,
			Long caseSpecificId, JsonNode recordNode, String checkId, String checkStatus, JsonNode costApprovalNode)
			throws JsonProcessingException {
		logger.info("Got request in setCaseSpecificDetails at : {}", new Date());
		CaseSpecificRecordDetail caseSpecificRecordDetail = new CaseSpecificRecordDetail();
		caseSpecificRecordDetail.setInstructionCheckId(checkId);
		caseSpecificRecordDetail
				.setComponentName(component.has("Component name") ? component.get("Component name").asText() : "");
		caseSpecificRecordDetail.setProduct(component.has("PRODUCT") ? component.get("PRODUCT").asText() : "");
		caseSpecificRecordDetail.setComponentRecordField(mapper.writeValueAsString(recordNode));
		caseSpecificRecordDetail.setCostApprovalRecord(costApprovalNode);
		caseSpecificRecordDetail.setCheckStatus(checkStatus);

		caseSpecificRecordDetail.setCaseSpecificId(caseSpecificId);

		caseSpecificRecordDetail.setUpdatedDate(new Date());

		List<CaseSpecificRecordDetail> newCaseSpecificRecordDetails = caseSpecificRecordDetailRepository
				.findByInstructionCheckIdOrderByCaseSpecificDetailIdDesc(checkId);

		String akaName = recordNode.has("Aka Name") ? recordNode.get("Aka Name").asText() : "";
		String thirdPartyCompany = recordNode.has("Third party Company(city)")
				? recordNode.get("Third party Company(city)").asText()
				: "";
		String city = recordNode.has("City") ? recordNode.get("City").asText() : "";

		caseSpecificRecordDetail.setFunctionalEntityName(akaName);

		caseSpecificRecordDetail.setEntityLocation(thirdPartyCompany);

		if (caseSpecificRecordDetail.getEntityLocation() == null
				|| caseSpecificRecordDetail.getEntityLocation().isEmpty()) {
			caseSpecificRecordDetail.setEntityLocation(city);
		}

		if (CollectionUtils.isNotEmpty(newCaseSpecificRecordDetails)) {
			CaseSpecificRecordDetail newCaseSpecificRecordDetail = newCaseSpecificRecordDetails.get(0);
			caseSpecificRecordDetail.setCaseSpecificDetailId(newCaseSpecificRecordDetail.getCaseSpecificDetailId());
			caseSpecificRecordDetail.setCheckCreatedDate(newCaseSpecificRecordDetail.getCheckCreatedDate());
			caseSpecificRecordDetail.setCheckStatus(newCaseSpecificRecordDetail.getCheckStatus());
			caseSpecificRecordDetail.setOnlineStatus(newCaseSpecificRecordDetail.getOnlineStatus());
			caseSpecificRecordDetail.setCbvUtvStatus(newCaseSpecificRecordDetail.getCbvUtvStatus());
			caseSpecificRecordDetail.setSpocStatus(newCaseSpecificRecordDetail.getSpocStatus());
			caseSpecificRecordDetail.setVendorStatus(newCaseSpecificRecordDetail.getVendorStatus());
			caseSpecificRecordDetail.setStellarStatus(newCaseSpecificRecordDetail.getStellarStatus());
			caseSpecificRecordDetail.setWellknownStatus(newCaseSpecificRecordDetail.getWellknownStatus());
			caseSpecificRecordDetail.setSuspectStatus(newCaseSpecificRecordDetail.getSuspectStatus());
		} else {
			caseSpecificRecordDetail.setCheckCreatedDate(new Date());
		}

		return caseSpecificRecordDetail;
	}

//	private void processComponentCheckIdData(ObjectMapper mapper, JsonNode componentCheckIdData,String costApproval,JsonNode costApprovalNode) {
	private JsonNode processComponentCheckIdData(ObjectMapper mapper, JsonNode componentCheckIdData) {
		JsonNode costApprovalNode = mapper.createObjectNode();
		String componentCheckIdDataStr = componentCheckIdData.toString();
		componentCheckIdDataStr = componentCheckIdDataStr.replace(RESULT, "taskSpecs");
		logger.info("Request to Costing URL : {}", componentCheckIdDataStr);
		String costingUrlResponse = apiService.sendDataToPost(costingUrl, componentCheckIdDataStr);
		if (costingUrlResponse != null) {
			try {
				JsonNode costingUrlResponseNode = mapper.readTree(costingUrlResponse);
				ArrayNode dataList = costingUrlResponseNode.has(DATA) ? (ArrayNode) costingUrlResponseNode.get(DATA)
						: mapper.createArrayNode();

				JsonNode dataNode = dataList.isEmpty() ? mapper.createObjectNode() : dataList.get(0);

				JsonNode resultNode = dataNode.has(RESULT) ? dataNode.get(RESULT) : mapper.createObjectNode();

				ArrayNode componentScopingList = resultNode.has(COMPONENT_SCOPING)
						? (ArrayNode) resultNode.get(COMPONENT_SCOPING)
						: mapper.createArrayNode();

				JsonNode componentScoping = componentScopingList.isEmpty() ? mapper.createObjectNode()
						: componentScopingList.get(0);

				ArrayNode componentList = componentScoping.has(COMPONENTS)
						? (ArrayNode) componentScoping.get(COMPONENTS)
						: mapper.createArrayNode();

				JsonNode component = componentList.isEmpty() ? mapper.createObjectNode() : componentList.get(0);

				ArrayNode recordList = component.has(RECORDS) ? (ArrayNode) component.get(RECORDS)
						: mapper.createArrayNode();

				JsonNode recordNode = recordList.isEmpty() ? mapper.createObjectNode() : recordList.get(0);
				costApprovalNode = recordNode;
			} catch (JsonProcessingException e) {
				logger.info("Parsing Issuue for Costing Engine Url:{}", e.getMessage());
			}
		}
		return costApprovalNode;
	}

}
