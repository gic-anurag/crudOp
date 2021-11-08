package com.fadv.cspi.remote.pojo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class RuleDescriptionPOJO {
	private String ruleNumber;
	private String ruleName;
	private String ruleDescription;
	private String componentName;
	private String akaName;
	private String clientCode;
	private String sbu;
	private String packageCode;
	private String subComponentName;
	private List<String> ducumentNames;
	private String description;
	private Date timestamp;
}
