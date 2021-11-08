package com.gic.fadv.spoc.pojo;

import lombok.Data;

@Data
public class PackageQuestionnaireExtractPOJO {
	
	private Long qbId;

	private String id;
	private String questionName;
	private String questionScope;
	private String categoryName;
	private String categoryId;
	private String componentId;
	private String componentName;
	private String productId;
	private String productName;
	private String clientId;
	private String clientCode;
	private String clientName;
	private String sbuId;
	private String sbuName;
	private String packageId;
	private String packageName;
	private String formLabel;
	private String reportLabel;
	private String packageQuestion;
	private String ngDocumentId;
	private String ngDocumentName;
	private String ngDocumentFieldId;
	private String ngDocumentField;
	private String deType;
	private String questionType;
	private Integer sequenceId;
	private Boolean isMandatory;
	private Integer globalQuestionId;
	private String priorityId;
	private Boolean docPresent;
	private Boolean reportRequired;
}
