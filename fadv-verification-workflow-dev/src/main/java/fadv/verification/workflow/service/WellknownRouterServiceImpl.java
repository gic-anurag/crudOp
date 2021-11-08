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
public class WellknownRouterServiceImpl implements WellknownRouterService {
	
	@Autowired
	private VerificationEventStatusRepository verificationEventStatusRepository;
	
	@Autowired
	private ApiService apiService;

	@Autowired
	private RouterHistoryRepository routerHistoryRepository;
	
	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;
	
	@Value("${router.wellknown.rest.url}")
	private String wellknownRouterUrl;
	
	private static final String SUCCESS = "success";
	private static final String RESULT = "result";
	private static final String PROCESSED = "Processed";
	
	@Override
	public void processWellknownRouters(ObjectMapper mapper, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo, String requestStr, JsonNode requestNode) throws JsonProcessingException {

		RouterHistory routerHistory = new RouterHistory();
		routerHistory.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		routerHistory.setEngineName("Wellknown");
		routerHistory.setStartTime(new Date());
		routerHistory.setEngineRequest(requestNode);
		routerHistory.setCaseNumber(caseSpecificInfo.getCaseNumber());
		routerHistory.setCurrentEngineStatus("Initiated");
		routerHistory.setCaseSpecificRecordDetailId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		routerHistory.setEngineResponse(null);
		routerHistory.setEndTime(null);
		RouterHistory newRouterHistory = routerHistoryRepository.save(routerHistory);
		createWellKnownPreVerificationEvent(caseSpecificRecordDetail, caseSpecificInfo);
		String responsStr = apiService.sendDataToPost(wellknownRouterUrl, requestStr);
		JsonNode responseNode = mapper.readValue(responsStr, JsonNode.class);

		boolean success = responseNode.has(SUCCESS) ? responseNode.get(SUCCESS).asBoolean() : Boolean.FALSE;
		String routerResult = responseNode.has(RESULT) ? responseNode.get(RESULT).asText() : "";
		if (Boolean.TRUE.equals(success) && StringUtils.isNotEmpty(routerResult)) {
			if(routerResult.equalsIgnoreCase("Well known records not found")) {
				//Updated is Manual Check true
				caseSpecificRecordDetail.setIsCheckManual(true);
			}
			createWellKnownPostVerificationEvent(routerResult,caseSpecificRecordDetail, caseSpecificInfo);
			caseSpecificRecordDetail.setWellknownStatus(PROCESSED);
			caseSpecificRecordDetail.setUpdatedDate(new Date());
			caseSpecificRecordDetailRepository.save(caseSpecificRecordDetail);
			newRouterHistory.setCurrentEngineStatus(PROCESSED);
		} else {
			createWellKnownPostVerificationEvent("Failed",caseSpecificRecordDetail, caseSpecificInfo);
			newRouterHistory.setCurrentEngineStatus("Failed");
			//Updated is Manual Check true
			caseSpecificRecordDetail.setIsCheckManual(true);
			caseSpecificRecordDetailRepository.save(caseSpecificRecordDetail);
		}
		newRouterHistory.setEndTime(new Date());
		newRouterHistory.setEngineResponse(responseNode);
		routerHistoryRepository.save(newRouterHistory);
	}
	
	private void createWellKnownPreVerificationEvent(CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo) {
		VerificationEventStatus verificationEventStatus = new VerificationEventStatus();

		verificationEventStatus.setEvent("Request Initiated");
		verificationEventStatus.setStatus("Day 0");

		verificationEventStatus.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		verificationEventStatus.setStage("WellKnown");
		verificationEventStatus.setEventType("auto");
		verificationEventStatus.setCaseNo(caseSpecificInfo.getCaseNumber());
		//verificationEventStatus.setUser("System");
		verificationEventStatus.setRequestId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		verificationEventStatusRepository.save(verificationEventStatus);
	}

	private void createWellKnownPostVerificationEvent(String routerResult, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo) {
		VerificationEventStatus verificationEventStatus = new VerificationEventStatus();
		if (StringUtils.equalsIgnoreCase(routerResult, "WellKnown records found")) {
			verificationEventStatus.setStatus("Day 0");
		} else {
			verificationEventStatus.setStatus("Day 0");
		}
		verificationEventStatus.setEvent(routerResult);
		verificationEventStatus.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		verificationEventStatus.setStage("WellKnown");
		verificationEventStatus.setEventType("auto");
		verificationEventStatus.setCaseNo(caseSpecificInfo.getCaseNumber());
		//verificationEventStatus.setUser("System");
		verificationEventStatus.setRequestId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		verificationEventStatusRepository.save(verificationEventStatus);
	}
}
