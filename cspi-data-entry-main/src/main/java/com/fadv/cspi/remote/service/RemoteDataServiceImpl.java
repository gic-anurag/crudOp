package com.fadv.cspi.remote.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fadv.cspi.entities.CaseClientDetails;
import com.fadv.cspi.entities.ContactCardMaster;
import com.fadv.cspi.entities.PackCompProd;
import com.fadv.cspi.exception.ServiceException;
import com.fadv.cspi.remote.pojo.MrlRulePOJO;
import com.fadv.cspi.remote.pojo.RuleDescriptionPOJO;
import com.fadv.cspi.remote.pojo.SlaSearchPOJO;
import com.fadv.cspi.service.ApiService;
import com.fadv.cspi.service.CaseAssociatedDocumentsService;
import com.fadv.cspi.service.CaseClientDetailsService;
import com.fadv.cspi.service.ContactCardMasterService;
import com.fadv.cspi.service.PackCompProdService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class RemoteDataServiceImpl implements RemoteDataService {

	private static final String ERROR_CODE_404 = "ERROR_CODE_404";

	private static final String RECORD_NOT_FOUND = "Record not found";

	private static final Logger logger = LoggerFactory.getLogger(RemoteDataServiceImpl.class);

	ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Value("${mrl.rule.document.url}")
	private String mrlRuleDescriptionUrl;

	@Value("${scoping.rule.document.url}")
	private String scopingRuleDescriptionUrl;

	@Autowired
	private CaseAssociatedDocumentsService caseAssociatedDocumentsService;

	@Autowired
	private ContactCardMasterService contactCardMasterService;

	@Autowired
	private ApiService apiService;

	@Autowired
	private CaseClientDetailsService caseClientDetailsService;

	@Autowired
	private PackCompProdService packCompProdService;

	@Override
	public List<String> getMrlDocs(JsonNode mrlNode) throws ServiceException {

		ArrayNode documentsName = mapper.createArrayNode();

		try {
			String mrlRuleDescriptionDocStr = mapper.writeValueAsString(mrlNode);

			String mrlRuleDescriptionResponse = apiService.sendDataToPost(mrlRuleDescriptionUrl,
					mrlRuleDescriptionDocStr);

			JsonNode mrlRuleDescriptionResponseNode = mrlRuleDescriptionResponse != null
					? mapper.readValue(mrlRuleDescriptionResponse, JsonNode.class)
					: mapper.createObjectNode();
			logger.info("Value of mrlRuleDescriptionResponseNode : {}", mrlRuleDescriptionResponseNode);

			documentsName = mrlRuleDescriptionResponseNode.has("ducumentNames")
					? (ArrayNode) mrlRuleDescriptionResponseNode.get("ducumentNames")
					: mapper.createArrayNode();
			logger.info("Value of documentsName : {}", documentsName);

		} catch (JsonProcessingException e) {
			logger.error("Exception while mapping mrl document response : {}", e.getMessage());
		}
		if (documentsName != null && !documentsName.isEmpty()) {
			return mapper.convertValue(documentsName, new TypeReference<List<String>>() {
			});
		}

		throw new ServiceException(RECORD_NOT_FOUND, ERROR_CODE_404);
	}

	// ========================================================================================
	// ============================= Mrl Docs Rules -
	// Start====================================
	// ========================================================================================
	@Override
	public List<ObjectNode> getMrlDocRules(JsonNode mrlNode) throws ServiceException {

		List<ObjectNode> ruleParam = new ArrayList<>();

		try {
			String mrlRuleDescriptionDocStr = mapper.writeValueAsString(mrlNode);

			String mrlRuleDescriptionResponse = apiService.sendDataToPost(mrlRuleDescriptionUrl,
					mrlRuleDescriptionDocStr);

			JsonNode mrlRuleDescriptionResponseNode = mrlRuleDescriptionResponse != null
					? mapper.readValue(mrlRuleDescriptionResponse, JsonNode.class)
					: mapper.createObjectNode();
			logger.info("Value of mrlRuleDescriptionResponseNode : {}", mrlRuleDescriptionResponseNode);

			String rulesParamStr = mrlRuleDescriptionResponseNode.has("rulesParamJson")
					? mrlRuleDescriptionResponseNode.get("rulesParamJson").asText()
					: "{}";
			if (rulesParamStr != null && StringUtils.isNotEmpty(rulesParamStr)) {
				JsonNode rulesParamJson = mapper.readTree(rulesParamStr);
				ArrayNode ruleParamNewNode = rulesParamJson.has("RuleParam")
						? (ArrayNode) rulesParamJson.get("RuleParam")
						: mapper.createArrayNode();

				List<Map<String, Object>> ruleParamMap = mapper.convertValue(ruleParamNewNode,
						new TypeReference<List<Map<String, Object>>>() {
						});

				ruleParamMap = ruleParamMap.parallelStream().map(data -> {
					Map<String, Object> newData = new HashMap<>();
					for (Map.Entry<String, Object> ruleMap : data.entrySet()) {
						String newKey = ruleMap.getKey().replaceAll(" \\s*", "");
						newData.put(newKey, ruleMap.getValue());
					}

					return newData;
				}).collect(Collectors.toList());

				ruleParam = mapper.convertValue(ruleParamMap, new TypeReference<List<ObjectNode>>() {
				});
			}
			logger.info("Value of rulesParamJson : {}", ruleParam);

		} catch (JsonProcessingException e) {
			logger.error("Exception while mapping mrl document response : {}", e.getMessage());
		}
		if (ruleParam != null && !ruleParam.isEmpty()) {
			return ruleParam;
		}

		throw new ServiceException(RECORD_NOT_FOUND, ERROR_CODE_404);
	}

	// ========================================================================================
	// =============================Mrl Docs Rule -
	// End========================================
	// ========================================================================================
	@Override
	public List<MrlRulePOJO> getMrlRuleList(Long caseId) {

		List<String> akaNameList = new ArrayList<>();
		try {
			akaNameList = caseAssociatedDocumentsService.getAkaNamesByCaseId(caseId);
		} catch (ServiceException e1) {
			akaNameList = new ArrayList<>();
			logger.error("Exception occurred while fetching akaName list : {}", e1.getMessage());
		}

		List<MrlRulePOJO> mrlRulePOJOs = new ArrayList<>();

		for (String akaName : akaNameList) {
			ContactCardMaster contactCardMaster = contactCardMasterService.getContactCardByAkaName(akaName);
			if (contactCardMaster != null) {
				String componentName = contactCardMaster.getComponentMaster() != null
						? contactCardMaster.getComponentMaster().getComponentName()
						: "";

				MrlRulePOJO mrlRulePOJO = new MrlRulePOJO();
				mrlRulePOJO.setAkaName(akaName);
				mrlRulePOJO.setComponentName(componentName);

				ObjectNode mrlSearchNode = mapper.createObjectNode();
				mrlSearchNode.put("akaName", akaName);
				mrlSearchNode.put("componentName", componentName);

				List<String> mrlDocList = new ArrayList<>();
				try {
					mrlDocList = getMrlDocs(mrlSearchNode);
				} catch (ServiceException e) {
					logger.error("Mrl Docs Not found for search details : {}", mrlSearchNode);
					mrlDocList = new ArrayList<>();
				}
				mrlRulePOJO.setDocumentNames(mrlDocList);
				mrlRulePOJOs.add(mrlRulePOJO);
			}
		}
		return mrlRulePOJOs;
	}

	@Override
	public List<RuleDescriptionPOJO> getMrlRuleDescription(Long caseId) throws ServiceException {
		List<SlaSearchPOJO> slaSearchPOJOs = createSlaSearchRequestBody(caseId);
		List<RuleDescriptionPOJO> ruleDescriptionPOJOs = new ArrayList<>();

		for (SlaSearchPOJO slaSearchPOJO : slaSearchPOJOs) {
			JsonNode slaSearchNode = mapper.convertValue(slaSearchPOJO, JsonNode.class);
			String ruleDescriptionResponse = apiService.sendDataToPost(mrlRuleDescriptionUrl, slaSearchNode.toString());

			if (ruleDescriptionResponse != null) {
				try {
					RuleDescriptionPOJO ruleDescriptionPOJO = mapper.readValue(ruleDescriptionResponse,
							RuleDescriptionPOJO.class);
					ruleDescriptionPOJOs.add(ruleDescriptionPOJO);
				} catch (JsonProcessingException e) {
					logger.error("Exception occurred while mapping rule response to object : {}", e.getMessage());
				}
			}
		}
		return ruleDescriptionPOJOs;
	}

	@Override
	public List<RuleDescriptionPOJO> getSlaRuleDescription(Long caseId) throws ServiceException {
		List<SlaSearchPOJO> slaSearchPOJOs = createSlaSearchRequestBody(caseId);
		ArrayNode slaSearchNode = mapper.convertValue(slaSearchPOJOs, ArrayNode.class);
		List<RuleDescriptionPOJO> ruleDescriptionPOJOs = new ArrayList<>();

		String ruleDescriptionResponse = apiService.sendDataToPost(scopingRuleDescriptionUrl, slaSearchNode.toString());

		if (ruleDescriptionResponse != null) {
			try {
				JsonNode ruleResponseNode = mapper.readTree(ruleDescriptionResponse);
				ArrayNode dataNode = ruleResponseNode.has("data") ? (ArrayNode) ruleResponseNode.get("data")
						: mapper.createArrayNode();
				ruleDescriptionPOJOs = mapper.convertValue(dataNode, new TypeReference<List<RuleDescriptionPOJO>>() {
				});
				ruleDescriptionPOJOs = ruleDescriptionPOJOs.parallelStream().filter(
						data -> !StringUtils.containsIgnoreCase(data.getDescription(), "Error: incorrect request"))
						.collect(Collectors.toList());
			} catch (JsonProcessingException e) {
				logger.error("Exception occurred while mapping rule response to object : {}", e.getMessage());
			}
		}

		return ruleDescriptionPOJOs;
	}

	private List<SlaSearchPOJO> createSlaSearchRequestBody(Long caseId) throws ServiceException {

		CaseClientDetails caseClientDetails = caseClientDetailsService.findByCaseDetailsId(caseId);
		String clientName = caseClientDetails.getClientMaster() != null
				? caseClientDetails.getClientMaster().getClientName()
				: "";
		String sbuName = caseClientDetails.getSbuMaster() != null ? caseClientDetails.getSbuMaster().getSbuName() : "";
		String packageName = caseClientDetails.getPackageMaster() != null
				? caseClientDetails.getPackageMaster().getPackageName()
				: "";
		Long packageId = caseClientDetails.getPackageMaster() != null
				? caseClientDetails.getPackageMaster().getPackageMasterId()
				: 0;

		return setSlaSearchByProduct(packageId, clientName, sbuName, packageName);

	}

	private List<SlaSearchPOJO> setSlaSearchByProduct(Long packageId, String clientName, String sbuName,
			String packageName) throws ServiceException {

		List<SlaSearchPOJO> slaSearchPOJOs = new ArrayList<>();

		if (packageId > 0 && StringUtils.isNotEmpty(clientName) && StringUtils.isNotEmpty(sbuName)
				&& StringUtils.isNotEmpty(packageName)) {

			List<PackCompProd> packCompProds = packCompProdService.findByPackageMasterId(packageId);

			for (PackCompProd packCompProd : packCompProds) {
				String componentName = packCompProd.getComponentMaster() != null
						? packCompProd.getComponentMaster().getComponentName()
						: "";
				String productName = packCompProd.getProductMaster() != null
						? packCompProd.getProductMaster().getProductName()
						: "";

				if (StringUtils.isNotEmpty(componentName) && StringUtils.isNotEmpty(productName)) {
					SlaSearchPOJO slaSearchPOJO = new SlaSearchPOJO();
					slaSearchPOJO.setClientCode(clientName);
					slaSearchPOJO.setComponentName(componentName);
					slaSearchPOJO.setPackageCode(packageName);
					slaSearchPOJO.setSbu(sbuName);
					slaSearchPOJO.setSubComponentName(productName);
					slaSearchPOJOs.add(slaSearchPOJO);
				}
			}
		}
		return slaSearchPOJOs;
	}
}
