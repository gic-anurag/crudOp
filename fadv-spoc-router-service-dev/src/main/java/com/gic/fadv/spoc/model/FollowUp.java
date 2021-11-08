package com.gic.fadv.spoc.model;

import java.util.Date;

import lombok.Data;

@Data
public class FollowUp {
	/*
	 * @Id
	 * 
	 * @GeneratedValue(strategy = GenerationType.IDENTITY)
	 * 
	 * private Long followupId;
	 */

	private String followupStatus;
	private String followupDescription;
	private String actionType;
	private String relationToCspi;
	private String checkFlow;
	private Long userid;
	private Date createDate = new Date();
	private int isActive;
}
