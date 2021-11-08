package fadv.verification.workflow.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fadv.verification.workflow.model.AttemptHistory;
import fadv.verification.workflow.model.AttemptStatusData;
import fadv.verification.workflow.model.CaseSpecificInfo;
import fadv.verification.workflow.model.CaseSpecificRecordDetail;
import fadv.verification.workflow.model.RouterHistory;
import fadv.verification.workflow.model.VerificationEventStatus;
import fadv.verification.workflow.pojo.SPOCEmailConfigPOJO;
import fadv.verification.workflow.repository.AttemptHistoryRepository;
import fadv.verification.workflow.repository.AttemptStatusDataRepository;
import fadv.verification.workflow.repository.CaseSpecificRecordDetailRepository;
import fadv.verification.workflow.repository.RouterHistoryRepository;
import fadv.verification.workflow.repository.VerificationEventStatusRepository;
import fadv.verification.workflow.utility.Utility;

@Service
public class SpocRouterServiceImpl implements SpocRouterService {

	private static final String FAILED = "Failed";

	@Autowired
	private ApiService apiService;

	@Autowired
	private RouterHistoryRepository routerHistoryRepository;

	@Autowired
	private AttemptHistoryRepository attemptHistoryRepository;

	@Autowired
	private AttemptStatusDataRepository attemptStatusDataRepository;

	@Autowired
	private VerificationEventStatusRepository verificationEventStatusRepository;

	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;

	@Value("${router.spoc.rest.url}")
	private String spocRouterUrl;
	
	@Value("${holiday.list.url}")
	private String holidayListUrl;

	private static final String SUCCESS = "success";
	private static final Logger logger = LoggerFactory.getLogger(SpocRouterServiceImpl.class);
	private static final String RESULT = "result";
	private static final String L3_STATUS = "l3Status";
	private static final String L3_RESPONSE = "l3Response";
	private static final String PROCESSED = "Processed";
	private static final String SPOC_FOUND = "SPOC records found";
	private static final String SPOC_EMAIL_RESPONSE = "spocEmailResponse";
	
