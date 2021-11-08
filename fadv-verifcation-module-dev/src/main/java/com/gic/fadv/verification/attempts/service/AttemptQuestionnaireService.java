package com.gic.fadv.verification.attempts.service;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.gic.fadv.verification.attempts.pojo.MiRqDataPOJO;

@Service
public interface AttemptQuestionnaireService {

	String processAttempQuestionnaire(String checkId, ObjectMapper mapper, ArrayNode attemptQuestionnaireArrNode)
			throws JsonProcessingException, JsonMappingException;

	void updateAttemptFollowUp(Long followUpId, String checkId, String followUpStatus);

	String sendDataToL3Mi(ObjectMapper mapper, MiRqDataPOJO miRqDataPOJO) throws JsonProcessingException;

	String getMIRQExecutiveComment(ObjectMapper mapper, ArrayNode additionalInfoArr) throws JsonProcessingException;

}
