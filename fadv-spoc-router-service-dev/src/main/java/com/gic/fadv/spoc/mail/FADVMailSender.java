package com.gic.fadv.spoc.mail;


import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;



@Component
public class FADVMailSender {
	private static final String DOMAIN_URL = "domainUrl";

	private static final String EMAIL_PROBLEM_TEXT = "EMAIL_PROBLEM_TEXT";

	private static final Logger logger = LoggerFactory.getLogger(FADVMailSender.class);
	
    private final EmailService emailService;
    private final EmailFormatter emailFormatter;

    @Value("${domain.url}")
    private String domainUrl;

    @Autowired
    public FADVMailSender(EmailService emailService, EmailFormatter emailFormatter) {
        this.emailService = emailService;
        this.emailFormatter = emailFormatter;
    }

    private String getFullPath(String url){
        return domainUrl+url;
    }
    
    public void sendEmailTempateWithFileAttachment(String emailAddress, Map<String, String> params, String template, String subject,List<File> fileObjList){
	       try {

	    	   //String logoImgTag = getFooterLogo();	           
				
	           //params.put(ORTConstants.MENU_ORTELLIGENCE, ORTConstants.ORTELLIGENCE);           
	           //params.put(ORTConstants.LOGO_IMG_TAG, logoImgTag);
	           
	           if(StringUtils.isNotEmpty(template)) {
	               String body = emailFormatter.formatResourceMessage(template, params);
	               
	               for(Map.Entry<String, String> pair : params.entrySet()){
	                   String key="##"+pair.getKey()+"##";
	                   subject = subject.replace(key,pair.getValue());
	               }
	               emailService.sendMailWithCcBccAndFileAttachment(Collections.singletonList(emailAddress), subject, body, null, null, fileObjList);
	               logger.info("Email is sent successfull to {}",emailAddress);
	           }

	       }catch (Exception e){
	    	   logger.error(e.getMessage(),e);
	       }
	    }
    /**
     * This method is used to generate reset password link.
     * @param userMaster
     * @param authLink
     * @return
     */
	/*
	 * public String getPasswordLink(UserMaster userMaster, String authLink) {
	 * String url = "account/activate?id="; String incUserId =
	 * AESEncryption.encrypt(userMaster.getUserId().toString()); String paramValues
	 * = "##userId##@@@@@##key##".replace("##userId##",
	 * incUserId).replace("##key##", userMaster.getActivationKey()); return
	 * getFullPath(url + AESEncryption.encrypt(paramValues) + "&key=" + authLink); }
	 */
    /**
     * This method is used to send Verification Link
     * @param to String
     * @param key String
     * @param name String
     * @param userId String
     */
	/*
	 * public void sendVerificationLink(UserMaster userMaster,String authLink){ try
	 * { String activationLink = getPasswordLink(userMaster, authLink); String
	 * logoImgTag = getFooterLogo();
	 * 
	 * HashMap<String, String> params = new HashMap<>(); params.put("name",
	 * getFirstNameOrUserName(userMaster)); params.put("link", activationLink);
	 * params.put(DOMAIN_URL, domainUrl); params.put(ORTConstants.USER_NAME,
	 * userMaster.getUserName()); params.put(ORTConstants.MENU_ORTELLIGENCE,
	 * ORTConstants.ORTELLIGENCE); params.put(EMAIL_PROBLEM_TEXT,
	 * ORTConstants.EMAIL_PROBLEM_TEXT); params.put(ORTConstants.LOGO_IMG_TAG,
	 * logoImgTag);
	 * 
	 * String template = null; String subject = null;
	 * if(authLink.equals(ORTConstants.ACT_KEY)) { template =
	 * "account-verification"; subject =
	 * ORTConstants.ORTELLIGENCE_ACCOUNT_ACTIVATION; }else
	 * if(authLink.equals(ORTConstants.FORGET_KEY)) { template = "reset-password";
	 * subject = ORTConstants.ORTELLIGENCE_RESET; }
	 * 
	 * if(StringUtils.isNotEmpty(template)) { String body =
	 * emailFormatter.formatResourceMessage(template, params);
	 * emailService.sendMailWithCcBccAndAttachment(Collections.singletonList(
	 * userMaster.getEmailAddress()), subject, body, null, null, null);
	 * logger.info("Verification Link email Send successfully"); }
	 * 
	 * }catch (Exception e){ logger.error(e.getMessage(),e); } }
	 */

