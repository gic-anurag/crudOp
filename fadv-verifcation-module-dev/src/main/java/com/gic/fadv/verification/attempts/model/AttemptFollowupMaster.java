package com.gic.fadv.verification.attempts.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class AttemptFollowupMaster {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long followupId;
	private String followupStatus;
	private String followupDescription;
	private String actionType;
	private String relationToCspi;
	private String checkFlow;
	private String comments;
	private String userid;
	private Date createDate=new Date();
	private Integer isActive;

}
