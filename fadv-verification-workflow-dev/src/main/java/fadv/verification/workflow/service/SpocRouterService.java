package fadv.verification.workflow.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fadv.verification.workflow.model.CaseSpecificInfo;
import fadv.verification.workflow.model.CaseSpecificRecordDetail;

@Service
public interface SpocRouterService {

	void processSpocRouters(ObjectMapper mapper, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo, String requestStr, JsonNode requestNode) throws JsonProcessingException;

	void processRouters(ObjectMapper mapper, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo, String requestStr, JsonNode requestNode) throws JsonProcessingException;

}