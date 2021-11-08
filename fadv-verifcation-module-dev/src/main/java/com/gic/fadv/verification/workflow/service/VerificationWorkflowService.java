package com.gic.fadv.verification.workflow.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.verification.ae.model.RouterHistory;
import com.gic.fadv.verification.workflow.model.BotRequestHistory;
import com.gic.fadv.verification.workflow.pojo.SpocFilterPOJO;

@Service
public interface VerificationWorkflowService {

	ResponseEntity<ObjectNode> rerunSpocRouterService(String checkId);

	List<String> getRouterStatusListByEngineName(String engineName);
	
	List<String> getRouterEngineNameList();
	List<RouterHistory> fetchFilteredRouters(SpocFilterPOJO spocFilterPOJO);

	List<BotRequestHistory> fetchBotHistory(SpocFilterPOJO spocFilterPOJO);

	List<String> getBotStatusList();

}
