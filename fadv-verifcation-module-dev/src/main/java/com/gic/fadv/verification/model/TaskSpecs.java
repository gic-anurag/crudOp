package com.gic.fadv.verification.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TaskSpecs {
	
	 @JsonProperty("ComponentScoping") 
	 public List<ComponentScoping> componentScoping;
}
