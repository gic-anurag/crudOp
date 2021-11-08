package com.gic.fadv.spoc.pojo;

import java.util.List;

import lombok.Data;

@Data
public class CaseSpecificInfoPOJO {
	private long caseSpecificId;
	private String caseReference;
	private String caseMoreInfo;
	private String caseDetails;
	private String clientSpecificFields;
	private String caseNumber;
	private String caseRefNumber;
	private String clientCode;
	private String clientName;
	private String sbuName;
	private String packageName;
	private String candidateName;
	private List<CaseSpecificRecordDetailPOJO> caseSpecificRecordDetail;
	private String dataEntryInfo;
}
