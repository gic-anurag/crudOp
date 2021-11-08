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
import fadv.verification.workflow.service.WellknownRouterService;

public class WellknownRouterTask implements Callable<String> {

	private final ObjectMapper mapper;
	private final List<CaseSpecificRecordDetail> caseSpecificRecordDetails;
	private final CaseSpecificInfo caseSpecificInfo;
	private final WellknownRouterService wellknownRouterService;
	private static final Logger logger = LoggerFactory.getLogger(WellknownRouterTask.class);
	private final CountDownLatch countDownLatch;

	public WellknownRouterTask(ObjectMapper mapper, List<CaseSpecificRecordDetail> caseSpecificRecordDetails,
			CaseSpecificInfo caseSpecificInfo, WellknownRouterService wellknownRouterService,
			CountDownLatch countDownLatch) {
		this.mapper = mapper;
		this.caseSpecificRecordDetails = caseSpecificRecordDetails;
		this.caseSpecificInfo = caseSpecificInfo;
		this.wellknownRouterService = wellknownRouterService;
		this.countDownLatch = countDownLatch;
	}

	@Override
	public String call() {
		logger.info("Wellknow router started at : {}", new Date());
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
			logger.error("Wellknown Task Exception:{}", e.getMessage());
		} finally {
			countDownLatch.countDown();
		}
		logger.info("Wellknow router ended at : {}", new Date());
		return "Wellknown Router Completed";
	}

	private void processRouters(ObjectMapper mapper, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo, String requestStr, JsonNode requestNode) throws JsonProcessingException {
		if (StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getComponentName(), "Employment")
				&& StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getCheckStatus(), "Open")
				&& !StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getWellknownStatus(), "Processed")) {
			try {
				wellknownRouterService.processWellknownRouters(mapper, caseSpecificRecordDetail, caseSpecificInfo,
						requestStr, requestNode);
			} catch (JsonProcessingException e) {
				logger.error("Exception while processing Wellknown Router : {}", e.getMessage());
				e.printStackTrace();
			}
		}
	}
}