package com.gic.fadv.verification.attempts.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class AttemptQuestionnaire {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long attemptQuestionnaireId;
	private String checkId;
	private String questionName;	
	private String globalQuestionId;
	private String applicationData;	
	private String status;	
	private String adjudication;	
	private String verifiedData;	
	private String reportComments;
	private Long userid;
    private Date createDate=new Date();
    private String id;
    private String questionScope;
    private String questionType;
    private String componentName;
    private String productName;
    private String formLabel;
    private String answere;
    private Boolean mandatory;
    private String type;
}
