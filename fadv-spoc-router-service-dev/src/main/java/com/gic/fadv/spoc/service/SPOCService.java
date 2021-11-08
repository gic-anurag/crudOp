package com.gic.fadv.spoc.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public interface SPOCService {
	
	ObjectNode processRequestBody(JsonNode requestNode) throws JsonMappingException, JsonProcessingException;

}
