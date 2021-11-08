package com.gic.fadv.online.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PersonInfoSearch {
	private String name;
	private String address;
	private String din;
	private String dob;
	private String father_name;
	private String services;
	private String contexts;
	private String state;
	private String startdate;
	private String verify_id;
	private String api;
	//For WorldCheck
	private String gender;
	private String countryAcr;
	/*
	 * private String user;
	 * 
	 * @JsonProperty("auth_token") private String authToken;
	 */
}
