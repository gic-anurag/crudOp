package com.gic.fadv.cbvutvi4v.controller;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.cbvutvi4v.model.AttemptHistory;
import com.gic.fadv.cbvutvi4v.model.AttemptStatusData;
import com.gic.fadv.cbvutvi4v.model.CBVUTVI4V;
import com.gic.fadv.cbvutvi4v.model.CBVUTVI4VReq;
import com.gic.fadv.cbvutvi4v.model.CBVUTVI4VRuleConfig;
import com.gic.fadv.cbvutvi4v.model.Component;
import com.gic.fadv.cbvutvi4v.model.ComponentScoping;
import com.gic.fadv.cbvutvi4v.model.VerificationSLA;
import com.gic.fadv.cbvutvi4v.pojo.CaseReferencePOJO;
import com.gic.fadv.cbvutvi4v.pojo.CheckVerificationPOJO;
import com.gic.fadv.cbvutvi4v.pojo.FileUploadPOJO;
import com.gic.fadv.cbvutvi4v.pojo.QuestionPOJO;
import com.gic.fadv.cbvutvi4v.pojo.QuestionnairePOJO;
import com.gic.fadv.cbvutvi4v.pojo.TaskSpecsPOJO;
import com.gic.fadv.cbvutvi4v.repository.CBVUTVI4VRuleConfigRepository;
import com.gic.fadv.cbvutvi4v.service.CBVUTVI4VApiService;
import com.gic.fadv.cbvutvi4v.service.CBVUTVI4VService;
import com.google.gson.JsonParser;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
@Api(value = "Cross Reference")
public class CBVUTVI4VRouterController {

	@Autowired
	private CBVUTVI4VApiService cbvutvi4vApiService;
	@Autowired
	private Environment env;
	@Autowired
	private CBVUTVI4VRuleConfigRepository cbvutvi4vRuleConfigRepository;
	@Autowired
	private RestTemplateBuilder restTemplateBuilder;

	@Autowired
	private CBVUTVI4VService cbvutvi4vService;

	@Value("${cbvutv.verifictioneventstatus.rest.url}")
	private String verifictioneventstatusRestUrl;

	@Value("${spocattemptstatusdata.rest.url}")
	private String spocAttemptStatusDataRestUrl;

	@Value("${verification.status.url.l3}")
	private String verificationStatusL3Url;

	@Value("${questionaire.list.l3.url}")
	private String questionaireURL;

	private static final Logger logger = LoggerFactory.getLogger(CBVUTVI4VRouterController.class);

	@ApiOperation(value = "This service is used to process Records at CBV-UTV-I4V router and return the result to workflow router ", response = List.class)
	@PostMapping(path = "/async/cbvutvi4vrouter", consumes = "application/json", produces = "application/json")
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

	@ApiOperation(value = "This service is used to process Records at CBV-UTV-I4V router and return the result to workflow router ", response = List.class)
	@PostMapping(path = "/cbvutvi4vrouter", consumes = "application/json", produces = "application/json")
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

	@ApiOperation(value = "This service is used to process Records at CBV-UTV-I4V router and return the result to workflow router ", response = List.class)
	@PostMapping(path = "/cbvutvrouter", consumes = "application/json", produces = "application/json")
	public ObjectNode doRecordProcess(@RequestBody JsonNode requestBody) {
		logger.info("Got Request :\n {}", requestBody);
		ObjectNode response = cbvutvi4vService.processRequestBody(requestBody);
		logger.info("Record request Processed");
		return response;
	}

