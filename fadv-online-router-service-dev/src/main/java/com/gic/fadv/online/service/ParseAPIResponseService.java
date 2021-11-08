package com.gic.fadv.online.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.online.model.PersonInfoSearch;

@Service
public interface ParseAPIResponseService {
	
	ObjectNode parseAdverseMediaResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,
			String personalResponse, ObjectNode resultAdverseMedia);
	ObjectNode parseLoanDefaulterResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,
			String personalResponse, ObjectNode resultLoan);
	ObjectNode parseManupatraResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch, String manuPatraResponse,
			ObjectNode resultManupatra);
	String parseMCAResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch, String personalResponse,
			ObjectNode resultMCA);
	ObjectNode parseWatchoutResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,
			String fulfillmentResponse, String din, ObjectNode resultWatchOut);
	ObjectNode parseWorldCheckResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,
			String worldCheckResponse, String din, ObjectNode resultWorldCheck);
	
}
