package com.gic.fadv.verification.mapping.pojo;

import lombok.Data;

@Data
public class VendorQuestionnairePOJO {
	
	private Long questionnaireMappingId;
	
	private String componentName;
	
	private String productName;

	private String globalQuestionId;

	private String globalQuestion;

	private String questioneScope;
	
	private String reportComments;
	
	private String status;
	
	private String verifiedData;
	
	private String type;
	
	private Long id;
}
