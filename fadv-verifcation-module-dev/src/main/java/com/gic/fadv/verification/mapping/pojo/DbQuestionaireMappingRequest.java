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
public class DbQuestionaireMappingRequest {

	private Long id;
	private String componentName;
	private String productName;
	private String globalQuestionId;
	private String globalQuestion;
	private String questioneType;
	private String packageQuestion;
	private String reportLabel;
	private String productQuestionId;
	private String productId;
	private String componentId;
	private String pqPrecedence;
	private String isRequired;
	private String isReportRequired;
	private String questioneScope;
	private String pkgCompProdQstnId;
	private String packageCompProductId;
	private String entityName;
	private String taggingType;
	private String remarks;
	private String status;
	private String verifiedData;
	private String reportComments;

	private Date createdDateTime;

}
