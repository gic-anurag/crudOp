package com.gic.fadv.suspect.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface SuspectService {

	ObjectNode processRequestBody(JsonNode requestNode);
	
}
