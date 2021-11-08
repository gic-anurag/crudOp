package com.gic.fadv.verification.keycloak.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.verification.keycloak.interfaces.UserInterface;
import com.gic.fadv.verification.keycloak.pojo.UserPOJO;
import com.gic.fadv.verification.keycloak.repository.FadvUsersRepository;
import com.gic.fadv.verification.keycloak.service.KeycloakService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class KeycloakController {
	
	@Autowired
	private KeycloakService keycloakService;
	@Autowired
	private FadvUsersRepository fadvUsersRepository;
	
	@PostMapping("/create-user")
	public ResponseEntity<ObjectNode> createUser(@RequestBody UserPOJO userPOOJ) {
		return keycloakService.createNewUser(userPOOJ);
	}
	
	@PostMapping(path = "/signin", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> getSignIn(@RequestBody JsonNode requestBody) {
		return keycloakService.getSignInToken(requestBody);
	}
	@GetMapping("/get-user-list")
	public List<UserInterface> getUsers() {
		try {
			return fadvUsersRepository.getUserList();
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}
	
}
