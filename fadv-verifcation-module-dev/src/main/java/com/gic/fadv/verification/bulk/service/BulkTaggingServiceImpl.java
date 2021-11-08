package com.gic.fadv.verification.bulk.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.verification.attempts.model.AttemptDeposition;
import com.gic.fadv.verification.attempts.model.AttemptFollowupMaster;
import com.gic.fadv.verification.attempts.model.AttemptHistory;
import com.gic.fadv.verification.attempts.model.AttemptStatus;
import com.gic.fadv.verification.attempts.model.AttemptStatusData;
import com.gic.fadv.verification.attempts.model.AttemptVerificationModes;
import com.gic.fadv.verification.attempts.model.CaseSpecificInfo;
import com.gic.fadv.verification.attempts.model.CaseSpecificRecordDetail;
import com.gic.fadv.verification.attempts.repository.AttemptDepositionRepository;
import com.gic.fadv.verification.attempts.repository.AttemptFollowupMasterRepository;
import com.gic.fadv.verification.attempts.repository.AttemptMasterRepository;
import com.gic.fadv.verification.attempts.repository.AttemptStatusDataRepository;
import com.gic.fadv.verification.attempts.repository.AttemptStatusRepository;
import com.gic.fadv.verification.attempts.repository.AttemptVerificationModesRepository;
import com.gic.fadv.verification.attempts.repository.CaseSpecificInfoRepository;
import com.gic.fadv.verification.attempts.repository.CaseSpecificRecordDetailRepository;
import com.gic.fadv.verification.bulk.interfaces.TemplateDetailsInterface;
import com.gic.fadv.verification.bulk.model.BulkTemplateHeaders;
import com.gic.fadv.verification.bulk.pojo.BotDataPOJO;
import com.gic.fadv.verification.bulk.pojo.BotMetadataPOJO;
import com.gic.fadv.verification.bulk.pojo.BotTaskSpecsPOJO;
import com.gic.fadv.verification.bulk.pojo.BulkBotVerifiyPOJO;
import com.gic.fadv.verification.bulk.pojo.BulkCaseReferencePOJO;
import com.gic.fadv.verification.bulk.pojo.BulkTemplateHeaderFieldsPOJO;
import com.gic.fadv.verification.bulk.pojo.BulkTemplateHeadersPOJO;
import com.gic.fadv.verification.bulk.pojo.ClientDetailsPOJO;
import com.gic.fadv.verification.bulk.pojo.ComponentDetailsPOJO;
import com.gic.fadv.verification.bulk.pojo.FileUploadPOJO;
import com.gic.fadv.verification.bulk.pojo.PackageDetailsPOJO;
import com.gic.fadv.verification.bulk.pojo.ProductNamePOJO;
import com.gic.fadv.verification.bulk.pojo.RequestProcessorPOJO;
import com.gic.fadv.verification.bulk.pojo.SbuDetailsPOJO;
import com.gic.fadv.verification.bulk.repository.BulkTemplateHeadersRepository;
import com.google.common.collect.ImmutableList;

@Service
public class BulkTaggingServiceImpl implements BulkTaggingService {

	private static final String ATTEMPTS = "attempts";

	private static final String CASE_REF = "Case Ref #";

	private static final String CHECK_ID = "Check Id";

	private static final String RESPONSE = "response";

	private static final String L3_ERROR_RESPONSE = "{\"Error\": \"Error as reponse from L3\"}";

	@Autowired
	private BulkApiService bulkApiService;

	@Autowired
	private BulkTemplateHeadersRepository bulkTemplateHeadersRepository;

	@Autowired
	private CaseSpecificInfoRepository caseSpecificInfoRepository;

	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;

	@Autowired
	private AttemptMasterRepository attemptMasterRepository;

	@Autowired
	private AttemptVerificationModesRepository attemptVerificationModesRepository;

	@Autowired
	private AttemptDepositionRepository attemptDepositionRepository;

	@Autowired
	private AttemptFollowupMasterRepository attemptFollowupMasterRepository;

	@Autowired
	private AttemptStatusRepository attemptStatusRepository;

	@Autowired
	private AttemptStatusDataRepository attemptStatusDataRepository;

	private static final Logger logger = LoggerFactory.getLogger(BulkTaggingServiceImpl.class);

