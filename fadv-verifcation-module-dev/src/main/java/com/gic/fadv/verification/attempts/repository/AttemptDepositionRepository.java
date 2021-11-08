package com.gic.fadv.verification.attempts.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gic.fadv.verification.attempts.model.AttemptDeposition;

public interface AttemptDepositionRepository extends JpaRepository<AttemptDeposition, Long> {

	AttemptDeposition findTopByDepositionName(String depositionName);
}
