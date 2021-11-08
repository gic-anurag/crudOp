package fadv.verification.workflow.service;

import java.io.File;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface EmailService {

	String sendEmail(String subjectLine, String fromEmailAddress, List<String> toEmailAddressList,
			List<String> ccEmailAddressList, List<String> bccEmailAddressList, String emailTemplate, File file);

}
