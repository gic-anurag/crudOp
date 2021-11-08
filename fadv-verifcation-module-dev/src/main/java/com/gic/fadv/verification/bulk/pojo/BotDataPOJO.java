package com.gic.fadv.verification.bulk.pojo;

import lombok.Data;

@Data
public class BotDataPOJO {
	
	private String taskBy;

    private String taskSerialNo;

    private String taskName;

    private String taskId;

    private BotTaskSpecsPOJO taskSpecs;

}