    /**
     * This method is used to get email footer logo
     * @return {@link String}
     */
	/*
	 * private String getFooterLogo() { String logoPath = domainUrl.replace("#/",
	 * "") + "assets/images/logo.png"; return
	 * "<img style='height: 100px; width: 140px;' alt='Logo' src='" + logoPath +
	 * "'>"; }
	 */
    
    /**
     * This method is used to send Confirmation Email
     * @param to String
     * @param name String
     */
	/*
	 * public void sendConfirmationEmail(UserMaster userMaster, String authLink){
	 * try {
	 * 
	 * String logoImgTag = getFooterLogo();
	 * 
	 * HashMap<String, String> params = new HashMap<>(); params.put("name",
	 * getFirstNameOrUserName(userMaster)); params.put(DOMAIN_URL, domainUrl);
	 * params.put(ORTConstants.USER_NAME, userMaster.getUserName());
	 * params.put(ORTConstants.MENU_ORTELLIGENCE, ORTConstants.ORTELLIGENCE);
	 * params.put(EMAIL_PROBLEM_TEXT, ORTConstants.EMAIL_PROBLEM_TEXT);
	 * params.put(ORTConstants.LOGO_IMG_TAG, logoImgTag);
	 * 
	 * String template = null; String subject = null;
	 * if(authLink.equals(ORTConstants.ACT_KEY)) { template =
	 * "account-confirmation"; subject =
	 * ORTConstants.ORTELLIGENCE_ACCOUNT_CONFIRMATION; }else
	 * if(authLink.equals(ORTConstants.FORGET_KEY)) { template =
	 * "password-confirmation"; subject = ORTConstants.KEY_CHANGE_SUCCESSFULLY; }
	 * 
	 * if(StringUtils.isNotEmpty(template)) { String body =
	 * emailFormatter.formatResourceMessage(template, params);
	 * emailService.sendMailWithCcBccAndAttachment(Collections.singletonList(
	 * userMaster.getEmailAddress()), subject, body, null, null, null);
	 * logger.info("Account confirmation email Send successfully"); }
	 * 
	 * }catch (Exception e){ logger.error(e.getMessage(),e); } }
	 */
    
    /**
     * This method is used to get first name or user name
     * @param userMaster {@link UserMaster}
     * @return {@link String}
     */
	/*
	 * private String getFirstNameOrUserName(UserMaster userMaster) { StringBuilder
	 * stringBuilder = new StringBuilder();
	 * if(StringUtils.isNotBlank(userMaster.getFirstName())) {
	 * stringBuilder.append(userMaster.getFirstName()); } else {
	 * stringBuilder.append(userMaster.getUserName()); } return
	 * stringBuilder.toString(); }
	 */
    
    /**
     * This method is used to send item unavailable Email
     * @param to String
     * @param name String
     */
	/*
	 * public void itemUnAvailableInCart(UserMaster userMaster, Map<String, String>
	 * params){ try {
	 * 
	 * String logoImgTag = getFooterLogo(); String alertUrl=domainUrl+"usr/alert?";
	 * 
	 * params.put("alertUrl", alertUrl);
	 * 
	 * params.put(ORTConstants.MENU_ORTELLIGENCE, ORTConstants.ORTELLIGENCE);
	 * params.put(ORTConstants.LOGO_IMG_TAG, logoImgTag);
	 * 
	 * String template = "alert-item-unavailable";
	 * if(StringUtils.isNotEmpty(template)) { String body =
	 * emailFormatter.formatResourceMessage(template, params); String subject =
	 * ORTConstants.ALERT_ITEM_UNAVAILABLE;
	 * 
	 * for(Map.Entry<String, String> pair : params.entrySet()){ String
	 * key="##"+pair.getKey()+"##"; subject = subject.replace(key,pair.getValue());
	 * }
	 * 
	 * emailService.sendMailWithCcBccAndAttachment(Collections.singletonList(
	 * userMaster.getEmailAddress()), subject, body, null, null, null); }
	 * 
	 * }catch (Exception e){ logger.error(e.getMessage(),e); } }
	 */
    

