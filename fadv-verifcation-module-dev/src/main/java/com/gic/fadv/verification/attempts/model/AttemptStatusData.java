package com.gic.fadv.verification.attempts.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import lombok.Data;
@Entity
@Data
public class AttemptStatusData {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long statusId;
	private long attemptId;
	private long endstatusId;
	private long modeId;
	private long depositionId;
	private Date dateOfCreation=new Date();
	
	@OneToOne
	@JoinColumn(name = "attemptId", insertable = false, updatable = false)
	private AttemptHistory attemptHistory;

	@OneToOne
	@JoinColumn(name = "endstatusId", insertable = false, updatable = false)
	private AttemptFollowupMaster attemptFollowupMaster;

	@OneToOne
	@JoinColumn(name = "modeId", insertable = false, updatable = false)
	private AttemptVerificationModes attemptVerificationModes;
	
	@OneToOne
	@JoinColumn(name = "depositionId", insertable = false, updatable = false)
	private AttemptDeposition attemptDeposition;
	
}
