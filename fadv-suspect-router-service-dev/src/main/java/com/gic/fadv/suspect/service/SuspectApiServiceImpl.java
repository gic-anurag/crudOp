package com.gic.fadv.suspect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SuspectApiServiceImpl implements SuspectApiService{
	
	@Value("${suspect.rest.url}") 
	private String suspectRestUrl;
	
	@Value("${unisuspect.rest.url}") 
	private String unisuspectRestUrl;
	 
	
	/*
	 * @Value("${cbvutvi4v.rest.url}") private String cbvUtvI4vRestUrl;
	 */
	@Autowired
	private RestTemplateBuilder restTemplateBuilder;
	
		  @Override public String sendDataToSuspectRest(String jsonData) 
		  { 
			  try {
				  HttpHeaders headers = new HttpHeaders();
				  headers.setContentType(MediaType.APPLICATION_JSON); HttpEntity<String>
				  request = new HttpEntity<>(jsonData, headers); 
				  RestTemplate restTemplate = restTemplateBuilder.build(); 
				  return restTemplate.postForObject(suspectRestUrl, request, String.class); 
			} catch (Exception e) { 
					  e.printStackTrace(); 
					  return e.getMessage(); 
					  } 
			}
		 
		
		  @Override public String sendDataToUniSuspectRest(String jsonData) 
		  { 
			  try {
				  HttpHeaders headers = new HttpHeaders();
				  headers.setContentType(MediaType.APPLICATION_JSON); 
				  HttpEntity<String> request = new HttpEntity<>(jsonData, headers); 
				  RestTemplate restTemplate =
						  restTemplateBuilder.build(); 
				  return restTemplate.postForObject(unisuspectRestUrl, request, String.class); 
			} catch (Exception e) 
			  	{ 
					  e.printStackTrace(); 
					  return e.getMessage(); 
					  } 
			  }
		 

		/*
		 * @Override public String sendDataToCbvUtvI4vRest(String jsonData) { try {
		 * HttpHeaders headers = new HttpHeaders();
		 * headers.setContentType(MediaType.APPLICATION_JSON); HttpEntity<String>
		 * request = new HttpEntity<>(jsonData, headers); RestTemplate restTemplate =
		 * restTemplateBuilder.build(); return
		 * restTemplate.postForObject(cbvUtvI4vRestUrl, request, String.class); } catch
		 * (Exception e) { e.printStackTrace(); return e.getMessage(); } }
		 */
	
}

