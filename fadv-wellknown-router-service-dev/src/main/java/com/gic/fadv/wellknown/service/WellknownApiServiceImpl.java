package com.gic.fadv.wellknown.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WellknownApiServiceImpl implements WellknownApiService{
	
	@Value("${wellknown.rest.url}") 
	private String wellknownRestUrl;
	@Autowired
	private RestTemplateBuilder restTemplateBuilder;
	
		  @Override public String sendDataToWellknownRest(String jsonData) 
		  { 
			  try {
				  HttpHeaders headers = new HttpHeaders();
				  headers.setContentType(MediaType.APPLICATION_JSON); 
				  HttpEntity<String>request = new HttpEntity<>(jsonData, headers); 
				  RestTemplate restTemplate = restTemplateBuilder.build(); 
				  return restTemplate.postForObject(wellknownRestUrl, request, String.class); 
			} catch (Exception e) { 
					  e.printStackTrace(); 
					  return e.getMessage(); 
					  } 
			}	
}

