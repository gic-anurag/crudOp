package com.gic.fadv.verification.keycloak.pojo;

import java.util.List;

import com.gic.fadv.verification.skill.pojo.SkillPOJO;

import lombok.Data;

@Data
public class UserPOJO {

	private Long fadvUserId;
	
	private String firstName;
	
	private String lastName;
	
	private String password;

	private String repassword;
	
	private String fadvEmail;

	private String employeeId;

	private String userLocation;

	private String mobileNumber;

	private String userType;

	private String userRole;
	
	private List<SkillPOJO> skills;
}
