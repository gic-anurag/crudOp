package com.gic.fadv.online.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.online.model.PersonInfoSearch;

@Service
public interface OnlineAPIParsingService {
	
	void parseWorldCheckResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,String worldCheckResponse, String din, ObjectNode resultWorldCheck);
	void parseManupatraResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch, String manuPatraResponse,ObjectNode resultManupatra);
	void parseWatchoutResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,String fulfillmentResponse, String din, ObjectNode resultWatchOut);
	void parseLoanDefaulterResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,String personalResponse, ObjectNode resultLoan);
	void parseAdverseMediaResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,String personalResponse, ObjectNode resultAdverseMedia);
	String parseMCAResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch, String personalResponse,ObjectNode resultMCA);
}
