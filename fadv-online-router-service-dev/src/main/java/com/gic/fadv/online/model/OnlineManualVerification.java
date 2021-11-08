package com.gic.fadv.online.model;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class OnlineManualVerification {
	  private Long onlineManualVerificationId;	
	  private String sBU; 
	  private String packageName;
	  private String clientName; 
	  private String timeCreation;
	  private String updatedTime;
	  private String caseNumber;
	  private String status;
	  private String crn;
	  private String candidateName;
	  private List<OnlineVerificationChecks> onlineVerificationChecksList;
	  private String dataEntryResult;
}
