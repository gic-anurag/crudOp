package com.gic.fadv.cbvutvi4v.service;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;
import javax.net.ssl.SSLContext;

import org.apache.commons.collections4.MapUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
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

@Service
public class ApiServiceImpl implements ApiService {

	@Autowired
	private RestTemplateBuilder restTemplateBuilder;

	@Value("${questionaire.l3.auth_token}")
	private String jwtToken;

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

	@Override
	public String sendDataToL3Get(String requestUrl) {
		logger.info("Request Url for L3 Get : {}", requestUrl);
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

			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);
			httpHeaders.set("tokenId", jwtToken);
			HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
			RestTemplate restTemplate = restTemplateBuilder.build();
			restTemplate.setRequestFactory(requestFactory);
			ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity,
					String.class);
			if (HttpStatus.OK.equals(response.getStatusCode()) || HttpStatus.CREATED.equals(response.getStatusCode()))
				return response.getBody();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception while calling L3 Get url : {}, Error : {}", requestUrl, e.getMessage());
			return null;
		}
	}

	@Override
	public String sendDataToL3Post(String requestUrl, String requestStr, Map<String, String> headerMap) {
		logger.info("Request Url for L3 Post : {}", requestUrl);
		logger.info("Request Body for L3 Post : {}", requestStr);
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

			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);
			httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			httpHeaders.set("tokenId", jwtToken);

			if (MapUtils.isNotEmpty(headerMap)) {
				for (Map.Entry<String, String> entry : headerMap.entrySet()) {
					httpHeaders.set(entry.getKey(), entry.getValue());
				}
			}
			HttpEntity<String> requestEntity = new HttpEntity<>(requestStr, httpHeaders);
			RestTemplate restTemplate = restTemplateBuilder.build();
			restTemplate.setRequestFactory(requestFactory);

			return restTemplate.postForObject(requestUrl, requestEntity, String.class);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception while calling L3 Post url : {}, Error : {}", requestUrl, e.getMessage());
			return null;
		}
	}
}
