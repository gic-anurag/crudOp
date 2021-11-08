package com.gic.fadv.online.pv.pojo;
import lombok.Data;
@Data
public class Metadata
{
    private String requestType;

    private String responseTime;

    private String version;

    private String attempt;

    private String requestAuthToken;

    private String multiTask;

    private String responseType;

    private String task;

    private String taskGroupId;

    private String processName;

    private String processId;

    private String requestId;

    private String requestDate;

    private String botId;

    private String stageId;

    private String responseId;

    private Status status;

}