    /**
     * This method used to send mail to assigned surgeon
     * @param userMaster UserMaster
     * @param caseNo Long
     * @param loginUserName String
     */
	/*
	 * public void caseAssignToSurgeonTemplate(UserMaster userMaster, Long caseNo,
	 * String loginUserName ){ try { String logoImgTag = getFooterLogo();
	 * HashMap<String, String> params = new HashMap<>();
	 * params.put("assignedUserName", userMaster.getDisplayName());
	 * params.put(ORTConstants.USER_NAME, userMaster.getUserName());
	 * params.put(ORTConstants.MENU_ORTELLIGENCE, ORTConstants.ORTELLIGENCE);
	 * params.put(ORTConstants.LOGO_IMG_TAG, logoImgTag);
	 * params.put(ORTConstants.CASE_NO, caseNo.toString());
	 * params.put(ORTConstants.LOGIN_USER_NAME, loginUserName);
	 * 
	 * String template = "case-assign"; String subject = "Case Reassign";
	 * 
	 * if(StringUtils.isNotEmpty(template)) { String body =
	 * emailFormatter.formatResourceMessage(template, params);
	 * emailService.sendMailWithCcBccAndAttachment(Collections.singletonList(
	 * userMaster.getEmailAddress()), subject, body, null, null, null);
	 * logger.info("Surgeon reassign successfully"); }
	 * 
	 * }catch (Exception e){ logger.error(e.getMessage(),e); } }
	 */
	
	/**
	 * This method used to create template for reject case and send mail to previous assigned user 
	 * @param previousAssignedUser UserMaster
	 * @param caseNo Long
	 * @param loginUserName String
	 */
	/*
	 * public void caseRejectBySurgeonTemplate(UserMaster previousAssignedUser, Long
	 * caseNo, String loginUserName ){ try { String logoImgTag = getFooterLogo();
	 * HashMap<String, String> params = new HashMap<>();
	 * params.put("previousAssignedUserName",
	 * previousAssignedUser.getDisplayName());
	 * params.put(ORTConstants.MENU_ORTELLIGENCE, ORTConstants.ORTELLIGENCE);
	 * params.put(ORTConstants.LOGO_IMG_TAG, logoImgTag);
	 * params.put(ORTConstants.CASE_NO, caseNo.toString());
	 * params.put(ORTConstants.LOGIN_USER_NAME, loginUserName);
	 * 
	 * String template = "case-reject"; String subject = "Case Reject";
	 * 
	 * if(StringUtils.isNotEmpty(template)) { String body =
	 * emailFormatter.formatResourceMessage(template, params);
	 * emailService.sendMailWithCcBccAndAttachment(Collections.singletonList(
	 * previousAssignedUser.getEmailAddress()), subject, body, null, null, null);
	 * logger.info("Surgeon rejected successfully"); } }catch (Exception e){
	 * logger.error(e.getMessage(),e); } }
	 * 
	 * public void caseAcceptedBySurgeonTemplate(UserMaster previousAssignedUser,
	 * Long caseNo, String loginUserName ){ try { String logoImgTag =
	 * getFooterLogo(); HashMap<String, String> params = new HashMap<>();
	 * params.put("previousAssignedUserName",
	 * previousAssignedUser.getDisplayName());
	 * params.put(ORTConstants.MENU_ORTELLIGENCE, ORTConstants.ORTELLIGENCE);
	 * params.put(ORTConstants.LOGO_IMG_TAG, logoImgTag);
	 * params.put(ORTConstants.CASE_NO, caseNo.toString());
	 * params.put(ORTConstants.LOGIN_USER_NAME, loginUserName);
	 * 
	 * String template = "case-accept"; String subject = "Case Accept";
	 * 
	 * if(StringUtils.isNotEmpty(template)) { String body =
	 * emailFormatter.formatResourceMessage(template, params);
	 * emailService.sendMailWithCcBccAndAttachment(Collections.singletonList(
	 * previousAssignedUser.getEmailAddress()), subject, body, null, null, null);
	 * logger.info("Surgeon accepted successfully"); } }catch (Exception e){
	 * logger.error(e.getMessage(),e); } }
	 * 
	 * 
	 * public void zoomMeetingEmail(SalesRepresentativeConverter
	 * salesRepresentative, Map<String, String> params, String template, String
	 * subject){ try {
	 * 
	 * String logoImgTag = getFooterLogo();
	 * 
	 * params.put(ORTConstants.MENU_ORTELLIGENCE, ORTConstants.ORTELLIGENCE);
	 * params.put(ORTConstants.LOGO_IMG_TAG, logoImgTag);
	 * 
	 * if(StringUtils.isNotEmpty(template)) { String body =
	 * emailFormatter.formatResourceMessage(template, params);
	 * 
	 * for(Map.Entry<String, String> pair : params.entrySet()){ String
	 * key="##"+pair.getKey()+"##"; subject = subject.replace(key,pair.getValue());
	 * }
	 * 
	 * emailService.sendMailWithCcBccAndAttachment(Collections.singletonList(
	 * salesRepresentative.getEmailAddress()), subject, body, null, null, null); }
	 * 
	 * }catch (Exception e){ logger.error(e.getMessage(),e); } }
	 */
	
