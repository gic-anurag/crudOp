package fadv.verification.workflow.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fadv.verification.workflow.model.CaseSpecificInfo;
import fadv.verification.workflow.model.CaseSpecificRecordDetail;
import fadv.verification.workflow.model.L3ApiRequestHistory;
import fadv.verification.workflow.model.RouterHistory;
import fadv.verification.workflow.model.VerificationEventStatus;
import fadv.verification.workflow.repository.CaseSpecificRecordDetailRepository;
import fadv.verification.workflow.repository.RouterHistoryRepository;
import fadv.verification.workflow.repository.VerificationEventStatusRepository;

@Service
public class PassportServiceImpl implements PassportService {
	private static final Logger logger = LoggerFactory.getLogger(PassportServiceImpl.class);
	
	@Autowired
	private VerificationEventStatusRepository verificationEventStatusRepository;
	
	@Autowired
	private ApiService apiService;

	@Autowired
	private RouterHistoryRepository routerHistoryRepository;
	
	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;
	
	@Value("${passport.verification.l3.rest.url}")
	private String passportVerificationl3RestUrl;
	
	private static final String SUCCESS = "success";
	private static final String PROCESSED = "Processed";
	
	@Override
	public void processPassport(ObjectMapper mapper, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo, String requestStr, JsonNode requestNode) throws JsonProcessingException {

		RouterHistory routerHistory = new RouterHistory();
		routerHistory.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		routerHistory.setEngineName("Passport verification");
		routerHistory.setStartTime(new Date());
		routerHistory.setEngineRequest(requestNode);
		routerHistory.setCaseNumber(caseSpecificInfo.getCaseNumber());
		routerHistory.setCurrentEngineStatus("Initiated");
		routerHistory.setCaseSpecificRecordDetailId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		routerHistory.setEngineResponse(null);
		routerHistory.setEndTime(null);
		RouterHistory newRouterHistory = routerHistoryRepository.save(routerHistory);
		createPassportPreVerificationEvent(caseSpecificRecordDetail, caseSpecificInfo);
		String responseStr = apiService.sendDataToL3Post(passportVerificationl3RestUrl, requestStr,null);
		logger.info("Passport Verification API Response:{}",responseStr);
		JsonNode responseNode = mapper.createObjectNode();
		try {
			responseNode = mapper.readValue(responseStr, JsonNode.class);
		} catch (JsonProcessingException e) {
			logger.info("Exception while mapping passport router response : {}", e.getMessage());
			responseNode = mapper.createObjectNode();
		}
		//Save Data in DB. Since we are hitting to L3
		saveL3RequestResponse(mapper, caseSpecificRecordDetail, caseSpecificInfo, requestStr, responseNode);
		boolean success = responseNode.has(SUCCESS) ? responseNode.get(SUCCESS).asBoolean() : Boolean.FALSE;
		if (Boolean.TRUE.equals(success)) {
			createPassportPostVerificationEvent("Success",caseSpecificRecordDetail, caseSpecificInfo);
			caseSpecificRecordDetail.setPassportStatus(PROCESSED);
			caseSpecificRecordDetail.setUpdatedDate(new Date());
			caseSpecificRecordDetailRepository.save(caseSpecificRecordDetail);
			//newRouterHistory.setCurrentEngineStatus(PROCESSED);
		} else {
			createPassportPostVerificationEvent("Failed",caseSpecificRecordDetail, caseSpecificInfo);
			newRouterHistory.setCurrentEngineStatus("Failed");
		}
		newRouterHistory.setEndTime(new Date());
		newRouterHistory.setEngineResponse(responseNode);
		routerHistoryRepository.save(newRouterHistory);
	}

	private void saveL3RequestResponse(ObjectMapper mapper, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo, String requestStr, JsonNode responseNode)
			throws JsonProcessingException, JsonMappingException {
		L3ApiRequestHistory l3ApiRequestHistory = new L3ApiRequestHistory();
		l3ApiRequestHistory.setCaseNumber(caseSpecificInfo.getCaseNumber());
		l3ApiRequestHistory.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		l3ApiRequestHistory.setCreatedDate(new Date());
		l3ApiRequestHistory.setUpdatedDate(new Date());
		l3ApiRequestHistory.setRequestUrl(passportVerificationl3RestUrl);
		l3ApiRequestHistory.setL3Rquest(mapper.readTree(requestStr));
		l3ApiRequestHistory.setL3Response(responseNode);
		l3ApiRequestHistory.setRequestType("Passport");
	}
	
	private void createPassportPreVerificationEvent(CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo) {
		VerificationEventStatus verificationEventStatus = new VerificationEventStatus();

		verificationEventStatus.setEvent("Request Initiated");
		verificationEventStatus.setStatus("Day 0");

		verificationEventStatus.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		verificationEventStatus.setStage("Passport");
		verificationEventStatus.setEventType("auto");
		verificationEventStatus.setCaseNo(caseSpecificInfo.getCaseNumber());
		//verificationEventStatus.setUser("System");
		verificationEventStatus.setRequestId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		verificationEventStatusRepository.save(verificationEventStatus);
	}

	private void createPassportPostVerificationEvent(String routerResult, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo) {
		VerificationEventStatus verificationEventStatus = new VerificationEventStatus();
		verificationEventStatus.setStatus("Day 0");
		verificationEventStatus.setEvent(routerResult);
		verificationEventStatus.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		verificationEventStatus.setStage("Passport");
		verificationEventStatus.setEventType("auto");
		verificationEventStatus.setCaseNo(caseSpecificInfo.getCaseNumber());
		//verificationEventStatus.setUser("System");
		verificationEventStatus.setRequestId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		verificationEventStatusRepository.save(verificationEventStatus);
	}
}
