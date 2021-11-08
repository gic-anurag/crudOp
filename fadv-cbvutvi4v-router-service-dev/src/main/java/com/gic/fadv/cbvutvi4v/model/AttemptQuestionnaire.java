package com.gic.fadv.cbvutvi4v.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

//@Entity
@Data
public class AttemptQuestionnaire {
	/*
	 * @Id
	 * 
	 * @GeneratedValue(strategy = GenerationType.IDENTITY) private long
	 * attemptQuestionnaireId;
	 */
	private Long checkId;
	private String question;	
	private String globalQuestionId;
	private String applicationData;	
	private String status;	
	private String adjudication;	
	private String verifiedData;	
	private String reportComments;
	private Long userid;
    private Date createDate=new Date();
}
