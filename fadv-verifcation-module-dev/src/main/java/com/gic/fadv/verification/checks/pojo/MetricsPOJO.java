package com.gic.fadv.verification.checks.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class MetricsPOJO {
	public String startTime;
	public String endTime;
	public long timeInMills;
	public int timeInSeconds;
	public String statusCode;
}
