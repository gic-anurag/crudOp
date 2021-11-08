package com.gic.fadv.verification.attempts.pojo;

import lombok.Data;

@Data
public class QuestionnairePOJO {
	 	private String id;
	 	private String questionName;	
		private String globalQuestionId;
	    private String questionScope;
	    private String questionType;
	    private String componentName;
	    private String productName;
	    private String formLabel;
	    private String answere;
	    private Boolean mandatory;
}
