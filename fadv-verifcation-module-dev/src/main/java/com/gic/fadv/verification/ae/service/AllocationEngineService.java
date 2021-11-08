package com.gic.fadv.verification.ae.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.verification.ae.interfaces.CaseSpecificInterface;
import com.gic.fadv.verification.ae.model.CaseSpecificCheckAllocation;
import com.gic.fadv.verification.ae.model.CaseSpecificCheckPriority;
import com.gic.fadv.verification.attempts.model.CaseSpecificInfo;
import com.gic.fadv.verification.attempts.model.CaseSpecificRecordDetail;
import com.gic.fadv.verification.ae.model.RouterHistory;
import com.gic.fadv.verification.ae.pojo.AllocateCheckToUserPOJO;
import com.gic.fadv.verification.ae.pojo.NextCheckRequestPOJO;
import com.gic.fadv.verification.ae.repository.CaseSpecificCheckAllocationRepository;
import com.gic.fadv.verification.ae.repository.CaseSpecificCheckPriorityRepository;
import com.gic.fadv.verification.ae.repository.RouterHistoryRepository;
import com.gic.fadv.verification.attempts.repository.CaseSpecificInfoRepository;
import com.gic.fadv.verification.attempts.repository.CaseSpecificRecordDetailRepository;

@Service
public class AllocationEngineService {

	@Autowired
	Environment environment;

	@Autowired
	CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;

	@Autowired
	CaseSpecificCheckPriorityRepository caseSpecificCheckPriorityRepository;

	@Autowired
	CaseSpecificCheckAllocationRepository caseSpecificCheckAllocationRepository;

	@Autowired
	CaseSpecificInfoRepository caseSpecificInfoRepository;

	@Autowired
	RouterHistoryRepository routerHistoryRepository;

	private static final Logger logger = LoggerFactory.getLogger(AllocationEngineService.class);

	public List<CaseSpecificRecordDetail> getNewChecks() {
		String defaultStatus = environment.getProperty("check.allocation.default.status");
		String aeStatus = environment.getProperty("check.allocation.ae.status");

		List<CaseSpecificRecordDetail> retlist = caseSpecificRecordDetailRepository
				.findByCheckAllocationStatusAndIsCheckManual(defaultStatus, true);
		retlist.forEach(cd -> caseSpecificRecordDetailRepository
				.updateCheckAllocationStatus(cd.getCaseSpecificDetailId(), aeStatus));
		return retlist;
	}

	@Transactional
	public void savePriorityChecks(List<CaseSpecificCheckPriority> priorityChecks) {
		caseSpecificCheckPriorityRepository.saveAll(priorityChecks);
	}

	public CaseSpecificRecordDetail getNextCheck(NextCheckRequestPOJO nextCheckRequest) {
		Long userId = nextCheckRequest.getUserId();

		CaseSpecificCheckPriority pCheck = caseSpecificCheckPriorityRepository
				.findTopByIsAllocatedOrderByPriorityAscPriorityDateAsc(false);
		CaseSpecificRecordDetail checkDetail = null;
		if (pCheck != null) {
			String checkId = pCheck.getCheckId();
			caseSpecificCheckPriorityRepository.updateCheckPriorityAllocated(checkId);

			checkDetail = caseSpecificRecordDetailRepository.findTopByInstructionCheckId(checkId);

			CaseSpecificCheckAllocation aCheck = CaseSpecificCheckAllocation.builder().checkId(checkId).userId(userId)
					.build();
			caseSpecificCheckAllocationRepository.save(aCheck);
		}
		return checkDetail;
	}

