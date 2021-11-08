package com.gic.fadv.verification.attempts.service;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.collections4.MapUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContextBuilder;
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
public class L3APIServiceImpl implements L3APIService {

	private static final String REQUEST_FAILED = "Request Failed";

	private static final String REQUEST_SUCCESSFUL = "Request Successful.";

	private static final String TOKEN_ID = "tokenId";

	private static final Logger logger = LoggerFactory.getLogger(L3APIServiceImpl.class);

	@Value("${component.question.list.L3.url}")
	private String componentQuestionListL3Url;

	@Value("${questionaire.l3.auth_token}")
	private String jwtToken;

	@Autowired
	private RestTemplateBuilder restTemplateBuilder;

	@Override
	public String sendDataTogetComponent(String componentListL3Url) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set(TOKEN_ID, jwtToken);
			HttpEntity<String> request = new HttpEntity<>(headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			ResponseEntity<String> response = restTemplate.exchange(componentListL3Url, HttpMethod.GET, request,
					String.class);

			if (response.getStatusCode() == HttpStatus.OK) {
				logger.info(REQUEST_SUCCESSFUL);
				return response.getBody();
			} else {
				logger.info(REQUEST_FAILED);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	@Override
	public String sendDataTogetComponentQuestion(String componentId) {
		try {

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
			headers.set(TOKEN_ID, jwtToken);
			HttpEntity<String> request = new HttpEntity<>(headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			restTemplate.setRequestFactory(requestFactory);
			ResponseEntity<String> response = restTemplate.exchange(
					componentQuestionListL3Url + componentId + "/componentId", HttpMethod.GET, request, String.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				logger.info(REQUEST_SUCCESSFUL);
				return response.getBody();
			} else {
				logger.info(REQUEST_FAILED);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	@Override
	public String sendDataTogetCaseId(String url, String caseId) {
		logger.info("URL---------- {}", url);
		logger.info("JSON DATA-------------{}", caseId);
		try {

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
			headers.set(TOKEN_ID, jwtToken);
			HttpEntity<String> request = new HttpEntity<>(headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			restTemplate.setRequestFactory(requestFactory);
			ResponseEntity<String> response = restTemplate.exchange(url + caseId, HttpMethod.GET, request,
					String.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				logger.info(REQUEST_SUCCESSFUL);
				return response.getBody();
			} else {
				logger.info(REQUEST_FAILED);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	/*
	 * Function used for Post Method
	 */
	public String sendDataToRest(String url, String jsonData, Map<String, String> headerMap) {
		logger.info("URL----------{}", url);
		logger.info("JSON DATA-------------{}", jsonData);
		try {
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
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			headers.set(TOKEN_ID, jwtToken);
			if (MapUtils.isNotEmpty(headerMap)) {
				for (Map.Entry<String, String> entry : headerMap.entrySet()) {
					logger.info("Key = {}, value = {}", entry.getKey(), entry.getValue());
					headers.set(entry.getKey(), entry.getValue());
				}

			}
			HttpEntity<String> request = new HttpEntity<>(jsonData, headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			restTemplate.setRequestFactory(requestFactory);
			return restTemplate.postForObject(url, request, String.class);
		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public String sendDataTogetVerification(String verificationUrl, String checkId) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> request = new HttpEntity<>(headers);
			RestTemplate restTemplate = restTemplateBuilder.build();
			ResponseEntity<String> response = restTemplate.exchange(verificationUrl + checkId, HttpMethod.GET, request,
					String.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				logger.info(REQUEST_SUCCESSFUL);
				return response.getBody();
			} else {
				logger.info(REQUEST_FAILED);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
}
