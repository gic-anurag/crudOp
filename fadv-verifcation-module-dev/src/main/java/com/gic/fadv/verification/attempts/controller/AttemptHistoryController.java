package com.gic.fadv.verification.attempts.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.gic.fadv.verification.ae.model.RouterHistory;
import com.gic.fadv.verification.ae.repository.RouterHistoryRepository;
import com.gic.fadv.verification.attempts.model.AttemptHistory;
import com.gic.fadv.verification.attempts.model.AttemptStatusData;
import com.gic.fadv.verification.attempts.model.CaseSpecificRecordDetail;
import com.gic.fadv.verification.attempts.pojo.ContactAttemptsPOJO;
import com.gic.fadv.verification.attempts.pojo.MiRqDataPOJO;
import com.gic.fadv.verification.attempts.repository.AttemptMasterRepository;
import com.gic.fadv.verification.attempts.repository.AttemptStatusDataRepository;
import com.gic.fadv.verification.attempts.repository.CaseSpecificRecordDetailRepository;
import com.gic.fadv.verification.attempts.service.APIService;
import com.gic.fadv.verification.attempts.service.AttemptHistoryService;
import com.gic.fadv.verification.attempts.service.AttemptQuestionnaireService;
import com.gic.fadv.verification.online.model.OnlineVerificationChecks;
import com.gic.fadv.verification.online.pojo.OnlineAttemptHistoryPOJO;
import com.gic.fadv.verification.online.repository.OnlineVerificationChecksRepository;
import com.gic.fadv.verification.pojo.AttemptHistoryPOJO;

