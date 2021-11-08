package com.gic.fadv.verification.checks.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class StatusPOJO {
	public boolean success;
	public String message;
	public String statusCode;
}
