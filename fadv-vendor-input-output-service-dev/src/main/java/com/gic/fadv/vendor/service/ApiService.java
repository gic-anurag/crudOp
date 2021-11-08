package com.gic.fadv.vendor.service;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface ApiService {

	String sendDataToGet(String requestUrl);

	String sendDataToPost(String requestUrl, String requestStr);

	String sendDataToL3Post(String requestUrl, String requestStr, Map<String, String> headerMap);

	String sendDataToL3Get(String requestUrl);

}
