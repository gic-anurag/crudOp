package com.gic.fadv.online.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public interface OnlinePreProcessService {

	ObjectNode processRequestBody(JsonNode recordNode);
}
