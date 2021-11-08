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
public class VendorInputRouterServiceImpl implements VendorInputRouterService {

	@Autowired
	private ApiService apiService;

	@Autowired
	private RouterHistoryRepository routerHistoryRepository;

	@Autowired
	private VerificationEventStatusRepository verificationEventStatusRepository;

	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;

	@Value("${router.vendor.input.rest.url}")
	private String vendorInputRouterUrl;
	@Value("${verification.status.url.l3}")
	private String verificationStatusL3Url;

	private static final String SUCCESS = "success";
	private static final String PROCESSED = "Processed";

	@Override
	public void processVendorInputRouters(ObjectMapper mapper, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo, String requestStr, JsonNode requestNode) throws JsonProcessingException {

		RouterHistory routerHistory = new RouterHistory();
		routerHistory.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		routerHistory.setEngineName("VendorInput");
		routerHistory.setStartTime(new Date());
		routerHistory.setEngineRequest(requestNode);
		routerHistory.setCaseNumber(caseSpecificInfo.getCaseNumber());
		routerHistory.setCurrentEngineStatus("Initiated");
		routerHistory.setCaseSpecificRecordDetailId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		routerHistory.setEngineResponse(null);
		routerHistory.setEndTime(null);
		RouterHistory newRouterHistory = routerHistoryRepository.save(routerHistory);
		createVendorInputPreVerificationEvent(caseSpecificRecordDetail, caseSpecificInfo);
		String responsStr = apiService.sendDataToPost(vendorInputRouterUrl, requestStr);
		JsonNode responseNode = mapper.readValue(responsStr, JsonNode.class);

		boolean success = responseNode.has(SUCCESS) ? responseNode.get(SUCCESS).asBoolean() : Boolean.FALSE;

		if (Boolean.FALSE.equals(success)) {
			createVendorInputPostVerificationEvent("Manual", caseSpecificRecordDetail, caseSpecificInfo);
			// Updated is Manual Check true
			caseSpecificRecordDetail.setIsCheckManual(true);
			newRouterHistory.setCurrentEngineStatus(PROCESSED);
			newRouterHistory.setEngineResponse(responseNode);
			routerHistoryRepository.save(newRouterHistory);
		}
		caseSpecificRecordDetail.setVendorStatus(PROCESSED);
		caseSpecificRecordDetail.setUpdatedDate(new Date());
		caseSpecificRecordDetailRepository.save(caseSpecificRecordDetail);
	}

	private void createVendorInputPostVerificationEvent(String routerResult,
			CaseSpecificRecordDetail caseSpecificRecordDetail, CaseSpecificInfo caseSpecificInfo) {
		VerificationEventStatus verificationEventStatus = new VerificationEventStatus();
		if (StringUtils.equalsIgnoreCase(routerResult, "File Created")) {
			verificationEventStatus.setStatus("F2");
		} else {
			verificationEventStatus.setStatus("Day 0");
		}

		verificationEventStatus.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		verificationEventStatus.setStage("Vendor-Input");
		verificationEventStatus.setEventType("auto");
		verificationEventStatus.setCaseNo(caseSpecificInfo.getCaseNumber());
		verificationEventStatus.setRequestId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		verificationEventStatusRepository.save(verificationEventStatus);
	}

	private void createVendorInputPreVerificationEvent(CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo) {
		VerificationEventStatus verificationEventStatus = new VerificationEventStatus();
		verificationEventStatus.setEvent("Request Initiated");
		verificationEventStatus.setStatus("Day 0");
		verificationEventStatus.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
		verificationEventStatus.setStage("Vendor-Input");
		verificationEventStatus.setEventType("auto");
		verificationEventStatus.setCaseNo(caseSpecificInfo.getCaseNumber());
		verificationEventStatus.setRequestId(caseSpecificRecordDetail.getCaseSpecificDetailId());
		verificationEventStatusRepository.save(verificationEventStatus);
	}
}
