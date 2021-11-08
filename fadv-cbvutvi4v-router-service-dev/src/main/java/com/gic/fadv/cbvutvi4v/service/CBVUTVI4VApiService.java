package com.gic.fadv.cbvutvi4v.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.gic.fadv.cbvutvi4v.pojo.CaseSpecificInfoPOJO;

@Service
public interface CBVUTVI4VApiService {
	
	String LookUpDataAtCbvUtvI4vRest(String jsonData);
	String LookUpDataAtVerifcationSLA(String jsonData);
	String sendDataToAttemptHistory(String jsonData);
	String sendDataToAttempt(String requestUrl, String jsonData);
	String sendDataToCaseSpecificInfo(String caseSpecificInfoStr);
	String sendDataToVerificationEventStatus(String requestUrl, String jsonData);
	String sendDataToRest(String url, String jsonData, Map<String, String> headerMap);
	String sendDataToCaseSpecificRecord(String caseSpecificInfoStr);
	String sendDataTogetCheckId(String url,String checkId);
}
