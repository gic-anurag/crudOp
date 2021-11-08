package com.gic.fadv.suspect.model;

import lombok.Data;

@Data
public class DatumRes {

	public String taskName;
	public String taskId;
	public String taskBy;
	public int taskSerialNo;
	public Result result;
}
