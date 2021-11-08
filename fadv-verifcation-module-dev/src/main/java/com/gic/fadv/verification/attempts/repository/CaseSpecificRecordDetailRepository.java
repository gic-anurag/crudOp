package com.gic.fadv.verification.attempts.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gic.fadv.verification.ae.interfaces.CaseSpecificInterface;
import com.gic.fadv.verification.attempts.model.CaseSpecificRecordDetail;
import com.gic.fadv.verification.event.model.VerificationAuditTrail;
import com.gic.fadv.verification.word.pojo.WordFileDetailsPOJO;

@Transactional
public interface CaseSpecificRecordDetailRepository extends JpaRepository<CaseSpecificRecordDetail, Long> {
	List<CaseSpecificRecordDetail> findByInstructionCheckId(String checkId);
	
	
	@Query(value = "SELECT A.request_id AS requestNo, A.check_id AS checkId, A.case_no AS caseNo,  "
			+ "C.candidate_name AS candidateName, C.client_name AS clientName, C.package_name AS packageName, "
			+ "C.client_code AS clientCode, A.event_name AS eventName, B.case_specific_record_status AS caseStatus, "
			+ "A.created_date_time AS createDate,'day-0' as day "
			+ "FROM \"verification\".\"verification_event_status\" AS A "
			+ "INNER JOIN \"verification\".\"case_specific_record_detail\" AS B "
			+ "ON A.request_id = B.case_specific_detail_id "
			+ "INNER JOIN \"verification\".\"case_specific_info\" AS C " + "ON B.case_specific_id = C.case_specific_id "
			+ "WHERE A.check_id = :checkId OR A.request_id = :requestNo " + "OR C.candidate_name LIKE :candidateName  "
			+ " OR C.client_name LIKE :clientName OR C.package_name LIKE :packageName", nativeQuery = true)
	List<VerificationAuditTrail> getAuditTrail(String checkId, Long requestNo, String candidateName, String clientName,
			String packageName);

	@Query(value = "SELECT A.instruction_check_id AS checkId, B.candidate_name AS candidateName,  "
			+ "CAST(A.component_record_field AS VARCHAR) componentRecordField "
			+ "FROM \"verification\".\"case_specific_record_detail\" AS A  "
			+ "INNER JOIN \"verification\".\"case_specific_info\" AS B " + "ON A.case_specific_id = B.case_specific_id "
			+ "WHERE A.component_name = 'Education' " + "AND A.case_specific_detail_id = :requestNo  "
			+ "AND A.instruction_check_id = :checkId LIMIT 1", nativeQuery = true)
	WordFileDetailsPOJO getWordFileDetails(String checkId, Long requestNo);

	@Modifying(clearAutomatically = true)
	@Query(value = "UPDATE {h-schema}case_specific_record_detail "
			+ "SET check_status = 'verified' WHERE instruction_check_id = :checkId", nativeQuery = true)
	void updateCaseVerified(String checkId);

	// Vijay Logic

	List<CaseSpecificRecordDetail> findByCheckAllocationStatus(String checkAllocationStatus);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update CaseSpecificRecordDetail caseRecord set caseRecord.checkAllocationStatus = :aeStatus where caseRecord.caseSpecificDetailId = :caseId")
	void updateCheckAllocationStatus(@Param("caseId") Long caseSpecificDetailId, @Param("aeStatus") String aeStatus);

	CaseSpecificRecordDetail findTopByInstructionCheckId(String checkId);

	List<CaseSpecificRecordDetail> findByIsCheckManual(Boolean isCheckManual);

	List<CaseSpecificRecordDetail> findByCheckAllocationStatusAndIsCheckManual(String defaultStatus,
			Boolean isCheckManual);

	@Query(value = "SELECT B.case_specific_detail_id AS caseSpecificDetailId, A.case_ref_number AS caseRefNumber, "
			+ "B.instruction_check_id AS instructionCheckId, A.client_name AS clientName, B.component_name AS componentName, "
			+ "B.product, A.candidate_name AS candidateName, B.functional_entity_name AS functionalEntityName, "
			+ "B.entity_location AS entityLocation, B.check_allocation_status AS checkAllocationStatus, "
			+ "B.user_id AS userId FROM {h-schema}case_specific_info A, "
			+ "{h-schema}case_specific_record_detail B WHERE A.case_specific_id = B.case_specific_id "
			+ "AND B.is_check_manual = :isCheckManual", nativeQuery = true)
	List<CaseSpecificInterface> getCaseDetailsByManual(Boolean isCheckManual);

	@Query(value = "SELECT B.case_specific_detail_id AS caseSpecificDetailId, A.case_ref_number AS caseRefNumber, "
			+ "B.instruction_check_id AS instructionCheckId, A.client_name AS clientName, B.component_name AS componentName, "
			+ "B.product, A.candidate_name AS candidateName, B.functional_entity_name AS functionalEntityName, "
			+ "B.entity_location AS entityLocation, B.check_allocation_status AS checkAllocationStatus, "
			+ "B.user_id AS userId FROM {h-schema}case_specific_info A, {h-schema}case_specific_record_detail B, {h-schema}router_history C "
			+ "WHERE B.case_specific_detail_id = C.case_specific_record_detail_id "
			+ "AND A.case_specific_id = B.case_specific_id "
			+ "AND B.user_id = :userId AND B.check_allocation_status = :checkAllocationStatus AND "
			+ "C.current_engine_status = :currentEngineStatus ORDER BY b.check_created_date", nativeQuery = true)
	List<CaseSpecificInterface> getNextCheckDetails(String checkAllocationStatus, String currentEngineStatus,
			Long userId);
	
	@Query(value="SELECT B.case_specific_detail_id AS caseSpecificDetailId, A.case_ref_number AS caseRefNumber, "
			+ "B.instruction_check_id AS instructionCheckId, A.client_name AS clientName, B.component_name AS componentName, "
			+ "B.product, A.candidate_name AS candidateName, B.functional_entity_name AS functionalEntityName, "
			+ "B.entity_location AS entityLocation, B.check_allocation_status AS checkAllocationStatus, "
			+ "B.user_id AS userId FROM {h-schema}case_specific_info A, {h-schema}case_specific_record_detail B "
			+ "WHERE A.case_specific_id = B.case_specific_id "
			+ "AND B.instruction_check_id = :checkId ORDER BY B.check_created_date LIMIT 1", nativeQuery = true)
	List<CaseSpecificInterface> getNextCheckDetailsByCheckId(String checkId);
	
	@Query(value="Select * from {h-schema}case_specific_record_detail where "
			+ "instruction_check_id IN (:checkIdList)", nativeQuery = true)
	List<CaseSpecificRecordDetail> getByCheckIdList(List<String> checkIdList);
}
