package com.gic.fadv.stellar.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class DatumRes {
	
	    public String taskName;
	    public String taskId;
	    public String taskBy;
	    public int taskSerialNo;
	    @JsonProperty("result")
	    public Result result;
}
