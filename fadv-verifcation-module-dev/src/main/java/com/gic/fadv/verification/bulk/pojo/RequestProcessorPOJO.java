package com.gic.fadv.verification.bulk.pojo;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class RequestProcessorPOJO {

	private Long requestProcessorId;
	
	private String requestType;

	private JsonNode requestJson;
	
	private JsonNode responseJson;

}
