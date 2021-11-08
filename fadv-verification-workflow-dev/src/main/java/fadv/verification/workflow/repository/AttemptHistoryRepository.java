package fadv.verification.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import fadv.verification.workflow.model.AttemptHistory;

@Transactional
public interface AttemptHistoryRepository extends JpaRepository<AttemptHistory, Long> {
	
}
