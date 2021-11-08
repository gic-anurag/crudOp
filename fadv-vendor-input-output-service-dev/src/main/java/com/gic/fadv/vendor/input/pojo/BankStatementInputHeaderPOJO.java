package com.gic.fadv.vendor.input.pojo;

import lombok.Data;

@Data
public class BankStatementInputHeaderPOJO {

	private String insID;
	private String process;
	private String cSPIStatus;
	private String clientName;
	private String code;
	private String caseRefNumber;
	private String cRMName;
	private String candidateCompleteName;
	private String dateofBirth;
	/*
	 * Bank Statement Fields
	 */
	private String accountNumber;
	private String bankName;
	private String pOSFrom;
	private String pOSTO;
	private String city;
	private String state;
	private String initiationDate;
	private String initiationBy;
	private String initiationStatus;
	private String initiationRemarks;
	private String singleDouble;
}
