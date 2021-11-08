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
import fadv.verification.workflow.service.VendorInputRouterService;

public class VendorInputRouterTask implements Callable<String> {

	private final ObjectMapper mapper;
	private final List<CaseSpecificRecordDetail> caseSpecificRecordDetails;
	private final CaseSpecificInfo caseSpecificInfo;
	private final VendorInputRouterService vendorInputRouterService;
	private static final Logger logger = LoggerFactory.getLogger(VendorInputRouterTask.class);
	private static final String CRIMINAL = "Criminal";
	private static final String EDUCATION = "Education";
	private static final String ADDRESS = "Address";
	private final CountDownLatch countDownLatch;
	private final ObjectNode otherDetails;

	public VendorInputRouterTask(ObjectMapper mapper, List<CaseSpecificRecordDetail> caseSpecificRecordDetails,
			CaseSpecificInfo caseSpecificInfo, VendorInputRouterService vendorInputRouterService,
			ObjectNode otherDetails, CountDownLatch countDownLatch) {
		this.mapper = mapper;
		this.caseSpecificRecordDetails = caseSpecificRecordDetails;
		this.caseSpecificInfo = caseSpecificInfo;
		this.vendorInputRouterService = vendorInputRouterService;
		this.countDownLatch = countDownLatch;
		this.otherDetails = otherDetails;
	}

	@Override
	public String call() {
		logger.info("Vendor router started at : {}", new Date());
		try {
			for (CaseSpecificRecordDetail caseSpecificRecordDetail : caseSpecificRecordDetails) {
				ObjectNode requestNode = mapper.createObjectNode();
				requestNode.set("caseSpecificRecordDetail", mapper.valueToTree(caseSpecificRecordDetail));
				requestNode.set("caseSpecificInfo", mapper.valueToTree(caseSpecificInfo));
				requestNode.set("otherDetails", otherDetails);
				try {
					String requestStr = mapper.writeValueAsString(requestNode);
					processRouters(mapper, caseSpecificRecordDetail, caseSpecificInfo, requestStr, requestNode);
				} catch (JsonProcessingException e) {
					logger.error("Exception while converting request node to string : {}", e.getMessage());
				}
			}
		} catch (Exception e) {
			logger.error("Vendor Task Exception:{}", e.getMessage());
		} finally {
			countDownLatch.countDown();
		}
		logger.info("Vendor router ended at : {}", new Date());
		return "Vendor Input Router Completed";
	}

	private void processRouters(ObjectMapper mapper, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo, String requestStr, JsonNode requestNode) throws JsonProcessingException {
		if ((StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getComponentName(), ADDRESS)
				|| StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getComponentName(), EDUCATION))
				&& StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getCheckStatus(), "Open")
				&& !StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getVendorStatus(), "Processed")) {
			try {
				vendorInputRouterService.processVendorInputRouters(mapper, caseSpecificRecordDetail, caseSpecificInfo,
						requestStr, requestNode);
			} catch (JsonProcessingException e) {
				logger.error("Exception while processing Spoc Router : {}", e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
