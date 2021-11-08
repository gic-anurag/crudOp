package com.gic.fadv.verification.keycloak.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class FadvUsers {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long fadvUserId;
	
	private String keycloakUserId;
	
	private String employeeId;
	
	private String userLocation;
	
	private String mobileNumber;
	
	private String userType;
	
	private String userRole;
}
