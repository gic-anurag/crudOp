package com.gic.fadv.verification.event.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gic.fadv.verification.event.model.VerificationEventStatus;

import com.gic.fadv.verification.pojo.VerificationEventStatusPOJO;

@Service
public interface VerificationEventStatusService {

	public List<VerificationEventStatus> getAllEvents();
	
	public VerificationEventStatus save( VerificationEventStatus verificationEventStatus);
	
	public List<VerificationEventStatus> findByCheckId(String checkId);
	
	public List<VerificationEventStatus> getVerificationEventByFilter(VerificationEventStatusPOJO verificationEventStatusPOJO);
}
