package com.gic.fadv.verification.config;


import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * SwaggerEnvironmentProperties are used to get properties from
 * application-*.properties file depending upon the environment that we have
 * set.
 * 
 * @author mskhan
 * @since 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "swagger.env")
public class SwaggerProperties {

	@NotNull
	private String verificationApiPackage;
//	@NotNull
//	private String verificationApiEvent;
//	@NotNull
//	private String verificationApiSkillMaster;

	@NotNull
	private String attemptApiPackage;
	@NotNull
	private String onlineApiPackage;
	@NotNull
	private String wordApiPackage;

	@NotNull
	private String protocol;

	@NotNull
	private String title;
	@NotNull
	private String version;

	@NotNull
	private String verificationApiDescription;
//	@NotNull
//	private String verificationApiEventDescription;
//	@NotNull
//	private String verificationApiSkillMasterDescription;
	@NotNull
	private String attemptApiPackageDescription;
	@NotNull
	private String onlineApiPackageDescription;
	@NotNull
	private String wordApiPackageDescription;
	
	@NotNull
	private String allocationEngineApiPackage;
	@NotNull
	private String allocationEngineApiPackageDescription;
	@NotNull
	private String bulkApiPackage;
	@NotNull
	private String bulkApiPackageDescription;
	@NotNull
	private String checkBotRequestApiPackage;
	@NotNull
	private String checkBotRequestApiPackageDescription;
	@NotNull
	private String docsApiPackage;
	@NotNull
	private String docsApiPackageDescription;
	@NotNull
	private String spocApiPackage;
	@NotNull
	private String spocApiPackageDescription;
	@NotNull
	private String skillApiPackage;
	@NotNull
	private String skillApiPackageDescription;
}
