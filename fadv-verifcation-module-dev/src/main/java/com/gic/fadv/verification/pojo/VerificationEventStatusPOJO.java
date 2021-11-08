package com.gic.fadv.verification.pojo;

import java.util.Date;


import lombok.Data;


@Data
public class VerificationEventStatusPOJO {
	
	private Long verificationEventStatusId;
	private String checkId;
	private String eventName;
	private String eventType;
	private String status;
	private Date createdDateTime=new Date();

}
