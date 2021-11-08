package com.gic.fadv.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).select().paths(PathSelectors.ant("/api/**"))
				.apis(RequestHandlerSelectors.basePackage("com.gic.fadv")).build().apiInfo(metaData());
	}

	private ApiInfo metaData() {
		return new ApiInfoBuilder().title("FADV Data Entry service REST API").description(
				"\"FADV Data Entry REST API Endpoints. These are independent REST API which can be called by Authorized parties\"")
				.version("1.0.0").license("These API are protected under FADV and GIC License")
				.licenseUrl("http://www.gridinfocom.com").contact(new Contact("Vishwanath Kumar",
						"http://www.gridinfocom.com/", "vishwanath.kumar@gridinfocom.com"))
				.build();
	}
}