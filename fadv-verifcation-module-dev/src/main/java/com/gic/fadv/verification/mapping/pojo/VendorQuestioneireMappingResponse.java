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
public class VendorQuestioneireMappingResponse {
	
	private Long id;
	private String componentName;
	private String productName;
	private String globalQuestionId;
	private String globalQuestion;
	private String questioneType;
	private String reportLabel;
	private String pqPrecedence;
	private String entityName;
	private String questioneScope;
	
	private String status;
	private Date createdDateTime;
	
	private String fieldMapping;

}
