package fadv.verification.workflow.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import fadv.verification.workflow.model.CaseSpecificRecordDetail;

@Transactional
public interface CaseSpecificRecordDetailRepository extends JpaRepository<CaseSpecificRecordDetail, Long> {

	List<CaseSpecificRecordDetail> findByInstructionCheckIdOrderByCaseSpecificDetailIdDesc(String checkId);
}
