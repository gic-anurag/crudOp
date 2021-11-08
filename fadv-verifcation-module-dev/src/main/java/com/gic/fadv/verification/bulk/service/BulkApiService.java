package com.gic.fadv.verification.bulk.service;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface BulkApiService {

	String sendDataToPost(String requestUrl, String requestStr);

	String sendDataToL3Post(String requestUrl, String requestStr, Map<String, String> headerMap);

	String sendDataToL3Get(String requestUrl);

	String sendDataToGet(String requestUrl);

}
