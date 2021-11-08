package com.gic.fadv.cbvutvi4v.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class CaseSpecificRecordDetailPOJO {

	private long caseSpecificDetailId;
	private String instructionCheckId;
	private String componentName;
	private String product;
	private String componentRecordField;
	private Date checkCreatedDate = new Date();
	private String checkStatus;
	private long caseSpecificId;
	
	private Date checkDueDate;
	private String functionalEntityName;
	private String entityLocation;
	private String checkTat;
	private String caseSpecificRecordStatus;
}