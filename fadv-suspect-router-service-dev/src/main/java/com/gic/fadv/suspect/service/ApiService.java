package com.gic.fadv.suspect.service;

public interface ApiService {

	String sendDataToGet(String requestUrl, String requestStr);

	String sendDataToPost(String requestUrl, String requestStr);
}
