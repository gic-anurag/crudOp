package com.gic.fadv.spoc.mail;

import java.io.File;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailService {

	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    private static final String SENDER_ADDRESS="emp.verification@fadv.com";


	@Autowired
	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	/**
	 * This method is used to send email with CC, BCC and file attachments
	 * @param lstTo List<String>
	 * @param subject List<String>
	 * @param body List<String>
	 * @param lstBcc List<String>
	 * @param lstCc List<String>
	 * @param lstFilePaths List<String>
	 * @throws MessagingException throws {@link MessagingException}
	 */
	@Async
	 void sendMailWithCcBccAndAttachment(List<String> lstTo,String subject, String body,List<String> lstBcc, List<String> lstCc,List<String> lstFilePaths) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		if(CollectionUtils.isNotEmpty(lstTo)) {
			helper.setTo(lstTo.stream().toArray(String[]::new));
		}
		helper.setReplyTo(SENDER_ADDRESS);
		helper.setFrom(SENDER_ADDRESS);
		helper.setSubject(subject);
		helper.setText(body, true);
		if(CollectionUtils.isNotEmpty(lstBcc)) {
			helper.setBcc(lstBcc.stream().toArray(String[]::new));
		}
		if(CollectionUtils.isNotEmpty(lstCc)) {
			helper.setBcc(lstCc.stream().toArray(String[]::new));
		}
		if(CollectionUtils.isNotEmpty(lstFilePaths)) {
			lstFilePaths.forEach(fileName->{
				try {
					helper.addAttachment(fileName, new File(fileName));
				} catch (MessagingException e) {
					logger.error(e.getMessage(),e);
				}
			});
		}
		
		mailSender.send(message);
	}
	
	/**
	 * This method is used to send email with CC, BCC and file attachments
	 * @param lstTo List
	 * @param subject List
	 * @param body List
	 * @param lstBcc List
	 * @param lstCc List
	 * @param lstFiles List
	 * @throws MessagingException throws {@link MessagingException}
	 */
	@Async
	void sendMailWithCcBccAndFileAttachment(List<String> lstTo, String subject, String body, List<String> lstBcc, List<String> lstCc, List<File> lstFiles) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		if(CollectionUtils.isNotEmpty(lstTo)) {
			helper.setTo(lstTo.stream().toArray(String[]::new));
		}
		
		helper.setReplyTo(SENDER_ADDRESS);
		helper.setFrom(SENDER_ADDRESS);
		helper.setSubject(subject);
		helper.setText(body, true);
		if(CollectionUtils.isNotEmpty(lstBcc)) {
			helper.setBcc(lstBcc.stream().toArray(String[]::new));
		}
		if(CollectionUtils.isNotEmpty(lstCc)) {
			helper.setCc(lstCc.stream().toArray(String[]::new));
		}
		if(CollectionUtils.isNotEmpty(lstFiles)) {
			lstFiles.forEach(file->{
				try {
					String fileName = file.getAbsolutePath();
					String attachMentName = fileName.substring(fileName.lastIndexOf('/')+1, fileName.length());
					helper.addAttachment(attachMentName, new File(fileName));
				} catch (MessagingException e) {
					logger.error(e.getMessage(),e);
				}
			});
		}

		mailSender.send(message);
	}
}
