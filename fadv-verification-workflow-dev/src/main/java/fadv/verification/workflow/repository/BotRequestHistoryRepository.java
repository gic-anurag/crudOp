package fadv.verification.workflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import fadv.verification.workflow.model.BotRequestHistory;

@Transactional
public interface BotRequestHistoryRepository extends JpaRepository<BotRequestHistory, Long> {
	List<BotRequestHistory> findByCaseNumber(String caseNumber);

	@Query(value = "SELECT * FROM {h-schema}bot_request_history WHERE request_status IN (:requestStatus) "
			+ "AND retry_count <= :retryCount", nativeQuery = true)
	List<BotRequestHistory> getNotProcessedRequests(List<String> requestStatus, int retryCount);
	
	@Query(value = "SELECT * FROM {h-schema}bot_request_history WHERE request_status IN (:requestStatus)"
			+ " AND (email_sent <> :emailStatus OR email_sent IS NULL) AND retry_count > :retryCount", nativeQuery = true)
	List<BotRequestHistory> getFailedRequests(List<String> requestStatus, String emailStatus, int retryCount);
}
