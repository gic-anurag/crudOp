package com.gic.fadv.verification.online.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.gic.fadv.verification.online.model.OnlineManualVerification;

@Transactional
public interface OnlineManualVerificationRepository extends JpaRepository<OnlineManualVerification, Long> {
	List<OnlineManualVerification> findDistinctByStatusOrderByUpdatedTimeDesc(String status);

	List<OnlineManualVerification> findByCaseNumber(String caseNumber);

	@Query(value = "SELECT DISTINCT A.* FROM {h-schema}online_manual_verification AS A "
			+ "WHERE LOWER(A.status) = 'manual' AND "
			+ "CASE WHEN :fromDate != '' THEN CAST(DATE(A.time_creation) AS VARCHAR) >= :fromDate ELSE TRUE END AND "
			+ "CASE WHEN :toDate != '' THEN CAST(DATE(A.time_creation) AS VARCHAR) <= :toDate ELSE TRUE END AND "
			+ "CASE WHEN :caseNumber != '' THEN A.case_number = :caseNumber ELSE TRUE END AND "
			+ "CASE WHEN :caseNumber != '' THEN A.case_number = :caseNumber ELSE TRUE END AND "
			+ "CASE WHEN :crnNo != '' THEN A.crn_no = :crnNo ELSE TRUE END AND "
			+ "CASE WHEN :clientName != '' THEN A.client_name LIKE :clientName ELSE TRUE END AND "
			+ "CASE WHEN :candidateName != '' THEN A.candidate_name LIKE :candidateName ELSE TRUE END AND "
			+ "CASE WHEN :sbu != '' THEN A.sbu LIKE :sbu ELSE TRUE END AND "
			+ "CASE WHEN :packageName != '' THEN A.package_name LIKE :packageName ELSE TRUE END "
			+ "ORDER BY A.updated_time DESC ", nativeQuery = true)
	List<OnlineManualVerification> getByFilter(String fromDate, String toDate, String caseNumber, String clientName,
			String crnNo, String candidateName, String sbu, String packageName);

	@Query(value = "SELECT B.* FROM {h-schema}online_verification_checks A "
			+ "INNER JOIN {h-schema}online_manual_verification B "
			+ "ON A.online_manual_verification_id = B.online_manual_verification_id "
			+ "WHERE A.check_id = :checkId LIMIT 1", nativeQuery = true)
	List<OnlineManualVerification> getByCheckId(String checkId);

	@Query(value = "SELECT DISTINCT B.sub_component_name FROM {h-schema}online_manual_verification A, "
			+ "{h-schema}online_verification_checks B "
			+ "WHERE A.online_manual_verification_id = B.online_manual_verification_id "
			+ "AND A.online_manual_verification_id=:onlineManualVerificationId ORDER BY B.sub_component_name", nativeQuery = true)
	List<String> getProductNameListByCaseNumber(Long onlineManualVerificationId);

	@Query(value = "SELECT DISTINCT B.check_id FROM {h-schema}online_manual_verification A, "
			+ "{h-schema}online_verification_checks B "
			+ "WHERE A.online_manual_verification_id = B.online_manual_verification_id "
			+ "AND A.online_manual_verification_id=:onlineManualVerificationId", nativeQuery = true)
	List<String> getCheckIdListByCaseNumber(Long onlineManualVerificationId);

	@Modifying(clearAutomatically = true)
	@Query(value = "UPDATE verification.online_manual_verification A SET secondary_name = :secondaryName "
			+ "WHERE online_manual_verification_id = :onlineManualVerificationId", nativeQuery = true)
	void updateSecondaryName(String secondaryName, Long onlineManualVerificationId);
}
