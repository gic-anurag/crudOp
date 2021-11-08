package com.gic.fadv.verification.online.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.gic.fadv.verification.online.model.OnlineVerificationChecks;
import com.gic.fadv.verification.pojo.OnlineVerificationChecksPOJO;

@Service
public interface OnlineVerificationChecksService {

	ArrayNode processManuptraOutput(ObjectMapper mapper, List<OnlineVerificationChecks> onlineVerificationCheckList)
			throws JsonProcessingException;

	ArrayNode processOutputFile(ObjectMapper mapper, String outputFile) throws JsonProcessingException;

	void processL3VerifyCheckIds(ObjectMapper mapper, List<String> checkIdList);

	Map<String, String> createOnlineVerificationChecks(ObjectMapper mapper,
			List<OnlineVerificationChecksPOJO> onlineVerificationCheckList);

	String sendDataToL3ClearOutcomeAttempt(ObjectMapper mapper, String checkId) throws JsonProcessingException;

	Map<String, String> rerunOnlineRequests(ObjectMapper mapper, JsonNode requestBody);

	void createDiscrepantProcessAttempt(OnlineVerificationChecks onlineVerificationChecks, String l3Status,
			String l3Response);

	String sendAllClearDataToL3(ObjectMapper mapper, List<String> checkIdList) throws JsonProcessingException;

	String sendDataToL3DiscrepantProcessAttempt(ObjectMapper mapper, String checkId) throws JsonProcessingException;

}
