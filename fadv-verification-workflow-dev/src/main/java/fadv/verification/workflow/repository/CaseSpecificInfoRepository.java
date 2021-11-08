package fadv.verification.workflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import fadv.verification.workflow.model.CaseSpecificInfo;

@Transactional
public interface CaseSpecificInfoRepository extends JpaRepository<CaseSpecificInfo, Long> {
	
	List<CaseSpecificInfo> findByCaseNumberOrderByCaseSpecificIdDesc(String caseNumber);
}