	@Value("${bulk.tagging.client.l3.url}")
	private String clientNameListUrl;

	@Value("${bulk.tagging.sbu.l3.url}")
	private String sbuNameUrl;

	@Value("${bulk.tagging.package.l3.url}")
	private String packageNameUrl;

	@Value("${component.list.L3.url}")
	private String componentNameUrl;

	@Value("${bulk.tagging.product.name}")
	private String productNameUrl;

	@Value("${request.processor.url}")
	private String requestProcessorUrl;

	ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Override
	public List<ClientDetailsPOJO> getClientDetailsFromL3() {
		String l3Response = bulkApiService.sendDataToL3Get(clientNameListUrl);
		if (l3Response == null || StringUtils.isEmpty(l3Response)) {
			return new ArrayList<>();
		}
		try {
			JsonNode responseNode = mapper.readTree(l3Response);

			if (responseNode.has(RESPONSE)) {
				List<ClientDetailsPOJO> clientDetailsPOJOs = mapper.convertValue(responseNode.get(RESPONSE),
						new TypeReference<List<ClientDetailsPOJO>>() {
						});

				if (clientDetailsPOJOs != null && CollectionUtils.isNotEmpty(clientDetailsPOJOs)) {
					return clientDetailsPOJOs;
				}
			}

		} catch (JsonProcessingException e) {
			logger.error("Exception while mapping L3 response for clientNames : {} : {}", clientNameListUrl,
					e.getMessage());
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	@Override
	public List<SbuDetailsPOJO> getSbuNameFromL3(String clientName) {
		String requestStr = sbuNameUrl.replace("<<clientName>>", clientName);
		String l3Response = bulkApiService.sendDataToL3Get(requestStr);

		if (l3Response == null || StringUtils.isEmpty(l3Response)) {
			return new ArrayList<>();
		}
		try {
			JsonNode responseNode = mapper.readTree(l3Response);

			if (responseNode.has(RESPONSE)) {
				List<SbuDetailsPOJO> sbuDetailsPOJOs = mapper.convertValue(responseNode.get(RESPONSE),
						new TypeReference<List<SbuDetailsPOJO>>() {
						});

				if (sbuDetailsPOJOs != null && CollectionUtils.isNotEmpty(sbuDetailsPOJOs)) {
					return sbuDetailsPOJOs;
				}
			}

		} catch (JsonProcessingException e) {
			logger.error("Exception while mapping L3 response for sbuNames : {} : {}", requestStr, e.getMessage());
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	@Override
	public List<PackageDetailsPOJO> getPackageNameFromL3(String clientName, String sbuName) {
		String requestStr = packageNameUrl.replace("<<clientName>>", clientName);
		requestStr = requestStr.replace("<<sbuName>>", sbuName);
		String l3Response = bulkApiService.sendDataToL3Get(requestStr);

		if (l3Response == null || StringUtils.isEmpty(l3Response)) {
			return new ArrayList<>();
		}
		try {
			JsonNode responseNode = mapper.readTree(l3Response);

			if (responseNode.has(RESPONSE)) {
				List<PackageDetailsPOJO> packageDetailsPOJOs = mapper.convertValue(responseNode.get(RESPONSE),
						new TypeReference<List<PackageDetailsPOJO>>() {
						});

				if (packageDetailsPOJOs != null && CollectionUtils.isNotEmpty(packageDetailsPOJOs)) {
					return packageDetailsPOJOs;
				}
			}

		} catch (JsonProcessingException e) {
			logger.error("Exception while mapping L3 response for packageName : {} : {}", requestStr, e.getMessage());
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	@Override
	public List<ComponentDetailsPOJO> getComponentNameList() {
		String l3Response = bulkApiService.sendDataToL3Get(componentNameUrl);
		if (l3Response == null || StringUtils.isEmpty(l3Response)) {
			return new ArrayList<>();
		}
		try {
			JsonNode responseNode = mapper.readTree(l3Response);

			if (responseNode.has(RESPONSE)) {
				List<ComponentDetailsPOJO> componentDetailsPOJOs = mapper.convertValue(responseNode.get(RESPONSE),
						new TypeReference<List<ComponentDetailsPOJO>>() {
						});

				if (componentDetailsPOJOs != null && CollectionUtils.isNotEmpty(componentDetailsPOJOs)) {
					return componentDetailsPOJOs;
				}
			}

		} catch (JsonProcessingException e) {
			logger.error("Exception while mapping L3 response for componentNames : {} : {}", componentNameUrl,
					e.getMessage());
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	@Override
	public List<ProductNamePOJO> getProductNameList(String componentName) {
		String requestStr = productNameUrl.replace("<<componentName>>", componentName);
		String l3Response = bulkApiService.sendDataToL3Get(requestStr);
		if (l3Response == null || StringUtils.isEmpty(l3Response)) {
			return new ArrayList<>();
		}
		try {
			JsonNode responseNode = mapper.readTree(l3Response);

			if (responseNode.has(RESPONSE)) {
				List<ProductNamePOJO> productNamePOJOs = mapper.convertValue(responseNode.get(RESPONSE),
						new TypeReference<List<ProductNamePOJO>>() {
						});

				if (productNamePOJOs != null && CollectionUtils.isNotEmpty(productNamePOJOs)) {
					return productNamePOJOs;
				}
			}

		} catch (JsonProcessingException e) {
			logger.error("Exception while mapping L3 response for productNames : {} : {}", requestStr, e.getMessage());
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	@Override
	public ResponseEntity<ObjectNode> createTemplateHeader(BulkTemplateHeadersPOJO bulkTemplateHeadersPOJO) {

		BulkTemplateHeaders bulkTemplateHeaders = new BulkTemplateHeaders();

		bulkTemplateHeaders.setCreatedDate(new Date());
		String componentName = bulkTemplateHeadersPOJO.getComponentName();

		if (componentName != null && StringUtils.isNotEmpty(componentName)) {
			List<BulkTemplateHeaders> bulkTemplateHeadersList = bulkTemplateHeadersRepository
					.findByComponentName(componentName);
			if (CollectionUtils.isNotEmpty(bulkTemplateHeadersList)) {
				bulkTemplateHeaders = bulkTemplateHeadersList.get(0);
			}
		}
		bulkTemplateHeaders.setStatus(bulkTemplateHeadersPOJO.getStatus());
		bulkTemplateHeaders.setTemplateDescription(bulkTemplateHeadersPOJO.getTemplateDescription());
		bulkTemplateHeaders.setTemplateName(bulkTemplateHeadersPOJO.getTemplateName());
		bulkTemplateHeaders.setTemplateHeader(bulkTemplateHeadersPOJO.getTemplateHeader());
		bulkTemplateHeaders.setComponentName(bulkTemplateHeadersPOJO.getComponentName());
		bulkTemplateHeaders.setUpdatedDate(new Date());

		bulkTemplateHeadersRepository.save(bulkTemplateHeaders);

		return ResponseEntity.ok(createResponse(true, "Template Created", mapper.createArrayNode()));
	}

	@Override
	public List<TemplateDetailsInterface> getTemplateNameList() {
		List<TemplateDetailsInterface> templateNameList = bulkTemplateHeadersRepository.getAllTemplateName();
		return templateNameList != null ? templateNameList : new ArrayList<>();
	}

	@Override
	public ArrayNode getTemplateByName(String templateName) {
		List<BulkTemplateHeaders> bulkTemplateHeadersList = bulkTemplateHeadersRepository
				.findByTemplateName(templateName);
		if (bulkTemplateHeadersList != null && CollectionUtils.isNotEmpty(bulkTemplateHeadersList)) {
			return bulkTemplateHeadersList.get(0).getTemplateHeader();
		}
		return mapper.createArrayNode();
	}

	@Override
	public List<TemplateDetailsInterface> getTemplateByComponentName(String componentName) {
		List<TemplateDetailsInterface> templateNameList = bulkTemplateHeadersRepository
				.getAllTemplateNameByComponent(componentName);
		return templateNameList != null ? templateNameList : new ArrayList<>();
	}

	@Override
	public String getMappedTemplate(BulkTemplateHeadersPOJO bulkTemplateHeadersPOJO) {

		String componentName = bulkTemplateHeadersPOJO.getComponentName();

		if (componentName != null && StringUtils.isNotEmpty(componentName)) {
			List<BulkTemplateHeaders> bulkTemplateHeadersList = bulkTemplateHeadersRepository
					.findByComponentName(componentName);
			if (CollectionUtils.isNotEmpty(bulkTemplateHeadersList)) {
				return bulkTemplateHeadersList.get(0).getTemplateHeader().toString();
			}
		}

		return "[]";

	}

	@Override
	public ResponseEntity<ObjectNode> saveBulkTaggingData(BulkTemplateHeadersPOJO bulkTemplateHeadersPOJO) {
		return validateBulkTaggingData(bulkTemplateHeadersPOJO.getTemplateHeader(),
				bulkTemplateHeadersPOJO.getBulkTemplateHeadersId(), bulkTemplateHeadersPOJO.getComponentName());
	}

	public ResponseEntity<ObjectNode> validateBulkTaggingData(ArrayNode dataArr, Long templateId,
			String componentName) {
		Optional<BulkTemplateHeaders> bulkTemplateHeadersOpt = bulkTemplateHeadersRepository.findById(templateId);
		List<JsonNode> successData = new ArrayList<>();
		ArrayNode failedData = mapper.createArrayNode();
		if (bulkTemplateHeadersOpt.isPresent()) {
			BulkTemplateHeaders bulkTemplateHeaders = bulkTemplateHeadersOpt.get();
			ArrayNode bulkTemplateHeadersArr = bulkTemplateHeaders.getTemplateHeader() != null
					? bulkTemplateHeaders.getTemplateHeader()
					: mapper.createArrayNode();
			List<BulkTemplateHeaderFieldsPOJO> bulkTemplateHeaderFieldsPOJOs = mapper
					.convertValue(bulkTemplateHeadersArr, new TypeReference<List<BulkTemplateHeaderFieldsPOJO>>() {
					});
			if (CollectionUtils.isEmpty(bulkTemplateHeaderFieldsPOJOs)) {
				return ResponseEntity.badRequest()
						.body(createResponse(false, "No template header found", mapper.createArrayNode()));
			}

			checkFieldsInTemplate(dataArr, successData, failedData, bulkTemplateHeaderFieldsPOJOs);
			processSuccessData(successData, componentName);
			if (failedData.isEmpty()) {
				return ResponseEntity.ok(createResponse(true, "Tagging Completed", mapper.createArrayNode()));
			} else {
				return ResponseEntity.ok(createResponse(true, "Tagging Completed", failedData));
			}
		} else {
			return ResponseEntity.badRequest()
					.body(createResponse(false, "Invalid Template Mapping ID", mapper.createArrayNode()));
		}
	}

	private void checkFieldsInTemplate(ArrayNode dataArr, List<JsonNode> successData, ArrayNode failedData,
			List<BulkTemplateHeaderFieldsPOJO> bulkTemplateHeaderFieldsPOJOs) {
		for (JsonNode dataNode : dataArr) {
			List<String> dataKeys = ImmutableList.copyOf(dataNode.fieldNames());
			boolean isFailed = false;
			for (BulkTemplateHeaderFieldsPOJO bulkTemplateHeaderFieldsPOJO : bulkTemplateHeaderFieldsPOJOs) {
				String fieldName = bulkTemplateHeaderFieldsPOJO.getFieldName();
				if ((!dataKeys.contains(fieldName) && Boolean.TRUE.equals(bulkTemplateHeaderFieldsPOJO.isMandate()))
						|| (dataKeys.contains(fieldName)
								&& Boolean.TRUE.equals(bulkTemplateHeaderFieldsPOJO.isMandate())
								&& (dataNode.get(fieldName) == null
										|| StringUtils.isEmpty(dataNode.get(fieldName).asText())))) {
					isFailed = true;
					break;
				}
			}
			if (isFailed) {
				failedData.add(dataNode);
			} else {
				successData.add(dataNode);
			}
		}
	}

	private ObjectNode createResponse(Boolean success, String message, ArrayNode data) {
		ObjectNode resposneNode = mapper.createObjectNode();
		resposneNode.put("success", success);
		resposneNode.put("message", message);
		resposneNode.set("data", data);
		return resposneNode;
	}

	private void processSuccessData(List<JsonNode> successData, String componentName) {
		logger.info("Success Data : {}", successData);
		List<String> crnNoList = successData.stream().map(data -> data.has(CASE_REF) ? data.get(CASE_REF).asText() : "")
				.collect(Collectors.toList());
		crnNoList = new ArrayList<>(new HashSet<>(crnNoList));
		logger.info("crnNoList : {}", crnNoList);
		for (String crnNo : crnNoList) {
			CaseSpecificInfo caseSpecificInfo = new CaseSpecificInfo();
			List<CaseSpecificInfo> caseSpecificInfoList = caseSpecificInfoRepository.findByCaseRefNumber(crnNo);
			if (CollectionUtils.isNotEmpty(caseSpecificInfoList)) {
				caseSpecificInfo = caseSpecificInfoList.get(0);
			}
			List<JsonNode> checkDetailsList = successData.stream()
					.filter(data -> data.get(CASE_REF) != null
							&& StringUtils.equalsIgnoreCase(data.get(CASE_REF).asText(), crnNo))
					.collect(Collectors.toList());
			try {
				if (caseSpecificInfo != null && caseSpecificInfo.getCaseReference() != null
						&& StringUtils.isNotEmpty(caseSpecificInfo.getCaseReference())) {
					createVerifyJson(checkDetailsList, caseSpecificInfo, componentName);
				}
			} catch (JsonProcessingException e) {
				logger.error("Exception occured while mapping case reference from database : {}", e.getMessage());
			}
		}
	}

	private void createVerifyJson(List<JsonNode> checkDetailsList, CaseSpecificInfo caseSpecificInfo,
			String componentName) throws JsonProcessingException {
		BulkBotVerifiyPOJO bulkBotVerifiyPOJO = new BulkBotVerifiyPOJO();
		BotMetadataPOJO botMetadataPOJO = new BotMetadataPOJO();
		BotDataPOJO botDataPOJO = new BotDataPOJO();
		BotTaskSpecsPOJO botTaskSpecsPOJO = new BotTaskSpecsPOJO();

		botTaskSpecsPOJO
				.setCaseReference(mapper.readValue(caseSpecificInfo.getCaseReference(), BulkCaseReferencePOJO.class));

		List<String> checkIdList = checkDetailsList.stream()
				.map(data -> data.has(CHECK_ID) ? data.get(CHECK_ID).asText() : "").collect(Collectors.toList());
		botTaskSpecsPOJO.setCheckId(checkIdList);

		JsonNode checkIdNode = checkDetailsList.get(0);

		botTaskSpecsPOJO.setBatchProcess(getBatchProcessAndFileUpload(checkIdNode, componentName));

		FileUploadPOJO fileUploadPOJO = new FileUploadPOJO();
		fileUploadPOJO
				.setDirectory(checkIdNode.has("fileUpload path") ? checkIdNode.get("fileUpload path").asText() : "");
		fileUploadPOJO.setBatchUploadDocument(new ArrayList<>());
		botTaskSpecsPOJO.setFileUpload(fileUploadPOJO);
		// Set Value for Other Fields Also
		botDataPOJO.setTaskSpecs(botTaskSpecsPOJO);
		botDataPOJO.setTaskBy("BO");
		botDataPOJO.setTaskId("");
		botDataPOJO.setTaskName("CSPIBatchProcess");
		botDataPOJO.setTaskSerialNo("");
		List<BotDataPOJO> botDataPOJOList = new ArrayList<>();
		botDataPOJOList.add(botDataPOJO);

		botMetadataPOJO.setAttempt("");
		botMetadataPOJO.setMultiTask("");
		botMetadataPOJO.setProcessId("");
		botMetadataPOJO.setProcessName("");
		botMetadataPOJO.setRequestAuthToken("");
		botMetadataPOJO.setRequestDate("");
		botMetadataPOJO.setRequestId("");
		botMetadataPOJO.setRequestType("");
		botMetadataPOJO.setStageId("");
		botMetadataPOJO.setStageName("");
		botMetadataPOJO.setTask("CSPIBatchProcess");
		botMetadataPOJO.setTaskGroupId("");
		botMetadataPOJO.setTxLabel(caseSpecificInfo.getCaseNumber());
		botMetadataPOJO.setVersion("");
		bulkBotVerifiyPOJO.setData(botDataPOJOList);
		bulkBotVerifiyPOJO.setMetadata(botMetadataPOJO);

		RequestProcessorPOJO requestProcessorPOJO = new RequestProcessorPOJO();
		requestProcessorPOJO.setRequestJson(mapper.convertValue(bulkBotVerifiyPOJO, JsonNode.class));
		requestProcessorPOJO.setRequestType("verify");

		String verifyJsonString = mapper.writeValueAsString(requestProcessorPOJO);
		logger.info("Value of Verify Json : {}", verifyJsonString);
		// Save This verify Json into Database

		// bulkVerifyPOJO.setCaseReference(mapper.readValue(caseSpecificInfo.getCaseReference(),
		// BulkCaseReferencePOJO.class));
		// BulkVerifyPOJO bulkVerifyPOJO = new BulkVerifyPOJO();
		// bulkVerifyPOJO.setCheckId(checkIdList);
		// bulkVerifyPOJO.setBatchProcess(getBatchProcessAndFileUpload(checkIdNode,
		// componentName));
		// bulkVerifyPOJO.setFileUpload(fileUploadPOJO);
		// String verifyJsonString = mapper.writeValueAsString(bulkVerifyPOJO);

		String l3VerifyResponse = bulkApiService.sendDataToPost(requestProcessorUrl, verifyJsonString);

		if (l3VerifyResponse == null) {
			l3VerifyResponse = L3_ERROR_RESPONSE;
		}

		logger.info("verifyResponse : {}", l3VerifyResponse);

		createAttempt(checkIdNode, getL3Status(l3VerifyResponse), l3VerifyResponse, checkIdList);
	}

	private String getL3Status(String l3Response) {
		if (StringUtils.equalsIgnoreCase(l3Response, L3_ERROR_RESPONSE)) {
			return "failed";
		} else {
			return "success";
		}
	}

	private ObjectNode getBatchProcessAndFileUpload(JsonNode checkIdNode, String componentName) {

		List<String> excludeKeys = Arrays.asList(CHECK_ID, CASE_REF, "Uploadfiletype", "fileUpload path");
		List<String> dataKeys = ImmutableList.copyOf(checkIdNode.fieldNames());

		ObjectNode batchProcessNode = mapper.createObjectNode();
		batchProcessNode.put("componentName", componentName);
		for (String keyName : dataKeys) {
			if (!excludeKeys.contains(keyName)) {
				batchProcessNode.put(keyName, checkIdNode.has(keyName) ? checkIdNode.get(keyName).asText() : "");
			}
		}

		return batchProcessNode;
	}

	private void createAttempt(JsonNode checkIdNode, String l3Status, String l3Response, List<String> checkIdList) {

		for (String checkId : checkIdList) {

			String attemptStatus = checkIdNode.has(ATTEMPTS) ? checkIdNode.get(ATTEMPTS).asText() : "";
			String followUpStatus = checkIdNode.has("ngStatusDescription")
					? checkIdNode.get("ngStatusDescription").asText()
					: "";
			String deposition = checkIdNode.has("disposition") ? checkIdNode.get("disposition").asText() : "";
			String verificationMode = checkIdNode.has("modeOfVerification")
					? checkIdNode.get("modeOfVerification").asText()
					: "";

			AttemptHistory attemptHistory = new AttemptHistory();

			CaseSpecificRecordDetail caseSpecificRecordDetails = caseSpecificRecordDetailRepository
					.findTopByInstructionCheckId(checkId);

			attemptHistory.setRequestid(caseSpecificRecordDetails.getCaseSpecificDetailId());
			attemptHistory.setCheckid(checkId);
			attemptHistory.setName(checkIdNode.has("verifierName") ? checkIdNode.get("verifierName").asText() : "");
			attemptHistory.setEmailAddress(checkIdNode.has("emailId") ? checkIdNode.get("emailId").asText() : "");
			attemptHistory.setAttemptDescription(checkIdNode.has(ATTEMPTS) ? checkIdNode.get(ATTEMPTS).asText() : "");
			attemptHistory.setClosureExpectedDate(
					checkIdNode.has("expectedClosureDate") ? checkIdNode.get("expectedClosureDate").asText() : "");
			attemptHistory.setCreateDate(new Date());
			attemptHistory.setExecutiveSummary(
					checkIdNode.has("executiveSummaryComments") ? checkIdNode.get("executiveSummaryComments").asText()
							: "");
			attemptHistory.setL3Response(l3Response);
			attemptHistory.setL3Status(l3Status);

			getAttemptMasterDetailsId(attemptStatus, followUpStatus, deposition, verificationMode, attemptHistory);
		}
	}

	private void getAttemptMasterDetailsId(String attemptStatus, String followUpStatus, String deposition,
			String verificationMode, AttemptHistory attemptHistory) {
		Long verificationModeId = (long) 14;
		Long attemptStatusId = (long) 0;
		Long followUpId = (long) 0;
		Long dispositionId = (long) 13;
		if (StringUtils.isNotEmpty(verificationMode)) {
			AttemptVerificationModes attemptVerificationModes = attemptVerificationModesRepository
					.findTopByVerificationMode(verificationMode);
			if (attemptVerificationModes != null) {
				verificationModeId = attemptVerificationModes.getVerificationModeId();
			}
		}
		if (StringUtils.isNotEmpty(attemptStatus)) {
			AttemptStatus attemptStatusData = attemptStatusRepository.findTopByAttemptStatus(attemptStatus);
			if (attemptStatusData != null) {
				attemptStatusId = attemptStatusData.getAttemptStatusid();
			}
		}
		if (StringUtils.isNotEmpty(followUpStatus)) {
			AttemptFollowupMaster attemptFollowupMaster = attemptFollowupMasterRepository
					.findTopByFollowupStatus(followUpStatus);
			if (attemptFollowupMaster != null) {
				followUpId = attemptFollowupMaster.getFollowupId();
			}
		}
		if (StringUtils.isNotEmpty(deposition)) {
			AttemptDeposition attemptDeposition = attemptDepositionRepository.findTopByDepositionName(deposition);
			if (attemptDeposition != null) {
				dispositionId = attemptDeposition.getDepositionId();
			}
		}
		if (followUpId != 0) {
			attemptHistory.setFollowupId(followUpId);
		}
		if (attemptStatusId != 0) {
			attemptHistory.setAttemptStatusid(attemptStatusId);
		}
		AttemptHistory attemptHistoryNew = attemptMasterRepository.save(attemptHistory);

		createAttemptStatusData(verificationModeId, followUpId, dispositionId, attemptHistoryNew);
	}

	private void createAttemptStatusData(Long verificationModeId, Long followUpId, Long dispositionId,
			AttemptHistory attemptHistoryNew) {
		AttemptStatusData attemptStatusData = new AttemptStatusData();
		attemptStatusData.setAttemptId(attemptHistoryNew.getAttemptid());

		if (dispositionId != 0) {
			attemptStatusData.setDepositionId(dispositionId);
		}

		if (verificationModeId != 0) {
			attemptStatusData.setModeId(verificationModeId);
		}
		if (followUpId != 0) {
			attemptStatusData.setEndstatusId(followUpId);
			attemptStatusDataRepository.save(attemptStatusData);
		}
	}

//	@Override
//	public ResponseEntity<ObjectNode> saveBulkTaggingDataMap(BulkTemplateMappingPOJO bulkTemplateMappingPOJO) {
//		BulkTemplateMapping bulkTemplateMapping = mapper.convertValue(bulkTemplateMappingPOJO,
//				BulkTemplateMapping.class);
//		bulkTemplateMapping.setCreatedDate(new Date());
//
//		BulkTemplateMapping bulkTemplateMappingNew = bulkTemplateMappingRepository.getTemplateId(
//				bulkTemplateMapping.getClientName(), bulkTemplateMapping.getSbuName(),
//				bulkTemplateMapping.getPackageName(), bulkTemplateMapping.getComponentName(),
//				bulkTemplateMapping.getProductName());
//		logger.info("test : {}", bulkTemplateMappingNew);
//		if (bulkTemplateMappingNew != null) {
//			Long id = bulkTemplateMappingNew.getBulkTemplateMappingId() != null
//					? bulkTemplateMappingNew.getBulkTemplateMappingId()
//					: 0;
//			logger.info("Existing Template Id : {}", id);
//			if (id != 0) {
//				bulkTemplateMapping.setBulkTemplateMappingId(id);
//				bulkTemplateMapping.setCreatedDate(bulkTemplateMappingNew.getCreatedDate());
//			}
//		}
//
//		bulkTemplateMapping.setUpdatedDate(new Date());
//
//		bulkTemplateMappingRepository.save(bulkTemplateMapping);
//
//		return ResponseEntity.ok(createResponse(true, "Successfully Mapped", mapper.createArrayNode()));
//	}
}
