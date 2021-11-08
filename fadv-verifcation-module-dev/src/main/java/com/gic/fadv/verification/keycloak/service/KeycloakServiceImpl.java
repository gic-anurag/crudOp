package com.gic.fadv.verification.keycloak.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.verification.keycloak.interfaces.UserInterface;
import com.gic.fadv.verification.keycloak.model.FadvUsers;
import com.gic.fadv.verification.keycloak.pojo.UserPOJO;
import com.gic.fadv.verification.keycloak.repository.FadvUsersRepository;
import com.gic.fadv.verification.skill.model.SkillMapping;
import com.gic.fadv.verification.skill.pojo.SkillPOJO;
import com.gic.fadv.verification.skill.repository.SkillMappingRepository;

@Service
public class KeycloakServiceImpl implements KeycloakService {

	private static final String BODY = "body";

	private static final String MESSAGE = "message";

	private static final String STATUS = "status";

	private static final String ERROR_DESCRIPTION = "error_description";

	private static final String ERROR = "error";

	private static final String PASSWORD = "password";

	private static final String USERNAME = "username";

	@Autowired
	private KeycloakApiService keycloakApiService;

	@Autowired
	private FadvUsersRepository fadvUsersRepository;
	
	@Autowired
	private SkillMappingRepository skillMappingRepository;

	@Value("${keycloak.auth-server-url}")
	private String keycloakserverUrl;

	@Value("${keycloak.username}")
	private String keycloakUsername;

	@Value("${keycloak.password}")
	private String keycloakPassword;

	@Value("${keycloak.resource}")
	private String keycloakResource;

	@Value("${keycloak.credentials.secret}")
	private String keycloakClientSecret;

	@Value("${keycloak.realm}")
	private String keycloakRealm;

	@Value("${keycloak.login.api}")
	private String keycloakLoginUrl;

	Keycloak keycloak = null;

	private static final Logger logger = LoggerFactory.getLogger(KeycloakServiceImpl.class);

