package com.gic.fadv.verification.attempts.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gic.fadv.verification.attempts.model.AttemptVerificationModes;

public interface AttemptVerificationModesRepository extends JpaRepository<AttemptVerificationModes, Long> {
	
	AttemptVerificationModes findTopByVerificationMode(String verificationMode);
}
