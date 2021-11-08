package com.gic.fadv.verification.attempts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.gic.fadv.verification.attempts.interfaces.CaseSpecificDetailsInterface;
import com.gic.fadv.verification.attempts.model.CaseSpecificInfo;

@Transactional
public interface CaseSpecificInfoRepository extends JpaRepository<CaseSpecificInfo, Long> {

	List<CaseSpecificInfo> findByCaseNumber(String caseNumber);

	@Query(value = "SELECT * FROM {h-schema}case_specific_info " + "AS A, {h-schema}case_specific_record_detail AS B "
			+ "WHERE A.case_specific_id = B.case_specific_id"
			+ " AND instruction_check_id= :checkId", nativeQuery = true)
	List<CaseSpecificInfo> findByCheckId(String checkId);

	@Query(value = "SELECT DISTINCT A.* " + "FROM {h-schema}case_specific_info AS A "
			+ "INNER JOIN {h-schema}case_specific_record_detail AS B " + "ON A.case_specific_id = B.case_specific_id "
			+ "WHERE CAST(DATE(A.created_date) AS VARCHAR) BETWEEN SYMMETRIC :fromDate AND :toDate AND "
			+ "CASE WHEN :clientName !=  '' THEN LOWER(A.client_name) LIKE LOWER(:clientName) ELSE TRUE END AND "
			+ "CASE WHEN :crnNo !=  '' THEN A.case_ref_number = :crnNo ELSE TRUE END AND "
			+ "CASE WHEN :candidateName !=  '' THEN LOWER(A.candidate_name) Like LOWER(:candidateName) ELSE TRUE END AND "
			+ "CASE WHEN :checkId !=  '' THEN B.instruction_check_id = :checkId ELSE TRUE END AND "
			+ "CASE WHEN :productName !=  '' THEN LOWER(B.product) Like LOWER(:productName) ELSE TRUE END AND "
			+ "CASE WHEN :componentName !=  '' THEN LOWER(B.component_name) Like LOWER(:componentName) ELSE TRUE END", nativeQuery = true)
	List<CaseSpecificInfo> getCaseSpecificInfo(String fromDate, String toDate, String clientName, String crnNo,
			String candidateName, String checkId, String productName, String componentName);

	@Query(value = "SELECT DISTINCT B.case_specific_detail_id AS requestId, A.case_ref_number AS caseReferenceNumber, "
			+ "B.instruction_check_id AS checkId, A.client_name AS clientName, B.component_name AS componentName, "
			+ "B.product AS productName, A.candidate_name AS candidateName, CAST(B.component_record_field AS VARCHAR) componentRecord, "
			+ "B.check_status AS statusOfCheck, CAST(B.check_created_date AS VARCHAR) checkCreatedDate, "
			+ "CAST(B.check_due_date AS VARCHAR) checkDueDate, B.check_tat AS checkTat, A.sbu_name AS sbuName, "
			+ "A.package_name AS packageName, A.case_number AS caseNumber " + "FROM {h-schema}case_specific_info AS A "
			+ "INNER JOIN {h-schema}case_specific_record_detail AS B ON A.case_specific_id = B.case_specific_id "
			+ "WHERE (LOWER(B.check_status) <> 'verified' OR B.check_status IS NULL) AND CAST(DATE(A.created_date) AS VARCHAR) "
			+ "BETWEEN SYMMETRIC :fromDate AND :toDate AND "
			+ "CASE WHEN :clientName !=  '' THEN LOWER(A.client_name) LIKE LOWER(:clientName) ELSE TRUE END AND "
			+ "CASE WHEN :crnNo !=  '' THEN A.case_ref_number = :crnNo ELSE TRUE END AND "
			+ "CASE WHEN :candidateName !=  '' THEN LOWER(A.candidate_name) Like LOWER(:candidateName) ELSE TRUE END AND "
			+ "CASE WHEN :checkId !=  '' THEN B.instruction_check_id = :checkId ELSE TRUE END AND "
			+ "CASE WHEN :productName !=  '' THEN LOWER(B.product) Like LOWER(:productName) ELSE TRUE END AND "
			+ "CASE WHEN :componentName !=  '' THEN LOWER(B.component_name) Like LOWER(:componentName) ELSE TRUE END AND "
			+ "CASE WHEN :userId !=  0 THEN B.user_id = :userId ELSE TRUE END", nativeQuery = true)
	List<CaseSpecificDetailsInterface> getCaseDetailsUsingFilters(String fromDate, String toDate, String clientName,
			String crnNo, String candidateName, String checkId, String productName, String componentName, Long userId);
	
	List<CaseSpecificInfo> findByCaseRefNumber(String caseRefNumber);
}
