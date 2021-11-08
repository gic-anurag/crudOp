package com.gic.fadv.verification.workflow.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.verification.ae.model.RouterHistory;
import com.gic.fadv.verification.workflow.model.BotRequestHistory;
import com.gic.fadv.verification.workflow.pojo.SpocFilterPOJO;
import com.gic.fadv.verification.workflow.service.VerificationWorkflowService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class VerificationWorkflowController {

	@Autowired
	private VerificationWorkflowService verificationWorkflowService;

	@GetMapping(path = "/rerun-spoc-router/{checkId}", produces = "application/json")
	public ResponseEntity<ObjectNode> rerunSpocRouterService(@PathVariable(value = "checkId") String checkId) {
		return verificationWorkflowService.rerunSpocRouterService(checkId);
	}

	@GetMapping(path = "/router-status-list/{engineName}", produces = "application/json")
	public List<String> getRouterStatusListByEngineName(@PathVariable(value = "engineName") String engineName) {
		return verificationWorkflowService.getRouterStatusListByEngineName(engineName);
	}

	@PostMapping(path = "/get-spoc-failed-list", produces = "application/json", consumes = "application/json")
	public List<RouterHistory> fetchAllFailedSpocRouters(@Valid @RequestBody SpocFilterPOJO spocFilterPOJO) {
		return verificationWorkflowService.fetchFilteredRouters(spocFilterPOJO);
	}

	@PostMapping(path = "/get-bot-history", produces = "application/json", consumes = "application/json")
	public List<BotRequestHistory> fetchAllBotHistory(@Valid @RequestBody SpocFilterPOJO spocFilterPOJO) {
		return verificationWorkflowService.fetchBotHistory(spocFilterPOJO);
	}

	@GetMapping(path = "/bot-status-list", produces = "application/json")
	public List<String> getBotStatusList() {
		return verificationWorkflowService.getBotStatusList();
	}

	@GetMapping(path = "/router-name-list", produces = "application/json")
	public List<String> getRouterNameList() {
		return verificationWorkflowService.getRouterEngineNameList();
	}

}
