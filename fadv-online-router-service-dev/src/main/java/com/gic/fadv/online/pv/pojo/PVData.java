package com.gic.fadv.online.pv.pojo;

import lombok.Data;
@Data
public class PVData
{
    private Result result;

    private String taskSerialNo;

    private String taskName;

    private Metrics metrics;

    private Logs logs;

    private String taskId;

}
