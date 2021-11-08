package com.gic.fadv.verification.checks.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CheckPOJO {
	public String requestId;
	public String checkID;
	public String productName;
	public String akaName;
	public String componentName;
}
