package com.gic.fadv.verification.online.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import com.gic.fadv.verification.online.controller.OnlineManualVerificationController;

@Service
public class OnlineManualVerificationServiceImpl implements OnlineManualVerificationService {

	private static final Logger logger = LoggerFactory.getLogger(OnlineManualVerificationServiceImpl.class);

	@Autowired
	private OnlineApiService onlineApiService;
	
	@Value("${associate.docs.case.url}")
	private String associateDocsCaseUrl;
	
	@Value("${mrl.rule.description.url}")
	private String mrlRuleDescriptionUrl;
	
	@Override
	public ArrayNode getFilesFromAssociatePath(JsonNode requestBody) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		String caseNumber = requestBody.has("caseNumber") ? requestBody.get("caseNumber").asText() : "";
		String checkIdListStr = requestBody.has("checkIdList") ? requestBody.get("checkIdList").toString() : "";
		String componentName = requestBody.has("componentName") ? requestBody.get("componentName").asText() : "";
		String akaName = requestBody.has("akaName") ? requestBody.get("akaName").asText() : "";

		List<String> documentList = getMrlDocumentNames(mapper, componentName, akaName); 
		
		checkIdListStr = checkIdListStr.replace("[", "").replace("]", "").replace("\"", "");
		
		String[] split = checkIdListStr.split(",");
		List<String> checkIdList = Arrays.asList(split);
		checkIdList.replaceAll(String::trim);
		
		logger.info("checkIdList : {}", checkIdList);

		String associateDocsResponse = onlineApiService.sendDataToGet(associateDocsCaseUrl + caseNumber);

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

								List<String> filePathList = new ArrayList<>();
								for (JsonNode checkIdsNode : checkIdsNodeList) {
									String akaNameStr= checkIdsNode.has("akaName")
											? checkIdsNode.get("akaName").asText()
											: "";
									String docCheckId = checkIdsNode.has("checkId")
											? checkIdsNode.get("checkId").asText()
											: "";
									String filePath = docsDataNode.has("filePath")
											? docsDataNode.get("filePath").asText()
											: "";
									String documentName = docsDataNode.has("documentName")
											? docsDataNode.get("documentName").asText()
											: "";
									if (checkIdList.contains(docCheckId) && StringUtils.isNotEmpty(filePath)
											&& documentList.contains(documentName)) {
										logger.info("CheckId Matched! Take filePath : {}", filePath);
										if (!filePathList.contains(filePath)) {
											filePathList.add(filePath);
											ObjectNode filePathNode = mapper.createObjectNode();
											filePathNode.put("checkId", docCheckId);
											filePathNode.put("documentName", documentName);
											filePathNode.put("filePath", filePath);
											filePathNode.put("akaName", akaName);
											filePathMap.add(filePathNode);
										}
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

	@Override
	public List<String> getMrlDocumentNames(ObjectMapper mapper, String componentName, String akaName) {
		ArrayNode documentsName = mapper.createArrayNode();
		if (StringUtils.isNotEmpty(akaName) && StringUtils.isNotEmpty(componentName)) {
			ObjectNode mrlRuleDescriptionDocNode = mapper.createObjectNode();
			mrlRuleDescriptionDocNode.put("componentName", componentName);
			mrlRuleDescriptionDocNode.put("akaName", akaName);
			
			try {
				String mrlRuleDescriptionDocStr = mapper.writeValueAsString(mrlRuleDescriptionDocNode);

				String mrlRuleDescriptionResponse = onlineApiService.sendDataToPost(mrlRuleDescriptionUrl,
						mrlRuleDescriptionDocStr);

				JsonNode mrlRuleDescriptionResponseNode = mrlRuleDescriptionResponse != null
						? mapper.readValue(mrlRuleDescriptionResponse, JsonNode.class)
						: mapper.createObjectNode();

				documentsName = mrlRuleDescriptionResponseNode.has("ducumentNames")
						? (ArrayNode) mrlRuleDescriptionResponseNode.get("ducumentNames")
						: mapper.createArrayNode();
				logger.info("Value of documentsName : {}", documentsName);

			} catch (JsonProcessingException e) {
				logger.error("Exception while mapping mrl document response : {}", e.getMessage());
				e.printStackTrace();
			}
		}
		if (documentsName != null && !documentsName.isEmpty()) {
			return mapper.convertValue(documentsName, new TypeReference<List<String>>() {});
		}
		return new ArrayList<>();
	}
}
