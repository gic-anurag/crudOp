package com.gic.fadv.verification.keycloak.service;

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
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.gic.fadv.verification.keycloak.handler.RestTemplateResponseErrorHandler;

@Service
public class KeycloakApiServiceImpl implements KeycloakApiService {
	
	@Autowired
	private RestTemplateBuilder restTemplateBuilder;
	
	private static final Logger logger = LoggerFactory.getLogger(KeycloakApiServiceImpl.class);

	@Override
	public ResponseEntity<String> postKeycloakApi(String requestUrl, Map<String, String> reuqestBody,
			Map<String, String> headerMap) {
		logger.info("Request URL: {}", requestUrl);
		logger.info("Request body: {}", reuqestBody);
		logger.info("Request header: {}", headerMap);
		try {
			/* Set SSL context here */
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();

			CloseableHttpClient closeableHttpClient = HttpClients.custom().setSSLContext(sslContext)
					.setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
			HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
			httpComponentsClientHttpRequestFactory.setHttpClient(closeableHttpClient);

			/* Set media type in request headers. */
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			if (MapUtils.isNotEmpty(headerMap)) {
				for (Map.Entry<String, String> entry : headerMap.entrySet()) {
					httpHeaders.set(entry.getKey(), entry.getValue());
				}
			}

			MultiValueMap<String, Object> requestBodyMap = new LinkedMultiValueMap<>();

			if (MapUtils.isNotEmpty(reuqestBody)) {
				for (Map.Entry<String, String> entry : reuqestBody.entrySet()) {
					requestBodyMap.add(entry.getKey(), entry.getValue());
				}
			}

			HttpEntity<MultiValueMap<String, Object>> httpRequestEntity = new HttpEntity<>(requestBodyMap, httpHeaders);
			RestTemplate restTemplate = restTemplateBuilder.build();
			restTemplate.setRequestFactory(httpComponentsClientHttpRequestFactory);
			restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());

			/* Make a POST request using REST. */
			return restTemplate.exchange(requestUrl, HttpMethod.POST,
					httpRequestEntity, String.class);
		} catch (Exception e) {
			logger.debug("Exception while post to keycloak url : {}", requestUrl, e);
			return null;
		}
	}
}
