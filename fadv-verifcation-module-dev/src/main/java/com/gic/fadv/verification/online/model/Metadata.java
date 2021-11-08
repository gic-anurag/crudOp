package com.gic.fadv.verification.online.model;

import lombok.Data;

@Data
public class Metadata {
	private String requestType;
	private String version;
	private String attempt;
	private String requestAuthToken;
	private String taskDesc;
	private String multiTask;
	private String task;
	private String taskGroupId;
	private String processName;
	private String processId;
	private String requestId;
	private String requestDate;
	private String stageId;
}
