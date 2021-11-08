package fadv.verification.workflow.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fadv.verification.workflow.model.CaseSpecificInfo;
import fadv.verification.workflow.model.CaseSpecificRecordDetail;
import fadv.verification.workflow.pojo.FutureResultPOJO;
import fadv.verification.workflow.repository.CaseSpecificRecordDetailRepository;
import fadv.verification.workflow.task.CBVUTVRouterTask;
import fadv.verification.workflow.task.OnlineRouterTask;
import fadv.verification.workflow.task.PassportTask;
//import fadv.verification.workflow.task.SpocRouterTask
import fadv.verification.workflow.task.SuspectRouterTask;
import fadv.verification.workflow.task.VendorInputRouterTask;
import fadv.verification.workflow.task.WellknownRouterTask;

@Service
public class RouterServiceImpl implements RouterService {

	private static final String PROCESSED = "Processed";

	@Autowired
	private CBVUTVRouterService cbvutvRouterService;

	@Autowired
	private OnlineRouterService onlineRouterService;

	@Autowired
	private VendorInputRouterService vendorInputRouterService;

//	@Autowired
//	private SpocRouterService spocRouterService

	@Autowired
	private SuspectRouterService suspectRouterService;

	@Autowired
	WellknownRouterService wellknownRouterService;
	
	@Autowired
	PassportService passportService;
	
	@Autowired
	BotRequestService botRequestService;

	@Autowired
	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;

	private static final Logger logger = LoggerFactory.getLogger(RouterServiceImpl.class);

	@Override
	public void processCaseRecordDetails(List<CaseSpecificRecordDetail> caseSpecificRecordDetails,
			CaseSpecificInfo caseSpecificInfo, ObjectNode otherDetails) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		ExecutorService executorService = Executors.newFixedThreadPool(6);
		final CountDownLatch countDownLatch = new CountDownLatch(6);
		Future<String> onlineFuture = executorService.submit(new OnlineRouterTask(mapper, caseSpecificRecordDetails,
				caseSpecificInfo, onlineRouterService, countDownLatch));
//		Future<String> spocFuture = executorService.submit(new SpocRouterTask(mapper, caseSpecificRecordDetails,
//				caseSpecificInfo, spocRouterService, countDownLatch))
		Future<String> cbvUtvFuture = executorService.submit(new CBVUTVRouterTask(mapper, caseSpecificRecordDetails,
				caseSpecificInfo, cbvutvRouterService, countDownLatch));
		Future<String> vendorInputFuture = executorService.submit(new VendorInputRouterTask(mapper,
				caseSpecificRecordDetails, caseSpecificInfo, vendorInputRouterService, otherDetails, countDownLatch));
		Future<String> suspectFuture = executorService.submit(new SuspectRouterTask(mapper, caseSpecificRecordDetails,
				caseSpecificInfo, suspectRouterService, countDownLatch));
		Future<String> wellknownFuture = executorService.submit(new WellknownRouterTask(mapper,
				caseSpecificRecordDetails, caseSpecificInfo, wellknownRouterService, countDownLatch));
		Future<String> passportFuture = executorService.submit(new PassportTask(mapper,
				caseSpecificRecordDetails, caseSpecificInfo, passportService, countDownLatch));

		try {
			countDownLatch.await();
			FutureResultPOJO futureResultPOJO = new FutureResultPOJO();
			futureResultPOJO.setOnlineFuture(onlineFuture);
//			futureResultPOJO.setSpocFuture(spocFuture)
			futureResultPOJO.setCbvUtvFuture(cbvUtvFuture);
			futureResultPOJO.setVendorInputFuture(vendorInputFuture);
			futureResultPOJO.setSuspectFuture(suspectFuture);
			futureResultPOJO.setWellknownFuture(wellknownFuture);
			futureResultPOJO.setPassportFuture(passportFuture);
			processFutureResult(futureResultPOJO, caseSpecificRecordDetails, caseSpecificInfo);
		} catch (InterruptedException | ExecutionException e1) {
			logger.error("Exception occurred while processing router threads : {}", e1.getMessage());
			Thread.currentThread().interrupt();
			e1.printStackTrace();
		} finally {
			executorService.shutdown();
		}
	}

	private void processFutureResult(FutureResultPOJO futureResultPOJO, 
			List<CaseSpecificRecordDetail> caseSpecificRecordDetails, CaseSpecificInfo caseSpecificInfo) throws InterruptedException, ExecutionException {
		String onlineFutureStr = futureResultPOJO.getOnlineFuture().get() != null ? futureResultPOJO.getOnlineFuture().get() : "";
//		String spocFutureStr = futureResultPOJO.getSpocFuture().get() != null ? futureResultPOJO.getSpocFuture().get() : ""
		String vendorInputFutureStr = futureResultPOJO.getVendorInputFuture().get() != null ? futureResultPOJO.getVendorInputFuture().get() : "";
		String cbvUtvFutureStr = futureResultPOJO.getCbvUtvFuture().get() != null ? futureResultPOJO.getCbvUtvFuture().get() : "";
		String suspectFutureStr = futureResultPOJO.getSuspectFuture().get() != null ? futureResultPOJO.getSuspectFuture().get() : "";
		String wellknownFutureStr = futureResultPOJO.getWellknownFuture().get() != null ? futureResultPOJO.getWellknownFuture().get() : "";
		String passportFutureStr = futureResultPOJO.getPassportFuture().get() != null
				? futureResultPOJO.getPassportFuture().get()
				: "";
		
		if (StringUtils.equalsIgnoreCase(onlineFutureStr, "Online Router Completed")
//				&& StringUtils.equalsIgnoreCase(spocFutureStr, "Spoc Router Completed")
				&& StringUtils.equalsIgnoreCase(vendorInputFutureStr, "Vendor Input Router Completed")
				&& StringUtils.equalsIgnoreCase(cbvUtvFutureStr, "CBVUTV Router Completed")
				&& StringUtils.equalsIgnoreCase(suspectFutureStr, "Suspect Router Completed")
				&& StringUtils.equalsIgnoreCase(wellknownFutureStr, "Wellknown Router Completed")
				&& StringUtils.equalsIgnoreCase(passportFutureStr, "Passport Service Completed")) {
			    
			botRequestService.updateIncomingRequestStatus(caseSpecificInfo.getCaseNumber(), PROCESSED);
			List<CaseSpecificRecordDetail> newCaseSpecificRecordDetails = new ArrayList<>();
			for (CaseSpecificRecordDetail caseSpecificRecordDetail : caseSpecificRecordDetails) {
				if (StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getOnlineStatus(), PROCESSED)
						&& StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getSpocStatus(), PROCESSED)
						&& StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getCbvUtvStatus(), PROCESSED)
						&& StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getVendorStatus(), PROCESSED)
						&& StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getSuspectStatus(), PROCESSED)
						&& StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getWellknownStatus(), PROCESSED)
						&& StringUtils.equalsIgnoreCase(caseSpecificRecordDetail.getPassportStatus(), PROCESSED)) {
					newCaseSpecificRecordDetails.add(caseSpecificRecordDetail);
				}
			}
			updateCaseSpecificDetails(newCaseSpecificRecordDetails);
		}
	}

	private void updateCaseSpecificDetails(List<CaseSpecificRecordDetail> newCaseSpecificRecordDetails) {
		if (CollectionUtils.isNotEmpty(newCaseSpecificRecordDetails)) {
			caseSpecificRecordDetailRepository.saveAll(newCaseSpecificRecordDetails);
		}
	}
}
