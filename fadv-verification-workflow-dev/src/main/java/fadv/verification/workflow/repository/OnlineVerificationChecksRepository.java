package fadv.verification.workflow.repository;

import java.util.List;

import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import fadv.verification.workflow.model.OnlineVerificationChecks;

@Transactional
public interface OnlineVerificationChecksRepository extends JpaRepository<OnlineVerificationChecks, Long> {
	
	List<OnlineVerificationChecks> findByCheckIdOrderByOnlineVerificationCheckIdDesc(String checkId);
}
