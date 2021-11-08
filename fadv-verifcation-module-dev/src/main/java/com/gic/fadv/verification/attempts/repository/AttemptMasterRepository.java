package com.gic.fadv.verification.attempts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gic.fadv.verification.attempts.model.AttemptHistory;
import com.gic.fadv.verification.attempts.pojo.ContactAttemptsPOJO;
import com.gic.fadv.verification.attempts.pojo.MiRqDataPOJO;

@Repository
@Transactional
public interface AttemptMasterRepository extends JpaRepository<AttemptHistory, Long> {

	List<AttemptHistory> findByCheckid(String checkId);
	
	List<AttemptHistory> findByCheckidOrderByAttemptidDesc(String checkId);

	@Query(value = "SELECT A.attemptid AS attempId, A.requestid AS requestId, "
			+ "A.name AS sourceName, A.job_title AS jobTitle, A.source_phone AS sourcePhone, "
			+ "A.contact_date AS contactDate, A.followup_date AS followUpDate, "
			+ "B.attempt_status AS attemptStatus, A.number_provided AS numberProvided, "
			+ "A.closure_expected_date AS expectedClosureDate, A.attempt_description AS attemptDescription, "
			+ "A.fax_number AS faxNumber, A.email_address AS emailId, A.is_current AS isCurrent "
			+ "FROM {h-schema}attempt_history AS A " + "INNER JOIN {h-schema}attempt_status AS B "
			+ "ON A.attempt_statusid = B.attempt_statusid "
			+ "WHERE B.attempt_status LIKE '%Email%' OR B.attempt_status LIKE '%Fax%' "
			+ "OR B.attempt_status LIKE '%Call%' OR B.attempt_status LIKE '%Letter%' "
			+ "AND A.checkid = :checkId", nativeQuery = true)
	List<ContactAttemptsPOJO> getContactAttempts(String checkId);

	@Query(value = "SELECT A.followup_date AS followUpDate, A.closure_expected_date AS expectedCosureDate, "
			+ "A.checkid AS checkId, C.followup_status AS followUpStatus, "
			+ "C.followup_description AS followUpDescription, A.l3status AS l3Status, "
			+ "D.component_name AS componentName, E.sbu_name AS sbuName, D.product AS productName, "
			+ "E.package_name AS packageName, cast(E.case_reference AS varchar) caseReference, "
			+ "A.email_address AS emailId, A.executive_summary AS executiveSummaryComments, "
			+ "A.name AS verifierName, A.job_title AS verifierDesignation, A.source_phone AS verifierNumber, "
			+ "G.verification_mode AS modeOfVerification, H.attempt_type AS verificationAttemptType, "
			+ "C.relation_to_cspi AS endStatusOfTheVerification, A.attempt_description AS internalNotes, "
			+ "F.deposition_name AS disposition, CAST(D.component_record_field AS VARCHAR) componentRecordField, "
			+ "CAST(A.additional_info AS VARCHAR) additionalInfo "
			+ "FROM {h-schema}attempt_history A "
			+ "INNER JOIN {h-schema}attempt_status_data B ON A.attemptid = B.attempt_id "
			+ "INNER JOIN {h-schema}attempt_followup_master C ON B.endstatus_id = C.followup_id "
			+ "INNER JOIN {h-schema}case_specific_record_detail D "
			+ "ON A.requestid = D.case_specific_detail_id "
			+ "INNER JOIN {h-schema}case_specific_info E "
			+ "ON D.case_specific_id = E.case_specific_id "
			+ "INNER JOIN {h-schema}attempt_deposition AS F ON B.deposition_id = F.deposition_id "
			+ "INNER JOIN {h-schema}attempt_verification_modes AS G "
			+ "ON B.mode_id = G.verification_mode_id INNER JOIN {h-schema}attempt_status AS H "
			+ "ON A.attempt_statusid = H.attempt_statusid "
			+ "WHERE A.checkid = :checkId AND (A.l3status <> :l3Status OR A.l3status IS NULL) "
			+ "AND C.followup_status IN (:followUpStatus)", nativeQuery = true)
	List<MiRqDataPOJO> getAllDataForMIReq(List<String> followUpStatus, String checkId, String l3Status);

	@Modifying
	@Query(value = "UPDATE {h-schema}attempt_history AS A " + "SET l3status = :l3Status, l3response = :l3Response "
			+ "FROM {h-schema}attempt_status_data B, {h-schema}attempt_followup_master C "
			+ "WHERE A.attemptid = B.attempt_id " + "AND  B.endstatus_id = C.followup_id "
			+ "AND C.followup_status IN (:followUpStatus) " + "AND A.checkid = :checkId", nativeQuery = true)
	void updateAttemptL3Status(List<String> followUpStatus, String checkId, String l3Status, String l3Response);
}