	public ResponseEntity<String> allocateCheckToUser(AllocateCheckToUserPOJO allocateCheckToUserPOJO) {
		try {
			List<CaseSpecificRecordDetail> caseSpecificRecordDetailList = new ArrayList<>();
			List<RouterHistory> routerHistoryList = new ArrayList<>();
			for (Long id : allocateCheckToUserPOJO.getCaseSpecificRecordIds()) {
				Optional<CaseSpecificRecordDetail> caseSpecificRecordDetailOpt = caseSpecificRecordDetailRepository
						.findById(id);

				if (caseSpecificRecordDetailOpt.isPresent()) {
					CaseSpecificRecordDetail caseSpecificRecordDetail = caseSpecificRecordDetailOpt.get();
					Optional<CaseSpecificInfo> caseSpecificInfoOpt = caseSpecificInfoRepository
							.findById(caseSpecificRecordDetail.getCaseSpecificId());

					if (caseSpecificInfoOpt.isPresent()) {
						RouterHistory routerHistory = new RouterHistory();
						CaseSpecificInfo caseSpecificInfo = caseSpecificInfoOpt.get();
						caseSpecificRecordDetail.setUserId(allocateCheckToUserPOJO.getUserId());
						caseSpecificRecordDetail.setCheckAllocationStatus("AE");
						caseSpecificRecordDetailList.add(caseSpecificRecordDetail);

						routerHistory.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
						routerHistory.setEndTime(new Date());
						routerHistory.setStartTime(new Date());
						routerHistory.setEngineName("Manual");
						routerHistory.setCaseSpecificRecordDetailId(caseSpecificRecordDetail.getCaseSpecificDetailId());
						routerHistory.setCurrentEngineStatus("Allocated");
						routerHistory.setCaseNumber(caseSpecificInfo.getCaseNumber());

						routerHistoryList.add(routerHistory);
					}

				}
			}
			if (CollectionUtils.isNotEmpty(caseSpecificRecordDetailList)
					&& CollectionUtils.isNotEmpty(routerHistoryList)) {
				caseSpecificRecordDetailRepository.saveAll(caseSpecificRecordDetailList);
				routerHistoryRepository.saveAll(routerHistoryList);
			}
			return new ResponseEntity<>("Checks Allocated to User", HttpStatus.OK);
		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			return new ResponseEntity<>("Unable To Allocate Checks", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public CaseSpecificInterface getNextUserChecks(NextCheckRequestPOJO nextCheckRequest) {
		try {
			Long userId = nextCheckRequest.getUserId();
			if (userId != 0) {
				List<CaseSpecificInterface> caseSpecificInterfaces = caseSpecificRecordDetailRepository
						.getNextCheckDetails("AE", "Allocated", userId);

				if (CollectionUtils.isNotEmpty(caseSpecificInterfaces)) {
					return caseSpecificInterfaces.get(0);
				}
				String checkId = getNextPriorityCheck(userId);

				if (checkId != null) {
					List<CaseSpecificInterface> newCaseSpecificInterfaces = caseSpecificRecordDetailRepository
							.getNextCheckDetailsByCheckId(checkId);
					if (CollectionUtils.isNotEmpty(newCaseSpecificInterfaces)) {
						return newCaseSpecificInterfaces.get(0);
					}
				}
			}
		} catch (NumberFormatException e) {
			logger.debug("Exception while parsing user id : {}", e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private String getNextPriorityCheck(Long userId) {
		CaseSpecificCheckPriority caseSpecificCheckPriority = caseSpecificCheckPriorityRepository
				.findTopByIsAllocatedOrderByPriorityAscPriorityDateAsc(false);
		if (caseSpecificCheckPriority != null) {
			caseSpecificCheckPriority.setAllocated(true);
			caseSpecificCheckPriorityRepository.save(caseSpecificCheckPriority);
			CaseSpecificCheckAllocation caseSpecificCheckAllocation = new CaseSpecificCheckAllocation();
			caseSpecificCheckAllocation.setUserId(userId);
			caseSpecificCheckAllocation.setCheckId(caseSpecificCheckPriority.getCheckId());
			caseSpecificCheckAllocation.setCheckCreatedDate(new Date());
			caseSpecificCheckAllocationRepository.save(caseSpecificCheckAllocation);
			String checkId = caseSpecificCheckPriority.getCheckId();
			CaseSpecificRecordDetail caseSpecificRecordDetail = caseSpecificRecordDetailRepository
					.findTopByInstructionCheckId(checkId);
			if (caseSpecificRecordDetail != null) {
				caseSpecificRecordDetail.setUserId(userId);
				caseSpecificRecordDetail.setCheckAllocationStatus("AE");
				caseSpecificRecordDetailRepository.save(caseSpecificRecordDetail);

				Optional<CaseSpecificInfo> caseSpecificInfoOpt = caseSpecificInfoRepository
						.findById(caseSpecificRecordDetail.getCaseSpecificId());

				if (caseSpecificInfoOpt.isPresent()) {
					RouterHistory routerHistory = new RouterHistory();
					CaseSpecificInfo caseSpecificInfo = caseSpecificInfoOpt.get();

					routerHistory.setCheckId(caseSpecificRecordDetail.getInstructionCheckId());
					routerHistory.setEndTime(new Date());
					routerHistory.setStartTime(new Date());
					routerHistory.setEngineName("Manual");
					routerHistory.setCaseSpecificRecordDetailId(caseSpecificRecordDetail.getCaseSpecificDetailId());
					routerHistory.setCurrentEngineStatus("Allocated");
					routerHistory.setCaseNumber(caseSpecificInfo.getCaseNumber());
					routerHistoryRepository.save(routerHistory);
					return checkId;
				}
			}
		}
		return null;
	}
	
	public ObjectNode getAllcoatedChecksCount(Long userId) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		int noAllocatedChecks = routerHistoryRepository.getAllocatedTaskByUserId(userId);
		int noCompletedChecks = routerHistoryRepository.getCompletedTaskByUserId(userId);
		
		ObjectNode allocatedCases = mapper.createObjectNode();
		allocatedCases.put("allocatedTask", noAllocatedChecks);
		allocatedCases.put("completedTask", noCompletedChecks);
		
		return allocatedCases;
	}
}
