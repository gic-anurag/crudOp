package com.gic.fadv.spoc.service;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.collections4.MapUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SPOCApiServiceImpl implements SPOCApiService{
	
	  @Value("${spoclist.rest.url}") 
	  private String spocListRestUrl;
	  @Value("${spocemail.rest.url}")
	  private String spocEmailRestUrl;
	  @Value("${spocemailtemplate.rest.url}")
	  private String spocEmailTemplateRestUrl;
	  @Value("${spoctemplatesearch.rest.url}")
	  private String spocTemplateSearchRestUrl;
	  @Value ("${spocexceltemplate.rest.url}")
	  private String spocExcelTemplateRestUrl;
	  @Value ("${spocattempthistory.rest.url}")
	  private String spocAttemptHistoryRestUrl;
	  @Value("${casespecificinfo.rest.url}")
	  private String caseSpecificInfoUrl;
	  @Value("${casespecificrecord.rest.url}")
	  private String caseSpecificRecord;
	  @Value("${questionaire.l3.auth_token}")
	  private String token;
	  

		private static final Logger logger = LoggerFactory.getLogger(SPOCApiServiceImpl.class);
		
	  @Autowired
	  private RestTemplateBuilder restTemplateBuilder;
	
		  @Override public String sendDataToSPOCListRest(String jsonData) 
		  { 
			  try 
			  {
				  HttpHeaders headers = new HttpHeaders();
				  headers.setContentType(MediaType.APPLICATION_JSON); 
				  HttpEntity<String> request = new HttpEntity<>(jsonData, headers); 
				  RestTemplate restTemplate =restTemplateBuilder.build(); 
				  return restTemplate.postForObject(spocListRestUrl, request, String.class); 
				  } 
			  catch (Exception e) 
			  { 
				  e.printStackTrace(); 
				  return e.getMessage(); 
				  } 
			  }

		@Override
		public String sendDataToEmailConfig(String jsonData) {
			try 
			  {
				  HttpHeaders headers = new HttpHeaders();
				  headers.setContentType(MediaType.APPLICATION_JSON); 
				  HttpEntity<String> request = new HttpEntity<>(jsonData, headers); 
				  RestTemplate restTemplate =restTemplateBuilder.build(); 
				  return restTemplate.postForObject(spocEmailRestUrl, request, String.class); 
				  } 
			  catch (Exception e) 
			  { 
				  e.printStackTrace(); 
				  return e.getMessage(); 
				  } 
		}

		@Override
		public String sendDataToEmailTemplate(String jsonData) {
			try 
			  {
				  HttpHeaders headers = new HttpHeaders();
				  headers.setContentType(MediaType.APPLICATION_JSON); 
				  HttpEntity<String> request = new HttpEntity<>(jsonData, headers); 
				  RestTemplate restTemplate =restTemplateBuilder.build(); 
				  return restTemplate.postForObject(spocEmailTemplateRestUrl, request, String.class); 
				  } 
			  catch (Exception e) 
			  { 
				  e.printStackTrace(); 
				  return e.getMessage(); 
				  }
		}
		
		@Override
		public String sendDataToFindEmailTemplateNumber(String jsonData) {
			try 
			  {
				  HttpHeaders headers = new HttpHeaders();
				  headers.setContentType(MediaType.APPLICATION_JSON); 
				  HttpEntity<String> request = new HttpEntity<>(jsonData, headers); 
				  RestTemplate restTemplate =restTemplateBuilder.build(); 
				  return restTemplate.postForObject(spocTemplateSearchRestUrl, request, String.class); 
				  } 
			  catch (Exception e) 
			  { 
				  e.printStackTrace(); 
				  return e.getMessage(); 
				  }
		}
		
		@Override
		public String sendDataToExcelTemplate(String jsonData) {
			try 
			  {
				  HttpHeaders headers = new HttpHeaders();
				  headers.setContentType(MediaType.APPLICATION_JSON); 
				  HttpEntity<String> request = new HttpEntity<>(jsonData, headers); 
				  RestTemplate restTemplate =restTemplateBuilder.build(); 
				  return restTemplate.postForObject(spocExcelTemplateRestUrl, request, String.class); 
				  } 
			  catch (Exception e) 
			  { 
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
		
		/*
		 * Case Specific Info Rest Call
		 */
		@Override
		public String sendDataToCaseSpecificInfo(String caseSpecificInfoStr) {
			try 
			  {
				  HttpHeaders headers = new HttpHeaders();
				  headers.setContentType(MediaType.APPLICATION_JSON); 
				  HttpEntity<String> request = new HttpEntity<>(caseSpecificInfoStr, headers); 
				  RestTemplate restTemplate =restTemplateBuilder.build(); 
				  return restTemplate.postForObject(caseSpecificInfoUrl, request, String.class); 
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
		
		/*
		 * Case Specific Record Rest Call
		 */
		@Override
		public String sendDataToCaseSpecificRecord(String caseSpecificInfoStr) {
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<String> request = new HttpEntity<>(caseSpecificInfoStr, headers);
				RestTemplate restTemplate = restTemplateBuilder.build();
				return restTemplate.postForObject(caseSpecificRecord, request, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				return e.getMessage();
			}
		}
		
		@Override
		public String sendDataTogetCheckId(String url,String checkId) {
			logger.info("URL----------"+url);
			logger.info("JSON DATA-------------"+checkId);
			try {
				
				SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy() {
					public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
						return true;
					}
				}).build();
				
				CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(sslContext)
						.setSSLHostnameVerifier(new NoopHostnameVerifier()).build();

				HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

				requestFactory.setHttpClient(httpClient);
				
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				//String accessToken="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ7XCJtdGVpZFwiOlwiOGI3NGQ0NGMtMDU5MC00MDdjLThiZDgtNjFkM2FkN2Y2ZjM5XCIsXCJ1c2VySWRcIjpcIjA0MDBiZjFiLTgzZDctNDI3YS1iYjEwLWFmOTMwMzFlMDVhNlwiLFwiYXBwU2Vzc2lvbklkXCI6XCI5ODgwNThlYS0yODY4LTQ1YmUtOTk0Yi0zYmZjMWFjODg2NDRcIixcInByb2plY3RJZFwiOlwicHJvamVjdElkXCIsXCJwcm9qZWN0VmVyc2lvbklkXCI6XCJwcm9qZWN0VmVyc2lvbklkXCJ9IiwiZXhwIjoxNjI0OTkxNDAwfQ.pY8-23LNWixue9zy_66c1RiVDhiQuNCXsgK5IihlW3sh3XAjG08uzdUPvhbABLzkKJZTdoQzlLTGW3BP7b9Hcw";
				headers.set("tokenId", token);
				HttpEntity<String> request = new HttpEntity<>(headers);
				RestTemplate restTemplate = restTemplateBuilder.build();
				restTemplate.setRequestFactory(requestFactory);
				ResponseEntity<String> response = restTemplate.exchange(
						url+checkId,
				        HttpMethod.GET,
				        request,
				        String.class
				);
				//return response;
				if (response.getStatusCode() == HttpStatus.OK) {
				    System.out.println("Request Successful.");
				    System.out.println(response.getBody());
				    return response.getBody();
				} else {
				    System.out.println("Request Failed");
				    System.out.println(response.getStatusCode());
				    return null;
				}
				//return restTemplate.postForObject(componentListL3Url, request, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				return e.getMessage();
			}
		}
		
		@Override
		public String sendDataToRest(String url,String jsonData,Map<String,String> headerMap) {
			logger.info("URL----------"+url);
			logger.info("JSON DATA-------------"+jsonData);
			try {
				SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy() {
					public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
						return true;
					}
				}).build();
				
				CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(sslContext)
						.setSSLHostnameVerifier(new NoopHostnameVerifier()).build();

				HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

				requestFactory.setHttpClient(httpClient);
				
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
				//String accessToken="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ7XCJtdGVpZFwiOlwiOGI3NGQ0NGMtMDU5MC00MDdjLThiZDgtNjFkM2FkN2Y2ZjM5XCIsXCJ1c2VySWRcIjpcIjA0MDBiZjFiLTgzZDctNDI3YS1iYjEwLWFmOTMwMzFlMDVhNlwiLFwiYXBwU2Vzc2lvbklkXCI6XCI5ODgwNThlYS0yODY4LTQ1YmUtOTk0Yi0zYmZjMWFjODg2NDRcIixcInByb2plY3RJZFwiOlwicHJvamVjdElkXCIsXCJwcm9qZWN0VmVyc2lvbklkXCI6XCJwcm9qZWN0VmVyc2lvbklkXCJ9IiwiZXhwIjoxNjI0OTkxNDAwfQ.pY8-23LNWixue9zy_66c1RiVDhiQuNCXsgK5IihlW3sh3XAjG08uzdUPvhbABLzkKJZTdoQzlLTGW3BP7b9Hcw";
				headers.set("tokenId", token);
				if(MapUtils.isNotEmpty(headerMap)) {
					for (Map.Entry<String,String> entry : headerMap.entrySet()) { 
			            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue()); 
			            headers.set(entry.getKey(), entry.getValue());
					}
					
				}
				HttpEntity<String> request = 
						new HttpEntity<>(jsonData, headers);
				System.out.println("Request"+request);
				RestTemplate restTemplate = restTemplateBuilder.build();
				restTemplate.setRequestFactory(requestFactory);
				return restTemplate.postForObject(url, request, String.class);
			} catch (Exception e) {
				logger.debug(e.getMessage(), e);
				return null;
			}
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
				//return response;
				if (response.getStatusCode() == HttpStatus.OK) {
				    System.out.println("Request Successful.");
				    System.out.println(response.getBody());
				    return response.getBody();
				} else {
				    System.out.println("Request Failed");
				    System.out.println(response.getStatusCode());
				    return null;
				}
				//return restTemplate.postForObject(componentListL3Url, request, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				return e.getMessage();
			}
		}
}

