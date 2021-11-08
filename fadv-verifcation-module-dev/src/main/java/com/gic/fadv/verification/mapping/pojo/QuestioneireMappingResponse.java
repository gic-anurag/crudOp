package com.gic.fadv.verification.mapping.pojo;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class QuestioneireMappingResponse {

	private Long id;
	private String component;
	private String productName;
	private String globalQuestionId;
	private String globalQuestion;
	private String questioneType;
	private String formLabel;
	private String reportLabel;
	private String pqPrecedence;
	private String mandatory;
	private String inputType;
	private String entityName;
	private String questioneScope;
	
	private String status;
	private Date createdDateTime;
	
	private String fieldMapping;
	
	
	public QuestioneireMappingResponse(String globalQuestionId, Long id, String component, String productName,
			String globalQuestion, String questioneType, String formLabel, String reportLabel, String pqPrecedence,
			String mandatory, String inputType, String entityName, String questioneScope, String status,
			Date createdDateTime, String fieldMapping) {
		super();
		this.globalQuestionId = globalQuestionId;
		this.id = id;
		this.component = component;
		this.productName = productName;
		this.globalQuestion = globalQuestion;
		this.questioneType = questioneType;
		this.formLabel = formLabel;
		this.reportLabel = reportLabel;
		this.pqPrecedence = pqPrecedence;
		this.mandatory = mandatory;
		this.inputType = inputType;
		this.entityName = entityName;
		this.questioneScope = questioneScope;
		this.status = status;
		this.createdDateTime = createdDateTime;
		this.fieldMapping = fieldMapping;
	}

}
