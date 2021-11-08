package com.gic.fadv.verification.online.pojo;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class OnlineAttemptHistoryPOJO {

	private Long onlineManualVerifiationId;
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
	private Long userid;
	private String sourcePhone;
	private Integer isCurrent;
	private String contactCardName;
	private Long followupId;
	private Long refAttemptId;
	private String executiveSummary;
	private String additionalFieldsTag;
	private JsonNode additionalFields;
	private List<String> checkIds;
}
