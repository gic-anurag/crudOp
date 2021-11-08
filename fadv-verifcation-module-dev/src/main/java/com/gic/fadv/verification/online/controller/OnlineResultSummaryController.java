package com.gic.fadv.verification.online.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.gic.fadv.verification.online.model.OnlineResultSummary;
import com.gic.fadv.verification.online.model.OnlineVerificationChecks;
import com.gic.fadv.verification.online.pojo.OnlineResultSummaryPOJO;
import com.gic.fadv.verification.online.repository.OnlineResultSummaryRepository;
import com.gic.fadv.verification.online.repository.OnlineVerificationChecksRepository;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class OnlineResultSummaryController {

	@Autowired
	private OnlineResultSummaryRepository onlineResultSummaryRepository;

	@Autowired
	private OnlineVerificationChecksRepository onlineVerificationChecksRepository;

	@GetMapping("/result-summary-title/{checkId}")
	public List<String> getResultSummaryTitle(@PathVariable(value = "checkId") String checkId) {
		List<OnlineVerificationChecks> onlineVerificationChecks = onlineVerificationChecksRepository
				.findByCheckIdAndApiNameOrderByOnlineVerificationCheckIdDesc(checkId, "Manupatra");
		List<String> titleList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(onlineVerificationChecks)) {
			OnlineVerificationChecks onlineVerificationCheck = onlineVerificationChecks.get(0);
			ArrayNode outputResult = onlineVerificationCheck.getOutputResult();

			if (!outputResult.isEmpty()) {
				for (JsonNode output : outputResult) {
					String title = output.has("title") ? output.get("title").asText() : "";
					if (StringUtils.isNotEmpty(title)) {
						titleList.add(title);
					}
				}
			}
		}
		return titleList;
	}
	
	@GetMapping("/result-summary-list/{checkId}")
	public List<OnlineResultSummary> getResultSummaryList(@PathVariable(value = "checkId") String checkId) {
		return onlineResultSummaryRepository.findByCheckId(checkId);
	}

	@PostMapping("/result-summary")
	public ResponseEntity<Object> saveResultSummary(@RequestBody List<OnlineResultSummaryPOJO> onlineResultSummaryPOJOs) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		List<OnlineResultSummary> onlineResultSummaries = new ArrayList<>();

		for (OnlineResultSummaryPOJO onlineResultSummaryPOJO : onlineResultSummaryPOJOs) {
			OnlineResultSummary onlineResultSummary = mapper.convertValue(onlineResultSummaryPOJO,
					OnlineResultSummary.class);
			onlineResultSummary.setCreatedDate(new Date());
			onlineResultSummary.setUpdatedDate(new Date());
			onlineResultSummaries.add(onlineResultSummary);
		}
		if (CollectionUtils.isNotEmpty(onlineResultSummaries)) {
			onlineResultSummaryRepository.saveAll(onlineResultSummaries);
			
			return ResponseEntity.status(201).body(HttpStatus.SC_CREATED);
		}
		return ResponseEntity.ok().body(HttpStatus.SC_OK);
	}

}
