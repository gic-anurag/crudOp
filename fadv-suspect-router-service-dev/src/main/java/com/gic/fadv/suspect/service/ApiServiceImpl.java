package com.gic.fadv.suspect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiServiceImpl implements ApiService {

	@Autowired
	private RestTemplateBuilder restTemplateBuilder;

	private static final Logger logger = LoggerFactory.getLogger(ApiServiceImpl.class);

	@Override
	public String sendDataToPost(String requestUrl, String requestStr) {
		logger.info("Request Url for Post : {}", requestUrl);
		logger.info("Request Body for Post : {}", requestStr);
		try {
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> requestEntity = new HttpEntity<>(requestStr, httpHeaders);
			RestTemplate restTemplate = restTemplateBuilder.build();
			return restTemplate.postForObject(requestUrl, requestEntity, String.class);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception while Post request to request url : {}, Error : {}", requestUrl, e.getMessage());
			return null;
		}
	}

	@Override
	public String sendDataToGet(String requestUrl, String requestStr) {
		logger.info("Request Url for Get : {}", requestUrl);
		try {
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
			RestTemplate restTemplate = restTemplateBuilder.build();
			ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity,
					String.class);
			if (HttpStatus.OK.equals(response.getStatusCode()) || HttpStatus.CREATED.equals(response.getStatusCode()))
				return response.getBody();
			return null;
		} catch (Exception e) {
			logger.info("Exception while Get request to request url : {}, Error : {}", requestUrl, e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
}
