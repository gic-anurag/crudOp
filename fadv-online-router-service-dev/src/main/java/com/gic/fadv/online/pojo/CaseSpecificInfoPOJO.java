package com.gic.fadv.online.pojo;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;


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
	private String status;//Set I for Online 
	private List<CaseSpecificRecordDetailPOJO> caseSpecificRecordDetail;
}
