package com.gic.fadv.verification.event.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;

@Entity
@Data
public class VerifiedChecks {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long verifyCheckId;
	private String checkId;
	private String akaName;
	private String companyName;
	private Long attemptid;
	private Long caseSpecificId;

}
