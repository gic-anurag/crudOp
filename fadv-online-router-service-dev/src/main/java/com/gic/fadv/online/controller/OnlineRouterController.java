package com.gic.fadv.online.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
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
import com.gic.fadv.online.model.AttemptHistory;
import com.gic.fadv.online.model.AttemptStatusData;
import com.gic.fadv.online.model.Component;
import com.gic.fadv.online.model.ComponentScoping;
import com.gic.fadv.online.model.Datum;
import com.gic.fadv.online.model.OnlineManualVerification;
import com.gic.fadv.online.model.OnlineReq;
import com.gic.fadv.online.model.OnlineVerificationChecks;
import com.gic.fadv.online.pojo.CaseReferencePOJO;
import com.gic.fadv.online.pojo.CaseSpecificInfoPOJO;
import com.gic.fadv.online.pojo.CaseSpecificRecordDetailPOJO;
import com.gic.fadv.online.pojo.CheckVerificationPOJO;
import com.gic.fadv.online.pojo.FileUploadPOJO;
import com.gic.fadv.online.pojo.OnlineAttemptsPOJO;
import com.gic.fadv.online.pojo.TaskSpecsPOJO;
import com.gic.fadv.online.pojo.VerificationEventStatusPOJO;
import com.gic.fadv.online.service.OnlineAPIParsingService;
import com.gic.fadv.online.service.OnlineApiService;
import com.google.gson.JsonParser;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/india")
@Api(value = "Cross Reference", description = "Operations pertaining to Employment Suspect List (Cross Reference master data)")
public class OnlineRouterController {

	@Autowired
	private OnlineApiService onlineApiService;
	@Autowired
	private Environment env;
	@Autowired
	private RestTemplateBuilder restTemplateBuilder;
	@Autowired
	private OnlineAPIParsingService onlineAPIParsingService;
	@Value("${online.router.mca.rest.url}")
	private String mcaRestUrl;
	@Value("${online.router.loan.rest.url}")
	private String loanRestUrl;
	@Value("${online.router.manupatra.rest.url}")
	private String manupatraRestUrl;
	@Value("${online.router.adversemedia.rest.url}")
	private String adversemediaRestUrl;
	@Value("${online.router.worldcheck.rest.url}")
	private String worldcheckRestUrl;
	@Value("${online.router.watchout.rest.url}")
	private String watchoutRestUrl;
	@Value("${verification.online.manual.UI.url}")
	private String verificationOnlineManualUIUrl;
	@Value("${spocattemptstatusdata.rest.url}")
	private String spocAttemptStatusDataRestUrl;
	@Value("${spocattempthistory.rest.url}")
	private String spocAttemptHistoryRestUrl;
	@Value("${casespecificinfo.rest.url}")
	private String caseSpecificInfoUrl;
	@Value("${online.verifictioneventstatus.rest.url}")
	private String verifictioneventstatusRestUrl;
	@Value("${questionaire.list.l3.url}")
	private String questionaireURL;
	@Value("${data.entry.l3.url}")
	private String l3DataEntryURL;
	
	
	@Value("${verification.url.checkid.l3}")
	private String verificationStatusUrlL3;

	@Value("${verification.status.url.l3}")
	private String verificationStatusL3Url;
	
	private static final Logger logger = LoggerFactory.getLogger(OnlineRouterController.class);

	@ApiOperation(value = "This service is used to process Records at CBV-UTV-I4V router and return the result to workflow router ", response = List.class)
	@PostMapping(path = "/async/onlinerouter2", consumes = "application/json", produces = "application/json")
	public DeferredResult<ResponseEntity<String>> doAsyncProcess(@RequestBody String inStr) {
		DeferredResult<ResponseEntity<String>> ret = new DeferredResult<>();
		ForkJoinPool.commonPool().submit(() -> {
			logger.info("Got async Request:\n" + inStr);
			processRequest(inStr, true);
			ret.setResult(ResponseEntity.ok("ok"));
		});
		ret.onCompletion(() -> logger.info("async process request done"));
		return ret;
	}
	
//	@PostMapping("/test-detail")
//	public Map<String, String> testGetDetail(@RequestBody String inStr) throws JsonProcessingException {
//		// Creating the ObjectMapper object
//		ObjectMapper mapper = new ObjectMapper();
//		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//		// Converting the JSONString to Object
//		OnlineReq onlineReq = mapper.readValue(inStr, OnlineReq.class);
//		return getDetailsFromDataEntry(onlineReq);
//	}

