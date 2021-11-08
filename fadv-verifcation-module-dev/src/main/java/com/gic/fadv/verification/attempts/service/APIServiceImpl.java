package com.gic.fadv.verification.attempts.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.fadv.verification.attempts.model.AttemptHistory;
import com.gic.fadv.verification.pojo.AttemptHistoryPOJO;

@Service
public class APIServiceImpl implements APIService {

	@Value("${attempthistory.rest.url}")
	private String spocAttemptHistoryRestUrl;
	
	@Value("${caseSpecification.rest.url}")
	private String caseSpecificationRestUrl;

	@Autowired
	private RestTemplateBuilder restTemplateBuilder;
	
	@Autowired
	private EntityManager entityManager;

	@Override
	public String sendDataToPost(String jsonData) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> request = new HttpEntity<>(jsonData, headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			return restTemplate.postForObject(caseSpecificationRestUrl, request, String.class);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	/*
	 * Attempt History Rest Call
	 */
	@Override
	public String sendDataToAttemptHistory(String jsonData) {
		try 
		  {
			  HttpHeaders headers = new HttpHeaders();
			  headers.setContentType(MediaType.APPLICATION_JSON); 
			  HttpEntity<String> request = new HttpEntity<>(jsonData, headers); 
			  RestTemplate restTemplate =restTemplateBuilder.build(); 
			  return restTemplate.postForObject(spocAttemptHistoryRestUrl, request, String.class); 
			  } 
		  catch (Exception e) 
		  { 
			  e.printStackTrace(); 
			  return e.getMessage(); 
			  }
	}
	
	/*
	 * Attempt Status Rest Call
	 */
	@Override
	public String sendDataToAttempt(String requestUrl, String jsonData) {
		try 
		  {
			  HttpHeaders headers = new HttpHeaders();
			  headers.setContentType(MediaType.APPLICATION_JSON); 
			  HttpEntity<String> request = new HttpEntity<>(jsonData, headers); 
			  RestTemplate restTemplate =restTemplateBuilder.build(); 
			  return restTemplate.postForObject(requestUrl, request, String.class); 
			  } 
		  catch (Exception e) 
		  { 
			  e.printStackTrace(); 
			  return e.getMessage(); 
			  }
	}
	
	@Override
	public String sendDataToL3ByCheckId(String requestUrl, List<String> checkIdList) {
		try 
		  {
			  HttpHeaders headers = new HttpHeaders();
			  headers.setContentType(MediaType.APPLICATION_JSON); 
			  HttpEntity<List<String>> request = new HttpEntity<>(checkIdList, headers); 
			  RestTemplate restTemplate =restTemplateBuilder.build(); 
			  return restTemplate.postForObject(requestUrl, request, String.class); 
			  } 
		  catch (Exception e) 
		  { 
			  e.printStackTrace(); 
			  return e.getMessage(); 
			  }
	}
	
	@Override
	public String sendDataToVerificationEventStatus(String requestUrl, String jsonData) {
		try 
		  {
			  HttpHeaders headers = new HttpHeaders();
			  headers.setContentType(MediaType.APPLICATION_JSON); 
			  HttpEntity<String> request = new HttpEntity<>(jsonData, headers); 
			  RestTemplate restTemplate =restTemplateBuilder.build(); 
			  return restTemplate.postForObject(requestUrl, request, String.class); 
			  } 
		  catch (Exception e) 
		  { 
			  e.printStackTrace(); 
			  return e.getMessage(); 
			  }
	}
	
	public List<AttemptHistory> getAttemptHistoryByFilter(AttemptHistoryPOJO attemptHistoryPOJO){
		
		boolean isFilter = false;
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<AttemptHistory> criteriaQuery = criteriaBuilder.createQuery(AttemptHistory.class);
		Root<AttemptHistory> itemRoot = criteriaQuery.from(AttemptHistory.class);

		List<Predicate> predicates = new ArrayList<>();
		List<AttemptHistory> attemptHistoryList = new ArrayList<AttemptHistory>();

		if (attemptHistoryPOJO.getAttemptid() != null) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("id"), attemptHistoryPOJO.getAttemptid()));
			isFilter = true;
		}
		String checkId = attemptHistoryPOJO.getCheckid() != null? attemptHistoryPOJO.getCheckid().toString(): "";
		if (StringUtils.isNotEmpty(checkId)) {
			predicates.add(
					criteriaBuilder.equal(itemRoot.get("checkid"), checkId));
			isFilter = true;
		}
		
		if(isFilter) {
              
              criteriaQuery.where(predicates.toArray(new Predicate[0]));
              attemptHistoryList = entityManager.createQuery(criteriaQuery).getResultList();
              }
		

		return attemptHistoryList;
	}
	
	@Override
	public String sendDataToget(String url,String param) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			//String accessToken="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ7XCJtdGVpZFwiOlwiOGI3NGQ0NGMtMDU5MC00MDdjLThiZDgtNjFkM2FkN2Y2ZjM5XCIsXCJ1c2VySWRcIjpcIjA0MDBiZjFiLTgzZDctNDI3YS1iYjEwLWFmOTMwMzFlMDVhNlwiLFwiYXBwU2Vzc2lvbklkXCI6XCI5ODgwNThlYS0yODY4LTQ1YmUtOTk0Yi0zYmZjMWFjODg2NDRcIixcInByb2plY3RJZFwiOlwicHJvamVjdElkXCIsXCJwcm9qZWN0VmVyc2lvbklkXCI6XCJwcm9qZWN0VmVyc2lvbklkXCJ9IiwiZXhwIjoxNjI0OTkxNDAwfQ.pY8-23LNWixue9zy_66c1RiVDhiQuNCXsgK5IihlW3sh3XAjG08uzdUPvhbABLzkKJZTdoQzlLTGW3BP7b9Hcw";
			//headers.set("tokenId", accessToken);
			HttpEntity<String> request = new HttpEntity<>(headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			ResponseEntity<String> response = restTemplate.exchange(
					url+param,
			        HttpMethod.GET,
			        request,
			        String.class
			);
			if (response.getStatusCode() == HttpStatus.OK) {
			    return response.getBody();
			} else {
			    return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
}
