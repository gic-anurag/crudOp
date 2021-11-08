package com.gic.fadv.verification.ae.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.gic.fadv.verification.ae.model.RouterHistory;

@Transactional
public interface RouterHistoryRepository extends JpaRepository<RouterHistory, Long> {
	List<RouterHistory> findByCheckIdAndEngineName(String checkId, String engineName);

	@Query(value = "SELECT DISTINCT COUNT(instruction_check_id) FROM {h-schema}case_specific_record_detail WHERE user_id = :userId", nativeQuery = true)
	int getAllocatedTaskByUserId(Long userId);

	@Query(value = "SELECT DISTINCT COUNT(instruction_check_id) FROM {h-schema}router_history A, {h-schema}case_specific_record_detail B "
			+ "WHERE A.check_id = B.instruction_check_id AND B.user_id = :userId AND A.current_engine_status = 'Verified'", nativeQuery = true)
	int getCompletedTaskByUserId(Long userId);

	@Query(value = "select * from verification.router_history rh where rh.engine_name =:engineName and rh.current_engine_status ='Failed' "
			+ "and rh.check_id not in (select rh.check_id from verification.router_history rh where rh.engine_name ='SPOC' "
			+ "and rh.current_engine_status in ('Processed', 'Initiated')) AND "
			+ "CAST(DATE(rh.start_time) AS VARCHAR) BETWEEN SYMMETRIC :fromDate AND :toDate AND "
			+ "CASE WHEN :checkId !=  '' THEN rh.check_id = :checkId ELSE TRUE END AND "
			+ "CASE WHEN :caseNumber !=  '' THEN rh.case_number = :caseNumber ELSE TRUE END "
			+ "order by check_id", nativeQuery = true)
	List<RouterHistory> getAllFailedRouterHistory(String engineName, String fromDate, String toDate, String caseNumber,
			String checkId);

	@Query(value = "select * from verification.router_history rh where LOWER(rh.engine_name) = LOWER(:engineName) "
			+ "AND CAST(DATE(rh.start_time) AS VARCHAR) BETWEEN SYMMETRIC :fromDate AND :toDate AND "
			+ "CASE WHEN :checkId !=  '' THEN rh.check_id = :checkId ELSE TRUE END AND "
			+ "CASE WHEN :caseNumber !=  '' THEN rh.case_number = :caseNumber ELSE TRUE END AND "
			+ "CASE WHEN :engineStatus != '' THEN  LOWER(rh.current_engine_status) = LOWER(:engineStatus) ELSE TRUE END "
			+ "order by check_id", nativeQuery = true)
	List<RouterHistory> getFilteredRouterHistory(String engineName, String fromDate, String toDate, String caseNumber,
			String checkId, String engineStatus);

	@Query(value = "SELECT DISTINCT rh.current_engine_status FROM verification.router_history rh WHERE rh.current_engine_status IS NOT NULL AND "
			+ " rh.current_engine_status <> '' AND LOWER(rh.engine_name) = LOWER(:engineName)", nativeQuery = true)
	List<String> getRouterStatusListByEngineName(String engineName);

	@Query(value = "select distinct engine_name from verification.router_history rh  ", nativeQuery = true)
	List<String> getRouterEngineNameList();
}
