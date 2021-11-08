package com.gic.fadv.verification.online.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Service
public interface OnlineManualVerificationService {

	List<String> getMrlDocumentNames(ObjectMapper mapper, String componentName, String akaName);

	ArrayNode getFilesFromAssociatePath(JsonNode requestBody);

}
