package com.gic.fadv.spoc.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.mail.internet.MimeMessage;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.spoc.pojo.SPOCListPOJO;
import com.gic.fadv.spoc.pojo.SpocConfigPOJO;
import com.gic.fadv.spoc.pojo.SpocEmailAddressMappingPOJO;
import com.gic.fadv.spoc.pojo.SPOCEmailConfigPOJO;
import com.gic.fadv.spoc.pojo.SPOCEmailTemplateMappingPOJO;
import com.gic.fadv.spoc.pojo.SPOCEmailTemplatePOJO;
import com.gic.fadv.spoc.pojo.SPOCExcelTemplatePOJO;
import com.gic.fadv.spoc.pojo.CaseReferencePOJO;
import com.gic.fadv.spoc.pojo.CaseSpecificInfoPOJO;
import com.gic.fadv.spoc.pojo.CaseSpecificRecordDetailPOJO;
import com.gic.fadv.spoc.pojo.CheckVerificationPOJO;
import com.gic.fadv.spoc.pojo.FileUploadPOJO;
import com.gic.fadv.spoc.pojo.PackageQuestionnaireExtractPOJO;
import com.gic.fadv.spoc.pojo.QuestionPOJO;
import com.gic.fadv.spoc.pojo.QuestionnairePOJO;
import com.gic.fadv.spoc.pojo.SPOCBulkPOJO;
import com.gic.fadv.spoc.pojo.TaskSpecsPOJO;
import com.gic.fadv.spoc.pojo.TemplateHeaders;
import com.gic.fadv.spoc.utility.JExcelUtility;
import com.gic.fadv.spoc.utility.Utility;
import com.gic.fadv.spoc.pojo.GlobalQuestionAndFormLabelPOJO;

import jxl.write.WriteException;

@Service
public class SPOCServiceImpl implements SPOCService {

	private static final String SPOC_FOUND_FOR_BULK = "SPOC FOUND FOR BULK";
	private static final String SPOC_EMAIL_RESPONSE = "spocEmailResponse";
	private static final String EMAIL_SENT = "Email Sent";
	private static final String RESPONSE = "response";
	private static final String PLEASE_SPECIFY = "(Please specify)";
	@Autowired
	private ApiService apiService;
	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	SPOCApiService spocApiService;

	private static final Logger logger = LoggerFactory.getLogger(SPOCServiceImpl.class);

	// SPOC Service Specific URL
	@Value("${spoclist.rest.url}")
	private String spocListRestUrl;
	@Value("${spocemail.rest.url}")
	private String spocEmailRestUrl;
	@Value("${spocemailtemplate.rest.url}")
	private String spocEmailTemplateRestUrl;
	@Value("${spoctemplatesearch.rest.url}")
	private String spocTemplateSearchRestUrl;
	@Value("${spocexceltemplate.rest.url}")
	private String spocExcelTemplateRestUrl;

	@Value("${holiday.list.url}")
	private String holidayListUrl;

	@Value("${associate.filepaths.rest.url}")
	private String associateFilePathUrl;

	@Value("${spoc.question.package.url}")
	private String questionPackageUrl;

	// Questionnaire and verification specific URL
	@Value("${questionaire.list.l3.url}")
	private String questionaireURL;
	@Value("${verification.status.url.l3}")
	private String verificationStatusL3Url;
	// MRL Related URL
	@Value("${associate.docs.case.url}")
	private String associateDocsUrl;
	// From Email Address
	@Value("${spring.mail.username}")
	private String fromUserName;
	// Doc URL
	@Value("${doc.url}")
	private String docUrl;
	// Local File Download Location
	@Value("${local.file.download.location}")
	private String localFileLocation;

	@Value("${spoc.mail.send.to}")
	private List<String> mailsentTo;

	@Value("${spoc.mail.send.to.cc}")
	private List<String> ccMailsentTo;

	@Value("${local.file.zip.location}")
	private String localFileZiplocation;
	@Value("${spoc.bulk.url}")
	private String spocBulkUrl;

	@Value("${check.specific.record}")
	private String checkSpecificRecordDetailsURL;

	@Value("${spoc.additional.config.url}")
	private String spocAdditionalConfigURL;

	private static final String ENGINE = "engine";
	private static final String SUCCESS = "success";
	private static final String MESSAGE = "message";
	private static final String STATUS = "status";
	private static final String RESULT = "result";
	private static final String SPOC_RESULT = "spocResult";
	private static final String SUCCEEDED = "SUCCEEDED";
	private static final String FAILED = "FAILED";
	private static final String SPOC_FOUND = "SPOC records found";
	private static final String SPOC_NOT_FOUND = "SPOC records not found";
	private static final String L3_ERROR_RESPONSE = "{\"Error\": \"Error as reponse from L3\"}";
	private static final String L3_STATUS = "l3Status";
	private static final String ERROR = "error";
	private static final String L3_RESPONSE = "l3Response";
	private static final String RETURN_MESSAGE = "returnMessage";
	private static final String PERSONAL_DETAILS_BVF = "personaldetailsasperbvf";

	@Override
	public ObjectNode processRequestBody(JsonNode requestNode) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		Map<String, String> responseMap = new HashMap<>();

