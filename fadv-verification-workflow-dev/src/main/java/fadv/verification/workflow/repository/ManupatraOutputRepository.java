package fadv.verification.workflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import fadv.verification.workflow.model.ManupatraOutput;

@Transactional
public interface ManupatraOutputRepository extends JpaRepository<ManupatraOutput, Long> {
	
	List<ManupatraOutput> findByCheckIdAndTitle(String checkId, String title);
	
}
