package com.gic.fadv.verification.keycloak.config;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;

public class KeycloakConfig {

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

	Keycloak keycloak = null;
	
	public Keycloak getInstance() {
		if (keycloak == null) {

			keycloak = KeycloakBuilder.builder().serverUrl(keycloakserverUrl).realm(keycloakRealm)
					.grantType(OAuth2Constants.PASSWORD).username(keycloakUsername).password(keycloakPassword)
					.clientId(keycloakResource).clientSecret(keycloakClientSecret)
					.resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build()).build();
		}
		return keycloak;
	}
}