		if (!requestNode.isEmpty()) {
			JsonNode caseSpecificInfoNode = requestNode.has("caseSpecificInfo") ? requestNode.get("caseSpecificInfo")
					: mapper.createObjectNode();
			JsonNode caseSpecificRecordDetailNode = requestNode.has("caseSpecificRecordDetail")
					? requestNode.get("caseSpecificRecordDetail")
					: mapper.createObjectNode();

			CaseSpecificInfoPOJO caseSpecificInfoPOJO = mapper.convertValue(caseSpecificInfoNode,
					CaseSpecificInfoPOJO.class);
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO = mapper
					.convertValue(caseSpecificRecordDetailNode, CaseSpecificRecordDetailPOJO.class);

			responseMap = processRecords(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO);
		}
		return generateResponseStr(mapper, responseMap);
	}

	private ObjectNode generateResponseStr(ObjectMapper mapper, Map<String, String> responseMap) {
		ObjectNode responseNode = mapper.createObjectNode();
		responseNode.put(ENGINE, SPOC_RESULT);
		if (responseMap != null && StringUtils.equalsIgnoreCase(responseMap.get(EMAIL_SENT), SUCCESS)) {
			responseNode.put(SUCCESS, true);
			responseNode.put(MESSAGE, SUCCEEDED);
			responseNode.put(STATUS, 200);
			if (StringUtils.equalsIgnoreCase(responseMap.get(L3_RESPONSE), L3_ERROR_RESPONSE)) {
				responseNode.put(L3_STATUS, ERROR);
			} else {
				responseNode.put(L3_STATUS, SUCCESS);
			}
			if (responseMap.get(SPOC_EMAIL_RESPONSE) != null) {
				responseNode.put(SPOC_EMAIL_RESPONSE, responseMap.get(SPOC_EMAIL_RESPONSE));
			}
			responseNode.put(L3_RESPONSE, responseMap.get(L3_RESPONSE));
			responseNode.put(RESULT, responseMap.get(RETURN_MESSAGE));
		} else if (responseMap != null
				&& StringUtils.equalsIgnoreCase(responseMap.get(RETURN_MESSAGE), SPOC_FOUND_FOR_BULK)) {
			responseNode.put(SUCCESS, true);
			responseNode.put(MESSAGE, SUCCEEDED);
			responseNode.put(STATUS, 200);
			responseNode.put(RESULT, SPOC_FOUND_FOR_BULK);
		} else if (responseMap != null && (responseMap.get(RETURN_MESSAGE) != null
				|| StringUtils.isNotEmpty(responseMap.get(RETURN_MESSAGE)))) {
			responseNode.put(SUCCESS, false);
			responseNode.put(MESSAGE, FAILED);
			responseNode.put(STATUS, 200);
			responseNode.put(RESULT, responseMap.get(RETURN_MESSAGE));

		} else {
			responseNode.put(SUCCESS, false);
			responseNode.put(MESSAGE, FAILED);
			responseNode.put(STATUS, 200);
			responseNode.put(RESULT, "Unable to process engine");
		}
		return responseNode;
	}

	private Map<String, String> processRecords(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO) throws JsonProcessingException {

		String recordStr = caseSpecificRecordDetailPOJO.getComponentRecordField();
		JsonNode recordNode = mapper.readTree(recordStr);

		String failedMessage = "";
		if (recordNode != null) {
			String miTagged = recordNode.has("MI") ? recordNode.get("MI").asText() : "";
			logger.info("Value of MI : {}", miTagged);
			if (StringUtils.equalsIgnoreCase("No", miTagged) || StringUtils.isEmpty(miTagged)) {
				String thirdAkaName = getThirdAkaName(mapper, caseSpecificRecordDetailPOJO);
				ObjectNode spocSearchNode = mapper.createObjectNode();
				spocSearchNode.put("companyAkaName", thirdAkaName);
				logger.info("Value of SPOC Search String : {}", spocSearchNode);
				String spocResponse = null;
				List<SPOCListPOJO> spocList = getSpocResponseList(mapper, spocSearchNode, spocResponse);

				if (CollectionUtils.isNotEmpty(spocList)) {
					SPOCListPOJO spocPojo = spocList.get(0);
					return processSpocEmail(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO, spocPojo,
							thirdAkaName);
				} else {
					failedMessage = "SPOC list is Empty";
				}
			} else {
				failedMessage = "MI is not No or Blank/null";
			}
		} else {
			failedMessage = "Check Record is Empty";
		}
		if (StringUtils.isNotEmpty(failedMessage)) {
			logger.info("failedMessage : {}", failedMessage);
			return createFailedResponse(failedMessage);
		}
		return null;
	}

	private List<SPOCListPOJO> getSpocResponseList(ObjectMapper mapper, ObjectNode spocSearchNode,
			String spocResponse) {
		try {
			spocResponse = apiService.sendDataToPost(spocListRestUrl, spocSearchNode.toString());
			logger.info("Value of SPOC Response : {}", spocResponse);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception while spoc search api call : {}", e.getMessage());
		}

		List<SPOCListPOJO> spocList = new ArrayList<>();
		if (spocResponse != null && !StringUtils.isEmpty(spocResponse)) {
			try {
				spocList = mapper.readValue(spocResponse, new TypeReference<List<SPOCListPOJO>>() {
				});
			} catch (JsonProcessingException e) {
				logger.info("Error while mapping SPOC response : {}", e.getMessage());
			}
		}
		return spocList;
	}

	private String getSubjectLine(String subjectLine, String firstName, String lastName, String checkId,
			String clientName, JsonNode checkRecord, boolean isHtml) {
		String startStr = "<\\s*<\\s*";
		String endStr = "\\s*>\\s*>";
		if (isHtml) {
			startStr = "&lt;(?:&nbsp;|\\s)*&lt;(?:&nbsp;|\\s)*";
			endStr = "(?:&nbsp;|\\s)*&gt;(?:&nbsp;|\\s)*&gt;";
		}

		Map<String, String> keyList = Utility.findStringMapBetweenChars(subjectLine, startStr, endStr, isHtml);

		for (Map.Entry<String, String> keyEntry : keyList.entrySet()) {
			if (StringUtils.equalsIgnoreCase(keyEntry.getKey(), "First Name")) {
				subjectLine = StringUtils.replace(subjectLine, keyEntry.getValue(), firstName);
			} else if (StringUtils.equalsIgnoreCase(keyEntry.getKey(), "Last Name")) {
				subjectLine = StringUtils.replace(subjectLine, keyEntry.getValue(), lastName);
			} else if (StringUtils.equalsIgnoreCase(keyEntry.getKey(), "Check ID")) {
				subjectLine = StringUtils.replace(subjectLine, keyEntry.getValue(), checkId);
			} else if (StringUtils.equalsIgnoreCase(keyEntry.getKey(), "Client Name")) {
				subjectLine = StringUtils.replace(subjectLine, keyEntry.getValue(), clientName);
			} else {
				String keyValue = checkRecord.has(keyEntry.getKey())
						&& StringUtils.isNotEmpty(checkRecord.get(keyEntry.getKey()).asText())
								? checkRecord.get(keyEntry.getKey()).asText()
								: PLEASE_SPECIFY;
				if (StringUtils.containsIgnoreCase(keyEntry.getKey(), "date")) {
					keyValue = getDateFormat(keyValue);
				}
				subjectLine = StringUtils.replace(subjectLine, keyEntry.getValue(), keyValue);
			}
		}

		return subjectLine;
	}

	private Map<String, String> processSpocEmail(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, SPOCListPOJO spocPojo, String akaName)
			throws JsonProcessingException {
		logger.info("Value of aka Name:{}", akaName);
		String failedMessage = "";

		String caseDetailsStr = caseSpecificInfoPOJO.getCaseDetails();
		String componentName = caseSpecificRecordDetailPOJO.getComponentName();
		JsonNode caseDetails = mapper.readTree(caseDetailsStr);

		String dataEntryStr = caseSpecificInfoPOJO.getDataEntryInfo() != null
				|| StringUtils.isNotEmpty(caseSpecificInfoPOJO.getDataEntryInfo())
						? caseSpecificInfoPOJO.getDataEntryInfo()
						: "{}";
		JsonNode dataEntryNode = mapper.readTree(dataEntryStr);
		JsonNode personalDetailsNode = dataEntryNode.has(PERSONAL_DETAILS_BVF) ? dataEntryNode.get(PERSONAL_DETAILS_BVF)
				: mapper.createObjectNode();

		String firstName = personalDetailsNode.has("firstname")
				&& StringUtils.isNotEmpty(personalDetailsNode.get("firstname").asText())
						? personalDetailsNode.get("firstname").asText()
						: "";
		String lastName = personalDetailsNode.has("lastname")
				&& StringUtils.isNotEmpty(personalDetailsNode.get("lastname").asText())
						? personalDetailsNode.get("lastname").asText()
						: "";
		
		if(StringUtils.isEmpty(firstName) && StringUtils.isEmpty(lastName)) {
			firstName=PLEASE_SPECIFY;
		}
		String clientName = PLEASE_SPECIFY;
		String emailSent = FAILED;
		SPOCEmailConfigPOJO spocEmailConfigPOJO = new SPOCEmailConfigPOJO();
		if (caseDetails != null) {
			clientName = caseDetails.has("Client Name(Full Business name)")
					&& StringUtils.isNotEmpty(caseDetails.get("Client Name(Full Business name)").asText())
							? caseDetails.get("Client Name(Full Business name)").asText()
							: PLEASE_SPECIFY;
		}

		// Taking information from caseSpecificInfo
		String recordStr = caseSpecificRecordDetailPOJO.getComponentRecordField();
		JsonNode records = mapper.readTree(recordStr);

		String employeeID = PLEASE_SPECIFY;
		String cityName = PLEASE_SPECIFY;

		Map<String, Object> recordResult = mapper.convertValue(records, new TypeReference<Map<String, Object>>() {
		});
		logger.info("Size of recordResult:{}", recordResult.size());
		if (records != null) {
			employeeID = records.get("Employee ID") != null
					&& StringUtils.isNotEmpty(records.get("Employee ID").asText()) ? records.get("Employee ID").asText()
							: PLEASE_SPECIFY;
			cityName = records.get("City") != null && StringUtils.isNotEmpty(records.get("City").asText())
					? records.get("City").asText()
					: PLEASE_SPECIFY;
		}

		String spocEmailSearchString = "{\"contactCardName\":" + "\"" + spocPojo.getCompanyAkaName() + "\"" + "}";
		logger.info("spoc Email Search String: {}", spocEmailSearchString);
		List<SPOCEmailConfigPOJO> spocEmailConfigList = new ArrayList<>();
		try {
			String spocEmailResponse = apiService.sendDataToPost(spocEmailRestUrl, spocEmailSearchString);
			logger.info("SPOC Email Response : {}", spocEmailResponse);
			spocEmailConfigList = mapper.readValue(spocEmailResponse, new TypeReference<List<SPOCEmailConfigPOJO>>() {
			});

		} catch (Exception e) {
			failedMessage = "Exception while calling sendDataToEmailConfig";
			logger.info("{} : {}", failedMessage, e.getMessage());
		}
		boolean isSendDataToL3 = true;
		if (CollectionUtils.isNotEmpty(spocEmailConfigList)) {

			spocEmailConfigPOJO = spocEmailConfigList.get(0);
			Long templateNo = null;
			List<String> toEmailAddressList = new ArrayList<>();
			List<String> ccEmailAddressList = new ArrayList<>();
			List<String> bccEmailAddressList = new ArrayList<>();
			SpocConfigPOJO spocConfigPOJO = additionalInfoForSPOC(akaName, mapper, employeeID, cityName);

			if (spocConfigPOJO != null) {
				templateNo = spocConfigPOJO.getTemplateNo() != null ? spocConfigPOJO.getTemplateNo() : 0;
				toEmailAddressList = spocConfigPOJO.getToEmailAddress() != null ? spocConfigPOJO.getToEmailAddress()
						: new ArrayList<>();
				ccEmailAddressList = spocConfigPOJO.getCcEmailAddress() != null ? spocConfigPOJO.getCcEmailAddress()
						: new ArrayList<>();
			}

			SPOCEmailConfigPOJO spocCEmailConfigPOJO = spocEmailConfigList.get(0);
			String checkId = caseSpecificRecordDetailPOJO.getInstructionCheckId();

			String subjectLine = getSubjectLine(spocCEmailConfigPOJO.getSubjectLine(), firstName, lastName, checkId,
					clientName, records, false);
			String attachmentName = getSubjectLine(spocCEmailConfigPOJO.getMRLDocumentAttachmentFileName(), firstName,
					lastName, checkId, clientName, records, false);
			String mailBodyOrAttachment = spocCEmailConfigPOJO.getVerificationDetailsInMailBodyOrAttachment();
			String contactCardName = spocCEmailConfigPOJO.getContactCardName();
			String singleOrBulkInitiation = spocCEmailConfigPOJO.getSingleInitiationBulkInitiation();

			if (singleOrBulkInitiation.equalsIgnoreCase("Single Initiation")) {
				if (mailBodyOrAttachment.equalsIgnoreCase("mail body")) {
					String spocTemplateSearchString = "{\"contactCardName\":" + "\"" + contactCardName + "\""
							+ ",\"componentName\":" + "\"" + caseSpecificRecordDetailPOJO.getComponentName() + "\""
							+ "}";
					logger.info("Spoc template Search String : {}", spocTemplateSearchString);
					logger.info("Call Spoc Email Template Search Rest End Points");
					List<SPOCEmailTemplateMappingPOJO> spocEmailTemplateMappingList = null;

					try {
						String spocTemplateResponse = apiService.sendDataToPost(spocEmailTemplateRestUrl,
								spocTemplateSearchString);
						logger.info("SPOC Email Template Response : {}", spocTemplateResponse);
						spocEmailTemplateMappingList = mapper.readValue(spocTemplateResponse,
								new TypeReference<List<SPOCEmailTemplateMappingPOJO>>() {
								});

					} catch (Exception e) {
						failedMessage = "Exception while calling spocEmailTemplateMapping Api";
						logger.info("{} : {}", failedMessage, e.getMessage());
					}
					String spocTemplateNumberSearch = null;
					String spocTemplateNumberResponse = null;
					if (CollectionUtils.isNotEmpty(spocEmailTemplateMappingList)) {
						// Need to Change Template Number to ID
						String templateNumber = spocEmailTemplateMappingList.get(0).getTemplateNumber();
						if (templateNo > 0) {
							templateNumber = templateNo.toString();
						}
						logger.info("Send Information With Mail Body : {}", templateNumber);
						spocTemplateNumberSearch = "{\"id\":" + "\"" + templateNumber + "\"" + "}";
						logger.info("Value of Email Template Search String : {}", spocTemplateNumberSearch);
						List<SPOCEmailTemplatePOJO> spocEmailTemplateList = null;
						try {
							spocTemplateNumberResponse = apiService.sendDataToPost(spocTemplateSearchRestUrl,
									spocTemplateNumberSearch);
							logger.info("SPOC Email Template Number Response : {}", spocTemplateNumberResponse);
							spocEmailTemplateList = mapper.readValue(spocTemplateNumberResponse,
									new TypeReference<List<SPOCEmailTemplatePOJO>>() {
									});
						} catch (Exception e) {
							failedMessage = "Exception in calling sendDataToFind Email Template Number";
							logger.info("{} : {}", failedMessage, e.getMessage());
						}

						if (CollectionUtils.isNotEmpty(spocEmailTemplateList)) {
							String spocEmailTemplate = getSubjectLine(spocEmailTemplateList.get(0).getEmailTemplate(),
									firstName, lastName, checkId, clientName, records, true);

							logger.info("Value of Email Template : {}", spocEmailTemplate);

							// Before Sending Email Download the MRL Documents
							List<File> fileList = null;
							fileList = getFileList(mapper, caseSpecificInfoPOJO.getCaseNumber(), checkId,
									attachmentName, akaName, componentName);
							// Merge File
							logger.info("File List:{} and its Size:{}", fileList, fileList.size());
							try {
								File file = mergeFile(fileList, attachmentName);
								if (CollectionUtils.isEmpty(fileList)) {
									file = null;
								}
								emailSent = sendEmail(subjectLine, toEmailAddressList, ccEmailAddressList,
										bccEmailAddressList, spocEmailTemplate, file);
							} catch (IOException e) {
								failedMessage = "Exception while sending mail without attachment";
								logger.error("Exception while sending mail without attachment : {}", e.getMessage());
							}

						} else {
							failedMessage = "Spoc Template Not Found";
							logger.info("Spoc Template Not Found : {}", spocTemplateNumberResponse);
						}
					} else {
						failedMessage = "Value of spocEmailTemplateMappingList is Empty";
						logger.info(failedMessage);
					}
				} else {
					logger.info("Send mail as Attachment");

					logger.info("Generate Excel and Send by Mail");
					logger.info("Generate Excel");

					String spocExcelTemplateSearchString = "{\"contactCardName\":" + "\""
							+ spocEmailConfigList.get(0).getContactCardName() + "\"" + ",\"componentName\":" + "\""
							+ caseSpecificRecordDetailPOJO.getComponentName() + "\"" + "}";
					logger.info("Spoc template Search String : {}", spocExcelTemplateSearchString);
					String spocTemplateResponse = apiService.sendDataToPost(spocExcelTemplateRestUrl,
							spocExcelTemplateSearchString);
					logger.info("SPOC Excel Template Response : {}", spocTemplateResponse);
					List<SPOCExcelTemplatePOJO> spocEmailTemplateList = mapper.readValue(spocTemplateResponse,
							new TypeReference<List<SPOCExcelTemplatePOJO>>() {
							});
					spocTemplateResponse = mapper.writeValueAsString(spocEmailTemplateList);
					if (spocTemplateResponse != null && !spocTemplateResponse.equals("[]")) {
						logger.info("Send Information With Excel attachment : {}",
								spocEmailTemplateList.get(0).getTemplateHeaders());

						ObjectMapper oMapper = new ObjectMapper();
						// object -> Map
						TemplateHeaders[] map = oMapper.convertValue(spocEmailTemplateList.get(0).getTemplateHeaders(),
								TemplateHeaders[].class);
						List<TemplateHeaders> th = new ArrayList<>(Arrays.asList(map));
						logger.info("{}", th.size());
						logger.info("{} {}", map[0], th.get(0));
						// Take Excel value to fill
						List<String> excelData = new ArrayList<>();
						if (records != null) {
							for (int i = 0; i < th.size(); i++) {
								String temp = "";
								String convertedDate = "";
								if (StringUtils.equalsIgnoreCase("Client Name", th.get(i).getDocumentName())) {
									temp = clientName;
								} else if (StringUtils.equalsIgnoreCase("Name of the Candidate",
										th.get(i).getDocumentName())) {
									temp = firstName + " " + lastName;
								} else if (StringUtils.equalsIgnoreCase("CheckID", th.get(i).getDocumentName())) {
									temp = checkId;
								} else if (StringUtils.equalsIgnoreCase("DOB", th.get(i).getDocumentName())
										|| StringUtils.equalsIgnoreCase("Date of Exit", th.get(i).getDocumentName())
										|| StringUtils.equalsIgnoreCase("Date of Expiry", th.get(i).getDocumentName())
										|| StringUtils.equalsIgnoreCase("Date of LOA", th.get(i).getDocumentName())
										|| StringUtils.equalsIgnoreCase("Date of joining", th.get(i).getDocumentName())
										|| StringUtils.equalsIgnoreCase("Dates of employment",
												th.get(i).getDocumentName())) {
									temp = records.get(th.get(i).getDocumentName()) != null
											&& StringUtils.isNotEmpty(records.get(th.get(i).getDocumentName()).asText())
													? records.get(th.get(i).getDocumentName()).asText()
													: PLEASE_SPECIFY;
									if (StringUtils.isNotEmpty(temp)) {
										convertedDate = getDateFormat(temp);
										if (convertedDate != null) {
											temp = convertedDate;
										}
									}
								} else {
									temp = records.get(th.get(i).getDocumentName()) != null
											&& StringUtils.isNotEmpty(records.get(th.get(i).getDocumentName()).asText())
													? records.get(th.get(i).getDocumentName()).asText()
													: PLEASE_SPECIFY;
								}
								excelData.add(temp);
							}
						}

						try {
							/*
							 * ,firstName,lastName,employeeID,entitySpecificId,attachmentName,
							 * dateOfJoining,dateOfExit,designation,clientName,companyName, dateOfBirth
							 */
							JExcelUtility.writeJExcel(th, excelData);
						} catch (IOException | WriteException e) {
							logger.error(e.getMessage(), e);
						}
						logger.info("Sent as Attachment");
						String emailTemplate = findSpocEmailTemplate(contactCardName, caseSpecificRecordDetailPOJO,
								mapper, firstName, lastName, clientName);
						// Before Sending Email Download the MRL Documents
						List<File> fileList = null;
						fileList = getFileList(mapper, caseSpecificInfoPOJO.getCaseNumber(),
								caseSpecificRecordDetailPOJO.getInstructionCheckId(), attachmentName, akaName,
								componentName);

						// Merge File
						try {
							File file = mergeFile(fileList, attachmentName);
							if (CollectionUtils.isEmpty(fileList)) {
								file = null;
							}
							emailTemplate = emailTemplate == null ? "" : emailTemplate;
							emailSent = sendEmailWithAttachment(subjectLine, toEmailAddressList, ccEmailAddressList,
									bccEmailAddressList, emailTemplate, attachmentName, file);

						} catch (IOException e) {
							failedMessage = "Exception while sending mail with attachment";
							emailSent = FAILED;
							logger.error("Exception while sending mail with attachment : {}", e.getMessage());
						}
					} else {
						failedMessage = "SPOC Excel Template Response is Empty";
						logger.info(failedMessage);
					}
				}
			} else if (singleOrBulkInitiation.equalsIgnoreCase("Bulk Initiation")) {
				isSendDataToL3 = spocBulk(mapper, caseSpecificInfoPOJO, akaName, checkId);
			}

		} else {
			failedMessage = "Value of Email Config List is Empty";
			logger.info(failedMessage);
		}

		failedMessage = StringUtils.isEmpty(failedMessage) && StringUtils.equalsIgnoreCase(emailSent, FAILED)
				&& isSendDataToL3 ? "Unable to send email" : failedMessage;

		if (StringUtils.isNotEmpty(failedMessage)) {
			return createFailedResponse(failedMessage);
		}
		return processSpocResponse(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO, emailSent,
				spocEmailConfigPOJO, isSendDataToL3);
	}

	private boolean spocBulk(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO, String akaName,
			String checkId) throws JsonProcessingException {
		boolean isSendDataToL3;
		// Logic For Bulk Initiation
		// Take Excel template and generate Excel and take mrl document and make zip
		logger.info("Call Bulk Initiation");
		logger.info("Generate Excel");
		logger.info("Make flag to False");
		saveDataSPOCBulk(mapper, caseSpecificInfoPOJO, akaName, checkId);
		isSendDataToL3 = false;
		return isSendDataToL3;
	}

	private void saveDataSPOCBulk(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO, String akaName,
			String checkId) throws JsonProcessingException {
		// Save Data in DB
		SPOCBulkPOJO sPOCBulkPOJO = new SPOCBulkPOJO();
		sPOCBulkPOJO.setAkaName(akaName);
		sPOCBulkPOJO.setCaseReference(caseSpecificInfoPOJO.getCaseRefNumber());
		sPOCBulkPOJO.setCaseNumber(caseSpecificInfoPOJO.getCaseNumber());
		sPOCBulkPOJO.setCheckId(checkId);
		sPOCBulkPOJO.setCandidateName(caseSpecificInfoPOJO.getCandidateName());
		sPOCBulkPOJO.setClientName(caseSpecificInfoPOJO.getClientName());
		sPOCBulkPOJO.setFlag("A");
		// Call API for Storing Data in DB
		// spoc.bulk.url
		// get SPOC Bulk object as a json string
		String requestStr = mapper.writeValueAsString(sPOCBulkPOJO);
		logger.info(requestStr);
		String spocBulkResponse = apiService.sendDataToPost(spocBulkUrl, requestStr);
		logger.info("Response:{}", spocBulkResponse);
	}

	private String getThirdAkaName(ObjectMapper mapper, CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO) {

		String recordStr = caseSpecificRecordDetailPOJO.getComponentRecordField() != null
				? caseSpecificRecordDetailPOJO.getComponentRecordField()
				: "{}";
		String thirdAkaName = "";
		try {
			if (recordStr != null && StringUtils.isNotEmpty(recordStr)) {
				JsonNode recordNode = mapper.readValue(recordStr, JsonNode.class);
				thirdAkaName = recordNode.has("Agency Company AKA Name")
						? recordNode.get("Agency Company AKA Name").asText()
						: "";

				if (thirdAkaName == null || StringUtils.isEmpty(thirdAkaName)) {
					thirdAkaName = recordNode.has("Company Aka Name") ? recordNode.get("Company Aka Name").asText()
							: "";
					// thirdAkaName = recordNode.has("Agency Company AKA Name") ?
					// recordNode.get("Agency Company AKA Name").asText(): "";
				}
			}
		} catch (JsonProcessingException e) {
			logger.info("Error while mapping record node : {}", e.getMessage());
		}
		return thirdAkaName;
	}

	private Map<String, String> processSpocResponse(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, String emailSent,
			SPOCEmailConfigPOJO spocEmailConfigPOJO, boolean isSendDataToL3) {

		String checkId = caseSpecificRecordDetailPOJO.getInstructionCheckId() != null
				? caseSpecificRecordDetailPOJO.getInstructionCheckId()
				: "";

		Map<String, String> returnMessage = new HashMap<>();

		try {
			String spocEmailConfigStr = mapper.writeValueAsString(spocEmailConfigPOJO);
			logger.info("spocEmailConfigStr : {} ", spocEmailConfigStr);
			if (StringUtils.isNotEmpty(checkId)) {
				if (isSendDataToL3) {
					//temporarily Change 
					//String l3Response = sendVerifyDataToL3(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO,
						//	checkId, true, spocEmailConfigPOJO);
					String l3Response="";
					logger.info("SPOC FOUND");
					returnMessage.put(RETURN_MESSAGE, SPOC_FOUND);
					returnMessage.put(L3_RESPONSE, l3Response);
					if (spocEmailConfigStr != null && StringUtils.isNotEmpty(spocEmailConfigStr)) {
						returnMessage.put(SPOC_EMAIL_RESPONSE, spocEmailConfigStr);
					}
				} else {
					logger.info(SPOC_FOUND_FOR_BULK);
					returnMessage.put(RETURN_MESSAGE, SPOC_FOUND_FOR_BULK);
					returnMessage.put(L3_RESPONSE, "{}");
				}

			} else {
				logger.info("SPOC NOT FOUND");
				returnMessage.put(RETURN_MESSAGE, SPOC_NOT_FOUND);
				returnMessage.put(L3_RESPONSE, "{}");
			}
			returnMessage.put(EMAIL_SENT, emailSent);
			return returnMessage;
		} catch (JsonProcessingException e) {
			logger.error("Exception while sending verify data to l3 : {}", e.getMessage());
			return null;
		}
	}

	private Map<String, String> createFailedResponse(String response) {
		Map<String, String> returnMessage = new HashMap<>();
		returnMessage.put(RETURN_MESSAGE, response);
		returnMessage.put(L3_RESPONSE, "{}");
		returnMessage.put(EMAIL_SENT, FAILED);

		return returnMessage;
	}

	private String sendVerifyDataToL3(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, String checkId, boolean spoc,
			SPOCEmailConfigPOJO spocEmailConfigPOJO) throws JsonProcessingException {

		TaskSpecsPOJO taskSpecs = new TaskSpecsPOJO();

		String response = apiService.sendDataToGet(holidayListUrl);
		response = response.replace("[", "").replace("]", "").replace("\"", "");

		String[] split = response.split(",");
		List<String> holidayList = Arrays.asList(split);
		holidayList.replaceAll(String::trim);

		logger.info("holidayList : {}", holidayList);

		taskSpecs.setCaseReference(
				getCaseReference(mapper, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO, checkId, spoc));
		taskSpecs.setCheckVerification(
				getCheckVerification(caseSpecificRecordDetailPOJO, spoc, spocEmailConfigPOJO, holidayList));

		taskSpecs
				.setQuestionaire(getQuestionnaire(mapper, checkId, caseSpecificInfoPOJO, caseSpecificRecordDetailPOJO));

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

	private CaseReferencePOJO getCaseReference(ObjectMapper mapper, CaseSpecificInfoPOJO caseSpecificInfoPOJO,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, String checkId, boolean spoc)
			throws JsonProcessingException {
		CaseReferencePOJO caseReference = mapper.readValue(caseSpecificInfoPOJO.getCaseReference(),
				CaseReferencePOJO.class);

		caseReference.setCheckId(checkId);
		if (Boolean.TRUE.equals(spoc)) {
			caseReference.setNgStatus("F2");
			caseReference.setNgStatusDescription("Followup 2");
		}
		caseReference.setSbuName(caseSpecificInfoPOJO.getSbuName());
		caseReference.setProductName(caseSpecificRecordDetailPOJO.getProduct());
		caseReference.setPackageName(caseSpecificInfoPOJO.getPackageName());
		caseReference.setComponentName(caseSpecificRecordDetailPOJO.getComponentName());

		return caseReference;
	}

	private CheckVerificationPOJO getCheckVerification(CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO,
			boolean spoc, SPOCEmailConfigPOJO spocEmailConfigPOJO, List<String> holidays) {
		CheckVerificationPOJO checkVerification = new CheckVerificationPOJO();

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");

		checkVerification.setCountry("India");

		if (Boolean.TRUE.equals(spoc)) {
			checkVerification.setExecutiveSummaryComments("");
			checkVerification.setInternalNotes(
					"Name not disclosed, Official from Human Resource Department advised all verifications are handled via email. We have complied with this request.");
			checkVerification.setEndStatusOfTheVerification("Pending for Reply");
		}

		checkVerification.setReportComments("");
		checkVerification.setResultCode("");
		checkVerification.setEmailId("");
		checkVerification.setVerifierName("Name not disclosed");
		checkVerification.setGeneralRemarks("");
		checkVerification.setModeOfVerification("Email ID-Official");
		checkVerification.setVerifiedDate("");
		checkVerification.setAction("verifyChecks");
		checkVerification.setSubAction("verifyChecks");
		checkVerification.setActionCode("");
		checkVerification.setComponentName(caseSpecificRecordDetailPOJO.getComponentName());
		checkVerification.setAttempts("Internal");
		checkVerification.setDepartmentName("");
		checkVerification.setKeyWords("");
		checkVerification.setCost("");
		checkVerification.setIsThisAVerificationAttempt("Yes");
		checkVerification.setDateVerificationCompleted("");
		checkVerification.setDisposition("");

		try {
			checkVerification.setExpectedClosureDate(formatter.format(Utility.addDaysSkippingWeekends(new Date(),
					Integer.parseInt(spocEmailConfigPOJO.getExpectedClosureDate()), holidays)));
		} catch (Exception e) {
			checkVerification.setExpectedClosureDate("");
		}

		checkVerification.setVerifierDesignation("Official");
		try {
			checkVerification.setFollowUpDateAndTimes(formatter.format(Utility.addDaysSkippingWeekends(new Date(),
					Integer.parseInt(spocEmailConfigPOJO.getFollowUpDate1()), holidays)));
		} catch (Exception e) {
			checkVerification.setFollowUpDateAndTimes("");
		}
		checkVerification.setVerifierNumber("");

		return checkVerification;
	}

	private List<QuestionnairePOJO> getQuestionnaire(ObjectMapper mapper, String checkId,
			CaseSpecificInfoPOJO caseSpecificInfoPOJO, CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO)
			throws JsonProcessingException {
		// List<String> quetionRefIDList = new ArrayList<>();
		// quetionRefIDList.add("500110");
		// quetionRefIDList.add("807981");
		// quetionRefIDList.add("800054");
		// Take Out List of Questionnaire according to Component, Product and Package
		// Name
		PackageQuestionnaireExtractPOJO packageQuestionnaireExtractPOJO = new PackageQuestionnaireExtractPOJO();
		packageQuestionnaireExtractPOJO.setComponentName(caseSpecificRecordDetailPOJO.getComponentName());
		packageQuestionnaireExtractPOJO.setProductName(caseSpecificRecordDetailPOJO.getProduct());
		packageQuestionnaireExtractPOJO.setPackageName(caseSpecificInfoPOJO.getPackageName());
		Map<String,String> packageGlobalQuestionList = getGlobalQuestionList(mapper, packageQuestionnaireExtractPOJO);
		logger.info("Value of GlobalQuestionList:{} and size:{}", packageGlobalQuestionList,
				packageGlobalQuestionList.size());

		List<QuestionPOJO> questionPOJOList = new ArrayList<>();
		List<QuestionnairePOJO> questionnairePOJOList = new ArrayList<>();

		String requestUrl = questionaireURL + checkId;
		String questionResponse = apiService.sendDataToL3Get(requestUrl);
		ObjectNode attemptQuestionnaireNode = mapper.createObjectNode();
		JsonNode questionnaire = mapper.createObjectNode();

		logger.info("Questionnaire response : {}", questionResponse);
		if (questionResponse != null && StringUtils.isNotEmpty(questionResponse)) {
			attemptQuestionnaireNode = (ObjectNode) mapper.readTree(questionResponse);
		}
		if (attemptQuestionnaireNode != null && !attemptQuestionnaireNode.isEmpty()
				&& attemptQuestionnaireNode.has(RESPONSE)) {
			questionnaire = attemptQuestionnaireNode.get(RESPONSE);
		}
		if (questionnaire != null && !questionnaire.isEmpty()) {
			questionPOJOList = mapper.readValue(questionnaire.toString(), new TypeReference<List<QuestionPOJO>>() {
			});
		}

		for (QuestionPOJO questionPOJO : questionPOJOList) {
			String globalQuestionId = questionPOJO.getGlobalQuestionId() != null ? questionPOJO.getGlobalQuestionId()
					: "";
			/* if (quetionRefIDList.contains(globalQuestionId)) { */
			if(packageGlobalQuestionList.containsKey(globalQuestionId)) {
			//if (packageGlobalQuestionList.stream().anyMatch(s -> s.equals(globalQuestionId))) {
				QuestionnairePOJO questionnairePOJO = new QuestionnairePOJO();
				questionnairePOJO.setCaseQuestionRefID(globalQuestionId);
				questionnairePOJO.setAnswer(questionPOJO.getAnswere());
				//questionnairePOJO.setQuestion(questionPOJO.getQuestionName());
				//questionnairePOJO.setQuestion(questionPOJO.getFormLabel());
				questionnairePOJO.setQuestion(packageGlobalQuestionList.get(globalQuestionId));
				questionnairePOJO.setReportData("");
				questionnairePOJO.setStatus("");
				questionnairePOJO.setVerifiedData("");
				questionnairePOJOList.add(questionnairePOJO);
			}

		}
		logger.info("Size of Questionnaire List:{}", questionnairePOJOList.size());
		return questionnairePOJOList;
	}

	private String sendEmail(String subjectLine, List<String> toEmailAddressList, List<String> ccEmailAddressList,
			List<String> bccEmailAddressList, String emailTemplate, File file) {
		String emailSent = FAILED;
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

			if (CollectionUtils.isEmpty(toEmailAddressList)) {
				toEmailAddressList.addAll(mailsentTo);
			}
			if (CollectionUtils.isEmpty(ccEmailAddressList)) {
				ccEmailAddressList.addAll(ccMailsentTo);
			}

			if (CollectionUtils.isNotEmpty(toEmailAddressList)) {
				mimeMessageHelper.setTo(toEmailAddressList.stream().toArray(String[]::new));
			}
			if (CollectionUtils.isNotEmpty(ccEmailAddressList)) {
				mimeMessageHelper.setCc(ccEmailAddressList.stream().toArray(String[]::new));
			}
			if (CollectionUtils.isNotEmpty(bccEmailAddressList)) {
				mimeMessageHelper.setBcc(bccEmailAddressList.stream().toArray(String[]::new));
			}

			mimeMessageHelper.setFrom(fromUserName);
			mimeMessageHelper.setSubject(subjectLine);
			mimeMessageHelper.setText(emailTemplate, true);

			if (file != null) {
				FileSystemResource fileSystemResource = new FileSystemResource(file);
				mimeMessageHelper.addAttachment(file.getName(), fileSystemResource);
			}

			mailSender.send(mimeMessage);
			logger.info("Email sent successfully.");
			emailSent = SUCCESS;

		} catch (Exception e) {
			logger.info("Exception while sending mail : {}", e.getMessage());
		}
		return emailSent;
	}

	private String sendEmailWithAttachment(String subjectLine, List<String> toEmailAddressList,
			List<String> ccEmailAddressList, List<String> bccEmailAddressList, String emailTemplate, String attachment,
			File file) {

		String emailSent = FAILED;
		File myObj;
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

			if (CollectionUtils.isEmpty(toEmailAddressList)) {
				toEmailAddressList.addAll(mailsentTo);
			}
			if (CollectionUtils.isEmpty(ccEmailAddressList)) {
				ccEmailAddressList.addAll(ccMailsentTo);
			}

			if (CollectionUtils.isNotEmpty(toEmailAddressList)) {
				mimeMessageHelper.setTo(toEmailAddressList.stream().toArray(String[]::new));
			}
			if (CollectionUtils.isNotEmpty(ccEmailAddressList)) {
				mimeMessageHelper.setCc(ccEmailAddressList.stream().toArray(String[]::new));
			}
			if (CollectionUtils.isNotEmpty(bccEmailAddressList)) {
				mimeMessageHelper.setBcc(bccEmailAddressList.stream().toArray(String[]::new));
			}

			mimeMessageHelper.setFrom(fromUserName);
			mimeMessageHelper.setSubject(subjectLine);
			mimeMessageHelper.setText(emailTemplate, true);
			File currDir = new File(".");
			String path = currDir.getAbsolutePath();
			String fileLocation = path.substring(0, path.length() - 1) + "tempj.xls";
			myObj = new File(fileLocation);

			mimeMessageHelper.addAttachment(attachment + ".xls", myObj);

			if (file != null) {
				FileSystemResource fileSystemResource = new FileSystemResource(file);
				mimeMessageHelper.addAttachment(file.getName(), fileSystemResource);
			}

			mailSender.send(mimeMessage);
			emailSent = SUCCESS;

			try {
				TimeUnit.SECONDS.sleep(10);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
			try {
				Files.delete(myObj.toPath());
			} catch (Exception e) {
				logger.error("Exception while deleting file : {}", e.getMessage());
			}

		} catch (Exception e1) {
			logger.info("Exception while sending mail : {}", e1.getMessage());
		}

		return emailSent;

	}

	// PDf Merge Logic
	private File mergeFile(List<File> fileList, String desiredFileName) throws IOException {
		List<PDDocument> pdDocuments = new ArrayList<>();

		// Instantiating PDFMergerUtility class
		PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
		// Setting the destination file
		pdfMergerUtility.setDestinationFileName(localFileLocation + desiredFileName + ".pdf");

		// Loading an existing PDF document
		for (File file : fileList) {
			pdDocuments.add(PDDocument.load(file));
			pdfMergerUtility.addSource(file);
		}
		// Merging the two documents
		pdfMergerUtility.mergeDocuments(null);
		for (PDDocument pdDocument : pdDocuments) {
			pdDocument.close();
		}

		for (File file : fileList) {
			try {
				Files.delete(file.toPath());
			} catch (Exception e) {
				logger.error("Exception while deleting file : {}", e.getMessage());
			}
		}
		return new File(localFileLocation + desiredFileName + ".pdf");
	}

	private List<File> getFileList(ObjectMapper mapper, String caseNumber, String checkId, String attachmentName,
			String akaName, String componentName) throws JsonProcessingException {

		List<String> checkIdList = new ArrayList<>();
		checkIdList.add(checkId);
		ObjectNode requestNode = mapper.createObjectNode();
		requestNode.put("caseNumber", caseNumber);
		requestNode.put("checkIdList", checkIdList.toString());
		requestNode.put("componentName", componentName);
		requestNode.put("akaName", akaName);

		String requestNodeStr = mapper.writeValueAsString(requestNode);
		logger.info("Request Node Value : {}", requestNodeStr);

		String filePathMapStr = spocApiService.sendDataToAttempt(associateFilePathUrl, requestNodeStr);

		logger.info("associate file response : {}", filePathMapStr);

		ArrayNode responseArrayNode = mapper.readValue(filePathMapStr, ArrayNode.class);

		List<File> attachmentList = new ArrayList<>();
		int index = 1;
		for (JsonNode responseNode : responseArrayNode) {
			String filePath = responseNode.has("filePath") ? responseNode.get("filePath").asText() : "";
			File filename = new File(localFileLocation + attachmentName + "-" + index + ".pdf");
			try {
				URL newURL = new URL(docUrl + filePath);
				logger.info("Filename : {} \nnewUrl : {}", filename, newURL);
				FileUtils.copyURLToFile(newURL, filename);
			} catch (Exception e) {
				logger.error("Exception While Copying File From URL To Disk:{}", e.getMessage());
				filename = null;
				// e.printStackTrace();
			}
			index++;
			if (filename != null) {
				attachmentList.add(filename);
			}

		}
		return attachmentList;
	}

	private String getDateFormat(String dateAsString) {
		DateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd");
		String returnDate = "";
		try {
			Date date = sourceFormat.parse(dateAsString);
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
			returnDate = formatter.format(date);
			logger.info("Value of date : {}, New date : {}", date, returnDate);
		} catch (ParseException e) {
			logger.error("Exception occurred while parsing date : {}", e.getMessage());
			returnDate = dateAsString;
			// e.printStackTrace();
		}
		return returnDate;
	}

	private SpocConfigPOJO additionalInfoForSPOC(String akaName, ObjectMapper mapper, String employeeId, String city) {
		logger.info("Value of aka Name : {}", akaName);
		ObjectNode spocSearchNode = mapper.createObjectNode();
		spocSearchNode.put("companyName", akaName);
		spocSearchNode.put("thirdCompanyName", akaName);

		SpocConfigPOJO spocConfigPOJO = new SpocConfigPOJO();

		logger.info("Value of SPOC Search String : {}", spocSearchNode);
		String spocAdditinalResponse = apiService.sendDataToPost(spocAdditionalConfigURL, spocSearchNode.toString());
		logger.info("spocAdditinalResponse : {}", spocAdditinalResponse);
		if (spocAdditinalResponse != null) {
			try {
				List<SpocEmailAddressMappingPOJO> spocEmailAddressMappingPOJOs = mapper.readValue(spocAdditinalResponse,
						new TypeReference<List<SpocEmailAddressMappingPOJO>>() {
						});

				if (CollectionUtils.isNotEmpty(spocEmailAddressMappingPOJOs)) {
					List<SpocEmailAddressMappingPOJO> spocEmailAddressMappingFiltered = new ArrayList<>();

					if (StringUtils.isNotEmpty(employeeId)) {
						spocEmailAddressMappingFiltered = spocEmailAddressMappingPOJOs.stream()
								.filter(data -> StringUtils.equalsIgnoreCase(data.getMappingType(), "Employee Id")
										&& StringUtils.startsWith(employeeId, data.getMappingValue()))
								.collect(Collectors.toList());
					}

					if (CollectionUtils.isEmpty(spocEmailAddressMappingFiltered) && StringUtils.isNotEmpty(city)) {
						spocEmailAddressMappingFiltered = spocEmailAddressMappingPOJOs.stream()
								.filter(data -> StringUtils.equalsIgnoreCase(data.getMappingType(), "City")
										&& StringUtils.equalsIgnoreCase(data.getMappingValue(), city))
								.collect(Collectors.toList());
					}
					if (CollectionUtils.isNotEmpty(spocEmailAddressMappingFiltered)) {
						String toEmailStr = spocEmailAddressMappingFiltered.get(0).getToEmailAddress();
						String ccEmailStr = spocEmailAddressMappingFiltered.get(0).getCcEmailAddress();
						spocConfigPOJO.setTemplateNo(spocEmailAddressMappingFiltered.get(0).getTemplateNo());

						try {
							spocConfigPOJO.setCcEmailAddress(Arrays.asList(ccEmailStr.trim().split("\\s*,\\s*")));
						} catch (Exception e) {
							spocConfigPOJO.setCcEmailAddress(new ArrayList<>());
						}

						try {
							spocConfigPOJO.setToEmailAddress(Arrays.asList(toEmailStr.trim().split("\\s*,\\s*")));
						} catch (Exception e) {
							spocConfigPOJO.setToEmailAddress(new ArrayList<>());
						}

						logger.info("templatNo : {} ", spocConfigPOJO.getTemplateNo());
						logger.info("ccEmailAddressList : {}", spocConfigPOJO.getCcEmailAddress());
						logger.info("toEmailAddressList : {}", spocConfigPOJO.getToEmailAddress());

					}
				}
			} catch (JsonProcessingException e) {
				logger.error("Exception while mapping spoc email address mapping response : {}", e.getMessage());
			}
		}
		return spocConfigPOJO;
	}

	/*
	 * List<String> getGlobalQuestionList(ObjectMapper mapper,
	 * PackageQuestionnaireExtractPOJO packageQuestionnaireExtractPOJO) {
	 */
	Map<String, String> getGlobalQuestionList(ObjectMapper mapper,
			PackageQuestionnaireExtractPOJO packageQuestionnaireExtractPOJO) {
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		List<GlobalQuestionAndFormLabelPOJO> questionIdList = new ArrayList<>();
		try {
			ObjectNode requestNode = mapper.createObjectNode();
			requestNode.put("componentName", packageQuestionnaireExtractPOJO.getComponentName());
			requestNode.put("packageName", packageQuestionnaireExtractPOJO.getPackageName());
			requestNode.put("productName", packageQuestionnaireExtractPOJO.getProductName());
			String response = apiService.sendDataToPost(questionPackageUrl, requestNode.toString());
			questionIdList = mapper.readValue(response, new TypeReference<List<GlobalQuestionAndFormLabelPOJO>>() {
			});

		} catch (Exception e) {
			logger.error("Exception occurred while fetching global question id list : {}", e.getMessage());
		}
		Map<String, String> questionMap = questionIdList.stream()
				.collect(Collectors.toMap(GlobalQuestionAndFormLabelPOJO::getGlobalQuestionId,
						GlobalQuestionAndFormLabelPOJO::getFormLabel, (questionId1, questionId2) -> {
							logger.info("Duplicate QuestionID Found:{}", questionId1);
							return questionId1;
						}));
		logger.info("questionIdList : {}", questionIdList);

		return questionMap;
			/*response = response != null ? response : "[]";
			response = response.replace("[", "").replace("]", "").replace("\"", "");
			String[] split = response.split(",");
			questionIdList = Arrays.asList(split);
			questionIdList.replaceAll(String::trim);
		} catch (Exception e) {
			logger.error("Exception occurred while fetching global question id list : {}", e.getMessage());
		}
		logger.info("questionIdList : {}", questionIdList);
		return questionIdList;*/
	}

	private String findSpocEmailTemplate(String contactCardName,
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetailPOJO, ObjectMapper mapper, String firstName,
			String lastName, String clientName) {
		String spocTemplateSearchString = "{\"contactCardName\":" + "\"" + contactCardName + "\""
				+ ",\"componentName\":" + "\"" + caseSpecificRecordDetailPOJO.getComponentName() + "\"" + "}";
		logger.info("Spoc template Search String : {}", spocTemplateSearchString);
		logger.info("Call Spoc Email Template Search Rest End Points");
		List<SPOCEmailTemplateMappingPOJO> spocEmailTemplateMappingList = null;
		try {
			String spocTemplateResponse = apiService.sendDataToPost(spocEmailTemplateRestUrl, spocTemplateSearchString);
			logger.info("SPOC Email Template Response : {}", spocTemplateResponse);
			spocEmailTemplateMappingList = mapper.readValue(spocTemplateResponse,
					new TypeReference<List<SPOCEmailTemplateMappingPOJO>>() {
					});

		} catch (Exception e) {
			logger.info("Excpetion in calling sendDataToEmailConfig : {}", e.getMessage());
		}

		String spocTemplateNumberSearch = null;
		String spocTemplateNumberResponse = null;

		if (CollectionUtils.isNotEmpty(spocEmailTemplateMappingList)) {
			// Need to Change Template Number to ID
			logger.info("Send Information With Mail Body : {}",
					spocEmailTemplateMappingList.get(0).getTemplateNumber());
			spocTemplateNumberSearch = "{\"id\":" + "\"" + spocEmailTemplateMappingList.get(0).getTemplateNumber()
					+ "\"" + "}";
			logger.info("Value of Email Template Search String : {}", spocTemplateNumberSearch);
			List<SPOCEmailTemplatePOJO> spocEmailTemplateList = null;
			try {
				spocTemplateNumberResponse = apiService.sendDataToPost(spocTemplateSearchRestUrl,
						spocTemplateNumberSearch);
				logger.info("SPOC Email Template Number Response : {}", spocTemplateNumberResponse);
				spocEmailTemplateList = mapper.readValue(spocTemplateNumberResponse,
						new TypeReference<List<SPOCEmailTemplatePOJO>>() {
						});
			} catch (Exception e) {
				logger.info("Excpetion in calling sendDataToFind Email Template Number : {}", e.getMessage());
				e.printStackTrace();
			}
			if (CollectionUtils.isNotEmpty(spocEmailTemplateList)) {
				String spocEmailTemplate = getSubjectLine(spocEmailTemplateList.get(0).getEmailTemplate(), firstName,
						lastName, spocTemplateNumberResponse, clientName, null, false);
				logger.info("Value of SPOC EMAIL Template:{}", spocEmailTemplate);
				return spocEmailTemplate;
			} else {
				logger.info("Value of SpocEmailTemplateList is Empty:{}", spocEmailTemplateList);
				return null;
			}

		} else {
			logger.info("Value of spocEmailTemplateMappingList:{}", spocEmailTemplateMappingList);
			return null;
		}
	}
}
