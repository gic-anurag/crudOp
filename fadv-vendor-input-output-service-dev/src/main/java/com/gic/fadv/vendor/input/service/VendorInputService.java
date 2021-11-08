package com.gic.fadv.vendor.input.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public interface VendorInputService {

	ObjectNode processRequestBody(JsonNode requestNode);

}
