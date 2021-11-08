package com.gic.fadv.verification.mapping.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;

@Entity
@Table(name = "db_questionaire_mapping")
@Data
public class DbQuestionaireMapping implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1395250721580335786L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	@CsvBindByName(column = "Component Name")
	private String componentName;

	@CsvBindByName(column = "Product Name")
	@Column(columnDefinition = "text")
	private String productName;

	@CsvBindByName(column = "GLOBAL_QUESTION_ID")
	private String globalQuestionId;

	@CsvBindByName(column = "GLOBAL_QUESTION")
	@Column(columnDefinition = "text")
	private String globalQuestion;

	@CsvBindByName(column = "QUESTIONE_TYPE")
	private String questioneType;

	@CsvBindByName(column = "PACKAGE_QUESTION")
	@Column(columnDefinition = "text")
	private String packageQuestion;

	@CsvBindByName(column = "REPORT_LABEL")
	@Column(columnDefinition = "text")
	private String reportLabel;

	@CsvBindByName(column = "PRODUCT_QUESTION_ID")
	private String productQuestionId;

	@CsvBindByName(column = "PRODUCT_ID")
	private String productId;

	@CsvBindByName(column = "COMPONENT_ID")
	private String componentId;

	@CsvBindByName(column = "PQ_PRECEDENCE")
	private String pqPrecedence;

	@CsvBindByName(column = "IS_REQUIRED")
	private String isRequired;

	@CsvBindByName(column = "IS_REPORT_REQUIRED")
	private String isReportRequired;

	@CsvBindByName(column = "QUESTIONE_SCOPE")
	@Column(columnDefinition = "text")
	private String questioneScope;

	@CsvBindByName(column = "PKG_COMP_PROD_QSTN_ID")
	@Column(columnDefinition = "text")
	private String pkgCompProdQstnId;

	@CsvBindByName(column = "PACKAGE_COMP_PRODUCT_ID")
	@Column(columnDefinition = "text")
	private String packageCompProductId;

	@CsvBindByName(column = "ENTITY_NAME")
	@Column(columnDefinition = "text")
	private String entityName;

	@CsvBindByName(column = "Tagging Type")
	@Column(columnDefinition = "text")
	private String taggingType;

	@CsvBindByName(column = "Remarks")
	private String remarks;

	@CsvBindByName(column = "Status")
	private String status;

	@CsvBindByName(column = "Verified Data")
	private String verifiedData;

	@CsvBindByName(column = "Report Comments")
	private String reportComments;

	private Date createdDateTime;

}