import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class AttemptHistoryController {

	@Autowired
	private APIService apiService;
	@Autowired
	private AttemptMasterRepository attemptMasterRepository;
	@Autowired
	private AttemptStatusDataRepository attemptStatusDataRepository;
	@Autowired
	private AttemptQuestionnaireService attemptQuestionnaireService;
	@Autowired
	private OnlineVerificationChecksRepository onlineVerificationChecksRepository;
	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;
	@Autowired
	private AttemptHistoryService attemptHistoryService;

	@Autowired
	private RouterHistoryRepository routerHistoryRepository;

	private static final Logger logger = LoggerFactory.getLogger(AttemptHistoryController.class);

	@GetMapping("/attempthistory/{checkId}")
	public List<AttemptHistoryPOJO> getAllAttempts(@PathVariable(value = "checkId") String checkId) {
		List<AttemptHistoryPOJO> attemptHistoryPOJOs = new ArrayList<>();
		List<AttemptHistory> attemptHistories = attemptMasterRepository.findByCheckid(checkId);

		for (AttemptHistory attemptHistory : attemptHistories) {
			AttemptHistoryPOJO attemptHistoryPOJO = new AttemptHistoryPOJO();
			attemptHistoryPOJO.setAttemptid(attemptHistory.getAttemptid());
			attemptHistoryPOJO.setName(attemptHistory.getName());
			attemptHistoryPOJO.setJobTitle(attemptHistory.getJobTitle());
			attemptHistoryPOJO.setSourcePhone(attemptHistory.getSourcePhone());
			attemptHistoryPOJO.setContactDate(attemptHistory.getContactDate());
			attemptHistoryPOJO.setFollowupDate(attemptHistory.getFollowupDate());
			attemptHistoryPOJO.setFollowupId(attemptHistory.getFollowupId());
			attemptHistoryPOJO.setNumberProvided(attemptHistory.getNumberProvided());
			attemptHistoryPOJO.setClosureExpectedDate(attemptHistory.getClosureExpectedDate());
			attemptHistoryPOJO.setAttemptDescription(attemptHistory.getAttemptDescription());
			attemptHistoryPOJO.setFaxNumber(attemptHistory.getFaxNumber());
			attemptHistoryPOJO.setEmailAddress(attemptHistory.getEmailAddress());
			if (attemptHistory.getAttemptStatus() != null) {
				attemptHistoryPOJO.setAttemptStatus(attemptHistory.getAttemptStatus().getAttemptStatus());
				attemptHistoryPOJO.setAttemptType(attemptHistory.getAttemptStatus().getAttemptType());
			}

			attemptHistoryPOJOs.add(attemptHistoryPOJO);
		}
		if (CollectionUtils.isNotEmpty(attemptHistoryPOJOs)) {
			attemptHistoryPOJOs.sort((o1, o2)
                    -> o1.getAttemptid().compareTo(
                            o2.getAttemptid()));
		}
		return attemptHistoryPOJOs;
	}

	@GetMapping("/attempt-history/{checkId}")
	public List<AttemptHistory> getAllAttemptsByCheckId(@PathVariable(value = "checkId") String checkId) {
		return attemptMasterRepository.findByCheckid(checkId);
	}

	@PostMapping("/attempt-history")
	public AttemptHistory createAttemptDeposition(@Valid @RequestBody AttemptHistory attemptMaster) {
		attemptMaster.setRequestid(attemptMaster.getRequestid());
		attemptMaster.setCheckid(attemptMaster.getCheckid());
		attemptMaster.setName(attemptMaster.getName());
		attemptMaster.setJobTitle(attemptMaster.getJobTitle());
		attemptMaster.setEmailAddress(attemptMaster.getEmailAddress());
		attemptMaster.setFaxNumber(attemptMaster.getFaxNumber());
		attemptMaster.setContactDate(attemptMaster.getContactDate());
		attemptMaster.setFollowupDate(attemptMaster.getFollowupDate());
		attemptMaster.setAttemptStatusid(attemptMaster.getAttemptStatusid());
		attemptMaster.setNumberProvided(attemptMaster.getNumberProvided());
		attemptMaster.setAttemptDescription(attemptMaster.getAttemptDescription());
		attemptMaster.setClosureExpectedDate(attemptMaster.getClosureExpectedDate());
		// Userid need to be set
		attemptMaster.setSourcePhone(attemptMaster.getSourcePhone());
		attemptMaster.setIsCurrent(0);
		attemptMaster.setContactCardName(attemptMaster.getContactCardName());
		attemptMaster.setFollowupId(attemptMaster.getFollowupId());
		attemptMaster.setRefAttemptId(attemptMaster.getRefAttemptId());
		Long attemptStatusId = attemptMaster.getAttemptStatusid() != null ? attemptMaster.getAttemptStatusid() : 0;

		if (attemptStatusId == 33) {
			attemptMaster.setAdditionalFieldsTag("Email");
		} else if (attemptStatusId == 34) {
			attemptMaster.setAdditionalFieldsTag("Cost");
		} else if (attemptStatusId == 35) {
			attemptMaster.setAdditionalFieldsTag("MI");
		}
		attemptMaster.setAdditionalFields(attemptMaster.getAdditionalFields());

		List<RouterHistory> routerHistories = routerHistoryRepository
				.findByCheckIdAndEngineName(attemptMaster.getCheckid(), "Manual");
		if (routerHistories != null && CollectionUtils.isNotEmpty(routerHistories)) {
			for (RouterHistory routerHistory : routerHistories) {
				if (attemptStatusId == 54) {
					routerHistory.setCurrentEngineStatus("Verified");
				} else {
					routerHistory.setCurrentEngineStatus("In Progress");
				}
				routerHistoryRepository.save(routerHistory);
			}
		}

		return attemptMasterRepository.save(attemptMaster);
	}

	@ApiOperation(value = "This service is used to search deposition by filter on any field or multiple fields", response = List.class)
	@PostMapping("/attempt-history/search")
	public ResponseEntity<List<AttemptHistory>> getAttemptHistoryByFilter(
			@RequestBody AttemptHistoryPOJO attemptHistoryPOJO) {
		return ResponseEntity.ok().body(apiService.getAttemptHistoryByFilter(attemptHistoryPOJO));
	}

	@GetMapping("/contact/{checkId}")
	public List<ContactAttemptsPOJO> getContactAttempts(@PathVariable(value = "checkId") String checkId) {
		return attemptMasterRepository.getContactAttempts(checkId);
	}

	@PostMapping("/online-attempt-history")
	public ResponseEntity<Object> createOnlineAttemptHistory(
			@Valid @RequestBody OnlineAttemptHistoryPOJO attemptHistoryPOJO) {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		List<OnlineVerificationChecks> onlineVerificationCheckList = onlineVerificationChecksRepository
				.getByProductListAndOnlineManualVerificationId(attemptHistoryPOJO.getOnlineManualVerifiationId(),
						attemptHistoryPOJO.getCheckIds());

		AttemptHistory attemptHistory = attemptHistoryService.getAttemptHistory(mapper, attemptHistoryPOJO);

		AttemptStatusData attemptStatusData = new AttemptStatusData();
		attemptStatusData.setDepositionId((long) 13);
		attemptStatusData.setEndstatusId((long) 7);
		attemptStatusData.setModeId((long) 6);

		for (OnlineVerificationChecks onlineVerificationChecks : onlineVerificationCheckList) {

			List<CaseSpecificRecordDetail> caseSpecificRecordDetailList = caseSpecificRecordDetailRepository
					.findByInstructionCheckId(onlineVerificationChecks.getCheckId());
			if (CollectionUtils.isNotEmpty(caseSpecificRecordDetailList)) {
				Long caseSpecificInfoId = caseSpecificRecordDetailList.get(0).getCaseSpecificDetailId();
				attemptHistory.setRequestid(caseSpecificInfoId);
				attemptHistory.setCheckid(onlineVerificationChecks.getCheckId());

				AttemptHistory newAttemptHistory = attemptMasterRepository.save(attemptHistory);

				attemptStatusData.setAttemptId(newAttemptHistory.getAttemptid());
				attemptStatusDataRepository.save(attemptStatusData);

				List<String> followUpStatus = new ArrayList<>();
				followUpStatus.add("MI-RQ");
				List<MiRqDataPOJO> miRqDataPOJOList = attemptMasterRepository.getAllDataForMIReq(followUpStatus,
						newAttemptHistory.getCheckid(), "success");

				for (MiRqDataPOJO miRqDataPOJO : miRqDataPOJOList) {
					try {
						attemptQuestionnaireService.sendDataToL3Mi(mapper, miRqDataPOJO);
					} catch (JsonProcessingException e) {
						logger.error("Exception while sending MI-RQ verify checks to L3 : {}", e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}

		return ResponseEntity.status(201).body("CREATED");
	}

	@GetMapping("/test-mi")
	public String testMi() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String additionalInfoStr = "[{\"document\": \"\", \"component\": \"\", \"missingInformation\": \"DOB and Father's name would be required\"}]";
		ArrayNode temp = (ArrayNode) mapper.readTree(additionalInfoStr);
		return attemptQuestionnaireService.getMIRQExecutiveComment(mapper, temp);
	}

}
