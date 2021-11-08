package com.gic.fadv.spoc.controller;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.spoc.model.AttemptHistory;
import com.gic.fadv.spoc.model.AttemptStatusData;
import com.gic.fadv.spoc.model.Component;
import com.gic.fadv.spoc.model.ComponentScoping;
import com.gic.fadv.spoc.model.SPOCReq;
import com.gic.fadv.spoc.service.ApiService;
import com.gic.fadv.spoc.service.SPOCApiService;
import com.gic.fadv.spoc.service.SPOCService;
import com.gic.fadv.spoc.utility.Utility;
import com.gic.fadv.spoc.pojo.CaseReferencePOJO;
import com.gic.fadv.spoc.pojo.CheckVerificationPOJO;
import com.gic.fadv.spoc.pojo.FileUploadPOJO;
import com.gic.fadv.spoc.pojo.SPOCEmailConfigPOJO;
import com.gic.fadv.spoc.pojo.SPOCEmailTemplateMappingPOJO;
import com.gic.fadv.spoc.pojo.SPOCEmailTemplatePOJO;
import com.gic.fadv.spoc.pojo.SPOCExcelTemplatePOJO;
import com.gic.fadv.spoc.pojo.SPOCListPOJO;
import com.gic.fadv.spoc.pojo.TaskSpecsPOJO;
import com.gic.fadv.spoc.pojo.TemplateHeaders;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.io.File;
import java.io.IOException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
@Api(value = "Cross Reference")
public class SPOCRouterController {

	@Autowired
	SPOCApiService spocApiService;

	@Autowired
	private SPOCService spocService;
	
	@Autowired
	private ApiService apiService;
	
	@Value("${holiday.list.url}")
	private String holidayListUrl;

	@Autowired
	private Environment env;
	@Autowired
	private JavaMailSender mailSender;

	@Value("${questionaire.list.l3.url}")
	private String questionaireURL;

	@Value("${verification.status.url.l3}")
	private String verificationStatusL3Url;

	@Value("${spocattemptstatusdata.rest.url}")
	private String spocAttemptStatusDataRestUrl;

	@Value("${verifictioneventstatus.rest.url}")
	private String verifictionEventStatusUrl;

	@Value("${associate.docs.case.url}")
	private String associateDocsUrl;

	@Value("${spring.mail.username}")
	private String fromUserName;

	@Value("${doc.url}")
	private String docUrl;

	@Value("${local.file.download.location}")
	private String localFileLocation;

	private static final Logger logger = LoggerFactory.getLogger(SPOCRouterController.class);

	@ApiOperation(value = "This service is used to process Records at Spoc router and return the result to workflow router ", response = List.class)
	@PostMapping(path = "/async/spocrouter", consumes = "application/json", produces = "application/json")
	public DeferredResult<ResponseEntity<String>> doAsyncProcess(@RequestBody String inStr) {
		DeferredResult<ResponseEntity<String>> ret = new DeferredResult<>();
		ForkJoinPool.commonPool().submit(() -> {
			logger.info("Got async Request :\n {}", inStr);
			processRequest(inStr, true);
			ret.setResult(ResponseEntity.ok("ok"));
		});
		ret.onCompletion(() -> logger.info("async process request done"));
		return ret;
	}

