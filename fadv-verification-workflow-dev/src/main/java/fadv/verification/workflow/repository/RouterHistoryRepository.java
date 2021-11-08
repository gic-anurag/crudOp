package fadv.verification.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import fadv.verification.workflow.model.RouterHistory;

@Transactional
public interface RouterHistoryRepository extends JpaRepository<RouterHistory, Long> {

	RouterHistory findByCheckIdAndCaseNumber(String checkId, String caseNo);
	
}
