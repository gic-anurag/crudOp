package com.gic.fadv.verification.online.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import com.gic.fadv.verification.online.model.OnlineVerificationChecks;
import com.gic.fadv.verification.pojo.L3CaseDetails;
import com.gic.fadv.verification.pojo.OnlineChecksLogPOJO;
import com.gic.fadv.verification.pojo.QuestionnairePOJO;
import com.gic.fadv.verification.pojo.VerifyChecksPOJO;

@Transactional
public interface OnlineVerificationChecksRepository extends JpaRepository<OnlineVerificationChecks, Long> {
	List<OnlineVerificationChecks> findByOnlineManualVerificationIdOrderByApiNameAsc(Long onlineManualVerificationId);

	List<OnlineVerificationChecks> findByApiName(String apiName);

	List<OnlineVerificationChecks> findByOnlineManualVerificationId(Long onlineManualVerificationId);

	List<OnlineVerificationChecks> findByCheckIdAndApiNameOrderByOnlineVerificationCheckIdDesc(String checkId,
			String apiName);

	@Query(value = "SELECT A.checkid AS checkId, CAST(C.case_reference as varchar) caseReference,  F.followup_status AS ngStatus, "
			+ "F.followup_description AS ngStatusDescription, F.relation_to_cspi AS endStatusOfTheVerification, "
			+ "A.attempt_description AS internalNotes, E.deposition_name AS disposition, B.component_name AS componentName, "
			+ "A.name AS verifierName, A.job_title AS verifierDesignation, A.source_phone AS verifierNumber, "
			+ "A.email_address AS emailId, A.executive_summary AS executiveSummaryComments, H.attempt_type AS verificationAttemptType, "
			+ "C.package_name AS packageName, C.sbu_name AS sbuName, B.product AS productName, G.verification_mode AS modeOfVerification, "
			+ "A.closure_expected_date AS expectedClosureDate, A.followup_date AS followUpDate "
			+ "FROM {h-schema}attempt_history AS A " + "INNER JOIN {h-schema}case_specific_record_detail AS B "
			+ "ON A.checkid = B.instruction_check_id " + "INNER JOIN {h-schema}case_specific_info AS C "
			+ "ON B.case_specific_id = C.case_specific_id " + "INNER JOIN {h-schema}attempt_status_data AS D "
			+ "ON A.attemptid = D.attempt_id " + "INNER JOIN {h-schema}attempt_deposition AS E "
			+ "ON D.deposition_id = E.deposition_id " + "INNER JOIN {h-schema}attempt_followup_master AS F "
			+ "ON D.endstatus_id = F.followup_id " + "INNER JOIN {h-schema}attempt_verification_modes AS G "
			+ "ON D.mode_id = G.verification_mode_id " + "INNER JOIN {h-schema}attempt_status AS H "
			+ "ON A.attempt_statusid = H.attempt_statusid " + "WHERE A.checkid = :checkId "
			+ "ORDER BY A.attemptid DESC LIMIT 1", nativeQuery = true)
	List<VerifyChecksPOJO> findAllVerifyChecks(String checkId);

	@Query(value = "SELECT global_question_id AS caseQuestionRefID,  question_name AS question, "
			+ "application_data AS answer, status, verified_data AS verifiedData, report_comments AS reportData "
			+ "FROM {h-schema}attempt_questionnaire WHERE check_id = :checkId", nativeQuery = true)
	List<QuestionnairePOJO> findAllQuestionnaire(String checkId);

	@Query(value = "SELECT B.instruction_check_id AS checkId, "
			+ "A.sbu_name AS sbuName, A.package_name AS packageName, "
			+ "B.product AS productName, B.component_name AS componentName, "
			+ "CAST(A.case_reference AS VARCHAR) caseReference " + "FROM {h-schema}case_specific_info AS A "
			+ "INNER JOIN {h-schema}case_specific_record_detail AS B " + "ON A.case_specific_id = B.case_specific_id "
			+ "WHERE B.instruction_check_id = :checkId AND B.component_name = :componentName "
			+ "LIMIT 1", nativeQuery = true)
	L3CaseDetails getAllL3Cases(String checkId, String componentName);

	@Query(value = "SELECT result FROM "
			+ "{h-schema}online_verification_checks WHERE check_id = :checkId", nativeQuery = true)
	List<String> getAllChecksByCheckId(String checkId);

	@Query(value = "SELECT DISTINCT A.case_number AS caseNumber, A.crn_no AS crnNo, " + "B.check_id AS checkId, "
			+ "A.candidate_name AS candidateName, A.secondary_name AS secondaryName, A.client_name AS clientName, "
			+ "A.sbu, A.package_name AS packageName, " + "B.api_name AS apiName, B.initial_result AS initialResult, "
			+ "B.result, B.created_date as createdDate,B.updated_date as updatedDate " + "FROM {h-schema}online_manual_verification AS A "
			+ "INNER JOIN {h-schema}online_verification_checks AS B "
			+ "ON A.online_manual_verification_id = B.online_manual_verification_id "
			+ "WHERE CAST(DATE(B.updated_date) AS VARCHAR) BETWEEN SYMMETRIC ?1 AND ?2 AND " + "CASE "
			+ "WHEN ?3 !=  '' THEN A.case_number = ?3 " + "ELSE TRUE " + "END AND " + "CASE "
			+ "WHEN ?4 !=  '' THEN A.crn_no = ?4 " + "ELSE TRUE " + "END", nativeQuery = true)
	List<OnlineChecksLogPOJO> getVerifyChecksLog(String fromDate, String toDate, String caseNumber, String crnNo);

