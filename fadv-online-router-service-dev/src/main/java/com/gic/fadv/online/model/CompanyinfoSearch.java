package com.gic.fadv.online.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CompanyinfoSearch {
	private String name;
	private String address;
	private String contact;
	private String domain;
	private String contexts;
	private String services;
	/*
	 * private String user;
	 * 
	 * @JsonProperty("auth_token") private String authToken;
	 */
}
