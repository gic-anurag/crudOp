package com.gic.fadv.verification.controller.event;

import java.util.List;

import javax.validation.Valid;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.gic.fadv.verification.attempts.repository.CaseSpecificRecordDetailRepository;
import com.gic.fadv.verification.event.model.VerificationAuditTrail;
import com.gic.fadv.verification.event.model.VerificationEventStatus;
import com.gic.fadv.verification.event.service.VerificationEventStatusService;
import com.gic.fadv.verification.exception.ResourceNotFoundException;
import com.gic.fadv.verification.pojo.VerificationEventStatusPOJO;
import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class VerificationEventStatusController {
	
	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;
	
	@Autowired
	private VerificationEventStatusService  verificationEventStatusService;
	
	private static final Logger logger = LoggerFactory.getLogger(VerificationEventStatusController.class);

	
	@GetMapping("/event")
	public List<VerificationEventStatus> getAllEvents() {
		return verificationEventStatusService.getAllEvents();
	}
	
	
	
	@PostMapping("/event")
	public VerificationEventStatus createVerificationEventStatus(@Valid @RequestBody VerificationEventStatus verificationEventStatus) {
		
		verificationEventStatus.setStatus("A");
		return verificationEventStatusService.save(verificationEventStatus);
	}
	
	@GetMapping("/event/{checkid}")
	public List<VerificationEventStatus> getEventByCheckId(
			@PathVariable(name = "checkid", required = false) String checkId) {
		
		return verificationEventStatusService.findByCheckId(checkId);
	}
	
	@ApiOperation(value = "This service is used to search deposition by filter on any field or multiple fields", response = List.class)
	@PostMapping("/event/search")
	public ResponseEntity<List<VerificationEventStatus>> getVerificationEventByFilter(
			@RequestBody VerificationEventStatusPOJO verificationEventStatusPOJO) throws ResourceNotFoundException {

		return ResponseEntity.ok().body(verificationEventStatusService.getVerificationEventByFilter(verificationEventStatusPOJO));
	}
	
	@PostMapping("/audit-trail")
	public List<VerificationAuditTrail> getAuditTrail(@RequestBody JsonNode reqString) {
		String checkId = reqString.has("checkId") ? reqString.get("checkId").asText() : "";
		Long requestNo = reqString.has("requestNo") ? reqString.get("requestNo").asLong() : 0;	
		String candidateName = reqString.has("candidateName") ? "%" + reqString.get("candidateName").asText() + "%" : "";
		String clientName = reqString.has("clientName") ? "%" + reqString.get("clientName").asText() + "%" : "";
		String packageName = reqString.has("packageName") ? "%" + reqString.get("packageName").asText() + "%" : "";
		
		logger.info("checkId: {}, requestNo: {}, candidateName: {}, clientName :{}, packageName: {}", 
				checkId, requestNo, candidateName, clientName, packageName);
		
		return caseSpecificRecordDetailRepository.getAuditTrail(checkId, requestNo, candidateName, clientName, packageName);
	}
}
