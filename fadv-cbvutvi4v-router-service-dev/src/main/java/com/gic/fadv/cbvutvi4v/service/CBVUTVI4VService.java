package com.gic.fadv.cbvutvi4v.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public interface CBVUTVI4VService {
	
	ObjectNode processRequestBody(JsonNode requestNode);

}
