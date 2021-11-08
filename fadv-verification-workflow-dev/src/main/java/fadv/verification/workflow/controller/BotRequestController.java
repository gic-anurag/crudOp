package fadv.verification.workflow.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fadv.verification.workflow.pv.pojo.CaseReference;
import fadv.verification.workflow.pv.pojo.PVData;
import fadv.verification.workflow.pv.pojo.PVRequest;
import fadv.verification.workflow.pv.pojo.Result;
import fadv.verification.workflow.repository.L3ApiRequestHistoryRepository;
import fadv.verification.workflow.repository.RouterHistoryRepository;
import fadv.verification.workflow.model.L3ApiRequestHistory;
import fadv.verification.workflow.model.RouterHistory;
import fadv.verification.workflow.pojo.RootPOJO;
import fadv.verification.workflow.service.ApiService;
import fadv.verification.workflow.service.BotRequestService;
import fadv.verification.workflow.service.BotRerunService;
import fadv.verification.workflow.utility.Utility;
import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class BotRequestController {

	@Autowired
	private BotRequestService botRequestService;

	@Value("${verification.url.checkid.l3}")
	private String verificationStatusUrlL3;

	@Autowired
	private ApiService apiService;
	@Autowired
	private L3ApiRequestHistoryRepository l3ApiRequestHistoryRepository;

	@Autowired
	private BotRerunService botRerunService;

	@Autowired
	private RouterHistoryRepository routerHistoryRepository;

	private static final Logger logger = LoggerFactory.getLogger(BotRequestController.class);

	@PostMapping("/bot-request")
	public RootPOJO processBotRequest(@RequestBody RootPOJO rootPOJO) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		logger.info("Bot requested data : {}", rootPOJO);
		return botRequestService.processBotRequest(mapper, rootPOJO);
	}

	@PostMapping("/test-name")
	public String checkNames(@RequestBody JsonNode requestNode) {
		String primaryName = requestNode.has("primaryName") ? requestNode.get("primaryName").asText() : "";
		String secondaryName = requestNode.has("secondaryName") ? requestNode.get("secondaryName").asText() : "";
		return Utility.compareName(primaryName, secondaryName);
	}

	@PostMapping("/aysnc/bot-request")
	public ObjectNode processBotRequestAysnc(@RequestBody RootPOJO rootPOJO) {
		// Instantiate an executor service
		ExecutorService executor = Executors.newSingleThreadExecutor();

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		logger.info("Bot requested data : {}", rootPOJO);
		executor.submit(() -> botRequestService.submitBotRequest(mapper, rootPOJO));
		logger.info("Bot requested data end");
		executor.shutdown();

		return botRequestService.respondBotRequest(rootPOJO);
	}

	@PostMapping("/aysnc/bot-scheduler")
	public void processBotSchedulerRequest(@RequestBody RootPOJO rootPOJO) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		botRequestService.processBotRequest(mapper, rootPOJO);
	}

	@GetMapping(path = "/rerun-bot-request/{caseNumber}", produces = "application/json")
	public ResponseEntity<ObjectNode> rerunBotService(@PathVariable(value = "caseNumber") String caseNumber) {
		return botRerunService.rerunBotService(caseNumber);
	}

	// This Service is used to receive the data from bot for Passport verification
	@ApiOperation(value = "This service is used to receive the data from bot for Passport verification", response = ObjectNode.class)
	@PostMapping(path = "/passport-verification", consumes = "application/json", produces = "application/json")
	public ObjectNode doPassportVerification(@RequestBody JsonNode requestBody) {
		logger.info("Got Request :\n {}", requestBody);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			PVRequest pvRequest = mapper.treeToValue(requestBody, PVRequest.class);
			logger.info("Value of pvRequest" + pvRequest.toString());
			// Other logic for checking passport verified or not
			String checkId = "";
			List<PVData> pvData = pvRequest.getData() != null ? pvRequest.getData() : new ArrayList<>();
			if (CollectionUtils.isNotEmpty(pvData)) {
				Result result = pvData.get(0).getResult();
				// Take out CheckID from caseReference
				if (result != null) {
					CaseReference caseRefernce = result.getCaseReference();
					if (caseRefernce != null) {
						checkId = caseRefernce.getCheckId() != null ? caseRefernce.getCheckId() : "";
						if (StringUtils.isNotEmpty(checkId)) {
							L3ApiRequestHistory l3ApiRequestHistory = new L3ApiRequestHistory();
							RouterHistory routerHistory = new RouterHistory();
							routerHistory = routerHistoryRepository.findByCheckIdAndCaseNumber(checkId,
									caseRefernce.getCaseNo());
							if (result.getPassportVerification() != null) {
								// Send Data to L3 for Clear Taggging
								l3ApiRequestHistory = l3ApiRequestHistoryRepository.findByCheckIdAndRequestType(checkId,
										"passport");
								if (l3ApiRequestHistory != null) {
									// Match CheckID and Router History Processed
									apiService.sendDataToGet(verificationStatusUrlL3 + checkId);
									routerHistory.setCurrentEngineStatus("Processed");
									routerHistory.setEndTime(new Date());
								}
							} else {
								routerHistory.setCurrentEngineStatus("Failed");
								routerHistory.setEndTime(new Date());
							}
							if (routerHistory != null) {
								routerHistoryRepository.save(routerHistory);
							} else {
								logger.info("Unble to Save");
							}

						} else {
							logger.info("CheckId is empty");
						}
					} else {
						logger.info("Case Reference is null");
					}
				} else {
					logger.info("Result is null");
				}
			} else {
				logger.info("PV Data is Empty");
				// Router History Failed
			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ObjectNode response = (ObjectNode) requestBody;
		// ObjectNode response = onlineWorkflowService.processRequestBody(requestBody);
		logger.info("Record request Processed");
		return response;
	}
}