	@Override
	public void processRouters(ObjectMapper mapper, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo, String requestStr, JsonNode requestNode) throws JsonProcessingException {
		if (StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getComponentName(), "Employment")
				&& StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getProduct(), "HR")
				&& StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getCheckStatus(), "Open")
				&& !StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getSpocStatus(), PROCESSED)
				) {
			try {
				processSpocRouters(mapper, caseSpecificRecordDetail, caseSpecificInfo, requestStr,
						requestNode);
			} catch (JsonProcessingException e) {
				logger.error("Exception while processing Spoc Router : {}", e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void processSpocRouters(ObjectMapper mapper, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo, String requestStr, JsonNode requestNode) throws JsonProcessingException {

		RouterHistory routerHistory = new RouterHistory();
		routerHistory.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		routerHistory.setEngineName("SPOC");
		routerHistory.setStartTime(new Date());
		routerHistory.setEngineRequest(requestNode);
		routerHistory.setCaseNumber(caseSpecificInfo.getCaseNumber());
		routerHistory.setCurrentEngineStatus("Initiated");
		routerHistory.setCaseSpecificRecordDetailId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		routerHistory.setEngineResponse(null);
		routerHistory.setEndTime(null);
		RouterHistory newRouterHistory = routerHistoryRepository.save(routerHistory);

		createSpocPreVerificationEvent(caseSpecificRecordDetail, caseSpecificInfo);
		String responsStr = apiService.sendDataToPost(spocRouterUrl, requestStr);
		logger.info("Spoc router response : {}", responsStr);
		JsonNode responseNode = mapper.readValue(responsStr, JsonNode.class);

		boolean success = responseNode.has(SUCCESS) ? responseNode.get(SUCCESS).asBoolean() : Boolean.FALSE;
		String routerResult = responseNode.has(RESULT) ? responseNode.get(RESULT).asText() : "";
		String spocEmailStr = responseNode.has(SPOC_EMAIL_RESPONSE) ? responseNode.get(SPOC_EMAIL_RESPONSE).asText()
				: "{}";
		JsonNode spocEmailNode = mapper.readValue(spocEmailStr, JsonNode.class);
		spocEmailNode = spocEmailNode != null ? spocEmailNode : mapper.createObjectNode();

		SPOCEmailConfigPOJO spocEmailConfigPOJO = new SPOCEmailConfigPOJO();
		if (spocEmailNode != null && !spocEmailNode.isEmpty()) {
			spocEmailConfigPOJO = mapper.convertValue(spocEmailNode, SPOCEmailConfigPOJO.class);
		}
		logger.info("spoc email response node : {}", spocEmailNode);
		if (Boolean.TRUE.equals(success) && StringUtils.isNotEmpty(routerResult)) {
			if(!routerResult.equalsIgnoreCase("SPOC FOUND FOR BULK")) {
				createSpocAttempt(routerResult, caseSpecificRecordDetail, responseNode, spocEmailConfigPOJO);
			}
			createSpocPostVerificationEvent(routerResult, caseSpecificRecordDetail, caseSpecificInfo);
			if(routerResult.equalsIgnoreCase("SPOC records not found")) {
				//Updated is Manual Check true
				caseSpecificRecordDetail.setIsCheckManual(true);
			}
			caseSpecificRecordDetail.setSpocStatus(PROCESSED);
			caseSpecificRecordDetail.setUpdatedDate(new Date());
			caseSpecificRecordDetailRepository.save(caseSpecificRecordDetail);
			newRouterHistory.setCurrentEngineStatus(PROCESSED);
		} else {
			createSpocPostVerificationEvent(FAILED, caseSpecificRecordDetail, caseSpecificInfo);
			newRouterHistory.setCurrentEngineStatus(FAILED);
			//Updated is Manual Check true
			caseSpecificRecordDetail.setIsCheckManual(true);
			caseSpecificRecordDetailRepository.save(caseSpecificRecordDetail);
		}
		newRouterHistory.setEndTime(new Date());
		newRouterHistory.setEngineResponse(responseNode);
		routerHistoryRepository.save(newRouterHistory);
	}

	private void createSpocAttempt(String routerResult, CaseSpecificRecordDetail caseSpecificRecordDetail,
			JsonNode responseNode, SPOCEmailConfigPOJO spocEmailConfigPOJO) {
		AttemptHistory attemptHistory = new AttemptHistory();

		String l3Response = responseNode.has(L3_RESPONSE) ? responseNode.get(L3_RESPONSE).asText() : "";
		String l3Status = responseNode.has(L3_STATUS) ? responseNode.get(L3_STATUS).asText() : "";
		if (StringUtils.equalsIgnoreCase(routerResult, SPOC_FOUND)) {
			attemptHistory.setAttemptDescription(
					"Name not disclosed, Official from Human Resource Department advised all verifications are handled via email. We have complied with this request.");
		}

		attemptHistory.setAttemptStatusid((long) 10);
		attemptHistory.setName("Name not disclosed");
		attemptHistory.setName(spocEmailConfigPOJO.getSourceName());
		attemptHistory.setEmailAddress(spocEmailConfigPOJO.getToEmailID());
		attemptHistory.setJobTitle(spocEmailConfigPOJO.getHRDesignation());
		attemptHistory.setCheckid(caseSpecificRecordDetail.getInstructionCheckId());
		attemptHistory.setFollowupId((long) 2);
		attemptHistory.setRequestid(caseSpecificRecordDetail.getCaseSpecificDetailId());
		attemptHistory.setContactDate(new Date().toString());
		attemptHistory.setL3Response(l3Response);
		attemptHistory.setL3Status(l3Status);
		
		String response = apiService.sendDataToGet(holidayListUrl);
		response = response.replace("[", "").replace("]", "").replace("\"", "");
		
		String[] split = response.split(",");
		List<String> holidayList = Arrays.asList(split);
		holidayList.replaceAll(String::trim);
		
		logger.info("holidayList : {}", holidayList);
		
		int followUpDays = Integer.parseInt(spocEmailConfigPOJO.getFollowUpDate1());
		attemptHistory.setFollowupDate((Utility.addDaysSkippingWeekends(new Date(), followUpDays, holidayList)).toString());
		int expectedClosureDate = Integer.parseInt(spocEmailConfigPOJO.getExpectedClosureDate());
		attemptHistory
				.setClosureExpectedDate((Utility.addDaysSkippingWeekends(new Date(), expectedClosureDate, holidayList)).toString());
		Date contactDate = new Date();
		attemptHistory.setContactDate(contactDate.toString());

		AttemptHistory newAttemptHistory = attemptHistoryRepository.save(attemptHistory);
		createAttemptStatusData(newAttemptHistory);
	}

	private void createAttemptStatusData(AttemptHistory attemptHistory) {
		Long attemptId = attemptHistory.getAttemptid() != null ? attemptHistory.getAttemptid() : 0;
		if (attemptId != 0) {
			AttemptStatusData attemptStatusData = new AttemptStatusData();
			attemptStatusData.setAttemptId(attemptId);
			attemptStatusData.setDepositionId((long) 13);
			attemptStatusData.setEndstatusId((long) 1);
			attemptStatusData.setModeId((long) 14);
			attemptStatusDataRepository.save(attemptStatusData);
		}
	}

	private void createSpocPreVerificationEvent(CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo) {
		VerificationEventStatus verificationEventStatus = new VerificationEventStatus();

		verificationEventStatus.setEvent("Request Initiated");
		verificationEventStatus.setStatus("Day 0");

		verificationEventStatus.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		verificationEventStatus.setStage("SPOC");
		verificationEventStatus.setEventType("auto");
		verificationEventStatus.setCaseNo(caseSpecificInfo.getCaseNumber());
		//verificationEventStatus.setUser("System");
		verificationEventStatus.setRequestId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		verificationEventStatusRepository.save(verificationEventStatus);
	}

	private void createSpocPostVerificationEvent(String routerResult, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo) {
		VerificationEventStatus verificationEventStatus = new VerificationEventStatus();
		if (StringUtils.equalsIgnoreCase(routerResult, SPOC_FOUND)) {
			verificationEventStatus.setStatus("F2");
		} else {
			verificationEventStatus.setStatus("Day 0");
		}
		verificationEventStatus.setEvent(routerResult);
		verificationEventStatus.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		verificationEventStatus.setStage("SPOC");
		verificationEventStatus.setEventType("auto");
		verificationEventStatus.setCaseNo(caseSpecificInfo.getCaseNumber());
		//verificationEventStatus.setUser("System");
		verificationEventStatus.setRequestId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		verificationEventStatusRepository.save(verificationEventStatus);
	}
}
