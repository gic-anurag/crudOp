package com.gic.fadv.verification.attempts.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gic.fadv.verification.attempts.model.AttemptFollowupMaster;

public interface AttemptFollowupMasterRepository extends JpaRepository<AttemptFollowupMaster, Long> {

	AttemptFollowupMaster findTopByFollowupStatus(String followupStatus);
}
