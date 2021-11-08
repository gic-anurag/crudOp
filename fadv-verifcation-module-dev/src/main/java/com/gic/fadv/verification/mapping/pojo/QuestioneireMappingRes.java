package com.gic.fadv.verification.mapping.pojo;

import java.util.Date;

public interface QuestioneireMappingRes {
	
	 String getGlobalQuestionId();
	 Long getId();
	 String getComponent();
	 String getProductName();
	 String getGlobalQuestion();
	 String getQuestioneType();
	 String getFormLabel();
	 String getReportLabel();
	 String getPqPrecedence();
	 String getMandatory();
	 String getInputType();
	 String getEntityName();
	 String getQuestioneScope();
	 Date getCreatedDateTime();
	 String getFieldMapping();
	 String getStatus();
	 String getVerifiedData();

}
