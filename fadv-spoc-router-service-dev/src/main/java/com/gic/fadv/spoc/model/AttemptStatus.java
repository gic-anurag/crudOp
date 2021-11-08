package com.gic.fadv.spoc.model;

import java.util.Date;

import lombok.Data;

@Data
public class AttemptStatus {

	/*
	 * @Id
	 * 
	 * @GeneratedValue(strategy = GenerationType.IDENTITY)
	 * */
	
	private Long attemptStatusid;
	private String attempStatus;
	private String attemptType;
	private String userid;
	private Date createDate = new Date();
}
