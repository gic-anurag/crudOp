package com.gic.fadv.verification.mapping.pojo;

import java.util.Date;

public interface OnlineQuestioneireMappingRes {
	
	
	 String getGlobalQuestionId();
	 Long getId();
	 String getComponentName();
	 String getProductName();
	 String getGlobalQuestion();
	 String getQuestioneType();
	 String getReportLabel();
	 String getPqPrecedence();
	 String getEntityName();
	 String getQuestioneScope();
	 String getStatus();
	 Date getCreatedDateTime();
	 String getFieldMapping();
}
