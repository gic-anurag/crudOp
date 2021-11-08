package com.gic.fadv.online.model;

import java.util.Date;

import lombok.Data;

@Data
public class AttemptStatusData {

	/*
	 * @Id
	 * 
	 * @GeneratedValue(strategy = GenerationType.IDENTITY)
	 * */
	
	private Long statusId;
	private Long attemptId;
	private Long endstatusId;
	private Long modeId;
	private Long depositionId;
	private Date dateOfCreation = new Date();
}
