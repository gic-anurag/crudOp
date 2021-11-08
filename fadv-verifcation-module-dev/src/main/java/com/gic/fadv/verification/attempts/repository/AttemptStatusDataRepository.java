package com.gic.fadv.verification.attempts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gic.fadv.verification.attempts.model.AttemptStatusData;

public interface AttemptStatusDataRepository extends JpaRepository<AttemptStatusData, Long> {
	
	List<AttemptStatusData> findByAttemptId(Long attemptId);
	
	@Query(value = "SELECT * FROM {h-schema}attempt_status_data AS A, {h-schema}attempt_history AS B, "
			+ "{h-schema}attempt_deposition AS C, {h-schema}attempt_followup_master AS D, "
			+ "{h-schema}attempt_verification_modes AS E " 
			+ "WHERE "
			+ "	A.attempt_id = B.attemptid AND " 
			+ "	A.deposition_id = C.deposition_id AND "
			+ "	A.endstatus_id = D.followup_id AND " + "	A.mode_id = E.verification_mode_id AND "
			+ "	A.status_id = :statusId", nativeQuery = true)
	List<AttemptStatusData> queryByStatusId(@Param("statusId") Long statusId);
	
	@Query(value = "SELECT * FROM {h-schema}attempt_status_data AS A, {h-schema}attempt_history AS B, "
			+ "{h-schema}attempt_deposition AS C, {h-schema}attempt_followup_master AS D, "
			+ "{h-schema}attempt_verification_modes AS E, {h-schema}attempt_status AS F "
			+ "WHERE "
			+ "	A.attempt_id = B.attemptid AND "
			+ "	A.deposition_id = C.deposition_id AND "
			+ "	A.endstatus_id = D.followup_id AND "
			+ "	A.mode_id = E.verification_mode_id AND "
			+ "	B.attempt_statusid = F.attempt_statusid AND "
			+ "	LOWER(F.attempt_type) = LOWER(:attemptType)", nativeQuery = true)
	List<AttemptStatusData> queryByAttemptType(@Param("attemptType") String attemptType);
	
	@Query(value = "SELECT * FROM {h-schema}attempt_status_data AS A, {h-schema}attempt_history AS B, "
			+ "{h-schema}attempt_deposition AS C, {h-schema}attempt_followup_master AS D, "
			+ "{h-schema}attempt_verification_modes AS E, {h-schema}attempt_status AS F "
			+ "WHERE "
			+ "	A.attempt_id = B.attemptid AND "
			+ "	A.deposition_id = C.deposition_id AND "
			+ "	A.endstatus_id = D.followup_id AND "
			+ "	A.mode_id = E.verification_mode_id AND "
			+ "	B.attempt_statusid = F.attempt_statusid", nativeQuery = true)
	List<AttemptStatusData> queryAll();

}
