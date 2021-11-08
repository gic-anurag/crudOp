package com.fadv.cspi.remote.pojo;

import java.util.List;

import lombok.Data;

@Data
public class MrlRulePOJO {

	private String akaName;

	private String componentName;

	private List<String> documentNames;
}