	public Keycloak getInstance() {
		if (keycloak == null) {

			keycloak = KeycloakBuilder.builder().serverUrl(keycloakserverUrl).realm(keycloakRealm)
					.grantType(OAuth2Constants.PASSWORD).username(keycloakUsername).password(keycloakPassword)
					.clientId(keycloakResource).clientSecret(keycloakClientSecret)
					.resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build()).build();
		}
		return keycloak;
	}

	@Override
	public ResponseEntity<ObjectNode> createNewUser(UserPOJO userPOJO) {

		logger.info("Create new user request : {}", userPOJO);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		ObjectNode responseNode = mapper.createObjectNode();

		if (!StringUtils.equalsIgnoreCase(userPOJO.getPassword(), userPOJO.getRepassword())) {
			responseNode.put(ERROR, "password_do_not_match");
			responseNode.put(ERROR_DESCRIPTION, "Password provided does not match");
			return new ResponseEntity<>(responseNode, HttpStatus.BAD_REQUEST);
		}

		UsersResource usersResource = getInstance().realm(keycloakRealm).users();
		CredentialRepresentation credentialRepresentation = createPasswordCredentials(userPOJO.getPassword());

		UserRepresentation kcUser = new UserRepresentation();
		kcUser.setUsername(userPOJO.getFadvEmail());
		kcUser.setCredentials(Collections.singletonList(credentialRepresentation));
		kcUser.setFirstName(userPOJO.getFirstName());
		kcUser.setLastName(userPOJO.getLastName());
		kcUser.setEmail(userPOJO.getFadvEmail());
		kcUser.setEnabled(true);
		kcUser.setEmailVerified(false);

		Map<String, List<String>> clientRoles = new HashMap<>();
		clientRoles.put(keycloakResource, Arrays.asList(userPOJO.getUserRole()));
		kcUser.setClientRoles(clientRoles);
		Response response = usersResource.create(kcUser);

		String userId = getInstance().realm(keycloakRealm).users().search(userPOJO.getFadvEmail()).get(0).getId();

		try {
			if (userId != null && StringUtils.isNotEmpty(userId) && response.getStatus() == 201) {
				assignClientRoleToUser(userId, userPOJO.getUserRole());

				FadvUsers fadvUsers = createUserExtended(userId, userPOJO);
				userPOJO.setFadvUserId(fadvUsers.getFadvUserId());
				userPOJO.setPassword(null);
				userPOJO.setRepassword(null);
				asssignSkillToUser(userPOJO);
				
				responseNode.put(STATUS, response.getStatus());
				responseNode.put(MESSAGE, "success");
				responseNode.set(BODY, mapper.convertValue(userPOJO, JsonNode.class));
				return new ResponseEntity<>(responseNode, HttpStatus.CREATED);
			} else {
				responseNode.put(STATUS, response.getStatus());
				responseNode.put(MESSAGE, response.getStatusInfo().toString());
				return new ResponseEntity<>(responseNode, HttpStatus.valueOf(response.getStatus()));
			}
		} catch (Exception e) {
			logger.error("Exception occurred while mapping role : {}", e.getMessage());
			UserResource user = keycloak.realm(keycloakRealm).users().get(userId);
			user.remove();
			responseNode.put(ERROR, "internal_server_error");
			responseNode.put(ERROR_DESCRIPTION, "Some error occurred. Please try again in sometime.");
			return new ResponseEntity<>(responseNode, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	private void asssignSkillToUser(UserPOJO userPOJO) {
		List<SkillMapping> skillMappings = new ArrayList<>();
		for (SkillPOJO skillPOJO : userPOJO.getSkills()) {
			SkillMapping skillMapping = new SkillMapping();
			skillMapping.setUserId(userPOJO.getFadvUserId());
			skillMapping.setSkillId(skillPOJO.getSkillId());
			skillMapping.setSkillType(skillPOJO.getSkillType());
			skillMappings.add(skillMapping);
		}
		if (CollectionUtils.isNotEmpty(skillMappings)) {
			skillMappingRepository.saveAll(skillMappings);
		}
	}

	private void assignClientRoleToUser(String userId, String userRole) {
		String clientId = getInstance().realm(keycloakRealm).clients().findByClientId(keycloakResource).get(0).getId();
		List<RoleRepresentation> roleToAdd = new LinkedList<>();
		roleToAdd.add(
				getInstance().realm(keycloakRealm).clients().get(clientId).roles().get(userRole).toRepresentation());

		UserResource user = keycloak.realm(keycloakRealm).users().get(userId);
		user.roles().clientLevel(clientId).add(roleToAdd);
	}

	private FadvUsers createUserExtended(String userId, UserPOJO userPOJO) {
		FadvUsers fadvUsers = new FadvUsers();
		fadvUsers.setKeycloakUserId(userId);
		fadvUsers.setEmployeeId(userPOJO.getEmployeeId());
		fadvUsers.setMobileNumber(userPOJO.getMobileNumber());
		fadvUsers.setUserLocation(userPOJO.getUserLocation());
		fadvUsers.setUserRole(userPOJO.getUserRole());
		fadvUsers.setUserType(userPOJO.getUserType());

		return fadvUsersRepository.save(fadvUsers);
	}

	private static CredentialRepresentation createPasswordCredentials(String password) {
		CredentialRepresentation passwordCredentials = new CredentialRepresentation();
		passwordCredentials.setTemporary(false);
		passwordCredentials.setType(CredentialRepresentation.PASSWORD);
		passwordCredentials.setValue(password);
		return passwordCredentials;
	}

	@Override
	public ResponseEntity<String> getSignInToken(JsonNode requestBody) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		String username = requestBody.has(USERNAME) ? requestBody.get(USERNAME).asText() : "";
		String password = requestBody.has(PASSWORD) ? requestBody.get(PASSWORD).asText() : "";

		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
			Map<String, String> requsetMap = new HashedMap<>();
			requsetMap.put("client_id", keycloakResource);
			requsetMap.put(USERNAME, username);
			requsetMap.put(PASSWORD, password);
			requsetMap.put("grant_type", PASSWORD);
			requsetMap.put("client_secret", keycloakClientSecret);

			String requestUrl = keycloakserverUrl + keycloakLoginUrl;
			ResponseEntity<String> apiResponse = keycloakApiService.postKeycloakApi(requestUrl, requsetMap, null);
			if (apiResponse == null) {
				return badRequestError(mapper);
			}
			apiResponse = getUserDetails(apiResponse);
			if (apiResponse == null) {
				return badRequestError(mapper);
			}
			return apiResponse;
		}
		ObjectNode response = mapper.createObjectNode();
		response.put(ERROR, "invalid_information");
		response.put(ERROR_DESCRIPTION, "Please provide valid username and password");
		return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
	}

	private ResponseEntity<String> badRequestError(ObjectMapper mapper) {
		ObjectNode response = mapper.createObjectNode();
		response.put(ERROR, "internal_server_error");
		response.put(ERROR_DESCRIPTION, "Some error occurred. Please try again in sometime.");
		return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
	}

	private ResponseEntity<String> getUserDetails(ResponseEntity<String> response) {
		// Get user details using sign in response
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		try {
			ObjectNode responseNode = (ObjectNode) mapper.readTree(response.getBody());
			String accessToken = responseNode.has("access_token") ? responseNode.get("access_token").asText() : "";

			if (StringUtils.isNotEmpty(accessToken)) {
				// Split Token in Chunks
				String[] chunks = accessToken.split("\\.");
				Base64.Decoder decoder = Base64.getDecoder();

				JsonNode payloadNode = mapper.readTree(new String(decoder.decode(chunks[1])));
				String userId = payloadNode.has("sub") ? payloadNode.get("sub").asText() : "";

				if (StringUtils.isNotEmpty(userId)) {
					UserInterface userTuple = fadvUsersRepository.getUserByUserId(userId);
					if (userTuple != null) {
						List<String> skillNames = skillMappingRepository.getSkillNamesByUserId(userTuple.getUserId());
						ObjectNode userNode = mapper.convertValue(userTuple, ObjectNode.class);
						if (skillNames != null && CollectionUtils.isNotEmpty(skillNames)) {
							ArrayNode skillNode = mapper.convertValue(skillNames, ArrayNode.class);
							userNode.set("skills", skillNode);
						}
						responseNode.set("user", userNode);
						return ResponseEntity.ok().body(responseNode.toString());
					}
				}
			}
		} catch (JsonProcessingException e) {
			logger.error("Exception occurred while fetching user details : {}", e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
}