	@Query(value = "SELECT * FROM {h-schema}online_verification_checks "
			+ "WHERE result NOT IN (:status) ", nativeQuery = true)
	List<OnlineVerificationChecks> getVerifyChecksByResult(List<String> status);

	@Modifying(clearAutomatically = true)
	@Query(value = "UPDATE {h-schema}online_verification_checks "
			+ "SET matched_identifiers=:matchedIdentifier, output_file=:outputFile, "
			+ "result = :finalResult, updated_date=:updateDate, pending_status=:isPending, "
			+ "retry_no=:retryNo, verify_id=:verifyId "
			+ "WHERE check_id = :checkId AND api_name = :apiName", nativeQuery = true)
	void updateVerifyChecks(String checkId, String finalResult, String updateDate, String isPending, String retryNo,
			String verifyId, String matchedIdentifier, String outputFile, String apiName);

	@Modifying(clearAutomatically = true)
	@Query(value = "UPDATE {h-schema}online_verification_checks AS A " + " SET result = :worldCheckResult "
			+ " FROM {h-schema}online_manual_verification B "
			+ " WHERE A.online_manual_verification_id = B.online_manual_verification_id "
			+ " AND B.crn_no = :crnNo AND A.api_name = :apiName", nativeQuery = true)
	void updateVerifyChecks(String crnNo, String worldCheckResult, String apiName);

	@Query(value = "SELECT DISTINCT B.check_id FROM {h-schema}online_manual_verification A, "
			+ "{h-schema}online_verification_checks B "
			+ "WHERE A.online_manual_verification_id = B.online_manual_verification_id "
			+ "AND A.crn_no = :crnNo AND B.api_name = :apiName AND B.sent_tol3 IS NOT TRUE", nativeQuery = true)
	List<String> getCheckIdList(String crnNo, String apiName);

	List<OnlineVerificationChecks> findByOnlineManualVerificationIdAndApiName(Long id, String apiName);

	@Query(value = "SELECT CAST(B.input_file AS VARCHAR) FROM {h-schema}online_manual_verification A, "
			+ "{h-schema}online_verification_checks B "
			+ "WHERE A.online_manual_verification_id = B.online_manual_verification_id "
			+ "AND B.api_name = 'Manupatra' AND A.online_manual_verification_id = :id " + "LIMIT 1", nativeQuery = true)
	String getSecondaryName(Long id);

	@Query(value = "SELECT * FROM {h-schema}online_verification_checks "
			+ "WHERE check_id IN (:checkIdList)", nativeQuery = true)
	List<OnlineVerificationChecks> getVerificationChecksByCheckIdList(List<String> checkIdList);

	@Modifying(clearAutomatically = true)
	@Query(value = "UPDATE {h-schema}online_verification_checks "
			+ "SET matched_identifiers=:matchedIdentifier, output_file=:outputFile, "
			+ "result = :finalResult, updated_date=:updateDate, pending_status=:isPending, "
			+ "retry_no=:retryNo, verify_id=:verifyId, online_manual_verification_id = :onlineManualId "
			+ "WHERE check_id = :checkId AND api_name = :apiName", nativeQuery = true)
	void updateVerifyChecksbyCheckId(String checkId, String finalResult, String updateDate, String isPending,
			String retryNo, String verifyId, String matchedIdentifier, String outputFile, Long onlineManualId,
			String apiName);

	@Query(value = "SELECT DISTINCT B.check_id FROM " + "{h-schema}online_manual_verification A "
			+ "INNER JOIN {h-schema}online_verification_checks B "
			+ "ON A.online_manual_verification_id = B.online_manual_verification_id "
			+ "WHERE A.case_number = :caseNumber", nativeQuery = true)
	List<String> getOnlyCheckIdList(String caseNumber);

	@Query(value = "SELECT * FROM {h-schema}online_verification_checks WHERE online_manual_verification_id = :onlineManualVerificationId "
			+ "AND check_id IN (:checkIds)", nativeQuery = true)
	List<OnlineVerificationChecks> getByProductListAndOnlineManualVerificationId(Long onlineManualVerificationId,
			List<String> checkIds);

	@Query(value="SELECT * FROM {h-schema}online_verification_checks A WHERE A.api_name IN "
			+ "(SELECT A.api_name FROM {h-schema}online_verification_checks A "
			+ "WHERE A.online_verification_check_id = :onlineVerificationCheckId) AND "
			+ "A.online_manual_verification_id IN (SELECT A.online_manual_verification_id "
			+ "FROM verification.online_verification_checks A "
			+ "WHERE A.online_verification_check_id = :onlineVerificationCheckId)", nativeQuery=true)
	List<OnlineVerificationChecks> getOnlineVerificationChecksById(Long onlineVerificationCheckId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="UPDATE {h-schema}online_verification_checks SET sent_tol3 = true where check_id in (:checkIdList)", nativeQuery=true)
	void updateL3StatusUsingCheckIdList(List<String> checkIdList);
	
	@Modifying(clearAutomatically = true)
	@Query(value="UPDATE {h-schema}online_verification_checks SET result = 'Clear' where check_id in (:checkIdList)", nativeQuery=true)
	void updatedL3StatusUsingCheckIdList(List<String> checkIdList);
	
	//Add Logic for Clear and Sent to L3
	//List<OnlineVerificationChecks> findByIdAndSentToL3(Long id,boolean sentToL3);
}
