package com.gic.fadv.verification.event.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class VerificationEventStatus {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long verificationEventStatusId;
	private Long requestId;//Take from Case_specific_record_details
	private String caseNo;
	private String checkId;
	private String eventName;
	private String eventType;
	private String status;
	private Date createdDateTime=new Date();
	@Column(name="user_id")
	private String user;//For Auto is System otherwise userid
}
