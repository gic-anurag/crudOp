package com.fadv.cspi.service;

import org.springframework.stereotype.Service;

@Service
public interface ApiService {

	String sendDataToPost(String requestUrl, String requestStr);

	String sendDataToGet(String requestUrl);

}
