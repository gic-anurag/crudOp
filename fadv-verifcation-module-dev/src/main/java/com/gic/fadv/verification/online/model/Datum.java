package com.gic.fadv.verification.online.model;

import lombok.Data;

@Data
public class Datum {
	private String taskSerialNo;
	private String taskName;
	private String taskId;
	private TaskSpecs taskSpecs;
}