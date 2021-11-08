package com.gic.fadv.stellar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class StellarApiServiceImpl implements StellarApiService{

	@Value("${stellar.rest.url}") 
	private String stellarRestUrl;
	 
	
	@Value("${fadv.stellar.mrl.url}")
	private String stellarMrlUrl;
	
	@Autowired
	private RestTemplateBuilder restTemplateBuilder;
	
	
	  @Override public String sendDataToStellarRest(String jsonData) {
	  
	  try { 
			  HttpHeaders headers = new HttpHeaders();
			  headers.setContentType(MediaType.APPLICATION_JSON); 
			  HttpEntity<String> request = new HttpEntity<>(jsonData, headers); 
			  RestTemplate restTemplate =restTemplateBuilder.build(); 
			  return restTemplate.postForObject(stellarRestUrl, request, String.class); 
		  } catch (Exception e) 
	  		{ 
			  e.printStackTrace(); 
			  return e.getMessage(); 
			  } 
	  	}

	@Override
	public String callInstraStellarMRLRouter(String jsonData) {
		try {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<String> request = 
						new HttpEntity<>(jsonData, headers);
				RestTemplate restTemplate = restTemplateBuilder.build();
						return restTemplate.postForObject(stellarMrlUrl, request, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				return e.getMessage();
			}
		}
	
}

