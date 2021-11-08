package fadv.verification.workflow.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fadv.verification.workflow.model.BotRequestHistory;
import fadv.verification.workflow.repository.BotRequestHistoryRepository;
import fadv.verification.workflow.service.EmailService;

@Component
public class BotRequestEmailScheduler {

	@Autowired
	private BotRequestHistoryRepository botRequestHistoryRepository;
	
	private static final String FAILED = "Failed";
	private static final String SUCCESS = "Success";

	@Autowired
	private EmailService emailService;

	@Value("${spring.mail.username}")
	private String fromUserName;

	@Value("${spring.mail.toemailaddress}")
	private String toEmailAddressList;
	
	@Value("${environment.email.identification}")
	private String emailIdentification;
	
	@Value("${scheduled.api.retries.count}")
	private String retryCount;
	
	private static final Logger logger = LoggerFactory.getLogger(BotRequestEmailScheduler.class);

	@Scheduled(cron = "${scheduled.email.request.cron}")
	public void getInProgressBotRequests() {
		logger.info("Email retry scheduler started");
		List<String> botRequestStatus = new ArrayList<>();
		botRequestStatus.add(FAILED);
		List<BotRequestHistory> botRequestHistories = botRequestHistoryRepository
				.getFailedRequests(botRequestStatus, SUCCESS, Integer.parseInt(retryCount));
//		List<BotRequestHistory> newBotRequestHistories = new ArrayList<>();

//		Date todayDate = new Date();

//		for (BotRequestHistory botRequestHistory : botRequestHistories) {
//			if (StringUtils.equalsIgnoreCase(botRequestHistory.getRequestStatus(), FAILED)) {
//				newBotRequestHistories.add(botRequestHistory);
//			} else {
//				if (Utility.getDateDiffInMinutes(botRequestHistory.getEmailDate(), todayDate) > 15) {
//					newBotRequestHistories.add(botRequestHistory);
//				}
//			}
//		}
		if (CollectionUtils.isNotEmpty(botRequestHistories)) {
			notifyFailedBotRequests(botRequestHistories);
		}
	}

	private void notifyFailedBotRequests(List<BotRequestHistory> botRequestHistories) {

		logger.info("botRequestHistories : {}", botRequestHistories);
		
		String subjectLine = emailIdentification + " - Alert !!! Verification SLA not completed.";

		String emailTemplate = "<h3>Below cases failed to meet verification SLA</h3> <ol>";

		String[] toEmailAddress = toEmailAddressList.trim().split("\\s*,\\s*");
		List<String> toEmailAddresses = new ArrayList<>();
		toEmailAddresses.addAll(Arrays.asList(toEmailAddress));
		List<String> ccEmailAddressList = new ArrayList<>();
		List<String> bccEmailAddressList = new ArrayList<>();

		StringBuilder emailTemplateBuilder = new StringBuilder();
		for (BotRequestHistory botRequestHistory : botRequestHistories) {
			emailTemplateBuilder.append(" <li>" + botRequestHistory.getCaseNumber() + "&nbsp;&nbsp;&nbsp;&nbsp;<b>Request Time : </b>"
					+ botRequestHistory.getCreatedDate().toString() + "</li>");
		}
		emailTemplate = emailTemplate + emailTemplateBuilder.toString() + " </ol>";

		String emailSent = emailService.sendEmail(subjectLine, fromUserName, toEmailAddresses, ccEmailAddressList, bccEmailAddressList,
				emailTemplate, null);
		updateBotEmailStatus(botRequestHistories, emailSent);
	}
	
	private void updateBotEmailStatus(List<BotRequestHistory> botRequestHistories, String emailSent) {
		List<BotRequestHistory> newBotRequestHistories = new ArrayList<>();
		for (BotRequestHistory botRequestHistory : botRequestHistories) {
			botRequestHistory.setEmailSent(emailSent);
			newBotRequestHistories.add(botRequestHistory);
		}
		if (CollectionUtils.isNotEmpty(newBotRequestHistories)) {
			botRequestHistoryRepository.saveAll(newBotRequestHistories);
		}
	}
}
