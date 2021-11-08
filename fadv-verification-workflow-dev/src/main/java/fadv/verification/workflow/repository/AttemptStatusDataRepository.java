package fadv.verification.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import fadv.verification.workflow.model.AttemptStatusData;

@Transactional
public interface AttemptStatusDataRepository extends JpaRepository<AttemptStatusData, Long> {
	
}
