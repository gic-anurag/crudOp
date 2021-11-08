package fadv.verification.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import fadv.verification.workflow.model.VerificationEventStatus;

@Transactional
public interface VerificationEventStatusRepository extends JpaRepository<VerificationEventStatus, Long> {
	
}
