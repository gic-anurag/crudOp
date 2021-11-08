package com.gic.fadv.vendor.input.service;

import java.util.ArrayList;
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
import com.gic.fadv.vendor.input.pojo.CaseSpecificInfoPOJO;
import com.gic.fadv.vendor.input.pojo.CaseSpecificRecordDetailPOJO;
import com.gic.fadv.vendor.input.pojo.DigiAddressClientListPOJO;
import com.gic.fadv.vendor.input.pojo.EducationAVLPOJO;
import com.gic.fadv.vendor.service.ApiService;

@Service
public class VendorInputServiceImpl implements VendorInputService {

	private static final String COUNTRY = "Country";

	private static final String STATUS = "Status";

	@Autowired
	private ApiService apiService;

	@Value("${local.file.location}")
	private String localFileLocation;

	@Value("${sftp.input.file.path}")
	private String sftpInputFilePath;

	@Value("${digiAddressClientList.rest.url}")
	private String digiAddressClientListRestUrl;

	@Value("${educationAVL.rest.url}")
	private String educatioAVLRestUrl;

	@Value("${associate.filepaths.rest.url}")
	private String associateFilePathUrl;

	@Value("${doc.url}")
	private String docUrl;

	@Value("${education.mrl.file.location}")
	private String educationFileLocation;

	@Value("${education.pdf.file.location}")
	private String educationPdfFileLocation;

	@Value("${mrl.rule.description.url}")
	private String mrlRuleDescriptionUrl;

	private static final Logger logger = LoggerFactory.getLogger(VendorInputServiceImpl.class);
	private static final String ENGINE = "engine";
	private static final String SUCCESS = "success";
	private static final String MESSAGE = "message";
	private static final String RESULT = "result";
	private static final String VENDOR_RESULT = "vendorInput";
	private static final String SUCCEEDED = "SUCCEEDED";
	private static final String FAILED = "FAILED";
	private static final String ADDRESS = "Address";
	private static final String EDUCATION = "Education";
	private String failedMessage = "";
	// =============================================================================

	@Override
	public ObjectNode processRequestBody(JsonNode requestNode) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		boolean isVendor = false;
		if (!requestNode.isEmpty()) {
			JsonNode caseSpecificInfoNode = requestNode.has("caseSpecificInfo") ? requestNode.get("caseSpecificInfo")
					: mapper.createObjectNode();
			JsonNode caseSpecificRecordDetailNode = requestNode.has("caseSpecificRecordDetail")
					? requestNode.get("caseSpecificRecordDetail")
					: mapper.createObjectNode();

			JsonNode otherDetails = requestNode.has("otherDetails") ? requestNode.get("otherDetails")
					: mapper.createObjectNode();

			CaseSpecificInfoPOJO caseSpecificInfoPOJO = mapper.convertValue(caseSpecificInfoNode,
					CaseSpecificInfoPOJO.class);
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO = mapper
					.convertValue(caseSpecificRecordDetailNode, CaseSpecificRecordDetailPOJO.class);

			isVendor = processInitiate(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO, otherDetails);
		}
		return generateResponseStr(mapper, isVendor);
	}

	private boolean processInitiate(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, JsonNode otherDetails) {

		boolean isVendor = false;
		try {
			isVendor = processRecord(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO, otherDetails);

		} catch (JsonProcessingException e) {
			failedMessage = "Exception while mapping record node : " + e.getMessage();
			logger.error(failedMessage);
		}
		return isVendor;
	}

	private boolean processRecord(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, JsonNode otherDetails)
			throws JsonProcessingException {

		String componentName = caseSpecificRecordDetailPOJO.getComponentName() != null
				? caseSpecificRecordDetailPOJO.getComponentName()
				: "";
		String productName = caseSpecificRecordDetailPOJO.getProduct() != null
				? caseSpecificRecordDetailPOJO.getProduct()
				: "";
		String recordStr = caseSpecificRecordDetailPOJO.getComponentRecordField() != null
				? caseSpecificRecordDetailPOJO.getComponentRecordField()
				: "{}";
		JsonNode recordNode = mapper.readValue(recordStr, JsonNode.class);
		boolean isVendor = false;
		if (StringUtils.equalsIgnoreCase(componentName, ADDRESS)) {
			isVendor = getAddressRecordMap(mapper, caseSpecificInfoPOJO, componentName, productName,
					caseSpecificRecordDetailPOJO, recordNode);
		} else if (StringUtils.equalsIgnoreCase(componentName, EDUCATION)) {
			isVendor = getEducationRecordMap(mapper, componentName, productName, caseSpecificRecordDetailPOJO,
					recordNode);
		} else {
			failedMessage = "Component Name is neither Address nor Education";
			logger.error(failedMessage);
		}
		return isVendor;
	}

