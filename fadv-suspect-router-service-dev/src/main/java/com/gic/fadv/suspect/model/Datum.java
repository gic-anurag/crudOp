package com.gic.fadv.suspect.model;

import lombok.Data;

@Data
public class Datum {

	public String taskName;
	public String taskId;
	public String taskBy;
	public int taskSerialNo;
	public TaskSpecs taskSpecs;
}
