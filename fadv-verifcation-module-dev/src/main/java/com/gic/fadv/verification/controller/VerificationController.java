package com.gic.fadv.verification.controller;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gic.fadv.verification.attempts.model.AttemptHistory;
import com.gic.fadv.verification.attempts.model.AttemptStatusData;
import com.gic.fadv.verification.attempts.model.CaseSpecificInfo;
import com.gic.fadv.verification.attempts.model.CaseSpecificRecordDetail;
import com.gic.fadv.verification.attempts.repository.CaseSpecificInfoRepository;
import com.gic.fadv.verification.attempts.repository.CaseSpecificRecordDetailRepository;
import com.gic.fadv.verification.attempts.service.APIService;
import com.gic.fadv.verification.model.Component;
import com.gic.fadv.verification.model.ComponentScoping;
import com.gic.fadv.verification.model.VerificationReq;
import com.gic.fadv.verification.online.service.OnlineApiService;
import com.gic.fadv.verification.pojo.CityListPOJO;
import com.gic.fadv.verification.pojo.L3AkaNamePOJO;
import com.gic.fadv.verification.pojo.VerificationEventStatusPOJO;
import com.google.gson.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india/")
public class VerificationController {

	private static final Logger logger = LoggerFactory.getLogger(VerificationController.class);

	@Autowired
	private Environment env;

	@Autowired
	APIService apiService;

	@Autowired
	private OnlineApiService onlineApiService;

	@Autowired
	private CaseSpecificInfoRepository caseSpecificInfoRepository;

	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;

	@Value("${attemptstatusdata.rest.url}")
	private String attemptStatusDataRestUrl;

	@Value("${document.list.l3.url}")
	private String documentListl3Url;

	@Value("${unique.fields.l3.url}")
	private String uniqueFieldsL3Url;

	@Value("${l3.city.list.url}")
	private String l3CityListUrl;

	@Value("${aka.name.l3.url}")
	private String akaNameUrl;

	@Value("${verificationeventstatus.rest.url}")
	private String verificationeventstatusDataRestUrl;

	@PostMapping(path = "/async/verification", consumes = "application/json", produces = "application/json")
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

