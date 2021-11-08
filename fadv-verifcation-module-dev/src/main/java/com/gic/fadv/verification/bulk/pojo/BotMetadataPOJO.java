package com.gic.fadv.verification.bulk.pojo;

import lombok.Data;

@Data
public class BotMetadataPOJO {
	private String requestType;

    private String txLabel;

    private String version;

    private String attempt;

    private String requestAuthToken;

    private String multiTask;

    private String task;

    private String taskGroupId;

    private String stageName;

    private String processName;

    private String processId;

    private String requestId;

    private String requestDate;

    private String stageId;
}
