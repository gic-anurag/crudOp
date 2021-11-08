package fadv.verification.workflow.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import fadv.verification.workflow.model.L3ApiRequestHistory;

@Transactional
public interface L3ApiRequestHistoryRepository extends JpaRepository<L3ApiRequestHistory, Long> {

	L3ApiRequestHistory findByCheckIdAndRequestType(String checkId, String type);

}
