package fadv.verification.workflow.service;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fadv.verification.workflow.model.CaseSpecificInfo;
import fadv.verification.workflow.model.CaseSpecificRecordDetail;
import fadv.verification.workflow.model.RouterHistory;
import fadv.verification.workflow.model.VerificationEventStatus;
import fadv.verification.workflow.repository.CaseSpecificRecordDetailRepository;
import fadv.verification.workflow.repository.RouterHistoryRepository;
import fadv.verification.workflow.repository.VerificationEventStatusRepository;

@Service
public class SuspectRouterServiceImpl implements SuspectRouterService {
	
	@Autowired
	private VerificationEventStatusRepository verificationEventStatusRepository;
	
	@Autowired
	private ApiService apiService;

	@Autowired
	private RouterHistoryRepository routerHistoryRepository;
	
	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;
	
	@Value("${router.suspect.rest.url}")
	private String suspectRouterUrl;
	
	private static final String SUCCESS = "success";
	private static final String RESULT = "result";
	private static final String PROCESSED = "Processed";
	
	@Override
	public void processSuspectRouters(ObjectMapper mapper, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo, String requestStr, JsonNode requestNode) throws JsonProcessingException {

		RouterHistory routerHistory = new RouterHistory();
		routerHistory.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		routerHistory.setEngineName("Suspect");
		routerHistory.setStartTime(new Date());
		routerHistory.setEngineRequest(requestNode);
		routerHistory.setCaseNumber(caseSpecificInfo.getCaseNumber());
		routerHistory.setCurrentEngineStatus("Initiated");
		routerHistory.setCaseSpecificRecordDetailId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		routerHistory.setEngineResponse(null);
		routerHistory.setEndTime(null);
		RouterHistory newRouterHistory = routerHistoryRepository.save(routerHistory);
		createSuspectPreVerificationEvent(caseSpecificRecordDetail, caseSpecificInfo);
		String responsStr = apiService.sendDataToPost(suspectRouterUrl, requestStr);
		JsonNode responseNode = mapper.readValue(responsStr, JsonNode.class);

		boolean success = responseNode.has(SUCCESS) ? responseNode.get(SUCCESS).asBoolean() : Boolean.FALSE;
		String routerResult = responseNode.has(RESULT) ? responseNode.get(RESULT).asText() : "";
		if (Boolean.TRUE.equals(success) && StringUtils.isNotEmpty(routerResult)) {
			createSuspectPostVerificationEvent(routerResult, caseSpecificRecordDetail, caseSpecificInfo);
			if(routerResult.equalsIgnoreCase("Suspect records not found")) {
				//Updated is Manual Check true
				caseSpecificRecordDetail.setIsCheckManual(true);
			}
			caseSpecificRecordDetail.setSuspectStatus(PROCESSED);
			caseSpecificRecordDetail.setUpdatedDate(new Date());
			caseSpecificRecordDetailRepository.save(caseSpecificRecordDetail);
			newRouterHistory.setCurrentEngineStatus(PROCESSED);
			
		} else {
			createSuspectPostVerificationEvent("Failed", caseSpecificRecordDetail, caseSpecificInfo);
			newRouterHistory.setCurrentEngineStatus("Failed");
			//Updated is Manual Check true
			caseSpecificRecordDetail.setIsCheckManual(true);
			caseSpecificRecordDetailRepository.save(caseSpecificRecordDetail);
		}
		newRouterHistory.setEndTime(new Date());
		newRouterHistory.setEngineResponse(responseNode);
		routerHistoryRepository.save(newRouterHistory);
	}
	
	private void createSuspectPreVerificationEvent(CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo) {
		VerificationEventStatus verificationEventStatus = new VerificationEventStatus();

		verificationEventStatus.setEvent("Request Initiated");
		verificationEventStatus.setStatus("Day 0");

		verificationEventStatus.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		verificationEventStatus.setStage("Suspect");
		verificationEventStatus.setEventType("auto");
		verificationEventStatus.setCaseNo(caseSpecificInfo.getCaseNumber());
		//verificationEventStatus.setUser("System");
		verificationEventStatus.setRequestId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		verificationEventStatusRepository.save(verificationEventStatus);
	}

	private void createSuspectPostVerificationEvent(String routerResult, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo) {
		VerificationEventStatus verificationEventStatus = new VerificationEventStatus();
		if (StringUtils.equalsIgnoreCase(routerResult, "Suspect records found")) {
			verificationEventStatus.setStatus("Day 0");
		} else {
			verificationEventStatus.setStatus("Day 0");
		}
		verificationEventStatus.setEvent(routerResult);
		verificationEventStatus.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		verificationEventStatus.setStage("Suspect");
		verificationEventStatus.setEventType("auto");
		verificationEventStatus.setCaseNo(caseSpecificInfo.getCaseNumber());
		//verificationEventStatus.setUser("System");
		verificationEventStatus.setRequestId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		verificationEventStatusRepository.save(verificationEventStatus);
	}
}
