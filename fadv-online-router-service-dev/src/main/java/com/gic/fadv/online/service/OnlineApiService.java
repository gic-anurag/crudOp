package com.gic.fadv.online.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface OnlineApiService {
	String sendDataToCompanyRest(String jsonData);
	String sendDataToPersonalRest(String jsonData); 
	String sendDataTofulfillmentRest(String jsonData); 
	String sendDataToWorldCheckRest(String jsonData);
	String sendDataToManupatraRest(String jsonData);
	String sendDataToComponentWiseAPIRest();
	//New Changes
	String sendDataToServicePersonalRest(String jsonData);
	String sendDataToFinalPersonalRest(String jsonData);
	String sendDataToWatchOutRest(String jsonData);
	String sendDataToPersonalVerifyRest(String jsonData);
	//Single for OnlineRouter
	String sendDataToRestUrl(String url,String jsonData);
	String sendDataToAttempt(String requestUrl, String jsonData);
	String sendDataToVerificationEventStatus(String requestUrl, String jsonData);
	String sendDataToCaseSpecificRecord(String caseSpecificInfoStr);
	String sendDataToRest(String url, String jsonData, Map<String, String> headerMap);
	String sendDataTogetCheckId(String url, String checkId);
	String sendDataToL3ByCheckId(String requestUrl, List<String> checkIdList);
	String getRequestFromL3(String url);
	String sendDataToLocalRest(String requestUrl, String requestBody);
	String getDataFromLocalRest(String requestUrl);
}
