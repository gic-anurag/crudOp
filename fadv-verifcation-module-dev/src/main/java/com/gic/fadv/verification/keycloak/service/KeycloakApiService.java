package com.gic.fadv.verification.keycloak.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface KeycloakApiService {

	ResponseEntity<String> postKeycloakApi(String requestUrl, Map<String, String> reuqestBody,
			Map<String, String> headerMap);

}
