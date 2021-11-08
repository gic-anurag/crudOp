package com.gic.fadv.spoc.service;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface SPOCApiService {
	
	String sendDataToSPOCListRest(String jsonData);
	String sendDataToEmailConfig(String jsonData);
	String sendDataToEmailTemplate(String jsonData); 
	String sendDataToFindEmailTemplateNumber(String jsonData);
	String sendDataToExcelTemplate(String jsonData); 
	//Save Data At Verification Engine
	String sendDataToAttemptHistory(String jsonData);
	String sendDataToAttempt(String requestUrl, String jsonData);
	String sendDataToCaseSpecificInfo(String caseSpecificInfoStr);
	String sendDataToVerificationEventStatus(String requestUrl, String jsonData);
	String sendDataToCaseSpecificRecord(String caseSpecificInfoStr);
	String sendDataTogetCheckId(String url, String checkId);
	String sendDataToRest(String url, String jsonData, Map<String, String> headerMap);
	String sendDataToget(String url,String param);
}
