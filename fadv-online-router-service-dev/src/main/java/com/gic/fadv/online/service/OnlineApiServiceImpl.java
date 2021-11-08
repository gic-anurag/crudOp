package com.gic.fadv.online.service;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.collections4.MapUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OnlineApiServiceImpl implements OnlineApiService{

	@Value("${online.company.rest.url}") 
	private String companyRestUrl;
	
	@Value("${online.personal.rest.url}")
	private String personalRestUrl;
	
	@Value("${online.fulfillment.rest.url}")
	private String fulfillmentRestUrl;
	
	@Value ("${online.worldcheck.rest.url}")
	private String worldCheckRestUrl;
	
	@Value("${online.manupatra.rest.url}")
	private String manupatraRestUrl;
	
	@Value("${online.component.wise.api.rest.url}")
	private String componentWiseAPIRestUrl;
	
	@Value("${online.watchout.rest.url}")
	private String watchOutRestUrl;
	@Value("${online.personal.verify.rest.url}")
	private String personalVerifyRestUrl;
	
	@Value("${online.service.personal.rest.url}")
	private String servicePersonalRestUrl;
	@Value("${online.final.personal.rest.url}")
	private String finalPersonalRestUrl;

	  @Value("${casespecificrecord.rest.url}")
	  private String caseSpecificRecord;
	  @Value("${questionaire.l3.auth_token}")
	  private String token;
	  

		private static final Logger logger = LoggerFactory.getLogger(OnlineApiServiceImpl.class);
		
	@Autowired
	private RestTemplateBuilder restTemplateBuilder;
	
	@Override
	public String sendDataToCompanyRest(String jsonData) {
		 
		  try { 
				  HttpHeaders headers = new HttpHeaders();
				  headers.setContentType(MediaType.APPLICATION_JSON); 
				  HttpEntity<String> request = new HttpEntity<>(jsonData, headers); 
				  RestTemplate restTemplate =restTemplateBuilder.build(); 
				  return restTemplate.postForObject(companyRestUrl, request, String.class); 
			  } catch (Exception e) 
		  		{ 
				  e.printStackTrace(); 
				  return e.getMessage(); 
				  }
		}

	@Override
	public String sendDataToPersonalRest(String jsonData) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> request = 
					new HttpEntity<>(jsonData, headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			System.out.println(personalRestUrl);
			System.out.println("Json String"+jsonData);
					return restTemplate.postForObject(personalRestUrl, request, String.class);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	@Override
	public String sendDataTofulfillmentRest(String jsonData) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> request = new HttpEntity<>(jsonData, headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			return restTemplate.postForObject(fulfillmentRestUrl, request, String.class);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	@Override
	public String sendDataToWorldCheckRest(String jsonData) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> request = 
					new HttpEntity<>(jsonData, headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
					return restTemplate.postForObject(worldCheckRestUrl, request, String.class);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	@Override
	public String sendDataToManupatraRest(String jsonData) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> request = 
					new HttpEntity<>(jsonData, headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
					return restTemplate.postForObject(manupatraRestUrl, request, String.class);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	@Override
	public String sendDataToComponentWiseAPIRest() {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> request = 
					new HttpEntity<>(headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			return restTemplate.getForObject(componentWiseAPIRestUrl,String.class);
			//return restTemplate.postForObject(manupatraRestUrl, request, String.class);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	@Override
	public String sendDataToServicePersonalRest(String jsonData) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> request = 
					new HttpEntity<>(jsonData, headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			System.out.println(servicePersonalRestUrl);
			System.out.println("Json String"+jsonData);
					return restTemplate.postForObject(servicePersonalRestUrl, request, String.class);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	
	@Override
	public String sendDataToFinalPersonalRest(String jsonData) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> request = 
					new HttpEntity<>(jsonData, headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			System.out.println(finalPersonalRestUrl);
			System.out.println("Json String"+jsonData);
					return restTemplate.postForObject(finalPersonalRestUrl, request, String.class);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	@Override
	public String sendDataToWatchOutRest(String jsonData) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> request = 
					new HttpEntity<>(jsonData, headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			System.out.println(watchOutRestUrl);
			System.out.println("Json String"+jsonData);
					return restTemplate.postForObject(watchOutRestUrl, request, String.class);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	@Override
	public String sendDataToPersonalVerifyRest(String jsonData) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> request = 
					new HttpEntity<>(jsonData, headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			System.out.println(personalVerifyRestUrl);
			System.out.println("Json String"+jsonData);
					return restTemplate.postForObject(personalVerifyRestUrl, request, String.class);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	/*
	 * THis function will used for OnlineRouter Controller
	 */
	@Override
	public String sendDataToRestUrl(String url,String jsonData) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> request = 
					new HttpEntity<>(jsonData, headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			System.out.println(url);
			System.out.println("Json String"+jsonData);
					return restTemplate.postForObject(url, request, String.class);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	/*
	 * Attempt History Rest Call
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

	 /* Case Specific Record Rest Call
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

	/*
	 * L3 Rest Call
	 */
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
	public String getRequestFromL3(String url) {
		logger.info("URL----------{}", url);
		try {
			/* Set SSL context here */
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy() {
				@Override
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
			headers.set("tokenId", token);
			HttpEntity<String> request = new HttpEntity<>(headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			restTemplate.setRequestFactory(requestFactory);
			ResponseEntity<String> response = restTemplate.exchange(
					url,
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

	@Override
	public String sendDataToLocalRest(String requestUrl, String requestBody) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			return restTemplate.postForObject(requestUrl, request, String.class);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	@Override
	public String getDataFromLocalRest(String requestUrl) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> request = new HttpEntity<>(headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			
			ResponseEntity<String> response = restTemplate.exchange(
					requestUrl,
			        HttpMethod.GET,
			        request,
			        String.class
			);
			return response.getBody();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}

