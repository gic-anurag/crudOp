package com.gic.fadv.verification.mapping.pojo;

import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class QuestionaireMappingRequest {
	
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
	

}
