package com.gic.fadv.online.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.fadv.online.model.OnlineReq;
import com.gic.fadv.online.model.OnlineVerificationChecks;
import com.gic.fadv.online.pojo.ApiServiceResultPOJO;

@Service
public interface OnlineService {

	Map<String, String> getDetailsFromDataEntry(OnlineReq onlineReq);
	String getSecondaryName(String primaryName, String passportName, String panName, String dlName, String voterIdName);
	void callBack(String postStr);
	ApiServiceResultPOJO getApiServiceNames(ObjectMapper mapper, OnlineReq onlineReq) throws JsonProcessingException;
	ApiServiceResultPOJO processApiService(ObjectMapper mapper, String serviceName, Map<String, String> resultMap,
			ApiServiceResultPOJO apiServiceResultPOJO);
	JsonNode writeRecordResultMap(ObjectMapper mapper, OnlineReq onlineReq, ApiServiceResultPOJO apiServiceResultPOJO);
	List<String> processScheduledApiService(ObjectMapper mapper, OnlineVerificationChecks onlineVerificationChecks,
			List<String> checkIdList) throws JsonMappingException, JsonProcessingException;
	void runParallelService(List<OnlineVerificationChecks> onlineVerificationChecksList) throws InterruptedException, ExecutionException;
}
