package com.gic.fadv.online.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public interface OnlineFinalService {
	
	ObjectNode setManupatraResponse(ObjectMapper mapper, JsonNode apiResponseNode);

	ObjectNode setLoanDefaulterResponse(ObjectMapper mapper, JsonNode apiResponseNode);

	ObjectNode setAdverseMediaResponse(ObjectMapper mapper, JsonNode apiResponseNode);

	ObjectNode setWorldCheckResponse(ObjectMapper mapper, JsonNode apiResponseNode);

	ObjectNode setWatchoutResponse(ObjectMapper mapper, JsonNode apiResponseNode);

	ObjectNode setMcaResponse(ObjectMapper mapper, JsonNode apiResponseNode);

	String getManupatraFinalStatus(String primaryStatus, String secondaryStatus);

}