	@ApiOperation(value = "This service is used to process Records at CBV-UTV-I4V router and return the result to workflow router ", response = List.class)
	@PostMapping(path = "/onlinerouter2", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> doProcess(@RequestBody String inStr) {
		try {
			logger.info("Got Request:\n" + inStr);
			String response = processRequest(inStr, false);
			logger.info("Processed Request");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}

	private String processRequest(String inStr, boolean asyncStatus) {
		LocalDateTime startTime = LocalDateTime.now();
		try {
			// Creating the ObjectMapper object
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			// Converting the JSONString to Object
			OnlineReq onlineReq = mapper.readValue(inStr, OnlineReq.class);
			JsonNode dataObj = null;
			String onlineResponse = "";
			
			Map<String, String> resultMap = getDetailsFromDataEntry(onlineReq);
			
			for (int i = 0; i < onlineReq.getData().size(); i++) {
				List<ComponentScoping> componentScoping = onlineReq.getData().get(i).getTaskSpecs()
						.getComponentScoping();
				for (int j = 0; j < componentScoping.size(); j++) {
					ComponentScoping componentScopingNode = componentScoping.get(j);
					String clientName = componentScopingNode.getCLIENT_NAME().trim();
					// String clientCode=componentScopingNode.getCLIENT_CODE().trim();
					String clientSBU = componentScopingNode.getSBU_NAME().trim();
					String packageName = componentScopingNode.getPackageName().trim();
					String caseNo = componentScopingNode.getCASE_NUMBER();
					String crn = componentScopingNode.getCASE_REF_NUMBER();
					String candidateName = StringUtils.isEmpty(resultMap.get("primaryName")) ? componentScopingNode.getCandidate_Name() : resultMap.get("primaryName");

					List<Component> components = componentScopingNode.getComponents();

					CaseSpecificInfoPOJO caseSpecificInfo = new CaseSpecificInfoPOJO();
					caseSpecificInfo.setCandidateName(componentScopingNode.getCandidate_Name());
					caseSpecificInfo.setCaseDetails(componentScopingNode.getCaseDetails().toString());
					caseSpecificInfo.setCaseMoreInfo(componentScopingNode.getCaseMoreInfo().toString());
					caseSpecificInfo.setCaseReference(componentScopingNode.getCaseReference().toString());
					caseSpecificInfo.setCaseRefNumber(componentScopingNode.getCASE_REF_NUMBER());
					caseSpecificInfo.setCaseNumber(componentScopingNode.getCASE_NUMBER());
					caseSpecificInfo.setClientCode(componentScopingNode.getCLIENT_CODE());
					caseSpecificInfo.setClientName(componentScopingNode.getCLIENT_NAME());
					caseSpecificInfo.setSbuName(componentScopingNode.getSBU_NAME());
					caseSpecificInfo.setPackageName(componentScopingNode.getPackageName());
					//Used for Online Status should be I
					//caseSpecificInfo.setStatus("I");
					caseSpecificInfo.setClientSpecificFields(componentScopingNode.getClientSpecificFields().toString());

					OnlineAttemptsPOJO onlineAttemptsPOJO = new OnlineAttemptsPOJO();
					onlineAttemptsPOJO.setCaseSpecificInfoPOJO(caseSpecificInfo);
					onlineAttemptsPOJO.setCaseNo(componentScopingNode.getCASE_NUMBER());
					onlineAttemptsPOJO.setComponentScoping(componentScopingNode);
					

					List<String> checkIdList = new ArrayList<>();
					onlineAttemptsPOJO.setCheckIdList(checkIdList);
					
					/*
					 * Insert Info in DB
					 */
					JsonNode caseSpecificInfoNode = mapper.convertValue(caseSpecificInfo,JsonNode.class); 
					String caseSpecificInfoStr =caseSpecificInfoNode.toString();
					
					String caseSpecificResponse=onlineApiService.sendDataToAttempt(caseSpecificInfoUrl, caseSpecificInfoStr);
					CaseSpecificInfoPOJO caseSpecificInfo1 = new CaseSpecificInfoPOJO();
					if(caseSpecificResponse!=null) {
						JsonNode caseSpecificResponseNode= mapper.readTree(caseSpecificResponse);
						caseSpecificInfo1 = mapper.treeToValue(caseSpecificResponseNode, CaseSpecificInfoPOJO.class);
					}
					Long caseSpecificInfoId=null;
					if(caseSpecificInfo1!=null) {
						caseSpecificInfoId=caseSpecificInfo1.getCaseSpecificId();
					}
					onlineAttemptsPOJO.setCaseSpecificInfoId(caseSpecificInfoId);
					

					/*
					 * Call ComponentWiseAPIMapping and fetch all mapped data
					 */
					JsonNode componentWiseAPINode = null;
					String componentWiseAPIstr = null;
					try {
						componentWiseAPIstr = onlineApiService.sendDataToComponentWiseAPIRest();
					} catch (Exception e) {
						e.printStackTrace();
					}
//					logger.info("Component Wise API Response" + componentWiseAPIstr);
					/*
					 * Convert String to JsonNode
					 */
					if (componentWiseAPIstr != null) {
						try {
							componentWiseAPINode = mapper.readTree(componentWiseAPIstr);
						} catch (IOException e) {
							e.printStackTrace();
						}
						/*
						 * Convert JsonNode to <String,JsonNode> Map
						 */
						Map<String, JsonNode> componentSubComponentJsonMAP = null;
						try {
							componentSubComponentJsonMAP = jsonNodeToMap(componentWiseAPINode);
						} catch (Exception e) {
							e.printStackTrace();
						}
//						logger.info("Value of componentSubComponentMapValue" + componentSubComponentJsonMAP);
						if (componentSubComponentJsonMAP != null) {
							/*
							 * Make List of OnlineVerificationChecks which insert the data in database
							 * according to Check ID
							 */
							List<OnlineVerificationChecks> onlineVerificationChecksList = new ArrayList<>();
							// Pass this list to function and add value of not clear checks
							for (int k = 0; k < components.size(); k++) {
								String componentName = components.get(k).getComponentname();
								String subComponentName = components.get(k).getPRODUCT();

								onlineAttemptsPOJO.setComponentName(componentName);
								onlineAttemptsPOJO.setProductName(subComponentName);
								/*
								 * Make Search String to search in componentSubComponentMAP
								 */

								String componentSubComponentSearchStr = componentName.trim() + "##"
										+ subComponentName.trim();
//								logger.info("Value of Search String in MAP" + componentSubComponentSearchStr);
								JsonNode componentSubComponentMAPValue = componentSubComponentJsonMAP
										.get(componentSubComponentSearchStr);
//								logger.info("Value After Search" + componentSubComponentMAPValue);
								/*-------------------------------------------------------------------------*/
								if (componentSubComponentMAPValue != null) {
									logger.info("Result Found");
									String serviceName = null;
									JsonNode inputParameters = null;
									if (componentSubComponentMAPValue.get("serviceName") != null) {
										serviceName = componentSubComponentMAPValue.get("serviceName").asText();
									}
									if (componentSubComponentMAPValue.get("inputParams") != null) {
										inputParameters = componentSubComponentMAPValue.get("inputParams");
									}
									/*
									 * Split serviceName on comma(,) delimiter
									 */
									String[] serviceList = serviceName.split(",");
//									logger.info("Service Array value" + serviceList.toString());
//									logger.info("Size of Service Array" + serviceList.length);
									for (int l = 0; l < serviceList.length; l++) {
										List<JsonNode> records = makeOnlineRouterResult(mapper, components, k,
												componentName, subComponentName, serviceList[l], inputParameters,
												onlineVerificationChecksList, onlineAttemptsPOJO,candidateName, resultMap);
									}
								} else {
									logger.info(
											"Result Not Found! Check for next Component and Sub Component Combination");
									continue;
								}
								// List<JsonNode> records = makeOnlineRouterResult(mapper, components, k,
								// componentName,componentSubComponentMAPValueResult);
							}
							if (CollectionUtils.isNotEmpty(onlineVerificationChecksList)) {
								OnlineManualVerification onlineManualVerification = new OnlineManualVerification();
								onlineManualVerification.setCandidateName(candidateName);
								onlineManualVerification.setCaseNumber(caseNo);
								onlineManualVerification.setPackageName(packageName);
								onlineManualVerification.setClientName(clientName);
								onlineManualVerification.setSBU(clientSBU);
								onlineManualVerification.setCrn(crn);
								onlineManualVerification.setStatus("Manual");
								onlineManualVerification.setTimeCreation(new Date().toString());
								onlineManualVerification.setUpdatedTime(new Date().toString());
								onlineManualVerification.setOnlineVerificationChecksList(onlineVerificationChecksList);
								// Convert Object to JsonNode
								JsonNode onlineManualVerificationNode = mapper.convertValue(onlineManualVerification,
										JsonNode.class);
								String onlineManualVerificationStr = onlineManualVerificationNode.toString();
								logger.info("Value Send to API" + onlineManualVerificationStr);
								/*
								 * Call Api to save this Information
								 */
								String response = onlineApiService.sendDataToRestUrl(verificationOnlineManualUIUrl,
										onlineManualVerificationStr);
//								logger.info("Response" + response);
							} else {
								logger.info("List is empty. All Online Request Resutl is Clear");
							}
						} else {
							logger.info("Map is empty! Please check Rest End Point call or Database is empty.");
						}
					} else {
						logger.info("Component Wise API Response is empty or null");
					}

//					caseSpecificInfo = onlineAttemptsPOJO.getCaseSpecificInfoPOJO();
//					JsonNode caseSpecificInfoNode = mapper.convertValue(caseSpecificInfo, JsonNode.class);
//					String caseSpecificInfoStr = caseSpecificInfoNode.toString();
//					logger.info("caseSpecificInfo value:{}",caseSpecificInfoStr);
//					onlineApiService.sendDataToAttempt(caseSpecificInfoUrl, caseSpecificInfoStr);
					
					checkIdList = onlineAttemptsPOJO.getCheckIdList();
					checkIdList = new ArrayList<>(new HashSet<>(checkIdList));
					
					onlineApiService.sendDataToL3ByCheckId(verificationStatusUrlL3, checkIdList);
				}
				// JsonObject = new JsonObject();
				onlineResponse = mapper.writeValueAsString(onlineReq);
				dataObj = mapper.readTree(onlineResponse);
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

			onlineResponse = mapper.writeValueAsString(dataObj);
			String returnStr = onlineResponse;
			returnStr = onlineResponse.replace("taskSpecs", "result");
//			logger.debug("Response:\n" + returnStr);
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

	private Map<String, JsonNode> jsonNodeToMap(JsonNode componentWiseAPINode) {
		Map<String, JsonNode> componentSubComponentJsonMAP = new HashMap<String, JsonNode>();
		if (componentWiseAPINode != null && componentWiseAPINode.isArray()) {
			ArrayNode componentWiseAPINodeList = (ArrayNode) componentWiseAPINode;
			for (int k = 0; k < componentWiseAPINodeList.size(); k++) {
				String componentName = null, subComponentName = null;
				if (!componentWiseAPINodeList.get(k).get("componentName").isNull()) {
					componentName = componentWiseAPINodeList.get(k).get("componentName").asText().trim();
				}
				if (!componentWiseAPINodeList.get(k).get("productName").isNull()) {
					subComponentName = componentWiseAPINodeList.get(k).get("productName").asText().trim();
				}
				String componentSubComponentJsonMAPKey = componentName + "##" + subComponentName;
				JsonNode componentSubComponentJsonMAPValue = componentWiseAPINodeList.get(k);
				componentSubComponentJsonMAP.put(componentSubComponentJsonMAPKey, componentSubComponentJsonMAPValue);
			}
		}
		return componentSubComponentJsonMAP;
	}
	
//	private Map<String, String> getDetailsFromDataEntry(String dataEntryResponse) {
	private Map<String, String> getDetailsFromDataEntry(OnlineReq onlineReq) {

		Map<String, String> resultMapList = new HashMap<>();
		String caseNumber = onlineReq.getMetadata().getTxLabel() != null ? onlineReq.getMetadata().getTxLabel() : "";
		
		if (!StringUtils.isEmpty(caseNumber)) {
			String dataEntryURL = l3DataEntryURL + caseNumber;
			String dataEntryResponse = onlineApiService.getRequestFromL3(dataEntryURL);

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			JsonNode objectNode = mapper.createObjectNode();

			if (dataEntryResponse != null) {
				try {
					objectNode = mapper.readTree(dataEntryResponse);
				} catch (JsonProcessingException e) {
					logger.error("Error : {}", e.getMessage());
					e.printStackTrace();
				}
			}

			JsonNode responseNode = objectNode.has("response") ? objectNode.get("response") : mapper.createObjectNode();
			JsonNode deDataNode = responseNode.has("deData") ? responseNode.get("deData") : mapper.createObjectNode();
			JsonNode personalDetailsNode = deDataNode.has("personaldetailsasperbvf")
					? deDataNode.get("personaldetailsasperbvf")
					: mapper.createObjectNode();
			JsonNode passportNode = deDataNode.has("passportasperdocument") ? deDataNode.get("passportasperdocument")
					: mapper.createObjectNode();
			JsonNode panCardNode = deDataNode.has("pancardasperdocument") ? deDataNode.get("pancardasperdocument")
					: mapper.createObjectNode();
			JsonNode dlNode = deDataNode.has("drivinglicenseasperdocument") ? deDataNode.get("drivinglicenseasperdocument")
					: mapper.createObjectNode();
			JsonNode voterIdNode = deDataNode.has("voteridasperdocument") ? deDataNode.get("voteridasperdocument")
					: mapper.createObjectNode();

			String primaryName = personalDetailsNode.has("candidatename")
					? personalDetailsNode.get("candidatename").asText()
					: "";
			String fathersName = personalDetailsNode.has("fathersname") ? personalDetailsNode.get("fathersname").asText()
					: "";
			String dob = personalDetailsNode.has("dob") ? personalDetailsNode.get("dob").asText() : "";
			String passportName = passportNode.has("candidatename") ? passportNode.get("candidatename").asText() : "";
			String panName = panCardNode.has("nameasperpancard") ? panCardNode.get("nameasperpancard").asText() : "";
			String dlName = dlNode.has("candidatename") ? dlNode.get("candidatename").asText() : "";
			String voterIdName = voterIdNode.has("candidatename") ? voterIdNode.get("candidatename").asText() : "";

			String secondaryName = getSecondaryName(primaryName, passportName, panName, dlName, voterIdName);
			String address = "";

			resultMapList.put("primaryName", primaryName);
			resultMapList.put("secondaryName", secondaryName);
			resultMapList.put("dob", dob);
			resultMapList.put("fathersName", fathersName);
			resultMapList.put("address", address);
		}

		return resultMapList;		
	}
	
	private Map<String, String> getDetailsFromJson(OnlineReq onlineReq) {
		
		String primaryName = "";
		String secondaryName = "";
		String dob = "";
		String fathersName = "";
		String address = "";
		
		Map<String, String> resultMapList = new HashMap<>();
		
		for (Datum data : onlineReq.getData()) {
			List<ComponentScoping> componentScopingList = data.getTaskSpecs().getComponentScoping();
			for (ComponentScoping componentScoping : componentScopingList) {
				for (Component component : componentScoping.getComponents()) {
					if (StringUtils.equalsIgnoreCase(component.getComponentname(), "Personal Information")) {
						List<JsonNode> records = component.getRecords();
						
						for (JsonNode record : records) {
							if (StringUtils.isEmpty(primaryName)) {
								primaryName = record.has("Candidate Name") ? record.get("Candidate Name").asText() : "";
							}
							if (StringUtils.isEmpty(dob)) {
								dob = record.has("DOB") ? record.get("DOB").asText() : "";
							}
							if (StringUtils.isEmpty(fathersName)) {
								fathersName = record.has("Father’s Name") ? record.get("Father’s Name").asText() : "";
							}
						}
					}
				}
			}
		}
		resultMapList.put("primaryName", primaryName);
		resultMapList.put("secondaryName", secondaryName);
		resultMapList.put("dob", dob);
		resultMapList.put("fathersName", fathersName);
		resultMapList.put("address", address);
		
		return resultMapList;
	}
	
	@GetMapping("/test-name")
	public String getSecondaryName(String primaryName, String passportName, String panName, String dlName, String voterIdName) {

		String secondaryName = "";
		String educationName = "";
		
		String[] primaryNameList = primaryName.split(" ");
		boolean passportflag = true;
		boolean panflag = true;
		boolean dlflag = true;
		boolean voterflag = true;
		boolean educationflag = true;
		for (int idx = 0; idx < primaryNameList.length; idx++) {
			if (!passportName.contains(primaryNameList[idx])) {
				passportflag = false;
			}
			if (!panName.contains(primaryNameList[idx])) {
				panflag = false;
			}
			if (!dlName.contains(primaryNameList[idx])) {
				dlflag = false;
			}
			if (!voterIdName.contains(primaryNameList[idx])) {
				voterflag = false;
			}
			if (!educationName.contains(primaryNameList[idx])) {
				educationflag = false;
			}
		}
		int passportLength = passportflag ? passportName.trim().length() : 0;
		int panLength = panflag ? panName.trim().length() : 0;
		int dlLength = dlflag ? dlName.trim().length() : 0;
		int voterIdLength = voterflag ? voterIdName.trim().length() : 0;
		int educationLength = educationflag ? educationName.trim().length() : 0;
		
		int maxVal = Stream.of(passportLength, panLength, dlLength, voterIdLength, educationLength).max(Integer::compareTo).get();
		
		if (passportLength == maxVal) {
			secondaryName = passportName;
		} else if (panLength == maxVal) {
			secondaryName = panName;
		} else if (dlLength == maxVal) {
			secondaryName = dlName;
		} else if (voterIdLength == maxVal) {
			secondaryName = voterIdName;
		} else if (educationLength == maxVal) {
			secondaryName = educationName;
		}
		return secondaryName;
	}

	private List<JsonNode> makeOnlineRouterResult(ObjectMapper mapper, List<Component> components, int k,
			String componentName, String subComponentName, String serviceName, JsonNode inputParams,
			List<OnlineVerificationChecks> onlineVerificationChecksList, OnlineAttemptsPOJO onlineAttemptsPOJO,String candidateName, Map<String, String> resultMap)
			throws JsonProcessingException, JsonMappingException {
		/*
		 * Check InputParameter for ArrayNode and iterate over record list and make
		 * search string
		 */
		/*
		 * Manupatra Watchout, Loan Defaulter (Cibil), MCA, Google, Worldcheck
		 * Manupatra, Adverse Media, Worldcheck Worldcheck Worldcheck, Adverse Media
		 * Service Name and Their input MCA ---->Input: Candidate First Name only 
		 * Candidate DOB ( DD/MM/YYYY) WatchOut ---->Input: Candidate Full Name  DIN #
		 * from output of Step 1  PAN # when available from DE Loan Defaulter
		 * ---->Input: Candidate Full Name  Address Worldcheck API ---->Input:
		 * Candidate Full Name  Candidate DOB ( DD/MM/ YYYY)  Father’s Name  PAN #
		 * when Available from DE and FD: Identity Document Action: Match DIN from Cross
		 * Directorship from Step 1(MCA Output) Manupatra API ---->Input: Candidate
		 * Full Name  ADDRESS  Father’s name Google API(Adverse)-->Input: Candidate
		 * Full Name  Negative Key Words  Father’s name  Address
		 */
		// Should Check for All Record or Run for one and append result in all Record
		List<JsonNode> records = components.get(k).getRecords();
		onlineAttemptsPOJO.setComponent(components.get(k));
//		logger.info("Service Name Inside MakeOnlineRouter " + serviceName);
		ArrayNode inputParamsList = mapper.createArrayNode();
		/* if(serviceName.equalsIgnoreCase("credit_reputational_mca")) { */
		if (serviceName.trim().equalsIgnoreCase("MCA")) {
//			logger.info("if reputational MCA");
			String recordSearchName = null, recordSearchDOB = null;
			if (inputParams.isArray()) {
				inputParamsList = (ArrayNode) inputParams;
				String ngFieldNameStr = null, apiFieldNameStr = null;
				for (int i = 0; i < inputParamsList.size(); i++) {
					if (inputParamsList.get(i).get("ngFieldName") != null) {
						ngFieldNameStr = inputParamsList.get(i).get("ngFieldName").asText();
					}
					if (inputParamsList.get(i).get("apiFieldName") != null) {
						apiFieldNameStr = inputParamsList.get(i).get("apiFieldName").asText();
					}
					if (apiFieldNameStr.equalsIgnoreCase("name")) {
						recordSearchName = ngFieldNameStr;
					}
					if (apiFieldNameStr.equalsIgnoreCase("dob")) {
						recordSearchDOB = ngFieldNameStr;
					}
				}
				/*
				 * System.out.println("Value of api FieldName"+apiFieldNameStr);
				 * System.out.println("Value of ng FieldName"+ngFieldNameStr);
				 * System.out.println("Value of api recordSearchName"+recordSearchName);
				 * System.out.println("Value of ng recordSearchDOB"+recordSearchDOB);
				 */
			}
			/*
			 * Logic For Iterating the record and Assigning the Value to Final Return Json
			 * (MCA Result)
			 */
			if (records.size() > 0) {
				String dinStr = processMCAAPIRecordWise(mapper, serviceName, records, recordSearchName, recordSearchDOB,
						false, onlineVerificationChecksList, onlineAttemptsPOJO,candidateName,subComponentName, resultMap);
				logger.info("Value of Din" + dinStr);
			} else {
				logger.info("Record is Empty");
			}
		} /* else if(serviceName.equalsIgnoreCase("credit_reputational_cibil")) { */
		else if (serviceName.trim().equalsIgnoreCase("Loan Defaulter (Cibil)")) {
//			logger.info("else if reputational cibil(LOAN)");
			String recordSearchName = null, recordSearchAddress = null;
			if (inputParams.isArray()) {
				inputParamsList = (ArrayNode) inputParams;
				String ngFieldNameStr = null, apiFieldNameStr = null;
				for (int i = 0; i < inputParamsList.size(); i++) {
					if (inputParamsList.get(i).get("ngFieldName") != null) {
						ngFieldNameStr = inputParamsList.get(i).get("ngFieldName").asText();
					}
					if (inputParamsList.get(i).get("apiFieldName") != null) {
						apiFieldNameStr = inputParamsList.get(i).get("apiFieldName").asText();
					}
					if (apiFieldNameStr.equalsIgnoreCase("name")) {
						recordSearchName = ngFieldNameStr;
					}
					if (apiFieldNameStr.equalsIgnoreCase("address")) {
						recordSearchAddress = ngFieldNameStr;
					}
				}
				/*
				 * System.out.println("Value of api FieldName"+apiFieldNameStr);
				 * System.out.println("Value of ng FieldName"+ngFieldNameStr);
				 * System.out.println("Value of api recordSearchName"+recordSearchName);
				 * System.out.println("Value of ng recordSearchAddress"+recordSearchAddress);
				 */
			}
			/*
			 * Logic For Iterating the record and Assigning the Value to Final Return Json
			 * (LOAN Result)
			 */
			if (records.size() > 0) {
				processLoanAPIRecordWise(mapper, serviceName, records, recordSearchName, recordSearchAddress,
						onlineVerificationChecksList, onlineAttemptsPOJO,candidateName,subComponentName, resultMap);
				logger.info("Calling Process Loan");
			} else {
				logger.info("Record is Empty");
			}
		} /* else if(serviceName.equalsIgnoreCase("adverse_media")) { */
		else if (serviceName.trim().equalsIgnoreCase("Adverse Media")
				|| serviceName.trim().equalsIgnoreCase("Google")) {
			/*
			 * Negative Key Words: What is the field From L3 Which Map to Negative Key
			 * Word.*
			 */
			logger.info("else if Adverse Media");
			String recordSearchName = null, recordSearchAddress = null, recordSearchFatherName = null;
			if (inputParams.isArray()) {
				inputParamsList = (ArrayNode) inputParams;
				String ngFieldNameStr = null, apiFieldNameStr = null;
				for (int i = 0; i < inputParamsList.size(); i++) {
					if (inputParamsList.get(i).get("ngFieldName") != null) {
						ngFieldNameStr = inputParamsList.get(i).get("ngFieldName").asText();
					}
					if (inputParamsList.get(i).get("apiFieldName") != null) {
						apiFieldNameStr = inputParamsList.get(i).get("apiFieldName").asText();
					}
					if (apiFieldNameStr.equalsIgnoreCase("name")) {
						recordSearchName = ngFieldNameStr;
					}
					if (apiFieldNameStr.equalsIgnoreCase("address")) {
						recordSearchAddress = ngFieldNameStr;
					}
					if (apiFieldNameStr.equalsIgnoreCase("father_name")) {
						recordSearchFatherName = ngFieldNameStr;
					}
				}
				/*
				 * System.out.println("Value of api FieldName"+apiFieldNameStr);
				 * System.out.println("Value of ng FieldName"+ngFieldNameStr);
				 * System.out.println("Value of api recordSearchName"+recordSearchName);
				 * System.out.println("Value of ng recordSearchAddress"+recordSearchAddress);
				 */
			}
			/*
			 * Logic For Iterating the record and Assigning the Value to Final Return Json
			 * (LOAN Result)
			 */
			if (records.size() > 0) {
				logger.info("Calling Process Adverse media");
				processAdverseAPIRecordWise(mapper, serviceName, records, recordSearchName, recordSearchAddress,
						recordSearchFatherName, onlineVerificationChecksList, onlineAttemptsPOJO,candidateName,subComponentName, resultMap);
			} else {
				logger.info("Record is Empty");
			}

		} /* else if(serviceName.equalsIgnoreCase("watchout")) { */
		else if (serviceName.trim().equalsIgnoreCase("Watchout")) {
			/*
			 * First Call to MCA and get din from MCA API. Call watchOut. Also take PAN #
			 * when Available from DE
			 */
			logger.info("else if watch out");
			String recordSearchName = null, recordSearchDOB = null;
			if (inputParams.isArray()) {
				inputParamsList = (ArrayNode) inputParams;
				String ngFieldNameStr = null, apiFieldNameStr = null;
				for (int i = 0; i < inputParamsList.size(); i++) {
					if (inputParamsList.get(i).get("ngFieldName") != null) {
						ngFieldNameStr = inputParamsList.get(i).get("ngFieldName").asText();
					}
					if (inputParamsList.get(i).get("apiFieldName") != null) {
						apiFieldNameStr = inputParamsList.get(i).get("apiFieldName").asText();
					}
					if (apiFieldNameStr.equalsIgnoreCase("name")) {
						recordSearchName = ngFieldNameStr;
					}
					if (apiFieldNameStr.equalsIgnoreCase("dob")) {
						recordSearchDOB = ngFieldNameStr;
					}
				}
				/*
				 * System.out.println("Value of api FieldName"+apiFieldNameStr);
				 * System.out.println("Value of ng FieldName"+ngFieldNameStr);
				 * System.out.println("Value of api recordSearchName"+recordSearchName);
				 * System.out.println("Value of ng recordSearchDOB"+recordSearchDOB);
				 */
			}
			/*
			 * Logic For Iterating the record and Assigning the Value to Final Return Json
			 * (MCA Result)
			 */
			String dinStr = null;
			if (records.size() > 0) {
				/*-------------------------Call To MCA-------------------------*/
				logger.info("Calling MCA API and get din");
				Boolean flag = true;
//				dinStr = processMCAAPIRecordWise(mapper, serviceName, records, recordSearchName, recordSearchDOB, flag,
//						onlineVerificationChecksList, onlineAttemptsPOJO,subComponentName);
				/*-------------------------MCA Return DIN-------------------------*/
//				logger.info("Value of Din" + dinStr);
				/*-------------------------Make call to Watchout-----------------*/
				logger.info("Calling Watch Out");
				processWatchoutAPIRecordWise(mapper, serviceName, records, recordSearchName, recordSearchDOB, dinStr,
						onlineVerificationChecksList, onlineAttemptsPOJO,candidateName,subComponentName, resultMap);
			} else {
				logger.info("Record is Empty");
			}
		} /* else if(serviceName.equalsIgnoreCase("manupatra")) { */
		else if (serviceName.trim().equalsIgnoreCase("Manupatra")) {
			/*
			 * Candidate Full Name, father's Name and Address
			 */
			logger.info("else if Manupatra");
			String recordSearchName = null, recordSearchAddress = null, recordSearchFatherName = null,
					recordSearchStartDate = null;
			if (inputParams.isArray()) {
				inputParamsList = (ArrayNode) inputParams;
				String ngFieldNameStr = null, apiFieldNameStr = null;
				for (int i = 0; i < inputParamsList.size(); i++) {
					if (inputParamsList.get(i).get("ngFieldName") != null) {
						ngFieldNameStr = inputParamsList.get(i).get("ngFieldName").asText();
					}
					if (inputParamsList.get(i).get("apiFieldName") != null) {
						apiFieldNameStr = inputParamsList.get(i).get("apiFieldName").asText();
					}
					if (apiFieldNameStr.equalsIgnoreCase("name")) {
						recordSearchName = ngFieldNameStr;
					}
					if (apiFieldNameStr.equalsIgnoreCase("address")) {// Should be Changed to State
						recordSearchAddress = ngFieldNameStr;
					}
					if (apiFieldNameStr.equalsIgnoreCase("father_name")) {
						recordSearchFatherName = ngFieldNameStr;
					}
					if (apiFieldNameStr.equalsIgnoreCase("startdate")) {
						recordSearchStartDate = ngFieldNameStr;
					}
				}
				/*
				 * System.out.println("Value of api FieldName"+apiFieldNameStr);
				 * System.out.println("Value of ng FieldName"+ngFieldNameStr);
				 * System.out.println("Value of api recordSearchName"+recordSearchName);
				 * System.out.println("Value of ng recordSearchAddress"+recordSearchAddress);
				 */
			}
			/*
			 * Logic For Iterating the record and Assigning the Value to Final Return Json
			 * (LOAN Result)
			 */
			if (records.size() > 0) {
				logger.info("Calling Manupatra API");
				processManupatraAPIRecordWise(mapper, serviceName, records, recordSearchName, recordSearchAddress,
						recordSearchFatherName, recordSearchStartDate, onlineVerificationChecksList, 
						onlineAttemptsPOJO,candidateName,subComponentName, resultMap);
			} else {
				logger.info("Record is Empty");
			}
		} /* else if(serviceName.equalsIgnoreCase("worldcheck")) { */
		else if (serviceName.trim().equalsIgnoreCase("Worldcheck")) {
			/*
			 * First Call to MCA and get din from MCA API. Call watchOut. Also take PAN #
			 * when Available from DE
			 */
			logger.info("else if WorldCheck");
			String recordSearchName = null, recordSearchDOB = null, recordSearchFatherName = null,
					recordSearchAddress = null;
			if (inputParams.isArray()) {
				inputParamsList = (ArrayNode) inputParams;
				String ngFieldNameStr = null, apiFieldNameStr = null;
				for (int i = 0; i < inputParamsList.size(); i++) {
					if (inputParamsList.get(i).get("ngFieldName") != null) {
						ngFieldNameStr = inputParamsList.get(i).get("ngFieldName").asText();
					}
					if (inputParamsList.get(i).get("apiFieldName") != null) {
						apiFieldNameStr = inputParamsList.get(i).get("apiFieldName").asText();
					}
					if (apiFieldNameStr.equalsIgnoreCase("name")) {
						recordSearchName = ngFieldNameStr;
					}
					if (apiFieldNameStr.equalsIgnoreCase("dob")) {
						recordSearchDOB = ngFieldNameStr;
					}
					if (apiFieldNameStr.equalsIgnoreCase("father_name")) {
						recordSearchFatherName = ngFieldNameStr;
					}
					if (apiFieldNameStr.equalsIgnoreCase("father_name")) {
						recordSearchAddress = ngFieldNameStr;
					}
				}
				
				/*
				 * System.out.println("Value of api FieldName"+apiFieldNameStr);
				 * System.out.println("Value of ng FieldName"+ngFieldNameStr);
				 * System.out.println("Value of api recordSearchName"+recordSearchName);
				 * System.out.println("Value of ng recordSearchDOB"+recordSearchDOB);
				 */
			}
			/*
			 * Logic For Iterating the record and Assigning the Value to Final Return Json
			 * (MCA Result)
			 */
			String dinStr = null;
			if (records.size() > 0) {
				/*-------------------------Call To MCA-------------------------*/
				logger.info("Calling MCA API and get din");
				Boolean flag = true;
				/*
				 * dinStr = processMCAAPIRecordWise(mapper, serviceName, records,
				 * recordSearchName, recordSearchDOB, flag, onlineVerificationChecksList,
				 * onlineAttemptsPOJO,candidateName,subComponentName);
				 */
				/*-------------------------MCA Return DIN-------------------------*/
//				logger.info("Value of Din" + dinStr);
				/*-------------------------Make call to WorldCheck-----------------*/
				/*
				 * Candidate Full Name Candidate DOB ( DD/MM/ YYYY) Father’s Name PAN # when
				 * Available from DE
				 */
				logger.info("Calling World Check");
				processWorldCheckAPIRecordWise(mapper, serviceName, records, recordSearchName, recordSearchDOB, dinStr,
						recordSearchFatherName, recordSearchAddress, onlineVerificationChecksList, 
						onlineAttemptsPOJO,candidateName,subComponentName, resultMap);
			} else {
				logger.info("Record is Empty");
			}

		}
		return records;
	}

	private String processMCAAPIRecordWise(ObjectMapper mapper, String serviceName, List<JsonNode> records,
			String recordSearchName, String recordSearchDOB, Boolean flag,
			List<OnlineVerificationChecks> onlineVerificationChecksList, OnlineAttemptsPOJO onlineAttemptsPOJO,String candidateName,String subComponentName, Map<String, String> resultMap)
			throws JsonProcessingException, JsonMappingException {
		String dinStr = null;
		logger.info("Inside MCA API and Making Result");
		List<CaseSpecificRecordDetailPOJO> caseSpecificRecordDetailPOJOs = new ArrayList<>();

		for (int l = 0; l < records.size(); l++) {
			JsonNode recordNode = records.get(l);
			ArrayNode resultArray = mapper.createArrayNode();
			if (recordNode.get("ruleResult").isArray()) {
				resultArray = (ArrayNode) recordNode.get("ruleResult").get(0).get("result");
			} else {
				resultArray = (ArrayNode) recordNode.get("ruleResult").get("result");
			}
			if (resultArray.get(0).toString().contains("Include")) {
				logger.info("Make an empty Json Array");
				ArrayNode ruleResultJsonNode = mapper.createArrayNode();
				if (!recordNode.get("ruleResult").isArray()) {
					logger.info("Write engine element as scopingEngine");
					logger.info("ruleresult is not Array! Make Array And Assign the Scoping Engine");
					((ObjectNode) recordNode.get("ruleResult")).put("engine", "ScopingEngine");
					ruleResultJsonNode.add(recordNode.get("ruleResult"));
					logger.info("Make ruleResult array");
					((ObjectNode) recordNode).set("ruleResult", ruleResultJsonNode);
					/*
					 * Add Logic for adding Online Engine
					 */
					((ObjectNode) recordNode.get("ruleResult")).put("engine", "onlineResult");
					((ObjectNode) recordNode.get("ruleResult")).put("success", true);
					((ObjectNode) recordNode.get("ruleResult")).put("message", "SUCCEEDED");
					((ObjectNode) recordNode.get("ruleResult")).put("status", 200);

				} else {
					logger.info("ruleresult is Array!Do next process");
				}
				/*
				 * Look for "engine":"onlineResult"
				 */
				ObjectNode onlineResultNode = mapper.createObjectNode();
				ArrayNode ruleResultArrNode = (ArrayNode) records.get(l).get("ruleResult");
				
				for (int i = 0; i < ruleResultArrNode.size(); i++) {
					JsonNode engineJsonNode = ruleResultArrNode.get(i);
					if (engineJsonNode.has("engine")) {
						String engineName = engineJsonNode.get("engine").asText();
						if (engineName.equalsIgnoreCase("onlineResult")) {
							onlineResultNode = (ObjectNode) engineJsonNode;
							break;
						}
					}
				}
				String personalSearchString = null, personalResponse = null;
				ObjectNode resultMCA = mapper.createObjectNode();

				JsonNode DOB = recordNode.get(recordSearchDOB);
				
				JsonNode din = recordNode.get("din");
				//JsonNode candidateName = recordNode.get(recordSearchName);
				// New Logic for Calling Stand Alone Api
				ObjectNode personalInfo = mapper.createObjectNode();
//				if (candidateName != null) {
//					personalInfo.put("name", candidateName);
//				} else {
//					personalInfo.put("name", "Sophia");
//				}
//				if (DOB != null) {
//					personalInfo.set("dob", DOB);
//				} else {
//					personalInfo.put("dob", "15-02-1994");
//				}
				personalInfo.put("name", resultMap.get("primaryName"));
				personalInfo.put("dob", resultMap.get("dob"));
				personalInfo.put("father_name", resultMap.get("fathersName"));

//				logger.info("MCA Input String" + personalInfo.toString());
				/*
				 * Logic for Request Initiated Attempt
				 * 
				 */
				String checkId=recordNode.get("checkId")!=null?recordNode.get("checkId").asText():"";
				caseSpecificRecordDetailPOJOs = saveIntiatedAttempt(checkId, onlineAttemptsPOJO, mapper, 
						caseSpecificRecordDetailPOJOs);

				try {
					personalResponse = onlineApiService.sendDataToRestUrl(mcaRestUrl, personalInfo.toString());
				} catch (Exception e) {
					logger.info("Exception" + e.getMessage());
				}

//				logger.info("MCA Response" + personalInfo.toString());
				onlineResultNode.put("MCA Result", personalResponse);
				((ArrayNode) recordNode.get("ruleResult")).add(onlineResultNode);
				/*
				 * Logic for Saving Manual Result to Database
				 */
				if (personalResponse != null) {
					JsonNode personalResponseNode = mapper.readTree(personalResponse);
					if (personalResponseNode != null && personalResponseNode.has("MCA")) {
						JsonNode mcaResponseNode = personalResponseNode.get("MCA");
						if (mcaResponseNode != null && mcaResponseNode.has("status")) {
							// if(!mcaResponseNode.get("status").asText().equalsIgnoreCase("clear")) {
							/*
							 * Make Entry to database
							 */
							OnlineVerificationChecks onlineVerificationChecks = new OnlineVerificationChecks();
							
							if (StringUtils.isNotEmpty(checkId)) {
								List<String> checkIdList = onlineAttemptsPOJO.getCheckIdList();
								checkIdList.add(checkId);
								onlineAttemptsPOJO.setCheckIdList(checkIdList);
								
								onlineVerificationChecks.setCheckId(checkId);
							}
							onlineVerificationChecks.setApiName("MCA");
							onlineVerificationChecks.setInitialResult(mcaResponseNode.get("status").asText());
							onlineVerificationChecks.setResult(mcaResponseNode.get("status").asText());
							if (mcaResponseNode.get("MCA Output") != null) {
								onlineVerificationChecks
										.setMatchedIdentifiers(mcaResponseNode.get("MCA Output").asText());

							}
							if (mcaResponseNode.get("Raw Output") != null) {
								onlineVerificationChecks.setOutputFile(mcaResponseNode.get("Raw Output").asText());
							}
							if (mcaResponseNode.get("MCA Input") != null) {
								onlineVerificationChecks.setInputFile(mcaResponseNode.get("MCA Input").asText());
							}
							onlineVerificationChecks.setCreatedDate(new Date().toString());
							onlineVerificationChecks.setUpdatedDate(new Date().toString());
							onlineVerificationChecksList.add(onlineVerificationChecks);
//							logger.info("Size of List in MCA API:", onlineVerificationChecksList.size());
							// }
							/*
							 * Logic for Saving Attempt
							 */
							onlineAttemptsPOJO.setApiName("MCA");
							onlineAttemptsPOJO.setProductName(subComponentName);
							if (mcaResponseNode.get("status").asText().equalsIgnoreCase("clear")) {
								/*
								 * Logic for Saving clear attempt from excel file
								 */
								saveClearOutcomeAttempt( checkId, mapper, onlineAttemptsPOJO);
							} else if (mcaResponseNode.get("status").asText().equalsIgnoreCase("Record Found")) {
								/*
								 * Logic for Saving Manual attempt from excel file
								 */
								saveDiscrepantProcessAttempt(checkId, mapper, onlineAttemptsPOJO);
							}else if(mcaResponseNode.get("status").asText().equalsIgnoreCase("Manual")) {
								saveReviewProcessAttempt(checkId, mapper, onlineAttemptsPOJO);
							}
						}
					}
				}
			} else {
				logger.info("Include not found");
			}
		}

		CaseSpecificInfoPOJO caseSpecificInfoPOJO = onlineAttemptsPOJO.getCaseSpecificInfoPOJO();
		if(caseSpecificInfoPOJO.getCaseSpecificRecordDetail()!=null) {
			caseSpecificRecordDetailPOJOs.addAll(caseSpecificInfoPOJO.getCaseSpecificRecordDetail());
		}
		caseSpecificInfoPOJO.setCaseSpecificRecordDetail(caseSpecificRecordDetailPOJOs);
		onlineAttemptsPOJO.setCaseSpecificInfoPOJO(caseSpecificInfoPOJO);
		return dinStr;
	}

	private void saveAttempt(ObjectMapper mapper, AttemptHistory attemptHistory, AttemptStatusData attemptStatusData) {

		try {
			JsonNode attemptHistoryJsonNode = mapper.valueToTree(attemptHistory);
//			logger.info("Value of attemptHistory bean to Json : {}", attemptHistoryJsonNode);
			String attemptHistoryStr = mapper.writeValueAsString(attemptHistoryJsonNode);
//			logger.info("Value of attemptHistory Json to String : {}", attemptHistoryStr);

			String attemptHistoryResponse = null;
			AttemptHistory attemptHistoryNew = new AttemptHistory();
			try {
				attemptHistoryResponse = onlineApiService.sendDataToAttempt(spocAttemptHistoryRestUrl,
						attemptHistoryStr);
//				logger.info("Attempt History Response : {}", attemptHistoryResponse);
				attemptHistoryNew = mapper.readValue(attemptHistoryResponse, new TypeReference<AttemptHistory>() {
				});
				if (attemptHistoryNew.getAttemptid() != null) {
					logger.info("Attempt saved sucessfully. : {}", attemptHistoryNew.getAttemptid());

					attemptStatusData.setAttemptId(attemptHistoryNew.getAttemptid());

					JsonNode attemptStatusDataJsonNode = mapper.valueToTree(attemptStatusData);
//					logger.info("Value of attemptStatusData bean to Json : {}", attemptStatusDataJsonNode);
					String attemptStatusDataStr = mapper.writeValueAsString(attemptStatusDataJsonNode);
//					logger.info("Value of attemptStatusData Json to String : {}", attemptStatusDataStr);

					String attemptStatusDataResponse = null;
					AttemptStatusData attemptStatusDataNew = new AttemptStatusData();

					try {
						attemptStatusDataResponse = onlineApiService.sendDataToAttempt(spocAttemptStatusDataRestUrl,
								attemptStatusDataStr);
//						logger.info("Attempt status data Response : {}", attemptStatusDataResponse);
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

	private void processLoanAPIRecordWise(ObjectMapper mapper, String serviceName, List<JsonNode> records,
			String recordSearchName, String recordSearchAddress,
			List<OnlineVerificationChecks> onlineVerificationChecksList, OnlineAttemptsPOJO onlineAttemptsPOJO,
			String candidateName,String subComponentName, Map<String, String> resultMap)
			throws JsonProcessingException, JsonMappingException {
		logger.info("Inside Loan API and Making Result");
		List<CaseSpecificRecordDetailPOJO> caseSpecificRecordDetailPOJOs = new ArrayList<>();
		for (int l = 0; l < records.size(); l++) {
			JsonNode recordNode = records.get(l);
			ArrayNode resultArray = mapper.createArrayNode();
			if (records.get(l).get("ruleResult").isArray()) {
				resultArray = (ArrayNode) records.get(l).get("ruleResult").get(0).get("result");
			} else {
				resultArray = (ArrayNode) records.get(l).get("ruleResult").get("result");
			}
			if (resultArray.get(0).toString().contains("Include")) {
				logger.info("Make an empty Json Array");
				ArrayNode ruleResultJsonNode = mapper.createArrayNode();
				if (!records.get(l).get("ruleResult").isArray()) {
					logger.info("Write engine element as scopingEngine");
					logger.info("ruleresult is not Array! Make Array And Assign the Scoping Engine");
					((ObjectNode) records.get(l).get("ruleResult")).put("engine", "ScopingEngine");
					ruleResultJsonNode.add(records.get(l).get("ruleResult"));
					logger.info("Make ruleResult array");
					((ObjectNode) records.get(l)).set("ruleResult", ruleResultJsonNode);

					/*
					 * Add Logic for adding Online Engine
					 */
					((ObjectNode) records.get(l).get("ruleResult")).put("engine", "onlineResult");
					((ObjectNode) records.get(l).get("ruleResult")).put("success", true);
					((ObjectNode) records.get(l).get("ruleResult")).put("message", "SUCCEEDED");
					((ObjectNode) records.get(l).get("ruleResult")).put("status", 200);

				} else {
					logger.info("ruleresult is Array!Do next process");
				}
				/*
				 * Look for "engine":"onlineResult"
				 */
				ObjectNode onlineResultNode = mapper.createObjectNode();
				ArrayNode ruleResultArrNode = (ArrayNode) records.get(l).get("ruleResult");
				for (int i = 0; i < ruleResultArrNode.size(); i++) {
					JsonNode engineJsonNode = ruleResultArrNode.get(i);
					if (engineJsonNode.has("engine")) {
						String engineName = engineJsonNode.get("engine").asText();
						if (engineName.equalsIgnoreCase("onlineResult")) {
							onlineResultNode = (ObjectNode) engineJsonNode;
							break;
						}
					}
				}
				String personalSearchString = null, personalResponse = null;
				ObjectNode resultLoan = mapper.createObjectNode();

				JsonNode address = records.get(l).get(recordSearchAddress);
				//JsonNode candidateName = records.get(l).get(recordSearchName);
				// New Logic for Calling Stand Alone Api
				ObjectNode personalInfo = mapper.createObjectNode();
//				if (candidateName != null) {
//					personalInfo.put("name", candidateName);
//				} else {
//					personalInfo.put("name", "RPA Testing");
//				}
//				if (address != null) {
//					personalInfo.set("address", address);
//				} else {
//					personalInfo.put("name", "Gurugram");
//				}

					personalInfo.put("name", resultMap.get("primaryName"));
					personalInfo.put("dob", resultMap.get("dob"));
					personalInfo.put("father_name", resultMap.get("fathersName"));
				
				
				// personalInfo.set("dob", DOB);
//				logger.info("Loan Input String" + personalInfo.toString());
				/*
				 * Logic for Request Initiated Attempt
				 */
				String checkId=recordNode.get("checkId")!=null?recordNode.get("checkId").asText():"";
				caseSpecificRecordDetailPOJOs = saveIntiatedAttempt(checkId, onlineAttemptsPOJO, mapper,
						caseSpecificRecordDetailPOJOs);

				try {
					personalResponse = onlineApiService.sendDataToRestUrl(loanRestUrl, personalInfo.toString());
				} catch (Exception e) {
					logger.info("Exception" + e.getMessage());
				}
//				logger.info("Loan Response" + personalInfo.toString());
				onlineResultNode.put("Loan Result", personalResponse);
				((ArrayNode) records.get(l).get("ruleResult")).add(onlineResultNode);

				/*
				 * Logic for Saving Manual Result to Database
				 */
				if (personalResponse != null) {
					JsonNode personalResponseNode = mapper.readTree(personalResponse);
					if (personalResponseNode != null && personalResponseNode.has("Loan Defaulter")) {
						JsonNode loanResponseNode = personalResponseNode.get("Loan Defaulter");
						if (loanResponseNode != null && loanResponseNode.has("status")) {
							// if(!loanResponseNode.get("status").asText().equalsIgnoreCase("clear")) {
							/*
							 * Make Entry to database
							 */
							OnlineVerificationChecks onlineVerificationChecks = new OnlineVerificationChecks();
							if (StringUtils.isNotEmpty(checkId))
							{
								List<String> checkIdList = onlineAttemptsPOJO.getCheckIdList();
								checkIdList.add(checkId);
								onlineAttemptsPOJO.setCheckIdList(checkIdList);
								onlineVerificationChecks.setCheckId(checkId);
							}
							onlineVerificationChecks.setApiName("Loan Defaulter");
							onlineVerificationChecks.setInitialResult(loanResponseNode.get("status").asText());
							onlineVerificationChecks.setResult(loanResponseNode.get("status").asText());
							if (loanResponseNode.get("Loan Defaulter Output") != null) {
								onlineVerificationChecks
										.setMatchedIdentifiers(loanResponseNode.get("Loan Defaulter Output").asText());
							}
							if (loanResponseNode.get("Raw Output") != null) {
								onlineVerificationChecks.setOutputFile(loanResponseNode.get("Raw Output").asText());
							}
							if (loanResponseNode.get("Loan Defaulter Input") != null) {
								onlineVerificationChecks
										.setInputFile(loanResponseNode.get("Loan Defaulter Input").asText());
							}
							onlineVerificationChecks.setCreatedDate(new Date().toString());
							onlineVerificationChecks.setUpdatedDate(new Date().toString());
							onlineVerificationChecksList.add(onlineVerificationChecks);
//							logger.info("Size of List in Loan Defaulter API:", onlineVerificationChecksList.size());
							// }
							/*
							 * Logic for Saving Attempt
							 */
							onlineAttemptsPOJO.setApiName("Loan Defaulter");
							onlineAttemptsPOJO.setProductName(subComponentName);
							if (loanResponseNode.get("status").asText().equalsIgnoreCase("clear")) {
								/*
								 * Logic for Saving clear attempt from excel file
								 */
								saveClearOutcomeAttempt(checkId, mapper, onlineAttemptsPOJO);
							} else if (loanResponseNode.get("status").asText().equalsIgnoreCase("Record Found")) {
								/*
								 * Logic for Saving Manual attempt from excel file
								 */
								saveDiscrepantProcessAttempt(checkId, mapper, onlineAttemptsPOJO);
							}else if(loanResponseNode.get("status").asText().equalsIgnoreCase("Manual")) {
								saveReviewProcessAttempt(checkId, mapper, onlineAttemptsPOJO);
							}
						}
					}
				}

			} else {
				logger.info("Include not found");
			}
		}
		CaseSpecificInfoPOJO caseSpecificInfoPOJO = onlineAttemptsPOJO.getCaseSpecificInfoPOJO();
		if(caseSpecificInfoPOJO.getCaseSpecificRecordDetail()!=null) {
			caseSpecificRecordDetailPOJOs.addAll(caseSpecificInfoPOJO.getCaseSpecificRecordDetail());
		}
		caseSpecificInfoPOJO.setCaseSpecificRecordDetail(caseSpecificRecordDetailPOJOs);
		onlineAttemptsPOJO.setCaseSpecificInfoPOJO(caseSpecificInfoPOJO);
	}

	private void processAdverseAPIRecordWise(ObjectMapper mapper, String serviceName, List<JsonNode> records,
			String recordSearchName, String recordSearchAddress, String recordSearchFatherName,
			List<OnlineVerificationChecks> onlineVerificationChecksList, 
			OnlineAttemptsPOJO onlineAttemptsPOJO,String candidateName,String subComponentName, Map<String, String> resultMap)
			throws JsonProcessingException, JsonMappingException {
		logger.info("Inside Adverse Media API and Making Result");
		List<CaseSpecificRecordDetailPOJO> caseSpecificRecordDetailPOJOs = new ArrayList<>();
		for (int l = 0; l < records.size(); l++) {
			JsonNode recordNode = records.get(l);
			ArrayNode resultArray = mapper.createArrayNode();
			if (records.get(l).get("ruleResult").isArray()) {
				resultArray = (ArrayNode) records.get(l).get("ruleResult").get(0).get("result");
			} else {
				resultArray = (ArrayNode) records.get(l).get("ruleResult").get("result");
			}
			if (resultArray.get(0).toString().contains("Include")) {
				logger.info("Make an empty Json Array");
				ArrayNode ruleResultJsonNode = mapper.createArrayNode();
				if (!records.get(l).get("ruleResult").isArray()) {
					logger.info("Write engine element as scopingEngine");
					logger.info("ruleresult is not Array! Make Array And Assign the Scoping Engine");
					((ObjectNode) records.get(l).get("ruleResult")).put("engine", "ScopingEngine");
					ruleResultJsonNode.add(records.get(l).get("ruleResult"));
					logger.info("Make ruleResult array");
					((ObjectNode) records.get(l)).set("ruleResult", ruleResultJsonNode);

					/*
					 * Add Logic for adding Online Engine
					 */
					((ObjectNode) records.get(l).get("ruleResult")).put("engine", "onlineResult");
					((ObjectNode) records.get(l).get("ruleResult")).put("success", true);
					((ObjectNode) records.get(l).get("ruleResult")).put("message", "SUCCEEDED");
					((ObjectNode) records.get(l).get("ruleResult")).put("status", 200);

				} else {
					logger.info("ruleresult is Array!Do next process");
				}
				/*
				 * Look for "engine":"onlineResult"
				 */
				ObjectNode onlineResultNode = mapper.createObjectNode();
				ArrayNode ruleResultArrNode = (ArrayNode) records.get(l).get("ruleResult");
				for (int i = 0; i < ruleResultArrNode.size(); i++) {
					JsonNode engineJsonNode = ruleResultArrNode.get(i);
					if (engineJsonNode.has("engine")) {
						String engineName = engineJsonNode.get("engine").asText();
						if (engineName.equalsIgnoreCase("onlineResult")) {
							onlineResultNode = (ObjectNode) engineJsonNode;
							break;
						}
					}
				}
				String personalSearchString = null, personalResponse = null;
				ObjectNode resultAdverseMedia = mapper.createObjectNode();

				JsonNode address = records.get(l).get(recordSearchAddress);
				//JsonNode candidateName = records.get(l).get(recordSearchName);
				JsonNode fatherName = records.get(l).get(recordSearchFatherName);

				// New Logic for Calling Stand Alone Api
				ObjectNode personalInfo = mapper.createObjectNode();
//				if (candidateName != null) {
//					personalInfo.put("name", candidateName);
//				} else {
//					personalInfo.put("name", "RPA Testing");
//				}
//				// personalInfo.set("dob", DOB);
//				if (address != null) {
//					personalInfo.set("address", address);
//				} else {
//					personalInfo.put("address", "Gurugram");
//				}
//				if (fatherName != null) {
//					personalInfo.set("father_name", fatherName);
//				} else {
//					personalInfo.put("father_name", "RPA Testing");
//				}
				personalInfo.put("name", resultMap.get("primaryName"));
				personalInfo.put("dob", resultMap.get("dob"));
				personalInfo.put("father_name", resultMap.get("fathersName"));
//				personalInfo.put("services", resultMap.get("services"));
//				personalInfo.put("contexts", resultMap.get("contexts"));

//				logger.info("Adverse Media Input String" + personalInfo.toString());
				/*
				 * Logic for Request Initiated Attempt
				 * 
				 */
				String checkId=recordNode.get("checkId")!=null?recordNode.get("checkId").asText():"";
				caseSpecificRecordDetailPOJOs = saveIntiatedAttempt(checkId, onlineAttemptsPOJO, mapper, 
						caseSpecificRecordDetailPOJOs);

				try {
					personalResponse = onlineApiService.sendDataToRestUrl(adversemediaRestUrl, personalInfo.toString());
				} catch (Exception e) {
					logger.info("Exception" + e.getMessage());
				}
//				logger.info("Adverse Media Response" + personalInfo.toString());
				onlineResultNode.put("Adverse Media Result", personalResponse);
				((ArrayNode) records.get(l).get("ruleResult")).add(onlineResultNode);

				/*
				 * Logic for Saving Manual Result to Database
				 */
				if (personalResponse != null) {
					JsonNode personalResponseNode = mapper.readTree(personalResponse);
					if (personalResponseNode != null && personalResponseNode.has("Adverse Media")) {
						JsonNode adverseMediaNode = personalResponseNode.get("Adverse Media");
						if (adverseMediaNode != null && adverseMediaNode.has("status")) {
							// if(!adverseMediaNode.get("status").asText().equalsIgnoreCase("clear")) {
							/*
							 * Make Entry to database
							 */
							OnlineVerificationChecks onlineVerificationChecks = new OnlineVerificationChecks();
							if (StringUtils.isNotEmpty(checkId)) {
								List<String> checkIdList = onlineAttemptsPOJO.getCheckIdList();
								checkIdList.add(checkId);
								onlineAttemptsPOJO.setCheckIdList(checkIdList);
								onlineVerificationChecks.setCheckId(checkId);
							}
							onlineVerificationChecks.setApiName("Adverse media");
							onlineVerificationChecks.setInitialResult(adverseMediaNode.get("status").asText());
							onlineVerificationChecks.setResult(adverseMediaNode.get("status").asText());
							if (adverseMediaNode.get("Adverse Media Output") != null) {
								onlineVerificationChecks
										.setMatchedIdentifiers(adverseMediaNode.get("Adverse Media Output").asText());
							}
							if (adverseMediaNode.get("Raw Output") != null) {
								onlineVerificationChecks.setOutputFile(adverseMediaNode.get("Raw Output").asText());
							}
							if (adverseMediaNode.get("Adverse media Input") != null) {
								onlineVerificationChecks
										.setInputFile(adverseMediaNode.get("Adverse media Input").asText());
							}
							onlineVerificationChecks.setCreatedDate(new Date().toString());
							onlineVerificationChecks.setUpdatedDate(new Date().toString());
							onlineVerificationChecksList.add(onlineVerificationChecks);
//							logger.info("Size of List in Adverse Media API:", onlineVerificationChecksList.size());
							// }
							/*
							 * Logic for Saving Attempt
							 */
							onlineAttemptsPOJO.setApiName("Adverse Media");
							onlineAttemptsPOJO.setProductName(subComponentName);
							if (adverseMediaNode.get("status").asText().equalsIgnoreCase("clear")) {
								/*
								 * Logic for Saving clear attempt from excel file
								 */
								saveClearOutcomeAttempt(checkId, mapper, onlineAttemptsPOJO);
							} else if (adverseMediaNode.get("status").asText().equalsIgnoreCase("Record Found")) {
								/*
								 * Logic for Saving Manual attempt from excel file
								 */
								saveDiscrepantProcessAttempt(checkId, mapper, onlineAttemptsPOJO);
							}else if(adverseMediaNode.get("status").asText().equalsIgnoreCase("Manual")) {
								saveReviewProcessAttempt(checkId, mapper, onlineAttemptsPOJO);
							}
							
						}
					}
				}

			} else {
				logger.info("Include not found");
			}
		}
		CaseSpecificInfoPOJO caseSpecificInfoPOJO = onlineAttemptsPOJO.getCaseSpecificInfoPOJO();
		if(caseSpecificInfoPOJO.getCaseSpecificRecordDetail()!=null) {
			caseSpecificRecordDetailPOJOs.addAll(caseSpecificInfoPOJO.getCaseSpecificRecordDetail());
		}
		caseSpecificInfoPOJO.setCaseSpecificRecordDetail(caseSpecificRecordDetailPOJOs);
		onlineAttemptsPOJO.setCaseSpecificInfoPOJO(caseSpecificInfoPOJO);
	}

	private void processManupatraAPIRecordWise(ObjectMapper mapper, String serviceName, List<JsonNode> records,
			String recordSearchName, String recordSearchAddress, String recordSearchFatherName,
			String recordSearchStartDate, List<OnlineVerificationChecks> onlineVerificationChecksList,
			OnlineAttemptsPOJO onlineAttemptsPOJO,String candidateName,String subComponentName, Map<String, String> resultMap) 
					throws JsonProcessingException, JsonMappingException {
		logger.info("Inside Manupatra API and Making Result");
		List<CaseSpecificRecordDetailPOJO> caseSpecificRecordDetailPOJOs = new ArrayList<>();
		for (int l = 0; l < records.size(); l++) {
			JsonNode recordNode = records.get(l);
			ArrayNode resultArray = mapper.createArrayNode();
			if (records.get(l).get("ruleResult").isArray()) {
				resultArray = (ArrayNode) records.get(l).get("ruleResult").get(0).get("result");
			} else {
				resultArray = (ArrayNode) records.get(l).get("ruleResult").get("result");
			}
			if (resultArray.get(0).toString().contains("Include")) {
				logger.info("Make an empty Json Array");
				ArrayNode ruleResultJsonNode = mapper.createArrayNode();

				if (!records.get(l).get("ruleResult").isArray()) {
					logger.info("Write engine element as scopingEngine");
					logger.info("ruleresult is not Array! Make Array And Assign the Scoping Engine");
					((ObjectNode) records.get(l).get("ruleResult")).put("engine", "ScopingEngine");
					ruleResultJsonNode.add(records.get(l).get("ruleResult"));
					logger.info("Make ruleResult array");
					((ObjectNode) records.get(l)).set("ruleResult", ruleResultJsonNode);

					/*
					 * Add Logic for adding Online Engine
					 */
					((ObjectNode) records.get(l).get("ruleResult")).put("engine", "onlineResult");
					((ObjectNode) records.get(l).get("ruleResult")).put("success", true);
					((ObjectNode) records.get(l).get("ruleResult")).put("message", "SUCCEEDED");
					((ObjectNode) records.get(l).get("ruleResult")).put("status", 200);

				} else {
					logger.info("ruleresult is Array!Do next process");
				}

				/*
				 * Look for "engine":"onlineResult"
				 */
				ObjectNode onlineResultNode = mapper.createObjectNode();
				ArrayNode ruleResultArrNode = (ArrayNode) records.get(l).get("ruleResult");
				for (int i = 0; i < ruleResultArrNode.size(); i++) {
					JsonNode engineJsonNode = ruleResultArrNode.get(i);
					if (engineJsonNode.has("engine")) {
						String engineName = engineJsonNode.get("engine").asText();
						if (engineName.equalsIgnoreCase("onlineResult")) {
							onlineResultNode = (ObjectNode) engineJsonNode;
							break;
						}
					}
				}
				String manuPatraSearchString = null; 
				String manuPatraResponsePrimary = null;
				String manuPatraResponseSecondary = null;
				ObjectNode resultManupatra = mapper.createObjectNode();

				JsonNode address = records.get(l).get(recordSearchAddress);// Should be Split and pick state
				//JsonNode candidateName = records.get(l).get(recordSearchName);
				JsonNode fatherName = records.get(l).get(recordSearchFatherName);

				// New Logic for Calling Stand Alone Api
				ObjectNode personalInfo = mapper.createObjectNode();
//				if (candidateName != null) {
//					personalInfo.put("name", candidateName);
//				} else {
//					personalInfo.put("name", "Sophia");
//				}
//				personalInfo.put("dob", "1995-12-13");
//				if (address != null) {
//					personalInfo.set("address", address);
//				} else {
//					personalInfo.put("address", "Gurugram");
//				}
//				if (fatherName != null) {
//					personalInfo.set("father_name", fatherName);
//				} else {
//					personalInfo.put("father_name", "RPA Testing");
//				}
				String contexts="Accused,Rape,Arrested,Fake,Fraud,Convicted,Assualt,"
						+ "Molested,Assassinated,FleshTrade,Burglary,SexRacket,Hawala,"
						+ "Betting,Smuggling,Pimp,Abeyance,Sabotage,Abetted,Racket,Criminal"
						+ ",Crime,Detained,Robbery,Forgery,Mortgage,Terrorist,Duped,Ban,"
						+ "Debarred,Wanted,Bankrupt,Summon,Bribery,Corruption,Trafficking,"
						+ "Hijack,Arson,Counterfeit,Piracy,CiberCrime,Hacking,Phishing,"
						+ "Extortion,Moneylaundering,Slavery,Manipulation,Stole,Snatch,"
						+ "Theft,Kidnapping,Hostage,Ransacke,Sack,SexualExploitation,"
						+ "WarCrimes,DistributionofFakeMedicine,Sentenced,AviationCrime,"
						+ "Mugshot,Stab,Harrasment,Fight,Quarrel,Disqalified,Foundguilty,"
						+ "HeldResponsible,Dispute,Alleged,Disqualifieddirector,Duping,"
						+ "Attacked,ChainSnatchers,Custody,Chitfundscam,illegal,"
						+ "legalproceedings,Narcotics ,nabbed,apprehend,gruesome,destroy,"
						+ "rapist,abduct,shoplifting,kingpin,contemnor,bootlegger,ambush,"
						+ "Cheat,cold-heartedmurder,Corpse,brutal,grive,abrasion,regret,"
						+ "unfortunateevent,gash,AircraftHijack,ArmsTrafficking,Cybercrime,"
						+ "Embezzlement,TaxFraud,TaxEvasion,IllicitTraffickinginstolengoods,"
						+ "InsiderTrading&MarketManipulation,MigrantSmuggling,"
						+ "IndecentAssault,Narcotics Trafficking,"
						+ "Pharmaceuticalproducttrafficking,Banned/FakeMedicines,"
						+ "IllegalProduction&Distribution(Narcotics/pharma/armsetc),"
						+ "SecuritiesFraud,Piracy(Sea),IllegalCartelInformation,PriceFixing,"
						+ "Antitrustviolations,LawViolations,Racketeering,Terrorism,"
						+ "TerrorFinancing,WantsandWarrants,parole,Probation,offender,"
						+ "Sexoffender,Committingacrime,jailed,inprison,behindthebars,"
						+ "firearm,drugs,alcohol/liquor,passjudgement,imprisonment,"
						+ "drugspeddler,impostor,Swindle,dacoit,deceive,manhandle,booked,"
						+ "Blacklisted ,trick,auto-lifter,unlawful,Illegaltrader,"
						+ "misappropriation,stealing,thief,larceny,anti-corruption,"
						+ "anti-moneylaundering,prosecute,charged,investigation,suspected,"
						+ "offense,fines,penalty,compensation,damages,sue,insolvent,"
						+ "borrower,impersonate,prostitution,brothel,infringement,"
						+ "iniquitous,breach,debauchery,dishonest,illegality,Battery,"
						+ "Homicide,manslaughter,misdemeanor,misfeasance,Felony,"
						+ "infractions,whitecollarcrime,vandalism,shoplifting,violence,"
						+ "treason,publicintoxication,trespass,disorderlyconduct,"
						+ "recklessdriving,drink&drive,hit&run,parkingornoiseviolations,"
						+ "buildingcodeviolations,littering,habitualtrafficoffender,"
						+ "Publicdrunkenness,workingunlicensed,drug-dealing,"
						+ "Succomedtoinjury,Lure,implead,hire,Posing,demanding,anonymus,"
						+ "steroid,cannabies,weed";
				personalInfo.put("name", resultMap.get("primaryName"));
				personalInfo.put("dob", resultMap.get("dob"));
				personalInfo.put("father_name", resultMap.get("fathersName"));
				personalInfo.put("address", resultMap.get("address"));
				personalInfo.put("contexts", contexts);
//				logger.info("Manupatra Input String" + personalInfo.toString());
				/*
				 * Logic for Request Initiated Attempt
				 * 
				 */
				String checkId=recordNode.get("checkId")!=null?recordNode.get("checkId").asText():"";
				caseSpecificRecordDetailPOJOs = saveIntiatedAttempt(checkId, onlineAttemptsPOJO, mapper, 
						caseSpecificRecordDetailPOJOs);

				ObjectNode objectNode = mapper.createObjectNode();
				try {
					manuPatraResponsePrimary = onlineApiService.sendDataToRestUrl(manupatraRestUrl, personalInfo.toString());
					objectNode.put("Primary result", manuPatraResponsePrimary);
				} catch (Exception e) {
					logger.info("Exception" + e.getMessage());
				}

				personalInfo.put("name", resultMap.get("secondaryName"));
				try {
					manuPatraResponseSecondary = onlineApiService.sendDataToRestUrl(manupatraRestUrl, personalInfo.toString());
					objectNode.put("Secondary result", manuPatraResponseSecondary);
				} catch (Exception e) {
					logger.info("Exception" + e.getMessage());
				}
				
				
				// Change this proper JsonNode Object
//				logger.info("Manupatra Response" + objectNode);
				onlineResultNode.set("Manupatra Result", objectNode);
				((ArrayNode) records.get(l).get("ruleResult")).add(onlineResultNode);

				/*
				 * Logic for Saving Manual Result to Database
				 */
				
				processManupatraResponse(mapper, manuPatraResponsePrimary,
						manuPatraResponseSecondary, checkId, onlineAttemptsPOJO,
						onlineVerificationChecksList, subComponentName);

			} else {
				logger.info("Include not found");
			}
		}
		CaseSpecificInfoPOJO caseSpecificInfoPOJO = onlineAttemptsPOJO.getCaseSpecificInfoPOJO();
		if(caseSpecificInfoPOJO.getCaseSpecificRecordDetail()!=null) {
			caseSpecificRecordDetailPOJOs.addAll(caseSpecificInfoPOJO.getCaseSpecificRecordDetail());
		}
		caseSpecificInfoPOJO.setCaseSpecificRecordDetail(caseSpecificRecordDetailPOJOs);
		onlineAttemptsPOJO.setCaseSpecificInfoPOJO(caseSpecificInfoPOJO);
	}
	
	private void processManupatraResponse(ObjectMapper mapper, String manuPatraResponsePrimary,
			String manuPatraResponseSecondary, String checkId, OnlineAttemptsPOJO onlineAttemptsPOJO,
			List<OnlineVerificationChecks> onlineVerificationChecksList, String subComponentName)
			throws JsonProcessingException {

		String primaryStatus = "";
		String primaryManupatraOutput = "";
		String primaryRawOutput = "";
		String primaryManupatraInput = "";
		String primaryKeyName = "";

		String secondaryStatus = "";
		String secondaryManupatraOutput = "";
		String secondaryRawOutput = "";
		String secondaryManupatraInput = "";
		String secondaryKeyName = "";

		if (manuPatraResponsePrimary != null) {
			JsonNode personalResponseNode = mapper.readTree(manuPatraResponsePrimary);
//			logger.info("Manupatra Response : {}", personalResponseNode);
//			logger.info("Response : {}", personalResponseNode.get("Manupatra").get("status"));

			if (personalResponseNode != null && personalResponseNode.has("Manupatra")) {
				JsonNode manupatraNode = personalResponseNode.get("Manupatra");

				if (manupatraNode != null && manupatraNode.has("status")) {

					primaryStatus = manupatraNode.get("status").asText();
					primaryManupatraOutput = manupatraNode.has("ManuPatra Output")
							? manupatraNode.get("ManuPatra Output").asText()
							: "";
					primaryRawOutput = manupatraNode.has("Raw Output") ? manupatraNode.get("Raw Output").asText() : "";
					primaryManupatraInput = manupatraNode.has("ManuPatra Input")
							? manupatraNode.get("ManuPatra Input").asText()
							: "";
					primaryKeyName = manupatraNode.has("Matched Key Name")
							? manupatraNode.get("Matched Key Name").asText()
							: "";
				}
			}
		}

		if (manuPatraResponseSecondary != null) {
			JsonNode personalResponseNode = mapper.readTree(manuPatraResponseSecondary);
//			logger.info("Manupatra Response : {}", personalResponseNode);
//			logger.info("Response : {}", personalResponseNode.get("Manupatra").get("status"));

			if (personalResponseNode != null && personalResponseNode.has("Manupatra")) {
				JsonNode manupatraNode = personalResponseNode.get("Manupatra");

				if (manupatraNode != null && manupatraNode.has("status")) {

					secondaryStatus = manupatraNode.get("status").asText();
					secondaryManupatraOutput = manupatraNode.has("ManuPatra Output")
							? manupatraNode.get("ManuPatra Output").asText()
							: "";
					secondaryRawOutput = manupatraNode.has("Raw Output") ? manupatraNode.get("Raw Output").asText()
							: "";
					secondaryManupatraInput = manupatraNode.has("ManuPatra Input")
							? manupatraNode.get("ManuPatra Input").asText()
							: "";
					secondaryKeyName = manupatraNode.has("Matched Key Name")
							? manupatraNode.get("Matched Key Name").asText()
							: "";
				}
			}
		}

		String finalStatus = "";
		if (StringUtils.equalsIgnoreCase(primaryStatus, "Manual")
				|| StringUtils.equalsIgnoreCase(secondaryStatus, "Manual")) {
			finalStatus = "Manual";
		} else if (StringUtils.equalsIgnoreCase(primaryStatus, "Record Found")
				|| StringUtils.equalsIgnoreCase(secondaryStatus, "Record Found")) {
			finalStatus = "Record Found";
		} else if (StringUtils.equalsIgnoreCase(primaryStatus, "Clear")
				&& StringUtils.equalsIgnoreCase(secondaryStatus, "Clear")) {
			finalStatus = "Clear";
		} else if (StringUtils.equalsIgnoreCase(primaryStatus, "Processing Request")
				|| StringUtils.equalsIgnoreCase(secondaryStatus, "Processing Request")) {
			finalStatus = "Processing Request";
		}

		ObjectNode manupatraOutput = mapper.createObjectNode();
		manupatraOutput.put("primary", primaryManupatraOutput);
		manupatraOutput.put("secondary", secondaryManupatraOutput);
		String manupatraOutputStr = mapper.writeValueAsString(manupatraOutput);

		ObjectNode rawOutput = mapper.createObjectNode();
		rawOutput.put("primary", primaryRawOutput);
		rawOutput.put("secondary", secondaryRawOutput);
		String rawOutputStr = mapper.writeValueAsString(rawOutput);

		ObjectNode manupatraInput = mapper.createObjectNode();
		manupatraInput.put("primary", primaryManupatraInput);
		manupatraInput.put("secondary", secondaryManupatraInput);
		String manupatraInputStr = mapper.writeValueAsString(manupatraInput);

		ObjectNode matchedKeyName = mapper.createObjectNode();
		matchedKeyName.put("primary", primaryKeyName);
		matchedKeyName.put("secondary", secondaryKeyName);
		String matchedKeyNameStr = mapper.writeValueAsString(matchedKeyName);

		OnlineVerificationChecks onlineVerificationChecks = new OnlineVerificationChecks();
		if (StringUtils.isNotEmpty(checkId)) {
			List<String> checkIdList = onlineAttemptsPOJO.getCheckIdList();
			checkIdList.add(checkId);
			onlineAttemptsPOJO.setCheckIdList(checkIdList);
			onlineVerificationChecks.setCheckId(checkId);
		}

//		Check for status before final push
//		finalStatus = "Manual";

		onlineVerificationChecks.setApiName("Manupatra");

		onlineVerificationChecks.setInitialResult(finalStatus);
		onlineVerificationChecks.setResult(finalStatus);

		onlineVerificationChecks.setMatchedIdentifiers(manupatraOutputStr);
		onlineVerificationChecks.setOutputFile(rawOutputStr);
		onlineVerificationChecks.setInputFile(manupatraInputStr);
//		onlineVerificationChecks.setMatchedIdentifiers(matchedKeyNameStr);

		onlineVerificationChecks.setCreatedDate(new Date().toString());
		onlineVerificationChecks.setUpdatedDate(new Date().toString());
		onlineVerificationChecksList.add(onlineVerificationChecks);

//		logger.info("Size of List in Manupatra API : {}", onlineVerificationChecksList.size());

// 		Logic for saving Attempt
		onlineAttemptsPOJO.setApiName("Manupatra");
		onlineAttemptsPOJO.setProductName(subComponentName);
		
		if (StringUtils.equalsIgnoreCase(finalStatus, "clear")) {
			saveClearOutcomeAttempt(checkId, mapper, onlineAttemptsPOJO);
		} else if (StringUtils.equalsIgnoreCase(finalStatus, "Record Found")) {
			saveDiscrepantProcessAttempt(checkId, mapper, onlineAttemptsPOJO);
		} else if (StringUtils.equalsIgnoreCase(finalStatus, "Manual")) {
			saveReviewProcessAttempt(checkId, mapper, onlineAttemptsPOJO);
		}
	}

	private void processWatchoutAPIRecordWise(ObjectMapper mapper, String serviceName, List<JsonNode> records,
			String recordSearchName, String recordSearchDOB, String dinStr,
			List<OnlineVerificationChecks> onlineVerificationChecksList, OnlineAttemptsPOJO onlineAttemptsPOJO,String candidateName
			,String subComponentName, Map<String, String> resultMap)
			throws JsonMappingException, JsonProcessingException {
		logger.info("Inside WatchOut API and Making Result");
		List<CaseSpecificRecordDetailPOJO> caseSpecificRecordDetailPOJOs = new ArrayList<>();
		for (int l = 0; l < records.size(); l++) {
			JsonNode recordNode = records.get(l);
			ArrayNode resultArray = mapper.createArrayNode();
			if (records.get(l).get("ruleResult").isArray()) {
				resultArray = (ArrayNode) records.get(l).get("ruleResult").get(0).get("result");
			} else {
				resultArray = (ArrayNode) records.get(l).get("ruleResult").get("result");
			}
			if (resultArray.get(0).toString().contains("Include")) {
				logger.info("Make an empty Json Array");
				ArrayNode ruleResultJsonNode = mapper.createArrayNode();

				if (!records.get(l).get("ruleResult").isArray()) {
					logger.info("Write engine element as scopingEngine");
					logger.info("ruleresult is not Array! Make Array And Assign the Scoping Engine");
					((ObjectNode) records.get(l).get("ruleResult")).put("engine", "ScopingEngine");
					ruleResultJsonNode.add(records.get(l).get("ruleResult"));
					logger.info("Make ruleResult array");
					((ObjectNode) records.get(l)).set("ruleResult", ruleResultJsonNode);

					/*
					 * Add Logic for adding Online Engine
					 */
					((ObjectNode) records.get(l).get("ruleResult")).put("engine", "onlineResult");
					((ObjectNode) records.get(l).get("ruleResult")).put("success", true);
					((ObjectNode) records.get(l).get("ruleResult")).put("message", "SUCCEEDED");
					((ObjectNode) records.get(l).get("ruleResult")).put("status", 200);

				} else {
					logger.info("ruleresult is Array!Do next process");
				}
				/*
				 * Look for "engine":"onlineResult"
				 */
				ObjectNode onlineResultNode = mapper.createObjectNode();
				ArrayNode ruleResultArrNode = (ArrayNode) records.get(l).get("ruleResult");
				System.out.println(ruleResultArrNode);
				for (int i = 0; i < ruleResultArrNode.size(); i++) {
					JsonNode engineJsonNode = ruleResultArrNode.get(i);
					if (engineJsonNode.has("engine")) {
						String engineName = engineJsonNode.get("engine").asText();
						if (engineName.equalsIgnoreCase("onlineResult")) {
							onlineResultNode = (ObjectNode) engineJsonNode;
							break;
						}
					}
				}
				String fulFillmentSearchString = null, fulfillmentResponse = null;
				ObjectNode resultWatchOut = mapper.createObjectNode();

				JsonNode DOB = records.get(l).get(recordSearchDOB);
				//JsonNode candidateName = records.get(l).get(recordSearchName);
				// New Logic for Calling Stand Alone Api
				ObjectNode personalInfo = mapper.createObjectNode();
				if (dinStr != null) {
//					if (candidateName != null) {
//						personalInfo.put("name", candidateName);
//					} else {
//						personalInfo.put("name", "RPA Testing");
//					}
//					if (DOB != null) {
//						personalInfo.set("dob", DOB);
//					} else {
//						personalInfo.put("dob", "15-02-1994");
//					}
					personalInfo.put("din", dinStr);
				}
				personalInfo.put("name", resultMap.get("primaryName"));
				personalInfo.put("dob", resultMap.get("dob"));
				personalInfo.put("father_name", resultMap.get("fathersName"));

				// personalInfo.set("address", address);
				// personalInfo.set("father_name", fatherName);
//				logger.info("Watchout Input String" + personalInfo.toString());
				/*
				 * Logic for Request Initiated Attempt
				 * 
				 */
				String checkId=recordNode.get("checkId")!=null?recordNode.get("checkId").asText():"";
     			caseSpecificRecordDetailPOJOs = saveIntiatedAttempt(checkId, onlineAttemptsPOJO, mapper, 
						caseSpecificRecordDetailPOJOs);

				try {
					fulfillmentResponse = onlineApiService.sendDataToRestUrl(watchoutRestUrl, personalInfo.toString());
				} catch (Exception e) {
					logger.info("Exception" + e.getMessage());
				}
//				logger.info("WatchOut Response(FulFillment)" + personalInfo.toString());
				onlineResultNode.put("Watchout Result", fulfillmentResponse);
				((ArrayNode) records.get(l).get("ruleResult")).add(onlineResultNode);

				/*
				 * Logic for Saving Manual Result to Database
				 */
				if (fulfillmentResponse != null) {
					JsonNode personalResponseNode = mapper.readTree(fulfillmentResponse);
					if (personalResponseNode != null && personalResponseNode.has("WatchOut")) {
						JsonNode watchoutNode = personalResponseNode.get("WatchOut");
						if (watchoutNode != null && watchoutNode.has("status")) {
							// if(!watchoutNode.get("status").asText().equalsIgnoreCase("clear")) {
							/*
							 * Make Entry to database
							 */
							OnlineVerificationChecks onlineVerificationChecks = new OnlineVerificationChecks();
							if (StringUtils.isNotEmpty(checkId))
							{
								List<String> checkIdList = onlineAttemptsPOJO.getCheckIdList();
								checkIdList.add(checkId);
								onlineAttemptsPOJO.setCheckIdList(checkIdList);
								onlineVerificationChecks.setCheckId(checkId);
							}
							onlineVerificationChecks.setApiName("Watchout");
							onlineVerificationChecks.setInitialResult(watchoutNode.get("status").asText());
							onlineVerificationChecks.setResult(watchoutNode.get("status").asText());
							if (watchoutNode.get("WatchOut Output") != null) {
								onlineVerificationChecks
										.setMatchedIdentifiers(watchoutNode.get("WatchOut Output").asText());
							}
							if (watchoutNode.get("Raw Output") != null) {
								onlineVerificationChecks.setOutputFile(watchoutNode.get("WatchOut Output").asText());
							}
							if (watchoutNode.get("WatchOut Input") != null) {
								onlineVerificationChecks.setInputFile(watchoutNode.get("WatchOut Input").asText());
							}
							onlineVerificationChecks.setCreatedDate(new Date().toString());
							onlineVerificationChecks.setUpdatedDate(new Date().toString());
							onlineVerificationChecksList.add(onlineVerificationChecks);
//							logger.info("Size of List in WatchOut API:", onlineVerificationChecksList.size());
							// }
							/*
							 * Logic for Saving Attempt
							 */
							onlineAttemptsPOJO.setApiName("WatchOut");
							onlineAttemptsPOJO.setProductName(subComponentName);
							if (watchoutNode.get("status").asText().equalsIgnoreCase("clear")) {
								/*
								 * Logic for Saving clear attempt from excel file
								 */
								saveClearOutcomeAttempt(checkId, mapper, onlineAttemptsPOJO);
							} else if (watchoutNode.get("status").asText().equalsIgnoreCase("Record Found")) {
								/*
								 * Logic for Saving Manual attempt from excel file
								 */
								saveDiscrepantProcessAttempt(checkId, mapper, onlineAttemptsPOJO);
							}else if(watchoutNode.get("status").asText().equalsIgnoreCase("Manual")) {
								saveReviewProcessAttempt(checkId, mapper, onlineAttemptsPOJO);
							}
						}
					}
				}

			} else {
				logger.info("Include not found");
			}
		}
		CaseSpecificInfoPOJO caseSpecificInfoPOJO = onlineAttemptsPOJO.getCaseSpecificInfoPOJO();
		if(caseSpecificInfoPOJO.getCaseSpecificRecordDetail()!=null) {
			caseSpecificRecordDetailPOJOs.addAll(caseSpecificInfoPOJO.getCaseSpecificRecordDetail());
		}
		caseSpecificInfoPOJO.setCaseSpecificRecordDetail(caseSpecificRecordDetailPOJOs);
		onlineAttemptsPOJO.setCaseSpecificInfoPOJO(caseSpecificInfoPOJO);
	}

	
	private void processWorldCheckAPIRecordWise(ObjectMapper mapper, String serviceName, List<JsonNode> records,
			String recordSearchName, String recordSearchDOB, String dinStr, String recordSearchFatherName,
			String recordSearchAddress, List<OnlineVerificationChecks> onlineVerificationChecksList,
			OnlineAttemptsPOJO onlineAttemptsPOJO,String candidateName,String subComponentName, Map<String, String> resultMap) 
					throws JsonMappingException, JsonProcessingException {
		logger.info("Inside Process World Check API");
		List<CaseSpecificRecordDetailPOJO> caseSpecificRecordDetailPOJOs = new ArrayList<>();
		for (int l = 0; l < records.size(); l++) {
			JsonNode recordNode = records.get(l);
			ArrayNode resultArray = mapper.createArrayNode();
			if (records.get(l).get("ruleResult").isArray()) {
				resultArray = (ArrayNode) records.get(l).get("ruleResult").get(0).get("result");
			} else {
				resultArray = (ArrayNode) records.get(l).get("ruleResult").get("result");
			}
			if (resultArray.get(0).toString().contains("Include")) {
				logger.info("Make an empty Json Array");
				ArrayNode ruleResultJsonNode = mapper.createArrayNode();

				if (!records.get(l).get("ruleResult").isArray()) {
					logger.info("Write engine element as scopingEngine");
					logger.info("ruleresult is not Array! Make Array And Assign the Scoping Engine");
					((ObjectNode) records.get(l).get("ruleResult")).put("engine", "ScopingEngine");
					ruleResultJsonNode.add(records.get(l).get("ruleResult"));
					logger.info("Make ruleResult array");
					((ObjectNode) records.get(l)).set("ruleResult", ruleResultJsonNode);

					/*
					 * Add Logic for adding Online Engine
					 */
					((ObjectNode) records.get(l).get("ruleResult")).put("engine", "onlineResult");
					((ObjectNode) records.get(l).get("ruleResult")).put("success", true);
					((ObjectNode) records.get(l).get("ruleResult")).put("message", "SUCCEEDED");
					((ObjectNode) records.get(l).get("ruleResult")).put("status", 200);

				} else {
					logger.info("ruleresult is Array!Do next process");
				}
				/*
				 * Look for "engine":"onlineResult"
				 */
				ObjectNode onlineResultNode = mapper.createObjectNode();
				ArrayNode ruleResultArrNode = (ArrayNode) records.get(l).get("ruleResult");
				for (int i = 0; i < ruleResultArrNode.size(); i++) {
					JsonNode engineJsonNode = ruleResultArrNode.get(i);
					if (engineJsonNode.has("engine")) {
						String engineName = engineJsonNode.get("engine").asText();
						if (engineName.equalsIgnoreCase("onlineResult")) {
							onlineResultNode = (ObjectNode) engineJsonNode;
							break;
						}
					}
				}
				String worldCheckSearchString = null, worldCheckResponse = null;
				ObjectNode resultWorldCheck = mapper.createObjectNode();

				JsonNode DOB = records.get(l).get(recordSearchDOB);
				JsonNode candidateName1 = records.get(l).get(recordSearchName);
				JsonNode fathername = records.get(l).get(recordSearchFatherName);
				JsonNode address = records.get(l).get(recordSearchAddress);

				// New Logic for Calling Stand Alone Api
				ObjectNode personalInfo = mapper.createObjectNode();
//				if (candidateName != null) {
//					personalInfo.put("name", candidateName);
//				} else {
//					personalInfo.put("name", "RPA Testing");
//				}
//				if (DOB != null) {
//					personalInfo.set("dob", DOB);
//				} else {
//					personalInfo.put("dob", "15-02-1994");
//				}
//				if (fathername != null) {
//					personalInfo.set("father_name", fathername);
//				} else {
//					personalInfo.put("father_name", "RPA Testing");
//				}
//				if (address != null) {
//					personalInfo.set("address", address);
//				} else {
//					personalInfo.put("address", "Gurugram");
//				}
				

				personalInfo.put("name", resultMap.get("primaryName"));
				personalInfo.put("dob", resultMap.get("dob"));
				personalInfo.put("father_name", resultMap.get("fathersName"));
				personalInfo.put("address", resultMap.get("address"));
				
//				logger.info("WorldCheck Input String" + personalInfo.toString());
				/*
				 * Logic for Request Initiated Attempt
				 * 
				 */
				String checkId=recordNode.get("checkId")!=null?recordNode.get("checkId").asText():"";
				caseSpecificRecordDetailPOJOs = saveIntiatedAttempt(checkId, onlineAttemptsPOJO, mapper, 
						caseSpecificRecordDetailPOJOs);

				try {
					worldCheckResponse = onlineApiService.sendDataToRestUrl(worldcheckRestUrl, personalInfo.toString());
				} catch (Exception e) {
					logger.info("Exception" + e.getMessage());
				}
//				logger.info("Worldcheck Response" + personalInfo.toString());
				onlineResultNode.put("Worldcheck Result", worldCheckResponse);
				((ArrayNode) records.get(l).get("ruleResult")).add(onlineResultNode);
				/*
				 * Logic for Saving Manual Result to Database
				 */
				if (worldCheckResponse != null) {
					JsonNode personalResponseNode = mapper.readTree(worldCheckResponse);
					if (personalResponseNode != null && personalResponseNode.has("WorldCheck")) {
						JsonNode worldCheckNode = personalResponseNode.get("WorldCheck");
						if (worldCheckNode != null && worldCheckNode.has("status")) {
							// if(!worldCheckNode.get("status").asText().equalsIgnoreCase("clear")) {
							/*
							 * Make Entry to database
							 */
							OnlineVerificationChecks onlineVerificationChecks = new OnlineVerificationChecks();
							if (StringUtils.isNotEmpty(checkId))
							{
								List<String> checkIdList = onlineAttemptsPOJO.getCheckIdList();
								checkIdList.add(checkId);
								onlineAttemptsPOJO.setCheckIdList(checkIdList);
								onlineVerificationChecks.setCheckId(checkId);
							}
							onlineVerificationChecks.setApiName("World Check");
							onlineVerificationChecks.setInitialResult(worldCheckNode.get("status").asText());
							onlineVerificationChecks.setResult(worldCheckNode.get("status").asText());
							
							onlineVerificationChecks.setMatchedIdentifiers("");
							/*
							 * if(worldCheckNode.get("World Check Output")!=null) {
							 * onlineVerificationChecks.setMatchedIdentifiers(worldCheckNode.
							 * get("World Check Output").asText()); }
							 */
							if (worldCheckNode.get("Raw Output") != null) {
								onlineVerificationChecks.setOutputFile(worldCheckNode.get("Raw Output").asText());
							}
							if (personalResponseNode.get("World Check Input") != null) {
								onlineVerificationChecks.setInputFile(worldCheckNode.get("World Check Input").asText());
							}
							onlineVerificationChecks.setCreatedDate(new Date().toString());
							onlineVerificationChecks.setUpdatedDate(new Date().toString());
							onlineVerificationChecksList.add(onlineVerificationChecks);
//							logger.info("Size of List in World Check API:", onlineVerificationChecksList.size());
							/*
							 * Logic for Saving Attempt
							 */
							onlineAttemptsPOJO.setApiName("WorldCheck");
							onlineAttemptsPOJO.setProductName(subComponentName);
							if (worldCheckNode.get("status").asText().equalsIgnoreCase("clear")) {
								/*
								 * Logic for Saving clear attempt from excel file
								 */
								saveClearOutcomeAttempt(checkId, mapper, onlineAttemptsPOJO);
							} else if (worldCheckNode.get("status").asText().equalsIgnoreCase("Record Found")) {
								/*
								 * Logic for Saving Manual attempt from excel file
								 */
								saveDiscrepantProcessAttempt(checkId, mapper, onlineAttemptsPOJO);
							}else if(worldCheckNode.get("status").asText().equalsIgnoreCase("Manual")) {
								saveReviewProcessAttempt(checkId, mapper, onlineAttemptsPOJO);
							}
							// }
						}
					}
				}

			} else {
				logger.info("Include not found");
			}
		}
		CaseSpecificInfoPOJO caseSpecificInfoPOJO = onlineAttemptsPOJO.getCaseSpecificInfoPOJO();
		if(caseSpecificInfoPOJO.getCaseSpecificRecordDetail()!=null) {
			caseSpecificRecordDetailPOJOs.addAll(caseSpecificInfoPOJO.getCaseSpecificRecordDetail());
		}
		caseSpecificInfoPOJO.setCaseSpecificRecordDetail(caseSpecificRecordDetailPOJOs);
		onlineAttemptsPOJO.setCaseSpecificInfoPOJO(caseSpecificInfoPOJO);
	}

	private void timeOut() {
		try {
			TimeUnit.SECONDS.sleep(100);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
	}

	private void callback(String postStr) {
		try {

//			logger.debug("postStr\n" + postStr);
			URL url = new URL(env.getProperty("online.router.callback.url"));
//			logger.debug("Using callback URL\n" + url);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			// Creating the ObjectMapper object
			String authToken = JsonParser.parseString(postStr).getAsJsonObject().get("metadata").getAsJsonObject()
					.get("requestAuthToken").getAsString();

//			logger.debug("Auth Token\n" + authToken);
			con.setRequestProperty("tokenId", authToken);

			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			os.write(postStr.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();
//			logger.debug("Callback POST Response Code: " + responseCode + " : " + con.getResponseMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private List<CaseSpecificRecordDetailPOJO> saveIntiatedAttempt(String checkId, OnlineAttemptsPOJO onlineAttemptsPOJO,
			ObjectMapper mapper, List<CaseSpecificRecordDetailPOJO> caseSpecificRecordDetailPOJOs) throws JsonProcessingException {
		if (StringUtils.isNotEmpty(checkId)) {
			
//			sendDataToL3IntiatedAttempt(onlineAttemptsPOJO.getComponentScoping(), onlineAttemptsPOJO.getComponent(),
//					mapper, checkId);
//			
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetail = new CaseSpecificRecordDetailPOJO();
			caseSpecificRecordDetail.setComponentName(onlineAttemptsPOJO.getComponentName());
			caseSpecificRecordDetail.setProduct(onlineAttemptsPOJO.getProductName());
			//caseSpecificRecordDetail.setComponentRecordField(recordNode);
			ObjectNode recordnode=mapper.createObjectNode();
			recordnode.put("record", "{}");
			caseSpecificRecordDetail.setComponentRecordField(recordnode.toString());
			caseSpecificRecordDetail.setCaseSpecificRecordStatus("Request Initiated");
			caseSpecificRecordDetail.setInstructionCheckId(checkId);
			caseSpecificRecordDetail.setCaseSpecificId(onlineAttemptsPOJO.getCaseSpecificInfoId());
			caseSpecificRecordDetailPOJOs.add(caseSpecificRecordDetail);
			
			/*
			 * Make Rest call and save this Info DB
			 */
			JsonNode caseSpecificRecordDetailNode = mapper.convertValue(caseSpecificRecordDetail,JsonNode.class);
			String caseSpecificRecordDetailStr=null;
			if(caseSpecificRecordDetailNode!=null) {
				caseSpecificRecordDetailStr=onlineApiService.sendDataToCaseSpecificRecord(caseSpecificRecordDetailNode.toString());
			}
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetail1 = new CaseSpecificRecordDetailPOJO();
			if(caseSpecificRecordDetailStr!=null) {
				JsonNode caseSpecificRecordNode= mapper.readTree(caseSpecificRecordDetailStr);
				caseSpecificRecordDetail1 = mapper.treeToValue(caseSpecificRecordNode, CaseSpecificRecordDetailPOJO.class);
			}
			Long caseSpecificRecordId=null;//Treat as request ID for other like attempt history
			if(caseSpecificRecordDetail1!=null) {
				caseSpecificRecordId=caseSpecificRecordDetail1.getCaseSpecificDetailId();
			}
			onlineAttemptsPOJO.setCaseSpecificRecordId(caseSpecificRecordId);

			AttemptHistory attemptHistory = new AttemptHistory();
			attemptHistory.setAttemptStatusid((long) 66);// (66, ' Request Initiated Via Integration -
															// ',
															// 'Valid', 3,
															// '2020-02-26
															// 13:28:06'),
			attemptHistory.setAttemptDescription("Request Initiated");
			attemptHistory.setName("Product Name:"+onlineAttemptsPOJO.getProductName()+
					", API Name:"+onlineAttemptsPOJO.getApiName());
			attemptHistory.setCheckid(checkId);

			AttemptStatusData attemptStatusData = new AttemptStatusData();
			attemptStatusData.setDepositionId((long) 13);
			attemptStatusData.setEndstatusId((long) 39);
			attemptStatusData.setModeId((long) 14);
			attemptHistory.setRequestid(caseSpecificRecordId);
			saveAttempt(mapper, attemptHistory, attemptStatusData);
			
			VerificationEventStatusPOJO verificationEventStatusPOJO = new VerificationEventStatusPOJO();
			verificationEventStatusPOJO.setCheckId(checkId);
			verificationEventStatusPOJO.setEventName("online");
			verificationEventStatusPOJO.setEventType("auto");
			verificationEventStatusPOJO.setCaseNo(onlineAttemptsPOJO.getCaseNo()); 
			verificationEventStatusPOJO.setUser("System");
			verificationEventStatusPOJO.setStatus("Request Initiated");
			verificationEventStatusPOJO.setRequestId(caseSpecificRecordId);
			String verificationEventStatusStr = mapper
					.writeValueAsString(
							verificationEventStatusPOJO);
			onlineApiService.sendDataToVerificationEventStatus(
					verifictioneventstatusRestUrl, verificationEventStatusStr);
		}
		return caseSpecificRecordDetailPOJOs;
	}

	private void saveClearOutcomeAttempt(String checkId, ObjectMapper mapper, 
			OnlineAttemptsPOJO onlineAttemptsPOJO) throws JsonProcessingException {
		if (StringUtils.isNotEmpty(checkId)) {
			
//			sendDataToL3ClearOutcomeAttempt(onlineAttemptsPOJO.getComponentScoping(), onlineAttemptsPOJO.getComponent(),
//					mapper, checkId);
			
			AttemptHistory attemptHistory = new AttemptHistory();
			attemptHistory.setAttemptStatusid((long) 67);// (67, ' Auto Tagged - Clear -
															// ',
															// 'Valid', 3,
															// '2020-02-26
															// 13:28:06'),
			attemptHistory.setAttemptDescription("Result received with No records");
			attemptHistory.setName("Product Name:"+onlineAttemptsPOJO.getProductName()+
					", API Name:"+onlineAttemptsPOJO.getApiName());
			attemptHistory.setCheckid(checkId);
			attemptHistory.setRequestid(onlineAttemptsPOJO.getCaseSpecificRecordId());

			AttemptStatusData attemptStatusData = new AttemptStatusData();
			attemptStatusData.setDepositionId((long) 3);
			attemptStatusData.setEndstatusId((long) 40); // Completed
			attemptStatusData.setModeId((long) 14);
			saveAttempt(mapper, attemptHistory, attemptStatusData);
			
			VerificationEventStatusPOJO verificationEventStatusPOJO = new VerificationEventStatusPOJO();
			verificationEventStatusPOJO.setCheckId(checkId);
			verificationEventStatusPOJO.setEventName("online");
			verificationEventStatusPOJO.setEventType("auto");
			verificationEventStatusPOJO.setCaseNo(onlineAttemptsPOJO.getCaseNo()); 
			verificationEventStatusPOJO.setUser("System");
			verificationEventStatusPOJO.setStatus("Result received with No records");
			verificationEventStatusPOJO.setRequestId(onlineAttemptsPOJO.getCaseSpecificRecordId());
			String verificationEventStatusStr = mapper
					.writeValueAsString(
							verificationEventStatusPOJO);
			onlineApiService.sendDataToVerificationEventStatus(
					verifictioneventstatusRestUrl, verificationEventStatusStr);
		}
	}

	private void saveReviewProcessAttempt(String checkId, ObjectMapper mapper, OnlineAttemptsPOJO onlineAttemptsPOJO) throws JsonProcessingException {
		if (StringUtils.isNotEmpty(checkId)) {
			
//			sendDataToL3ResultReviewAttempt(onlineAttemptsPOJO.getComponentScoping(), onlineAttemptsPOJO.getComponent(),
//					mapper, checkId);
			
			AttemptHistory attemptHistory = new AttemptHistory();
			attemptHistory.setAttemptStatusid((long) 68);// (68, ' Result Review - Manual -
															// ',
															// 'Valid', 3,
															// '2020-02-26
															// 13:28:06'),
			attemptHistory.setAttemptDescription("Result received to be Assigned");
			attemptHistory.setName("Product Name:"+onlineAttemptsPOJO.getProductName()+
					", API Name:"+onlineAttemptsPOJO.getApiName());
			attemptHistory.setCheckid(checkId);
			attemptHistory.setRequestid(onlineAttemptsPOJO.getCaseSpecificRecordId());

			AttemptStatusData attemptStatusData = new AttemptStatusData();
			attemptStatusData.setDepositionId((long) 14); // Record Found
			attemptStatusData.setEndstatusId((long) 40); // Completed
			attemptStatusData.setModeId((long) 14); // Blank
			saveAttempt(mapper, attemptHistory, attemptStatusData);
			
			VerificationEventStatusPOJO verificationEventStatusPOJO = new VerificationEventStatusPOJO();
			verificationEventStatusPOJO.setCheckId(checkId);
			verificationEventStatusPOJO.setEventName("online");
			verificationEventStatusPOJO.setEventType("auto");
			verificationEventStatusPOJO.setCaseNo(onlineAttemptsPOJO.getCaseNo()); 
			verificationEventStatusPOJO.setUser("System");
			verificationEventStatusPOJO.setStatus("Result received to be Assigned");
			verificationEventStatusPOJO.setRequestId(onlineAttemptsPOJO.getCaseSpecificRecordId());
			String verificationEventStatusStr = mapper
					.writeValueAsString(
							verificationEventStatusPOJO);
			onlineApiService.sendDataToVerificationEventStatus(
					verifictioneventstatusRestUrl, verificationEventStatusStr);
		}
	}
	
	private void saveDiscrepantProcessAttempt(String checkId, ObjectMapper mapper, OnlineAttemptsPOJO onlineAttemptsPOJO) throws JsonProcessingException {
		if (StringUtils.isNotEmpty(checkId)) {
			
//			sendDataToL3DiscrepantProcessAttempt(onlineAttemptsPOJO.getComponentScoping(), onlineAttemptsPOJO.getComponent(),
//					mapper, checkId);
			
			AttemptHistory attemptHistory = new AttemptHistory();
			attemptHistory.setAttemptStatusid((long) 69);// (68, ' Result Review - Manual -
															// ',
															// 'Valid', 3,
															// '2020-02-26
															// 13:28:06'),
			attemptHistory.setAttemptDescription("Record Found");
			attemptHistory.setName("Product Name:"+onlineAttemptsPOJO.getProductName()+
					", API Name:"+onlineAttemptsPOJO.getApiName());
			attemptHistory.setCheckid(checkId);
			attemptHistory.setRequestid(onlineAttemptsPOJO.getCaseSpecificRecordId());
			attemptHistory.setExecutiveSummary("Record Found");
			AttemptStatusData attemptStatusData = new AttemptStatusData();
			attemptStatusData.setDepositionId((long) 14); // Record Found
			attemptStatusData.setEndstatusId((long) 40); // Completed
			attemptStatusData.setModeId((long) 14); // Blank
			saveAttempt(mapper, attemptHistory, attemptStatusData);
			
			VerificationEventStatusPOJO verificationEventStatusPOJO = new VerificationEventStatusPOJO();
			verificationEventStatusPOJO.setCheckId(checkId);
			verificationEventStatusPOJO.setEventName("online");
			verificationEventStatusPOJO.setEventType("auto");
			verificationEventStatusPOJO.setCaseNo(onlineAttemptsPOJO.getCaseNo()); 
			verificationEventStatusPOJO.setUser("System");
			verificationEventStatusPOJO.setStatus("Record Found");
			verificationEventStatusPOJO.setRequestId(onlineAttemptsPOJO.getCaseSpecificRecordId());
			String verificationEventStatusStr = mapper
					.writeValueAsString(
							verificationEventStatusPOJO);
			onlineApiService.sendDataToVerificationEventStatus(
					verifictioneventstatusRestUrl, verificationEventStatusStr);
		}
	}
	
	private String sendDataToL3IntiatedAttempt(ComponentScoping componentScopingNode, Component component,
			ObjectMapper mapper, String checkId) throws JsonProcessingException {
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
		caseReference.setNgStatus("F1");
		caseReference.setNgStatusDescription("Followup 1");
		caseReference.setSbuName(componentScopingNode.getSBU_NAME());
		caseReference.setProductName(component.getPRODUCT());
		caseReference.setPackageName(componentScopingNode.getPackageName());
		caseReference.setComponentName(component.getComponentname());

		CheckVerificationPOJO checkVerification = new CheckVerificationPOJO();

		checkVerification.setCountry("India");
		checkVerification.setExecutiveSummaryComments("Comments");
		checkVerification.setReportComments("");
		checkVerification.setResultCode("");
		checkVerification.setEmailId("");
		checkVerification.setVerifierName("");
		checkVerification.setGeneralRemarks("");
		checkVerification.setModeOfVerification("");
		checkVerification.setVerifiedDate("");
		if (StringUtils.equalsAnyIgnoreCase(component.getComponentname(), "Database")) {
			checkVerification.setSubAction("databaseVerification");
		} else {
			checkVerification.setSubAction("verifyChecks");
		}
		checkVerification.setAction("verifyChecks");
		checkVerification.setActionCode("");
		checkVerification.setComponentName(component.getComponentname());
		checkVerification.setAttempts("Request Initiated");
		checkVerification.setDepartmentName("");
		checkVerification.setKeyWords("");
		checkVerification.setCost("");
		checkVerification.setIsThisAVerificationAttempt("Yes");
		checkVerification.setInternalNotes("Request Initiated Via Integration");
		checkVerification.setDateVerificationCompleted(todaysDateStr);
		checkVerification.setDisposition("");
		checkVerification.setExpectedClosureDate("");
		checkVerification.setEndStatusOfTheVerification("Work in Progress");
		checkVerification.setVerifierDesignation("");
		checkVerification.setFollowUpDateAndTimes("");
		checkVerification.setVerifierNumber("");

		taskSpecs.setCaseReference(caseReference);
		taskSpecs.setCheckVerification(checkVerification);

		taskSpecs.setQuestionaire(new ArrayList<>());

		FileUploadPOJO fileUpload = new FileUploadPOJO();
		fileUpload.setVerificationReplyDocument(new ArrayList<>());
		taskSpecs.setFileUpload(fileUpload);

		String getVerificationStatusforL3 = mapper.writeValueAsString(taskSpecs);
		String sendInfol3VeriStatus = null;

		try {
//			logger.info("getVerificationStatusforL3Node : {} ", getVerificationStatusforL3);
			sendInfol3VeriStatus = onlineApiService.sendDataToRest(verificationStatusL3Url, getVerificationStatusforL3,
					null);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return sendInfol3VeriStatus;
	}
	
	private String sendDataToL3ClearOutcomeAttempt(ComponentScoping componentScopingNode, 
			Component component, ObjectMapper mapper, String checkId) throws JsonProcessingException {
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
		caseReference.setNgStatus("Result Review - Clear");
		caseReference.setNgStatusDescription("Result received with No records");
		caseReference.setSbuName(componentScopingNode.getSBU_NAME());
		caseReference.setProductName(component.getPRODUCT());
		caseReference.setPackageName(componentScopingNode.getPackageName());
		caseReference.setComponentName(component.getComponentname());

		CheckVerificationPOJO checkVerification = new CheckVerificationPOJO();

		checkVerification.setCountry("India");
		checkVerification.setExecutiveSummaryComments("No Record");
		checkVerification.setReportComments("");
		checkVerification.setResultCode("");
		checkVerification.setEmailId("");
		checkVerification.setVerifierName("");
		checkVerification.setGeneralRemarks("");
		checkVerification.setModeOfVerification("Online");
		checkVerification.setVerifiedDate("");
		checkVerification.setAction("verifyChecks");
		if (StringUtils.equalsAnyIgnoreCase(component.getComponentname(), "Database")) {
			checkVerification.setSubAction("databaseVerification");
		} else {
			checkVerification.setSubAction("verifyChecks");
		}
		checkVerification.setActionCode("");
		checkVerification.setComponentName(component.getComponentname());
		checkVerification.setAttempts("");
		checkVerification.setDepartmentName("");
		checkVerification.setKeyWords("");
		checkVerification.setCost("");
		checkVerification.setIsThisAVerificationAttempt("No");
		checkVerification
				.setInternalNotes("Result received with No records");
		checkVerification.setDateVerificationCompleted(todaysDateStr);
		checkVerification.setDisposition("");
		checkVerification.setExpectedClosureDate("");
		checkVerification.setEndStatusOfTheVerification("Clear");
		checkVerification.setVerifierDesignation("");
		checkVerification.setFollowUpDateAndTimes("");
		checkVerification.setVerifierNumber("");

		taskSpecs.setCaseReference(caseReference);
		taskSpecs.setCheckVerification(checkVerification);

		
		  taskSpecs.setQuestionaire(new ArrayList<>());
		  
		  FileUploadPOJO fileUpload = new FileUploadPOJO();
		  fileUpload.setVerificationReplyDocument(new ArrayList<>());
		  taskSpecs.setFileUpload(fileUpload);
		 

		String getVerificationStatusforL3 = mapper.writeValueAsString(taskSpecs);
		String sendInfol3VeriStatus = null;

		try {
//			logger.info("getVerificationStatusforL3Node : {} ", getVerificationStatusforL3);
			sendInfol3VeriStatus = onlineApiService.sendDataToRest(verificationStatusL3Url, getVerificationStatusforL3,
					null);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return sendInfol3VeriStatus;
	}
	
	private String sendDataToL3ResultReviewAttempt(ComponentScoping componentScopingNode, 
			Component component, ObjectMapper mapper, String checkId) throws JsonProcessingException {
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
		caseReference.setNgStatus("F1");
		caseReference.setNgStatusDescription("Followup 1");
		caseReference.setSbuName(componentScopingNode.getSBU_NAME());
		caseReference.setProductName(component.getPRODUCT());
		caseReference.setPackageName(componentScopingNode.getPackageName());
		caseReference.setComponentName(component.getComponentname());

		CheckVerificationPOJO checkVerification = new CheckVerificationPOJO();

		checkVerification.setCountry("India");
		checkVerification.setExecutiveSummaryComments("Comments");
		checkVerification.setReportComments("");
		checkVerification.setResultCode("");
		checkVerification.setEmailId("");
		checkVerification.setVerifierName("");
		checkVerification.setGeneralRemarks("");
		checkVerification.setModeOfVerification("");
		checkVerification.setVerifiedDate("");
		checkVerification.setAction("verifyChecks");
		if (StringUtils.equalsAnyIgnoreCase(component.getComponentname(), "Database")) {
			checkVerification.setSubAction("databaseVerification");
		} else {
			checkVerification.setSubAction("verifyChecks");
		}
		checkVerification.setActionCode("");
		checkVerification.setComponentName(component.getComponentname());
		checkVerification.setAttempts("Result Review - Manual");
		checkVerification.setDepartmentName("");
		checkVerification.setKeyWords("");
		checkVerification.setCost("");
		checkVerification.setIsThisAVerificationAttempt("Yes");
		checkVerification
				.setInternalNotes("Result received to be Assigned");
		checkVerification.setDateVerificationCompleted(todaysDateStr);
		checkVerification.setDisposition("Record Found");
		checkVerification.setExpectedClosureDate("");
		checkVerification.setEndStatusOfTheVerification("Completed");
		checkVerification.setVerifierDesignation("");
		checkVerification.setFollowUpDateAndTimes("");
		checkVerification.setVerifierNumber("");

		taskSpecs.setCaseReference(caseReference);
		taskSpecs.setCheckVerification(checkVerification);

		
		  taskSpecs.setQuestionaire(new ArrayList<>());
		  
		  FileUploadPOJO fileUpload = new FileUploadPOJO();
		  fileUpload.setVerificationReplyDocument(new ArrayList<>());
		  taskSpecs.setFileUpload(fileUpload);
		 

		String getVerificationStatusforL3 = mapper.writeValueAsString(taskSpecs);
		String sendInfol3VeriStatus = null;

		try {
//			logger.info("getVerificationStatusforL3Node : {} ", getVerificationStatusforL3);
			sendInfol3VeriStatus = onlineApiService.sendDataToRest(verificationStatusL3Url, getVerificationStatusforL3,
					null);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return sendInfol3VeriStatus;
	}
	
	
	private String sendDataToL3DiscrepantProcessAttempt(ComponentScoping componentScopingNode, 
			Component component, ObjectMapper mapper, String checkId) throws JsonProcessingException {
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
		caseReference.setNgStatus("Result Review - Record Found");
		caseReference.setNgStatusDescription("Record Found");
		caseReference.setSbuName(componentScopingNode.getSBU_NAME());
		caseReference.setProductName(component.getPRODUCT());
		caseReference.setPackageName(componentScopingNode.getPackageName());
		caseReference.setComponentName(component.getComponentname());

		CheckVerificationPOJO checkVerification = new CheckVerificationPOJO();

		checkVerification.setCountry("India");
		checkVerification.setExecutiveSummaryComments("Possible Hit");
		checkVerification.setReportComments("");
		checkVerification.setResultCode("");
		checkVerification.setEmailId("");
		checkVerification.setVerifierName("");
		checkVerification.setGeneralRemarks("");
		checkVerification.setModeOfVerification("Online");
		checkVerification.setVerifiedDate("");
		checkVerification.setAction("verifyChecks");
		if (StringUtils.equalsAnyIgnoreCase(component.getComponentname(), "Database")) {
			checkVerification.setSubAction("databaseVerification");
		} else {
			checkVerification.setSubAction("verifyChecks");
		}
		checkVerification.setActionCode("");
		checkVerification.setComponentName(component.getComponentname());
		//checkVerification.setAttempts("Result Review - Record Found");
		checkVerification.setAttempts("");
		checkVerification.setDepartmentName("");
		checkVerification.setKeyWords("");
		checkVerification.setCost("");
		checkVerification.setIsThisAVerificationAttempt("No");
		checkVerification
				.setInternalNotes("Record Found");
		checkVerification.setDateVerificationCompleted(todaysDateStr);
		checkVerification.setDisposition("");
		checkVerification.setExpectedClosureDate("");
		checkVerification.setEndStatusOfTheVerification("Possible Hit");
		checkVerification.setVerifierDesignation("");
		checkVerification.setFollowUpDateAndTimes("");
		checkVerification.setVerifierNumber("");

		taskSpecs.setCaseReference(caseReference);
		taskSpecs.setCheckVerification(checkVerification);

		
		  taskSpecs.setQuestionaire(new ArrayList<>());
		  
		  FileUploadPOJO fileUpload = new FileUploadPOJO();
		  fileUpload.setVerificationReplyDocument(new ArrayList<>());
		  taskSpecs.setFileUpload(fileUpload);
		 

		String getVerificationStatusforL3 = mapper.writeValueAsString(taskSpecs);
		String sendInfol3VeriStatus = null;

		try {
//			logger.info("getVerificationStatusforL3Node : {} ", getVerificationStatusforL3);
			sendInfol3VeriStatus = onlineApiService.sendDataToRest(verificationStatusL3Url, getVerificationStatusforL3,
					null);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return sendInfol3VeriStatus;
	}
	
}