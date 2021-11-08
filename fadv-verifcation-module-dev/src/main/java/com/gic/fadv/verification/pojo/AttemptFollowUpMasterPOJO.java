package com.gic.fadv.verification.pojo;

import lombok.Data;

@Data
public class AttemptFollowUpMasterPOJO {

	private Long followupId;
	private String followupStatus;
	private String followupDescription;
	private String actionType;
	private String relationToCspi;
	private String checkFlow;
	private String comments;
	private String userid;
}
