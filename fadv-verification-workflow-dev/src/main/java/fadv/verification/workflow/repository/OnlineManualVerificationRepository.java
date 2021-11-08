package fadv.verification.workflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import fadv.verification.workflow.model.OnlineManualVerification;

public interface OnlineManualVerificationRepository extends JpaRepository<OnlineManualVerification, Long> {
	
	List<OnlineManualVerification> findByCaseNumberOrderByOnlineManualVerificationIdDesc(String caseNumber);
}
