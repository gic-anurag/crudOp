package com.gic.fadv.stellar.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Result {
	
	 @JsonProperty("ComponentScoping") 
	 public ComponentScoping componentScoping;
}
