package com.gic.fadv.verification.event.model;

public interface VerificationAuditTrail {
	String getRequestNo();
	String getCheckId();
	String getCaseNo();
	String getCandidateName();
	String getClientName();
	String getPackageName();
	String getClientCode();
	String getEventName();
	String getCaseStatus();
	String getCreateDate();
	String getDay();
}
