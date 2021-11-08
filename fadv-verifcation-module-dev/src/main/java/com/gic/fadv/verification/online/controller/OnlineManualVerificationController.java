package com.gic.fadv.verification.online.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.collections4.CollectionUtils;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.verification.attempts.service.APIService;
import com.gic.fadv.verification.online.model.OnlineManualVerification;
import com.gic.fadv.verification.online.model.OnlineVerificationChecks;
import com.gic.fadv.verification.online.repository.OnlineManualVerificationRepository;
import com.gic.fadv.verification.online.repository.OnlineVerificationChecksRepository;
import com.gic.fadv.verification.online.service.OnlineManualVerificationService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class OnlineManualVerificationController {
	@Autowired
	private OnlineManualVerificationRepository onlineManualVerificationRepository;
	
	@Autowired
	private OnlineManualVerificationService onlineManualVerificationService;

	@Autowired
	private APIService apiService;

	@Value("${associate.docs.case.url}")
	private String associateDocsCaseUrl;

	@Value("${associate.filepaths.rest.url}")
	private String associateFilePathUrl;

	@Value("${doc.url}")
	private String docUrl;

	@Autowired
	private OnlineVerificationChecksRepository onlineVerificationChecksRepository;

	private static final Logger logger = LoggerFactory.getLogger(OnlineManualVerificationController.class);

	@PostMapping("/online-manual-verification-search")
	public List<OnlineManualVerification> getAllOnlineManualVerification(@RequestBody JsonNode requestBody) {
		logger.info("request : {}", requestBody);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		requestBody = requestBody != null ? requestBody : mapper.createObjectNode();
		String clientName = requestBody.has("clientName") ? requestBody.get("clientName").asText() : "";
		String candidateName = requestBody.has("candidateName") ? requestBody.get("candidateName").asText() : "";
		String crnNo = requestBody.has("crnNo") ? requestBody.get("crnNo").asText() : "";
		String fromDate = requestBody.has("fromDate") ? requestBody.get("fromDate").asText() : "";
		String toDate = requestBody.has("toDate") ? requestBody.get("toDate").asText() : "";
		String caseNumber = requestBody.has("caseNumber") ? requestBody.get("caseNumber").asText() : "";
		String sbu = requestBody.has("sbu") ? requestBody.get("sbu").asText() : "";
		String packageName = requestBody.has("packageName") ? requestBody.get("packageName").asText() : "";
		candidateName = StringUtils.isEmpty(candidateName) ? candidateName : "%" + candidateName + "%";
		clientName = StringUtils.isEmpty(clientName) ? clientName : "%" + clientName + "%";
		sbu = StringUtils.isEmpty(sbu) ? sbu : "%" + sbu + "%";
		packageName = StringUtils.isEmpty(packageName) ? packageName : "%" + packageName + "%";
		fromDate = StringUtils.equalsIgnoreCase(fromDate, "null") ? "" : fromDate;
		toDate = StringUtils.equalsIgnoreCase(toDate, "null") ? "" : toDate;

		return onlineManualVerificationRepository.getByFilter(fromDate, toDate, caseNumber, clientName, crnNo,
				candidateName, sbu, packageName);
	}

	@PostMapping("/online-manual-verification")
	public OnlineManualVerification createOnlineManualVerification(
			@Valid @RequestBody OnlineManualVerification onlineManualVerification) {
		if (onlineManualVerification.getOnlineManualVerificationId() != null) {
			// Logic for Update
			onlineManualVerification
					.setOnlineManualVerificationId(onlineManualVerification.getOnlineManualVerificationId());
			onlineManualVerification.setStatus(onlineManualVerification.getStatus());
		}
		onlineManualVerification.setSBU(onlineManualVerification.getSBU());
		onlineManualVerification.setPackageName(onlineManualVerification.getPackageName());
		onlineManualVerification.setClientName(onlineManualVerification.getClientName());
		onlineManualVerification.setUpdatedTime(onlineManualVerification.getUpdatedTime());
		onlineManualVerification.setTimeCreation(new Date().toString());
		onlineManualVerification.setCaseNumber(onlineManualVerification.getCaseNumber());
		onlineManualVerification.setStatus("manual");
		onlineManualVerification.setCrnNo(onlineManualVerification.getCrnNo());
		onlineManualVerification.setCandidateName(onlineManualVerification.getCandidateName());
		return onlineManualVerificationRepository.save(onlineManualVerification);
	}

	@PostMapping("/onlineapi-manual-verification")
	public String saveOnlineManualVerification(@Valid @RequestBody String onlineManualVerificationStr)
			throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JsonNode onlineManualNode = mapper.readTree(onlineManualVerificationStr);

		if (onlineManualNode != null) {
			OnlineManualVerification onlineManualVerification = new OnlineManualVerification();
			if (onlineManualNode.has("sbu")) {
				onlineManualVerification.setSBU(onlineManualNode.get("sbu").asText());
			}
			if (onlineManualNode.has("packageName")) {
				onlineManualVerification.setPackageName(onlineManualNode.get("packageName").asText());
			}
			if (onlineManualNode.has("clientName")) {
				onlineManualVerification.setClientName(onlineManualNode.get("clientName").asText());
			}
			if (onlineManualNode.has("updatedTime")) {
				onlineManualVerification.setUpdatedTime(onlineManualNode.get("updatedTime").asText());
			}

			if (onlineManualNode.has("caseNumber")) {
				onlineManualVerification.setCaseNumber(onlineManualNode.get("caseNumber").asText());
			}
			if (onlineManualNode.has("crn")) {
				onlineManualVerification.setCrnNo(onlineManualNode.get("crn").asText());
			}
			if (onlineManualNode.has("candidateName")) {
				onlineManualVerification.setCandidateName(onlineManualNode.get("candidateName").asText());
			}
			if (onlineManualNode.has("candidateName")) {
				onlineManualVerification.setCandidateName(onlineManualNode.get("candidateName").asText());
			}
			if (onlineManualNode.has("candidateName")) {
				onlineManualVerification.setCandidateName(onlineManualNode.get("candidateName").asText());
			}
			if (onlineManualNode.has("dataEntryResult")) {
				onlineManualVerification.setDataEntryResult(onlineManualNode.get("dataEntryResult").asText());
			}
			onlineManualVerification.setStatus("manual");
			onlineManualVerification.setUpdatedTime(new Date().toString());
			String caseNumber = onlineManualNode.has("caseNumber") ? onlineManualNode.get("caseNumber").asText() : "";
			if (!StringUtils.isEmpty(caseNumber)) {
				List<OnlineManualVerification> onlineManualVerificationsList = onlineManualVerificationRepository
						.findByCaseNumber(caseNumber);
				if (!onlineManualVerificationsList.isEmpty()) {
					onlineManualVerification.setOnlineManualVerificationId(
							onlineManualVerificationsList.get(0).getOnlineManualVerificationId());
					onlineManualVerification.setTimeCreation(onlineManualVerificationsList.get(0).getTimeCreation());
				}
			} else {
				if (onlineManualNode.has("timeCreation")) {
					onlineManualVerification.setTimeCreation(onlineManualNode.get("timeCreation").asText());
				}
			}

			if (onlineManualVerification.getTimeCreation() == null
					|| StringUtils.isEmpty(onlineManualVerification.getTimeCreation())
					|| StringUtils.equalsIgnoreCase(onlineManualVerification.getTimeCreation(), "null")) {
				onlineManualVerification.setTimeCreation(new Date().toString());
			}

			OnlineManualVerification newOnlineManualVerification = onlineManualVerificationRepository
					.save(onlineManualVerification);

			return processOnlineVerifyChecks(onlineManualNode,
					newOnlineManualVerification.getOnlineManualVerificationId());

		}
		return "Json is null";
	}

	private String processOnlineVerifyChecks(JsonNode onlineManualNode, Long onlineManualVerifyId) {
		ArrayNode onlineVerificationChecksListArrNode = (ArrayNode) onlineManualNode
				.get("onlineVerificationChecksList");
		List<OnlineVerificationChecks> onlineVerificationChecksList = getOnlineVerifyChecksFromArrayNode(
				onlineVerificationChecksListArrNode, onlineManualVerifyId);

		Map<String, OnlineVerificationChecks> onlineChecksMap = new HashMap<>();

		List<String> checkIdList = onlineVerificationChecksList.stream().map(OnlineVerificationChecks::getCheckId)
				.collect(Collectors.toList());
		checkIdList = new ArrayList<>(new HashSet<>(checkIdList));

		List<OnlineVerificationChecks> existingVerifcationChecks = onlineVerificationChecksRepository
				.getVerificationChecksByCheckIdList(checkIdList);

		for (OnlineVerificationChecks onlineVerificationChecks : existingVerifcationChecks) {
			onlineChecksMap.put(onlineVerificationChecks.getCheckId() + onlineVerificationChecks.getApiName(),
					onlineVerificationChecks);
		}

		logger.info("existing online checks : {} ", existingVerifcationChecks);

//		List<OnlineVerificationChecks> nonExistingVerifcationChecks = onlineVerificationChecksList.stream().filter(
//				e -> onlineVerificationChecksList.stream().anyMatch(d -> !e.getCheckId().equals(d.getCheckId())))
//				.collect(Collectors.toList());

		List<OnlineVerificationChecks> nonExistingVerifcationChecks = new ArrayList<>();

		if (CollectionUtils.isEmpty(existingVerifcationChecks)) {
			nonExistingVerifcationChecks = onlineVerificationChecksList;
		} else {
			for (OnlineVerificationChecks onlineVerificationChecks : onlineVerificationChecksList) {
				if (!onlineChecksMap
						.containsKey(onlineVerificationChecks.getCheckId() + onlineVerificationChecks.getApiName())) {
					nonExistingVerifcationChecks.add(onlineVerificationChecks);
				}
			}
		}

		logger.info("non existing online checks : {} ", nonExistingVerifcationChecks);

		if (!CollectionUtils.isEmpty(existingVerifcationChecks)) {
			updateVerifyChecksByCheckId(existingVerifcationChecks);
		}

		if (!CollectionUtils.isEmpty(nonExistingVerifcationChecks)) {
			onlineVerificationChecksRepository.saveAll(nonExistingVerifcationChecks);
		}
		return "Record Saved Successfully";
	}

	private void updateVerifyChecksByCheckId(List<OnlineVerificationChecks> existingVerifcationChecks) {
		for (OnlineVerificationChecks onlineVerificationChecks : existingVerifcationChecks) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			String checkId = onlineVerificationChecks.getCheckId() != null ? onlineVerificationChecks.getCheckId() : "";
			String finalResult = onlineVerificationChecks.getResult() != null ? onlineVerificationChecks.getResult()
					: "";
			String isPending = onlineVerificationChecks.getPendingStatus() != null
					? onlineVerificationChecks.getPendingStatus()
					: "";
			String retryNo = onlineVerificationChecks.getRetryNo() != null ? onlineVerificationChecks.getRetryNo() : "";
			String verifyId = onlineVerificationChecks.getVerifyId() != null ? onlineVerificationChecks.getVerifyId()
					: "";
			String matchedIdentifier = onlineVerificationChecks.getMatchedIdentifiers() != null
					? onlineVerificationChecks.getMatchedIdentifiers()
					: "";
			Long onlineManualId = onlineVerificationChecks.getOnlineManualVerificationId() != null
					? onlineVerificationChecks.getOnlineManualVerificationId()
					: 0;

			String outputFile = onlineVerificationChecks.getOutputFile() != null
					? onlineVerificationChecks.getOutputFile()
					: "";
			String apiName = onlineVerificationChecks.getApiName() != null ? onlineVerificationChecks.getApiName() : "";
			String updateDate = new Date().toString();
			if (onlineManualId != 0 && !StringUtils.isEmpty(apiName) && !StringUtils.isEmpty(checkId)) {
				onlineVerificationChecksRepository.updateVerifyChecksbyCheckId(checkId, finalResult, updateDate,
						isPending, retryNo, verifyId, matchedIdentifier, outputFile, onlineManualId, apiName);
			}
		}
	}

	private List<OnlineVerificationChecks> getOnlineVerifyChecksFromArrayNode(
			ArrayNode onlineVerificationChecksArrayNode, Long onlineManualVerifyId) {
		List<OnlineVerificationChecks> onlineVerificationChecksList = new ArrayList<>();

		for (JsonNode onlineVerificationChecksNode : onlineVerificationChecksArrayNode) {
			OnlineVerificationChecks onlineVerificationChecks = new OnlineVerificationChecks();
			onlineVerificationChecks.setOnlineManualVerificationId(onlineManualVerifyId);

			String checkId = onlineVerificationChecksNode.get("checkId") != null
					? onlineVerificationChecksNode.get("checkId").asText()
					: "";

			if (StringUtils.isNotEmpty(checkId)) {
				onlineVerificationChecks.setCheckId(checkId);
			}
			if (onlineVerificationChecksNode.has("apiName")) {
				onlineVerificationChecks.setApiName(onlineVerificationChecksNode.get("apiName").asText());
			}
			if (onlineVerificationChecksNode.has("result")) {
				onlineVerificationChecks.setResult(onlineVerificationChecksNode.get("result").asText());
			}
			if (onlineVerificationChecksNode.has("initialResult")) {
				onlineVerificationChecks.setInitialResult(onlineVerificationChecksNode.get("initialResult").asText());
			}
			if (onlineVerificationChecksNode.has("matchedIdentifiers")) {
				onlineVerificationChecks
						.setMatchedIdentifiers(onlineVerificationChecksNode.get("matchedIdentifiers").asText());
			}
			if (onlineVerificationChecksNode.has("inputFile")) {
				if (StringUtils.isNotEmpty(onlineVerificationChecksNode.get("inputFile").asText())) {
					onlineVerificationChecks.setInputFile(onlineVerificationChecksNode.get("inputFile").asText());
				} else {
					onlineVerificationChecks.setInputFile("{}");
				}
			}
			if (onlineVerificationChecksNode.has("outputFile")) {
				onlineVerificationChecks.setOutputFile(onlineVerificationChecksNode.get("outputFile").asText());
			}
			if (onlineVerificationChecksNode.has("createdDate")) {
				onlineVerificationChecks.setCreatedDate(onlineVerificationChecksNode.get("createdDate").asText());
			}
			if (onlineVerificationChecksNode.has("updatedDate")) {
				onlineVerificationChecks.setUpdatedDate(onlineVerificationChecksNode.get("updatedDate").asText());
			}
			if (onlineVerificationChecksNode.has("verifyId")) {
				onlineVerificationChecks.setVerifyId(onlineVerificationChecksNode.get("verifyId").asText());
			}
			if (onlineVerificationChecksNode.has("isPending")) {
				onlineVerificationChecks.setPendingStatus(onlineVerificationChecksNode.get("isPending").asText());
			}
			if (onlineVerificationChecksNode.has("retryNo")) {
				onlineVerificationChecks.setRetryNo(onlineVerificationChecksNode.get("retryNo").asText());
			}
			onlineVerificationChecksList.add(onlineVerificationChecks);
		}

		return onlineVerificationChecksList;
	}

	@GetMapping("/online-candidate-details/{id}")
	public ObjectNode getCandidateDetails(@PathVariable(value = "id") Long onlineManualVerificationId) {
		Optional<OnlineManualVerification> onlineManualVerificationList = onlineManualVerificationRepository
				.findById(onlineManualVerificationId);

		// Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		ObjectNode resultMapList = mapper.createObjectNode();

		if (onlineManualVerificationList.isPresent()) {
			OnlineManualVerification onlineManualVerification = onlineManualVerificationList.get();
			String dataEntryResult = onlineManualVerification.getDataEntryResult() != null
					? onlineManualVerification.getDataEntryResult()
					: "{}";

			JsonNode dataEntryResultNode = mapper.createObjectNode();
			try {
				dataEntryResultNode = mapper.readValue(dataEntryResult, JsonNode.class);
			} catch (JsonProcessingException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
				return mapper.createObjectNode();
			}
			dataEntryResultNode = dataEntryResultNode != null ? dataEntryResultNode : mapper.createObjectNode();

			String primaryName = dataEntryResultNode.has("primaryName")
					? dataEntryResultNode.get("primaryName").asText()
					: "";
			String secondaryName = dataEntryResultNode.has("secondaryName") ? dataEntryResultNode.get("secondaryName").asText() : "";
			String dob = dataEntryResultNode.has("dob") ? dataEntryResultNode.get("dob").asText() : "";
			String fathersName = dataEntryResultNode.has("fathersname")
					? dataEntryResultNode.get("fathersname").asText()
					: "";

			resultMapList.put("primaryName", primaryName);
			resultMapList.put("secondaryName", secondaryName);
			resultMapList.put("dob", dob);
			resultMapList.put("fathersName", fathersName);
		}
		return resultMapList;
	}

	@GetMapping("/online-order-details/{id}")
	public ObjectNode getOrderDetails(@PathVariable(value = "id") Long onlineManualVerificationId) {
		Optional<OnlineManualVerification> onlineManualVerificationList = onlineManualVerificationRepository
				.findById(onlineManualVerificationId);

		// Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		ObjectNode resultMapList = mapper.createObjectNode();

		if (onlineManualVerificationList.isPresent()) {
			OnlineManualVerification onlineManualVerification = onlineManualVerificationList.get();

			resultMapList.put("crnNo", onlineManualVerification.getCrnNo());
			resultMapList.put("clientName", onlineManualVerification.getClientName());
			resultMapList.put("sbu", onlineManualVerification.getSBU());
			resultMapList.put("packageName", onlineManualVerification.getPackageName());
			resultMapList.put("crnCreatedDate", onlineManualVerification.getTimeCreation());
		}
		return resultMapList;
	}

	@GetMapping("/mrl-file-list/{caseNumber}")
	public ArrayNode getMrlFileList(@PathVariable(value = "caseNumber") String caseNumber) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		ArrayNode responseArrayNode = mapper.createArrayNode();

		if (StringUtils.isNotEmpty(caseNumber)) {
			List<String> checkIdList = onlineVerificationChecksRepository.getOnlyCheckIdList(caseNumber);

			if (CollectionUtils.isNotEmpty(checkIdList)) {

				try {
					ObjectNode requestNode = mapper.createObjectNode();
					requestNode.put("caseNumber", caseNumber);
					requestNode.put("checkIdList", checkIdList.toString());
					String requestNodeStr = mapper.writeValueAsString(requestNode);

					String filePathMapStr = apiService.sendDataToAttempt(associateFilePathUrl, requestNodeStr);
					responseArrayNode = mapper.readValue(filePathMapStr, ArrayNode.class);

				} catch (JsonProcessingException e) {
					logger.error("Exception while reading response : {}", e.getMessage());
					e.printStackTrace();
					return responseArrayNode;
				}

			}
		}

		return responseArrayNode;
	}

	@PostMapping("/associate-filepaths")
	public ArrayNode getFilesFromAssociatePath(@RequestBody JsonNode requestBody) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		String caseNumber = requestBody.has("caseNumber") ? requestBody.get("caseNumber").asText() : "";
		String checkIdListStr = requestBody.has("checkIdList") ? requestBody.get("checkIdList").toString() : "";

		checkIdListStr = checkIdListStr.replace("[", "").replace("]", "").replace("\"", "");
		
		String[] split = checkIdListStr.split(",");
		List<String> checkIdList = Arrays.asList(split);
		checkIdList.replaceAll(String::trim);
		
		logger.info("checkIdList : {}", checkIdList);

		String associateDocsResponse = apiService.sendDataToget(associateDocsCaseUrl, caseNumber);

		logger.info("Value of AssociateDocsResponse : {}", associateDocsResponse);

		ArrayNode filePathMap = mapper.createArrayNode();
		JsonNode associateDocsResponseNode = mapper.createObjectNode();
		try {
			associateDocsResponseNode = mapper.readTree(associateDocsResponse);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception while parsing associate response : {}", e.getMessage());
		}

		if (associateDocsResponseNode != null) {
			JsonNode responseNode = associateDocsResponseNode.has("response")
					? associateDocsResponseNode.get("response")
					: mapper.createObjectNode();
			if (responseNode.has("associateDocs")) {
				ArrayNode associateDocsNodeList = mapper.createArrayNode();
				if (responseNode.get("associateDocs").isArray()) {
					associateDocsNodeList = (ArrayNode) responseNode.get("associateDocs");
				} else {
					associateDocsNodeList.add(responseNode.get("associateDocs"));
				}
				for (JsonNode associateDocsNode : associateDocsNodeList) {
					if (associateDocsNode != null && associateDocsNode.has("docsData")
							&& associateDocsNode.get("docsData").isArray()) {

						ArrayNode docsArrDataNode = (ArrayNode) associateDocsNode.get("docsData");

						for (JsonNode docsDataNode : docsArrDataNode) {
							if (docsDataNode.has("checkIds") && docsDataNode.get("checkIds").isArray()) {
								ArrayNode checkIdsNodeList = (ArrayNode) docsDataNode.get("checkIds");

								for (JsonNode checkIdsNode : checkIdsNodeList) {
									String docCheckId = checkIdsNode.has("checkId")
											? checkIdsNode.get("checkId").asText()
											: "";
									String filePath = docsDataNode.has("filePath")
											? docsDataNode.get("filePath").asText()
											: "";
									String documentName = docsDataNode.has("documentName")
											? docsDataNode.get("documentName").asText()
											: "";
									if (checkIdList.contains(docCheckId) && StringUtils.isNotEmpty(filePath)) {
										logger.info("CheckId Matched! Take filePath : {}", filePath);
										ObjectNode filePathNode = mapper.createObjectNode();
										filePathNode.put("checkId", docCheckId);
										filePathNode.put("documentName", documentName);
										filePathNode.put("filePath", filePath);
										filePathMap.add(filePathNode);
									} else {
										logger.info("CheckId is not Matched.");
									}
								}
							}
						}
					}
				}

			}
		}
		logger.info("Value of filePathArrNode : {}", filePathMap);

		return filePathMap;
	}
	
	@PostMapping("/mrl-document-list")
	public ArrayNode getMrlDocumentList(@RequestBody JsonNode requestBody) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		return onlineManualVerificationService.getFilesFromAssociatePath(requestBody);
	}
	
	@GetMapping("/product-name-list/{onlineManualVerificationId}")
	public List<String> getProductNameList(@PathVariable(value = "onlineManualVerificationId") Long onlineManualVerificationId) {
		return onlineManualVerificationRepository.getProductNameListByCaseNumber(onlineManualVerificationId);
	}
	
	@GetMapping("/checkid-list/{onlineManualVerificationId}")
	public List<String> getCheckIdList(@PathVariable(value = "onlineManualVerificationId") Long onlineManualVerificationId) {
		return onlineManualVerificationRepository.getCheckIdListByCaseNumber(onlineManualVerificationId);
	}
}
