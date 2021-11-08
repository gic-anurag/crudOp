package com.gic.fadv.verification.checks.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class MetadataPOJO {
	public String botId;
	public String responseId;
	public String responseType;
	public String responseTime;
	public StatusPOJO status;
	public String processName;
	public String processId;
	public String stageId;
	public String task;
	public String taskGroupId;
	public String requestDate;
	public String requestType;
	public String requestId;
	public String version;
	public int attempt;
	public String multiTask;
	public String requestAuthToken;
}