	/**
	 * This method is used to send mail when user status change. 
	 * @param emailAddress
	 * @param params
	 * @param template
	 * @param subject
	 */
	/*
	 * public void sendMailUserChangeStatus(String emailAddress, Map<String, String>
	 * params, String template, String subject) { try {
	 * 
	 * String logoImgTag = getFooterLogo();
	 * 
	 * params.put(ORTConstants.MENU_ORTELLIGENCE, ORTConstants.ORTELLIGENCE);
	 * params.put(ORTConstants.LOGO_IMG_TAG, logoImgTag);
	 * 
	 * if (StringUtils.isNotEmpty(template)) { String body =
	 * emailFormatter.formatResourceMessage(template, params);
	 * 
	 * for (Map.Entry<String, String> pair : params.entrySet()) { String key = "##"
	 * + pair.getKey() + "##"; subject = subject.replace(key, pair.getValue()); }
	 * 
	 * emailService.sendMailWithCcBccAndAttachment(Collections.singletonList(
	 * emailAddress), subject, body, null, null, null); }
	 * 
	 * } catch (Exception e) { logger.error(e.getMessage(), e); } }
	 */
    /**
     * This method is used to send authentication OTP
     * @param userMaster
     * @param otp
     */

	/*
	 * public void sendAuthenticationOtp(UserMaster userMaster,String otp){ try {
	 * 
	 * String logoImgTag = getFooterLogo();
	 * 
	 * HashMap<String, String> params = new HashMap<>(); params.put("name",
	 * getFirstNameOrUserName(userMaster)); params.put("otp", otp);
	 * params.put(ORTConstants.MENU_ORTELLIGENCE, ORTConstants.ORTELLIGENCE);
	 * params.put(EMAIL_PROBLEM_TEXT, ORTConstants.EMAIL_PROBLEM_TEXT);
	 * params.put(ORTConstants.LOGO_IMG_TAG, logoImgTag);
	 * 
	 * String template = "authentication-otp"; String subject =
	 * ORTConstants.AUTHENTICATION_OTP;
	 * 
	 * if(StringUtils.isNotEmpty(template)) { String body =
	 * emailFormatter.formatResourceMessage(template, params);
	 * emailService.sendMailWithCcBccAndAttachment(Collections.singletonList(
	 * userMaster.getEmailAddress()), subject, body, null, null, null);
	 * logger.info("Authentication Otp email Send successfully"); }
	 * 
	 * }catch (Exception e){ logger.error(e.getMessage(),e); } }
	 */
	/**
     * This method is used to send password reset reminder with Link
     * @param to String
     * @param key String
     * @param name String
     * @param userId String
     */
	/*
	 * public void sendPasswordReminder(UserMaster userMaster,String authLink,
	 * String days){ try { String activationLink = getPasswordLink(userMaster,
	 * authLink); String logoImgTag = getFooterLogo();
	 * 
	 * HashMap<String, String> params = new HashMap<>(); params.put("name",
	 * getFirstNameOrUserName(userMaster)); params.put("link", activationLink);
	 * params.put(DOMAIN_URL, domainUrl); params.put("days", days);
	 * params.put(ORTConstants.USER_NAME, userMaster.getUserName());
	 * params.put(ORTConstants.MENU_ORTELLIGENCE, ORTConstants.ORTELLIGENCE);
	 * params.put(EMAIL_PROBLEM_TEXT, ORTConstants.EMAIL_PROBLEM_TEXT);
	 * params.put(ORTConstants.LOGO_IMG_TAG, logoImgTag);
	 * 
	 * String template = null; String subject = null; template =
	 * "reset-password-reminder"; subject =
	 * ORTConstants.ORTELLIGENCE_RESET_REMINDER;
	 * 
	 * if(StringUtils.isNotEmpty(template)) { String body =
	 * emailFormatter.formatResourceMessage(template, params);
	 * emailService.sendMailWithCcBccAndAttachment(Collections.singletonList(
	 * userMaster.getEmailAddress()), subject, body, null, null, null); } }catch
	 * (Exception e){ logger.error(e.getMessage(),e); } }
	 */
    
}
