package com.gic.fadv.verification.attempts.service;

import java.util.Map;

import org.springframework.stereotype.Service;


@Service
public interface L3APIService {
	String sendDataTogetComponent(String url);
	String sendDataTogetComponentQuestion(String componentId);
	String sendDataToRest(String url,String jsonData,Map<String,String> headerMap);
	String sendDataTogetCaseId(String url,String caseId);
	String sendDataTogetVerification(String componentListL3Url,String CheckId);
}
