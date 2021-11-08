package com.gic.fadv.cbvutvi4v.pojo;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;


@Data
public class VerificationEventStatusPOJO {
	
	private Long verificationEventStatusId;
	private Long requestId;//Take from Case_specific_record_details
	private String caseNo;
	private String checkId;
	private String eventName;
	private String eventType;
	private String status;
	private Date createdDateTime=new Date();
	private String user;//For Auto is System otherwise userid
}
