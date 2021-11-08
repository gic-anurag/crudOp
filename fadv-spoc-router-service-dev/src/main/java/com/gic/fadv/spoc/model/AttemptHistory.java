package com.gic.fadv.spoc.model;

import java.util.Date;

import lombok.Data;

@Data
public class AttemptHistory {

	private Long attemptid;
	private Long requestid;
	private String checkid;
	private String name;
	private String jobTitle;
	private String emailAddress;
	private String faxNumber;
	private String contactDate;
	private String followupDate;
	private Long attemptStatusid;
	private String numberProvided;
	private String attemptDescription;
	private String closureExpectedDate;
	private String userid;
	private Date createDate = new Date();
	private String sourcePhone;
	private Integer isCurrent;
	private String contactCardName;
	private Long followupId;
	private Long refAttemptId;
}
