package fadv.verification.workflow.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public interface BotRerunService {

	ResponseEntity<ObjectNode> rerunBotService(String caseNumber);

}