	private String processRequest(String inStr, boolean asyncStatus) {
		try {
			// Creating the ObjectMapper object
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			// Converting the JSONString to Object
			CBVUTVI4VReq cbvutvReq = mapper.readValue(inStr, CBVUTVI4VReq.class);

			String mrlReqString = "";
			String cbvResponse = "";

			for (int i = 0; i < cbvutvReq.getData().size(); i++) {
				List<ComponentScoping> componentScoping = cbvutvReq.getData().get(i).getTaskSpecs()
						.getComponentScoping();
				for (int j = 0; j < componentScoping.size(); j++) {
					ComponentScoping componentScopingNode = componentScoping.get(j);
					List<Component> components = componentScopingNode.getComponents();

//					CaseSpecificInfoPOJO caseSpecificInfo = new CaseSpecificInfoPOJO();
//					caseSpecificInfo.setCandidateName(componentScopingNode.getCandidate_Name());
//					caseSpecificInfo.setCaseDetails(componentScopingNode.getCaseDetails().toString());
//					caseSpecificInfo.setCaseMoreInfo(componentScopingNode.getCaseMoreInfo().toString());
//					caseSpecificInfo.setCaseReference(componentScopingNode.getCaseReference().toString());
//					caseSpecificInfo.setCaseRefNumber(componentScopingNode.getCASE_REF_NUMBER());
//					caseSpecificInfo.setCaseNumber(componentScopingNode.getCASE_NUMBER());
//					caseSpecificInfo.setClientCode(componentScopingNode.getCLIENT_CODE());
//					caseSpecificInfo.setClientName(componentScopingNode.getCLIENT_NAME());
//					caseSpecificInfo.setSbuName(componentScopingNode.getSBU_NAME());
//					caseSpecificInfo.setPackageName(componentScopingNode.getPackageName());
//					caseSpecificInfo.setClientSpecificFields(componentScopingNode.getClientSpecificFields().toString());
					/*
					 * Insert Info in DB
					 */
//					JsonNode caseSpecificInfoNode = mapper.convertValue(caseSpecificInfo, JsonNode.class);
//					String caseSpecificInfoStr = caseSpecificInfoNode.toString();
//					String caseSpecificResponse = cbvutvi4vApiService.sendDataToCaseSpecificInfo(caseSpecificInfoStr);
//					CaseSpecificInfoPOJO caseSpecificInfo1 = new CaseSpecificInfoPOJO();
//					if (caseSpecificResponse != null) {
//						JsonNode caseSpecificResponseNode = mapper.readTree(caseSpecificResponse);
//						caseSpecificInfo1 = mapper.treeToValue(caseSpecificResponseNode, CaseSpecificInfoPOJO.class);
//					}
//					Long caseSpecificInfoId = null;
//					if (caseSpecificInfo1 != null) {
//						caseSpecificInfoId = caseSpecificInfo1.getCaseSpecificId();
//					}
//					List<CaseSpecificRecordDetailPOJO> caseSpecificRecordDetailPOJOs = new ArrayList<CaseSpecificRecordDetailPOJO>();

					for (int k = 0; k < components.size(); k++) {
						List<JsonNode> records = components.get(k).getRecords();
						if (!records.isEmpty()) {
							for (int l = 0; l < records.size(); l++) {
								ArrayNode resultArray;
								if (records.get(l).get("ruleResult").isArray()) {
									resultArray = (ArrayNode) records.get(l).get("ruleResult").get(0).get("result");
								} else {
									resultArray = (ArrayNode) records.get(l).get("ruleResult").get("result");
								}
								if (resultArray.get(0).toString().contains("Include")) {
									logger.info("Send to CBV-UTV-I4V Process/Service");
									logger.info("Look for Employment Component only");

									String result = "";
									if (components.get(k).getComponentname().equals("Employment")) {
										ArrayNode ruleResultJsonNode = (ArrayNode) records.get(l).get("ruleResult");
										ObjectNode cbvutvi4vResult = mapper.createObjectNode();
										cbvutvi4vResult.put("engine", "cbvutvi4vResult");
										cbvutvi4vResult.put("success", true);
										cbvutvi4vResult.put("message", "SUCCEEDED");
										cbvutvi4vResult.put("status", 200);
										ruleResultJsonNode.add(cbvutvi4vResult);
										ArrayNode modules = mapper.createArrayNode();
										String cbvSlaSearchString = "{\"clientCode\":\""
												+ componentScoping.get(j).getCLIENT_CODE() + "\"}";
										logger.info("Value of Sla Search String : {}", cbvSlaSearchString);
										String cbvVerificationSLAResponse = cbvutvi4vApiService
												.LookUpDataAtVerifcationSLA(cbvSlaSearchString);
										logger.info("Value of verification Response : {}", cbvVerificationSLAResponse);
										if (cbvVerificationSLAResponse != null
												&& !cbvVerificationSLAResponse.equals("[]")) {
											List<VerificationSLA> verificationSLA = mapper.readValue(
													cbvVerificationSLAResponse,
													new TypeReference<List<VerificationSLA>>() {
													});
											/* Code related to verification SLA will go here */
											logger.info("Value of HTS : {}", verificationSLA.get(0).getHTS());
											for (int m = 0; m < verificationSLA.size(); m++) {
												if (verificationSLA.get(0).getHTS().equalsIgnoreCase("No")) {
													logger.info("Got verification SLA NO");
													String cbvSearchString = "";
													JsonNode thirdAkaName = records.get(l)
															.get("Third Party or Agency Name and Address");
													if (thirdAkaName != null && !thirdAkaName.isNull()
															&& thirdAkaName.asText().trim().length() > 1) {

														cbvSearchString = "{\"companyAkaName\":"
																+ records.get(l)
																		.get("Third Party or Agency Name and Address")
																+ ",\"productName\":" + "\""
																+ components.get(k).getPRODUCT() + "\"" + "}";

													} else {
														cbvSearchString = "{\"companyAkaName\":"
																+ records.get(l).get("Company Aka Name")
																+ ",\"productName\":" + "\""
																+ components.get(k).getPRODUCT() + "\"" + "}";
													}
													logger.info("Value of CBV-UTV-I4V Search String : {}",
															cbvSearchString);
													cbvResponse = cbvutvi4vApiService
															.LookUpDataAtCbvUtvI4vRest(cbvSearchString);
													logger.info("Value of cbvResponse : {}", cbvResponse);
													List<CBVUTVI4V> cbvutvi4v = mapper.readValue(cbvResponse,
															new TypeReference<List<CBVUTVI4V>>() {
															});
													if (cbvResponse != null && !cbvResponse.equals("[]")) {
														logger.info("Value of Flag : {}", cbvutvi4v.get(0).getFlag());

														// Adding Results as JSON Elements
														JsonNode fieldJsonNode = mapper.valueToTree(verificationSLA);
														JsonNode fieldJsonNode1 = mapper.valueToTree(cbvutvi4v);
														result = "{\"CBVUTVI4V Result\":\"CBVUTVI4V records found\"}";
														JsonNode resultJsonNode = mapper.readTree(result);

														ObjectNode cbvutvi4vLookup = mapper.createObjectNode();
														cbvutvi4vLookup.put("module", "cbvutvi4vLookUp");
														cbvutvi4vLookup.put("success", true);
														cbvutvi4vLookup.put("message", "SUCCEEDED");
														cbvutvi4vLookup.put("status", 200);
														cbvutvi4vLookup.set("fields", fieldJsonNode);
														((ArrayNode) cbvutvi4vLookup.get("fields")).add(fieldJsonNode1);
														cbvutvi4vLookup.set("result", resultJsonNode);
														modules.add(cbvutvi4vLookup);
														((ObjectNode) records.get(l).get("ruleResult").get(3))
																.set("modules", modules);

														if (cbvutvi4v.get(0).getFlag().equals("C")) {

															String checkId = records.get(l).get("checkId") != null
																	? records.get(l).get("checkId").asText()
																	: "";
															sendDataToL3CannotVerify(componentScopingNode,
																	components.get(k), records.get(l), mapper, checkId);

															// processComponent(subComponentName,components.get(k),
															// verificationSLA, cbvutvi4v);
															logger.info("Execute Cannot be verified");
															ObjectNode cbvutvRuleResult = mapper.createObjectNode();
															cbvutvRuleResult.put("module", "cbvutvRuleResult");
															fieldJsonNode = mapper.readTree("[]");
															// JsonNode ruleResultJsonNode1 = mapper.readTree("Excute
															// Cannot be verified");
															cbvutvRuleResult.put("success", true);
															cbvutvRuleResult.put("message", "SUCCEEDED");
															cbvutvRuleResult.put("status", 200);
															cbvutvRuleResult.set("fields", fieldJsonNode);
															cbvutvRuleResult.put("result", "Excute Cannot be verified");
															modules.add(cbvutvRuleResult);
															((ObjectNode) records.get(l).get("ruleResult").get(3))
																	.set("modules", modules);
															/*
															 * Logic for Attempt Save
															 */

															if (StringUtils.isNotEmpty(checkId)) {
																/*
																 * Logic for Saving information in Database tables
																 * Client Specific Tables and Client Specific Record
																 * details
																 */
//																logger.info("Make Rest call to Save");

//																CaseSpecificRecordDetailPOJO caseSpecificRecordDetail = new CaseSpecificRecordDetailPOJO();
//																caseSpecificRecordDetail.setComponentName(
//																		components.get(k).getComponentname());
//																caseSpecificRecordDetail
//																		.setProduct(components.get(k).getPRODUCT());
//																caseSpecificRecordDetail.setComponentRecordField(
//																		records.get(l).toString());
//																caseSpecificRecordDetail.setInstructionCheckId(checkId);
//																caseSpecificRecordDetail
//																		.setCaseSpecificId(caseSpecificInfoId);
//																caseSpecificRecordDetail.setCaseSpecificRecordStatus(
//																		"cannot be verify");
//																caseSpecificRecordDetailPOJOs
//																		.add(caseSpecificRecordDetail);
																/*
																 * Make Rest call and save this Info DB
																 */
//																JsonNode caseSpecificRecordDetailNode = mapper
//																		.convertValue(caseSpecificRecordDetail,
//																				JsonNode.class);
//																String caseSpecificRecordDetailStr = null;
//																if (caseSpecificRecordDetailNode != null) {
//																	caseSpecificRecordDetailStr = cbvutvi4vApiService
//																			.sendDataToCaseSpecificRecord(
//																					caseSpecificRecordDetailNode
//																							.toString());
//																}
//																CaseSpecificRecordDetailPOJO caseSpecificRecordDetail1 = new CaseSpecificRecordDetailPOJO();
//																if (caseSpecificRecordDetailStr != null) {
//																	JsonNode caseSpecificRecordNode = mapper
//																			.readTree(caseSpecificRecordDetailStr);
//																	caseSpecificRecordDetail1 = mapper.treeToValue(
//																			caseSpecificRecordNode,
//																			CaseSpecificRecordDetailPOJO.class);
//																}
//																Long caseSpecificRecordId = null;// Treat as request ID
																									// for other like
//																									// attempt history
//																if (caseSpecificRecordDetail1 != null) {
//																	caseSpecificRecordId = caseSpecificRecordDetail1
//																			.getCaseSpecificDetailId();
//																}
																/*
																 * Make call to Attempt History
																 */
//																AttemptHistory attemptHistory = new AttemptHistory();
//																attemptHistory.setAttemptStatusid((long) 55);// (55,NULL,'Cannot
//																												// be
//																												// verified','Valid','2020-02-26
//																												// 13:28:17',3),
//																attemptHistory.setAttemptDescription(
//																		"Employment details not disclosed as per Company policy. Hence cannot verify.");
//																attemptHistory.setName("Name not disclosed");
//																attemptHistory.setJobTitle("Official");
//																attemptHistory.setCheckid(checkId);
//																attemptHistory.setFollowupId((long) 33);
//																attemptHistory.setRequestid(caseSpecificRecordId);
//																// attemptHistory.setFollowupId((long)1);
//																attemptHistory.setContactDate(new Date().toString());
																// attemptHistory.setDateVerificationCompleted(new
																// Date())
																// attemptHistory.setExecutiveSummary("Cannot be
																// verified (Not disclosed as per company policy")

//																saveAttempt(mapper, attemptHistory);
//
//																VerificationEventStatusPOJO verificationEventStatusPOJO = new VerificationEventStatusPOJO();
//																verificationEventStatusPOJO.setCheckId(checkId);
//																verificationEventStatusPOJO.setEventName("cbvutv");
//																verificationEventStatusPOJO.setEventType("auto");
//																verificationEventStatusPOJO.setCaseNo(
//																		componentScopingNode.getCASE_NUMBER());
//																verificationEventStatusPOJO.setUser("System");
//																verificationEventStatusPOJO
//																		.setStatus("Cannot be verified");
//																verificationEventStatusPOJO
//																		.setRequestId(caseSpecificRecordId);
//																String verificationEventStatusStr = mapper
//																		.writeValueAsString(
//																				verificationEventStatusPOJO);
//																cbvutvi4vApiService.sendDataToVerificationEventStatus(
//																		verifictioneventstatusRestUrl,
//																		verificationEventStatusStr);

																logger.info("Value of Instruction Check ID : {}",
																		checkId);
															} else {
																logger.info("Instruction Check Id is Null");
															}

														} else if (cbvutvi4v.get(0).getFlag().equals("U")) {

															String checkId = records.get(l).get("checkId") != null
																	? records.get(l).get("checkId").asText()
																	: "";
															sendDataToL3UnableVerify(componentScopingNode,
																	components.get(k), records.get(l), mapper, checkId);

															// processComponent(subComponentName,components.get(k),
															// verificationSLA, cbvutvi4v);
															logger.info("Excute Unabled to verified");
															ObjectNode cbvutvRuleResult = mapper.createObjectNode();
															cbvutvRuleResult.put("module", "cbvutvRuleResult");
															fieldJsonNode = mapper.readTree("[]");
															// JsonNode ruleResultJsonNode1 = mapper.readTree("Excute
															// Unabled to verified");
															cbvutvRuleResult.put("success", true);
															cbvutvRuleResult.put("message", "SUCCEEDED");
															cbvutvRuleResult.put("status", 200);
															cbvutvRuleResult.set("fields", fieldJsonNode);
															cbvutvRuleResult.put("result",
																	"Excute Unabled to verified");
															modules.add(cbvutvRuleResult);
															((ObjectNode) records.get(l).get("ruleResult").get(3))
																	.set("modules", modules);
															/*
															 * Logic for Attempt Save
															 */

															if (StringUtils.isNotEmpty(checkId)) {

//																logger.info("Make Rest call to Save");

//																CaseSpecificRecordDetailPOJO caseSpecificRecordDetail = new CaseSpecificRecordDetailPOJO();
//																caseSpecificRecordDetail.setComponentName(
//																		components.get(k).getComponentname());
//																caseSpecificRecordDetail
//																		.setProduct(components.get(k).getPRODUCT());
//																caseSpecificRecordDetail.setComponentRecordField(
//																		records.get(l).toString());
//																caseSpecificRecordDetail.setInstructionCheckId(checkId);
//																caseSpecificRecordDetail
//																		.setCaseSpecificId(caseSpecificInfoId);
//																caseSpecificRecordDetail.setCaseSpecificRecordStatus(
//																		"unable to verify");
//																caseSpecificRecordDetailPOJOs
//																		.add(caseSpecificRecordDetail);

																/*
																 * Make Rest call and save this Info DB
																 */
//																JsonNode caseSpecificRecordDetailNode = mapper
//																		.convertValue(caseSpecificRecordDetail,
//																				JsonNode.class);
//																String caseSpecificRecordDetailStr = null;
//																if (caseSpecificRecordDetailNode != null) {
//																	caseSpecificRecordDetailStr = cbvutvi4vApiService
//																			.sendDataToCaseSpecificRecord(
//																					caseSpecificRecordDetailNode
//																							.toString());
//																}
//																CaseSpecificRecordDetailPOJO caseSpecificRecordDetail1 = new CaseSpecificRecordDetailPOJO();
//																if (caseSpecificRecordDetailStr != null) {
//																	JsonNode caseSpecificRecordNode = mapper
//																			.readTree(caseSpecificRecordDetailStr);
//																	caseSpecificRecordDetail1 = mapper.treeToValue(
//																			caseSpecificRecordNode,
//																			CaseSpecificRecordDetailPOJO.class);
//																}
//																Long caseSpecificRecordId = null;// Treat as request ID
//																									// for other like
//																									// attempt history
//																if (caseSpecificRecordDetail1 != null) {
//																	caseSpecificRecordId = caseSpecificRecordDetail1
//																			.getCaseSpecificDetailId();
//																}
//																/*
//																 * Make call to Attempt History
//																 */
//																AttemptHistory attemptHistory = new AttemptHistory();
//																attemptHistory.setAttemptStatusid((long) 56);// (56,NULL,
//																												// 'Unable
//																												// to
//																												// verify',
//																												// 'Valid',
//																												// '2020-02-26
//																												// 13:28:17',
//																												// 3),
//																attemptHistory.setAttemptDescription(
//																		"Name not disclosed, Official from the Human Resource Department verbally stated that employment details will not be disclosed to First Advantage Private Limited citing mutual process disagreement. Hence Unable to verify");
//																attemptHistory.setName("Name not disclosed");
//																attemptHistory.setJobTitle("Official");
//																attemptHistory.setCheckid(checkId);
//																attemptHistory.setFollowupId((long) 35);
//																// attemptHistory.setFollowupId((long)1);
//																attemptHistory.setContactDate(new Date().toString());
//																attemptHistory.setRequestid(caseSpecificRecordId);
//																// attemptHistory.setDateVerificationCompleted(new
//																// Date())
//																// attemptHistory.setExecutiveSummary("Unable to
//																// Verify")
//
//																saveAttempt(mapper, attemptHistory);
//
//																VerificationEventStatusPOJO verificationEventStatusPOJO = new VerificationEventStatusPOJO();
//																verificationEventStatusPOJO.setCheckId(checkId);
//																verificationEventStatusPOJO.setEventName("cbvutv");
//																verificationEventStatusPOJO.setEventType("auto");
//																verificationEventStatusPOJO.setCaseNo(
//																		componentScopingNode.getCASE_NUMBER());
//																verificationEventStatusPOJO.setUser("System");
//																verificationEventStatusPOJO.setStatus("CBV");
//																verificationEventStatusPOJO
//																		.setRequestId(caseSpecificRecordId);
//
//																String verificationEventStatusStr = mapper
//																		.writeValueAsString(
//																				verificationEventStatusPOJO);
//																cbvutvi4vApiService.sendDataToVerificationEventStatus(
//																		verifictioneventstatusRestUrl,
//																		verificationEventStatusStr);

																logger.info("Value of Instruction Check ID : {}",
																		checkId);
															} else {
																logger.info("Instruction Check Id is Null");
															}

														} else {
															logger.info("Move to next engine");
														}
														logger.info("CBVUTVI4V found");
													} else {
														logger.info("CBVUTVI4V Not found result is Empty");
														JsonNode fieldJsonNode = mapper.valueToTree(verificationSLA);
														JsonNode fieldJsonNode1 = mapper.valueToTree(cbvutvi4v);
														result = "{\"result\":\"Move to Next Engine\"}";
														JsonNode resultJsonNode = mapper.readTree(result);

														ObjectNode cbvutvi4vLookup = mapper.createObjectNode();
														cbvutvi4vLookup.put("module", "cbvutvi4vLookUp");
														cbvutvi4vLookup.put("success", true);
														cbvutvi4vLookup.put("message", "SUCCEEDED");
														cbvutvi4vLookup.put("status", 200);
														cbvutvi4vLookup.set("fields", fieldJsonNode);
														((ArrayNode) cbvutvi4vLookup.get("fields")).add(fieldJsonNode1);
														cbvutvi4vLookup.set("result", resultJsonNode);
														modules.add(cbvutvi4vLookup);
														((ObjectNode) records.get(l).get("ruleResult").get(3))
																.set("modules", modules);

													}

												} else {
													logger.info("Value of HTS is Other Than No");
													result = "{\"result\":\"Move to Next Engine\"}";
													JsonNode fieldJsonNode = mapper.valueToTree(verificationSLA);
													JsonNode resultJsonNode = mapper.readTree(result);

													ObjectNode cbvutvi4vLookup = mapper.createObjectNode();
													cbvutvi4vLookup.put("module", "cbvutvi4vLookUp");
													cbvutvi4vLookup.put("success", true);
													cbvutvi4vLookup.put("message", "SUCCEEDED");
													cbvutvi4vLookup.put("status", 200);
													cbvutvi4vLookup.set("fields", fieldJsonNode);
													cbvutvi4vLookup.set("result", resultJsonNode);
													modules.add(cbvutvi4vLookup);

													ObjectNode cbvutvRuleResult = mapper.createObjectNode();
													cbvutvRuleResult.put("module", "cbvutvRuleResult");
													fieldJsonNode = mapper.readTree("[]");
													JsonNode ruleResultJsonNode1 = mapper.readTree("[]");
													cbvutvRuleResult.put("success", true);
													cbvutvRuleResult.put("message", "SUCCEEDED");
													cbvutvRuleResult.put("status", 200);
													cbvutvRuleResult.set("fields", fieldJsonNode);
													cbvutvRuleResult.set("result", ruleResultJsonNode1);
													modules.add(cbvutvRuleResult);
													((ObjectNode) records.get(l).get("ruleResult").get(3))
															.set("modules", modules);
												}
											}
										} else {
											logger.info("SLA Response is Empty");
											logger.info("CBV-UTV-I4V SLA Response : {}", cbvVerificationSLAResponse);
											result = "{\"result\":\"Move to next Engine\"}";
											JsonNode fieldJsonNode = mapper.readTree("[]");
											JsonNode resultJsonNode = mapper.readTree(result);

											ObjectNode cbvutvi4vLookup = mapper.createObjectNode();
											cbvutvi4vLookup.put("module", "cbvutvi4vLookUp");
											cbvutvi4vLookup.put("success", true);
											cbvutvi4vLookup.put("message", "SUCCEEDED");
											cbvutvi4vLookup.put("status", 200);
											cbvutvi4vLookup.set("fields", fieldJsonNode);
											cbvutvi4vLookup.set("result", resultJsonNode);
											modules.add(cbvutvi4vLookup);

											ObjectNode cbvutvRuleResult = mapper.createObjectNode();
											cbvutvRuleResult.put("module", "cbvutvRuleResult");
											fieldJsonNode = mapper.readTree("[]");
											JsonNode ruleResultJsonNode1 = mapper.readTree("[]");
											cbvutvRuleResult.put("success", true);
											cbvutvRuleResult.put("message", "SUCCEEDED");
											cbvutvRuleResult.put("status", 200);
											cbvutvRuleResult.set("fields", fieldJsonNode);
											cbvutvRuleResult.set("result", ruleResultJsonNode1);
											modules.add(cbvutvRuleResult);
											((ObjectNode) records.get(l).get("ruleResult").get(3)).set("modules",
													modules);
										}
									} else {
										logger.info("Other than Employment Component");

										result = "{\"result\":\"CbvUtvI4v Record not Found\"}";
										JsonNode fieldJsonNode = mapper.readTree("[]");
										JsonNode resultJsonNode = mapper.readTree(result);
										ArrayNode ruleResultJsonNode = (ArrayNode) records.get(l).get("ruleResult");
										ObjectNode cbvutvi4vResult = mapper.createObjectNode();
										cbvutvi4vResult.put("engine", "cbvutvi4vResult");
										cbvutvi4vResult.put("success", true);
										cbvutvi4vResult.put("message", "SUCCEEDED");
										cbvutvi4vResult.put("status", 200);
										ruleResultJsonNode.add(cbvutvi4vResult);
										ArrayNode modules = mapper.createArrayNode();

										ObjectNode cbvutvi4vLookup = mapper.createObjectNode();
										cbvutvi4vLookup.put("module", "cbvutvi4vLookUp");
										cbvutvi4vLookup.put("success", true);
										cbvutvi4vLookup.put("message", "SUCCEEDED");
										cbvutvi4vLookup.put("status", 200);
										cbvutvi4vLookup.set("fields", fieldJsonNode);
										cbvutvi4vLookup.set("result", resultJsonNode);
										modules.add(cbvutvi4vLookup);

										ObjectNode cbvutvRuleResult = mapper.createObjectNode();
										cbvutvRuleResult.put("module", "cbvutvRuleResult");
										fieldJsonNode = mapper.readTree("[]");
										JsonNode ruleResultJsonNode1 = mapper.readTree("[]");
										cbvutvRuleResult.put("success", true);
										cbvutvRuleResult.put("message", "SUCCEEDED");
										cbvutvRuleResult.put("status", 200);
										cbvutvRuleResult.set("fields", fieldJsonNode);
										cbvutvRuleResult.set("result", ruleResultJsonNode1);
										modules.add(cbvutvRuleResult);
										((ObjectNode) records.get(l).get("ruleResult").get(3)).set("modules", modules);
									}
									if (cbvResponse != null && !cbvResponse.equals("[]")) {
										logger.info("CBV-UTV-I4V Value : {}", cbvResponse);
										logger.info("Found Result In CBV-UTV-I4V");
									} else {
										logger.info("CBV-UTV-I4V : {}", cbvResponse);
										logger.info("Not Found Result In CBV/UTV/I4V");
									}
									/* Convert Information to String */
									mrlReqString = mapper.writeValueAsString(cbvutvReq);
									/* Log Information Database Table */

								} else {
									logger.info("Include not found");
								}
							}
						}
					}

				}
			}
			String returnStr = mrlReqString;
			logger.info("Response :\n {}", returnStr);
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

	private String sendDataToL3CannotVerify(ComponentScoping componentScopingNode, Component component,
			JsonNode recordNode, ObjectMapper mapper, String checkId)
			throws JsonMappingException, JsonProcessingException {
		/*
		 * Logic for Sending Verification Data to L3
		 */
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
		Date todaysDate = new Date();
		String todaysDateStr = formatter.format(todaysDate);

		TaskSpecsPOJO taskSpecs = new TaskSpecsPOJO();
		CaseReferencePOJO caseReference = mapper.readValue(componentScopingNode.getCaseReference().toString(),
				CaseReferencePOJO.class);

		caseReference.setCheckId(checkId);
		caseReference.setNgStatus("CBV");
		caseReference.setNgStatusDescription("Cannot be verified");
		caseReference.setSbuName(componentScopingNode.getSBU_NAME());
		caseReference.setProductName(component.getPRODUCT());
		caseReference.setPackageName(componentScopingNode.getPackageName());
		caseReference.setComponentName(component.getComponentname());

		CheckVerificationPOJO checkVerification = new CheckVerificationPOJO();

		checkVerification.setCountry("India");
		checkVerification.setExecutiveSummaryComments("Cannot be verified (Not disclosed as per company policy)");
		checkVerification.setReportComments("");
		checkVerification.setResultCode("");
		checkVerification.setEmailId("");
		checkVerification.setVerifierName("Name not disclosed");
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
		checkVerification
				.setInternalNotes("Employment details not disclosed as per Company policy. Hence cannot be verified");
		checkVerification.setDateVerificationCompleted(todaysDateStr);
		checkVerification.setDisposition("");
		checkVerification.setExpectedClosureDate("");
		checkVerification.setEndStatusOfTheVerification("Cannot be verified");
		checkVerification.setVerifierDesignation("Official");
		checkVerification.setFollowUpDateAndTimes("");
		checkVerification.setVerifierNumber("");

		taskSpecs.setCaseReference(caseReference);
		taskSpecs.setCheckVerification(checkVerification);

		taskSpecs.setQuestionaire(new ArrayList<>());
		/*
		 * Make Questionnaire Object and Push in Questionnaire List
		 */
		List<String> quetionRefIDList = new ArrayList<>();
		quetionRefIDList.add("500110");
		quetionRefIDList.add("807981");
		quetionRefIDList.add("800054");
		/*
		 * Call for Questionnaire List
		 */
		String questionList = null;
		List<QuestionPOJO> questionnairePOJOList = new ArrayList<>();
		List<QuestionnairePOJO> questionnairePOJOList1 = new ArrayList<>();
		try {
			questionList = cbvutvi4vApiService.sendDataTogetCheckId(questionaireURL, checkId);
			logger.info("Question List" + questionList);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			ObjectNode attemptQuestionnaireNode = mapper.createObjectNode();
			try {
				attemptQuestionnaireNode = (ObjectNode) mapper.readTree(questionList);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			if (attemptQuestionnaireNode != null && attemptQuestionnaireNode.has("response")) {
				JsonNode questionnaire = attemptQuestionnaireNode.get("response");
				try {
					questionnairePOJOList = mapper.readValue(questionnaire.toString(),
							new TypeReference<List<QuestionPOJO>>() {
							});
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < questionnairePOJOList.size(); i++) {
			if (quetionRefIDList.contains(questionnairePOJOList.get(i).getGlobalQuestionId())) {
				QuestionnairePOJO questionnairePOJO1 = new QuestionnairePOJO();
				questionnairePOJO1.setCaseQuestionRefID(questionnairePOJOList.get(i).getGlobalQuestionId());
				questionnairePOJO1.setAnswer(questionnairePOJOList.get(i).getAnswere());
				questionnairePOJO1.setQuestion(questionnairePOJOList.get(i).getQuestionName());
				questionnairePOJO1.setReportData("Name not disclosed, Official from the Human "
						+ "Resource Department verbally stated that employment details will not "
						+ "be disclosed to First Advantage Private Limited citing mutual process"
						+ " disagreement.Hence Unable to verify");
				questionnairePOJO1.setStatus("");
				questionnairePOJO1.setVerifiedData("");
				questionnairePOJOList1.add(questionnairePOJO1);
			}
		}
		taskSpecs.setQuestionaire(questionnairePOJOList1);

		FileUploadPOJO fileUpload = new FileUploadPOJO();
		fileUpload.setVerificationReplyDocument(new ArrayList<>());
		taskSpecs.setFileUpload(fileUpload);

		String getVerificationStatusforL3 = mapper.writeValueAsString(taskSpecs);
		String sendInfol3VeriStatus = null;

		try {
			logger.info("getVerificationStatusforL3Node : {} ", getVerificationStatusforL3);
			sendInfol3VeriStatus = cbvutvi4vApiService.sendDataToRest(verificationStatusL3Url,
					getVerificationStatusforL3, null);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return sendInfol3VeriStatus;
	}

	private String sendDataToL3UnableVerify(ComponentScoping componentScopingNode, Component component,
			JsonNode recordNode, ObjectMapper mapper, String checkId)
			throws JsonMappingException, JsonProcessingException {
		/*
		 * Logic for Sending Verification Data to L3
		 */

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
		Date todaysDate = new Date();
		String todaysDateStr = formatter.format(todaysDate);

		TaskSpecsPOJO taskSpecs = new TaskSpecsPOJO();
		CaseReferencePOJO caseReference = mapper.readValue(componentScopingNode.getCaseReference().toString(),
				CaseReferencePOJO.class);

		caseReference.setCheckId(checkId);
		caseReference.setNgStatus("UTV");
		caseReference.setNgStatusDescription("Unable to Verify");
		caseReference.setSbuName(componentScopingNode.getSBU_NAME());
		caseReference.setProductName(component.getPRODUCT());
		caseReference.setPackageName(componentScopingNode.getPackageName());
		caseReference.setComponentName(component.getComponentname());

		CheckVerificationPOJO checkVerification = new CheckVerificationPOJO();

		checkVerification.setCountry("India");
		checkVerification.setExecutiveSummaryComments("Unable to Verify");
		checkVerification.setReportComments("");
		checkVerification.setResultCode("");
		checkVerification.setEmailId("");
		checkVerification.setVerifierName("Name not disclosed");
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
				"Name not disclosed, Official from the Human Resource Department verbally stated that employment details will not be disclosed to First Advantage Private Limited citing mutual process disagreement. Hence Unable to verify");
		checkVerification.setDateVerificationCompleted(todaysDateStr);
		checkVerification.setDisposition("");
		checkVerification.setExpectedClosureDate("");
		checkVerification.setEndStatusOfTheVerification("Unable to Verify");
		checkVerification.setVerifierDesignation("Official");
		checkVerification.setFollowUpDateAndTimes("");
		checkVerification.setVerifierNumber("");

		taskSpecs.setCaseReference(caseReference);
		taskSpecs.setCheckVerification(checkVerification);

		taskSpecs.setQuestionaire(new ArrayList<>());
		List<String> quetionRefIDList = new ArrayList<>();
		quetionRefIDList.add("500110");
		quetionRefIDList.add("807981");
		quetionRefIDList.add("800054");
		/*
		 * Call for Questionnaire List
		 */
		String questionList = null;
		List<QuestionPOJO> questionnairePOJOList = new ArrayList<>();
		List<QuestionnairePOJO> questionnairePOJOList1 = new ArrayList<>();
		try {
			questionList = cbvutvi4vApiService.sendDataTogetCheckId(questionaireURL, checkId);
			logger.info("Question List" + questionList);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			ObjectNode attemptQuestionnaireNode = mapper.createObjectNode();
			try {
				attemptQuestionnaireNode = (ObjectNode) mapper.readTree(questionList);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			if (attemptQuestionnaireNode != null && attemptQuestionnaireNode.has("response")) {
				JsonNode questionnaire = attemptQuestionnaireNode.get("response");
				try {
					questionnairePOJOList = mapper.readValue(questionnaire.toString(),
							new TypeReference<List<QuestionPOJO>>() {
							});
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < questionnairePOJOList.size(); i++) {
			if (quetionRefIDList.contains(questionnairePOJOList.get(i).getGlobalQuestionId())) {
				QuestionnairePOJO questionnairePOJO1 = new QuestionnairePOJO();
				questionnairePOJO1.setCaseQuestionRefID(questionnairePOJOList.get(i).getGlobalQuestionId());
				questionnairePOJO1.setAnswer(questionnairePOJOList.get(i).getAnswere());
				questionnairePOJO1.setQuestion(questionnairePOJOList.get(i).getQuestionName());
				questionnairePOJO1.setReportData("Name not disclosed, Official from the Human "
						+ "Resource Department verbally stated that employment details will not "
						+ "be disclosed to First Advantage Private Limited citing mutual process"
						+ " disagreement.Hence Unable to verify");
				questionnairePOJO1.setStatus("");
				questionnairePOJO1.setVerifiedData("");
				questionnairePOJOList1.add(questionnairePOJO1);
			}
		}

		taskSpecs.setQuestionaire(questionnairePOJOList1);

		FileUploadPOJO fileUpload = new FileUploadPOJO();
		fileUpload.setVerificationReplyDocument(new ArrayList<>());
		taskSpecs.setFileUpload(fileUpload);

		String getVerificationStatusforL3 = mapper.writeValueAsString(taskSpecs);
		String sendInfol3VeriStatus = null;

		try {
			logger.info("getVerificationStatusforL3Node : {} ", getVerificationStatusforL3);
			sendInfol3VeriStatus = cbvutvi4vApiService.sendDataToRest(verificationStatusL3Url,
					getVerificationStatusforL3, null);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return sendInfol3VeriStatus;
	}

	private Component processComponent(String subComponentName, Component component,
			List<VerificationSLA> verificationSLA, List<CBVUTVI4V> cbvutvi4v) throws JsonProcessingException {

		List<CBVUTVI4VRuleConfig> ruleConfigList = cbvutvi4vRuleConfigRepository
				.findByComponentNameAndSubComponentName("ANY", "ANY");

		// decisionId = "_87FFA799-220B-4F27-8944-B854C23E904D"
		String modelName = "";
		String nameSpace = "";
		String config = "";
		if (ruleConfigList != null && !ruleConfigList.isEmpty()) {
			CBVUTVI4VRuleConfig ruleConfigs = ruleConfigList.get(0);
			config = ruleConfigs.getConfigs();
			modelName = ruleConfigs.getModelName();
			nameSpace = ruleConfigs.getModelNamespace();
		}
		logger.info("Value of Rule Configuration : {}", config);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode verificationSLAnode = mapper.valueToTree(verificationSLA);
		JsonNode cbvutvi4vnode = mapper.valueToTree(cbvutvi4v);
		ObjectNode rootNode = mapper.createObjectNode();

		rootNode.put("model-namespace", nameSpace);
		rootNode.put("model-name", modelName);
		rootNode.put("decision-name", modelName);

		ObjectNode dmnContext = mapper.createObjectNode();
		dmnContext.put("Today Date", new Date().toString());
		JsonNode jsonNode = mapper.readTree(config);
		dmnContext.set("Expected Data", jsonNode);

		rootNode.set("dmn-context", dmnContext);

		logger.info("Value of Component Size : {}", component.getRecords().size());
		ArrayNode records = mapper.createArrayNode();
		for (int i = 0; i < component.getRecords().size(); i++) {

			ObjectNode record = mapper.createObjectNode();
			record.put("RecordNbr", i);
			Map<String, Object> result = mapper.convertValue(component.getRecords().get(i),
					new TypeReference<Map<String, Object>>() {
					});
			for (Map.Entry<String, Object> entry : result.entrySet()) {
				String keyStr = checkField(entry.getKey());
				record.put(keyStr, entry.getValue().toString());
			}
			record.set("VerificationSLAdata", verificationSLAnode);
			record.set("CBVUTVList", cbvutvi4vnode);
			records.add(record);
		}
		((ObjectNode) rootNode.get("dmn-context")).set("Records Data", records);
		String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
		logger.info("-------------------- : {}", jsonString);
		String decisionCentralResult = callDecisionServerResult(rootNode);
		logger.info("////////////////////////////////////////");
		logger.info("DMNResult : {}", decisionCentralResult);

		logger.info("Parse DMN Result");
		JsonNode dmnResultNode = mapper.readTree(decisionCentralResult);
		ArrayNode dmnResult = (ArrayNode) dmnResultNode.get("result").get("dmn-evaluation-result")
				.get("decision-results").get("_DD72578F-D24E-40F0-9A6E-8357DD312F73").get("result");
		logger.info("value of DMN Result : {}", dmnResult.size());
		logger.info("value of DMN Result : {}", dmnResult);
		// Iterate over Record Result and Assigned the value to DMN Value
		logger.info("{}", dmnResult.get(0).get("RecordResult").get(0).get("DMNResult"));
		for (int i = 0; i < component.getRecords().size(); i++) {
			ArrayNode resultArray;
			if (component.getRecords().get(i).get("ruleResult").isArray()) {
				resultArray = (ArrayNode) component.getRecords().get(i).get("ruleResult").get(0).get("result");
			} else {
				resultArray = (ArrayNode) component.getRecords().get(i).get("ruleResult").get("result");
			}
			if (resultArray.get(0).toString().contains("Include")) {
				JsonNode recordResult = dmnResult.get(i).get("RecordResult").get(0).get("DMNResult");
				ArrayNode modules = mapper.createArrayNode();
				ObjectNode cbvutvRuleResult = mapper.createObjectNode();
				cbvutvRuleResult.put("module", "cbvutvRuleResult");
				JsonNode fieldJsonNode = mapper.readTree("[]");
				cbvutvRuleResult.put("success", true);
				cbvutvRuleResult.put("message", "SUCCEEDED");
				cbvutvRuleResult.put("status", 200);
				cbvutvRuleResult.set("fields", fieldJsonNode);
				cbvutvRuleResult.set("result", recordResult);
				modules.add(cbvutvRuleResult);
				logger.info("iteration : {}, Value of RecordResult : {}", i, recordResult);
				if (recordResult.toString().contains("Unable to verify")) {
					logger.info("Excute - Unable to verify");
					Map<String, String> result = mapper.convertValue(dmnResult.get(i).get("RecordResult").get(0),
							new TypeReference<Map<String, String>>() {
							});
					for (Map.Entry<String, String> entry : result.entrySet()) {
						if (entry.getKey().equals("DMNResult"))
							continue;
						((ObjectNode) component.getRecords().get(i)).put(entry.getKey(), entry.getValue());
					}
				} else if (recordResult.toString().contains("Cannot be verified")) {
					logger.info("Execute - Cannot be verified");
					Map<String, String> result = mapper.convertValue(dmnResult.get(i).get("RecordResult").get(0),
							new TypeReference<Map<String, String>>() {
							});
					for (Map.Entry<String, String> entry : result.entrySet()) {
						if (entry.getKey().equals("DMNResult"))
							continue;
						((ObjectNode) component.getRecords().get(i)).put(entry.getKey(), entry.getValue());
					}
				} else {
					logger.info("Move check to next Engine");
					Map<String, String> result = mapper.convertValue(dmnResult.get(i).get("RecordResult").get(0),
							new TypeReference<Map<String, String>>() {
							});
					for (Map.Entry<String, String> entry : result.entrySet()) {
						if (entry.getKey().equals("DMNResult"))
							continue;
						((ObjectNode) component.getRecords().get(i)).put(entry.getKey(), entry.getValue());
					}
				}
				logger.info("Value of modules : {}", component.getRecords().get(i));
				try {
					modules.add((component.getRecords().get(i).get("ruleResult").get(3).get("modules")));
				} catch (Exception e) {
					logger.error("Value of error messages : {}", e.getMessage());
				}
				((ObjectNode) component.getRecords().get(i).get("ruleResult").get(3)).set("modules", modules);

			} else {
				logger.info("Discarding the Not Include record");
			}
		}

		return component;
	}

	private String callDecisionServerResult(ObjectNode dmnContext) {

		logger.info("{}", dmnContext);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(dmnContext.toString(), headers);
		String rhdmAdminUserId = env.getProperty("rhdmAdmin.userid");
		String rhdmAdminPassword = env.getProperty("rhdmAdmin.password");
		RestTemplate restTemplate = restTemplateBuilder.basicAuthentication(rhdmAdminUserId, rhdmAdminPassword).build();
		String resultAsJsonStr = restTemplate.postForObject(env.getProperty("india.cbvutv.rhdm.dc.url"), request,
				String.class);
		logger.info(resultAsJsonStr);
		return resultAsJsonStr;
	}

	/*
	 * remove slash "/" from field name
	 */
	private String checkField(String field) {
		String ret = field;
		String[] specialChars = env.getProperty("india.cbvutvi4v.special.chars").split(",");
		for (int i = 0; i < specialChars.length; i++) {
			ret = ret.replace(specialChars[i], "");
		}
		// Adding replace " of " with "_of_"
		String[] specialWords = env.getProperty("india.cbvutvi4v.special.words").split(",");
		for (int i = 0; i < specialWords.length; i++) {
			String fromStr = " " + specialWords[i] + " ";
			String toStr = "_" + specialWords[i] + "_";
			ret = ret.replace(fromStr, toStr);
		}
		return ret.trim();
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
				attemptHistoryResponse = cbvutvi4vApiService.sendDataToAttemptHistory(attemptHistoryStr);
				logger.info("Attempt History Response : {}", attemptHistoryResponse);
				attemptHistoryNew = mapper.readValue(attemptHistoryResponse, new TypeReference<AttemptHistory>() {
				});
				if (attemptHistoryNew.getAttemptid() != null) {
					logger.info("Attempt saved sucessfully. : {}", attemptHistoryNew.getAttemptid());

					AttemptStatusData attemptStatusData = new AttemptStatusData();
					attemptStatusData.setAttemptId(attemptHistoryNew.getAttemptid());
					attemptStatusData.setDepositionId((long) 13);
					attemptStatusData.setEndstatusId((long) 1);
					attemptStatusData.setModeId((long) 14);

					JsonNode attemptStatusDataJsonNode = mapper.valueToTree(attemptStatusData);
					logger.info("Value of attemptStatusData bean to Json : {}", attemptStatusDataJsonNode);
					String attemptStatusDataStr = mapper.writeValueAsString(attemptStatusDataJsonNode);
					logger.info("Value of attemptStatusData Json to String : {}", attemptStatusDataStr);

					String attemptStatusDataResponse = null;
					AttemptStatusData attemptStatusDataNew = new AttemptStatusData();

					try {
						attemptStatusDataResponse = cbvutvi4vApiService.sendDataToAttempt(spocAttemptStatusDataRestUrl,
								attemptStatusDataStr);
						logger.info("Attempt status data Response : {}", attemptStatusDataResponse);
						attemptStatusDataNew = mapper.readValue(attemptHistoryResponse,
								new TypeReference<AttemptStatusData>() {
								});
						if (attemptStatusDataNew.getAttemptId() != null) {
							logger.info("Attempt status data saved sucessfully. : {}",
									attemptStatusDataNew.getAttemptId());
						} else {
							logger.info("Attempt status data not saved.");
						}
					} catch (Exception e) {
						logger.error("Exception in calling save attempt status data : {} ", e.getMessage());
						logger.error(e.getMessage(), e);
						e.printStackTrace();
					}

				} else {
					logger.info("Attempt history not saved.");
				}
			} catch (Exception e) {
				logger.error("Exception in calling save attempt history : {} ", e.getMessage());
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		} catch (IllegalArgumentException | JsonProcessingException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}

	}

	private void callback(String postStr) {
		try {

			logger.info("postStr : \n {}", postStr);
			URL url = new URL(env.getProperty("cbvutvi4v.router.callback.url"));
			logger.info("Using callback URL : \n {}", url);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");

			String authToken = JsonParser.parseString(postStr).getAsJsonObject().get("metadata").getAsJsonObject()
					.get("requestAuthToken").getAsString();
			logger.info("Auth Token : \n {}", authToken);
			con.setRequestProperty("tokenId", authToken);

			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			os.write(postStr.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();
			logger.info("Callback POST Response Code : {} : {}", responseCode, con.getResponseMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}