package com.gic.fadv.online.pojo;

import java.util.List;

import com.gic.fadv.online.model.Component;
import com.gic.fadv.online.model.ComponentScoping;

import lombok.Data;

@Data
public class OnlineAttemptsPOJO {
	private ComponentScoping componentScoping;
	private Component component;
	private String componentName;
	private String productName;
	private String caseNo;
	private CaseSpecificInfoPOJO caseSpecificInfoPOJO;
	private Long caseSpecificInfoId;
	private Long caseSpecificRecordId;
	private String apiName;
	
	private List<String> checkIdList;
	
}
