package com.gic.fadv.verification.controller.event;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.fadv.verification.event.model.VerifiedChecks;
import com.gic.fadv.verification.pojo.VerifiedChecksInterface;
import com.gic.fadv.verification.pojo.VerifiedChecksPOJO;
import com.gic.fadv.verification.repository.event.VerificationChecksRepository;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class VerificationChecksController {

	@Autowired
	private VerificationChecksRepository verificationChecksRepository;
	
	private static final Logger logger = LoggerFactory.getLogger(VerificationChecksController.class);

	
	@GetMapping("/verify-checks")
	public List<VerifiedChecks> getAllEvents() {
		return verificationChecksRepository.findAll();
	}
	
	@PostMapping("/verified-checks")
	public ResponseEntity<String> createVerifyChecks(@RequestBody JsonNode requestBody) {
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		String akaName = requestBody.has("akaName") ? requestBody.get("akaName").asText() : "";
		String companyName = requestBody.has("companyName") ? requestBody.get("companyName").asText() : "";
		String checkId = requestBody.has("checkId") ? requestBody.get("checkId").asText() : "";
		Long attemptId = requestBody.has("attemptId") ? requestBody.get("attemptId").asLong() : 0;
		Long caseSpecificId = requestBody.has("caseSpecificId") ? requestBody.get("caseSpecificId").asLong() : 0;
		
		if (StringUtils.isNotEmpty(checkId) && attemptId != 0 && caseSpecificId != 0) {
			VerifiedChecks verifyChecks = new VerifiedChecks();
			verifyChecks.setAkaName(akaName);
			verifyChecks.setCompanyName(companyName);
			verifyChecks.setCheckId(checkId);
			verifyChecks.setAttemptid(attemptId);
			verifyChecks.setCaseSpecificId(caseSpecificId);
			
			try {
				verificationChecksRepository.save(verifyChecks);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				return ResponseEntity.status(500).body("INTERNAL_SERVER_ERROR");		
			}
			return ResponseEntity.ok().body("Verification Check created");
		} else {
			return ResponseEntity.badRequest().body("Invalid check id or attempt id or case specific id");
		}
		
	}
	
	@PostMapping("/verified-checks-filter")
	public List<VerifiedChecksPOJO> getAllChecksByFilter(@RequestBody JsonNode requestBody) {

		String akaName = requestBody.has("akaName") ? requestBody.get("akaName").asText() : "";
		String companyName = requestBody.has("companyName") ? requestBody.get("companyName").asText() : "";
		
		List<VerifiedChecksInterface> verifiedChecksInterfaces = new ArrayList<>();
		
		if (StringUtils.isNotBlank(companyName)) {
			verifiedChecksInterfaces = verificationChecksRepository.filterByCompanyName(companyName);
		} else if (StringUtils.isNotBlank(akaName)) {
			verifiedChecksInterfaces = verificationChecksRepository.filterByAkaName(akaName);
		} else {
			return new ArrayList<>();
		}
		
		List<VerifiedChecksPOJO> verifiedChecksPOJOs = new ArrayList<>();
		
		for (int idx = 0; idx < verifiedChecksInterfaces.size(); idx ++) {
			VerifiedChecksPOJO verifiedChecksPOJO = new VerifiedChecksPOJO();
			
			String emailId = verifiedChecksInterfaces.get(idx).getEmailId() != null ? verifiedChecksInterfaces.get(idx).getEmailId() : ""; 
			if (emailId.contains(",")) {
				String[] emailList = emailId.split(",");
				emailId = String.join(", ", emailList);
			}
			verifiedChecksPOJO.setCandidateName(verifiedChecksInterfaces.get(idx).getCandidateName());
			verifiedChecksPOJO.setClientName(verifiedChecksInterfaces.get(idx).getClientName());
			verifiedChecksPOJO.setContactDate(verifiedChecksInterfaces.get(idx).getContactDate());
			verifiedChecksPOJO.setContactNo(verifiedChecksInterfaces.get(idx).getContactNo());
			verifiedChecksPOJO.setEmailId(emailId);
			verifiedChecksPOJO.setFaxNo(verifiedChecksInterfaces.get(idx).getFaxNo());
			verifiedChecksPOJO.setJobTitle(verifiedChecksInterfaces.get(idx).getJobTitle());
			verifiedChecksPOJO.setVerifyCheckId(verifiedChecksInterfaces.get(idx).getVerifyCheckId());
			verifiedChecksPOJOs.add(verifiedChecksPOJO);
		}
		return verifiedChecksPOJOs;
	}
	
}
