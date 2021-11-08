package com.gic.fadv.online.pojo;

import java.util.List;
import java.util.Map;

import com.gic.fadv.online.model.OnlineVerificationChecks;

import lombok.Data;

@Data
public class ApiServiceResultPOJO {

	private List<String> serviceNameList;
	private Map<String, String> resultServiceMap;
	private Long caseSpecificId;
	private Long caseSpecificRecordId;
	private List<String> checkIdList;
	List<OnlineVerificationChecks> onlineVerificationChecksList;
	private String productName;
	private String componentName;
	private String apiName;
	private String caseNumber;
	private String manuPatraResponsePrimary;
	private String manuPatraResponseSecondary;
	private Map<String, String> serviceResponseMap;
	private Map<String, String> dataEntryMap;
}