	@PostMapping(path = "/verification", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> doProcess(@RequestBody String inStr) {
		try {
			logger.info("Got Request:\n" + inStr);
			String response = processRequest(inStr, false);
			logger.info("Processed Request");
			logger.info("datarouter Response" + response);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}

	private String processRequest(String inStr, boolean asyncStatus) {
		try {
			LocalDateTime startTime = LocalDateTime.now();
			// Creating the ObjectMapper object
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			// Converting the JSONString to Object
			VerificationReq verificationReq = mapper.readValue(inStr, VerificationReq.class);

			String finalResponseString = "";
			/* Parse Json */
			for (int i = 0; i < verificationReq.getData().size(); i++) {
				List<ComponentScoping> componentScoping = verificationReq.getData().get(i).getTaskSpecs()
						.getComponentScoping();
				for (int j = 0; j < componentScoping.size(); j++) {

					CaseSpecificInfo caseSpecificInfo = new CaseSpecificInfo();
					caseSpecificInfo.setCandidateName(componentScoping.get(j).getCandidate_Name());
					caseSpecificInfo.setCaseDetails(componentScoping.get(j).getCaseDetails().toString());
					caseSpecificInfo.setCaseMoreInfo(componentScoping.get(j).getCaseMoreInfo().toString());
					caseSpecificInfo.setCaseReference(componentScoping.get(j).getCaseReference().toString());
					caseSpecificInfo.setCaseRefNumber(componentScoping.get(j).getCASE_REF_NUMBER());
					caseSpecificInfo.setCaseNumber(componentScoping.get(j).getCASE_NUMBER());
					caseSpecificInfo.setClientCode(componentScoping.get(j).getCLIENT_CODE());
					caseSpecificInfo.setClientName(componentScoping.get(j).getCLIENT_NAME());
					caseSpecificInfo.setSbuName(componentScoping.get(j).getSBU_NAME());
					caseSpecificInfo.setPackageName(componentScoping.get(j).getPackageName());
					caseSpecificInfo
							.setClientSpecificFields(componentScoping.get(j).getClientSpecificFields().toString());

					CaseSpecificInfo caseSpecificInfoNew = caseSpecificInfoRepository.save(caseSpecificInfo);

					AttemptHistory attemptHistory = new AttemptHistory();

					List<Component> component = componentScoping.get(j).getComponents();
					for (int k = 0; k < component.size(); k++) {
						List<JsonNode> records = component.get(k).getRecords();
						if (records.size() > 0) {
							for (int l = 0; l < records.size(); l++) {
								String checkId = records.get(l).get("checkId") != null
										? records.get(l).get("checkId").asText()
										: "";
								if (StringUtils.isNotEmpty(checkId)) {
									/*
									 * Logic for Saving information in Database tables Client Specific Tables and
									 * Client Specific Record details
									 */
									logger.info("Make Rest call to Save");
									attemptHistory = new AttemptHistory();
									attemptHistory.setAttemptStatusid((long) 10);// (10, ' Email - Sent', 'Valid', 3,
																					// '2020-02-26 13:28:06'),
									attemptHistory.setAttemptDescription(
											"Name not disclosed, Official from Human Resource Department advised all verifications are handled via email. We have complied with this request.");
									attemptHistory.setName("Verification");
									attemptHistory.setJobTitle("Official");
									Date contactDate = new Date();
									attemptHistory.setContactDate(contactDate.toString());
									attemptHistory.setCheckid(checkId);
									saveAttempt(mapper, attemptHistory);

									VerificationEventStatusPOJO verificationEventStatusPOJO = new VerificationEventStatusPOJO();
									verificationEventStatusPOJO.setCheckId(checkId);
									verificationEventStatusPOJO.setEventName("verification");
									verificationEventStatusPOJO.setEventType("auto");

									String verificationEventStatusStr = mapper
											.writeValueAsString(verificationEventStatusPOJO);
									apiService.sendDataToVerificationEventStatus(verificationeventstatusDataRestUrl,
											verificationEventStatusStr);

									if (caseSpecificInfoNew.getCaseSpecificId() != 0) {
										CaseSpecificRecordDetail caseSpecificRecordDetail = new CaseSpecificRecordDetail();
										caseSpecificRecordDetail
												.setCaseSpecificId(caseSpecificInfoNew.getCaseSpecificId());
										caseSpecificRecordDetail.setComponentName(component.get(k).getComponentname());
										caseSpecificRecordDetail.setProduct(component.get(k).getPRODUCT());
										caseSpecificRecordDetail.setComponentRecordField(records.get(l).toString());
										caseSpecificRecordDetail.setInstructionCheckId(checkId);
										caseSpecificRecordDetailRepository.save(caseSpecificRecordDetail);
										/* Save in Attempt History */

									} else {
										logger.info("Record failed to Save");
									}

									logger.info("Value of Instruction Check ID : {}", checkId);
								} else {
									logger.info("Instruction Check Id is Null : {}", checkId);
								}
							}
						}
					}
				}
			}

			finalResponseString = mapper.writeValueAsString(verificationReq);
			logger.info("Data Router process Json\n");
			logger.info(finalResponseString);
			// JsonObject = new JsonObject();
			// Converting the JSONString to Object
			logger.info("Adding Metrics and log Json Elements");
			JsonNode dataObj = mapper.readTree(finalResponseString);
			ObjectNode metricsObj = mapper.createObjectNode();

			LocalDateTime endTime = LocalDateTime.now();
			metricsObj.put("startTime", startTime.toString());
			metricsObj.put("endTime", endTime.toString());
			metricsObj.put("timeInMillis", "0");
			metricsObj.put("timeInSeconds", "0");
			metricsObj.put("statusCode", "OK");

			((ObjectNode) dataObj.get("data").get(0)).set("metrics", metricsObj);
			ObjectNode logs = mapper.createObjectNode();
			// JsonObject logs = new JsonObject();
			logs.put("field1", "No LOG");
			((ObjectNode) dataObj.get("data").get(0)).set("logs", logs);

			// Adding Status in Meta Data Also
			ObjectNode statusObj = mapper.createObjectNode();
			statusObj.put("success", true);
			statusObj.put("message", "Executon done successfullly");
			statusObj.put("statusCode", "200");
			((ObjectNode) dataObj.get("metadata")).set("status", statusObj);

			finalResponseString = mapper.writeValueAsString(dataObj);

			String returnStr = finalResponseString.replace("taskSpecs", "result");
			logger.debug("Response:\n" + returnStr);
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

	private void saveAttempt(ObjectMapper mapper, AttemptHistory attemptHistory) {

		try {
			JsonNode attemptHistoryJsonNode = mapper.valueToTree(attemptHistory);
			logger.info("Value of attemptHistory bean to Json" + attemptHistoryJsonNode);
			String attemptHistoryStr = mapper.writeValueAsString(attemptHistoryJsonNode);
			logger.info("Value of attemptHistory Json to String" + attemptHistoryStr);

			String attemptHistoryResponse = null;
			AttemptHistory attemptHistoryNew = new AttemptHistory();
			try {
				attemptHistoryResponse = apiService.sendDataToAttemptHistory(attemptHistoryStr);
				logger.info("Attempt History Response" + attemptHistoryResponse);
				attemptHistoryNew = mapper.readValue(attemptHistoryResponse, new TypeReference<AttemptHistory>() {
				});
				if (attemptHistoryNew.getAttemptid() != null) {
					logger.info("Attempt saved sucessfully." + attemptHistoryNew.getAttemptid());

					AttemptStatusData attemptStatusData = new AttemptStatusData();
					attemptStatusData.setAttemptId(attemptHistoryNew.getAttemptid());
					attemptStatusData.setDepositionId((long) 13);
					attemptStatusData.setEndstatusId((long) 1);
					attemptStatusData.setModeId((long) 14);

					JsonNode attemptStatusDataJsonNode = mapper.valueToTree(attemptStatusData);
					logger.info("Value of attemptStatusData bean to Json" + attemptStatusDataJsonNode);
					String attemptStatusDataStr = mapper.writeValueAsString(attemptStatusDataJsonNode);
					logger.info("Value of attemptStatusData Json to String" + attemptStatusDataStr);

					String attemptStatusDataResponse = null;
					AttemptStatusData attemptStatusDataNew = new AttemptStatusData();

					try {
						attemptStatusDataResponse = apiService.sendDataToAttempt(attemptStatusDataRestUrl,
								attemptStatusDataStr);
						logger.info("Attempt status data Response" + attemptStatusDataResponse);
						attemptStatusDataNew = mapper.readValue(attemptStatusDataResponse,
								new TypeReference<AttemptStatusData>() {
								});
						if (attemptStatusDataNew.getStatusId() != 0) {
							logger.info("Attempt status data saved sucessfully." + attemptStatusDataNew.getStatusId());
						} else {
							logger.info("Attempt status data not saved.");
						}
					} catch (Exception e) {
						logger.info("Exception in calling save attempt status data " + e.getMessage());
						e.printStackTrace();
					}

				} else {
					logger.info("Attempt history not saved.");
				}
			} catch (Exception e) {
				logger.info("Exception in calling save attempt history " + e.getMessage());
				e.printStackTrace();
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void callback(String postStr) {
		try {

			logger.debug("postStr\n" + postStr);
			URL url = new URL(env.getProperty("data.router.callback.url"));

			logger.debug("Using callback URL\n" + url);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");

			String authToken = JsonParser.parseString(postStr).getAsJsonObject().get("metadata").getAsJsonObject()
					.get("requestAuthToken").getAsString();
			logger.debug("Auth Token\n" + authToken);
			con.setRequestProperty("tokenId", authToken);

			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			os.write(postStr.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();
			logger.debug("Callback POST Response Code: " + responseCode + " : " + con.getResponseMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@GetMapping(path = "extcspi/document", produces = "application/json")
	public String getDocumentList() {
		return onlineApiService.sendDataToL3Get(documentListl3Url);
	}

	@GetMapping(path = "extcspi/getuniquefields", produces = "application/json")
	public String getfieldList() {
		return onlineApiService.sendDataToL3Get(uniqueFieldsL3Url);
	}

	@GetMapping(path = "akaname/search/{searchString}", produces = "application/json")
	public List<L3AkaNamePOJO> searchAkaName(@PathVariable(value = "searchString") String searchString)
			throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		String akaNameUrlL3 = akaNameUrl + searchString.trim();
		String response = onlineApiService.sendDataToL3Get(akaNameUrlL3);

		if (response != null && !response.isEmpty()) {
			JsonNode responseNode = mapper.readTree(response);
			if (responseNode != null && !responseNode.isEmpty()) {
				ArrayNode dataNode = responseNode.has("response") ? (ArrayNode) responseNode.get("response")
						: mapper.createArrayNode();
				if (!dataNode.isEmpty()) {
					List<L3AkaNamePOJO> l3AkaNamePOJOs = mapper.convertValue(dataNode,
							new TypeReference<List<L3AkaNamePOJO>>() {
							});
					return new ArrayList<>(new HashSet<>(l3AkaNamePOJOs));
				}
			}
		}
		return new ArrayList<>();
	}

	@GetMapping("/get-indian-city-list")
	public List<CityListPOJO> getIndianCityList() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		ObjectNode requestNode = mapper.createObjectNode();
		requestNode.put("countryName", "India");

		String responseCity = onlineApiService.sendDataToL3Post(l3CityListUrl, requestNode.toString(), null);

		if (responseCity != null && !responseCity.isEmpty()) {
			JsonNode responseNode = mapper.readTree(responseCity);
			if (responseNode != null && !responseNode.isEmpty()) {
				ArrayNode dataNode = responseNode.has("data") ? (ArrayNode) responseNode.get("data")
						: mapper.createArrayNode();
				if (!dataNode.isEmpty()) {
					return mapper.convertValue(dataNode, new TypeReference<List<CityListPOJO>>() {
					});
				}
			}
		}
		return new ArrayList<>();
	}
}