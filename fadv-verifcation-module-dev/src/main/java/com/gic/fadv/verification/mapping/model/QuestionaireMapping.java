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
@Table(name="questionaire_mapping")
@Data
public class QuestionaireMapping implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6048787238716142396L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id") 
	private Long id;
	@CsvBindByName(column = "Component")
	private String component;
	
	@CsvBindByName(column = "Product Name")
	@Column(columnDefinition="text")
	private String productName;
	
	@CsvBindByName(column = "GLOBAL_QUESTION_ID")
	private String globalQuestionId;
	
	@CsvBindByName(column = "GLOBAL_QUESTION")
	@Column(columnDefinition="text")
	private String globalQuestion;
	
	@CsvBindByName(column = "QUESTIONE_TYPE")
	private String questioneType;
	
	@CsvBindByName(column = "FORM_LABEL")
	@Column(columnDefinition="text")
	private String formLabel;
	
	@CsvBindByName(column = "REPORT_LABEL")
	@Column(columnDefinition="text")
	private String reportLabel;
	
	@CsvBindByName(column = "PQ_PRECEDENCE")
	private String pqPrecedence;
	
	@CsvBindByName(column = "Mandatory")
	private String mandatory;
	
	@CsvBindByName(column = "Input Type")
	private String inputType;
	
	@CsvBindByName(column = "ENTITY_NAME")
	private String entityName;
	
	@CsvBindByName(column = "QUESTIONE_SCOPE")
	private String questioneScope;
	
	private String status;
	private Date createdDateTime;
	
	private String type;
	
}
