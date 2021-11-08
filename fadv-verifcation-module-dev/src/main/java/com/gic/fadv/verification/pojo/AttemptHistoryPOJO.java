package com.gic.fadv.verification.pojo;

import lombok.Data;

@Data
public class AttemptHistoryPOJO {

	private Long attemptid;
	private String name;
	private String jobTitle;
	private String sourcePhone;
	private String contactDate;
	private String followupDate;
	private String numberProvided;
	private String closureExpectedDate;
	private String attemptDescription;
	private String faxNumber;
	private String emailAddress;
	private String attemptStatus;
	private String attemptType;
	private Long followupId;
	private String checkid;
}
