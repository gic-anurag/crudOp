package fadv.verification.workflow.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class VerificationEventStatus {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long verificationEventStatusId;
	private Long requestId;
	private String caseNo;
	private String checkId;
	private String eventType;//Auto/Manual
	private Date createdDateTime = new Date();
	@Column(name = "user_id")
	private Long userId;//System=null else userid (LONG)
	
	/***Added according to Client Suggestion*/
	private String stage;//Vendor Portal, OnlineSuspect, Vendor SPOC etc.
	private String event; //Input file generated,Request Intiated
	private String status; //Day 0 (End Status of Verification) -->change to F1 if SPOC sent mail
	@Column(columnDefinition  = "text")
	private String remarks;
	/***Added according to Client Suggestion*/

}
