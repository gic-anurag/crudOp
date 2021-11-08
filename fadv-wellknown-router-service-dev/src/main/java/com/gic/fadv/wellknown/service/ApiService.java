package com.gic.fadv.wellknown.service;

import org.springframework.stereotype.Service;

@Service
public interface ApiService {

	String sendDataToGet(String requestUrl, String requestStr);

	String sendDataToPost(String requestUrl, String requestStr);

}
