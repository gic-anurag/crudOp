package fadv.verification.workflow.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fadv.verification.workflow.pojo.RootPOJO;

@Service
public interface BotRequestService {

	RootPOJO processBotRequest(ObjectMapper mapper, RootPOJO rootPOJO);

	ObjectNode respondBotRequest(RootPOJO rootPOJO);

	void updateIncomingRequestStatus(String caseNumber, String requestStatus);

	void submitBotRequest(ObjectMapper mapper, RootPOJO rootPOJO);

}
