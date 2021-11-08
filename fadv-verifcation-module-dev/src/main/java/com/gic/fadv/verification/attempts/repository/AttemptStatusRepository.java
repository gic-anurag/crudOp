package com.gic.fadv.verification.attempts.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gic.fadv.verification.attempts.model.AttemptStatus;

public interface AttemptStatusRepository extends JpaRepository<AttemptStatus, Long> {

	AttemptStatus findTopByAttemptStatus(String attemptStatus);
}
