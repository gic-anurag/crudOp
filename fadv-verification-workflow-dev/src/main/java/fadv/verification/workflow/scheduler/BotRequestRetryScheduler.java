package fadv.verification.workflow.scheduler;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fadv.verification.workflow.model.BotRequestHistory;
import fadv.verification.workflow.repository.BotRequestHistoryRepository;
import fadv.verification.workflow.service.ApiService;

@Component
public class BotRequestRetryScheduler {

	@Autowired
	private BotRequestHistoryRepository botRequestHistoryRepository;

	@Autowired
	private ApiService apiService;
	
	@Value("${workflow.bot.async.url}")
	private String workflowBotUrl;
	
	@Value("${scheduled.api.retries.count}")
	private String retryCount;
	
	
	private static final String FAILED = "Failed";
	private static final Logger logger = LoggerFactory.getLogger(BotRequestRetryScheduler.class);

	@Scheduled(cron = "${scheduled.api.retries.cron}")
	public void runParallelApiRetries() {
		List<String> botRequestStatus = new ArrayList<>();
		botRequestStatus.add(FAILED);
		List<BotRequestHistory> botRequestHistories = botRequestHistoryRepository
				.getNotProcessedRequests(botRequestStatus, Integer.parseInt(retryCount));
		if (CollectionUtils.isNotEmpty(botRequestHistories)) {
			for (BotRequestHistory botRequestHistory : botRequestHistories) {
				callBotAsyncApi(botRequestHistory);
			}
		}
	}

	private void callBotAsyncApi(BotRequestHistory botRequestHistory) {
		// Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String requestStr;
		try {
			requestStr = mapper.writeValueAsString(botRequestHistory.getRequestBody());
			apiService.sendDataToPost(workflowBotUrl, requestStr);
		} catch (JsonProcessingException e) {
			logger.error("Exception while mapping bot request body : {}", e.getMessage());
			e.printStackTrace();
		}
	}
}
