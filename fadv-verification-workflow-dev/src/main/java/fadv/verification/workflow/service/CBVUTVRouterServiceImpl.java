package fadv.verification.workflow.service;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
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
import fadv.verification.workflow.repository.AttemptHistoryRepository;
import fadv.verification.workflow.repository.AttemptStatusDataRepository;
import fadv.verification.workflow.repository.CaseSpecificRecordDetailRepository;
import fadv.verification.workflow.repository.RouterHistoryRepository;
import fadv.verification.workflow.repository.VerificationEventStatusRepository;

@Service
public class CBVUTVRouterServiceImpl implements CBVUTVRouterService {
	private static final String FAILED = "Failed";
	private static final String MOVE_TO_NEXT_ENGINE = "Move to next engine";
	@Autowired
	private ApiService apiService;

	@Autowired
	private RouterHistoryRepository routerHistoryRepository;

	@Autowired
	private SpocRouterService spocRouterService;

	@Autowired
	private AttemptHistoryRepository attemptHistoryRepository;

	@Autowired
	private AttemptStatusDataRepository attemptStatusDataRepository;

	@Autowired
	private VerificationEventStatusRepository verificationEventStatusRepository;

	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;

	@Value("${router.cbvutvi4v.rest.url}")
	private String cbvUtvRouterUrl;

	private static final String SUCCESS = "success";
	private static final String CANNOT_VERIFY = "Cannot be verified";
	private static final String UNABLE_VERIFY = "Unable to Verify";
	private static final String RESULT = "result";
	private static final String L3_STATUS = "l3Status";
	private static final String L3_RESPONSE = "l3Response";
	private static final String PROCESSED = "Processed";

	@Override
	public void processCbvUtvRouters(ObjectMapper mapper, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo, String requestStr, JsonNode requestNode) throws JsonProcessingException {

		RouterHistory routerHistory = new RouterHistory();
		routerHistory.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		routerHistory.setEngineName("CBVUTV");
		routerHistory.setStartTime(new Date());
		routerHistory.setEngineRequest(requestNode);
		routerHistory.setCaseNumber(caseSpecificInfo.getCaseNumber());
		routerHistory.setCurrentEngineStatus("Initiated");
		routerHistory.setCaseSpecificRecordDetailId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		routerHistory.setEngineResponse(null);
		routerHistory.setEndTime(null);
		RouterHistory newRouterHistory = routerHistoryRepository.save(routerHistory);

		createCbvUtvPreVerificationEvent(caseSpecificRecordDetail, caseSpecificInfo);
		String responsStr = apiService.sendDataToPost(cbvUtvRouterUrl, requestStr);
		JsonNode responseNode = mapper.readValue(responsStr, JsonNode.class);

		boolean spocRouter = Boolean.FALSE;
		boolean success = responseNode.has(SUCCESS) ? responseNode.get(SUCCESS).asBoolean() : Boolean.FALSE;
		String routerResult = responseNode.has(RESULT) ? responseNode.get(RESULT).asText() : "";
		if (Boolean.TRUE.equals(success) && StringUtils.isNotEmpty(routerResult)) {
			if (StringUtils.equalsIgnoreCase(routerResult, MOVE_TO_NEXT_ENGINE)) {
				spocRouter = Boolean.TRUE;
			} else {
				createCbvUtvAttempt(routerResult, caseSpecificRecordDetail, responseNode);
			}
			createCbvUtvPostVerificationEvent(routerResult, caseSpecificRecordDetail, caseSpecificInfo);
			caseSpecificRecordDetail.setCbvUtvStatus(PROCESSED);
			caseSpecificRecordDetail.setUpdatedDate(new Date());
			caseSpecificRecordDetailRepository.save(caseSpecificRecordDetail);
			newRouterHistory.setCurrentEngineStatus(PROCESSED);

		} else {
			createCbvUtvPostVerificationEvent(FAILED, caseSpecificRecordDetail, caseSpecificInfo);
			newRouterHistory.setCurrentEngineStatus(FAILED);
		}
		newRouterHistory.setEndTime(new Date());
		newRouterHistory.setEngineResponse(responseNode);
		routerHistoryRepository.save(newRouterHistory);

		if (Boolean.TRUE.equals(spocRouter)) {
			spocRouterService.processRouters(mapper, caseSpecificRecordDetail, caseSpecificInfo, requestStr,
					requestNode);
		}
	}

	private void createCbvUtvAttempt(String routerResult, CaseSpecificRecordDetail caseSpecificRecordDetail,
			JsonNode responseNode) {
		AttemptHistory attemptHistory = new AttemptHistory();

		String l3Response = responseNode.has(L3_RESPONSE) ? responseNode.get(L3_RESPONSE).asText() : "";
		String l3Status = responseNode.has(L3_STATUS) ? responseNode.get(L3_STATUS).asText() : "";

		if (StringUtils.equalsIgnoreCase(routerResult, CANNOT_VERIFY)) {
			attemptHistory.setAttemptDescription(
					"Employment details not disclosed as per Company policy. Hence cannot verify.");
		} else if (StringUtils.equalsIgnoreCase(routerResult, UNABLE_VERIFY)) {
			attemptHistory.setAttemptDescription(
					"Name not disclosed, Official from the Human Resource Department verbally stated that employment details will not be disclosed to First Advantage Private Limited citing mutual process disagreement. Hence Unable to verify");
		}
		attemptHistory.setAttemptStatusid((long) 55);
		attemptHistory.setName("Name not disclosed");
		attemptHistory.setJobTitle("Official");
		attemptHistory.setCheckid(caseSpecificRecordDetail.getInstructionCheckId());
		attemptHistory.setFollowupId((long) 33);
		attemptHistory.setRequestid(caseSpecificRecordDetail.getCaseSpecificDetailId());
		attemptHistory.setContactDate(new Date().toString());
		attemptHistory.setL3Response(l3Response);
		attemptHistory.setL3Status(l3Status);

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

	private void createCbvUtvPostVerificationEvent(String routerResult,
			CaseSpecificRecordDetail caseSpecificRecordDetail, CaseSpecificInfo caseSpecificInfo) {
		VerificationEventStatus verificationEventStatus = new VerificationEventStatus();
		if (StringUtils.equalsIgnoreCase(routerResult, CANNOT_VERIFY)) {
			verificationEventStatus.setStatus("CBV");
		} else if (StringUtils.equalsIgnoreCase(routerResult, UNABLE_VERIFY)) {
			verificationEventStatus.setStatus("UTV");
		} else {
			verificationEventStatus.setStatus("Day 0");
		}
		verificationEventStatus.setEvent(routerResult);
		verificationEventStatus.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		verificationEventStatus.setStage("cbvutv");
		verificationEventStatus.setEventType("auto");
		verificationEventStatus.setCaseNo(caseSpecificInfo.getCaseNumber());
		//verificationEventStatus.setUser("System");
		verificationEventStatus.setRequestId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		verificationEventStatusRepository.save(verificationEventStatus);
	}

	private void createCbvUtvPreVerificationEvent(CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo) {
		VerificationEventStatus verificationEventStatus = new VerificationEventStatus();

		verificationEventStatus.setEvent("Request Initiated");
		verificationEventStatus.setStatus("Day 0");

		verificationEventStatus.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		verificationEventStatus.setStage("cbvutv");
		verificationEventStatus.setEventType("auto");
		verificationEventStatus.setCaseNo(caseSpecificInfo.getCaseNumber());
		//verificationEventStatus.setUser("System");
		verificationEventStatus.setRequestId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		verificationEventStatusRepository.save(verificationEventStatus);
	}
}