//	private boolean getAddressRecordMap(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
//			String componentName, String productName, CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO,
//			JsonNode recordNode) throws JsonProcessingException {
//
//		if (componentName.equalsIgnoreCase(ADDRESS)) {
//			logger.info("Check for Address Component");
//			if (productName.equalsIgnoreCase("Current Address - Physical Verification")
//					|| productName.equalsIgnoreCase("Permanent address - Physical Verification")
//					|| productName.equalsIgnoreCase("Previous address - Physical Verification")) {
//
//				String digitalVerificationIsApproved = checkDigiAddressClientList(mapper, caseSpecificInfoPOJO);
//				if (digitalVerificationIsApproved != null && digitalVerificationIsApproved.equalsIgnoreCase("No")) {
//					logger.info("Call processAddressComponents");
//					return processAddressComponent(mapper, caseSpecificRecordDetailPOJO, recordNode);
//				} else {
//					logger.info("Digital Verification is Approved not No");
//					return false;
//				}
//			} else {
//				logger.info("Sub Component is other than required");
//				return false;
//			}
//		} else {
//			return false;
//		}
//	}

	private boolean getAddressRecordMap(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			String componentName, String productName, CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO,
			JsonNode recordNode) throws JsonProcessingException {

		if (componentName.equalsIgnoreCase(ADDRESS)) {
			logger.info("Check for Address Component");
			String digitalVerificationIsApproved = checkDigiAddressClientList(mapper, caseSpecificInfoPOJO);

			if (digitalVerificationIsApproved == null) {
				failedMessage = "Digi-Address Client approved is neither Yes nor No";
				return false;
			}

			if ((digitalVerificationIsApproved.equalsIgnoreCase("No")
					&& (productName.equalsIgnoreCase("Current Address - Physical Verification")
							|| productName.equalsIgnoreCase("Permanent address - Physical Verification")
							|| productName.equalsIgnoreCase("Previous address - Physical Verification")))
					|| (digitalVerificationIsApproved.equalsIgnoreCase("Yes")
							&& (productName.equalsIgnoreCase("Previous address - Physical Verification")))) {
				logger.info("Call processAddressComponents");
				return processAddressComponent(mapper, caseSpecificRecordDetailPOJO, recordNode);
			} else {
				failedMessage = "Rule not statisfied. Send for manual verification";
				return false;
			}
		} else {
			failedMessage = "Component is not Address";
			return false;
		}
	}

	private boolean getEducationRecordMap(ObjectMapper mapper, String componentName, String productName,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, JsonNode recordNode) {
		if (componentName.equalsIgnoreCase(EDUCATION)
				&& productName.equalsIgnoreCase("Education (Highest Qualification)")) {
			logger.info("Check for Education Component and Product Name Education (Highest Qualification)");
			return processEducationComponent(mapper, caseSpecificRecordDetailPOJO, recordNode);
		} else {
			failedMessage = "Education component name or product name do not match.";
			return false;
		}
	}

	private ObjectNode generateResponseStr(ObjectMapper mapper, boolean isVendor) {
		ObjectNode responseNode = mapper.createObjectNode();
		responseNode.put(ENGINE, VENDOR_RESULT);
		if (isVendor) {
			responseNode.put(SUCCESS, isVendor);
			responseNode.put(MESSAGE, SUCCEEDED);
			responseNode.put(STATUS, 200);
			responseNode.put(RESULT, "Vendor Found");
		} else if (StringUtils.isNotEmpty(failedMessage)) {
			responseNode.put(SUCCESS, isVendor);
			responseNode.put(MESSAGE, FAILED);
			responseNode.put(STATUS, 200);
			responseNode.put(RESULT, failedMessage);
		} else {
			responseNode.put(SUCCESS, isVendor);
			responseNode.put(MESSAGE, FAILED);
			responseNode.put(STATUS, 200);
			responseNode.put(RESULT, "Vendor Not Found");
		}
		return responseNode;
	}

	private String checkDigiAddressClientList(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO)
			throws JsonProcessingException {
		String clientName = caseSpecificInfoPOJO.getClientName() != null ? caseSpecificInfoPOJO.getClientName() : "";
		String clientCode = caseSpecificInfoPOJO.getClientCode() != null ? caseSpecificInfoPOJO.getClientCode() : "";
		String sbuName = caseSpecificInfoPOJO.getSbuName() != null ? caseSpecificInfoPOJO.getSbuName() : "";
		String packageName = caseSpecificInfoPOJO.getPackageName() != null ? caseSpecificInfoPOJO.getPackageName() : "";

		ObjectNode requestNode = mapper.createObjectNode();
		requestNode.put("clientCode", clientCode);
		requestNode.put("clientName", clientName);
		requestNode.put("sbuName", sbuName);
		requestNode.put("packageName", packageName);

		String digiAddressClientListSearchString = mapper.writeValueAsString(requestNode);
		logger.info("Value of Digi-Address Client List Search String : {}", digiAddressClientListSearchString);

		String digiAddressClientListResponse = apiService.sendDataToPost(digiAddressClientListRestUrl,
				digiAddressClientListSearchString);
		logger.info("Value of Digi-Address Client List Response : {} ", digiAddressClientListResponse);

		List<DigiAddressClientListPOJO> digiAddressClientList = new ArrayList<>();
		if (digiAddressClientListResponse != null && !StringUtils.isEmpty(digiAddressClientListResponse)) {
			try {
				digiAddressClientList = mapper.readValue(digiAddressClientListResponse,
						new TypeReference<List<DigiAddressClientListPOJO>>() {
						});
			} catch (JsonProcessingException e) {
				failedMessage = "Error while mapping digi address client response : " + e.getMessage();
				logger.info(failedMessage);
			}
		}
		if (CollectionUtils.isNotEmpty(digiAddressClientList)) {
			return digiAddressClientList.get(0).getDigitalVerificationIsApproved();
		} else {
			failedMessage = "Digi address client list is empty.";
		}
		return null;
	}

	private boolean processAddressComponent(ObjectMapper mapper,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, JsonNode recordNode) {

		if (!recordNode.isEmpty()) {
			String checkId = caseSpecificRecordDetailPOJO.getInstructionCheckId() != null
					? caseSpecificRecordDetailPOJO.getInstructionCheckId()
					: "";
			String country = recordNode.has(COUNTRY) ? recordNode.get(COUNTRY).asText() : "";
			String miTagged = recordNode.has("MI") ? recordNode.get("MI").asText() : "";

			if (country.equalsIgnoreCase("India") && miTagged.equalsIgnoreCase("No") && !StringUtils.isEmpty(checkId)) {
				mapper.convertValue(recordNode, new TypeReference<Map<String, Object>>() {
				});
				logger.info("Generate Input File in the name of \"Address_Input_Mapping\"");
				return true;
			} else {
				logger.info("Don't Generate Address Input file. Check is tagged for MI");
				failedMessage = "Address : Value for MI is : " + miTagged + " and value for country is : " + country;
				return false;
			}
		}
		failedMessage = "Address : Check record is Empty";
		return false;
	}

	private boolean processEducationComponent(ObjectMapper mapper,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, JsonNode recordNode) {

		if (!recordNode.isEmpty()) {
			String checkId = caseSpecificRecordDetailPOJO.getInstructionCheckId() != null
					? caseSpecificRecordDetailPOJO.getInstructionCheckId()
					: "";
			String country = recordNode.has("College(Country)") ? recordNode.get("College(Country)").asText() : "";
			String miTagged = recordNode.has("MI") ? recordNode.get("MI").asText() : "";
			String universityAkaName = recordNode.has("University Aka Name")
					? recordNode.get("University Aka Name").asText()
					: "";

			return processEducationComponentChild(mapper, recordNode, checkId, country, miTagged, universityAkaName);
		}
		failedMessage = "Education : Check record is Empty";
		return false;
	}

	private boolean processEducationComponentChild(ObjectMapper mapper, JsonNode recordNode, String checkId,
			String country, String miTagged, String universityAkaName) {
		if (recordNode.has("University(Country)") && (country == null || StringUtils.isEmpty(country)))
			country = recordNode.get("University(Country)").asText();

		if (recordNode.has("College/Centre Name Aka Name")
				&& (universityAkaName == null || StringUtils.isEmpty(universityAkaName)))
			universityAkaName = recordNode.get("College/Centre Name Aka Name").asText();

		if (country.equalsIgnoreCase("India") && miTagged.equalsIgnoreCase("No") && !StringUtils.isEmpty(checkId)) {
			return getEducationAvlResult(mapper, recordNode, universityAkaName);
		} else {
			logger.info("Don't Generate Education Input file. Check is tagged for MI");
			failedMessage = "Education : Value for MI is : " + miTagged + " and value for country is : " + country;
			return false;
		}
	}

	private boolean getEducationAvlResult(ObjectMapper mapper, JsonNode recordNode, String universityAkaName) {

		String educationAVLSearchString = "{\"universityAKAname\":\"" + universityAkaName + "\"}";
		logger.info("Value of Education AVL Search String {}", educationAVLSearchString);
		String educationAVLResponse = apiService.sendDataToPost(educatioAVLRestUrl, educationAVLSearchString);

		logger.info("Value of Education AVL Response : {}", educationAVLResponse);

		List<EducationAVLPOJO> educationAvlList = new ArrayList<>();
		if (educationAVLResponse != null && !StringUtils.isEmpty(educationAVLResponse)) {
			try {
				educationAvlList = mapper.readValue(educationAVLResponse, new TypeReference<List<EducationAVLPOJO>>() {
				});
			} catch (JsonProcessingException e) {
				logger.info("Error while mapping education avl response : {}", e.getMessage());
				failedMessage = "Error while mapping education avl response : " + e.getMessage();
			}
		}
		if (CollectionUtils.isNotEmpty(educationAvlList)) {
			logger.info("Generate Input File in the name of \"Education_Input_Mapping\""
					+ "Generate the pdf files in the name of check ID (pdf file for education documents which is available).");
			return true;
		} else {
			failedMessage = "Education Avl list is empty";
			return false;
		}
	}

}
