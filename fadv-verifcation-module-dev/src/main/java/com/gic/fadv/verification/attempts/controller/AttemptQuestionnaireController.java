package com.gic.fadv.verification.attempts.controller;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.gic.fadv.verification.attempts.model.AttemptQuestionnaire;
import com.gic.fadv.verification.attempts.repository.AttemptQuestionnaireRepository;
import com.gic.fadv.verification.attempts.service.AttemptQuestionnaireService;
import com.gic.fadv.verification.attempts.service.L3APIService;
import com.gic.fadv.verification.pojo.AttemptQuestionnairePOJO;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class AttemptQuestionnaireController {

	@Autowired
	private AttemptQuestionnaireRepository attemptQuestionnaireRepository;
	@Autowired
	private AttemptQuestionnaireService attemptQuestionnaireService;
	@Autowired
	private L3APIService l3APIService;

	@Value("${verification.url.l3}")
	private String verificationL3Url;
	@Value("${verification.status.url.l3}")
	private String verificationStatusL3Url;

	private static final Logger logger = LoggerFactory.getLogger(AttemptQuestionnaireController.class);

	@GetMapping(path = { "/attempt-questionnaire/{checkId}", "/attempt-questionnaire" })
	public List<AttemptQuestionnaire> getAllStellars(@PathVariable(name = "checkId", required = false) String checkId) {
		checkId = checkId != null ? checkId : "";
		if (StringUtils.isNotEmpty(checkId)) {
			return attemptQuestionnaireRepository.findByCheckId(checkId);
		} else {
			return attemptQuestionnaireRepository.findAll();
		}

	}

	@GetMapping("/all-questionnaires/{checkId}")
	public List<AttemptQuestionnairePOJO> getAllQuestionnaire(
			@PathVariable(name = "checkId", required = true) String checkId) {
		return attemptQuestionnaireRepository.findAllByJoin(checkId);
	}

	@PostMapping("/attempt-questionnaire-form")
	public AttemptQuestionnaire createAttemptDeposition(@Valid @RequestBody AttemptQuestionnaire attemptQuestionnaire) {

		attemptQuestionnaire.setQuestionName(attemptQuestionnaire.getQuestionName());
		attemptQuestionnaire.setGlobalQuestionId(attemptQuestionnaire.getGlobalQuestionId());
		attemptQuestionnaire.setApplicationData(attemptQuestionnaire.getApplicationData());
		attemptQuestionnaire.setStatus(attemptQuestionnaire.getStatus());
		attemptQuestionnaire.setAdjudication(attemptQuestionnaire.getAdjudication());
		attemptQuestionnaire.setVerifiedData(attemptQuestionnaire.getVerifiedData());
		attemptQuestionnaire.setReportComments(attemptQuestionnaire.getReportComments());
		// attemptDeposition.setStatus("A"); return
		return attemptQuestionnaireRepository.save(attemptQuestionnaire);
	}

	@PostMapping("/attempt-questionnaire1/{checkId}")
	public String createAttemptQuestionnaire1(@PathVariable(value = "checkId") String checkId,
			@RequestBody String attemptQuestionnaire) {
		// Creating the ObjectMapper object
		logger.info("Called Attempt Questionnaire : {}", attemptQuestionnaire);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JsonNode attemptQuestionnaireNode = null;
		try {
			attemptQuestionnaireNode = mapper.readTree(attemptQuestionnaire);
		} catch (JsonProcessingException e) {
			logger.error("Error : {}", e.getMessage());
			e.printStackTrace();
		}
		ArrayNode attemptQuestionnaireArrNode = mapper.createArrayNode();
		JsonNode followupNode = null;
		if (attemptQuestionnaireNode != null) {
			attemptQuestionnaireArrNode = (ArrayNode) attemptQuestionnaireNode.get("questionaires");
			followupNode = attemptQuestionnaireNode.get("followupid");
			List<AttemptQuestionnaire> attemptQuestionnaireList = new ArrayList<>();
			try {
				attemptQuestionnaireList = mapper.readValue(attemptQuestionnaireArrNode.toString(),
						new TypeReference<List<AttemptQuestionnaire>>() {
						});
			} catch (JsonProcessingException e) {
				logger.error("Error : {}", e.getMessage());
				e.printStackTrace();
			}
			attemptQuestionnaireRepository.saveAll(attemptQuestionnaireList);
		}
		logger.info("Value of AttemptQuestion : {}", attemptQuestionnaireArrNode);
		logger.info("Value of followupNode : {}", followupNode);
		/*
		 * Logic for Sending Verification Data to L3
		 */
		String getVerificationStatusforL3 = null;
		logger.info("url: {} {}", verificationL3Url, checkId);
		try {
			getVerificationStatusforL3 = l3APIService.sendDataTogetVerification(verificationL3Url, checkId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Find and Replace null to ""
		getVerificationStatusforL3 = StringUtils.replace(getVerificationStatusforL3, "null", "\"\"");

		JsonNode getVerificationStatusforL3Node = null;
		try {
			getVerificationStatusforL3Node = mapper.readTree(getVerificationStatusforL3);
		} catch (JsonProcessingException e) { 
			logger.error("Error : {}", e.getMessage());
			e.printStackTrace();
		}
		logger.info("getVerificationStatusforL3Node : {}", getVerificationStatusforL3Node);

		String sendInfol3VeriStatus = null;
		try {
			sendInfol3VeriStatus = l3APIService.sendDataToRest(verificationStatusL3Url, getVerificationStatusforL3,
					null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.info("Value of Save : {}", sendInfol3VeriStatus);
		return followupNode.toString();

	}

	@PostMapping("/attempt-questionnaire/{checkId}")
	public String createAttemptQuestionnaire(@PathVariable(value = "checkId") String checkId,
			@RequestBody JsonNode attemptQuestionnaire) throws JsonProcessingException {
		logger.info("Called Attempt Questionnaire : {}", attemptQuestionnaire);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		ArrayNode attemptQuestionnaireArrNode = attemptQuestionnaire.has("questionaires")
				? (ArrayNode) attemptQuestionnaire.get("questionaires")
				: mapper.createArrayNode();
		Long followupId = attemptQuestionnaire.has("followupid") ? attemptQuestionnaire.get("followupid").asLong()
				: 0;
		String followupStatus = attemptQuestionnaire.has("followupstatus") ? attemptQuestionnaire.get("followupstatus").asText()
				: "";
		String followupDescription = attemptQuestionnaire.has("followupdescription") ? attemptQuestionnaire.get("followupdescription").asText()
				: "";

		logger.info("Value of followupId : {}", followupId);
		logger.info("Value of followupStatus : {}", followupStatus);
		logger.info("Value of followupDescription : {}", followupDescription);
		
		if (followupId != 0) {
			attemptQuestionnaireService.updateAttemptFollowUp(followupId, checkId,followupStatus);
		}
		
		return attemptQuestionnaireService.processAttempQuestionnaire(checkId, mapper, attemptQuestionnaireArrNode);
	}
}
