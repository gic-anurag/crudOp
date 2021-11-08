package com.fadv.cspi.remote.pojo;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class SlaSearchPOJO {
	@NotEmpty
	private String clientCode;
	@NotEmpty
	private String sbu;
	@NotEmpty
	private String packageCode;
	@NotEmpty
	private String componentName;
	@NotEmpty
	private String subComponentName;
}
