package fadv.verification.workflow.service;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fadv.verification.workflow.model.BotRequestHistory;
import fadv.verification.workflow.repository.BotRequestHistoryRepository;

@Service
public class BotRerunServiceImpl implements BotRerunService {

	@Autowired
	private ApiService apiService;

	@Value("${workflow.bot.async.url}")
	private String workflowBotUrl;

	@Autowired
	private BotRequestHistoryRepository botRequestHistoryRepository;

	private static final Logger logger = LoggerFactory.getLogger(BotRerunServiceImpl.class);
	ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Override
	public ResponseEntity<ObjectNode> rerunBotService(String caseNumber) {
		logger.info("Case Number received for rerun : {}", caseNumber);

		if (caseNumber != null && StringUtils.isNotEmpty(caseNumber)) {
			List<BotRequestHistory> botRequestHistories = botRequestHistoryRepository.findByCaseNumber(caseNumber);

			if (CollectionUtils.isNotEmpty(botRequestHistories)) {
				JsonNode requestBody = botRequestHistories.get(0).getRequestBody();

				try {
					String response = apiService.sendDataToPost(workflowBotUrl, mapper.writeValueAsString(requestBody));
					
					return new ResponseEntity<>(mapper.readValue(response, ObjectNode.class), HttpStatus.OK);

				} catch (Exception e) {
					logger.error("Exception while mapping bot request body : {}", e.getMessage());
					return new ResponseEntity<>(createResponse("Exception while mapping bot request body", false),
							HttpStatus.EXPECTATION_FAILED);
				}

			}
			return new ResponseEntity<>(createResponse("Bot request not found for given case number", false),
					HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(createResponse("Invalid Check Id", false), HttpStatus.BAD_REQUEST);
	}

	private ObjectNode createResponse(String message, boolean success) {
		ObjectNode responseNode = mapper.createObjectNode();

		responseNode.put("message", message);
		responseNode.put("success", success);

		return responseNode;
	}
}
