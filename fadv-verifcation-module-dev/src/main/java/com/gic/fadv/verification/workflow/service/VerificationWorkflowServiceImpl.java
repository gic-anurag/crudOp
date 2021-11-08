package com.gic.fadv.verification.workflow.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.verification.ae.model.RouterHistory;
import com.gic.fadv.verification.ae.repository.RouterHistoryRepository;
import com.gic.fadv.verification.attempts.model.CaseSpecificInfo;
import com.gic.fadv.verification.attempts.model.CaseSpecificRecordDetail;
import com.gic.fadv.verification.attempts.repository.CaseSpecificInfoRepository;
import com.gic.fadv.verification.attempts.repository.CaseSpecificRecordDetailRepository;
import com.gic.fadv.verification.online.service.OnlineApiService;
import com.gic.fadv.verification.workflow.model.BotRequestHistory;
import com.gic.fadv.verification.workflow.pojo.SpocFilterPOJO;
import com.gic.fadv.verification.workflow.repository.BotRequestHistoryRepository;

@Service
public class VerificationWorkflowServiceImpl implements VerificationWorkflowService {

	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;

	@Autowired
	private OnlineApiService onlineApiService;

	@Autowired
	private CaseSpecificInfoRepository caseSpecificInfoRepository;

	@Autowired
	private RouterHistoryRepository routerHistoryRepository;

	@Autowired
	private BotRequestHistoryRepository botRequestHistoryRepository;

	@Value("${bot.rerun.rest.url}")
	private String botRerunRestUrl;

	private static final Logger logger = LoggerFactory.getLogger(VerificationWorkflowServiceImpl.class);

	ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Override
	public List<RouterHistory> fetchFilteredRouters(SpocFilterPOJO spocFilterPOJO) {

		String fromDate = spocFilterPOJO.getFromDate() != null ? spocFilterPOJO.getFromDate() : "";
		String toDate = spocFilterPOJO.getToDate() != null ? spocFilterPOJO.getToDate() : "";
		String checkId = spocFilterPOJO.getCheckId() != null ? spocFilterPOJO.getCheckId() : "";
		String caseNumber = spocFilterPOJO.getCaseNumber() != null ? spocFilterPOJO.getCaseNumber() : "";
		String engineStatus = spocFilterPOJO.getEngineStatus() != null ? spocFilterPOJO.getEngineStatus() : "";
		String engineName = spocFilterPOJO.getEngineName() != null ? spocFilterPOJO.getEngineName() : "";

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		fromDate = StringUtils.isEmpty(fromDate) ? simpleDateFormat.format(new Date()) : fromDate;
		toDate = StringUtils.isEmpty(toDate) ? fromDate : toDate;

		if (StringUtils.isNotEmpty(fromDate) && StringUtils.isNotEmpty(toDate) && StringUtils.isNotEmpty(engineName)) {
			return routerHistoryRepository.getFilteredRouterHistory(engineName, fromDate, toDate, caseNumber, checkId,
					engineStatus);
		}
		return new ArrayList<>();
	}

	@Override
	public ResponseEntity<ObjectNode> rerunSpocRouterService(String checkId) {
		logger.info("Check Id received for rerun of SPOC router : {}", checkId);

		if (checkId != null && StringUtils.isNotEmpty(checkId)) {
			List<CaseSpecificRecordDetail> caseSpecificRecordDetails = caseSpecificRecordDetailRepository
					.findByInstructionCheckId(checkId);

			if (CollectionUtils.isNotEmpty(caseSpecificRecordDetails)) {
				CaseSpecificRecordDetail caseSpecificRecordDetail = caseSpecificRecordDetails.get(0);
				caseSpecificRecordDetail.setCbvUtvStatus("");
				caseSpecificRecordDetail.setSpocStatus("");
				caseSpecificRecordDetail.setUpdatedDate(new Date());

				List<CaseSpecificInfo> caseSpecificInfos = caseSpecificInfoRepository.findByCheckId(checkId);

				if (CollectionUtils.isNotEmpty(caseSpecificInfos)) {
					String caseNumber = caseSpecificInfos.get(0).getCaseNumber();
					caseSpecificRecordDetailRepository.save(caseSpecificRecordDetail);

					String response = onlineApiService.sendDataToGet(botRerunRestUrl + caseNumber);
					try {
						return new ResponseEntity<>(mapper.readValue(response, ObjectNode.class),
								HttpStatus.BAD_REQUEST);
					} catch (Exception e) {
						logger.error("Exception while mapping bot resposne body : {}", e.getMessage());
						return new ResponseEntity<>(createResponse("Exception while mapping bot request body", false),
								HttpStatus.EXPECTATION_FAILED);
					}
				}
				return new ResponseEntity<>(createResponse("Case details not found for given check id", false),
						HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<>(createResponse("Check details not found for given check id", false),
					HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(createResponse("Invalid Check Id", false), HttpStatus.BAD_REQUEST);
	}

	private ObjectNode createResponse(String message, boolean success) {
		ObjectNode responseNode = mapper.createObjectNode();

		responseNode.put("message", message);
		responseNode.put("success", success);

		return responseNode;
	}

	@Override
	public List<String> getRouterStatusListByEngineName(String engineName) {
		return routerHistoryRepository.getRouterStatusListByEngineName(engineName);
	}

	@Override
	public List<BotRequestHistory> fetchBotHistory(SpocFilterPOJO spocFilterPOJO) {
		String fromDate = spocFilterPOJO.getFromDate() != null ? spocFilterPOJO.getFromDate() : "";
		String toDate = spocFilterPOJO.getToDate() != null ? spocFilterPOJO.getToDate() : "";
		String caseNumber = spocFilterPOJO.getCaseNumber() != null ? spocFilterPOJO.getCaseNumber() : "";
		String requestStatus = spocFilterPOJO.getRequestStatus() != null ? spocFilterPOJO.getRequestStatus() : "";

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		fromDate = StringUtils.isEmpty(fromDate) ? simpleDateFormat.format(new Date()) : fromDate;
		toDate = StringUtils.isEmpty(toDate) ? fromDate : toDate;

		if (!StringUtils.isEmpty(fromDate) && !StringUtils.isEmpty(toDate)) {
			return botRequestHistoryRepository.getFilteredBotHistory(fromDate, toDate, caseNumber, requestStatus);
		}
		return new ArrayList<>();
	}

	@Override
	public List<String> getBotStatusList() {
		return botRequestHistoryRepository.getBotRequestStatusList();
	}

	@Override
	public List<String> getRouterEngineNameList() {
		return routerHistoryRepository.getRouterEngineNameList();
	}
}
