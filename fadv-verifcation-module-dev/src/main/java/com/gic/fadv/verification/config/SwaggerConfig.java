package com.gic.fadv.verification.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger API Documentation configuration
 * 
 * @author mukesh
 * @since 1.0
 */

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	@Autowired
	private SwaggerProperties swaggerEnvProperties;

	@Bean
	public Docket attemptDocket() {
		return new Docket(DocumentationType.SWAGGER_2).apiInfo(verificatioApiInfo()).select()
				.apis(RequestHandlerSelectors.basePackage("com.gic.fadv.verification")).paths(PathSelectors.any())
				.build().pathMapping("/").protocols(getProtocols());
	}

	private ApiInfo verificatioApiInfo() {
		return new ApiInfoBuilder().title(swaggerEnvProperties.getTitle()).description("Verification Module APIs")
				.version(swaggerEnvProperties.getVersion()).build();
	}

	private Set<String> getProtocols() {
		Set<String> protocols = new HashSet<>();
		protocols.add(swaggerEnvProperties.getProtocol());
		return protocols;
	}
}
