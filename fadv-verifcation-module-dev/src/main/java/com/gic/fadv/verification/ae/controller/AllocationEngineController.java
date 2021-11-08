package com.gic.fadv.verification.ae.controller;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gic.fadv.verification.attempts.model.CaseSpecificRecordDetail;
import com.gic.fadv.verification.attempts.repository.CaseSpecificRecordDetailRepository;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.verification.ae.interfaces.CaseSpecificInterface;
import com.gic.fadv.verification.ae.pojo.AllocateCheckToUserPOJO;
import com.gic.fadv.verification.ae.pojo.NextCheckRequestPOJO;
import com.gic.fadv.verification.ae.service.AllocationEngineService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class AllocationEngineController {
	private static final Logger logger = LoggerFactory.getLogger(AllocationEngineController.class);

	@Autowired
	AllocationEngineService allocationEngineService;

	@Autowired
	CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;

	@PostMapping(path = "/nextcheck", consumes = "application/json", produces = "application/json")
	public ResponseEntity<CaseSpecificRecordDetail> nextCheck(@RequestBody NextCheckRequestPOJO nextCheckRequest) {

		try {
			CaseSpecificRecordDetail nextCheck = allocationEngineService.getNextCheck(nextCheckRequest);
			return new ResponseEntity<>(nextCheck, HttpStatus.OK);
		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/get-allocate-cases")
	public List<CaseSpecificInterface> getAllocateCases() {
		try {
			return caseSpecificRecordDetailRepository.getCaseDetailsByManual(true);
		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	@PostMapping(path = "/allocate-check", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> allocateCheck(@Valid @RequestBody AllocateCheckToUserPOJO allocateCheckToUserPOJO) {
		return allocationEngineService.allocateCheckToUser(allocateCheckToUserPOJO);
	}

	@PostMapping(path = "/next-check", consumes = "application/json", produces = "application/json")
	public CaseSpecificInterface getNextCheckForUser(@RequestBody NextCheckRequestPOJO nextCheckRequest) {
		logger.info("Next check request : {}", nextCheckRequest);
		return allocationEngineService.getNextUserChecks(nextCheckRequest);
	}

	@GetMapping(path = "/followup-checks/{userId}")
	public List<CaseSpecificInterface> getFollowupChecks(@PathVariable(value = "userId") Long userId) {
		return caseSpecificRecordDetailRepository.getNextCheckDetails("AE", "In Progress", userId);

	}

	@GetMapping(path = "/allocated-checks-count/{userId}")
	public ObjectNode getAllcoatedChecksCount(@PathVariable(value = "userId") Long userId) {
		return allocationEngineService.getAllcoatedChecksCount(userId);
	}

}
