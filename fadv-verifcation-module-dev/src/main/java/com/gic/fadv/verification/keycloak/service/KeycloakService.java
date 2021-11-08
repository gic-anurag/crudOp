package com.gic.fadv.verification.keycloak.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.verification.keycloak.pojo.UserPOJO;

@Service
public interface KeycloakService {

	ResponseEntity<ObjectNode> createNewUser(UserPOJO userPOJO);

	ResponseEntity<String> getSignInToken(JsonNode requestBody);

}
