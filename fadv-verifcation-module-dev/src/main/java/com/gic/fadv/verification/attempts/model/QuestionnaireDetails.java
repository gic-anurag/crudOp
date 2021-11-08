package com.gic.fadv.verification.attempts.model;

import lombok.Data;

@Data
public class QuestionnaireDetails {
	
	private String id;
	private String globalQuestionId;
	private String questionName;
	private String questionScope;
	private String questionType;
	private String componentName;
	private String productName;
    private Boolean mandatory;

}
