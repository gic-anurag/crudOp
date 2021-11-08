package com.gic.fadv.verification.repository.event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gic.fadv.verification.event.model.VerificationEventStatus;


@Repository
public interface VerificationEventStatusRepository extends JpaRepository<VerificationEventStatus, Long> {
	
	List<VerificationEventStatus> findByCheckId(String checkId);

}
