package com.gic.fadv.wellknown.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public interface WellknownService {

	ObjectNode processRequestBody(JsonNode requestNode);

}
