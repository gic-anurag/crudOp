package com.gic.fadv.verification.checks.pojo;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DatumPOJO {
	public String taskName;
	public String taskId;
	public int taskSerialNo;
	public ResultPOJO result;
	public MetricsPOJO metrics;
	public JsonNode logs;
	public boolean conditional;
}
