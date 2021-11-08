package com.fadv.cspi.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class CaseSearchCriteriaPOJO {
	private Date startDate;
	private Date endDate;
	private String crNo;
	private String caseNo;
	private String caseCreationStatus;
	private String clientName;
}
