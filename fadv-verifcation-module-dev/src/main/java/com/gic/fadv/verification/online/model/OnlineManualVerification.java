package com.gic.fadv.verification.online.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class OnlineManualVerification {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long onlineManualVerificationId;
	private String sBU;
	private String packageName;
	private String clientName;
	private String timeCreation;
	private String updatedTime;
	private String caseNumber;
	private String status;
	private String crnNo;
	private String candidateName;
	private String secondaryName;
	private String dataEntryResult;
}
