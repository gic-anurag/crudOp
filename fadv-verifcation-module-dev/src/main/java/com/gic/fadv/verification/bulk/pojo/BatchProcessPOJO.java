package com.gic.fadv.verification.bulk.pojo;

import lombok.Data;

@Data
public class BatchProcessPOJO {
	private String checkId;
	private String componentName;
	private String endStatusOfTheVerification;
	private String modeOfInitiation;
	private String actionCode;
	private String resultCode;
	private String internalNotes;
	private String followUpDateAndTime;
	private String expectedClosureDate;
	private String isThisAVerificationAttempt;
	private String attempts;
	private String dateVerificationCompleted;
	private String executiveSummaryComments;
	private String reportComments;
	private String writtenVerification;
}
