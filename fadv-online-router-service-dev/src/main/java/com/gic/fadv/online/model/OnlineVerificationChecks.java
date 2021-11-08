package com.gic.fadv.online.model;

import lombok.Data;
@Data
public class OnlineVerificationChecks {
	 private Long onlineVerificationCheckId;
	 private Long onlineManualVerificationId;
	 private String checkId;
	 private String apiName;
	 private String result;
	 private String initialResult;
	 private String matchedIdentifiers;
	 private String inputFile;
	 private String outputFile;
	 private String createdDate;
	 private String updatedDate;
	 private String verifyId;
	 private String pendingStatus;
	 private String retryNo;
}
