package fadv.verification.workflow.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import fadv.verification.workflow.model.CaseSpecificInfo;
import fadv.verification.workflow.model.CaseSpecificRecordDetail;
import fadv.verification.workflow.service.OnlineRouterService;

public class OnlineRouterTask implements Callable<String> {

	private final ObjectMapper mapper;
	private final List<CaseSpecificRecordDetail> caseSpecificRecordDetails;
	private final CaseSpecificInfo caseSpecificInfo;
	private final OnlineRouterService onlineRouterService;
	private static final Logger logger = LoggerFactory.getLogger(OnlineRouterTask.class);
	private final CountDownLatch countDownLatch;

	public OnlineRouterTask(ObjectMapper mapper, List<CaseSpecificRecordDetail> caseSpecificRecordDetails,
			CaseSpecificInfo caseSpecificInfo, OnlineRouterService onlineRouterService, CountDownLatch countDownLatch) {
		this.mapper = mapper;
		this.caseSpecificRecordDetails = caseSpecificRecordDetails;
		this.caseSpecificInfo = caseSpecificInfo;
		this.onlineRouterService = onlineRouterService;
		this.countDownLatch = countDownLatch;
	}

	@Override
	public String call() {
		logger.info("Online router started at : {}", new Date());

		List<CaseSpecificRecordDetail> onlineCaseSpecificRecordDetails = new ArrayList<>();
		try {
			for (CaseSpecificRecordDetail caseSpecificRecordDetail : caseSpecificRecordDetails) {
				processRouters(mapper, caseSpecificRecordDetail, caseSpecificInfo);
				if (StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getComponentName(), "Database")
						&& StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getCheckStatus(), "Open")
						&& !StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getOnlineStatus(), "Processed")) {
					onlineCaseSpecificRecordDetails.add(caseSpecificRecordDetail);
				}
			}
			if (CollectionUtils.isNotEmpty(onlineCaseSpecificRecordDetails)) {
				onlineRouterService.processOnlineRouterService(mapper, onlineCaseSpecificRecordDetails,
						caseSpecificInfo);
			}
		} catch (Exception e) {
			logger.error("Online Task Exception:{}", e.getMessage());
		} finally {
			countDownLatch.countDown();
		}
		logger.info("Online router ended at : {}", new Date());
		return "Online Router Completed";
	}

	private void processRouters(ObjectMapper mapper, CaseSpecificRecordDetail caseSpecificRecordDetail,
			CaseSpecificInfo caseSpecificInfo) {
		if (StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getComponentName(), "Database")
				&& StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getCheckStatus(), "Pending")
				&& !StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getOnlineStatus(), "Processed")) {
			try {
				onlineRouterService.processRecordsForMI(mapper, caseSpecificRecordDetail, caseSpecificInfo);
			} catch (Exception e) {
				logger.error("Exception while processing Online Router : {}", e.getMessage());
				e.printStackTrace();
			}
		}
		if (StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getCheckStatus(), "Pending")
				&& (StringUtils.equalsIgnoreCase(caseSpecificInfo.getClientCode(), "W142")
						|| StringUtils.equalsIgnoreCase(caseSpecificInfo.getClientCode(), "W173"))) {
			try {
				onlineRouterService.processRecordsForMI(mapper, caseSpecificRecordDetail, caseSpecificInfo);
			} catch (Exception e) {
				logger.error("Exception while processing Online Router : {}", e.getMessage());
				e.printStackTrace();
			}
		}

		if (StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getCheckStatus(), "Cost Pending")
				&& (StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getComponentName(), "Education")
						|| StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getComponentName(), "Employment"))) {
			try {
				onlineRouterService.processRecordsForCost(mapper, caseSpecificRecordDetail, caseSpecificInfo);
			} catch (Exception e) {
				logger.error("Exception while processing Online Router Cost Approval : {}", e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
