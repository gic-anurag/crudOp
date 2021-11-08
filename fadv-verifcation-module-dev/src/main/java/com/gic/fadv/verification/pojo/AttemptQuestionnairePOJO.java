package com.gic.fadv.verification.pojo;

import java.util.Date;

public interface AttemptQuestionnairePOJO {
	
	Long getFollowupId();
	String getCheckid();
	String getFollowupDescription();
	String getFollowupStatus();
	
/*<<<<<<< HEAD
	private Long followupId;
	private Long checkid;
	private String followupDescription;
	private String followupStatus;
	private AttemptQuestionnaire attemptQuestionnaire;
	//private CaseSpecificRecordDetail caseSpecificRecordDetail;
	
	private long caseSpecificDetailId;
	private long instructionCheckId;
	private String componentName;
	private String product;
	//private String componentRecordField;
	private Date checkCreatedDate;
	private String checkStatus;
	private long caseSpecificId;
=======*/
	Long getAttemptQuestionnaireId();
	String getQuestionName();
	String getGlobalQuestionId();
	String getApplicationData();
	String getStatus();
	String getAdjudication();
	String getVerifiedData();
	String getReportComments();
	Long getUserid();
	Date getCreateDate();
	
	Long getCaseSpecificDetailId();
	String getInstructionCheckId();
	String getComponentName();
	String getProduct();
    String getComponentRecordField();
	Date getCheckCreatedDate();
	String getCheckStatus();
	Long getCaseSpecificId();
	/* >>>>>>> c60d42d9febefc1fdf444fd53df4c78f735740db */
	
	Date getCheckDueDate();
	String getFunctionalEntityName();
	String getEntityLocation();
	String getCheckTat();
}