	@ApiOperation(value = "This service is used to process Records at Spoc router and return the result to workflow router ", response = List.class)
	@PostMapping(path = "/spocrouter", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> doProcess(@RequestBody String inStr) {
		try {
			logger.info("Got Request :\n {}", inStr);
			String response = processRequest(inStr, false);
			logger.info("Processed Request");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}

	/*
	 * @GetMapping("/test-spoc-controller") public List<String> testController() {
	 * // Creating the ObjectMapper object ObjectMapper mapper = new ObjectMapper();
	 * mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	 * return spocService.additionalInfoForSPOC("RSM and Co All Locations", mapper,
	 * "56747", ""); }
	 */

	@ApiOperation(value = "This service is used to process Records at CBV-UTV-I4V router and return the result to workflow router ", response = List.class)
	@PostMapping(path = "/spocroutercontroller", consumes = "application/json", produces = "application/json")
	public ObjectNode doRecordProcess(@RequestBody JsonNode requestBody) {
		logger.info("Got Request :\n {}", requestBody);
		ObjectNode response = null;
		try {
			response = spocService.processRequestBody(requestBody);
		} catch (JsonProcessingException e) {
			logger.error("Exception while processing spoc service : {}", e.getMessage());
			e.printStackTrace();
		}
		logger.info("Record request Processed");
		return response;
	}

	private String processRequest(String inStr, boolean asyncStatus) {
		try {
			LocalDateTime startTime = LocalDateTime.now();

			// Creating the ObjectMapper object
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			// Converting the JSONString to Object
			SPOCReq spocReq = mapper.readValue(inStr, SPOCReq.class);

			JsonNode dataObj = null;
			String spocResponse = null;
			String spocEmailResponse = null;
			String spocTemplateResponse = null;

			for (int i = 0; i < spocReq.getData().size(); i++) {
				List<ComponentScoping> componentScoping = spocReq.getData().get(i).getTaskSpecs().getComponentScoping();
				for (int j = 0; j < componentScoping.size(); j++) {
					ComponentScoping componentScopingObj = componentScoping.get(j);
					List<Component> components = componentScoping.get(j).getComponents();

					JsonNode caseDetails = componentScoping.get(j).getCaseDetails();
					String firstName = caseDetails.get("Candidate's First Name") != null
							? caseDetails.get("Candidate's First Name").asText()
							: "";
					String lastName = caseDetails.get("Candidate's Last Name") != null
							? caseDetails.get("Candidate's Last Name").asText()
							: "";
					String clientName = caseDetails.get("Client Name(Full Business name)") != null
							? caseDetails.get("Client Name(Full Business name)").asText()
							: "";
					String dateOfBirth = caseDetails.get("Date of Birth") != null
							? caseDetails.get("Date of Birth").asText()
							: "";

//					CaseSpecificInfoPOJO caseSpecificInfo = new CaseSpecificInfoPOJO();
//					caseSpecificInfo.setCandidateName(componentScoping.get(j).getCandidate_Name());
//					caseSpecificInfo.setCaseDetails(componentScoping.get(j).getCaseDetails().toString());
//					caseSpecificInfo.setCaseMoreInfo(componentScoping.get(j).getCaseMoreInfo().toString());
//					caseSpecificInfo.setCaseReference(componentScoping.get(j).getCaseReference().toString());
//					caseSpecificInfo.setCaseRefNumber(componentScoping.get(j).getCASE_REF_NUMBER());
//					caseSpecificInfo.setCaseNumber(componentScoping.get(j).getCASE_NUMBER());
//					caseSpecificInfo.setClientCode(componentScoping.get(j).getCLIENT_CODE());
//					caseSpecificInfo.setClientName(componentScoping.get(j).getCLIENT_NAME());
//					caseSpecificInfo.setSbuName(componentScoping.get(j).getSBU_NAME());
//					caseSpecificInfo.setPackageName(componentScoping.get(j).getPackageName());
//					caseSpecificInfo
//							.setClientSpecificFields(componentScoping.get(j).getClientSpecificFields().toString());

					/*
					 * Insert Info in DB
					 */
//					JsonNode caseSpecificInfoNode = mapper.convertValue(caseSpecificInfo,JsonNode.class); 
//					String caseSpecificInfoStr =caseSpecificInfoNode.toString();
//					String caseSpecificResponse=spocApiService.sendDataToCaseSpecificInfo(caseSpecificInfoStr);
//					CaseSpecificInfoPOJO caseSpecificInfo1 = new CaseSpecificInfoPOJO();
//					if(caseSpecificResponse!=null) {
//						JsonNode caseSpecificResponseNode= mapper.readTree(caseSpecificResponse);
//						caseSpecificInfo1 = mapper.treeToValue(caseSpecificResponseNode, CaseSpecificInfoPOJO.class);
//					}
//					Long caseSpecificInfoId=null;
//					if(caseSpecificInfo1!=null) {
//						caseSpecificInfoId=caseSpecificInfo1.getCaseSpecificId();
//					}
//					
//					List<CaseSpecificRecordDetailPOJO> caseSpecificRecordDetailPOJOs = new ArrayList<CaseSpecificRecordDetailPOJO>();
//					AttemptHistory attemptHistory;

					for (int k = 0; k < components.size(); k++) {
						List<JsonNode> records = components.get(k).getRecords();
						if (!records.isEmpty()) {
							for (int l = 0; l < records.size(); l++) {
								// Check for MI=Yes
								logger.info("Value of MI" + records.get(l).get("MI"));
								if (records.get(l).has("MI")
										&& StringUtils.equalsIgnoreCase("No", records.get(l).get("MI").asText())) {

									String dateOfJoining = records.get(l).get("Date of joining") != null
											? records.get(l).get("Date of joining").asText()
											: "";
									String dateOfExit = records.get(l).get("Date of Exit") != null
											? records.get(l).get("Date of Exit").asText()
											: "";
									String employeeID = records.get(l).get("Employee ID") != null
											? records.get(l).get("Employee ID").asText()
											: "";
									String entitySpecificId = records.get(l).get("Entity Specific ID") != null
											? records.get(l).get("Entity Specific ID").asText()
											: "";
									String designation = records.get(l).get("Designation") != null
											? records.get(l).get("Designation").asText()
											: "";
									String companyName = records.get(l).get("Company Name") != null
											? records.get(l).get("Company Name").asText()
											: "";

									ArrayNode resultArray;
									if (records.get(l).get("ruleResult").isArray()) {
										resultArray = (ArrayNode) records.get(l).get("ruleResult").get(0).get("result");
									} else {
										resultArray = (ArrayNode) records.get(l).get("ruleResult").get("result");
									}
									if (resultArray.get(0).toString().contains("Include")) {
										String result = "";

										logger.info("Send to Spoc Process/Service");
										String spocSearchString = "";
										String spocEmailSearchString = "";
										String spocTemplateSearchString = "";
										JsonNode thirdAkaName = records.get(l)
												.get("Third Party or Agency Name and Address");
										if (thirdAkaName != null && !thirdAkaName.isNull()
												&& thirdAkaName.asText().trim().length() > 1) {

											spocSearchString = "{\"companyAkaName\":"
													+ records.get(l).get("Third Party or Agency Name and Address")
													+ ",\"companyName\":" + records.get(l).get("Company Name") + "}";

										} else {

											spocSearchString = "{\"companyAkaName\":"
													+ records.get(l).get("Company Aka Name") + ",\"companyName\":"
													+ records.get(l).get("Company Name") + "}";

										}
										logger.info("Value of Spoc Search String : {}", spocSearchString);
										logger.info("Call Spoc Rest End Points");
										List<SPOCListPOJO> spocList = null;
										try {
											spocResponse = spocApiService.sendDataToSPOCListRest(spocSearchString);
											logger.info("Spoc Response with ID : {}", spocResponse);
											spocList = mapper.readValue(spocResponse,
													new TypeReference<List<SPOCListPOJO>>() {
													});
											spocResponse = mapper.writeValueAsString(spocList);
										} catch (Exception e) {
											logger.info("Excpetion in calling sendDataToSpocListRest : {}",
													e.getMessage());
											e.printStackTrace();
										}
										if (spocResponse != null && !spocResponse.equals("[]")) {
											logger.info("Spoc Response Without ID : {}", spocResponse);
											logger.info("Found Result In Spoc");
											logger.info("Call SPOC Template Download");

											/*
											 * spocEmailSearchString = "{\"contactCardName\":" + "\"" +
											 * spocList.get(0).getCompanyName() + "\"" + "}";
											 */
											spocEmailSearchString = "{\"contactCardName\":" + "\""
													+ spocList.get(0).getCompanyAkaName() + "\"" + "}";
											logger.info("spoc Email Search String : {}", spocEmailSearchString);
											logger.info("Call Spoc Email Config Rest End Points");
											List<SPOCEmailConfigPOJO> spocEmailConfigList = null;
											try {
												spocEmailResponse = spocApiService
														.sendDataToEmailConfig(spocEmailSearchString);
												logger.info("SPOC Email Response : {}", spocEmailResponse);
												spocEmailConfigList = mapper.readValue(spocEmailResponse,
														new TypeReference<List<SPOCEmailConfigPOJO>>() {
														});
												spocEmailResponse = mapper.writeValueAsString(spocEmailConfigList);
											} catch (Exception e) {
												logger.info("Excpetion in calling sendDataToEmailConfig : {}",
														e.getMessage());
												e.printStackTrace();
											}
											if (spocEmailResponse != null && !spocEmailResponse.equals("[]")) {
												logger.debug("Value of SPOC Email Response : {}", spocEmailResponse);
												String ccEmailID = spocEmailConfigList.get(0).getCCEmailId();
												String fromEmailID = spocEmailConfigList.get(0).getFromEmailID();
												String subjectLine = spocEmailConfigList.get(0).getSubjectLine();
												String toEmailID = spocEmailConfigList.get(0).getToEmailID();
												String attachmentName = spocEmailConfigList.get(0)
														.getMRLDocumentAttachmentFileName();

												if (spocEmailConfigList.get(0)
														.getVerificationDetailsInMailBodyOrAttachment()
														.equalsIgnoreCase("mail body")) {
													spocTemplateSearchString = "{\"contactCardName\":" + "\""
															+ spocEmailConfigList.get(0).getContactCardName() + "\""
															+ ",\"componentName\":" + "\""
															+ components.get(k).getComponentname() + "\"" + "}";
													logger.info("Spoc template Search String : {}",
															spocTemplateSearchString);
													logger.info("Call Spoc Email Template Search Rest End Points");
													List<SPOCEmailTemplateMappingPOJO> spocEmailTemplateMappingList = null;
													try {
														spocTemplateResponse = spocApiService
																.sendDataToEmailTemplate(spocTemplateSearchString);
														logger.info("SPOC Email Template Response : {}",
																spocTemplateResponse);
														spocEmailTemplateMappingList = mapper.readValue(
																spocTemplateResponse,
																new TypeReference<List<SPOCEmailTemplateMappingPOJO>>() {
																});
														spocTemplateResponse = mapper
																.writeValueAsString(spocEmailTemplateMappingList);

													} catch (Exception e) {
														logger.info("Excpetion in calling sendDataToEmailConfig : {}",
																e.getMessage());
														e.printStackTrace();
													}

													String spocTemplateNumberSearch = null;
													String spocTemplateNumberResponse = null;
													if (spocTemplateResponse != null
															&& !spocTemplateResponse.equals("[]")) {
														// Need to Change Template Number to ID
														logger.info("Send Information With Mail Body : {}",
																spocEmailTemplateMappingList.get(0).getId());
														spocTemplateNumberSearch = "{\"id\":" + "\""
																+ spocEmailTemplateMappingList.get(0).getId() + "\""
																+ "}";
														logger.info("Value of Email Template Search String : {}",
																spocTemplateNumberSearch);
														List<SPOCEmailTemplatePOJO> spocEmailTemplateList = null;
														try {
															spocTemplateNumberResponse = spocApiService
																	.sendDataToFindEmailTemplateNumber(
																			spocTemplateNumberSearch);
															logger.info("SPOC Email Template Number Response : {}",
																	spocTemplateNumberResponse);
															spocEmailTemplateList = mapper.readValue(
																	spocTemplateNumberResponse,
																	new TypeReference<List<SPOCEmailTemplatePOJO>>() {
																	});
															spocTemplateNumberResponse = mapper
																	.writeValueAsString(spocEmailTemplateList);
														} catch (Exception e) {
															logger.info(
																	"Excpetion in calling sendDataToFind Email Template Number : {}",
																	e.getMessage());
															e.printStackTrace();
														}
														if (spocTemplateNumberResponse != null
																&& !spocTemplateNumberResponse.equals("[]")) {
															logger.debug("Value of Email Template : {}",
																	spocEmailTemplateList.get(0).getEmailTemplate());
															String template = spocEmailTemplateList.get(0)
																	.getEmailTemplate();

															String checkId = records.get(l).get("checkId") != null
																	? records.get(l).get("checkId").asText()
																	: "";

															if (StringUtils.isNotEmpty(checkId)) {

																if (StringUtils.containsIgnoreCase(subjectLine,
																		"<<First Name>>")) {
																	subjectLine = StringUtils.replaceIgnoreCase(
																			subjectLine, "<<First Name>>", firstName);
																}
																if (StringUtils.containsIgnoreCase(subjectLine,
																		"<<Last Name>>")) {
																	subjectLine = StringUtils.replaceIgnoreCase(
																			subjectLine, "<<Last Name>>", lastName);
																}
																if (StringUtils.containsIgnoreCase(subjectLine,
																		"<<Check ID>>")) {
																	subjectLine = StringUtils.replaceIgnoreCase(
																			subjectLine, "<<Check ID>>", checkId);
																}
																if (StringUtils.containsIgnoreCase(subjectLine,
																		"<<Employee ID>>")) {
																	subjectLine = StringUtils.replaceIgnoreCase(
																			subjectLine, "<<Employee ID>>", employeeID);
																}
																// Same Thing will do for attachemnt
																if (StringUtils.containsIgnoreCase(attachmentName,
																		"<<First Name>>")) {
																	attachmentName = StringUtils.replaceIgnoreCase(
																			attachmentName, "<<First Name>>",
																			firstName);
																}
																if (StringUtils.containsIgnoreCase(attachmentName,
																		"<<Last Name>>")) {
																	attachmentName = StringUtils.replaceIgnoreCase(
																			attachmentName, "<<Last Name>>", lastName);
																}
																if (StringUtils.containsIgnoreCase(attachmentName,
																		"<<Check ID>>")) {
																	attachmentName = StringUtils.replaceIgnoreCase(
																			attachmentName, "<<Check ID>>", checkId);
																}
																if (StringUtils.containsIgnoreCase(attachmentName,
																		"<<Employee ID>>")) {
																	attachmentName = StringUtils.replaceIgnoreCase(
																			attachmentName, "<<Employee ID>>",
																			employeeID);
																}
																HashMap<String, String> stringsMap = new HashMap<>();

																stringsMap.put("&lt;&lt;First Name&gt;&gt;", firstName);
																stringsMap.put("&lt;&lt; First Name&gt;&gt;",
																		firstName);
																stringsMap.put("&lt;&lt;Last Name&gt;&gt;", lastName);
																stringsMap.put("&lt;&lt; Last Name&gt;&gt;", lastName);
																stringsMap.put("&lt;&lt;Employee ID&gt;&gt;",
																		employeeID);
																stringsMap.put(
																		"&lt;&lt;<span style=\"color: black\">Employee ID&gt;&gt;",
																		employeeID);
																stringsMap.put("&lt;&lt;Entity Specific ID&gt;&gt;",
																		entitySpecificId);
																stringsMap.put(
																		"&lt;&lt;<span style=\"color: black\">Date of joining&gt;&gt;",
																		dateOfJoining);
																stringsMap.put("&lt;&lt;Date of joining&gt;&gt;",
																		dateOfJoining);
																stringsMap.put("&lt;&lt;Date of exit&gt;&gt;",
																		dateOfExit);
																stringsMap.put("&lt;&lt;Designation&gt;&gt;",
																		designation);
																stringsMap.put("&lt;&lt;Check ID&gt;&gt;", checkId);
																stringsMap.put("&lt;&lt; Client&nbsp; Name&gt;&gt;",
																		clientName);
																stringsMap.put("&lt;&lt;Company Name&gt;&gt;",
																		companyName);
																stringsMap.put("&lt;&lt;DOB&gt;&gt;", dateOfBirth);
																stringsMap.put(
																		"&lt;&lt;&nbsp;Entity Specific ID&gt;&gt;",
																		entitySpecificId);

																Set<Entry<String, String>> set = stringsMap.entrySet();
																Iterator<Entry<String, String>> iterator = set
																		.iterator();

																while (iterator.hasNext()) {
																	Map.Entry<String, String> stringEntry = iterator
																			.next();
																	if (StringUtils.containsIgnoreCase(template,
																			stringEntry.getKey())) {
																		template = StringUtils.replaceIgnoreCase(
																				template, stringEntry.getKey(),
																				stringEntry.getValue());
																	}
																}
																// Before Sending Email Download the MRL Documents
																List<File> fileList = downloadMRLDocs(mapper,
																		componentScoping, j, attachmentName, checkId);
																// Merge File
																File file = mergeFile(fileList, attachmentName);
																// Match CheckID
																/*
																 * sendEmail(fromEmailID, subjectLine, toEmailID,
																 * ccEmailID, template,fileList);
																 */
																sendEmail(fromEmailID, subjectLine, toEmailID,
																		ccEmailID, template, file);
																// This is commented for time being
																// sendDataToL3F1(componentScoping.get(j),
																// components.get(k), records.get(l),
																// mapper, checkId, toEmailID,
																// spocEmailConfigList.get(0));

																logger.info("Spoc Template Found : {}",
																		spocTemplateNumberResponse);
																logger.info(
																		"Since Email Sent. Please Save attempt histroy.");
																/*
																 * Logic for Attempt Save
																 */
																/*
																 * Tagging attempt - American Express Services India
																 * Limited
																 */
//															attemptHistory = new AttemptHistory();
//															attemptHistory.setAttemptStatusid((long) 10);// (10,' Email-Sent','Valid',3,'2020-02-26 13:28:06'),
//															attemptHistory.setAttemptDescription(
//																	"Name not disclosed, Official from Human Resource Department advised all verifications are handled via email. We have complied with this request.");
//															attemptHistory.setName(
//																	spocEmailConfigList.get(0).getSourceName());
//															attemptHistory.setJobTitle(
//																	spocEmailConfigList.get(0).getHRDesignation());
//															int followUpDays = Integer.parseInt(
//																	spocEmailConfigList.get(0).getFollowUpDate1());
//															attemptHistory.setFollowupDate(
//																	(Utility.addDaysSkippingWeekends(new Date(),
//																			followUpDays)).toString());
//															attemptHistory.setEmailAddress(
//																	spocEmailConfigList.get(0).getToEmailID());
//
//															int expectedClosureDate = Integer
//																	.parseInt(spocEmailConfigList.get(0)
//																			.getExpectedClosureDate());
//															attemptHistory.setClosureExpectedDate(
//																	(Utility.addDaysSkippingWeekends(new Date(),
//																			expectedClosureDate)).toString());
//															Date contactDate = new Date();
//															attemptHistory.setContactDate(contactDate.toString());

																/*
																 * Logic for Saving information in Database tables
																 * Client Specific Tables and Client Specific Record
																 * details
																 */
//															logger.info("Make Rest call to Save");
//
//															CaseSpecificRecordDetailPOJO caseSpecificRecordDetail = new CaseSpecificRecordDetailPOJO();
//															caseSpecificRecordDetail.setComponentName(
//																	components.get(k).getComponentname());
//															caseSpecificRecordDetail
//																	.setProduct(components.get(k).getPRODUCT());
//															caseSpecificRecordDetail
//																	.setComponentRecordField(records.get(l).toString());
//															caseSpecificRecordDetail.setInstructionCheckId(checkId);
//															caseSpecificRecordDetail.setCaseSpecificId(caseSpecificInfoId);
//															caseSpecificRecordDetail.setCaseSpecificRecordStatus("Email Sent");
//															caseSpecificRecordDetailPOJOs.add(caseSpecificRecordDetail);

																/*
																 * Make Rest call and save this Info DB
																 */
//															JsonNode caseSpecificRecordDetailNode = mapper.convertValue(caseSpecificRecordDetail,JsonNode.class);
//															String caseSpecificRecordDetailStr=null;
//															if(caseSpecificRecordDetailNode!=null) {
//																caseSpecificRecordDetailStr=spocApiService.sendDataToCaseSpecificRecord(caseSpecificRecordDetailNode.toString());
//															}
//															CaseSpecificRecordDetailPOJO caseSpecificRecordDetail1 = new CaseSpecificRecordDetailPOJO();
//															if(caseSpecificRecordDetailStr!=null) {
//																JsonNode caseSpecificRecordNode= mapper.readTree(caseSpecificRecordDetailStr);
//																caseSpecificRecordDetail1 = mapper.treeToValue(caseSpecificRecordNode, CaseSpecificRecordDetailPOJO.class);
//															}
//															Long caseSpecificRecordId=null;//Treat as request ID for other like attempt history
//															if(caseSpecificRecordDetail1!=null) {
//																caseSpecificRecordId=caseSpecificRecordDetail1.getCaseSpecificDetailId();
//															}
//
//															attemptHistory.setRequestid(caseSpecificRecordId);
//															attemptHistory.setCheckid(checkId);
//															attemptHistory.setFollowupId((long) 1);
//															saveAttempt(mapper, attemptHistory);
//
//															VerificationEventStatusPOJO verificationEventStatusPOJO = new VerificationEventStatusPOJO();
//															verificationEventStatusPOJO.setCheckId(checkId);
//															verificationEventStatusPOJO.setEventName("Spoc-Router");
//															verificationEventStatusPOJO.setEventType("auto");
//															verificationEventStatusPOJO.setCaseNo(componentScoping.get(j).getCASE_NUMBER()); 
//															verificationEventStatusPOJO.setUser("System");
//															verificationEventStatusPOJO.setStatus("Email Sent");
//															verificationEventStatusPOJO.setRequestId(caseSpecificRecordId);
//
//															String verificationEventStatusStr = mapper
//																	.writeValueAsString(verificationEventStatusPOJO);
//															spocApiService.sendDataToVerificationEventStatus(
//																	verifictionEventStatusUrl,
//																	verificationEventStatusStr);

																logger.info("Value of Instruction Check ID : {}",
																		checkId);
															} else {
																logger.info("Instruction Check Id is Null");
															}

														} else {
															logger.info(
																	"Spoc Template Not Found for template number : {}",
																	spocTemplateNumberResponse);
														}
													} else {
														logger.info("Spoc Template Not Found");
													}
													logger.info("Send Information With Mail Body");
												} else {
													logger.info("Generate Excel and Send by Mail");
													logger.info("Generate Excel");

													String spocExcelTemplateSearchString = "{\"contactCardName\":"
															+ "\"" + spocEmailConfigList.get(0).getContactCardName()
															+ "\"" + ",\"componentName\":" + "\""
															+ components.get(k).getComponentname() + "\"" + "}";
													logger.info("Spoc template Search String : {}",
															spocExcelTemplateSearchString);
													spocTemplateResponse = spocApiService
															.sendDataToExcelTemplate(spocExcelTemplateSearchString);
													logger.info("SPOC Email Template Response : {}",
															spocTemplateResponse);
													List<SPOCExcelTemplatePOJO> spocEmailTemplateList = mapper
															.readValue(spocTemplateResponse,
																	new TypeReference<List<SPOCExcelTemplatePOJO>>() {
																	});
													spocTemplateResponse = mapper
															.writeValueAsString(spocEmailTemplateList);
													if (spocTemplateResponse != null
															&& !spocTemplateResponse.equals("[]")) {
														logger.info("Send Information With Mail Body : {}",
																spocEmailTemplateList.get(0).getTemplateHeaders());

														ObjectMapper oMapper = new ObjectMapper();
														// object -> Map
														TemplateHeaders[] map = oMapper.convertValue(
																spocEmailTemplateList.get(0).getTemplateHeaders(),
																TemplateHeaders[].class);
														List<TemplateHeaders> th = new ArrayList<>(Arrays.asList(map));
														logger.debug("{}", th.size());
														logger.debug("{} {}", map[0], th.get(0));

														/*
														 * try { JExcelUtility.writeJExcel(th); } catch (IOException |
														 * WriteException e) { logger.error(e.getMessage(), e);
														 * e.printStackTrace(); }
														 */
														logger.info("Sent as Attachment");

														String checkId = records.get(l).get("checkId") != null
																? records.get(l).get("checkId").asText()
																: "";

														if (StringUtils.isNotEmpty(checkId)) {

															if (StringUtils.containsIgnoreCase(subjectLine,
																	"<<First Name>>")) {
																subjectLine = StringUtils.replaceIgnoreCase(subjectLine,
																		"<<First Name>>", firstName);
															}
															if (StringUtils.containsIgnoreCase(subjectLine,
																	"<<Last Name>>")) {
																subjectLine = StringUtils.replaceIgnoreCase(subjectLine,
																		"<<Last Name>>", lastName);
															}
															if (StringUtils.containsIgnoreCase(subjectLine,
																	"<<Check ID>>")) {
																subjectLine = StringUtils.replaceIgnoreCase(subjectLine,
																		"<<Check ID>>", checkId);
															}

															if (StringUtils.containsIgnoreCase(subjectLine,
																	"<<Employee ID>>")) {
																subjectLine = StringUtils.replaceIgnoreCase(subjectLine,
																		"<<Employee ID>>", employeeID);
															}
															// Same Thing will do for attachemnt
															if (StringUtils.containsIgnoreCase(attachmentName,
																	"<<First Name>>")) {
																attachmentName = StringUtils.replaceIgnoreCase(
																		attachmentName, "<<First Name>>", firstName);
															}
															if (StringUtils.containsIgnoreCase(attachmentName,
																	"<<Last Name>>")) {
																attachmentName = StringUtils.replaceIgnoreCase(
																		attachmentName, "<<Last Name>>", lastName);
															}
															if (StringUtils.containsIgnoreCase(attachmentName,
																	"<<Check ID>>")) {
																attachmentName = StringUtils.replaceIgnoreCase(
																		attachmentName, "<<Check ID>>", checkId);
															}
															if (StringUtils.containsIgnoreCase(attachmentName,
																	"<<Employee ID>>")) {
																attachmentName = StringUtils.replaceIgnoreCase(
																		attachmentName, "<<Employee ID>>", employeeID);
															}
															// Before Sending Email Download the MRL Documents
															List<File> fileList = downloadMRLDocs(mapper,
																	componentScoping, j, attachmentName, checkId);
															// Merge File
															File file = mergeFile(fileList, attachmentName);

															sendEmailWithAttachment(fromEmailID, subjectLine, toEmailID,
																	ccEmailID, null, attachmentName, file);
															// This is commented for time being
															sendDataToL3F1(componentScoping.get(j), components.get(k),
																	records.get(l), mapper, checkId, toEmailID,
																	spocEmailConfigList.get(0));
															/*
															 * Logic for Attempt Save
															 */
//														attemptHistory = new AttemptHistory();
//														attemptHistory.setAttemptStatusid((long) 10);// (10, ' Email -
															// Sent',
															// 'Valid',
															// 3,
															// '2020-02-26
															// 13:28:06'),
//														attemptHistory.setAttemptDescription(
//																"Name not disclosed, Official from Human Resource Department advised all verifications are handled via email. We have complied with this request.");
//														attemptHistory
//																.setName(spocEmailConfigList.get(0).getSourceName());
//														attemptHistory.setJobTitle(
//																spocEmailConfigList.get(0).getHRDesignation());
//														int followUpDays = Integer.parseInt(
//																spocEmailConfigList.get(0).getFollowUpDate1());
//														attemptHistory.setFollowupDate(
//																(Utility.addDaysSkippingWeekends(new Date(),
//																		followUpDays)).toString());
//														attemptHistory.setEmailAddress(
//																spocEmailConfigList.get(0).getToEmailID());
//														int expectedClosureDate = Integer.parseInt(
//																spocEmailConfigList.get(0).getExpectedClosureDate());
//														attemptHistory.setClosureExpectedDate(
//																(Utility.addDaysSkippingWeekends(new Date(),
//																		expectedClosureDate)).toString());
//														Date contactDate = new Date();
//														attemptHistory.setContactDate(contactDate.toString());
//														attemptHistory.setCheckid(checkId);

															/*
															 * Logic for Saving information in Database tables Client
															 * Specific Tables and Client Specific Record details
															 */
//														logger.info("Make Rest call to Save");
//
//														CaseSpecificRecordDetailPOJO caseSpecificRecordDetail = new CaseSpecificRecordDetailPOJO();
//														caseSpecificRecordDetail
//																.setComponentName(components.get(k).getComponentname());
//														caseSpecificRecordDetail
//																.setProduct(components.get(k).getPRODUCT());
//														caseSpecificRecordDetail
//																.setComponentRecordField(records.get(l).toString());
//														caseSpecificRecordDetail.setInstructionCheckId(checkId);
//														caseSpecificRecordDetail.setCaseSpecificId(caseSpecificInfoId);
//														caseSpecificRecordDetail.setCaseSpecificRecordStatus("Email Sent");
//														caseSpecificRecordDetailPOJOs.add(caseSpecificRecordDetail);

															/*
															 * Make Rest call and save this Info DB
															 */
//														JsonNode caseSpecificRecordDetailNode = mapper.convertValue(caseSpecificRecordDetail,JsonNode.class);
//														String caseSpecificRecordDetailStr=null;
//														if(caseSpecificRecordDetailNode!=null) {
//															caseSpecificRecordDetailStr=spocApiService.sendDataToCaseSpecificRecord(caseSpecificRecordDetailNode.toString());
//														}
//														CaseSpecificRecordDetailPOJO caseSpecificRecordDetail1 = new CaseSpecificRecordDetailPOJO();
//														if(caseSpecificRecordDetailStr!=null) {
//															JsonNode caseSpecificRecordNode= mapper.readTree(caseSpecificRecordDetailStr);
//															caseSpecificRecordDetail1 = mapper.treeToValue(caseSpecificRecordNode, CaseSpecificRecordDetailPOJO.class);
//														}
//														Long caseSpecificRecordId=null;//Treat as request ID for other like attempt history
//														if(caseSpecificRecordDetail1!=null) {
//															caseSpecificRecordId=caseSpecificRecordDetail1.getCaseSpecificDetailId();
//														}
//
//														attemptHistory.setRequestid(caseSpecificRecordId);
//														attemptHistory.setFollowupId((long) 1);
//
//														attemptHistory.setCheckid(checkId);
//														saveAttempt(mapper, attemptHistory);
//
//														VerificationEventStatusPOJO verificationEventStatusPOJO = new VerificationEventStatusPOJO();
//														verificationEventStatusPOJO.setCheckId(checkId);
//														verificationEventStatusPOJO.setEventName("Spoc-Router");
//														verificationEventStatusPOJO.setEventType("auto");
//														verificationEventStatusPOJO.setCaseNo(componentScoping.get(j).getCASE_NUMBER()); 
//														verificationEventStatusPOJO.setUser("System");
//														verificationEventStatusPOJO.setStatus("Email Sent");
//														verificationEventStatusPOJO.setRequestId(caseSpecificRecordId);
//
//														String verificationEventStatusStr = mapper
//																.writeValueAsString(verificationEventStatusPOJO);
//														spocApiService.sendDataToVerificationEventStatus(
//																verifictionEventStatusUrl, verificationEventStatusStr);

															logger.info("Value of Instruction Check ID : {}", checkId);
														} else {
															logger.info("Instruction Check Id is Null");
														}

													}
												}
												logger.info("Found Result In Spoc Email Config");
												logger.info("Sent Mail");
											} else {
												logger.debug("Value of SPOC Email Response : {}", spocEmailResponse);
												logger.info("Not Found Result In Spoc Email Config");
												logger.info("Mail not sent");
											}
											result = "{\"SPOC Result\":\"SPOC records found\"}";
										} else {
											logger.info("Spoc Response : {}", spocResponse);
											logger.debug("Not Found Result In Spoc");
											logger.info("Go to Next Engine");
											result = "{\"SPOC Result\":\"SPOC records not found\"}";
										}
										JsonNode fieldJsonNode = mapper.readTree(spocResponse);
										JsonNode resultJsonNode = mapper.readTree(result);

										ArrayNode ruleResultJsonNode = (ArrayNode) records.get(l).get("ruleResult");
										ObjectNode spocResult = mapper.createObjectNode();
										spocResult.put("engine", "spocResult");
										spocResult.put("success", true);
										spocResult.put("message", "SUCCEEDED");
										spocResult.put("status", 200);
										spocResult.set("fields", fieldJsonNode);
										spocResult.set("result", resultJsonNode);
										ruleResultJsonNode.add(spocResult);
										((ObjectNode) records.get(l)).set("ruleResult", ruleResultJsonNode);

									} else {
										logger.info("Include not found");
									}
								} else {
									logger.info("MI Value is not No");
								}
							}
						}
					}
//					if (!caseSpecificRecordDetailPOJOs.isEmpty()) {
//						caseSpecificInfo.setCaseSpecificRecordDetail(caseSpecificRecordDetailPOJOs);
//
//						JsonNode caseSpecificInfoNode = mapper.convertValue(caseSpecificInfo, JsonNode.class);
//						String caseSpecificInfoStr = caseSpecificInfoNode.toString();
//						spocApiService.sendDataToCaseSpecificInfo(caseSpecificInfoStr);
//					}
				}
				spocResponse = mapper.writeValueAsString(spocReq);
				dataObj = mapper.readTree(spocResponse);
				ObjectNode metricsObj = mapper.createObjectNode();

				LocalDateTime endTime = LocalDateTime.now();
				metricsObj.put("startTime", startTime.toString());
				metricsObj.put("endTime", endTime.toString());
				metricsObj.put("timeInMillis", "0");
				metricsObj.put("timeInSeconds", "0");
				metricsObj.put("statusCode", "OK");
				((ObjectNode) dataObj.get("data").get(0)).set("metrics", metricsObj);

				ObjectNode logs = mapper.createObjectNode();
				logs.put("field1", "No LOG");
				((ObjectNode) dataObj.get("data").get(0)).set("logs", logs);
			}
			// Adding Status in Meta Data Also
			ObjectNode statusObj = mapper.createObjectNode();
			statusObj.put("success", true);
			statusObj.put("message", "Executon done successfullly");
			statusObj.put("statusCode", "200");
			((ObjectNode) dataObj.get("metadata")).set("status", statusObj);

			spocResponse = mapper.writeValueAsString(dataObj);
			String returnStr = spocResponse;
			// returnStr =spocResponse.replace("taskSpecs", "result")
			// String returnStr = stellarResponse.toString()
			// String returnStr = SPOCReqString
			logger.debug("Response :\n {}", returnStr);
			if (asyncStatus)
				callback(returnStr);
			return returnStr;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			if (asyncStatus)
				callback(e.getMessage());
			return e.getMessage();
		}
	}

	private List<File> downloadMRLDocs(ObjectMapper mapper, List<ComponentScoping> componentScoping, int j,
			String attachmentName, String checkId) throws MalformedURLException {
		String associateDocsResponse = spocApiService.sendDataToget(associateDocsUrl,
				componentScoping.get(j).getCASE_NUMBER());
		logger.info("Value of AssociateDocsResponse" + associateDocsResponse);
		logger.info("Value of CheckId" + checkId);
		// Parse associateDocs
		ArrayNode filePathArrNode = mapper.createArrayNode();
		JsonNode associateDocsResponseNode = null;
		try {
			associateDocsResponseNode = mapper.readTree(associateDocsResponse);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e.getMessage());
		}
		if (associateDocsResponseNode != null && associateDocsResponseNode.has("response")) {
			// Look into response for Check ID
			JsonNode responseNode = associateDocsResponseNode.get("response");
			// Look for associateDocs
			if (responseNode != null && responseNode.has("associateDocs")) {
				JsonNode associateDocsNode = null;
				if (responseNode.get("associateDocs").isArray()) {
					ArrayNode associateDocsArrNode = (ArrayNode) responseNode.get("associateDocs");
					for (int y = 0; y < associateDocsArrNode.size(); y++) {
						associateDocsNode = associateDocsArrNode.get(y);
						if (associateDocsNode != null && associateDocsNode.has("docsData")) {
							if (associateDocsNode.get("docsData").isArray()) {
								ArrayNode docsArrDataNode = (ArrayNode) associateDocsNode.get("docsData");
								for (int x = 0; x < docsArrDataNode.size(); x++) {
									JsonNode docsDataNode = docsArrDataNode.get(x);
									if (docsDataNode != null && docsDataNode.has("checkIds")) {
										if (docsDataNode.get("checkIds").isArray()) {
											JsonNode checkIdsNode = docsDataNode.get("checkIds").get(0);
											if (checkIdsNode != null && checkIdsNode.has("checkId")) {
												// Matched CheckID and take file name
												if (StringUtils.equals(checkId, checkIdsNode.get("checkId").asText())) {
													logger.info("CheckId Matched! Take filePath"
															+ docsDataNode.get("filePath"));
													filePathArrNode.add(docsDataNode.get("filePath"));
												} else {
													logger.info("CheckId is not Matched.");
												}
											} else {
												logger.info("Value of CheckIdsNode is null "
														+ "or having no checkId field");
											}
										} else {
											logger.info("CheckIds is not Array" + docsDataNode.get("checkIds"));
										}
									}
								}
							} else {
								logger.info("Docs Data is Not Array");
							}
						}
					}
				}
			}
		}
		logger.info("Value of filePathArrNode" + filePathArrNode);
		// Download File and attached with mail
		List<File> fileList = new ArrayList<>();
		for (int x = 0; x < filePathArrNode.size(); x++) {
			File filename = new File(localFileLocation + attachmentName + "-" + x + ".pdf");
			URL newURL = new URL(docUrl + filePathArrNode.get(x).asText());
			logger.info("Filename:" + filename + "\n" + "newURL" + newURL);
			try {
				FileUtils.copyURLToFile(newURL, filename);
			} catch (Exception e) {
				e.printStackTrace();
			}
			fileList.add(filename);
		}
		return fileList;
	}

	private void saveAttempt(ObjectMapper mapper, AttemptHistory attemptHistory) {

		try {
			JsonNode attemptHistoryJsonNode = mapper.valueToTree(attemptHistory);
			logger.info("Value of attemptHistory bean to Json : {}", attemptHistoryJsonNode);
			String attemptHistoryStr = mapper.writeValueAsString(attemptHistoryJsonNode);
			logger.info("Value of attemptHistory Json to String : {}", attemptHistoryStr);

			String attemptHistoryResponse = null;
			AttemptHistory attemptHistoryNew = new AttemptHistory();
			try {
				attemptHistoryResponse = spocApiService.sendDataToAttemptHistory(attemptHistoryStr);
				logger.info("Attempt History Response : {}", attemptHistoryResponse);
				attemptHistoryNew = mapper.readValue(attemptHistoryResponse, new TypeReference<AttemptHistory>() {
				});
				if (attemptHistoryNew.getAttemptid() != null) {
					logger.info("Attempt saved sucessfully. : {}", attemptHistoryNew.getAttemptid());

					AttemptStatusData attemptStatusData = new AttemptStatusData();
					attemptStatusData.setAttemptId(attemptHistoryNew.getAttemptid());
					attemptStatusData.setDepositionId((long) 13);
					attemptStatusData.setEndstatusId((long) 1);
					attemptStatusData.setModeId((long) 45);

					JsonNode attemptStatusDataJsonNode = mapper.valueToTree(attemptStatusData);
					logger.info("Value of attemptStatusData bean to Json : {}", attemptStatusDataJsonNode);
					String attemptStatusDataStr = mapper.writeValueAsString(attemptStatusDataJsonNode);
					logger.info("Value of attemptStatusData Json to String : {}", attemptStatusDataStr);

					String attemptStatusDataResponse = null;
					AttemptStatusData attemptStatusDataNew = new AttemptStatusData();

					try {
						attemptStatusDataResponse = spocApiService.sendDataToAttempt(spocAttemptStatusDataRestUrl,
								attemptStatusDataStr);
						logger.info("Attempt status data Response : {}", attemptStatusDataResponse);
						attemptStatusDataNew = mapper.readValue(attemptStatusDataResponse,
								new TypeReference<AttemptStatusData>() {
								});
						if (attemptStatusDataNew.getStatusId() != null) {
							logger.info("Attempt status data saved sucessfully. : {}",
									attemptStatusDataNew.getStatusId());
						} else {
							logger.info("Attempt status data not saved.");
						}
					} catch (Exception e) {
						logger.info("Exception in calling save attempt status data : {}", e.getMessage());
						e.printStackTrace();
					}

				} else {
					logger.info("Attempt history not saved.");
				}
			} catch (Exception e) {
				logger.error("Exception in calling save attempt history : {}", e.getMessage());
				e.printStackTrace();
			}
		} catch (IllegalArgumentException | JsonProcessingException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}

	}

	private void callback(String postStr) {
		try {

			logger.debug("postStr :\n {}", postStr);
			URL url = new URL(env.getProperty("cbvutvi4v.router.callback.url"));
			logger.debug("Using callback URL :\n {}", url);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");

			// Creating the ObjectMapper object
			ObjectMapper mapper = new ObjectMapper();
			// Converting the JSONString to Object
			SPOCReq suspectReq = mapper.readValue(postStr, SPOCReq.class);
			String authToken = suspectReq.getMetadata().getRequestAuthToken();

			logger.debug("Auth Token :\n {}", authToken);
			con.setRequestProperty("tokenId", authToken);

			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			os.write(postStr.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();
			logger.debug("Callback POST Response Code : {} : {}", responseCode, con.getResponseMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/*
	 * void sendEmail(String fromEmailID, String subjectLine, String toEmailID,
	 * String ccEmailID, String emailTemplate,List<File> fileList) {
	 */
	void sendEmail(String fromEmailID, String subjectLine, String toEmailID, String ccEmailID, String emailTemplate,
			File file) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			List<String> lstTo = new ArrayList<String>();
			lstTo.add("aashutosh.kumar@gridinfocom.com");
			lstTo.add("aash202@gmail.com");
			lstTo.add("Archana.Bala@fadv.com");
			lstTo.add("Anju.Parameshwaran@fadv.com");
			lstTo.add("Diwakar.Narahari@fadv.com");
			if (CollectionUtils.isNotEmpty(lstTo)) {
				helper.setTo(lstTo.stream().toArray(String[]::new));
			}

			// helper.setReplyTo(SENDER_ADDRESS)
			// helper.setFrom("aash202@gmail.com");
			// helper.setFrom("emp.verification@fadv.com");
			helper.setFrom(fromUserName);
			helper.setSubject(subjectLine);
			helper.setText(emailTemplate, true);
			/* for (File file : fileList) { */
			FileSystemResource fr = new FileSystemResource(file);
			helper.addAttachment(file.getName(), fr);
			/* } */
			mailSender.send(message);

			try {
				TimeUnit.SECONDS.sleep(10);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}

			/* for (File file : fileList) { */
			if (file.delete()) {
				logger.info("File deleted successfully");
			} else {
				logger.info("Failed to delete the file");
			} /* } */

		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : {}", e.getMessage());
		}

	}

	void sendEmailWithAttachment(String fromEmailID, String subjectLine, String toEmailID, String ccEmailID,
			String emailTemplate, String attachment, File file) throws MessagingException, IOException {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			List<String> lstTo = new ArrayList<String>();
			lstTo.add("aashutosh.kumar@gridinfocom.com");
			lstTo.add("aash202@gmail.com");
			lstTo.add("Archana.Bala@fadv.com");
			lstTo.add("Anju.Parameshwaran@fadv.com");
			lstTo.add("Diwakar.Narahari@fadv.com");
			if (CollectionUtils.isNotEmpty(lstTo)) {
				helper.setTo(lstTo.stream().toArray(String[]::new));
			}
			// helper.setReplyTo(SENDER_ADDRESS)
			helper.setFrom("aash202@gmail.com");
			helper.setSubject(subjectLine);
			helper.setText("<h1>Check attachment for excel!</h1>", true);

			File currDir = new File(".");
			String path = currDir.getAbsolutePath();
			String fileLocation = path.substring(0, path.length() - 1) + "tempj.xls";
			File myObj = new File(fileLocation);
			// helper.addAttachment("tempj.xls", myObj)
			// helper.addAttachment(attachMentName, new File(fileName))
			helper.addAttachment(attachment + ".xls", myObj);
			// Attached MRL Docs
			/* for (File file : fileList) { */
			FileSystemResource fr = new FileSystemResource(file);
			helper.addAttachment(file.getName(), fr);
			/* } */
			mailSender.send(message);

			try {
				TimeUnit.SECONDS.sleep(10);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
			if (myObj.delete()) {
				logger.info("File deleted successfully");
			} else {
				logger.info("Failed to delete the file");
			}
			// Delete MRL Docs File
			/* for (File file : fileList) { */
			if (file.delete()) {
				logger.info("File deleted successfully");
			} else {
				logger.info("Failed to delete the file");
			}
			/* } */
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : {}", e.getMessage());
		}

		/*
		 * MimeMessage msg = javaMailSender.createMimeMessage();
		 * 
		 * // true = multipart message MimeMessageHelper helper = new
		 * MimeMessageHelper(msg, true); helper.setTo("aash202@gmail.com");
		 * 
		 * //helper.setSubject("Testing from Spring Boot");
		 * helper.setSubject(subjectLine); // default = text/plain
		 * //helper.setText("Check attachment for image!");
		 * 
		 * // true = text/html helper.setText("<h1>Check attachment for excel!</h1>",
		 * true); //helper.addAttachment("tempj.xls", new
		 * ClassPathResource("tempj.xls")); File currDir = new File("."); String path =
		 * currDir.getAbsolutePath(); String fileLocation = path.substring(0,
		 * path.length() - 1) + "tempj.xls"; File myObj = new File(fileLocation);
		 * //helper.addAttachment("tempj.xls", myObj);
		 * helper.addAttachment(attachment+".xls", myObj); javaMailSender.send(msg); try
		 * { TimeUnit.SECONDS.sleep(10); } catch (InterruptedException ie) {
		 * Thread.currentThread().interrupt(); } if(myObj.delete()) {
		 * System.out.println("File deleted successfully"); } else {
		 * System.out.println("Failed to delete the file"); }
		 */
	}

	private File mergeFile(List<File> fileList, String attachementName) throws IOException {
		PDFMergerUtility ut = new PDFMergerUtility();
		for (File file : fileList) {
			ut.addSource(file);
		}
		ut.setDestinationFileName(localFileLocation + attachementName + ".pdf");
		ut.mergeDocuments();
		File filename = new File(localFileLocation + attachementName + ".pdf");

		for (File file : fileList) {
			if (file.delete()) {
				logger.info("File deleted successfully");
			} else {
				logger.info("Failed to delete the file");
			}
		}
		return filename;
	}

	private String sendDataToL3F1(ComponentScoping componentScopingNode, Component component, JsonNode recordNode,
			ObjectMapper mapper, String checkId, String toEmailID, SPOCEmailConfigPOJO spocEmailConfigPOJO)
			throws JsonMappingException, JsonProcessingException {
		/*
		 * Logic for Sending Verification Data to L3
		 */

		TaskSpecsPOJO taskSpecs = new TaskSpecsPOJO();
		CaseReferencePOJO caseReference = mapper.readValue(componentScopingNode.getCaseReference().toString(),
				CaseReferencePOJO.class);
		
		String response = apiService.sendDataToGet(holidayListUrl);
		response = response.replace("[", "").replace("]", "").replace("\"", "");
		
		String[] split = response.split(",");
		List<String> holidayList = Arrays.asList(split);
		holidayList.replaceAll(String::trim);
		
		logger.info("holidayList : {}", holidayList);

		int followUpDays = Integer.parseInt(spocEmailConfigPOJO.getFollowUpDate1());
		int expectedClosureDays = Integer.parseInt(spocEmailConfigPOJO.getExpectedClosureDate());

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
		Date expectedClosureDate = Utility.addDaysSkippingWeekends(new Date(), expectedClosureDays, holidayList);
		String expectedClosureDateStr = formatter.format(expectedClosureDate);

		Date followUpDate = Utility.addDaysSkippingWeekends(new Date(), followUpDays, holidayList);
		String followUpDateStr = formatter.format(followUpDate);

		caseReference.setCheckId(checkId);
		caseReference.setNgStatus("F2");
		caseReference.setNgStatusDescription("Followup 2");
		caseReference.setSbuName(componentScopingNode.getSBU_NAME());
		caseReference.setProductName(component.getPRODUCT());
		caseReference.setPackageName(componentScopingNode.getPackageName());
		caseReference.setComponentName(component.getComponentname());

		CheckVerificationPOJO checkVerification = new CheckVerificationPOJO();

		checkVerification.setCountry("India");
		checkVerification.setExecutiveSummaryComments("");
		checkVerification.setReportComments("");
		checkVerification.setResultCode("");
		checkVerification.setEmailId(toEmailID);
		checkVerification.setVerifierName("");
		checkVerification.setGeneralRemarks("");
		checkVerification.setModeOfVerification("Email ID-Official");
		checkVerification.setVerifiedDate("");
		checkVerification.setAction("verifyChecks");
		if (StringUtils.equalsAnyIgnoreCase(component.getComponentname(), "Database")) {
			checkVerification.setSubAction("databaseVerification");
		} else {
			checkVerification.setSubAction("verifyChecks");
		}
		checkVerification.setActionCode("");
		checkVerification.setComponentName(component.getComponentname());
		checkVerification.setAttempts("Internal");
		checkVerification.setDepartmentName("");
		checkVerification.setKeyWords("");
		checkVerification.setCost("");
		checkVerification.setIsThisAVerificationAttempt("Yes");
		checkVerification.setInternalNotes(
				"Name not disclosed, Official from Human Resource Department advised all verifications are handled via email. We have complied with this request.");
		checkVerification.setDateVerificationCompleted("");
		checkVerification.setDisposition("");
		checkVerification.setExpectedClosureDate(expectedClosureDateStr);
		checkVerification.setEndStatusOfTheVerification("Pending for Reply");
		checkVerification.setVerifierDesignation("Official");
		checkVerification.setFollowUpDateAndTimes(followUpDateStr);
		checkVerification.setVerifierNumber("");

		taskSpecs.setCaseReference(caseReference);
		taskSpecs.setCheckVerification(checkVerification);

		taskSpecs.setQuestionaire(new ArrayList<>());
		/*
		 * Make Questionnaire Object and Push in Questionnaire List
		 */
//		List<String> quetionRefIDList = new ArrayList<>();
//		quetionRefIDList.add("500110");
//		quetionRefIDList.add("807981");
//		quetionRefIDList.add("800054");
//		/*
//		 * Call for Questionnaire List
//		 */
//		String questionList = null;
//		List<QuestionPOJO> questionnairePOJOList = new ArrayList<>();
//		List<QuestionnairePOJO> questionnairePOJOList1 = new ArrayList<>();
//		try {
//			questionList = spocApiService.sendDataTogetCheckId(questionaireURL, checkId);
//			logger.info("Question List : {}", questionList);
//			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//			ObjectNode attemptQuestionnaireNode = mapper.createObjectNode();
//			try {
//				attemptQuestionnaireNode = (ObjectNode) mapper.readTree(questionList);
//			} catch (JsonProcessingException e) {
//				e.printStackTrace();
//			}
//			if (attemptQuestionnaireNode != null && attemptQuestionnaireNode.has("response")) {
//				JsonNode questionnaire = attemptQuestionnaireNode.get("response");
//				try {
//					questionnairePOJOList = mapper.readValue(questionnaire.toString(),
//							new TypeReference<List<QuestionPOJO>>() {
//							});
//				} catch (JsonProcessingException e) {
//					logger.error(e.getMessage(), e);
//					e.printStackTrace();
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		for (int i = 0; i < questionnairePOJOList.size(); i++) {
//			if (quetionRefIDList.contains(questionnairePOJOList.get(i).getGlobalQuestionId())) {
//				QuestionnairePOJO questionnairePOJO1 = new QuestionnairePOJO();
//				questionnairePOJO1.setCaseQuestionRefID(questionnairePOJOList.get(i).getGlobalQuestionId());
//				questionnairePOJO1.setAnswer(questionnairePOJOList.get(i).getAnswere());
//				questionnairePOJO1.setQuestion(questionnairePOJOList.get(i).getQuestionName());
//				questionnairePOJO1.setReportData("");
//				questionnairePOJO1.setStatus("");
//				questionnairePOJO1.setVerifiedData("");
//				questionnairePOJOList1.add(questionnairePOJO1);
//			}
//		}
//		taskSpecs.setQuestionaire(questionnairePOJOList1);

		FileUploadPOJO fileUpload = new FileUploadPOJO();
		fileUpload.setVerificationReplyDocument(new ArrayList<>());
		taskSpecs.setFileUpload(fileUpload);

		String getVerificationStatusforL3 = mapper.writeValueAsString(taskSpecs);
		String sendInfol3VeriStatus = null;

		try {
			logger.info("getVerificationStatusforL3Node : {} ", getVerificationStatusforL3);
			sendInfol3VeriStatus = spocApiService.sendDataToRest(verificationStatusL3Url, getVerificationStatusforL3,
					null);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return sendInfol3VeriStatus;
	}
}