package com.gic.fadv.verification.pojo;

import lombok.Data;

@Data
public class CaseSpecificInfoPOJO {
	private Long requestId;
	private String caseReferenceNumber;
	private String checkId;
	private String clientName;
	private String componentName;
	private String productName;
	private String candidateName;
	private String functionalEntityName;
	private String entityLocation;
	private String statusofCheck;
	private String checkCreatedDate;
	private String checkDueDate;
	private String checkTAT;
	/* Added Sbu and PackageName for Searching the Questionnaire */
	private String sbuName;
	private String packageName;
	private String akaName;
	private String caseNumber;
}
