package fadv.verification.workflow.task;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fadv.verification.workflow.model.CaseSpecificInfo;
import fadv.verification.workflow.model.CaseSpecificRecordDetail;
import fadv.verification.workflow.service.SpocRouterService;

public class SpocRouterTask implements Callable<String> {

	private final ObjectMapper mapper;
	private final List<CaseSpecificRecordDetail> caseSpecificRecordDetails;
	private final CaseSpecificInfo caseSpecificInfo;
	private final SpocRouterService spocRouterService;
	private static final Logger logger = LoggerFactory.getLogger(SpocRouterTask.class);
	private final CountDownLatch countDownLatch;

	public SpocRouterTask(ObjectMapper mapper, List<CaseSpecificRecordDetail> caseSpecificRecordDetails,
			CaseSpecificInfo caseSpecificInfo, SpocRouterService spocRouterService, CountDownLatch countDownLatch) {
		this.mapper = mapper;
		this.caseSpecificRecordDetails = caseSpecificRecordDetails;
		this.caseSpecificInfo = caseSpecificInfo;
		this.spocRouterService = spocRouterService;
		this.countDownLatch = countDownLatch;
	}

	@Override
	public String call() {
		logger.info("SPOC router started at : {}", new Date());
		try {
		for (CaseSpecificRecordDetail caseSpecificRecordDetail : caseSpecificRecordDetails) {
			ObjectNode requestNode = mapper.createObjectNode();
			requestNode.set("caseSpecificRecordDetail", mapper.valueToTree(caseSpecificRecordDetail));
			requestNode.set("caseSpecificInfo", mapper.valueToTree(caseSpecificInfo));
			try {
				String requestStr = mapper.writeValueAsString(requestNode);
				processRouters(mapper, caseSpecificRecordDetail, caseSpecificInfo, requestStr, requestNode);
			} catch (JsonProcessingException e) {
				logger.error("Exception while converting request node to string : {}", e.getMessage());
			}
		}
		} catch (Exception e) {
			logger.error("SPOC Task Exception:{}", e.getMessage());
		} finally {
			countDownLatch.countDown();
		}
		logger.info("SPOC router ended at : {}", new Date());
		return "Spoc Router Completed";
	}

	private void processRouters(ObjectMapper mapper, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo, String requestStr, JsonNode requestNode) throws JsonProcessingException {
		if (StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getComponentName(), "Employment")
				&& StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getProduct(), "HR")
				&& StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getCheckStatus(), "Open")
				&& !StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getSpocStatus(), "Processed")
				) {
			try {
				spocRouterService.processSpocRouters(mapper, caseSpecificRecordDetail, caseSpecificInfo, requestStr,
						requestNode);
			} catch (JsonProcessingException e) {
				logger.error("Exception while processing Spoc Router : {}", e.getMessage());
				e.printStackTrace();
			}
		}
	}
}