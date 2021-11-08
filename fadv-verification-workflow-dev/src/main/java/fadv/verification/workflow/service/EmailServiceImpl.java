package fadv.verification.workflow.service;

import java.io.File;
import java.util.List;

import javax.mail.internet.MimeMessage;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

	@Autowired
	private JavaMailSender mailSender;
	private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
	private static final String FAILED = "Failed";
	private static final String SUCCESS = "Success";

	@Override
	public String sendEmail(String subjectLine, String fromEmailAddress, List<String> toEmailAddressList,
			List<String> ccEmailAddressList, List<String> bccEmailAddressList, String emailTemplate, File file) {
		String emailSent = FAILED;
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

			if (CollectionUtils.isNotEmpty(toEmailAddressList)) {
				mimeMessageHelper.setTo(toEmailAddressList.stream().toArray(String[]::new));
			}
			if (CollectionUtils.isNotEmpty(ccEmailAddressList)) {
				mimeMessageHelper.setCc(ccEmailAddressList.stream().toArray(String[]::new));
			}
			if (CollectionUtils.isNotEmpty(bccEmailAddressList)) {
				mimeMessageHelper.setBcc(bccEmailAddressList.stream().toArray(String[]::new));
			}

			mimeMessageHelper.setFrom(fromEmailAddress);
			mimeMessageHelper.setSubject(subjectLine);
			mimeMessageHelper.setText(emailTemplate, true);

			if (file != null) {
				FileSystemResource fileSystemResource = new FileSystemResource(file);
				mimeMessageHelper.addAttachment(file.getName(), fileSystemResource);
			}

			mailSender.send(mimeMessage);
			logger.info("Email sent successfully.");
			emailSent = SUCCESS;

		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : {}", e.getMessage());
		}
		return emailSent;
	}
}
