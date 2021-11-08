package com.gic.fadv.verification.attempts.controller;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.fadv.verification.attempts.model.AttemptStatus;
import com.gic.fadv.verification.attempts.model.AttemptStatusData;
import com.gic.fadv.verification.attempts.repository.AttemptStatusDataRepository;
import com.gic.fadv.verification.attempts.repository.AttemptStatusRepository;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class AttemptStatusController {
	
//	@Autowired
//	private APIService apiService;
	
	@Autowired
	private AttemptStatusRepository attemptStatusRepository;
	@Autowired
	private AttemptStatusDataRepository attemptStatusDataRepository;

//	private static final Logger logger = LoggerFactory.getLogger(AttemptStatusController.class);

	
	@GetMapping("/attempt-status")
	public List<AttemptStatus> getAllStellars() {
		return attemptStatusRepository.findAll();
	}

	@PostMapping("/attempt-status-data")
	public AttemptStatusData createAttemptStatusData(@Valid @RequestBody AttemptStatusData attemptStatusData) {
		return attemptStatusDataRepository.save(attemptStatusData);
	}
	
	@GetMapping(path = "/attempt-status-data/{statusId}", produces = "application/json")
	public String getAllByStatus(@PathVariable(value = "statusId") Long statusId) {
		List<AttemptStatusData> attemptStatusDataList = attemptStatusDataRepository.queryByStatusId(statusId);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JsonNode attemptStatusDataJsonNode = mapper.valueToTree(attemptStatusDataList);
		String attemptStatusDataStr = null;
		try {
			attemptStatusDataStr = mapper.writeValueAsString(attemptStatusDataJsonNode);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return attemptStatusDataStr;
	}
	
	@GetMapping(path = {"/attempt-history/{attemptType}", "/attempt-history"}, produces = "application/json")
	public String getAllByAttemptType(@PathVariable(name = "attemptType", required = false) String attemptType) {

		List<AttemptStatusData> attemptStatusDataList;
		
		if (StringUtils.isEmpty(attemptType)) {
			attemptStatusDataList = attemptStatusDataRepository.queryAll();
		} else {
			attemptStatusDataList = attemptStatusDataRepository.queryByAttemptType(attemptType);
		}
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JsonNode attemptStatusDataJsonNode = mapper.valueToTree(attemptStatusDataList);
		String attemptStatusDataStr = null;
		try {
			attemptStatusDataStr = mapper.writeValueAsString(attemptStatusDataJsonNode);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}
		
		return attemptStatusDataStr;
	}
}
