package com.gic.fadv.verification.attempts.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.gic.fadv.verification.attempts.model.AttemptHistory;
import com.gic.fadv.verification.online.pojo.OnlineAttemptHistoryPOJO;

@Service
public class AttemptHistoryServiceImpl implements AttemptHistoryService {
	
	@Override
	public AttemptHistory getAttemptHistory(ObjectMapper mapper, OnlineAttemptHistoryPOJO attemptHistoryPOJO) {
		AttemptHistory attemptHistory = new AttemptHistory();
		attemptHistory.setName(attemptHistoryPOJO.getName());
		attemptHistory.setJobTitle(attemptHistoryPOJO.getJobTitle());
		attemptHistory.setEmailAddress(attemptHistoryPOJO.getEmailAddress());
		attemptHistory.setFaxNumber(attemptHistoryPOJO.getFaxNumber());
		attemptHistory.setContactDate(attemptHistoryPOJO.getContactDate());
		attemptHistory.setFollowupDate(attemptHistoryPOJO.getFollowupDate());
		attemptHistory.setAttemptStatusid(attemptHistoryPOJO.getAttemptStatusid());
		attemptHistory.setNumberProvided(attemptHistoryPOJO.getNumberProvided());
		attemptHistory.setAttemptDescription(attemptHistoryPOJO.getAttemptDescription());
		attemptHistory.setClosureExpectedDate(attemptHistoryPOJO.getClosureExpectedDate());
		attemptHistory.setSourcePhone(attemptHistoryPOJO.getSourcePhone());
		attemptHistory.setIsCurrent(0);
		attemptHistory.setContactCardName(attemptHistoryPOJO.getContactCardName());
		attemptHistory.setFollowupId(attemptHistoryPOJO.getFollowupId());
		attemptHistory.setRefAttemptId(attemptHistoryPOJO.getRefAttemptId());
		Long attemptStatusId = attemptHistoryPOJO.getAttemptStatusid() != null ? attemptHistoryPOJO.getAttemptStatusid()
				: 0;
		attemptHistory.setL3Response("");
		attemptHistory.setL3Status("");
		attemptHistory.setFollowupId((long) 7);

		if (attemptStatusId == 33) {
			attemptHistory.setAdditionalFieldsTag("Email");
		} else if (attemptStatusId == 34) {
			attemptHistory.setAdditionalFieldsTag("Cost");
		} else if (attemptStatusId == 35) {
			attemptHistory.setAdditionalFieldsTag("MI");
		}
		
		ArrayNode additionalInfoList = mapper.createArrayNode();
		additionalInfoList.add(attemptHistoryPOJO.getAdditionalFields());
		
		attemptHistory.setAdditionalFields(additionalInfoList);
		return attemptHistory;
	}
}
