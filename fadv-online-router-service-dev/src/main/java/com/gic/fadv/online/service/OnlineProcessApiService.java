package com.gic.fadv.online.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public interface OnlineProcessApiService {

	void processApiService(ObjectMapper mapper, String serviceName, Map<String, String> serviceResponseMap,
			JsonNode dataEntryNode);

}
